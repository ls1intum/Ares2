package de.tum.cit.ase.ares.api.securitytest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;

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
		assertThat(ReservedPackageGuard.reservedPrefixOf("jdk.internal.misc")).isEqualTo("jdk.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("javax.activation")).isEqualTo("javax.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.sun.example")).isEqualTo("com.sun.");
	}

	/**
	 * Version 3 restores the Ares 1 breadth: the test harness and Ares' other
	 * non-shaded dependencies are reserved again, so a submission cannot shadow the
	 * classes that run or judge it.
	 */
	@Test
	void rejectsTheRestoredHarnessAndDependencyPrefixes() {
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.junit.jupiter.api.Assertions")).isEqualTo("org.junit.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("junit.framework.TestCase")).isEqualTo("junit.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.opentest4j.AssertionFailedError"))
				.isEqualTo("org.opentest4j.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.assertj.core.api.Assertions")).isEqualTo("org.assertj.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("com.fasterxml.jackson.databind.ObjectMapper"))
				.isEqualTo("com.fasterxml.jackson.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.yaml.snakeyaml.Yaml")).isEqualTo("org.yaml.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("org.apache.commons.io.FileUtils")).isEqualTo("org.apache.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("de.tum.in.test.api.Deadline"))
				.isEqualTo("de.tum.in.test.api.");
		assertThat(ReservedPackageGuard.reservedPrefixOf("worker.org.gradle.process.internal.worker.GradleWorkerMain"))
				.isEqualTo("worker.org.gradle.");
	}

	/**
	 * A derived scope broad enough to contain a newly reserved dependency now
	 * reports it, so JavaCreator refuses that derived permission.
	 */
	@Test
	void reportsARestoredNamespaceLyingBelowAPackage() {
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("com.github")).isEqualTo("com.github.javaparser.");
	}

	/**
	 * Every canonical reserved prefix is refused, not only the ones sampled in the
	 * tests above, so widening the list cannot leave an addition unenforced by the
	 * guard.
	 */
	@Test
	void reservesEveryCanonicalPrefix() {
		for (String prefix : WalaPathClassification.RESERVED_PACKAGE_PREFIXES) {
			assertThat(ReservedPackageGuard.reservedPrefixOf(prefix + "Example"))
					.as("prefix %s must be reserved", prefix).isEqualTo(prefix);
		}
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
		assertThat(ReservedPackageGuard.ancestorOfReservedPrefix("jdk")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("jdk")).isEqualTo("jdk.");
	}

	@Test
	void noLongerReservesTheReproducibilityPackagesOwnTestHelpers() {
		// anonymous.toolclasses. and metatest. were reserved for one downstream
		// consumer, so every Ares user refused two ordinary package names on its
		// behalf, with a diagnostic about trusted namespaces that meant nothing in
		// their project. They stay in WalaPathClassification.INFRA_PREFIXES, which
		// answers a different question: which frames are the harness rather than the
		// subject.
		assertThat(ReservedPackageGuard.reservedPrefixOf("anonymous.toolclasses.Helper")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("metatest.Foo")).isNull();
		assertThat(ReservedPackageGuard.reservedPrefixOf("metatest")).isNull();
	}
}
