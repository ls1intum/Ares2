package de.tum.cit.ase.ares.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;

class SecurityPolicyPipelineIntegrationTest {
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
			Path policy = root.resolve("policy.YmL");
			Files.writeString(policy, yaml(mode));
			SecurityPolicyReaderAndDirector pipeline = SecurityPolicyReaderAndDirector.builder()
					.securityPolicyFilePath(policy).projectFolderPath(root).build().createTestCases();
			TestCaseAbstractFactoryAndBuilder factory = pipeline.factoryAndBuilder();
			assertEquals(mode, factory.buildMode());
			assertEquals(ArchitectureMode.ARCHUNIT, factory.architectureMode());
			assertEquals(AOPMode.ASPECTJ, factory.aopMode());
			assertEquals(List.of("com.example.PolicyTest"), factory.testClasses());
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

	private String yaml(BuildMode mode) {
		String configuration = mode == BuildMode.MAVEN ? "JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ"
				: "JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ";
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: %s
				  theSupervisedCodeUsesTheFollowingPackage: com.example
				  theMainClassInsideThisPackageIs: Main
				  theFollowingClassesAreTestClasses: [com.example.PolicyTest]
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
				""".formatted(configuration);
	}
}
