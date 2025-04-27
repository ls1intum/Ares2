package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import java.io.IOException;

public class AllowedThirdPartyPackageMain {

    private AllowedThirdPartyPackageMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using ThirdPartyPackagePenguin.
     */
    public static void accessFileSystemViaThirdPartyPackage() throws IOException {
        ThirdPartyPackagePenguin.deleteFile();
    }
}
