import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) throws Exception {
        SystemInfo systemInfo = new SystemInfo();
        System.out.println(systemInfo);

        //String filePath = "test text.txt";
        String[] filePaths = {
                "10kb_largetextfile.txt",
                "100kb_largetextfile.txt",
                "500kb_largetextfile.txt",
                "1mb_largetextfile.txt",
                "5mb_largetextfile.txt",
                "10mb_largetextfile.txt",
                "30mb_largetextfile.txt",
        };

        for (String filePath : filePaths) {
            long sum1=0;
            long sum2=0;
            long sum3=0;

            for (int i = 0; i < 3; i++) {
                // Sequential
                long starttime = System.currentTimeMillis();
                Results sequentialResult = new Sequential(filePath);
                long diff = System.currentTimeMillis() - starttime;
                System.out.printf("Sequential taken %d milliseconds.\n", diff);

                // Concurrent #1: Runnable 2.0
                long starttime2 = System.currentTimeMillis();
                Results runnableResult = new RunnableBOW(filePath);
                long diff2 = System.currentTimeMillis() - starttime2;
                System.out.printf("Runnable taken %d milliseconds.\n", diff2);

                // Concurrent #2: via Future
                long starttime3 = System.currentTimeMillis();
                Results futureResult = new FutureBOW(filePath);
                long diff3 = System.currentTimeMillis() - starttime3;
                System.out.printf("Future taken %d milliseconds.\n", diff3);

                sum1 += diff;
                sum2 += diff2;
                sum3 += diff3;
                
            }

            System.out.println("===================================");
            System.out.println("======="+filePath+"========");

            System.out.println("Sequential average time: "+ sum1/3 +" milliseconds");
            System.out.println("Runnable average time: "+ sum2/3 +" milliseconds");
            System.out.println("Future average time: "+ sum3/3 +" milliseconds");

            System.out.println("===================================");
            System.out.println("");
        }
    }
}
