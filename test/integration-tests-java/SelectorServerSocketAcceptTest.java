import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.util.*;


public class SelectorServerSocketAcceptTest extends CriuTest {
    public static void main(String[] args) throws IOException {
         boolean result = new SelectorServerSocketAcceptTest().test(args);
         System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("Client connected: 0") && after.equals("Client connected: 1");
    }

    @Override
    public boolean runTest() throws Exception {
        // Selector state must be saved across C/R events
        int clientCount = 0;
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            spawnClient();
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    handleAccept(key, clientCount);
                }
                clientCount = clientCount + 1;
                
                if (clientCount == 1) {
                    this.notifyCheckpointReadiness();
                    Thread.sleep(5000);
                } 
            }

            if (clientCount == 2) break;
        }
        serverChannel.close();
        return false;
    }

    private void handleAccept(SelectionKey key, int count) throws Exception {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        resetPipe();
        writeToOutputPipe("Client connected: " + count);
    }

    private Process spawnClient() throws Exception {
        List<String> command = List.of(CriuTest.JAVA_LAUNCHER, "ConnectClient");
        return new ProcessBuilder(command).start();
    }
}
