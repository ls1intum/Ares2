package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.serviceLoader;

import java.io.IOException;

public class ExecuteServiceLoaderMain {

	private static final String TRUSTED_SCRIPT_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";

	private ExecuteServiceLoaderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the service-loader execution fixture.
	 */
	public static void accessFileSystemViaServiceLoader() throws IOException {
		Runtime.getRuntime().exec(TRUSTED_SCRIPT_PATH);
	}
}
