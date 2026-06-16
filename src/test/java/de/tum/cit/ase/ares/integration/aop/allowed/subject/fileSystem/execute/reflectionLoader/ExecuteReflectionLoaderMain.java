package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.reflectionLoader;

import java.io.IOException;

public class ExecuteReflectionLoaderMain {

	private static final String TRUSTED_SCRIPT_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";

	private ExecuteReflectionLoaderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the reflection-loader execution fixture.
	 */
	public static void accessFileSystemViaReflectionLoader() throws IOException {
		Runtime.getRuntime().exec(TRUSTED_SCRIPT_PATH);
	}
}
