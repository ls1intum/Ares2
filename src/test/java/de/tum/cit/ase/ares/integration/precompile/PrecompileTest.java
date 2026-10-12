package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedSettingsClassTestSupport;

/**
 * Runs the precompile flow, which writes the security-test scaffold into an
 * exercise, and checks what it writes.
 */
public class PrecompileTest {

	/**
	 * Package of the made-up exercise the precompile tests write into. It must not
	 * contain {@code de.tum.cit.ase}, which the copy step rewrites.
	 */
	private static final String EXERCISE_PACKAGE = "de.tum.cit.aet.exercise";

	/**
	 * Exercises the precompile flow ({@code writeTestCases}) which generates the
	 * security-test scaffold (architecture/AOP helper classes, localisation
	 * resources and the Phobos files).
	 * <p>
	 * The scaffold is written into a JUnit-managed temporary directory rather than
	 * the current working directory, so the repository is never polluted. The write
	 * target is nested several levels deep on purpose: the localisation step places
	 * its {@code resources} folder as a sibling two levels up, and the Phobos step
	 * climbs three levels up, so a deep target keeps every generated artefact
	 * inside {@code tempDir}, which {@link TempDir} removes afterwards.
	 */
	@Test
	void testPrecompileJavaMavenArchunitInstrumentation(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(projectFolderPath).build().createTestCases()
				.writeTestCases(writeTarget);
	}

	/**
	 * Proves the precompile carry-forward claim on the write side: a policy
	 * configuring {@code regardingPrivilegedExceptions} survives
	 * {@code createTestCases().writeTestCases(...)} as a real, separately compiled
	 * settings class, not merely that generation completes without throwing.
	 */
	@Test
	void writeTestCasesGeneratesTheTestBehaviorSettingsClassWhenConfigured(@TempDir Path tempDir) throws Exception {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path policyFile = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policyFile, """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theSupervisedCodeUsesTheFollowingPackage: "com.example"
				  theMainClassInsideThisPackageIs: "Main"
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				  theFollowingTestBehaviorIsConfigured:
				    regardingPrivilegedExceptions:
				      onlyPrivilegedExceptionsAreReported: true
				      theFailureMessageIs: "Precompiled default message"
				""");
		Path writeTarget = projectFolderPath.resolve("src/test/java");

		List<Path> written = SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyFile)
				.projectFolderPath(projectFolderPath).build().createTestCases().writeTestCases(writeTarget);

		String simpleClassName = GeneratedSettingsClassTestSupport
				.simpleClassNameOf(TestBehaviorConfiguration.GENERATED_CLASS_NAME);
		Path generatedSettingsClass = written.stream()
				.filter(path -> path.getFileName().toString().equals(simpleClassName + ".java")).findFirst()
				.orElseThrow(() -> new AssertionError("No generated test-behaviour settings class among: " + written));
		assertEquals(writeTarget.resolve(TestBehaviorConfiguration.GENERATED_CLASS_NAME.replace('.', '/') + ".java"),
				generatedSettingsClass,
				"the settings class must land under src/test/java, compiled alongside the rest of the generated sources");
		assertTrue(Files.exists(generatedSettingsClass));

		ClassLoader compiled = GeneratedSettingsClassTestSupport.compile(generatedSettingsClass,
				tempDir.resolve("compiled-settings"));
		Class<?> settingsClass = Class.forName(TestBehaviorConfiguration.GENERATED_CLASS_NAME, true, compiled);
		Field enabledField = settingsClass.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME);
		Field messageField = settingsClass.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME);
		assertEquals(true, enabledField.get(null));
		assertEquals("Precompiled default message", messageField.get(null));
		assertTrue(Files.exists(writeTarget.resolve("de/tum/cit/ase/ares/generated/GeneratedFailureReporting.java")),
				"the failure-reporting hook must be generated beside the settings class");
		Path resources = projectFolderPath.resolve("src/test/resources");
		assertTrue(Files.exists(resources.resolve("com/example/ares/api/localization/messages.properties")),
				"the copied Messages looks for its bundle below the exercise package");
		assertTrue(Files.notExists(resources.resolve("ares/api/localization/messages.properties")),
				"no bundle may land outside the exercise package, where the copied Messages never looks");
	}

	/**
	 * Precompile carries the secure baseline's temp-file rules into an exercise:
	 * the copied deny list still names the two calls with a directory and not the
	 * two without one, the copied ArchUnit check lets the latter through, and the
	 * copied settings, agent and aspect carry the start-up freeze.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,INSTRUMENTATION", "ARCHUNIT,ASPECTJ", "WALA,INSTRUMENTATION", "WALA,ASPECTJ" })
	void precompileCarriesTheTempFileRulesAndTheFreeze(String analysis, String aop, @TempDir Path tempDir)
			throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"), policyWithOneReadPath(analysis, aop));

		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(projectFolderPath)
				.build().createTestCases().writeTestCases(writeTarget);

		Path generated = writeTarget.resolve(EXERCISE_PACKAGE.replace('.', '/')).resolve("ares/api");
		String denyList = Files.readString(generated.resolve(
				"templates/architecture/java/" + analysis.toLowerCase() + "/methods/file-system-access-methods.txt"));
		for (String withDirectory : List.of("java.io.File.createTempFile(", "java.nio.file.Files.createTempFile(")) {
			assertTrue(denyList.lines().anyMatch(line -> line.startsWith(withDirectory)),
					() -> withDirectory + " with a directory is missing from the copied deny list");
		}
		assertFalse(denyList.lines().anyMatch(line -> line.matches(
				"java\\.(io\\.File|nio\\.file\\.Files)\\.createTempFile\\((java\\.lang\\.String|Ljava/lang/String;), ?(java\\.lang\\.String|Ljava/lang/String;)(, ?java\\.nio\\.file\\.attribute\\.FileAttribute\\[\\]|\\[Ljava/nio/file/attribute/FileAttribute;)?\\)")),
				"a createTempFile call without a directory is still on the copied deny list");
		if ("ARCHUNIT".equals(analysis)) {
			assertContains(generated.resolve("architecture/java/archunit/TransitivelyAccessesMethodsCondition.java"),
					"STANDARD_ALLOWED_CALLS");
		} else {
			assertContains(generated.resolve("architecture/java/wala/CustomCallgraphBuilder.java"),
					"isStandardAllowed");
			assertContains(generated.resolve("architecture/java/wala/WalaRule.java"), "isStandardAllowed");
		}
		assertContains(generated.resolve("aop/java/JavaAOPTestCaseSettings.java"), "frozenDefaultTempDirectory");
		if ("INSTRUMENTATION".equals(aop)) {
			assertContains(generated.resolve("aop/java/instrumentation/JavaInstrumentationAgent.java"),
					"captureTrustedStartupValues();");
			assertContains(generated.resolve("aop/java/instrumentation/JavaInstrumentationAgent.java"), EXERCISE_PACKAGE
					+ ".ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions");
		} else {
			assertContains(
					generated.resolve("aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemAdviceDefinitions.aj"),
					"frozenDefaultTempDirectory");
		}
	}

	/**
	 * Checks that a generated file contains a text.
	 *
	 * @param file     the generated file
	 * @param expected the text it must contain
	 * @throws IOException if the file cannot be read
	 */
	private static void assertContains(Path file, String expected) throws IOException {
		assertTrue(Files.exists(file), () -> "precompile did not write " + file);
		try (Stream<String> lines = Files.lines(file)) {
			assertTrue(lines.anyMatch(line -> line.contains(expected)), () -> file + " does not contain " + expected);
		}
	}

	/**
	 * Returns a policy for the made-up exercise that grants reading one file, so
	 * precompile writes the runtime checks too.
	 *
	 * @param analysis {@code ARCHUNIT} or {@code WALA}
	 * @param aop      {@code INSTRUMENTATION} or {@code ASPECTJ}
	 * @return the policy text
	 */
	private static String policyWithOneReadPath(String analysis, String aop) {
		return String.join("\n", "thisPolicyFileCompliesToThePolicyVersion: 1", "regardingTheSupervisedCode:",
				"  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_" + analysis + "_AND_" + aop,
				"  theSupervisedCodeUsesTheFollowingPackage: \"" + EXERCISE_PACKAGE + "\"",
				"  theMainClassInsideThisPackageIs: \"Main\"", "  theFollowingClassesAreTestClasses: [ ]",
				"  theFollowingResourceAccessesArePermitted:", "    regardingFileSystemInteractions:",
				"      - readAllFiles: true", "        overwriteAllFiles: false", "        createAllFiles: false",
				"        executeAllFiles: false", "        deleteAllFiles: false",
				"        onThisPathAndAllPathsBelow: \"data.txt\"", "    regardingNetworkConnections: [ ]",
				"    regardingCommandExecutions: [ ]", "    regardingThreadCreations: [ ]",
				"    regardingPackageImports: [ ]", "    regardingTimeouts:", "      - timeout: 3000", "");
	}
}
