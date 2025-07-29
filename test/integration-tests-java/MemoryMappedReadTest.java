import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

public class MemoryMappedReadTest extends CriuTest {
    public static void main(String[] args) throws Exception {
        boolean result = new MemoryMappedReadTest().test(args);
	System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("The quick") && after.equals("brown");
    }

    @Override
    public boolean runTest() throws Exception {
        try (RandomAccessFile file = new RandomAccessFile("file.txt", "rw");
            FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_ONLY, 
                0, 
                channel.size()
            );

            byte[] data = new byte[9];
            buffer.get(data);
            this.writeToOutputPipe(new String(data));
	    this.notifyCheckpointReadiness();

	    Thread.sleep(5000);
	    this.resetPipe();
            buffer.get();
            data = new byte[5];
            buffer.get(data);
	    this.writeToOutputPipe(new String(data));
        }
	return false;
    }
}

