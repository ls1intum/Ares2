package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.thirdPartyPackage;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import java.io.IOException;
import java.nio.file.Path;

public class WriteThirdPartyPackageMain {

    private WriteThirdPartyPackageMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ThirdPartyPackagePenguin} class for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaThirdPartyPackage(String text) throws IOException {
        ThirdPartyPackagePenguin.overwriteFile(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"));
    }
}