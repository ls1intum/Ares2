package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.scriptEngine;

import java.io.IOException;

public class ExecuteScriptEngineMain {

	private static final String TRUSTED_SCRIPT_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";
	private ExecuteScriptEngineMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the script-engine execution fixture.
	 */
	public static void accessFileSystemViaScriptEngine(String filePath) throws IOException {
		Runtime.getRuntime().exec(filePath);
	}

	/**
	 * Access the file system using the {@link ScriptEngine} for execution with
	 * default script.
	 */
	public static void accessFileSystemViaScriptEngine() throws IOException {
		accessFileSystemViaScriptEngine(TRUSTED_SCRIPT_PATH);
	}}
