package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedSettingsClassTestSupport;

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
	}

	/**
	 * Proves the precompile carry-forward claim on the read side, in isolation from
	 * {@code writeTestCases()}: with the generated settings class placed directly
	 * on the classpath and no {@code @Policy} annotation anywhere - the genuine
	 * precompile condition, where nothing dynamically resolves a policy -
	 * {@code ConfigurationUtils} still resolves the effective message from it.
	 */
	@Test
	void generatedSettingsClassGovernsBehaviourWithNoPolicyAnnotationPresent(@TempDir Path tempDir) throws Exception {
		ClassLoader isolated = generatedSettingsClassLoader(tempDir, true, "Generated settings message");
		GeneratedSettingsClassTestSupport.runWithClassLoader(isolated, () -> {
			Optional<String> message = ConfigurationUtils
					.getNonprivilegedFailureMessage(context(NoAnnotationFixture.class));

			assertTrue(message.isPresent());
			assertEquals("Generated settings message", message.get());
		});
	}

	/**
	 * A present annotation still wins over the generated settings class -
	 * precedence must hold identically in a precompile deployment, not only a
	 * postcompile one.
	 */
	@Test
	void annotationStillWinsOverTheGeneratedSettingsClass(@TempDir Path tempDir) throws Exception {
		ClassLoader isolated = generatedSettingsClassLoader(tempDir, true, "Generated settings message");
		GeneratedSettingsClassTestSupport.runWithClassLoader(isolated, () -> {
			Optional<String> message = ConfigurationUtils
					.getNonprivilegedFailureMessage(context(AnnotatedFixture.class));

			assertTrue(message.isPresent());
			assertEquals("Annotation message", message.get());
		});
	}

	private ClassLoader generatedSettingsClassLoader(Path tempDir, boolean enabled, String message) throws IOException {
		return GeneratedSettingsClassTestSupport.compileSource(tempDir, TestBehaviorConfiguration.GENERATED_CLASS_NAME,
				GeneratedSettingsClassTestSupport.settingsClassSource(TestBehaviorConfiguration.GENERATED_CLASS_NAME,
						"public static final boolean "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME + " = " + enabled
								+ ";",
						"public static final String "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME + " = \"" + message
								+ "\";"));
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
