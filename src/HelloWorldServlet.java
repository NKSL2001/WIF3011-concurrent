import java.io.*; 
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*; 
  
@WebServlet("/servlet") // This is the URL of the servlet.
public class HelloWorldServlet extends HttpServlet { 
    public void doGet(HttpServletRequest request, 
                      HttpServletResponse response) 
        throws IOException, ServletException 
    { 
        response.setContentType("text/html"); 
        PrintWriter out = response.getWriter(); 
        out.println("<html><head><title>Hello World Servlet</title></head>"); 
        out.println("<body>"); 
        out.println("<h1>Hello World Servlet</h1>"); 
        out.println("</body></html>"); 
        out.close(); 
        
    } 
}