package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The question a package permission has to ask, and the one it must not be
 * confused with.
 * <p>
 * {@code allowedPackages} grants the packages the validated production output
 * declares, and a permission is matched as a prefix, so a package lying
 * <em>above</em> a trusted namespace carries that namespace with it. Coverage
 * validation cannot catch that: it refuses a package <em>inside</em> a reserved
 * prefix, and a scope broad enough to contain one covers every compiled class
 * by construction, so it passes both checks and then grants the supervised code
 * imports from the framework supervising it.
 */
class JavaArchunitSupervisedClassesTest {

	@Test
	@DisplayName("Reports the trusted namespace lying below a package")
	void reportsTheReservedNamespaceLyingBelowAPackage() throws Exception {
		// The case that reaches allowedPackages: a derived scope of de.tum.cit is
		// refused by neither the reserved check nor the coverage check.
		assertThat(ancestorOfReservedPrefix("de.tum.cit")).isEqualTo("de.tum.cit.ase.ares.api.");
		assertThat(ancestorOfReservedPrefix("de.tum.cit.ase.ares")).isEqualTo("de.tum.cit.ase.ares.api.");
		assertThat(ancestorOfReservedPrefix("de")).isEqualTo("de.tum.cit.ase.ares.api.");
		// com is not reserved itself, but com.sun. lies below it.
		assertThat(ancestorOfReservedPrefix("com")).isEqualTo("com.sun.");
	}

	@Test
	@DisplayName("Normalises the trailing dot at both ends")
	void normalisesTheTrailingDotAtBothEnds() throws Exception {
		// Reserved prefixes carry a trailing dot and package names do not.
		assertThat(ancestorOfReservedPrefix("com.")).isEqualTo("com.sun.");
	}

	@Test
	@DisplayName("Leaves a package with nothing reserved below it alone")
	void leavesAnOrdinaryPackageAlone() throws Exception {
		assertThat(ancestorOfReservedPrefix("de.tum.cit.aet")).isNull();
		assertThat(ancestorOfReservedPrefix("assignment")).isNull();
		assertThat(ancestorOfReservedPrefix(null)).isNull();
		assertThat(ancestorOfReservedPrefix("  ")).isNull();
	}

	@Test
	@DisplayName("Compares on segment boundaries rather than on text")
	void comparesOnSegmentBoundariesRatherThanOnText() throws Exception {
		// de.tum.citadel merely begins with the letters of de.tum.cit and contains no
		// trusted namespace; a bare text comparison would say otherwise.
		assertThat(ancestorOfReservedPrefix("de.tum.citadel")).isNull();
	}

	@Test
	@DisplayName("Leaves a package that is itself reserved to the other guard")
	void doesNotReportAPackageThatIsItselfReserved() throws Exception {
		// Inside a trusted namespace rather than above one. Reporting it here as well
		// would give one package two reasons for refusal and a diagnostic naming the
		// wrong problem.
		assertThat(ancestorOfReservedPrefix("java")).isNull();
		assertThat(reservedPrefixOf("java")).isEqualTo("java.");
		assertThat(ancestorOfReservedPrefix("de.tum.cit.ase.ares.api")).isNull();
		assertThat(reservedPrefixOf("de.tum.cit.ase.ares.api")).isEqualTo("de.tum.cit.ase.ares.api.");
	}

	private static String ancestorOfReservedPrefix(String packageName) throws Exception {
		return invoke("ancestorOfReservedPrefix", packageName);
	}

	private static String reservedPrefixOf(String packageName) throws Exception {
		return invoke("reservedPrefixOf", packageName);
	}

	private static String invoke(String name, String packageName) throws Exception {
		Method method = JavaArchunitSupervisedClasses.class.getDeclaredMethod(name, String.class);
		method.setAccessible(true);
		return (String) method.invoke(null, packageName);
	}
}
