import java.io.*;
import java.util.*;

public class Sequential {
    Map<String, Integer> results;

    public Sequential(String filePath) throws IOException {
        List<String> splitted = new ArrayList<>();
        try (BufferedReader  sc = new BufferedReader (new FileReader(filePath))) {
            String line;
            while((line = sc.readLine()) != null){
                splitted.addAll(
                    Arrays.asList(line.split("\\W+"))
                    .stream()
                    .map(string ->  string.toLowerCase())
                    .filter(string -> !string.isEmpty())
                    .toList()
                );
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
        }
        
        HashMap<String, Integer> counts = new HashMap<>();
        for (String word : splitted) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }

        // store count results
        this.results = counts;
    }
}