import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class FileReaderTest extends CriuTest {
    public static void main(String[] args) {
        boolean result = new FileReaderTest().test(args);
	System.exit(result ? 0 : 1);
    }

    public boolean runCheck(String before, String after) {
        return before.equals("12345") && after.equals("678910");
    }

    public boolean runTest() {
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                this.writeToOutputPipe(line);

                if (lineCount == 5) {
                    notifyCheckpointReadiness();
		    Thread.sleep(5000);
		    resetPipe();
                }
            }

        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return false;
    }
}

