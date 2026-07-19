package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdPartyAccess;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ThirdPartyAccessPenguin {
	public void accessPathThroughThirdPartyPackage() throws IOException {
		FileUtils.forceDelete(new File("third-party-fixture.txt"));
	}
}
