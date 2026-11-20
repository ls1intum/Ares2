package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;

/**
 * Checks how a test's output echo and limit are resolved: the annotation first,
 * then the active policy, then today's defaults. Uses real policy files.
 */
class ConfigurationUtilsOutputMirroringTest {

	/** Where the output-mirroring policy fixtures live. */
	private static final String FIXTURES = "src/test/resources/de/tum/cit/ase/ares/api/internal/outputMirroring/";

	/** The policy applies when there is no annotation. */
	@Test
	void thePolicyAppliesWithoutAnAnnotation() throws Exception {
		TestContext context = contextFor("policyOnly");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isTrue();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(10);
	}

	/**
	 * An annotation decides mirroring, but the policy's lower limit still applies.
	 */
	@Test
	void anAnnotationKeepsThePolicysLowerLimit() throws Exception {
		TestContext context = contextFor("annotationAndPolicy");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isFalse();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(10);
	}

	/** An annotation's own lower limit applies under a higher policy limit. */
	@Test
	void anAnnotationsLowerLimitApplies() throws Exception {
		TestContext context = contextFor("lowerAnnotationAndPolicy");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isTrue();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(5);
	}

	/**
	 * A policy that writes no limit leaves the annotation's limit alone, even above
	 * the default.
	 */
	@Test
	void aPolicyWithoutALimitLeavesTheAnnotationsLimit() throws Exception {
		TestContext context = contextFor("higherAnnotationAndMirrorOnlyPolicy");

		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(MirrorOutput.DEFAULT_MAX_STD_OUT + 1);
	}

	/** A policy setting only the limit leaves mirroring off. */
	@Test
	void aLimitOnlyPolicyLeavesMirroringOff() throws Exception {
		TestContext context = contextFor("limitOnly");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isFalse();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(10);
	}

	/** Without a policy, only the annotation counts, so the defaults apply. */
	@Test
	void noPolicyMeansAnnotationOnly() throws Exception {
		TestContext context = contextFor("neither");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isFalse();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	/** A deactivated policy contributes nothing. */
	@Test
	void deactivatedPolicyMeansAnnotationOnly() throws Exception {
		TestContext context = contextFor("deactivated");

		assertThat(ConfigurationUtils.shouldMirrorOutput(context)).isFalse();
		assertThat(ConfigurationUtils.getMaxStandardOutput(context)).isEqualTo(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	/** An active policy without the category contributes nothing. */
	@Test
	void aPolicyWithoutTheCategoryContributesNothing() throws Exception {
		assertThat(ConfigurationUtils.findPolicyOutput(contextFor("withoutCategory"))).isEmpty();
	}

	/**
	 * A context for one of this class's fixture methods.
	 *
	 * @param methodName the fixture's name.
	 * @return the context
	 * @throws NoSuchMethodException if the fixture does not exist
	 */
	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = ConfigurationUtilsOutputMirroringTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(ConfigurationUtilsOutputMirroringTest.class));
		return context;
	}

	/** Runs under a policy that mirrors and limits to 10. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(FIXTURES + "PolicyOutputMirroringFull.yaml")
	private static void policyOnly() {
		// Read reflectively through the mocked context.
	}

	/** Runs under the same policy with an annotation that keeps the defaults. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(FIXTURES + "PolicyOutputMirroringFull.yaml")
	private static void annotationAndPolicy() {
		// Read reflectively through the mocked context.
	}

	/** Runs under the policy limiting to 10 with an annotation limiting to 5. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@MirrorOutput(value = MirrorOutputPolicy.ENABLED, maxCharCount = 5)
	@Policy(FIXTURES + "PolicyOutputMirroringFull.yaml")
	private static void lowerAnnotationAndPolicy() {
		// Read reflectively through the mocked context.
	}

	/** Runs under a policy without a limit and an annotation above the default. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@MirrorOutput(value = MirrorOutputPolicy.DISABLED, maxCharCount = MirrorOutput.DEFAULT_MAX_STD_OUT + 1)
	@Policy(FIXTURES + "PolicyOutputMirroringMirrorOnly.yaml")
	private static void higherAnnotationAndMirrorOnlyPolicy() {
		// Read reflectively through the mocked context.
	}

	/** Runs under a policy that only sets the limit. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(FIXTURES + "PolicyOutputMirroringLimitOnly.yaml")
	private static void limitOnly() {
		// Read reflectively through the mocked context.
	}

	/** Runs under neither. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private static void neither() {
		// Read reflectively through the mocked context.
	}

	/** Runs under a deactivated policy. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = FIXTURES + "PolicyOutputMirroringFull.yaml", activated = false)
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
