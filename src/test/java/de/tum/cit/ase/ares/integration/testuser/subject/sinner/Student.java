package de.tum.cit.ase.ares.integration.testuser.subject.sinner;

import de.tum.cit.ase.ares.integration.testuser.subject.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class Student {
    public void accessPathThroughThirdPartyPackage() throws IOException {
        ThirdPartyPackagePenguin.accessFileSystem();
    }
}
