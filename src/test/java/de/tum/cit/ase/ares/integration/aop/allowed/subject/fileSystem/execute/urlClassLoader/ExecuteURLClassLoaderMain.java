package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.urlClassLoader;

import java.io.IOException;

public class ExecuteURLClassLoaderMain {

	private static final String TRUSTED_SCRIPT_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";

	private ExecuteURLClassLoaderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the URL-class-loader execution fixture.
	 */
	public static void accessFileSystemViaURLClassLoader() throws IOException {
		Runtime.getRuntime().exec(TRUSTED_SCRIPT_PATH);
	}
}
