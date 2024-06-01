public class SystemInfo {
    int N_CPUS;
    long FREE_MEM;
    long MAX_MEM;
    long JVM_MEM;
    String OS_NAME;
    String OS_ARCH;
    String JAVA_VERSION;

    public SystemInfo() {
        this.N_CPUS = Runtime.getRuntime().availableProcessors();
        this.FREE_MEM = Runtime.getRuntime().freeMemory();
        this.MAX_MEM = Runtime.getRuntime().maxMemory();
        this.JVM_MEM = Runtime.getRuntime().totalMemory();
        this.OS_NAME = System.getProperty("os.name");
        this.OS_ARCH = System.getProperty("os.arch");
        this.JAVA_VERSION = System.getProperty("java.version");
    }

    public String toString(){
        return String.format("Operating System: %s\nOperating Architecture: %s\nJava Version: %s\nNumber of CPUs: %d\nFree Memory: %d MB\nMax Memory: %d MB\nJVM Memory: %d MB\n",
                OS_NAME, OS_ARCH, JAVA_VERSION, N_CPUS, FREE_MEM/1_048_576, MAX_MEM/1_048_576, JVM_MEM/1_048_576);
    }

    public String toJson(){
        return String.format("{\"os_name\":\"%s\",\"os_arch\":\"%s\",\"java_version\":\"%s\",\"num_cpu\":\"%d\",\"free_mem\":\"%d\",\"max_mem\":\"%d\",\"jvm_mem\":\"%d\"}",
                OS_NAME, OS_ARCH, JAVA_VERSION, N_CPUS, FREE_MEM, MAX_MEM, JVM_MEM);
    }




}
