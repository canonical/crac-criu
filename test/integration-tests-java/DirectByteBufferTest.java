import java.nio.ByteBuffer;

public class DirectByteBufferTest extends CriuTest {
    public static void main(String[] args) throws Exception {
        boolean result = new DirectByteBufferTest().test(args);
	System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String outputBeforeCheckpoint, String outputAfterRestore) {
        return outputBeforeCheckpoint.equals("12") && outputAfterRestore.equals("1256");
    }

    @Override
    public boolean runTest() throws Exception {
	ByteBuffer directBuffer = ByteBuffer.allocateDirect(5);
	directBuffer.put((byte)1);
	directBuffer.put((byte)2);
	directBuffer.put((byte)3);
	directBuffer.put((byte)4);
	directBuffer.flip();
	
        for (int i = 0; i < 2; i++) {
            this.writeToOutputPipe(Byte.toString(directBuffer.get()));
        }

	this.notifyCheckpointReadiness();
	Thread.sleep(5000);
	this.resetPipe();

	directBuffer.put((byte)5);
	directBuffer.put((byte)6);
	directBuffer.flip();

	for (int i = 0; i < 4; i++) {
	    this.writeToOutputPipe(Byte.toString(directBuffer.get()));
	}

	return false;
    }
}

