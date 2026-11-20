package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.testutilities.MockStores;

/**
 * Checks that only an active policy controls public failure-message
 * replacement, that the removed annotation is gone, and that a policy's test
 * behaviour is read once per file and test class.
 */
class ConfigurationUtilsTest {

	/** A policy that enables the fixed failure message. */
	private static final String POLICY_ENABLED = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelPrivilegedLowRuntime.yaml";
	/** A policy that disables the fixed failure message. */
	private static final String POLICY_DISABLED = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelLowRuntime.yaml";
	/** A policy without a test-behaviour category. */
	private static final String POLICY_WITHOUT_BEHAVIOR = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml";

	/** A test class with no policy. */
	static class PlainFixture {
		/** The method used to make a test context. */
		void test() {
		}
	}

	/** A test class with an enabling policy. */
	@Policy(value = POLICY_ENABLED)
	static class PolicyEnabledFixture {
		/** The method used to make a test context. */
		void test() {
		}
	}

	/** A test class with a disabling policy. */
	@Policy(value = POLICY_DISABLED)
	static class PolicyDisabledFixture {
		/** The method used to make a test context. */
		void test() {
		}
	}

	/** A test class with a policy that omits test behaviour. */
	@Policy(value = POLICY_WITHOUT_BEHAVIOR)
	static class PolicyWithoutBehaviorFixture {
		/** The method used to make a test context. */
		void test() {
		}
	}

	/** A test class with a deactivated policy. */
	@Policy(value = POLICY_ENABLED, activated = false)
	static class PolicyInactiveFixture {
		/** The method used to make a test context. */
		void test() {
		}
	}

	/** No policy leaves ordinary failure reporting in effect. */
	@Test
	void noPolicyReturnsEmpty() throws Exception {
		assertFalse(ConfigurationUtils.getNonprivilegedFailureMessage(context(PlainFixture.class)).isPresent());
	}

	/** The active policy supplies its configured message. */
	@Test
	void enabledPolicyReturnsPolicyMessage() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyEnabledFixture.class));
		assertTrue(message.isPresent());
		assertEquals("Policy message", message.get());
	}

	/** A disabled setting leaves the original failure visible. */
	@Test
	void disabledPolicyReturnsEmpty() throws Exception {
		assertFalse(
				ConfigurationUtils.getNonprivilegedFailureMessage(context(PolicyDisabledFixture.class)).isPresent());
	}

	/** An omitted behaviour category leaves the original failure visible. */
	@Test
	void missingBehaviorReturnsEmpty() throws Exception {
		assertFalse(ConfigurationUtils.getNonprivilegedFailureMessage(context(PolicyWithoutBehaviorFixture.class))
				.isPresent());
	}

	/** A deactivated policy contributes no failure-message setting. */
	@Test
	void inactivePolicyReturnsEmpty() throws Exception {
		assertFalse(
				ConfigurationUtils.getNonprivilegedFailureMessage(context(PolicyInactiveFixture.class)).isPresent());
	}

	/** The policy category is available only while its policy is active. */
	@Test
	void policyCategoryFollowsActivation() throws Exception {
		assertTrue(ConfigurationUtils.findPolicyFailureFeedback(context(PolicyEnabledFixture.class))
				.filter(category -> category.onlyPrivilegedExceptionsAreReported()).isPresent());
		assertTrue(ConfigurationUtils.findPolicyFailureFeedback(context(PolicyDisabledFixture.class))
				.filter(category -> !category.onlyPrivilegedExceptionsAreReported()).isPresent());
		assertFalse(
				ConfigurationUtils.findPolicyFailureFeedback(context(PolicyWithoutBehaviorFixture.class)).isPresent());
		assertFalse(ConfigurationUtils.findPolicyFailureFeedback(context(PolicyInactiveFixture.class)).isPresent());
	}

	/** Makes a context for the fixture's test method. */
	private static TestContext context(Class<?> type) throws Exception {
		return TestContextFixtures.of(type, "test");
	}

	/**
	 * Source importing the removed annotation fails to compile.
	 *
	 * @param tempDir where the source is written.
	 * @throws Exception if the source cannot be written
	 */
	@Test
	void removedAnnotationDoesNotCompile(@TempDir Path tempDir) throws Exception {
		Path source = tempDir.resolve("OldAnnotationImport.java");
		Files.writeString(source,
				"import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly; class OldAnnotationImport {} ");
		ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
		int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-classpath",
				System.getProperty("java.class.path"), source.toString());
		assertThat(exitCode).isNotZero();
		assertThat(diagnostics.toString(StandardCharsets.UTF_8)).contains("PrivilegedExceptionsOnly");
	}

	/** The class-level policy, rewritten by the tests. */
	private static final String CLASS_POLICY = "target/test-behavior-cache/ClassPolicy.yaml";

	/** The method-level policy, with a different message. */
	private static final String METHOD_POLICY = "target/test-behavior-cache/MethodPolicy.yaml";

	/** Writes both policies before each test. */
	@BeforeEach
	void writePolicies() throws IOException {
		write(CLASS_POLICY, "2s");
		write(METHOD_POLICY, "7s");
	}

	/** A rewrite after the first read changes nothing for the same class. */
	@Test
	void aRewriteAfterTheFirstReadChangesNothing() throws Exception {
		ExtensionContext classContext = classContext();
		JupiterContext context = JupiterContext.of(methodContext(classContext, "classPolicyTest"));

		ConfigurationUtils.prepareTestBehavior(context);
		write(CLASS_POLICY, "9s");

		assertThat(ConfigurationUtils.findPolicyFailureFeedback(context).orElseThrow()
				.theFailureMessageForOtherExceptionsIs()).isEqualTo("2s");
	}

	/**
	 * A policy file removed after the first read keeps its settings for the same
	 * class, since the earlier reading is used before the file is looked for.
	 */
	@Test
	void aRemovedPolicyKeepsItsFirstReading() throws Exception {
		ExtensionContext classContext = classContext();
		JupiterContext context = JupiterContext.of(methodContext(classContext, "classPolicyTest"));

		ConfigurationUtils.prepareTestBehavior(context);
		Files.delete(Path.of(CLASS_POLICY));

		assertThat(ConfigurationUtils.findPolicyFailureFeedback(context).orElseThrow()
				.theFailureMessageForOtherExceptionsIs()).isEqualTo("2s");
	}

	/** Another test class reads the file afresh. */
	@Test
	void anotherClassReadsAfresh() throws Exception {
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest")));
		write(CLASS_POLICY, "9s");

		JupiterContext other = JupiterContext.of(methodContext(classContext(), "classPolicyTest"));
		assertThat(ConfigurationUtils.findPolicyFailureFeedback(other).orElseThrow()
				.theFailureMessageForOtherExceptionsIs()).isEqualTo("9s");
	}

	/** A method-level policy is read up front and keeps its own settings. */
	@Test
	void aMethodLevelPolicyKeepsItsOwnSettings() throws Exception {
		ExtensionContext classContext = classContext();
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(classContext, "classPolicyTest")));
		write(METHOD_POLICY, "9s");

		JupiterContext methodPolicy = JupiterContext.of(methodContext(classContext, "methodPolicyTest"));
		assertThat(ConfigurationUtils.findPolicyFailureFeedback(methodPolicy).orElseThrow()
				.theFailureMessageForOtherExceptionsIs()).isEqualTo("7s");
	}

	/** A deactivated policy contributes no test behaviour. */
	@Test
	void aDeactivatedPolicyContributesNothing() throws Exception {
		JupiterContext context = JupiterContext.of(methodContext(classContext(), "deactivatedPolicyTest"));
		assertThat(ConfigurationUtils.findPolicyFailureFeedback(context)).isEmpty();
	}

	/**
	 * Writes a policy with the given failure message.
	 *
	 * @param path    where to write it.
	 * @param message the message.
	 */
	private static void write(String path, String message) throws IOException {
		Path file = Path.of(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, """
				thisPolicyFileCompliesToThePolicyVersion: 2
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				  theFollowingTestBehaviorIsConfigured:
				    regardingTheFailureFeedbackForTheTestCases:
				      onlyPrivilegedExceptionsAreReported: true
				      theFailureMessageForOtherExceptionsIs: "%s"
				""".formatted(message));
	}

	/**
	 * A class context of {@link CacheFixture} with a working store.
	 *
	 * @return the context.
	 */
	private static ExtensionContext classContext() {
		ExtensionContext classContext = mock(ExtensionContext.class);
		ExtensionContext.Store store = MockStores.workingStore();
		when(classContext.getStore(any())).thenReturn(store);
		when(classContext.getTestClass()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getTestMethod()).thenReturn(Optional.empty());
		when(classContext.getElement()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getParent()).thenReturn(Optional.empty());
		return classContext;
	}

	/**
	 * A method context below a class context.
	 *
	 * @param classContext the class context.
	 * @param methodName   the fixture method.
	 * @return the context.
	 */
	private static ExtensionContext methodContext(ExtensionContext classContext, String methodName)
			throws NoSuchMethodException {
		Method method = CacheFixture.class.getDeclaredMethod(methodName);
		ExtensionContext methodContext = mock(ExtensionContext.class);
		when(methodContext.getTestClass()).thenReturn(Optional.of(CacheFixture.class));
		when(methodContext.getTestMethod()).thenReturn(Optional.of(method));
		when(methodContext.getElement()).thenReturn(Optional.of(method));
		when(methodContext.getParent()).thenReturn(Optional.of(classContext));
		return methodContext;
	}

	/** The test class whose policies are read. */
	@Policy(CLASS_POLICY)
	static final class CacheFixture {

		/** Uses the class policy. */
		void classPolicyTest() {
		}

		/** Uses its own policy. */
		@Policy(METHOD_POLICY)
		void methodPolicyTest() {
		}

		/** Uses a deactivated policy. */
		@Policy(value = CLASS_POLICY, activated = false)
		void deactivatedPolicyTest() {
		}
	}
}
