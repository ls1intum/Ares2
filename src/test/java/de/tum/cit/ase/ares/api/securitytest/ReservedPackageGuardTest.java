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
		assertThat(ReservedPackageGuard.reservedPrefixOf("javax.activation")).isEqualTo("javax.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.sun.example")).isEqualTo("com.sun.");
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

	/**
	 * The complement of the guard above, and the one a package permission needs.
	 * Permissions are matched as prefixes, so permitting a package permits
	 * everything below it: a derived scope of de.tum.cit is refused by neither the
	 * reserved check nor the coverage check, yet it carries Ares' own namespace
	 * with it.
	 */
	@Test
	void reportsTheReservedNamespaceLyingBelowAPackage() {
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit")).isEqualTo("de.tum.cit.ase.ares.api.");
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit.ase.ares"))
				.isEqualTo("de.tum.cit.ase.ares.api.");
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de")).isEqualTo("de.tum.cit.ase.ares.api.");
	}

	@Test
	void leavesAPackageWithNothingReservedBelowItAlone() {
		// The assignment package and its neighbours contain no trusted namespace, so
		// permitting them grants only the project's own code.
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit.aet")).isNull();
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit.ase.ares.integration.testuser")).isNull();
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("assignment")).isNull();
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix(null)).isNull();
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("  ")).isNull();
	}

	@Test
	void comparesOnSegmentBoundariesRatherThanOnText() {
		// de.tum.cit.aetevil merely begins with the letters of de.tum.cit.aet, and
		// de.tum.citadel with those of de.tum.cit; neither contains a reserved
		// namespace, and a bare text comparison would say otherwise.
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.citadel")).isNull();
		// A package equal to a reserved prefix is inside it rather than above it, which
		// is the other guard's business and must not be reported here as well.
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit.ase.ares.api")).isNull();
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("de.tum.cit.ase.ares.api.")).isNull();
	}

	@Test
	void normalisesTheTrailingDotAtBothEnds() {
		// Reserved prefixes carry a trailing dot and package names do not, so both ends
		// are normalised before comparing. "com" is not reserved itself, but com.sun.
		// lies below it, so permitting it would permit that namespace too.
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("com")).isEqualTo("com.sun.");
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("com.")).isEqualTo("com.sun.");
	}

	@Test
	void doesNotReportAPackageThatIsItselfReserved() {
		// "java" normalises to the reserved prefix "java." exactly, so it is inside a
		// trusted namespace rather than above one. That is reservedPrefixOf's business,
		// and reporting it here as well would give one package two different reasons
		// for refusal and a diagnostic that names the wrong problem.
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("java")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("java")).isEqualTo("java.");
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("metatest")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("metatest")).isEqualTo("metatest.");
	}
}
