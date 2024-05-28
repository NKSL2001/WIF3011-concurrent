import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RunnableBOW implements Results {
    public volatile ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<String, Integer>();
    private final List<Thread> counterThreads = new ArrayList<>();

    public RunnableBOW(String filePath) {
        this(filePath, 100); //change lineThreshold to set number of lines needed for new thread
    }

    public RunnableBOW(String filePath, int lineThreshold) {
        RunnableRead readFile = new RunnableRead(filePath, this, lineThreshold);
        Thread readThread = new Thread(readFile);
        readThread.start();

        try {
            readThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        waitForCountingThreadsToFinish();

       /* // Print word counts
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }

    public Map<String, Integer> results() {
        return counts;
    }

    public synchronized void startCountingThread(List<String> listRead) {
        RunnableCount counter = new RunnableCount(listRead, counts);
        Thread counterThread = new Thread(counter);
        counterThreads.add(counterThread);
        counterThread.start();
        // System.out.println("Counting thread " + counterThread.getName() + " started."); //used for debugging
    }

    private void waitForCountingThreadsToFinish() {
        for (Thread counterThread : counterThreads) {
            try {
                counterThread.join();
                // System.out.println("Counting thread " + counterThread.getName() + " ended."); //used for debugging
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class RunnableRead implements Runnable {

        private String filePath;
        private final RunnableBOW app;
        private final int lineThreshold;

        public RunnableRead(String filePath, RunnableBOW app, int lineThreshold) {
            this.filePath = filePath;
            this.app = app;
            this.lineThreshold = lineThreshold;
        }

        @Override
        public void run() {
            try (
                BufferedReader br = new BufferedReader (
                    new InputStreamReader(
                        new FileInputStream(filePath), 
                        StandardCharsets.UTF_8
                    )
                )
            ) {
                String lineRead;
                List<String> listRead = new ArrayList<>();
                while ((lineRead = br.readLine()) != null) {
                    listRead.add(lineRead);

                    if (listRead.size() >= lineThreshold) {
                        app.startCountingThread(listRead);
                        // create new list to count with
                        listRead = new ArrayList<>();
                    }
                }

                // for last thread
                app.startCountingThread(listRead);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class RunnableCount implements Runnable {

        private final List<String> listRead;
        private volatile ConcurrentHashMap<String, Integer> counts;
        private final static Pattern wordsplitter = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

        public RunnableCount(List<String> listRead, ConcurrentHashMap<String, Integer> counts) {
            this.listRead = listRead;
            this.counts = counts;
        }

        @Override
        public void run() {
            listRead.stream()
                .flatMap(string -> Arrays.stream(wordsplitter.split(string)))
                .map(string -> string.toLowerCase())
                .filter(string -> !string.isEmpty())
                .forEach(word -> {
                    counts.merge(word, 1, Integer::sum);
                });
        }
    }
}