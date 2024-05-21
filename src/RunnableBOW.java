import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// ArrayList, HashMap, List, PriorityQueue, Queue
public class RunnableBOW {
    
    static AtomicBoolean isReading2 = new AtomicBoolean(true);
    static volatile String lineRead;
    static volatile Queue<String> listRead = new PriorityQueue<>();
    static volatile List<String> processed2 = new ArrayList<>();
    static volatile HashMap<String, Integer> counts = new HashMap<>();

    public RunnableBOW(String filePath, App app) {
        RunnableRead2 readFile2 = new RunnableRead2(listRead, filePath, lineRead, app, isReading2);
        RunnableCount2 countWord = new RunnableCount2(app, isReading2, listRead, processed2, counts);
        RunnableCount2 countWord2 = new RunnableCount2(app, isReading2, listRead, processed2, counts);

        Thread readThread2 = new Thread(readFile2);
        Thread countThread2 = new Thread(countWord);
        Thread countThread3 = new Thread(countWord2);

        readThread2.start();
        countThread2.start();
        countThread3.start();
        
        try {
            readThread2.join();
            countThread2.join();
            countThread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
