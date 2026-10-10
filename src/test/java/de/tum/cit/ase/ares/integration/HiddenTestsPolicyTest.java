package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.integration.testuser.HiddenTestsPolicyUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks that the policy's hidden-test schedule holds back and releases hidden
 * tests, that annotations still win, also over the policy's visibility, and
 * that an unmatched entry or a missing visibility switch fails.
 */
@UserBased(HiddenTestsPolicyUser.class)
class HiddenTestsPolicyTest {

	/** The message of a hidden test held back before its deadline. */
	private static final String BEFORE_DEADLINE = "hidden tests will be executed after the deadline.";

	/** The results of running {@link HiddenTestsPolicyUser}. */
	@UserTestResults
	private static Events tests;

	/** A passed policy deadline releases a hidden test. */
	@TestTest
	void policyPastDeadlineRuns() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("policyPastDeadlineRuns"));
	}

	/** A future policy deadline holds a hidden test back. */
	@TestTest
	void policyFutureDeadlineBlocks() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("policyFutureDeadlineBlocks", AssertionFailedError.class, BEFORE_DEADLINE));
	}

	/** An annotation's extension adds to the policy's deadline. */
	@TestTest
	void annotationExtensionAddsToThePolicy() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("annotationExtensionAddsToThePolicy", AssertionFailedError.class, BEFORE_DEADLINE));
	}

	/** A method deadline wins over the policy's. */
	@TestTest
	void methodDeadlineWinsOverThePolicy() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("methodDeadlineWinsOverThePolicy"));
	}

	/** The policy's always-run-before date releases a hidden test. */
	@TestTest
	void policyAlwaysRunBeforeRuns() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("policyAlwaysRunBeforeRuns"));
	}

	/** A public test ignores the policy's deadline. */
	@TestTest
	void publicTestIgnoresThePolicyDeadline() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("publicTestIgnoresThePolicyDeadline"));
	}

	/** A public annotation wins over the policy's list. */
	@TestTest
	void annotationWinsOverTheList() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("annotationWinsOverTheList"));
	}

	/** An unmatched list entry fails a public test, naming the entry. */
	@TestTest
	void unmatchedEntryFailsAPublicTest() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("unmatchedEntryFailsAPublicTest",
				IllegalArgumentException.class, "HiddenTestsPolicyUser#noSuchTest", Option.MESSAGE_CONTAINS));
	}

	/** A public annotation wins over a policy that hides unlisted tests. */
	@TestTest
	void annotationWinsWhenUnlistedTestsAreHidden() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("annotationWinsWhenUnlistedTestsAreHidden"));
	}

	/** An unmatched public entry fails a public test, naming the list and entry. */
	@TestTest
	void unmatchedPublicEntryFailsAPublicTest() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("unmatchedPublicEntryFailsAPublicTest",
				IllegalArgumentException.class,
				"theFollowingTestsArePublic entry \"de.tum.cit.ase.ares.integration.testuser.HiddenTestsPolicyUser#noSuchPublicTest\"",
				Option.MESSAGE_CONTAINS));
	}

	/** A policy without the visibility switch fails, naming the policy file. */
	@TestTest
	void aPolicyWithoutTheUnlistedSwitchFailsATest() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("aPolicyWithoutTheUnlistedSwitchFailsATest",
				SecurityException.class, "PolicyHiddenTestsWithoutSwitchUser.yaml", Option.MESSAGE_CONTAINS));
	}
}
