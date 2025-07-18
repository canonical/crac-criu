import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.util.Comparator;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class CriuTest {
    public static String MESSAGE_PIPE = "pipe";
    public static String OUTPUT_PIPE = "output";
    public static String JAVA_LAUNCHER = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java";
    public static String DUMP_DIR = "dump-dir";
    public static String CRIU_PATH = System.getenv("CRIU_PATH");

    public boolean test(String[] args) {
	try {
            if (args.length == 0) {
                return spawnTest();
            } else {
                return runTest();
            }
	} catch (Exception e) {
            return false;
        }
    }

    public boolean spawnTest() {
        try {
	    cleanup();
            init();
            long pid = startAndWait();
            String before = gatherOutput();
            checkpoint(pid);
            restore();
            String after = gatherOutput();
            return runCheck(before,after);
        } catch (Exception e) {
	    e.printStackTrace();
            return false;
        }
    }

    abstract boolean runCheck(String outputBeforeCP, String outputAfterRestore);

    abstract boolean runTest() throws Exception;

    protected long startAndWait() throws Exception {
	List<String> command = List.of(JAVA_LAUNCHER, this.getClass().getName(), "run");
        Process test = new ProcessBuilder (command)
                .redirectOutput(Paths.get(OUTPUT_PIPE).toFile())
                .redirectError(Paths.get(OUTPUT_PIPE).toFile())
                .start();
        waitForCheckpointReadiness();
        return test.pid();
    }

    protected int checkpoint(long pid) throws Exception {
        List<String> command = List.of(
                CRIU_PATH, "dump", "--shell-job", "--tcp-established",
                "-t", Long.toString(pid),
                "--file-locks", "-v4", "-D", DUMP_DIR 
        );
        Process p = new ProcessBuilder(command).inheritIO().start();
	return p.waitFor();
    }

    protected int restore() throws Exception {
        List<String> command = List.of(
                CRIU_PATH, "restore", "--shell-job", "--tcp-established",
                "--file-locks", "-v4", "-D", DUMP_DIR 
        );
        Process p = new ProcessBuilder(command).inheritIO().start();
	return p.waitFor();
    }

    private void cleanup() throws IOException {
	if (Files.exists(Paths.get(DUMP_DIR))) {
	    Files.list(Paths.get(DUMP_DIR)).forEach(x -> {
	        try { Files.delete(x); } catch (IOException e) {}
	    });
        }
	Files.deleteIfExists(Paths.get(OUTPUT_PIPE));
    }

    private void init() throws IOException {
        Path path = Paths.get(MESSAGE_PIPE);
        Files.write(path, "0".getBytes());
        path.toFile().deleteOnExit();

        path = Paths.get(OUTPUT_PIPE);
        Files.createFile(path);

	if (!Files.exists(Paths.get(DUMP_DIR))) {
            Files.createDirectory(Paths.get(DUMP_DIR));
        }
    }

    private String gatherOutput() throws Exception {
        String output = new String(Files.readAllBytes(Paths.get(OUTPUT_PIPE)));
        return output;
    }
    protected synchronized void notifyCheckpointReadiness() throws IOException {
        Files.write(Paths.get(MESSAGE_PIPE), "1".getBytes());
    }

    protected synchronized void waitForCheckpointReadiness() throws Exception {
        byte[] message;
        do {
            message = Files.readAllBytes(Paths.get(MESSAGE_PIPE));
            Thread.sleep(500);
        } while (message[0] != '1');
    }

    protected void writeToOutputPipe(String output) throws Exception {
        Files.write(
                Paths.get(OUTPUT_PIPE),
                output.getBytes(),
		StandardOpenOption.APPEND
        );
    }

    protected void resetPipe() throws Exception {
        Files.write(
            Paths.get(OUTPUT_PIPE),
            "".getBytes()
        );
    }

}
