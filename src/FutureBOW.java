import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FutureBOW {
    Map<String, Integer> results;

    public FutureBOW() {
        String filePath = "test text.txt";
        int chunkSize = 256;

        File file = new File(filePath);
        int numThreads = (int) Math.ceil(file.length() / (double) chunkSize);

        // Create the thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Future<Map<String, Integer>>[] futures = new Future[numThreads];
        // Submit the tasks to count word occurrences
        for (int i = 0; i < numThreads; i++) {
            futures[i] = executor.submit(
                new WordCountTask(
                    filePath, 
                    i * chunkSize, 
                    Math.min(file.length(), (i+1)*chunkSize) - 1, 
                    i
                )
            );
            // System.out.printf("%d, %d, run %d\n", i * chunkSize, Math.min(file.length(), (i+1)*chunkSize) - 1, i);
        }

        // Create a map to hold the combined word counts
        Map<String, Integer> combinedWordCounts = new HashMap<>();

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

        executor.shutdown();
        
        // System.out.println(combinedWordCounts.size());
        // System.out.println(combinedWordCounts);
        this.results = combinedWordCounts;

    }

    static class WordCountTask implements Callable<Map<String, Integer>> {
        private String filepath;
        private long start;
        private long end;
        private int i;

        public WordCountTask(String filepath, long start, long end, int i) {
            this.filepath = filepath;
            this.start = start;
            this.end = end;
            this.i = i;
        }

        @Override
        public Map<String, Integer> call() throws Exception {
            Map<String, Integer> wordCounts = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
                // Prepare buffer for string
                StringBuilder content = new StringBuilder();

                boolean removeFirstWord = false;
                if (start != 0) {
                    // Skip to the start position
                    reader.skip(start - 1);
                    char previousChar = (char) reader.read();
                    // check if word is broken before this (not a space), if so discard the first word before the first space character
                    removeFirstWord = !Character.toString(previousChar).matches("\\s");
                    // add back this character since `read` moved pointer forward
                    content.append(previousChar);
                }

                // Calculate the length to read
                int lengthToRead = (int) (this.end - this.start);

                // Read and append
                char[] buffer = new char[lengthToRead];
                reader.read(buffer, 0, lengthToRead);
                content.append(buffer);
                
                // Check if end of pointer is full word, if not read until reach a space or end of file
                int c;
                while ((c = reader.read()) != -1 && !Character.toString(c).matches("\\s")) {
                    content.append((char) c);
                }

                // Count word occurrences
                List<String> splitted = Arrays.asList(
                        content.toString().split("\\s+")
                    )
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

                    if (word.strip().isEmpty()) {
                        continue;
                    }

                    // Check if word contains non-word (\W) characters and break them before adding into the map
                    if (word.matches(".*\\W+.*")) {
                        for (String subword: word.split("\\W+")) {
                            if (subword.strip().isEmpty()) {
                                continue;
                            }
                            wordCounts.put(subword, wordCounts.getOrDefault(subword, 0) + 1);
                        }
                    } else {
                        // normal count
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                    }
                }
        
            } catch (Exception e) {
                e.printStackTrace();
            }

            return wordCounts;
        }
    }
}