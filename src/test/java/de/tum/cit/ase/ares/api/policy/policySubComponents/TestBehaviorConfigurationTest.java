package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

class TestBehaviorConfigurationTest {

	@Test
	void builderBuildsAnEmptyConfiguration() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNotNull(configuration);
	}

	@Test
	void literalFieldAssignmentsIsEmptyWhenNoCategoryIsConfigured() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertTrue(configuration.literalFieldAssignments().isEmpty());
	}

	/** An empty configuration names no category. */
	@Test
	void anEmptyConfigurationNamesNoCategory() {
		assertThat(TestBehaviorConfiguration.builder().build().firstConfiguredCategory()).isEmpty();
	}

	/** Each category this branch reads, alone in a configuration. */
	static Stream<Arguments> categories() {
		return Stream.of(Arguments.of(TestBehaviorFieldNames.VISIBILITY_AND_SCHEDULE,
				TestBehaviorConfiguration.builder()
						.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
								.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
						.build()));
	}

	/**
	 * A version 1 policy built in code refuses a test-behaviour category, naming
	 * the version, the category and the version it needs.
	 *
	 * @param category the category's policy key.
	 * @param behavior a configuration setting only that category.
	 */
	@ParameterizedTest
	@MethodSource("categories")
	void aVersionOnePolicyRefusesTheCategory(String category, TestBehaviorConfiguration behavior) {
		assertThat(behavior.firstConfiguredCategory()).contains(category);
		assertThatThrownBy(() -> policy(1, behavior)).isInstanceOf(IllegalArgumentException.class).hasMessage(Messages
				.localized("policy.version.test.behavior", 1, category, AresConstants.TEST_BEHAVIOR_POLICY_VERSION));
	}

	/**
	 * A version 2 policy built in code accepts the category.
	 *
	 * @param category the category's policy key.
	 * @param behavior a configuration setting only that category.
	 */
	@ParameterizedTest
	@MethodSource("categories")
	void aVersionTwoPolicyAcceptsTheCategory(String category, TestBehaviorConfiguration behavior) {
		assertThat(policy(2, behavior).thisPolicyFileCompliesToThePolicyVersion()).isEqualTo(2);
	}

	/** A version 1 policy may carry an empty test-behaviour section. */
	@Test
	void aVersionOnePolicyAcceptsAnEmptySection() {
		assertThat(policy(1, TestBehaviorConfiguration.builder().build()).thisPolicyFileCompliesToThePolicyVersion())
				.isEqualTo(1);
	}

	/** A policy built in code without a version declares the newest one. */
	@Test
	void aPolicyBuiltInCodeDeclaresTheNewestVersion() {
		assertThat(
				SecurityPolicy.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ)
						.thisPolicyFileCompliesToThePolicyVersion()).isEqualTo(AresConstants.MAXIMUM_POLICY_VERSION);
	}

	/**
	 * Builds a policy with the given version and test behaviour.
	 *
	 * @param version  the declared version.
	 * @param behavior the test behaviour.
	 * @return the policy.
	 */
	private static SecurityPolicy policy(int version, TestBehaviorConfiguration behavior) {
		return new SecurityPolicy(version,
				SupervisedCode.builder()
						.theFollowingProgrammingLanguageConfigurationIsUsed(
								ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ)
						.theFollowingClassesAreTestClasses(List.of())
						.theFollowingResourceAccessesArePermitted(ResourceAccesses.createRestrictive())
						.theFollowingTestBehaviorIsConfigured(behavior).build());
	}
}
