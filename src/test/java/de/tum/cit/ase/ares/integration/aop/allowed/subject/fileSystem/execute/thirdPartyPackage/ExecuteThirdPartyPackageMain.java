package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.thirdPartyPackage;

import java.io.IOException;

import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

/**
 * Test subject that starts a trusted script through a class from another
 * package. Ares analyses this class as if a student had written it.
 */
public class ExecuteThirdPartyPackageMain {

	/**
	 * Stops anyone from creating an instance, since this class only has static
	 * methods.
	 */
	private ExecuteThirdPartyPackageMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Starts the script through {@link ThirdPartyPackagePenguin}.
	 *
	 * @param filePath the script to start, relative to the project folder
	 * @return the started process, so that the caller can check what it did
	 * @throws IOException if the script cannot be started
	 */
	public static Process accessFileSystemViaThirdPartyPackage(String filePath) throws IOException {
		return ThirdPartyPackagePenguin.executeFile(filePath);
	}
}
