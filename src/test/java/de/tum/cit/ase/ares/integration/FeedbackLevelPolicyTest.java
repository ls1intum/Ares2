package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import java.util.List;

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

	/** The LOW text, in English. */
	private static final String LOW = "Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.";

	/**
	 * HIGH keeps the detailed message, in every mode: the action is named, which
	 * the LOW text never does.
	 */
	@TestTest
	void test_full() {
		for (String test : new String[] { "highArchunitAspectj", "highArchunitInstrumentation", "highWalaAspectj",
				"highWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1,
					testFailedWith(test, SecurityException.class, "tried to illegally", Option.MESSAGE_CONTAINS));
		}
	}

	/** DEBUG names the network category for a blocked connection. */
	@TestTest
	void test_debugNetwork() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugNetwork", SecurityException.class,
				"Allow it by adding an entry under regardingNetworkConnections in the security policy PolicyFeedbackLevelDEBUG.yaml.",
				Option.MESSAGE_CONTAINS));
	}

	/** DEBUG names the command category for a blocked command. */
	@TestTest
	void test_debugCommand() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugCommand", SecurityException.class,
				"Allow it by adding an entry under regardingCommandExecutions in the security policy PolicyFeedbackLevelDEBUG.yaml.",
				Option.MESSAGE_CONTAINS));
	}

	/** DEBUG names the thread category for a blocked thread start. */
	@TestTest
	void test_debugThread() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugThread", SecurityException.class,
				"Allow it by adding an entry under regardingThreadCreations in the security policy PolicyFeedbackLevelDEBUG.yaml.",
				Option.MESSAGE_CONTAINS));
	}

	/** LOW shows only that something was blocked, in every mode. */
	@TestTest
	void test_minimized() {
		for (String test : new String[] { "lowArchunitAspectj", "lowArchunitInstrumentation", "lowWalaAspectj",
				"lowWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1, testFailedWith(test, SecurityException.class, LOW));
		}
	}

	/**
	 * MEDIUM shows LOW's sentence for a read the architecture check finds, in every
	 * mode, since the action of a finding is not known.
	 */
	@TestTest
	void test_mediumArchitecture() {
		for (String test : new String[] { "mediumArchunitAspectj", "mediumArchunitInstrumentation", "mediumWalaAspectj",
				"mediumWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1, testFailedWith(test, SecurityException.class, LOW));
		}
	}

	/**
	 * DEBUG shows the exact package entry and the policy file for a forbidden
	 * import, in both backends.
	 */
	@TestTest
	void test_debugImport() {
		for (String test : new String[] { "debugImportAspectj", "debugImportInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1,
					testFailedWith(test, SecurityException.class,
							"Allow it by granting the import permission on java.nio.charset in the security policy "
									+ "PolicyFeedbackLevelDebugImport.yaml as:",
							Option.MESSAGE_CONTAINS));
		}
	}

	/** DEBUG names the category that would allow the read, in every mode. */
	@TestTest
	void test_debug() {
		for (String test : new String[] { "debugArchunitAspectj", "debugArchunitInstrumentation", "debugWalaAspectj",
				"debugWalaInstrumentation" }) {
			tests.assertThatEvents().haveExactly(1,
					testFailedWith(test, SecurityException.class,
							"Allow it by adding an entry under regardingFileSystemInteractions in the security policy "
									+ "PolicyFeedbackLevelDEBUG.yaml.",
							Option.MESSAGE_CONTAINS));
		}
	}

	/**
	 * With privileged-only reporting on, a blocked read still follows the level.
	 */
	@TestTest
	void test_privilegedOnlyKeepsTheLevel() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("privilegedOnlyKeepsTheLevel", SecurityException.class, LOW));
	}

	/**
	 * A runtime denial inside the test, with privileged-only reporting on, keeps
	 * the short text instead of the policy's message, in both backends.
	 */
	@TestTest
	void test_runtimeDenialKeepsTheLevelWhenPrivilegedOnly() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("runtimeDenialKeepsTheLevelWhenPrivilegedOnlyAspectj", SecurityException.class, LOW));
		tests.assertThatEvents().haveExactly(1, testFailedWith(
				"runtimeDenialKeepsTheLevelWhenPrivilegedOnlyInstrumentation", SecurityException.class, LOW));
	}

	/**
	 * A student {@code File} subclass Ares refuses to inspect is a blocked action:
	 * LOW shows only the fixed text, with privileged-only reporting off and on, in
	 * both backends.
	 */
	@TestTest
	void test_untrustedArgumentFollowsTheLevel() {
		for (String name : List.of("untrustedArgumentIsShortenedAspectj", "untrustedArgumentIsShortenedInstrumentation",
				"untrustedArgumentKeepsTheLevelWhenPrivilegedOnlyAspectj",
				"untrustedArgumentKeepsTheLevelWhenPrivilegedOnlyInstrumentation")) {
			tests.assertThatEvents().haveExactly(1, testFailedWith(name, SecurityException.class, LOW));
		}
	}

	/** A student's own Ares-looking exception gets the policy's message. */
	@TestTest
	void test_privilegedOnlyHidesAForgedViolation() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("privilegedOnlyHidesAForgedViolation", AssertionError.class, "Policy message"));
	}
}
