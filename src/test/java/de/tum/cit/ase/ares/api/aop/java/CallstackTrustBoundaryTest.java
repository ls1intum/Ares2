package de.tum.cit.ase.ares.api.aop.java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.foo.AllowedPackageMember;
import com.foobar.UnrelatedPackageMember;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ExemptedClassNests;

import p.TrustedTest;
import p.TrustedTestEvil;

/**
 * Regression tests for I-105/TD-051: both AOP backends' call-stack trust check
 * (allowed classes/packages) used a bare {@code String.startsWith} comparison
 * with no boundary check, so an allowed class/package also wrongly permitted an
 * unrelated sibling that merely shared its string prefix (e.g.
 * {@code p.TrustedTest} allowed also permitted {@code p.TrustedTestEvil}).
 * Exercises the real, fixed
 * {@code checkIfCallstackCriteriaIsViolated}/{@code inspectCallstackOnce} via
 * real stack frames (not synthetic input), across both the AspectJ and
 * instrumentation backends.
 */
class CallstackTrustBoundaryTest {

	private static void resetSettings() throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
	}

	@Test
	void exactAllowedClassMatchIsPermittedInBothBackends() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = { "p.TrustedTest" };
			assertNull(TrustedTest.checkAspectJ(allowedClasses));
			assertNull(TrustedTest.checkInstrumentation(allowedClasses));
		} finally {
			resetSettings();
		}
	}

	/** Expands the allow-listed {@code p.TrustedTest} from its own nest listing. */
	private static String[] expandedTrustedTest() {
		return ExemptedClassNests
				.expandThroughLoader(List.of("p.TrustedTest"), CallstackTrustBoundaryTest.class.getClassLoader())
				.toArray(String[]::new);
	}

	/**
	 * Classes declared inside an allow-listed class share its exemption once
	 * expanded.
	 */
	@Test
	void innerClassOfAllowedClassIsPermittedInBothBackends() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = expandedTrustedTest();
			assertNull(TrustedTest.Helper.checkAspectJ(allowedClasses));
			assertNull(TrustedTest.Helper.checkInstrumentation(allowedClasses));
			assertNull(TrustedTest.checkAspectJThroughAnonymousClass(allowedClasses));
			assertNull(TrustedTest.checkInstrumentationThroughLocalClass(allowedClasses));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Without the nest listing, a class's name alone no longer grants the
	 * exemption.
	 */
	@Test
	void innerClassIsNotPermittedByItsNameAlone() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = { "p.TrustedTest" };
			assertNotNull(TrustedTest.Helper.checkAspectJ(allowedClasses));
			assertNotNull(TrustedTest.Helper.checkInstrumentation(allowedClasses));
		} finally {
			resetSettings();
		}
	}

	/**
	 * A separate top-level class named {@code p.TrustedTest$Evil} is not in the
	 * allow-listed class's nest, so it is denied in both backends.
	 */
	@Test
	void topLevelClassNamedLikeANestedOneIsDeniedInBothBackends() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = expandedTrustedTest();
			assertTrue(List.of(allowedClasses).contains("p.TrustedTest$Helper"), List.of(allowedClasses).toString());
			assertFalse(List.of(allowedClasses).contains("p.TrustedTest$Evil"), List.of(allowedClasses).toString());
			assertNotNull(p.TrustedTest$Evil.checkAspectJ(allowedClasses));
			assertNotNull(p.TrustedTest$Evil.checkInstrumentation(allowedClasses));
		} finally {
			resetSettings();
		}
	}

	@Test
	void classSharingOnlyAStringPrefixIsDeniedInBothBackends() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = { "p.TrustedTest" };
			assertNotNull(TrustedTestEvil.checkAspectJ(allowedClasses));
			assertNotNull(TrustedTestEvil.checkInstrumentation(allowedClasses));
		} finally {
			resetSettings();
		}
	}

	@Test
	void memberOfAllowedPackageIsPermittedInBothBackends() throws Exception {
		try {
			resetSettings();
			JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedPackages", new String[] { "com.foo" }, "ARCH",
					"ASPECTJ");
			assertNull(AllowedPackageMember.checkAspectJ());
			assertNull(AllowedPackageMember.checkInstrumentation());
		} finally {
			resetSettings();
		}
	}

	@Test
	void packageSharingOnlyAStringPrefixIsDeniedInBothBackends() throws Exception {
		try {
			resetSettings();
			JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedPackages", new String[] { "com.foo" }, "ARCH",
					"ASPECTJ");
			assertNotNull(UnrelatedPackageMember.checkAspectJ());
			assertNotNull(UnrelatedPackageMember.checkInstrumentation());
		} finally {
			resetSettings();
		}
	}
}
