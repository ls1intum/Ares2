package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdPartyAccess;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import java.io.IOException;

public class ThirdPartyAccessPenguin {
    public void accessPathThroughThirdPartyPackage() throws IOException {
        ThirdPartyPackagePenguin.readFile();
    }
}
