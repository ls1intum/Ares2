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
        new PrintStream("file.txt").close();
    }

    public static void deleteFile() throws IOException {
        Files.delete(Path.of("file.txt"));
    }
}
