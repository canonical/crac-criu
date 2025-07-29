import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ConnectClient {
    public static void main(String[] args) throws Exception {
        Thread.sleep(2000);
        Socket socket = new Socket("localhost", 8080);
        socket.getInputStream();
        socket.close();
    }
}
