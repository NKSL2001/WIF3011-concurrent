import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// ArrayList, List, PriorityQueue, HashMap
public class App {

    static volatile List<String> processed = new ArrayList<>();
    static AtomicBoolean isReading = new AtomicBoolean(true);
    static AtomicBoolean isReading2 = new AtomicBoolean(true);

    static volatile String lineRead;
    static volatile Queue<String> listRead = new PriorityQueue<>();
    static volatile List<String> processed2 = new ArrayList<>();
    static volatile HashMap<String, Integer> counts = new HashMap<>();

    public static void main(String[] args) throws Exception {
        App app = new App(); // for Runnable 2.0 synchronized monitor

        String filePath = "test text.txt";

        // Sequential
        long starttime = System.currentTimeMillis();
        Sequential sequentialResult = new Sequential(filePath);
        long diff = System.currentTimeMillis() - starttime;
        System.out.printf("Sequential taken %d milliseconds.\n", diff);

        // Concurrent #1: via Runnable
        RunnableRead readFile = new RunnableRead(processed, isReading, filePath);
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

        // Concurrent via Runnable 2.0
        // 1 producer thread 2 consumer threads
        RunnableRead2 readFile2 = new RunnableRead2(listRead, filePath, lineRead, app, isReading2);
        RunnableCount2 countWord = new RunnableCount2(app, isReading2, listRead, processed2, counts);
        RunnableCount2 countWord2 = new RunnableCount2(app, isReading2, listRead, processed2, counts);

        Thread readThread2 = new Thread(readFile2);
        Thread countThread2 = new Thread(countWord);
        Thread countThread3 = new Thread(countWord2);

        long starttime4 = System.currentTimeMillis();
        readThread2.start();
        countThread2.start();
        countThread3.start();
        
        readThread2.join();
        countThread2.join();
        countThread3.join();
        long diff4 = System.currentTimeMillis() - starttime4;
        System.out.printf("Runnable 2.0 taken %d milliseconds.\n", diff4);
    }
}
