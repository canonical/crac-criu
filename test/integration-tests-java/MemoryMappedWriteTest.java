import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.FileChannel;

public class MemoryMappedWriteTest extends CriuTest {
    public static void main(String[] args) throws Exception {
        boolean result = new MemoryMappedWriteTest().test(args);
	System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("The quick") && after.equals("THE quick brown fox jumped over the lazy dog\n");
    }

    @Override
    public boolean runTest() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("file.txt", "rw");
            FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_WRITE, 
                0, 
                channel.size()
            );

            byte[] data = new byte[9];
            buffer.get(data);
            this.writeToOutputPipe(new String(data));

            buffer.rewind();
            buffer.put("THE".getBytes());

	    this.notifyCheckpointReadiness();
	    Thread.sleep(5000);
	    this.resetPipe();

	    this.writeToOutputPipe(Files.readString(Paths.get("file.txt")));

            // Revert changes
            buffer.rewind();
            buffer.put("The".getBytes());
        }
	return false;
    }
}

