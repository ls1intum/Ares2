package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.integration.testuser.StrictTimeoutPolicyUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks that the policy's strict timeout bounds supervised tests, that an
 * annotation still wins, and that the timeout's own worker needs no thread
 * permission.
 */
@UserBased(StrictTimeoutPolicyUser.class)
class StrictTimeoutPolicyTest {

	/** The results of running {@link StrictTimeoutPolicyUser}. */
	@UserTestResults
	private static Events tests;

	/** The policy alone stops an endless loop. */
	@TestTest
	void policyStopsAnEndlessLoop() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("policyStopsAnEndlessLoop", AssertionFailedError.class,
				"execution timed out after 200 ms"));
	}

	/** A test inside the policy's limit passes. */
	@TestTest
	void policyLetsAFastTestPass() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("policyLetsAFastTestPass"));
	}

	/** A longer annotation wins over the policy. */
	@TestTest
	void annotationLetsASlowTestPass() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("annotationLetsASlowTestPass"));
	}
}
