import java.io.*;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

// io: BufferedReader, FileReader, FileNotFoundException, IOException
public class RunnableRead2 implements Runnable {

    private volatile Queue<String> listRead;
    private String filePath;
    private volatile String lineRead;
    private final App app;
    private AtomicBoolean isReading;
    private final int capacity = 10;

    public RunnableRead2(Queue<String> listRead, String filePath, String lineRead, App app, AtomicBoolean isReading) {
        this.listRead = listRead;
        this.filePath = filePath;
        this.lineRead = lineRead;
        this.app = app;
        this.isReading = isReading;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((lineRead = br.readLine()) != null) {
                synchronized (app) {
                    while (listRead.size() == capacity) {
                        app.wait();
                    }
                    // add full unprocessed string to queue
                    listRead.add(lineRead);

                    app.notifyAll();
                }
            }
            isReading.getAndSet(false);

            // failsafe to prevent getting stuck
            synchronized (app) {
                app.notifyAll();
            }
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}