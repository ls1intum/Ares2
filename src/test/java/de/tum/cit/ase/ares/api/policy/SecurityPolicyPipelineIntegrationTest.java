package de.tum.cit.ase.ares.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;

class SecurityPolicyPipelineIntegrationTest {
	/** A compiled fixture without nested classes, standing in for a test class. */
	private static final String TEST_CLASS = "example.nest.NestHostFixture$Evil";

	@TempDir
	Path temporaryDirectory;

	@Test
	void carriesFullyPopulatedYamlThroughRealMavenAndGradlePipelines() throws IOException {
		for (BuildMode mode : BuildMode.values()) {
			Path root = Files.createDirectory(temporaryDirectory.resolve(mode.name().toLowerCase()));
			if (mode == BuildMode.MAVEN) {
				Files.writeString(root.resolve("pom.xml"), "<project/>");
			} else {
				Files.writeString(root.resolve("build.gradle.kts"), "plugins { java }");
			}
			compileFixtureInto(root, mode);
			Path policy = root.resolve("policy.YmL");
			Files.writeString(policy, yaml(mode));
			SecurityPolicyReaderAndDirector pipeline = SecurityPolicyReaderAndDirector.builder()
					.securityPolicyFilePath(policy).projectFolderPath(root).build().createTestCases();
			TestCaseAbstractFactoryAndBuilder factory = pipeline.factoryAndBuilder();
			assertEquals(mode, factory.buildMode());
			assertEquals(ArchitectureMode.ARCHUNIT, factory.architectureMode());
			assertEquals(AOPMode.ASPECTJ, factory.aopMode());
			assertEquals(List.of(TEST_CLASS), factory.testClasses());
			var resources = factory.resourceAccesses();
			assertEquals(root.toAbsolutePath().normalize() + "/data",
					resources.regardingFileSystemInteractions().get(0).onThisPathAndAllPathsBelow());
			assertEquals(0, resources.regardingNetworkConnections().get(0).onThePort());
			assertEquals(List.of("--version"), resources.regardingCommandExecutions().get(0).withTheseArguments());
			assertEquals(2, resources.regardingThreadCreations().get(0).createTheFollowingNumberOfThreads());
			assertEquals("java.util", resources.regardingPackageImports().get(0).importTheFollowingPackage());
			assertEquals(5000, resources.regardingTimeouts().get(0).timeout());
		}
	}

	/**
	 * A declared test class that was never compiled stops the real pipeline for
	 * both build tools, while the same policy naming a compiled class passes.
	 */
	@Test
	void aDeclaredTestClassThatWasNeverCompiledStopsThePipeline() throws IOException {
		for (BuildMode mode : BuildMode.values()) {
			Path root = Files.createDirectory(temporaryDirectory.resolve("ghost-" + mode.name().toLowerCase()));
			Files.writeString(root.resolve(mode == BuildMode.MAVEN ? "pom.xml" : "build.gradle.kts"),
					mode == BuildMode.MAVEN ? "<project/>" : "plugins { java }");
			compileFixtureInto(root, mode);
			Path policy = root.resolve("policy.yaml");
			Files.writeString(policy, yaml(mode).replace("[" + TEST_CLASS + "]", "[com.example.GhostTest]"));
			SecurityPolicyReaderAndDirector pipeline = SecurityPolicyReaderAndDirector.builder()
					.securityPolicyFilePath(policy).projectFolderPath(root).build();
			SecurityException refused = assertThrows(SecurityException.class, pipeline::createTestCases);
			assertTrue(refused.getMessage().contains("com.example.GhostTest"), refused.getMessage());
		}
	}

	@Test
	void invalidPolicyFailsBeforeAFactoryExists() throws IOException {
		Path root = Files.createDirectory(temporaryDirectory.resolve("invalid"));
		Files.writeString(root.resolve("pom.xml"), "<project/>");
		Path policy = root.resolve("policy.yaml");
		Files.writeString(policy, yaml(BuildMode.MAVEN).replace("onThePort: 0", "onThePort: null"));
		SecurityPolicyReaderAndDirector pipeline = SecurityPolicyReaderAndDirector.builder()
				.securityPolicyFilePath(policy).projectFolderPath(root).build();
		assertThrows(SecurityException.class, pipeline::createTestCases);
		assertThrows(NullPointerException.class, pipeline::factoryAndBuilder);
	}

	/**
	 * Places the compiled test-class fixture where the build tool keeps compiled
	 * tests.
	 */
	private static void compileFixtureInto(Path root, BuildMode mode) throws IOException {
		Path testOutput = root.resolve(mode == BuildMode.MAVEN ? "target/test-classes" : "build/classes/java/test");
		Path file = testOutput.resolve("example/nest/NestHostFixture$Evil.class");
		Files.createDirectories(file.getParent());
		try (InputStream input = SecurityPolicyPipelineIntegrationTest.class.getClassLoader()
				.getResourceAsStream("example/nest/NestHostFixture$Evil.class")) {
			Files.write(file, Objects.requireNonNull(input, "the fixture is compiled").readAllBytes());
		}
	}

	private String yaml(BuildMode mode) {
		String configuration = mode == BuildMode.MAVEN ? "JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ"
				: "JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ";
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: %s
				  theSupervisedCodeUsesTheFollowingPackage: com.example
				  theMainClassInsideThisPackageIs: Main
				  theFollowingClassesAreTestClasses: [%s]
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions:
				      - onThisPathAndAllPathsBelow: ${PROJECT_ROOT}/data
				        readAllFiles: true
				        overwriteAllFiles: false
				        createAllFiles: true
				        executeAllFiles: false
				        deleteAllFiles: false
				    regardingNetworkConnections:
				      - onTheHost: localhost
				        onThePort: 0
				        openConnections: true
				        sendData: false
				        receiveData: true
				    regardingCommandExecutions:
				      - executeTheCommand: java
				        withTheseArguments: [--version]
				    regardingThreadCreations:
				      - createTheFollowingNumberOfThreads: 2
				        ofThisClass: java.lang.Thread
				    regardingPackageImports:
				      - importTheFollowingPackage: java.util
				    regardingTimeouts:
				      - timeout: 5000
				""".formatted(configuration, TEST_CLASS);
	}
}
