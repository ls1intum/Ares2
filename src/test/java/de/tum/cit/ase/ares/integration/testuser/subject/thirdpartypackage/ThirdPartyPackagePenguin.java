package de.tum.cit.ase.ares.integration.testuser.subject.thirdpartypackage;

import java.io.IOException;
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
        Files.readString(Path.of("fileUsingFilesClass.txt"));
    }
}
