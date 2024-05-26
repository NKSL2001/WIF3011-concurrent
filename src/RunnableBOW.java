import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

// ArrayList, HashMap, List, PriorityQueue, Queue
public class RunnableBOW {

    static AtomicBoolean isReading2 = new AtomicBoolean(true);
    static volatile String lineRead;
    static volatile Queue<String> listRead = new PriorityQueue<>();
    static volatile HashMap<String, Integer> counts = new HashMap<>();
    private final List<Thread> counterThreads = new ArrayList<>();
    private final int lineThreshold; 
    private final App app;

    public RunnableBOW(String filePath, App app) {
        this(filePath, app, 20); //change lineThreshold to set number of lines needed for new thread
    }

    public RunnableBOW(String filePath, App app, int lineThreshold) {
        this.app = app;
        this.lineThreshold = lineThreshold;

        RunnableRead2 readFile2 = new RunnableRead2(listRead, filePath, lineRead, this, isReading2, lineThreshold);

        Thread readThread2 = new Thread(readFile2);
        readThread2.start();

        try {
            readThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        waitForCountingThreadsToFinish();

       /* // Print word counts
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }

    public synchronized void startCountingThread() {
        RunnableCount2 counter = new RunnableCount2(this, isReading2, listRead, new ArrayList<>(), counts);
        Thread counterThread = new Thread(counter);
        counterThreads.add(counterThread);
        counterThread.start();
        System.out.println("Counting thread " + counterThread.getName() + " started."); //used for debugging
    }

    private void waitForCountingThreadsToFinish() {
        for (Thread counterThread : counterThreads) {
            try {
                counterThread.join();
                System.out.println("Counting thread " + counterThread.getName() + " ended."); //used for debugging
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class RunnableRead2 implements Runnable {

        private volatile Queue<String> listRead;
        private String filePath;
        private volatile String lineRead;
        private final RunnableBOW app;
        private AtomicBoolean isReading;
        private final int lineThreshold;
        private int lineCount = 0;

        public RunnableRead2(Queue<String> listRead, String filePath, String lineRead, RunnableBOW app,
                             AtomicBoolean isReading, int lineThreshold) {
            this.listRead = listRead;
            this.filePath = filePath;
            this.lineRead = lineRead;
            this.app = app;
            this.isReading = isReading;
            this.lineThreshold = lineThreshold;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                while ((lineRead = br.readLine()) != null) {
                    synchronized (app) {
                        listRead.add(lineRead);
                        lineCount++;

                        if (lineCount >= lineThreshold) {
                            app.startCountingThread();
                            lineCount = 0;
                        }

                        app.notifyAll();
                    }
                }
                isReading.set(false);

                synchronized (app) {
                    app.notifyAll();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class RunnableCount2 implements Runnable {

        private final RunnableBOW app;
        private AtomicBoolean isReading;
        private volatile Queue<String> listRead;
        private volatile List<String> processed;
        private volatile HashMap<String, Integer> counts;

        public RunnableCount2(RunnableBOW app, AtomicBoolean isReading, Queue<String> listRead, List<String> processed, HashMap<String, Integer> counts) {
            this.app = app;
            this.isReading = isReading;
            this.listRead = listRead;
            this.processed = processed;
            this.counts = counts;
        }

        @Override
        public void run() {
            try {
                while (isReading.get() || !listRead.isEmpty()) {
                    synchronized (app) {
                        while (listRead.isEmpty() && isReading.get()) {
                            app.wait();
                        }
                        if (!listRead.isEmpty()) {
                            processed.addAll(
                                    Arrays.asList(listRead.poll().split("\\W+"))
                                            .stream()
                                            .map(String::toLowerCase)
                                            .toList()
                            );

                            for (String word : processed) {
                                counts.put(word, counts.getOrDefault(word, 0) + 1);
                            }

                            processed.clear();
                            app.notifyAll();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}