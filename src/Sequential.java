import java.io.*;
import java.util.*;

public class Sequential {
    Map<String, Integer> results;

    public Sequential() throws IOException {
        List<String> splitted = new ArrayList<>();
        try (BufferedReader  sc = new BufferedReader (new FileReader("test text.txt"))) {
            String line;
            while((line = sc.readLine()) != null){
                System.out.println(line);
                splitted.addAll(
                    Arrays.asList(line.split("\\W+"))
                    .stream()
                    .map(string ->  string.toLowerCase())
                    .toList()
                );
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        System.out.println(splitted);
        HashMap<String, Integer> counts = new HashMap<>();
        for (String word : splitted) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }

        System.out.println(counts.size());
        System.out.println(counts);
        this.results = counts;
    }
}