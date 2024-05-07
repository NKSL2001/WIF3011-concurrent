import java.util.List;

public class CountBOW implements Runnable {

    private volatile List<String> processed;

    public CountBOW(List<String> processed) {
        this.processed = processed;
    }

    @Override
    public void run() {
        System.out.println("countThread running"); // test msg
        while (processed != null) {
            System.out.println(processed);
            processed.remove(0);
            // wait readfile to send data to arraylist first
            Thread.yield();
        }
        // plan to cover hashmap to print size
    }
    // derrick: I made the same problem in indexoutofbound error, I still can't figure out, I'll try someday wed
}