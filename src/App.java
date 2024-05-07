public class App {
    public static void main(String[] args) throws Exception {
        long starttime = System.currentTimeMillis();
        Sequential sequentialResult = new Sequential();
        long diff = System.currentTimeMillis() - starttime;
        System.out.printf("Sequential taken %d milliseconds.\n", diff);
    }
}
