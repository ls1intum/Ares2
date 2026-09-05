package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

public class PrecompileTest {

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
	 * {@code createTestCases().writeTestCases(...)} as a real, readable generated
	 * resource, not merely that generation completes without throwing.
	 */
	@Test
	void writeTestCasesGeneratesTheTestBehaviorResourceWhenConfigured(@TempDir Path tempDir) throws IOException {
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

		Path generatedResource = written.stream()
				.filter(path -> path.getFileName().toString().equals("TestBehaviorConfiguration.properties"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("No generated test-behaviour resource among: " + written));
		assertEquals(
				projectFolderPath.resolve("src/test/resources")
						.resolve(TestBehaviorConfiguration.GENERATED_RESOURCE_PATH),
				generatedResource,
				"the resource must land under src/test/resources, the directory Maven actually copies onto the test classpath");
		assertTrue(Files.exists(generatedResource));
		String content = Files.readString(generatedResource);
		assertTrue(content.contains("regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported=true"));
		assertTrue(content.contains("regardingPrivilegedExceptions.theFailureMessageIs=Precompiled default message"));
	}

	/**
	 * Proves the precompile carry-forward claim on the read side, in isolation from
	 * {@code writeTestCases()}: with the generated resource placed directly on the
	 * classpath and no {@code @Policy} annotation anywhere - the genuine precompile
	 * condition, where nothing dynamically resolves a policy -
	 * {@code ConfigurationUtils} still resolves the effective message from it.
	 */
	@Test
	void generatedResourceGovernsBehaviourWithNoPolicyAnnotationPresent() throws Exception {
		writeGeneratedResource("regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported=true\n"
				+ "regardingPrivilegedExceptions.theFailureMessageIs=Generated resource message\n");

		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(NoAnnotationFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Generated resource message", message.get());
	}

	/**
	 * A present annotation still wins over the generated resource - precedence must
	 * hold identically in a precompile deployment, not only a postcompile one.
	 */
	@Test
	void annotationStillWinsOverTheGeneratedResource() throws Exception {
		writeGeneratedResource("regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported=true\n"
				+ "regardingPrivilegedExceptions.theFailureMessageIs=Generated resource message\n");

		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(context(AnnotatedFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	private Path generatedResourcePath;

	private void writeGeneratedResource(String content) throws IOException {
		generatedResourcePath = Path.of("target/test-classes")
				.resolve(TestBehaviorConfiguration.GENERATED_RESOURCE_PATH);
		Files.createDirectories(generatedResourcePath.getParent());
		Files.writeString(generatedResourcePath, content);
	}

	@AfterEach
	void deleteGeneratedResource() throws IOException {
		if (generatedResourcePath != null) {
			Files.deleteIfExists(generatedResourcePath);
			generatedResourcePath = null;
		}
	}

	static class NoAnnotationFixture {
		void test() {
		}
	}

	@PrivilegedExceptionsOnly("Annotation message")
	static class AnnotatedFixture {
		void test() {
		}
	}

	private static TestContext context(Class<?> type) throws Exception {
		Method method = type.getDeclaredMethod("test");
		return new TestContext() {
			@Override
			public Optional<Method> testMethod() {
				return Optional.of(method);
			}

			@Override
			public Optional<Class<?>> testClass() {
				return Optional.of(type);
			}

			@Override
			public Optional<Object> testInstance() {
				return Optional.empty();
			}

			@Override
			public Optional<String> displayName() {
				return Optional.of("test");
			}

			@Override
			public Optional<AnnotatedElement> annotatedElement() {
				return Optional.of(method);
			}

			@Override
			public Optional<TestType> findTestType() {
				return Optional.empty();
			}
		};
	}
}
