import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FutureBOW implements Results {
    Map<String, Integer> results;

    public FutureBOW(String filePath) {
        this(filePath, 16384);
    }

    public FutureBOW(String filePath, int chunkSize) {

        // Create a map to hold the combined word counts
        Map<String, Integer> combinedWordCounts = new HashMap<>();

        try {
            Path file = Path.of(filePath);
            long filesize = Files.size(file);

            int numThreads = (int) Math.ceil(filesize / (double) chunkSize);

            var channel = AsynchronousFileChannel.open(file);

            @SuppressWarnings("unchecked")
            CompletableFuture<Map<String, Integer>>[] futures = new CompletableFuture[numThreads];
            // start reading file tasks
            for (int i = 0; i < numThreads; i++) {
                WordCountTask task = new WordCountTask(i, chunkSize);
                futures[i] = task.countWords(channel);
            }

            // // Wait for all the tasks to complete and combine the results
            // for (int i = 0; i < numThreads; i++) {
            //     Map<String, Integer> wordCounts;

            //     try {
            //         wordCounts = futures[i].get();
            //         for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            //             String word = entry.getKey();
            //             int count = entry.getValue();

            //             combinedWordCounts.merge(word, count, Integer::sum);
            //         }

            //     } catch (InterruptedException | ExecutionException e) {
            //         e.printStackTrace();
            //     }
            // }

            // another implementation using Stream
            combinedWordCounts = Stream.of(futures)
                .parallel()
                .map(CompletableFuture::join)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    Integer::sum
                ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.results = combinedWordCounts;

    }

    public Map<String, Integer> results() {
        return results;
    }

    static class WordCountTask {
        ByteBuffer buffer;
        int chunkSize;
        int threadNum;
        private final static Pattern wordsplitter = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
        private final static String chunkBoundaryCondition = "\\s";

        public WordCountTask(int threadNum, int size) {
            this.threadNum = threadNum;
            this.chunkSize = size;
            this.buffer = ByteBuffer.allocate(size + 128); // additional 128 to capture cut-off words
        }

        public CompletableFuture<Map<String, Integer>> countWords(AsynchronousFileChannel channel) {
            CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

            channel.read(buffer, threadNum * chunkSize, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    completableFuture.complete(result);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    completableFuture.completeExceptionally(exc);
                }
            });

            return completableFuture.thenApplyAsync((length) -> {
                char previousChar = (char) buffer.get(0);
                // check if word is broken before this (not a space), if so discard the first
                // word before the first space character
                // do not remove first word if this is first thread
                boolean removeFirstWord = this.threadNum != 0
                        && !Character.toString(previousChar).matches(chunkBoundaryCondition);

                // Read and append
                buffer.position(0); // reset buffer position
                byte[] chars = new byte[this.chunkSize];
                buffer.get(chars, 0, Math.min(this.chunkSize, buffer.remaining()));
                String content = new String(chars, StandardCharsets.UTF_8);

                // Check if end of pointer is full word, if not read until reach a space or end
                // of file
                int c;
                try {
                    while ((c = buffer.get()) != -1) {
                        if (c < 0) {
                            // is UTF-8 multibyte sequence, decode manually
                            List<Byte> utf8Bytes = new ArrayList<>();
                            utf8Bytes.add((byte) c);
                            while ((c = buffer.get()) < 0) {
                                utf8Bytes.add((byte) c);
                            }
                            byte[] utf8BytesArray = new byte[utf8Bytes.size()];
                            content += new String(utf8BytesArray, StandardCharsets.UTF_8);
                            continue;
                        }
                        if (Character.toString(c).matches(chunkBoundaryCondition)) {
                            // exit condition
                            break;
                        }
                        content += (char) c;
                    }
                } catch (BufferUnderflowException e) {
                    // do nothing
                }

                // Count word occurrences
                // break at chunkBoundaryCondition because to ensure removing the correct first word
                Stream<String> splitted = Arrays.asList(content.split(chunkBoundaryCondition)).stream();
                
                if (removeFirstWord) {
                    // remove first word as it is broken from previous chunk
                    splitted = splitted.skip(1);
                }

                Map<String, Integer> wordCounts = splitted
                    .flatMap(string -> Arrays.stream(wordsplitter.split(string)))
                    .filter(string -> !string.isEmpty())
                    .collect(Collectors.toMap(String::toLowerCase, x -> 1, Integer::sum));

                return wordCounts;
            });
        }
    }
}