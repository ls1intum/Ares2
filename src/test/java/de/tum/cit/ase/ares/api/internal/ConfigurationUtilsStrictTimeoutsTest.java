package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutsConfiguration;

/**
 * Checks how the strict-timeout default is read from the policy a test runs
 * under, using real policy files.
 */
class ConfigurationUtilsStrictTimeoutsTest {

	/** Where the strict-timeout policy fixtures live. */
	private static final String FIXTURES = "src/test/resources/de/tum/cit/ase/ares/api/internal/strictTimeouts/";

	/** An active policy with the category yields it. */
	@Test
	void anActivePolicyYieldsItsCategory() throws Exception {
		Optional<StrictTimeoutsConfiguration> category = ConfigurationUtils
				.findPolicyStrictTimeouts(contextFor("withGrace"));

		assertThat(category).map(StrictTimeoutsConfiguration::timeout).contains(Duration.ofMillis(500));
		assertThat(category).flatMap(StrictTimeoutsConfiguration::terminationGrace).contains(Duration.ofMillis(10));
	}

	/** Without a policy, there is nothing to read. */
	@Test
	void noPolicyYieldsNothing() throws Exception {
		assertThat(ConfigurationUtils.findPolicyStrictTimeouts(contextFor("withoutPolicy"))).isEmpty();
	}

	/** A deactivated policy contributes nothing, even with the category. */
	@Test
	void aDeactivatedPolicyYieldsNothing() throws Exception {
		assertThat(ConfigurationUtils.findPolicyStrictTimeouts(contextFor("deactivated"))).isEmpty();
	}

	/** An active policy without the category yields nothing. */
	@Test
	void aPolicyWithoutTheCategoryYieldsNothing() throws Exception {
		assertThat(ConfigurationUtils.findPolicyStrictTimeouts(contextFor("withoutCategory"))).isEmpty();
	}

	/**
	 * A context for one of this class's fixture methods.
	 *
	 * @param methodName the fixture's name.
	 * @return the context
	 * @throws NoSuchMethodException if the fixture does not exist
	 */
	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = ConfigurationUtilsStrictTimeoutsTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(ConfigurationUtilsStrictTimeoutsTest.class));
		return context;
	}

	/** Runs under a policy with a timeout and a grace period. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(FIXTURES + "PolicyStrictTimeoutsWithGrace.yaml")
	private static void withGrace() {
		// Read reflectively through the mocked context.
	}

	/** Runs under no policy. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private static void withoutPolicy() {
		// Read reflectively through the mocked context.
	}

	/** Runs under a deactivated policy with a timeout. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = FIXTURES + "PolicyStrictTimeoutsOnly.yaml", activated = false)
	private static void deactivated() {
		// Read reflectively through the mocked context.
	}

	/** Runs under a policy without the behaviour category. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(FIXTURES + "PolicyWithoutTestBehavior.yaml")
	private static void withoutCategory() {
		// Read reflectively through the mocked context.
	}
}
