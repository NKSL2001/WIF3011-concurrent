public class App {
    public static void main(String[] args) throws Exception {
        SystemInfo systemInfo = new SystemInfo();
        System.out.println(systemInfo);

        //String filePath = "test text.txt";
        String[] filePaths = {
                "10kb_largetextfile.txt",
                "50kb_largetextfile.txt",
                "100kb_largetextfile.txt",
                "500kb_largetextfile.txt",
                "1mb_largetextfile.txt",
                "5mb_largetextfile.txt",
                "10mb_largetextfile.txt",
                "50mb_largetextfile.txt",
                "100mb_largetextfile.txt",
        };

        int numberOfAvgLoop = 3;
        String resultTimeJson = "{";

        for (String filePath : filePaths) {
            long sum1=0;
            long sum2=0;
            long sum3=0;

            for (int i = 0; i <= numberOfAvgLoop; i++) {
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

                if (i != 0){
                    // remove first load slowdown
                    sum1 += diff;
                    sum2 += diff2;
                    sum3 += diff3;
                }

                System.gc();
            }

            System.out.println("===================================");
            System.out.println("======="+filePath+"========");

            System.out.println("Sequential average time: "+ sum1/numberOfAvgLoop +" milliseconds");
            System.out.println("Runnable average time: "+ sum2/numberOfAvgLoop +" milliseconds");
            System.out.println("Future average time: "+ sum3/numberOfAvgLoop +" milliseconds");

            System.out.println("===================================");
            System.out.println("");
            
            resultTimeJson += String.format("\"%s\": {\"sequential\":%d,\"runnable\":%d,\"future\":%d},", 
                filePath.split("_")[0],
                sum1/numberOfAvgLoop,
                sum2/numberOfAvgLoop,
                sum3/numberOfAvgLoop
            );
        }

        resultTimeJson = resultTimeJson.substring(0, resultTimeJson.length()-1) + "}";

        System.out.println("\n\nCopy the following string in brackets {} and share it.");
        System.out.println("===================================");

        System.out.println("{\"system_info\": "+systemInfo.toJson() + ", \"results\": "+resultTimeJson+"}");
    }
}
