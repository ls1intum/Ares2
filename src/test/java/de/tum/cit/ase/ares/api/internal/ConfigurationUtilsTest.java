package de.tum.cit.ase.ares.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedSettingsClassTestSupport;

/**
 * Checks that only an active policy controls public failure-message
 * replacement.
 */
class ConfigurationUtilsTest {

	/** A policy that enables the fixed failure message. */
	private static final String POLICY_ENABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsEnabled.yaml";
	/** A policy that disables the fixed failure message. */
	private static final String POLICY_DISABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsDisabled.yaml";
	/** A policy without a test-behavior category. */
	private static final String POLICY_WITHOUT_BEHAVIOR = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyWithoutTestBehavior.yaml";

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

	/** A test class with a policy that omits test behavior. */
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

	/** An omitted behavior category leaves the original failure visible. */
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
		assertTrue(ConfigurationUtils.findPolicyPrivilegedExceptions(context(PolicyEnabledFixture.class))
				.filter(category -> category.onlyPrivilegedExceptionsAreReported()).isPresent());
		assertTrue(ConfigurationUtils.findPolicyPrivilegedExceptions(context(PolicyDisabledFixture.class))
				.filter(category -> !category.onlyPrivilegedExceptionsAreReported()).isPresent());
		assertFalse(ConfigurationUtils.findPolicyPrivilegedExceptions(context(PolicyWithoutBehaviorFixture.class))
				.isPresent());
		assertFalse(
				ConfigurationUtils.findPolicyPrivilegedExceptions(context(PolicyInactiveFixture.class)).isPresent());
	}

	/**
	 * A generated precompile setting cannot override a disabled postcompile policy.
	 */
	@Test
	void disabledPolicyWinsOverGeneratedSetting(@TempDir Path tempDir) throws Exception {
		ClassLoader isolated = GeneratedSettingsClassTestSupport.compileSource(tempDir,
				TestBehaviorConfiguration.GENERATED_CLASS_NAME,
				GeneratedSettingsClassTestSupport.settingsClassSource(TestBehaviorConfiguration.GENERATED_CLASS_NAME,
						"public static final boolean "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME + " = true;",
						"public static final String "
								+ TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME
								+ " = \"Generated message\";"));
		GeneratedSettingsClassTestSupport.runWithClassLoader(isolated, () -> assertFalse(
				ConfigurationUtils.getNonprivilegedFailureMessage(context(PolicyDisabledFixture.class)).isPresent()));
	}

	/** Makes a context for the fixture's test method. */
	private static TestContext context(Class<?> type) throws Exception {
		return TestContextFixtures.of(type, "test");
	}
}
