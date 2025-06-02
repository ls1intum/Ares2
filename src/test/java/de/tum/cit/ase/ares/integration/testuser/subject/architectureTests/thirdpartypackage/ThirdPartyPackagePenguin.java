package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is used to emulate a third-party package that is not part of the test user's codebase.
 */
public class ThirdPartyPackagePenguin {

    /**
     * This method is used to emulate a third-party package that is not part of the test user's codebase accessing the file system.
     */
    public static void accessFileSystem() throws IOException {
        new PrintStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt").close();
    }

    public static void readFile() throws IOException {
        Files.readString(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static String readFile_with_path(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    public static void overwriteFile() throws IOException {
        byte[] content = "Hello, world!".getBytes();
        Files.write(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), content);
    }

    public static void overwriteFile_with_path_text(String path, String text) throws IOException {
        byte[] content = text.getBytes();
        Files.write(Path.of(path), content);
    }

    public static void executeFile() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
    }

    public static void executeFile_with_path(String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(path);
    }

    public static void deleteFile() throws IOException {
        Files.delete(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static void deleteFile_with_path(String path) throws IOException {
        Files.delete(Path.of(path));
    }
}
