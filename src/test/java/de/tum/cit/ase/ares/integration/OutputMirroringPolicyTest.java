package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.OutputMirroringPolicyUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks that the policy's output limit bounds supervised tests, that output
 * within it is still recorded, and that an annotation replaces the policy.
 */
@UserBased(OutputMirroringPolicyUser.class)
class OutputMirroringPolicyTest {

	/** The results of running {@link OutputMirroringPolicyUser}. */
	@UserTestResults
	private static Events tests;

	/** The policy alone stops output beyond its limit. */
	@TestTest
	void policyLimitStopsTooMuchOutput() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("policyLimitStopsTooMuchOutput", SecurityException.class,
				"too much standard output", Option.MESSAGE_CONTAINS));
	}

	/** Output within the limit passes and is recorded. */
	@TestTest
	void policyLetsShortOutputPass() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("policyLetsShortOutputPass"));
	}

	/** An annotation replaces the whole policy category, limit included. */
	@TestTest
	void annotationReplacesThePolicy() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("annotationReplacesThePolicy"));
	}
}
