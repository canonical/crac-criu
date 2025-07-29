import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.util.*;


public class SelectorConnectedSocketTest extends CriuTest {
    public static void main(String[] args) throws IOException {
         boolean result = new SelectorConnectedSocketTest().test(args);
         System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("CONNECTED.WROTE 4 bytes.") && after.equals("READ: BYE\n");
    }

    @Override
    public boolean runTest() throws Exception {
        // Selector state holding key for connected socket must be saved across C/R events
        Process server = spawnServer();
        Thread.sleep(2000);

        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        boolean read = false;
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
              
                if(key.isConnectable()) {
                    handleConnect(selector, key);
                } else if (key.isWritable()) {
                    handleWrite(selector, key);
                    this.notifyCheckpointReadiness();
                    Thread.sleep(5000);
                } else if (key.isReadable()) {
                    handleRead(selector, key);
                    read = true;
                }
            }
            if (read) {
                socketChannel.close();
                selector.close();
                break;
            }
        }
        return false;
    }

    private void handleConnect(Selector selector, SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.finishConnect()) {
            channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            resetPipe();
            writeToOutputPipe("CONNECTED.");
        }
    }

    private void handleWrite(Selector selector, SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.wrap("bye\n".getBytes());
        int nbytes = channel.write(buffer);
        writeToOutputPipe("WROTE " + nbytes + " bytes.");
        key.interestOps(SelectionKey.OP_READ);
    }

    private void handleRead(Selector selector, SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        int bytesRead = channel.read(buffer);
        resetPipe();
        if (bytesRead > 0) {
            buffer.flip();
            String response = new String(buffer.array(), 0, buffer.limit());
            writeToOutputPipe("READ: " + response);
        } else {
            writeToOutputPipe("Read failed"); 
        }
    } 

    private Process spawnServer() throws Exception {
        List<String> command = List.of(CriuTest.JAVA_LAUNCHER, "EchoServer");
        return new ProcessBuilder(command).start();
    }

}
