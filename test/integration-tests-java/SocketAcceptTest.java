import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.List;


public class SocketAcceptTest extends CriuTest {
    public static void main(String[] args) throws IOException {
         boolean result = new SocketAcceptTest().test(args);
         System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("Client connected: 0") && after.equals("Client connected: 1");
    }

    @Override
    public boolean runTest() throws Exception {
        ServerSocket server = new ServerSocket(8080);
        int count = 0;
        
        while (count < 2) {
            Process clientProcess = spawnClient(); // will wait for server
            Socket client = server.accept();
            client.getInputStream();

            resetPipe();
            writeToOutputPipe("Client connected: " + count);

            client.close();

            count += 1;
            if (count == 1) {
                this.notifyCheckpointReadiness();
                Thread.sleep(5000);
            }
        }
        return false;
    }

    private Process spawnClient() throws Exception {
        List<String> command = List.of(CriuTest.JAVA_LAUNCHER, "ConnectClient");
        return new ProcessBuilder(command).start();
    }
}
