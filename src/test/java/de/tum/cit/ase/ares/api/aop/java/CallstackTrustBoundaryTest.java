package de.tum.cit.ase.ares.api.aop.java;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.foo.AllowedPackageMember;
import com.foobar.UnrelatedPackageMember;

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

	@Test
	void innerClassOfAllowedClassIsPermittedInBothBackends() throws Exception {
		try {
			resetSettings();
			String[] allowedClasses = { "p.TrustedTest" };
			assertNull(TrustedTest.Helper.checkAspectJ(allowedClasses));
			assertNull(TrustedTest.Helper.checkInstrumentation(allowedClasses));
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
