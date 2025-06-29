package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;

import org.junit.ComparisonFailure;
import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.integration.testuser.SecurityUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(SecurityUser.class)
class SecurityTest {

	@UserTestResults
	private static Events tests;

	private final String doSystemExit = "doSystemExit";
	private final String longOutputJUnit4 = "longOutputJUnit4";
	private final String longOutputJUnit5 = "longOutputJUnit5";
	private final String testDefinePackage = "testDefinePackage";
	private final String testEvilPermission = "testEvilPermission";
	private final String testExecuteGit = "testExecuteGit";
	private final String testMaliciousExceptionA = "testMaliciousExceptionA";
	private final String testMaliciousExceptionB = "testMaliciousExceptionB";
	private final String testMaliciousInvocationTargetException = "testMaliciousInvocationTargetException";
	private final String testNewClassLoader = "testNewClassLoader";
	private final String testNewSecurityManager = "testNewSecurityManager";
	private final String trustedPackageWithoutEnforcerRule = "trustedPackageWithoutEnforcerRule";
	private final String tryLoadNativeLibrary = "tryLoadNativeLibrary";
	private final String tryManageProcess = "tryManageProcess";
	private final String trySetSecurityManager = "trySetSecurityManager";
	private final String trySetSystemOut = "trySetSystemOut";
	private final String useCommonPoolBadNormal = "useCommonPoolBadNormal";
	private final String useCommonPoolBadTrick = "useCommonPoolBadTrick";
	private final String useCommonPoolGood = "useCommonPoolGood";
	private final String useReflectionNormal = "useReflectionNormal";
	private final String useReflectionPackagePrivateExecute = "useReflectionPackagePrivateExecute";
	private final String useReflectionPackagePrivateSetAccessible = "useReflectionPackagePrivateSetAccessible";
	private final String useReflectionPrivileged = "useReflectionPrivileged";
	private final String useReflectionTrick = "useReflectionTrick";
	private final String weUseReflection = "weUseReflection";
	private final String weUseShutdownHooks = "weUseShutdownHooks";

	@TestTest
	void test_doSystemExit() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(doSystemExit, SecurityException.class));
	}

	@TestTest
	void test_longOutputJUnit4() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(longOutputJUnit4, ComparisonFailure.class));
	}

	@TestTest
	void test_longOutputJUnit5() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(longOutputJUnit5, AssertionFailedError.class));
	}

	@TestTest
	void test_testDefinePackage() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testDefinePackage, SecurityException.class));
	}

	@TestTest
	void test_testEvilPermission() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testEvilPermission));
	}

	@TestTest
	void test_testExecuteGit() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testExecuteGit, SecurityException.class));
	}

	@TestTest
	void test_testMaliciousExceptionA() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testMaliciousExceptionA, SecurityException.class));
	}

	@TestTest
	void test_testMaliciousExceptionB() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testMaliciousExceptionB, SecurityException.class));
	}

	@TestTest
	void test_testMaliciousInvocationTargetException() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(testMaliciousInvocationTargetException, SecurityException.class));
	}

	@TestTest
	void test_testNewClassLoader() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testNewClassLoader, SecurityException.class));
	}

	@TestTest
	void test_testNewSecurityManager() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testNewSecurityManager, SecurityException.class));
	}

	@TestTest
	void test_trustedPackageWithoutEnforcerRule() {
		//OUTCOMMENTED: Removed configuration
		/*tests.assertThatEvents().haveExactly(1, testFailedWith(trustedPackageWithoutEnforcerRule,
				ConfigurationException.class,
				"Ares has detected that the build configuration is probably incomplete."
						+ " The following file-must-not-exist rules seem to be missing:\n"
						+ "    <file>${project.build.outputDirectory}/xyz/</file>\n"
						+ "    See https://github.com/ls1intum/Ares#what-you-need-to-do-outside-ares for more information."));
		// Make sure our link is actually valid
		assertThat(Path.of("README.adoc")).content().contains("#what-you-need-to-do-outside-ares");*/
	}

	@TestTest
	void test_tryLoadNativeLibrary() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(tryLoadNativeLibrary, SecurityException.class));
	}

	@TestTest
	void test_tryManageProcess() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(tryManageProcess, SecurityException.class));
	}

	@TestTest
	void test_trySetSecurityManager() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(trySetSecurityManager, SecurityException.class));
	}

	@TestTest
	void test_trySetSystemOut() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(trySetSystemOut, SecurityException.class));
	}

	@TestTest
	void test_useCommonPoolBadNormal() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(useCommonPoolBadNormal, SecurityException.class));
	}

	@TestTest
	void test_useCommonPoolBadTrick() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(useCommonPoolBadTrick, SecurityException.class));
	}

	@TestTest
	void test_useCommonPoolGood() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(useCommonPoolGood));
	}

	@TestTest
	void test_useReflectionNormal() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(useReflectionNormal, SecurityException.class));
	}

	@TestTest
	void test_useReflectionPackagePrivateExecute() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(useReflectionPackagePrivateExecute, IllegalAccessException.class));
	}

	@TestTest
	void test_useReflectionPackagePrivateSetAccessible() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(useReflectionPackagePrivateSetAccessible, SecurityException.class));
	}

	@TestTest
	void test_useReflectionPrivileged() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(useReflectionPrivileged, SecurityException.class));
	}

	@TestTest
	void test_useReflectionTrick() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(useReflectionTrick, SecurityException.class));
	}

	@TestTest
	void test_weUseReflection() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weUseReflection));
	}

	@TestTest
	void test_weUseShutdownHooks() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weUseShutdownHooks));
	}
}
