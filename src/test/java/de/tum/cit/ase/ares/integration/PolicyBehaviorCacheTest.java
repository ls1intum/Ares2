package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PolicyBehaviorCacheUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks that a policy letting the tested code overwrite its own file is
 * refused before any test code runs: every test using it fails with the refusal
 * naming the entry, a hidden test reveals nothing more, the file stays as it
 * was, and a method with its own policy still follows that policy.
 */
@UserBased(PolicyBehaviorCacheUser.class)
class PolicyBehaviorCacheTest {

	/** The results of running {@link PolicyBehaviorCacheUser}. */
	@UserTestResults
	private static Events tests;

	/** The three ways the class is supervised. */
	private static final int VARIANTS = 3;

	/** The part of the refusal that names the offending entry. */
	private static final String REFUSAL = "lets the tested code overwrite or create files below \"target/test-behavior-cache\"";

	/** A test that would overwrite the policy fails with the refusal instead. */
	@TestTest
	void aTestThatWouldOverwriteThePolicyFailsWithTheRefusal() {
		tests.assertThatEvents().haveExactly(VARIANTS,
				testFailedWith("a_overwritesThePolicy", SecurityException.class, REFUSAL, Option.MESSAGE_CONTAINS));
	}

	/** A hidden test fails with the same refusal and reveals nothing else. */
	@TestTest
	void aHiddenTestFailsWithTheSameRefusal() {
		tests.assertThatEvents().haveExactly(VARIANTS, testFailedWith("b_hiddenTestKeepsTheDeadline",
				SecurityException.class, REFUSAL, Option.MESSAGE_CONTAINS));
	}

	/** A public test fails with the refusal too. */
	@TestTest
	void aPublicTestFailsWithTheRefusal() {
		tests.assertThatEvents().haveExactly(VARIANTS,
				testFailedWith("c_publicTestStillRuns", SecurityException.class, REFUSAL, Option.MESSAGE_CONTAINS));
	}

	/** A method with its own policy, which grants no such write, still runs. */
	@TestTest
	void methodPolicyHasItsOwnDeadline() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("d_methodPolicyHasItsOwnDeadline"));
	}

	/**
	 * No test code under the refused policy ran: every copy still holds the
	 * deadline it started with.
	 *
	 * @throws IOException if a copy cannot be read
	 */
	@TestTest
	void noCopyWasRewritten() throws IOException {
		for (String copy : PolicyBehaviorCacheUser.COPIES) {
			assertThat(Files.readString(Path.of(copy), StandardCharsets.UTF_8)).contains("2200-01-01 00:00 UTC");
		}
	}
}
