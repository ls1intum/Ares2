package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.thirdPartyPackage;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class ExecuteThirdPartyPackageMain {

    private ExecuteThirdPartyPackageMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ThirdPartyPackagePenguin} class for execution.
     */
    public static void accessFileSystemViaThirdPartyPackage(String filePath) throws IOException {
        ThirdPartyPackagePenguin.executeFile_with_path(filePath);
    }
}