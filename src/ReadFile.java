
import java.io.*;
import java.util.*;
// BufferedReader, FileReader, FileNotFoundException, IOException
// ArrayList, Arrays, List

public class ReadFile implements Runnable {

    private volatile List<String> processed;

    public ReadFile(List<String> processed) {
        this.processed = processed;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\\\WIF3011-concurrent\\\\test text.txt"))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                processed.addAll(
                        Arrays.asList(line.split("\\W+"))
                                .stream()
                                .map(string -> string.toLowerCase())
                                .toList()
                );
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
