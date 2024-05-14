
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// BufferedReader, FileReader, FileNotFoundException, IOException
public class RunnableRead implements Runnable {

    private volatile List<String> processed;
    private AtomicBoolean isReading;
    private String filePath;

    public RunnableRead(List<String> processed, AtomicBoolean isReading, String filePath) {
        this.processed = processed;
        this.isReading = isReading;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String lineRead;

            while ((lineRead = bufferedReader.readLine()) != null) {
                // System.out.println(lineRead);
                processed.addAll(
                        Arrays.asList(lineRead.split("\\W+"))
                                .stream()
                                .map(string -> string.toLowerCase())
                                .toList());
                // wait in queue to prevent ConcurrentModificationException
                Thread.sleep(25);
            }
            // for countBOW to stop processing
            isReading.getAndSet(false);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
