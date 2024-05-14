
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// ArrayList, List
public class RunnableCount implements Runnable {

    private volatile List<String> processed = new ArrayList<>();
    private List<String> stored = new ArrayList<>();
    private AtomicBoolean isReading;
    private Map<String, Integer> results;

    public RunnableCount(List<String> processed, AtomicBoolean isReading) {
        this.processed = processed;
        this.isReading = isReading;
    }

    @Override
    public void run() {
        while (isReading.get()) {
            try {
                // wait in queue to prevent ConcurrentModificationException
                Thread.sleep(25);
                Thread.yield();
                // show what is processed per line
                if (!processed.isEmpty()) {
                    // System.out.println(processed);
                }

                // move to another array to clear prev array
                stored.addAll(processed);
                processed.removeAll(processed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // plan to cover hashmap to print size
        // for now just showing all processed BOW in one arraylist
        // System.out.println("\n".repeat(10));
        // System.out.println(stored);
        HashMap<String, Integer> counts = new HashMap<>();
        for (String word : stored) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }
        this.results=counts;
        System.out.println(results);
    }
}
