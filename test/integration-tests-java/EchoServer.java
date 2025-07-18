import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        
        Socket client = server.accept();
            
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            
        String message;
        while ((message = in.readLine()) != null) {
            out.println(message.toUpperCase());
            
            if ("bye".equalsIgnoreCase(message)) {
                break;
            }
        }
        client.close();
    }
}
