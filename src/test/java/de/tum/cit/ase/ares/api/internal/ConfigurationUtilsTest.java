package de.tum.cit.ase.ares.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedSettingsClassTestSupport;

/**
 * Checks which message a failed test shows instead of its real error, across
 * annotation, policy and generated settings class.
 */
class ConfigurationUtilsTest {

	/** A test class compiled beside a settings class, as a precompile run does. */
	private static final String BESIDE_TEST_CLASS = "com.example.exercise.GeneratedBesideTest";

	/** Set only if a student-supplied settings class's static initialiser runs. */
	private static final String INITIALISER_RAN_PROPERTY = "ares.test.studentSettingsInitialiserRan";

	/**
	 * A policy enabling privileged-exceptions-only reporting with its own message.
	 */
	private static final String POLICY_ENABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsEnabled.yaml";
	/** A policy explicitly disabling privileged-exceptions-only reporting. */
	private static final String POLICY_DISABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsDisabled.yaml";
	/** A policy without the {@code theFollowingTestBehaviorIsConfigured} field. */
	private static final String POLICY_WITHOUT_BEHAVIOR = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyWithoutTestBehavior.yaml";

	/** A test class with neither annotation nor policy. */
	static class PlainFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class with only {@code @PrivilegedExceptionsOnly}. */
	@PrivilegedExceptionsOnly("Annotation message")
	static class AnnotationOnlyFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class whose policy enables the default. */
	@Policy(value = POLICY_ENABLED)
	static class PolicyEnabledFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class whose policy disables the default. */
	@Policy(value = POLICY_DISABLED)
	static class PolicyDisabledFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class whose policy does not mention the setting. */
	@Policy(value = POLICY_WITHOUT_BEHAVIOR)
	static class PolicyWithoutBehaviorFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class whose enabling policy is switched off. */
	@Policy(value = POLICY_ENABLED, activated = false)
	static class PolicyInactiveFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class with both the annotation and an enabling policy. */
	@PrivilegedExceptionsOnly("Annotation message")
	@Policy(value = POLICY_ENABLED)
	static class AnnotationAndPolicyEnabledFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class with the annotation and a disabling policy. */
	@PrivilegedExceptionsOnly("Annotation message")
	@Policy(value = POLICY_DISABLED)
	static class AnnotationAndPolicyDisabledFixture {
		/** The fixture's test method, looked up by name. */
		void test() {
		}
	}

	/** A test class annotated itself, with one method annotated too. */
	@PrivilegedExceptionsOnly("Class message")
	static class ClassAndMethodAnnotationFixture {
		/** A test method with its own annotation. */
		@PrivilegedExceptionsOnly("Method message")
		void methodWithOwnAnnotation() {
		}

		/** A test method relying on its class's annotation. */
		void methodWithoutOwnAnnotation() {
		}
	}

	/** An annotated class enclosing a test class that has none. */
	@PrivilegedExceptionsOnly("Enclosing message")
	static class EnclosingClassFixture {
		/** The unannotated test class inside {@link EnclosingClassFixture}. */
		static class Inner {
			/** The fixture's test method, looked up by name. */
			void test() {
			}
		}
	}

	/** Without annotation or policy the real error is shown. */
	@Test
	void neitherAnnotationNorPolicyReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(context(PlainFixture.class));

		assertFalse(message.isPresent());
	}

	/** The annotation's message is used when only the annotation is present. */
	@Test
	void annotationOnlyReturnsAnnotationMessage() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationOnlyFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	/** An enabling policy's message is used when there is no annotation. */
	@Test
	void policyEnabledOnlyReturnsPolicyMessage() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyEnabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Policy message", message.get());
	}

	/** A disabling policy shows the real error. */
	@Test
	void policyDisabledOnlyReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyDisabledFixture.class));

		assertFalse(message.isPresent());
	}

	/** A policy that does not mention the setting shows the real error. */
	@Test
	void policyWithoutBehaviorWrapperReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyWithoutBehaviorFixture.class));

		assertFalse(message.isPresent());
	}

	/** A switched-off policy never enables the default. */
	@Test
	void inactivePolicyNeverActivatesTheBehaviouralDefault() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyInactiveFixture.class));

		assertFalse(message.isPresent());
	}

	/** The annotation's message wins over an enabling policy's. */
	@Test
	void annotationWinsOverPolicyEnabled() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationAndPolicyEnabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	/** The annotation still applies when the policy disables the default. */
	@Test
	void annotationStillWinsOverPolicyDisabled() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationAndPolicyDisabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	/** A method's own annotation wins over its class's. */
	@Test
	void methodAnnotationOverridesClassLevel() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(
				context(ClassAndMethodAnnotationFixture.class, "methodWithOwnAnnotation"));

		assertTrue(message.isPresent());
		assertEquals("Method message", message.get());
	}

	/** A class annotation applies to a method without its own. */
	@Test
	void classLevelAnnotationAppliesWhenMethodHasNone() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(
				context(ClassAndMethodAnnotationFixture.class, "methodWithoutOwnAnnotation"));

		assertTrue(message.isPresent());
		assertEquals("Class message", message.get());
	}

	/** An enclosing class's annotation applies to a nested test class. */
	@Test
	void enclosingClassAnnotationAppliesToInnerClassTest() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(EnclosingClassFixture.Inner.class));

		assertTrue(message.isPresent());
		assertEquals("Enclosing message", message.get());
	}

	/**
	 * A disabling policy is final, even when an enabled generated settings class
	 * exists.
	 */
	@Test
	void explicitlyDisabledPolicyWinsOverAnEnabledGeneratedSettingsClass(@TempDir Path tempDir) throws Exception {
		ClassLoader isolated = GeneratedSettingsClassTestSupport.compileSource(tempDir,
				TestBehaviorConfiguration.GENERATED_CLASS_NAME,
				GeneratedSettingsClassTestSupport.settingsClassSource(TestBehaviorConfiguration.GENERATED_CLASS_NAME,
						"public static final boolean "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME + " = true;",
						"public static final String "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME
								+ " = \"Generated settings message\";"));
		GeneratedSettingsClassTestSupport.runWithClassLoader(isolated, () -> {
			Optional<String> message = ConfigurationUtils
					.getNonprivilegedFailureMessage(context(PolicyDisabledFixture.class));

			assertFalse(message.isPresent());
		});
	}

	/**
	 * A settings class beside the test classes that lacks an expected field comes
	 * from another Ares version and must fail closed, not count as unconfigured.
	 */
	@Test
	void generatedSettingsClassMissingExpectedFieldFailsClosed(@TempDir Path tempDir) throws Exception {
		GeneratedSettingsClassTestSupport.compileSource(tempDir, TestBehaviorConfiguration.GENERATED_CLASS_NAME,
				GeneratedSettingsClassTestSupport.settingsClassSource(TestBehaviorConfiguration.GENERATED_CLASS_NAME,
						"public static final boolean "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME + " = true;"));
		Class<?> testClass = GeneratedSettingsClassTestSupport.compileTestClassBeside(tempDir, BESIDE_TEST_CLASS);
		GeneratedSettingsClassTestSupport.runWithClassLoader(testClass.getClassLoader(), () -> {
			SecurityException failure = assertThrows(SecurityException.class,
					() -> ConfigurationUtils.getNonprivilegedFailureMessage(context(testClass)));

			assertInstanceOf(NoSuchFieldException.class, failure.getCause());
		});
	}

	/**
	 * A settings class compiled into the same output as the running test class is
	 * the generated one, and its message is used.
	 */
	@Test
	void generatedSettingsClassBesideTheTestClassIsRead(@TempDir Path tempDir) throws Exception {
		compileEnabledSettingsClass(tempDir, "");
		Class<?> testClass = GeneratedSettingsClassTestSupport.compileTestClassBeside(tempDir, BESIDE_TEST_CLASS);
		GeneratedSettingsClassTestSupport.runWithClassLoader(testClass.getClassLoader(), () -> {
			Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(context(testClass));

			assertEquals(Optional.of("Generated settings message"), message);
		});
	}

	/**
	 * A class carrying the generated name but loaded from somewhere other than the
	 * test classes, as student code would be, is rejected before its static
	 * initialiser can run.
	 */
	@Test
	void studentSuppliedSettingsClassIsRejectedWithoutRunningIt(@TempDir Path tempDir) throws Exception {
		ClassLoader studentOutput = compileEnabledSettingsClass(tempDir,
				"static { System.setProperty(\"" + INITIALISER_RAN_PROPERTY + "\", \"true\"); }");
		try {
			GeneratedSettingsClassTestSupport.runWithClassLoader(studentOutput, () -> {
				SecurityException failure = assertThrows(SecurityException.class,
						() -> ConfigurationUtils.getNonprivilegedFailureMessage(context(PlainFixture.class)));

				assertTrue(failure.getMessage().contains(TestBehaviorConfiguration.GENERATED_CLASS_NAME));
			});
			assertNull(System.getProperty(INITIALISER_RAN_PROPERTY));
		} finally {
			System.clearProperty(INITIALISER_RAN_PROPERTY);
		}
	}

	/**
	 * Compiles an enabled settings class with a fixed message into {@code tempDir},
	 * plus any extra member such as a static initialiser.
	 *
	 * @param tempDir     the directory to compile into.
	 * @param extraMember source text of one more member, or an empty string.
	 * @return a class loader rooted at {@code tempDir}.
	 * @throws IOException if writing or compiling the source fails.
	 */
	private static ClassLoader compileEnabledSettingsClass(Path tempDir, String extraMember) throws IOException {
		return GeneratedSettingsClassTestSupport.compileSource(tempDir, TestBehaviorConfiguration.GENERATED_CLASS_NAME,
				GeneratedSettingsClassTestSupport.settingsClassSource(TestBehaviorConfiguration.GENERATED_CLASS_NAME,
						"public static final boolean "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME + " = true;",
						"public static final String "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME
								+ " = \"Generated settings message\";",
						extraMember));
	}

	/**
	 * A context for the {@code test} method of {@code type}.
	 *
	 * @param type the fixture class.
	 * @return the context.
	 * @throws Exception if the method cannot be found.
	 */
	private static TestContext context(Class<?> type) throws Exception {
		return context(type, "test");
	}

	/**
	 * A context for the named method of {@code type}.
	 *
	 * @param type       the fixture class.
	 * @param methodName the method's name.
	 * @return the context.
	 * @throws Exception if the method cannot be found.
	 */
	private static TestContext context(Class<?> type, String methodName) throws Exception {
		return TestContextFixtures.of(type, methodName);
	}
}
