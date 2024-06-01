import java.io.*;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*; 
import com.google.gson.*;
  
@WebServlet("/runtest") // This is the URL of the servlet.
public class TestingServlet extends HttpServlet { 
    public void doGet(HttpServletRequest request, 
                      HttpServletResponse response) 
        throws IOException, ServletException 
    {
        Gson gson = new Gson();
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
        Map<String, Map<String, Number>> resultTime = new HashMap<>();

        for (String filePath : filePaths) {
            String onDiskPath = getServletContext().getRealPath("/") + filePath;
            long sum1=0;
            long sum2=0;
            long sum3=0;

            for (int i = 0; i < numberOfAvgLoop; i++) {
                // Sequential
                long starttime = System.currentTimeMillis();
                Results sequentialResult = new Sequential(onDiskPath);
                long diff = System.currentTimeMillis() - starttime;

                // Concurrent #1: Runnable 2.0
                long starttime2 = System.currentTimeMillis();
                Results runnableResult = new RunnableBOW(onDiskPath);
                long diff2 = System.currentTimeMillis() - starttime2;

                // Concurrent #2: via Future
                long starttime3 = System.currentTimeMillis();
                Results futureResult = new FutureBOW(onDiskPath);
                long diff3 = System.currentTimeMillis() - starttime3;

                sum1 += diff;
                sum2 += diff2;
                sum3 += diff3;
            }
            
            Map<String, Number> timeMap = new HashMap<>();
            timeMap.put("sequential", sum1/numberOfAvgLoop);
            timeMap.put("runnable", sum2/numberOfAvgLoop);
            timeMap.put("future", sum3/numberOfAvgLoop);
            resultTime.put(filePath.split("_")[0], timeMap);
        }

        SystemInfo systemInfo = new SystemInfo();
        
        // BOW result to ensure the result is correct
        Map<String, Map<String, Integer>> countResult = new HashMap<>();
        String onDiskPath = getServletContext().getRealPath("/") + "1mb_largetextfile.txt";
        
        Results sequentialResult = new Sequential(onDiskPath);
        Results runnableResult = new RunnableBOW(onDiskPath);
        Results futureResult = new FutureBOW(onDiskPath);

        countResult.put("sequential", sequentialResult.results());
        countResult.put("runnable", runnableResult.results());
        countResult.put("future", futureResult.results());


        response.setContentType("application/json"); 
        PrintWriter out = response.getWriter(); 
        out.println(
            "{\"system_info\": "+systemInfo.toJson() + 
            ", \"results\": "+gson.toJson(resultTime) + 
            ", \"bow_1mb\": "+gson.toJson(countResult) + 
            "}"
        ); 
        out.close(); 
    } 
}