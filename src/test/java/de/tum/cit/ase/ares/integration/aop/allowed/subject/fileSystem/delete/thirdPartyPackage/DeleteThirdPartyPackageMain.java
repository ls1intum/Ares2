package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class DeleteThirdPartyPackageMain {

    private DeleteThirdPartyPackageMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ThirdPartyPackagePenguin} class for deletion.
     */
    public static void accessFileSystemViaThirdPartyPackage() throws IOException {
        String filePath = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";
        ThirdPartyPackagePenguin.deleteFile_with_path(filePath);
    }
}
