import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
// import java.util.stream.Stream;

public class FutureBOW {
    Map<String, Integer> results;

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

            // Wait for all the tasks to complete and combine the results
            for (int i = 0; i < numThreads; i++) {
                Map<String, Integer> wordCounts;

                try {
                    wordCounts = futures[i].get();
                    for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
                        String word = entry.getKey();
                        int count = entry.getValue();

                        combinedWordCounts.put(word, combinedWordCounts.getOrDefault(word, 0) + count);
                    }

                } catch (InterruptedException | ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // // another implementation using Stream but even more slower
            // combinedWordCounts = Stream.of(futures)
            //     .map(CompletableFuture::join)
            //     .reduce(new HashMap<>(), (acc, wordCounts) -> {
            //         for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            //             String word = entry.getKey();
            //             int count = entry.getValue();

            //             acc.put(word, acc.getOrDefault(word, 0) + count);
            //         }
            //         return acc;
            //     }, (acc1, acc2) -> {
            //         for (Map.Entry<String, Integer> entry : acc2.entrySet()) {
            //             String word = entry.getKey();
            //             int count = entry.getValue();

            //             acc1.put(word, acc1.getOrDefault(word, 0) + count);
            //         }
            //         return acc1;
            //     });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

        this.results = combinedWordCounts;

    }

    static class WordCountTask {
        ByteBuffer buffer;
        int chunkSize;
        int threadNum;

        public WordCountTask(int threadNum, int size) {
            this.threadNum = threadNum;
            this.chunkSize = size;
            this.buffer = ByteBuffer.allocate(size + 128); // additional 128 to capture cut-off words
        }
        
        public CompletableFuture<Map<String, Integer>> countWords (AsynchronousFileChannel channel){
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
                // do not remove first word is this is first thread
                boolean removeFirstWord = this.threadNum != 0 && !Character.toString(previousChar).matches("\\s");
                Map<String, Integer> wordCounts = new HashMap<>();

                // Read and append
                buffer.position(0); // reset buffer position
                byte[] chars = new byte[this.chunkSize];
                buffer.get(chars, 0, Math.min(this.chunkSize, buffer.remaining()));
                String content = new String(chars);

                // Check if end of pointer is full word, if not read until reach a space or end
                // of file
                int c;
                try {
                    while ((c = buffer.get()) != -1 && !Character.toString(c).matches("\\s")) {
                        content += (char) c;
                    }
                } catch (BufferUnderflowException e) {
                    // do nothing
                } catch (IllegalArgumentException e) {
                    // do nothing, non-ascii character
                }

                // Count word occurrences
                List<String> splitted = Arrays.asList(
                        content.toString().split("\\s+"))
                        .stream()
                        .map(string -> string.toLowerCase())
                        .toList();

                // Add records
                boolean firstWord = true;
                for (String word : splitted) {
                    if (firstWord && removeFirstWord) {
                        // remove first word as it is broken
                        firstWord = false;
                        continue;
                    }
                    firstWord = false;

                    // Break words with non-word (\W) characters before adding
                    // into the map
                    for (String subword : word.split("\\W+")) {
                        if (subword.strip().isEmpty()) {
                            continue;
                        }
                        wordCounts.put(subword, wordCounts.getOrDefault(subword, 0) + 1);
                    }
                }

                return wordCounts;
            });
        }
    }
}