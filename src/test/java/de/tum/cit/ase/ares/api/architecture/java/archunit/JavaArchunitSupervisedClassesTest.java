package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Whether an import permission reaches into a trusted namespace below it.
 * <p>
 * A permission covers the packages below it, so {@code de.tum.cit.ase} would
 * cover Ares' own API. {@code isReservedBelow} is the question the import rule
 * asks to keep such a permission out of every trusted namespace it does not
 * name, including the API Ares copies into a supervised project.
 */
class JavaArchunitSupervisedClassesTest {

	/**
	 * A package above a trusted namespace does not reach into it.
	 */
	@Test
	@DisplayName("Keeps a permission above a trusted namespace out of it")
	void keepsAPermissionAboveATrustedNamespaceOutOfIt() {
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase", "de.tum.cit.ase.ares.api.policy"))
				.isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de", "de.tum.cit.ase.ares.api")).isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("com", "com.sun.net.httpserver")).isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org", "org.aspectj.lang")).isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("net", "net.bytebuddy")).isTrue();
	}

	/**
	 * The rest of what lies below such a package stays covered.
	 */
	@Test
	@DisplayName("Leaves the ordinary packages below an ancestor covered")
	void leavesTheOrdinaryPackagesBelowAnAncestorCovered() {
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase", "de.tum.cit.ase")).isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase", "de.tum.cit.ase.exercise"))
				.isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase", "de.tum.cit.ase.ares.integration"))
				.isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org", "org.apache.xyz")).isFalse();
	}

	/**
	 * A permission naming the trusted namespace, or something inside it, reaches
	 * it.
	 */
	@Test
	@DisplayName("Lets a permission that names the trusted namespace reach it")
	void letsAPermissionThatNamesTheTrustedNamespaceReachIt() {
		assertThat(
				JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase.ares.api", "de.tum.cit.ase.ares.api.io"))
						.isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase.ares.api.policy",
				"de.tum.cit.ase.ares.api.policy.policySubComponents")).isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("java", "java.util")).isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org.aspectj", "org.aspectj.lang")).isFalse();
	}

	/**
	 * Prefixes are compared on segment boundaries rather than on text.
	 */
	@Test
	@DisplayName("Compares on segment boundaries rather than on text")
	void comparesOnSegmentBoundariesRatherThanOnText() {
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit", "de.tum.citadel.ares.api")).isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("ja", "javax.crypto")).isFalse();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("com", "com.sunny")).isFalse();
	}

	/**
	 * The API copied into a supervised project is a trusted namespace too, and an
	 * explicit permission for it reaches it.
	 */
	@Test
	@DisplayName("Treats the copied API as a trusted namespace")
	void treatsTheCopiedApiAsATrustedNamespace() {
		String copiedApiPrefix = "org.example.ares.api.";
		assertThat(
				JavaArchunitSupervisedClasses.isReservedBelow("org.example", "org.example.ares.api.x", copiedApiPrefix))
						.isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("de.tum.cit.ase", "de.tum.cit.ase.ares.api.policy",
				copiedApiPrefix)).isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org.example.ares.api", "org.example.ares.api.x",
				copiedApiPrefix)).isFalse();
		assertThat(
				JavaArchunitSupervisedClasses.isReservedBelow("org.example", "org.example.exercise", copiedApiPrefix))
						.isFalse();
	}

	/**
	 * A trusted namespace nested inside another stays closed to a permission that
	 * names only the outer one.
	 */
	@Test
	@DisplayName("Keeps a nested trusted namespace closed to the outer one")
	void keepsANestedTrustedNamespaceClosedToTheOuterOne() {
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org.aspectj", "org.aspectj.exercise.ares.api.x",
				"org.aspectj.exercise.ares.api.")).isTrue();
		assertThat(JavaArchunitSupervisedClasses.isReservedBelow("org.aspectj", "org.aspectj.lang",
				"org.aspectj.exercise.ares.api.")).isFalse();
	}
}
