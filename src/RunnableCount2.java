import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// Queue, List, ArrayList, HashMap, Arrays
public class RunnableCount2 implements Runnable {

    private final App app;
    private AtomicBoolean isReading;
    private volatile Queue<String> listRead;
    private volatile List<String> processed = new ArrayList<>();
    private volatile HashMap<String, Integer> counts = new HashMap<>();

    public RunnableCount2(App app, AtomicBoolean isReading, Queue<String> listRead, List<String> processed, HashMap<String, Integer> counts) {
        this.app = app;
        this.isReading = isReading;
        this.listRead = listRead;
        this.processed = processed;
        this.counts = counts;
    }

    @Override
    public void run() {
        try {
            // updated condition because will constantly clear queue
            while (isReading.get() || !listRead.isEmpty()) {
                synchronized (app) {
                    while (listRead.isEmpty() && isReading.get()) {
                        app.wait();
                    }
                    if (!listRead.isEmpty()) {
                        // same block for BOW function
                        processed.addAll(
                                Arrays.asList(listRead.poll().split("\\W+"))
                                        .stream()
                                        .map(string -> string.toLowerCase())
                                        .toList()
                        );

                        for (String word : processed) {
                            counts.put(word, counts.getOrDefault(word, 0) + 1);
                        }
                        // instead of count at the end, start count when next line is processed
                        // thus, clear arraylist to prevent re-counting
                        processed.removeAll(processed);
                        app.notifyAll();
                    }
                }
            }
            // display results
            // System.out.println(counts);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}