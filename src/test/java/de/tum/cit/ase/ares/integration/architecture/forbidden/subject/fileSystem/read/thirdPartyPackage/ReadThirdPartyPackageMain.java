package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.thirdPartyPackage;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class ReadThirdPartyPackageMain {

    private ReadThirdPartyPackageMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ThirdPartyPackagePenguin} class for reading.
     */
    public static void accessFileSystemViaThirdPartyPackage() throws IOException {
        ThirdPartyPackagePenguin.readFile();
    }
}