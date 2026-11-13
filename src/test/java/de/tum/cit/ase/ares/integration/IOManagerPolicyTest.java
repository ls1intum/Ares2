package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.IOManagerPolicyUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks the console manager a policy chooses, in each of the four combinations
 * of static analysis and runtime checks: the IO tester records and limits
 * output, no manager records nothing, and a custom manager loaded from where
 * the student code is, is refused.
 */
@UserBased(IOManagerPolicyUser.class)
class IOManagerPolicyTest {

	/** The results of running {@link IOManagerPolicyUser}. */
	@UserTestResults
	private static Events tests;

	/** The four mode combinations the user runs every test in. */
	private static final int MODE_COMBINATIONS = 4;

	/** The IO tester records output for an assertion. */
	@TestTest
	void ioTesterRecordsOutput() {
		tests.assertThatEvents().haveExactly(MODE_COMBINATIONS, finishedSuccessfully("ioTesterRecordsOutput"));
	}

	/** The IO tester stops output beyond the policy's limit. */
	@TestTest
	void ioTesterEnforcesTheLimit() {
		tests.assertThatEvents().haveExactly(MODE_COMBINATIONS, testFailedWith("ioTesterEnforcesTheLimit",
				SecurityException.class, "too much standard output", Option.MESSAGE_CONTAINS));
	}

	/** Without a manager, output beyond any limit passes. */
	@TestTest
	void noneLetsOutputThrough() {
		tests.assertThatEvents().haveExactly(MODE_COMBINATIONS, finishedSuccessfully("noneLetsOutputThrough"));
	}

	/** Without a manager, no IO tester can be handed to a test. */
	@TestTest
	void noneProvidesNoTester() {
		tests.assertThatEvents().haveExactly(MODE_COMBINATIONS,
				testFailedWith("noneProvidesNoTester", ParameterResolutionException.class));
	}

	/** A custom manager from the student output is refused. */
	@TestTest
	void customFromTheStudentOutputIsRefused() {
		tests.assertThatEvents().haveExactly(MODE_COMBINATIONS, testFailedWith("customFromTheStudentOutputIsRefused",
				SecurityException.class, "is loaded from where the student code is", Option.MESSAGE_CONTAINS));
	}
}
