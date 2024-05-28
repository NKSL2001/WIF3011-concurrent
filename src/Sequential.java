import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Sequential {
    Map<String, Integer> results;

    public Sequential(String filePath) throws IOException {
        List<String> splitted = new ArrayList<>();
        try (
            BufferedReader sc = new BufferedReader (
                new InputStreamReader(
                    new FileInputStream(filePath), 
                    StandardCharsets.UTF_8
                )
            )
        ) {
            String line;

            Pattern wordsplitter = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
            while((line = sc.readLine()) != null){
                splitted.addAll(
                    Arrays.asList(wordsplitter.split(line))
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
            counts.merge(word, 1, Integer::sum);
        }

        // store count results
        this.results = counts;
    }
}