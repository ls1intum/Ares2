package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Checks that Precompile carries the FileSystemView checks into an exercise,
 * since the copied pointcuts are what enforces there, and that it does not copy
 * the weaving marker, which nothing in a Precompile run reads.
 */
class PrecompileFileSystemViewTest {

	/**
	 * Package of the made-up exercise. It must not contain {@code de.tum.cit.ase},
	 * which the copy step rewrites.
	 */
	private static final String EXERCISE_PACKAGE = "de.tum.cit.aet.exercise";

	/**
	 * Writes the Precompile output and checks the copied pointcuts of the selected
	 * runtime enforcement.
	 *
	 * @param analysis the static analysis, {@code ARCHUNIT} or {@code WALA}
	 * @param aop      the runtime enforcement, {@code ASPECTJ} or
	 *                 {@code INSTRUMENTATION}
	 * @param tempDir  the folder holding the made-up exercise
	 * @throws IOException if a file cannot be written or read
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,INSTRUMENTATION", "ARCHUNIT,ASPECTJ", "WALA,INSTRUMENTATION", "WALA,ASPECTJ" })
	void precompileCarriesTheFileSystemViewChecks(String analysis, String aop, @TempDir Path tempDir)
			throws IOException {
		Path project = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Files.createDirectories(project.resolve("target/classes"));
		Path writeTarget = Files.createDirectories(project.resolve("src/test/java"));
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"), policy(analysis, aop));

		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(writeTarget);

		Path generated = writeTarget.resolve(EXERCISE_PACKAGE.replace('.', '/')).resolve("ares/api/aop/java");
		if ("ASPECTJ".equals(aop)) {
			Path pointcuts = generated.resolve("aspectj/adviceandpointcut/JavaAspectJFileSystemPointcutDefinitions.aj");
			String text = Files.readString(pointcuts);
			for (String method : new String[] { "getFiles", "getChild", "isParent", "createNewFolder" }) {
				assertTrue(text.contains("javax.swing.filechooser.FileSystemView+." + method + "(..)"),
						() -> "the copied pointcuts miss FileSystemView." + method);
			}
			assertFalse(
					Files.exists(generated.resolve("aspectj/adviceandpointcut/JavaAspectJWovenMarkerDefinitions.aj")),
					"Precompile copied the weaving marker, which nothing in a Precompile run reads");
		} else {
			String text = Files.readString(
					generated.resolve("instrumentation/pointcut/JavaInstrumentationPointcutDefinitions.java"));
			assertTrue(text.contains(
					"Map.entry(\"javax.swing.filechooser.FileSystemView\", List.of(\"getFiles\", \"getChild\", \"isParent\"))"),
					"the copied read map misses FileSystemView");
			assertTrue(
					text.contains(
							"Map.entry(\"javax.swing.filechooser.FileSystemView\", List.of(\"createNewFolder\"))"),
					"the copied create map misses FileSystemView");
		}
	}

	/**
	 * Returns a policy for the made-up exercise that grants reading one file, so
	 * Precompile writes the runtime checks too.
	 *
	 * @param analysis {@code ARCHUNIT} or {@code WALA}
	 * @param aop      {@code INSTRUMENTATION} or {@code ASPECTJ}
	 * @return the policy text
	 */
	private static String policy(String analysis, String aop) {
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
