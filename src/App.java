import java.util.ArrayList;
import java.util.List;

public class App {

    static volatile List<String> processed = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Sequential
        long starttime = System.currentTimeMillis();
        Sequential sequentialResult = new Sequential();
        long diff = System.currentTimeMillis() - starttime;
        System.out.printf("Sequential taken %d milliseconds.\n", diff);

        // Separate output
        System.out.println("\n".repeat(10));
        System.out.println("=".repeat(150));
        System.out.println("\n".repeat(10));

        // Concurrent #1: via Runnable
        ReadFile readFile = new ReadFile(processed);
        CountBOW countBOW = new CountBOW(processed);

        Thread readThread = new Thread(readFile);
        Thread countThread = new Thread(countBOW);

        readThread.start();
        countThread.start();
        readThread.join();
        countThread.join();
    }
}
