package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FeedbackLevelPolicyUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Proves the feedback level through the real pipeline in all four mode
 * combinations: the architecture check or the runtime check blocks the read,
 * and the report shows only what the level allows.
 */
@UserBased(FeedbackLevelPolicyUser.class)
class FeedbackLevelPolicyTest {

	/** Results of the supervised fixture class. */
	@UserTestResults
	private static Events tests;

	/** The MINIMIZED text, in English. */
	private static final String MINIMIZED = "Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.";

	/**
	 * FULL keeps the detailed message, in every mode: the action is named, which
	 * the MINIMIZED text never does.
	 */
	@TestTest
	void test_full() {
		for (String test : new String[] { "fullArchunitAspectj", "fullArchunitInstrumentation", "fullWalaAspectj",
				"fullWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1,
					testFailedWith(test, SecurityException.class, "tried to illegally", Option.MESSAGE_CONTAINS));
		}
	}

	/** DEBUG names the network category for a blocked connection. */
	@TestTest
	void test_debugNetwork() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugNetwork", SecurityException.class,
				"To allow this, add to regardingNetworkConnections", Option.MESSAGE_CONTAINS));
	}

	/** DEBUG names the command category for a blocked command. */
	@TestTest
	void test_debugCommand() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugCommand", SecurityException.class,
				"To allow this, add to regardingCommandExecutions", Option.MESSAGE_CONTAINS));
	}

	/** DEBUG names the thread category for a blocked thread start. */
	@TestTest
	void test_debugThread() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugThread", SecurityException.class,
				"To allow this, add to regardingThreadCreations", Option.MESSAGE_CONTAINS));
	}

	/** MINIMIZED shows only that something was blocked, in every mode. */
	@TestTest
	void test_minimized() {
		for (String test : new String[] { "minimizedArchunitAspectj", "minimizedArchunitInstrumentation",
				"minimizedWalaAspectj", "minimizedWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1, testFailedWith(test, SecurityException.class, MINIMIZED));
		}
	}

	/** DEBUG names the category that would allow the read, in every mode. */
	@TestTest
	void test_debug() {
		for (String test : new String[] { "debugArchunitAspectj", "debugArchunitInstrumentation", "debugWalaAspectj",
				"debugWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1, testFailedWith(test, SecurityException.class,
					"To allow this, add to regardingFileSystemInteractions", Option.MESSAGE_CONTAINS));
		}
	}

	/**
	 * With privileged-only reporting on, a blocked read still follows the level.
	 */
	@TestTest
	void test_privilegedOnlyKeepsTheLevel() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("privilegedOnlyKeepsTheLevel", SecurityException.class, MINIMIZED));
	}

	/**
	 * A runtime denial inside the test, with privileged-only reporting on, keeps
	 * the short text instead of the policy's message, in both backends.
	 */
	@TestTest
	void test_runtimeDenialKeepsTheLevelWhenPrivilegedOnly() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("runtimeDenialKeepsTheLevelWhenPrivilegedOnlyAspectj",
				SecurityException.class, MINIMIZED));
		tests.assertThatEvents().haveExactly(1, testFailedWith(
				"runtimeDenialKeepsTheLevelWhenPrivilegedOnlyInstrumentation", SecurityException.class, MINIMIZED));
	}

	/** A student's own Ares-looking exception gets the policy's message. */
	@TestTest
	void test_privilegedOnlyHidesAForgedViolation() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("privilegedOnlyHidesAForgedViolation", AssertionError.class, "Policy message"));
	}
}
