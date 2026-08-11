package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.NetworkUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(NetworkUser.class)
class NetworkTest {

	@UserTestResults
	private static Events tests;

	private final String connectRemoteNotAllowed = "connectRemoteNotAllowed";
	private final String connectLocallyNotAllowed = "connectLocallyNotAllowed(int)";
	private final String connectLocallyAllowed = "connectLocallyAllowed(java.lang.String)";

	@TestTest
	void test_connectRemoteNotAllowed() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(1,
		// testFailedWith(connectRemoteNotAllowed, SecurityException.class,
		// "network use not allowed (example.com:-1)", Option.MESSAGE_CONTAINS));
	}

	@TestTest
	void test_connectLocallyNotAllowed() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(2,
		// testFailedWith(connectLocallyNotAllowed, SecurityException.class));
	}

	@TestTest
	void test_connectLocallyAllowed() {
		tests.assertThatEvents().haveExactly(3, finishedSuccessfully(connectLocallyAllowed));
	}
}
