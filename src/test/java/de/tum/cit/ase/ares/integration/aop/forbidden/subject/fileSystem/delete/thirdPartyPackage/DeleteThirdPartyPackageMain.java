package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage;

import java.io.IOException;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

public class DeleteThirdPartyPackageMain {

	private DeleteThirdPartyPackageMain() {
		throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using the {@link ThirdPartyPackagePenguin} class for
	 * deletion.
	 */
	public static void accessFileSystemViaThirdPartyPackage() throws IOException {
		ThirdPartyPackagePenguin.deleteFile();
	}
}
