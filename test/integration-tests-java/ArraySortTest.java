import java.util.Arrays;

public class ArraySortTest extends CriuTest {
    public static void main(String[] args) throws Exception {
        boolean result = new ArraySortTest().test(args);
	System.exit(result ? 0 : 1);
    }

    @Override
    public boolean runCheck(String before, String after) {
        return before.equals("10") && after.equals("1");
    }

    @Override
    public boolean runTest() throws Exception {
	int[] array = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        this.writeToOutputPipe(Integer.toString(array[0]));
	Arrays.sort(array);
	this.notifyCheckpointReadiness();
	Thread.sleep(5000);
	this.resetPipe();
	this.writeToOutputPipe(Integer.toString(array[0]));
	return false;
    }
}

