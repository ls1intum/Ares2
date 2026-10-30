package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Verifies the restrictive {@link ResourceAccesses} fallback is genuinely
 * restrictive: every permission list is empty except the timeout, which must
 * enforce the same anti-DoS limit as {@link ResourceLimitsPermission}, and all
 * lists are unmodifiable.
 */
class ResourceAccessesTest {

	@Test
	void restrictiveEnforcesTheSameTimeoutAsResourceLimitsPermission() {
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertThat(restrictive.regardingTimeouts()).hasSize(1);
		assertThat(restrictive.regardingTimeouts().get(0).timeout())
				.isEqualTo(ResourceLimitsPermission.createRestrictive().timeout());
	}

	/**
	 * Checks that the restrictive timeout is exactly 3000 milliseconds. The value
	 * is pinned literally on purpose: the test above only compares two factories
	 * with each other, so it would stay green if both drifted together.
	 */
	@Test
	void theRestrictiveTimeoutIsThreeSeconds() {
		assertThat(ResourceLimitsPermission.createRestrictive().timeout()).isEqualTo(3000L);
	}

	/**
	 * Checks that a builder given no timeout falls back to the restrictive one.
	 * This default decides whether a policy assembled in code records a limit at
	 * all, so it must not quietly become an empty list.
	 */
	@Test
	void anOmittedTimeoutFallsBackToTheRestrictiveOne() {
		ResourceAccesses withoutTimeouts = ResourceAccesses.builder().build();
		assertThat(withoutTimeouts.regardingTimeouts()).containsExactly(ResourceLimitsPermission.createRestrictive());
	}

	@Test
	void restrictiveDeniesEveryOtherResource() {
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertThat(restrictive.regardingFileSystemInteractions()).isEmpty();
		assertThat(restrictive.regardingNetworkConnections()).isEmpty();
		assertThat(restrictive.regardingCommandExecutions()).isEmpty();
		assertThat(restrictive.regardingThreadCreations()).isEmpty();
		assertThat(restrictive.regardingPackageImports()).isEmpty();
	}

	@Test
	void restrictiveListsAreUnmodifiable() {
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertThatThrownBy(() -> restrictive.regardingTimeouts().add(ResourceLimitsPermission.createRestrictive()))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
