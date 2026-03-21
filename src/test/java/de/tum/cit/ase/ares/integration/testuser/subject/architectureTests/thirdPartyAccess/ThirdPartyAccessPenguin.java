package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdPartyAccess;

import java.io.IOException;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

public class ThirdPartyAccessPenguin {
	public void accessPathThroughThirdPartyPackage() throws IOException {
		ThirdPartyPackagePenguin.readFile();
	}
}
