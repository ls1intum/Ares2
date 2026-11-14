package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.integration.testuser.PolicyBehaviorCacheUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks that overwriting a policy file while its class runs changes nothing:
 * Ares keeps what it read before the first test, for a public class, for public
 * methods and for one shared instance, and a method's own policy still gives
 * that method its own schedule.
 */
@UserBased(PolicyBehaviorCacheUser.class)
class PolicyBehaviorCacheTest {

	/** The results of running {@link PolicyBehaviorCacheUser}. */
	@UserTestResults
	private static Events tests;

	/** The three ways the class is supervised. */
	private static final int VARIANTS = 3;

	/** What a hidden test reports before its deadline. */
	private static final String BEFORE_DEADLINE = "hidden tests will be executed after the deadline.";

	/** The overwrite itself succeeds in every variant. */
	@TestTest
	void overwritingThePolicySucceeds() {
		tests.assertThatEvents().haveExactly(VARIANTS, finishedSuccessfully("a_overwritesThePolicy"));
	}

	/** A hidden test is still held back by the deadline read before. */
	@TestTest
	void hiddenTestKeepsTheDeadline() {
		tests.assertThatEvents().haveExactly(VARIANTS, testFailedWith("b_hiddenTestKeepsTheDeadline",
				AssertionFailedError.class, BEFORE_DEADLINE, Option.MESSAGE_CONTAINS));
	}

	/** A public test still runs after the overwrite. */
	@TestTest
	void publicTestStillRuns() {
		tests.assertThatEvents().haveExactly(VARIANTS, finishedSuccessfully("c_publicTestStillRuns"));
	}

	/** A method with its own policy follows that policy's passed deadline. */
	@TestTest
	void methodPolicyHasItsOwnDeadline() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("d_methodPolicyHasItsOwnDeadline"));
	}
}
