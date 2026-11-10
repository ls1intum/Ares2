package de.tum.cit.ase.ares.integration.aop.allowed;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.nativeCommand.ExecuteNativeCommandMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.processBuilder.ExecuteProcessBuilderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.reflectionLoader.ExecuteReflectionLoaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.scriptEngine.ExecuteScriptEngineMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.serviceLoader.ExecuteServiceLoaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.urlClassLoader.ExecuteURLClassLoaderMain;

public class FileSystemAccessExecuteTest extends SystemAccessTest {

	/** Folder of the trusted scripts and of the file they write. */
	private static final String SUBJECT_FOLDER = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/";

	/**
	 * The script for this operating system. Spelled with forward slashes on every
	 * system because the security policies name it in exactly this form.
	 */
	private static final String TRUSTED_SCRIPT = SUBJECT_FOLDER
			+ (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows") ? "trustedExecute.bat"
					: "trustedExecute.sh");

	/** The file the trusted script writes, which proves that it really ran. */
	private static final Path TRUSTED_OUTPUT = Path.of(SUBJECT_FOLDER, "trusted_output.txt");

	/** What the trusted script writes, without surrounding white space. */
	private static final String EXPECTED_OUTPUT = "Let all who dare look upon these words tremble: Hello, world, and witness the unraveling of reality.";

	/** How long the trusted script may take, in seconds. */
	private static final long SCRIPT_TIMEOUT_SECONDS = 30;

	/** How long a stopped script may take to end, in seconds. */
	private static final long STOP_TIMEOUT_SECONDS = 5;

	/** A call into a test subject that starts the trusted script. */
	@FunctionalInterface
	private interface ScriptLauncher {

		/**
		 * Starts the script.
		 *
		 * @return the started process
		 * @throws IOException if the script cannot be started
		 */
		Process launch() throws IOException;
	}

	/**
	 * A started script that is stopped and tidied up when the check is finished.
	 * Anything going wrong while tidying up is reported beside a failed check
	 * rather than instead of it.
	 *
	 * @param process the started script
	 */
	private record StartedScript(Process process) implements AutoCloseable {

		/**
		 * Stops the script, then closes its three streams and removes the file it
		 * wrote. Each of these is tried even if an earlier one failed. Fails if the
		 * script does not end in time.
		 *
		 * @throws IOException          if a stream or the file cannot be closed or
		 *                              removed
		 * @throws InterruptedException if waiting for the script is interrupted
		 */
		@Override
		public void close() throws IOException, InterruptedException {
			Closeable outputFile = () -> Files.deleteIfExists(TRUSTED_OUTPUT);
			Closeable output = process.getInputStream();
			Closeable errors = process.getErrorStream();
			Closeable input = process.getOutputStream();
			try (outputFile; output; errors; input) {
				process.destroyForcibly();
				Assertions.assertTrue(process.waitFor(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS),
						"The stopped script did not end.");
			}
		}
	}

	/**
	 * Checks that Ares lets the script start and that the script really ran, by
	 * reading the file it writes.
	 *
	 * @param launcher the call that starts the script
	 * @throws IOException          if the output cannot be read or removed
	 * @throws InterruptedException if waiting for the script is interrupted
	 */
	private void assertTrustedScriptRan(ScriptLauncher launcher) throws IOException, InterruptedException {
		Files.deleteIfExists(TRUSTED_OUTPUT);
		Process process = Assertions.assertDoesNotThrow(launcher::launch, ERROR_SECURITY_EXCEPTION);
		try (StartedScript script = new StartedScript(process)) {
			assertWroteExpectedOutput(script.process());
		}
	}

	/**
	 * Checks that the process ended in time and successfully, and wrote the
	 * expected sentence.
	 *
	 * @param process the started script
	 * @throws IOException          if the output cannot be read
	 * @throws InterruptedException if waiting for the script is interrupted
	 */
	private static void assertWroteExpectedOutput(Process process) throws IOException, InterruptedException {
		Assertions.assertTrue(process.waitFor(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS), "The script did not finish.");
		Assertions.assertEquals(0, process.exitValue(), "The script ended with an error.");
		Assertions.assertEquals(EXPECTED_OUTPUT, Files.readString(TRUSTED_OUTPUT).strip(),
				"The script did not write the expected sentence.");
	}

	private static final String RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/runtime";
	private static final String PROCESS_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/processBuilder";
	private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/thirdPartyPackage";
	private static final String NATIVE_COMMAND_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/nativeCommand";
	private static final String SCRIPT_ENGINE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/scriptEngine";
	private static final String URL_CLASS_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/urlClassLoader";
	private static final String REFLECTION_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/reflectionLoader";
	private static final String SERVICE_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/serviceLoader";

	// <editor-fold desc="accessFileSystemViaRuntime">
	/**
	 * Starts the trusted script through Runtime.exec with a string under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntime(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with a string under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntime(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with a string under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntime(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with a string under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntime(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaRuntimeArray">
	/**
	 * Starts the trusted script through Runtime.exec with an array under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntimeArray(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with an array under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntimeArray(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with an array under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntimeArray(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through Runtime.exec with an array under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = RUNTIME_WITHIN_PATH)
	void test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteRuntimeMain.accessFileSystemViaRuntimeArray(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaProcessBuilder">
	/**
	 * Starts the trusted script through ProcessBuilder under ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteProcessBuilderMain.accessFileSystemViaProcessBuilder(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through ProcessBuilder under ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteProcessBuilderMain.accessFileSystemViaProcessBuilder(TRUSTED_SCRIPT));
	}

	/** Starts the trusted script through ProcessBuilder under WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteProcessBuilderMain.accessFileSystemViaProcessBuilder(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through ProcessBuilder under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteProcessBuilderMain.accessFileSystemViaProcessBuilder(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaThirdPartyPackage">
	/**
	 * Starts the trusted script through a class from another package under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a class from another package under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a class from another package under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a class from another package under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaNativeCommand">
	/**
	 * Starts the trusted script through a native command under ArchUnit and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = NATIVE_COMMAND_WITHIN_PATH)
	void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteNativeCommandMain.accessFileSystemViaNativeCommand(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a native command under ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = NATIVE_COMMAND_WITHIN_PATH)
	void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteNativeCommandMain.accessFileSystemViaNativeCommand(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a native command under WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = NATIVE_COMMAND_WITHIN_PATH)
	void test_accessFileSystemViaNativeCommandMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteNativeCommandMain.accessFileSystemViaNativeCommand(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through a native command under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = NATIVE_COMMAND_WITHIN_PATH)
	void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteNativeCommandMain.accessFileSystemViaNativeCommand(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaScriptEngine">
	/**
	 * Starts the trusted script through the script-engine subject under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
	void test_accessFileSystemViaScriptEngineMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteScriptEngineMain.accessFileSystemViaScriptEngine(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the script-engine subject under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
	void test_accessFileSystemViaScriptEngineMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteScriptEngineMain.accessFileSystemViaScriptEngine(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the script-engine subject under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
	void test_accessFileSystemViaScriptEngineMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteScriptEngineMain.accessFileSystemViaScriptEngine(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the script-engine subject under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
	void test_accessFileSystemViaScriptEngineMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteScriptEngineMain.accessFileSystemViaScriptEngine(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaURLClassLoader">
	/**
	 * Starts the trusted script through the URL-class-loader subject under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaURLClassLoaderMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteURLClassLoaderMain.accessFileSystemViaURLClassLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the URL-class-loader subject under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaURLClassLoaderMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteURLClassLoaderMain.accessFileSystemViaURLClassLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the URL-class-loader subject under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaURLClassLoaderMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteURLClassLoaderMain.accessFileSystemViaURLClassLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the URL-class-loader subject under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaURLClassLoaderMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteURLClassLoaderMain.accessFileSystemViaURLClassLoader(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaReflectionLoader">
	/**
	 * Starts the trusted script through the reflection-loader subject under
	 * ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = REFLECTION_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaReflectionLoaderMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteReflectionLoaderMain.accessFileSystemViaReflectionLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the reflection-loader subject under
	 * ArchUnit and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = REFLECTION_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaReflectionLoaderMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteReflectionLoaderMain.accessFileSystemViaReflectionLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the reflection-loader subject under WALA
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = REFLECTION_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaReflectionLoaderMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteReflectionLoaderMain.accessFileSystemViaReflectionLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the reflection-loader subject under WALA
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = REFLECTION_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaReflectionLoaderMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteReflectionLoaderMain.accessFileSystemViaReflectionLoader(TRUSTED_SCRIPT));
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaServiceLoader">
	/**
	 * Starts the trusted script through the service-loader subject under ArchUnit
	 * and AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_EXECUTE, withinPath = SERVICE_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaServiceLoaderMavenArchunitAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteServiceLoaderMain.accessFileSystemViaServiceLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the service-loader subject under ArchUnit
	 * and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_EXECUTE, withinPath = SERVICE_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaServiceLoaderMavenArchunitInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteServiceLoaderMain.accessFileSystemViaServiceLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the service-loader subject under WALA and
	 * AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_ASPECTJ_EXECUTE, withinPath = SERVICE_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaServiceLoaderMavenWalaAspectJ() throws Exception {
		assertTrustedScriptRan(() -> ExecuteServiceLoaderMain.accessFileSystemViaServiceLoader(TRUSTED_SCRIPT));
	}

	/**
	 * Starts the trusted script through the service-loader subject under WALA and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_EXECUTE, withinPath = SERVICE_LOADER_WITHIN_PATH)
	void test_accessFileSystemViaServiceLoaderMavenWalaInstrumentation() throws Exception {
		assertTrustedScriptRan(() -> ExecuteServiceLoaderMain.accessFileSystemViaServiceLoader(TRUSTED_SCRIPT));
	}
	// </editor-fold>
}
