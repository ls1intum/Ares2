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

	@Test
	void theRestrictiveTimeoutIsThreeSeconds() {
		// Pinned literally on purpose: the assertion above compares two factories
		// against each other and would stay green no matter what they both became, so
		// on its own it cannot notice the value silently drifting.
		assertThat(ResourceLimitsPermission.createRestrictive().timeout()).isEqualTo(3000L);
	}

	@Test
	void anOmittedTimeoutFallsBackToTheRestrictiveOne() {
		// The builder default is what decides whether a programmatically assembled
		// policy is bounded at all, so it must not quietly become an empty list.
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
