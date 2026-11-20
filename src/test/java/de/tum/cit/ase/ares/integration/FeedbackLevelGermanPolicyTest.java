package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FeedbackLevelGermanPolicyUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Proves that the DEBUG hint of an architecture finding names the right
 * category when the messages are German.
 */
@UserBased(FeedbackLevelGermanPolicyUser.class)
class FeedbackLevelGermanPolicyTest {

	/** Results of the supervised fixture class. */
	@UserTestResults
	private static Events tests;

	/** The German hint names the file-system category. */
	@TestTest
	void test_debugArchitectureInGerman() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("debugArchitectureInGerman", SecurityException.class,
				"regardingFileSystemInteractions", Option.MESSAGE_CONTAINS));
	}
}
