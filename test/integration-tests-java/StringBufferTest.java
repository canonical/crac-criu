import java.util.Arrays;

public class StringBufferTest extends CriuTest {
    public static void main(String[] args) throws Exception {
        boolean result = new StringBufferTest().test(args);
	System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("") && after.equals("TheQuickBrownFoxJumpsOverTheLazyDog");
    }

    @Override
    public boolean runTest() throws Exception {
	StringBuffer sb = new StringBuffer();
	sb.append("The");
	sb.append("Quick");
	sb.append("Brown");
	sb.append("Fox");
	sb.append("Jumps");
	this.notifyCheckpointReadiness();
	Thread.sleep(5000);
	sb.append("Over");
	sb.append("The");
	sb.append("Lazy");
	sb.append("Dog");
	this.writeToOutputPipe(sb.toString());
	return false;
    }
}

