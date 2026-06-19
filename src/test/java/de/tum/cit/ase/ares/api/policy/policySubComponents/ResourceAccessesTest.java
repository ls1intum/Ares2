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
