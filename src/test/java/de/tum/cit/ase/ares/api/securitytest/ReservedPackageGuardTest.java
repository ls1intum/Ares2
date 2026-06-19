package de.tum.cit.ase.ares.api.securitytest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Verifies the reserved-package guard rejects exactly the trusted
 * infrastructure namespaces and leaves legitimate supervised packages
 * (including the reproducibility test subjects under
 * {@code de.tum.cit.ase.ares.integration.*} and the pinned
 * {@code de.tum.cit.ase.ares}) untouched.
 */
class ReservedPackageGuardTest {

	@Test
	void rejectsTrustedInfrastructurePrefixes() {
		assertThat(ReservedPackageGuard.reservedPrefixOf("de.tum.cit.ase.ares.api.evil"))
				.isEqualTo("de.tum.cit.ase.ares.api.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.aspectj.weaver")).isEqualTo("org.aspectj.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("net.bytebuddy.dynamic")).isEqualTo("net.bytebuddy.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.ibm.wala.ipa")).isEqualTo("com.ibm.wala.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.tngtech.archunit.core"))
				.isEqualTo("com.tngtech.archunit.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("anonymous.toolclasses.Helper"))
				.isEqualTo("anonymous.toolclasses.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("metatest.Foo")).isEqualTo("metatest.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("jdk.internal.misc")).isEqualTo("jdk.");
	}

	@Test
	void allowsLegitimateAndHarnessPackages() {
		// The harness subjects and the pinned package must NOT be reserved.
		assertThat(ReservedPackageGuard.reservedPrefixOf("de.tum.cit.ase.ares.integration.testuser.subject")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("de.tum.cit.ase.ares")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.example.student")).isNull();
		// "anonymous." alone is the analysed-student namespace; only the toolclasses
		// sub-namespace is reserved.
		assertThat(ReservedPackageGuard.reservedPrefixOf("anonymous.classloadingsystem")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf(null)).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("")).isNull();
	}

	@Test
	void validatePackageThrowsOnlyForReserved() {
		assertThatThrownBy(() -> ReservedPackageGuard.validatePackage("de.tum.cit.ase.ares.api.evil"))
				.isInstanceOf(SecurityException.class);
		assertThatCode(() -> ReservedPackageGuard.validatePackage("de.tum.cit.ase.ares.integration.testuser.subject"))
				.doesNotThrowAnyException();
	}

	@Test
	void validateClassNamesThrowsOnReservedDeclaringPackage() {
		assertThatThrownBy(
				() -> ReservedPackageGuard.validateClassNames(List.of("de.tum.cit.ase.ares.api.evil.Smuggled")))
						.isInstanceOf(SecurityException.class);
		assertThatCode(() -> ReservedPackageGuard
				.validateClassNames(List.of("de.tum.cit.ase.ares.integration.testuser.subject.helloWorld.Penguin")))
						.doesNotThrowAnyException();
	}
}
