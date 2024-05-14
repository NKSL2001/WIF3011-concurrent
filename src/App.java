
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// ArrayList, List
public class App {

    static volatile List<String> processed = new ArrayList<>();
    static AtomicBoolean isReading = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        String filePath = "test text.txt";

        // Sequential
        long starttime = System.currentTimeMillis();
        Sequential sequentialResult = new Sequential(filePath);
        long diff = System.currentTimeMillis() - starttime;
        System.out.printf("Sequential taken %d milliseconds.\n", diff);

        // // Separate output
        // System.out.println("\n".repeat(10));
        // System.out.println("=".repeat(150));
        // System.out.println("\n".repeat(10));

        // Concurrent #1: via Runnable
        RunnableRead readFile = new RunnableRead(processed, isReading);
        RunnableCount countBOW = new RunnableCount(processed, isReading);

        Thread readThread = new Thread(readFile);
        Thread countThread = new Thread(countBOW);

        long starttime2 = System.currentTimeMillis();
        readThread.start();
        countThread.start();
        readThread.join();
        countThread.join();
        long diff2 = System.currentTimeMillis() - starttime2;
        System.out.printf("Runnable taken %d milliseconds.\n", diff2);

        // Concurrent #2: via Future
        int chunkSize = 32768;

        long starttime3 = System.currentTimeMillis();
        FutureBOW futureResult = new FutureBOW(filePath, chunkSize);
        long diff3 = System.currentTimeMillis() - starttime3;
        System.out.printf("Future taken %d milliseconds.\n", diff3);

    }
}
