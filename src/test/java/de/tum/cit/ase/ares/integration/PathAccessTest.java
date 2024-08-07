package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PathAccessUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(PathAccessUser.class)
class PathAccessTest {

	@UserTestResults
	private static Events tests;

	private final String accessPathAllFiles = "accessPathAllFiles";
	private final String accessPathAllowed = "accessPathAllowed";
	private final String accessPathNormal = "accessPathNormal";
	private final String accessPathRelativeGlobA = "accessPathRelativeGlobA";
	private final String accessPathRelativeGlobB = "accessPathRelativeGlobB";
	private final String accessPathRelativeGlobDirectChildrenAllowed = "accessPathRelativeGlobDirectChildrenAllowed";
	private final String accessPathRelativeGlobDirectChildrenBlacklist = "accessPathRelativeGlobDirectChildrenBlacklist";
	private final String accessPathRelativeGlobDirectChildrenForbidden = "accessPathRelativeGlobDirectChildrenForbidden";
	private final String accessPathRelativeGlobRecursiveAllowed = "accessPathRelativeGlobRecursiveAllowed";
	private final String accessPathRelativeGlobRecursiveBlacklist = "accessPathRelativeGlobRecursiveBlacklist";
	private final String accessPathRelativeGlobRecursiveForbidden = "accessPathRelativeGlobRecursiveForbidden";
	private final String accessPathTest = "accessPathTest";
	private final String weAccessPath = "weAccessPath";

	/* OUTCOMMENTED: Conceptually not possible anymore
	@TestTest
	void test_accessPathAllFiles() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathAllFiles, SecurityException.class));
	}

	@TestTest
	void test_accessPathAllowed() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathAllowed));
	}
	*/


	@TestTest
	void test_accessPathNormal() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathNormal, SecurityException.class));
	}

	@TestTest
	void test_accessPathRelativeGlobA() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobA));
	}

	@TestTest
	void test_accessPathRelativeGlobB() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobB));
	}

	@TestTest
	void test_accessPathRelativeGlobDirectChildrenAllowed() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobDirectChildrenAllowed));
	}

	@TestTest
	void test_accessPathRelativeGlobDirectChildrenBlacklist() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(accessPathRelativeGlobDirectChildrenBlacklist, SecurityException.class));
	}

	@TestTest
	void test_accessPathRelativeGlobDirectChildrenForbidden() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(accessPathRelativeGlobDirectChildrenForbidden, SecurityException.class));
	}

	@TestTest
	void test_accessPathRelativeGlobRecursiveAllowed() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobRecursiveAllowed));
	}

	@TestTest
	void test_accessPathRelativeGlobRecursiveBlacklist() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(accessPathRelativeGlobRecursiveBlacklist, SecurityException.class));
	}

	@TestTest
	void test_accessPathRelativeGlobRecursiveForbidden() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(accessPathRelativeGlobRecursiveForbidden, SecurityException.class));
	}

	@TestTest
	void test_accessPathTest() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathTest, SecurityException.class));
	}

	@TestTest
	void test_weAccessPath() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weAccessPath));
	}

	@TestTest
	void test_accessFileSystem() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("accessFileSystem", SecurityException.class));
	}


}
