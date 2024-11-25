package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.student;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class Student {
    public void accessPathThroughThirdPartyPackage() throws IOException {
        ThirdPartyPackagePenguin.accessFileSystem();
    }
}
