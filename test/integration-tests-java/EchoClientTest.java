import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.List;


public class EchoClientTest extends CriuTest {
    public static void main(String[] args) throws IOException {
         boolean result = new EchoClientTest().test(args);
         System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("HELLO") && after.equals("BYE");
    }

    @Override
    public boolean runTest() throws Exception {
        Process p = spawnServer();
        Thread.sleep(2000);
        Socket socket = new Socket("localhost", 8080);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("hello");
        writeToOutputPipe(in.readLine());

        // ready for checkpoint
        this.notifyCheckpointReadiness();
        Thread.sleep(5000);

        out.println("bye");
        resetPipe();
        writeToOutputPipe(in.readLine());
        return false;
        
    }

    private Process spawnServer() throws Exception {
        List<String> command = List.of(CriuTest.JAVA_LAUNCHER, "EchoServer");
        return new ProcessBuilder(command).start();
    }
}
