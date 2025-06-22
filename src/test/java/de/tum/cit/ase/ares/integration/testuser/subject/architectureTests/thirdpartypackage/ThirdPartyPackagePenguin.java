package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is used to emulate a third-party package that is not part of the test user's codebase.
 */
public class ThirdPartyPackagePenguin {

    public static String readFile() throws IOException {
        return Files.readString(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static Path overwriteFile() throws IOException {
        byte[] content = "Hello, world!".getBytes();
        return Files.write(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), content);
    }

    public static Process executeFile() throws IOException {
        return Runtime.getRuntime().exec("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
    }

    public static void deleteFile() throws IOException {
        Files.delete(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }
}
