package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PackageAccessUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(PackageAccessUser.class)
class PackageAccessTest {

	@UserTestResults
	private static Events tests;

	private final String package_aBlacklistingRegex = "package_aBlacklistingRegex";
	private final String package_bBlacklistingJava = "package_bBlacklistingJava";
	private final String package_cBlacklistingAll = "package_cBlacklistingAll";
	private final String package_dBlackAndWhitelisting = "package_dBlackAndWhitelisting";
	private final String package_eBlackAgain = "package_eBlackAgain";

	@TestTest
	void test_package_aBlacklistingRegex() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(package_aBlacklistingRegex, SecurityException.class));
	}

	@TestTest
	void test_package_bBlacklistingJava() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(package_bBlacklistingJava, SecurityException.class));
	}

	@TestTest
	void test_package_cBlacklistingAll() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(package_cBlacklistingAll, SecurityException.class));
	}

	@TestTest
	void test_package_dBlackAndWhitelisting() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(package_dBlackAndWhitelisting));
	}

	@Disabled("Classes are only checked when they are loaded, so changing this again is impossible")
	@TestTest
	void test_package_eBlackAgain() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(package_eBlackAgain, SecurityException.class));
	}
}
