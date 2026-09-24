package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PrivilegedExceptionDefaultUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Runs {@link PrivilegedExceptionDefaultUser}, whose policy alone enables
 * privileged-exceptions-only reporting, through the real test pipeline.
 */
@UserBased(PrivilegedExceptionDefaultUser.class)
class PrivilegedExceptionDefaultTest {

	/** The recorded results of running {@link PrivilegedExceptionDefaultUser}. */
	@UserTestResults
	private static Events tests;

	/** Name of the fixture test failing with an ordinary assertion. */
	private final String nonprivilegedFailure = "nonprivilegedFailure";
	/** Name of the fixture test failing with a privileged assertion. */
	private final String privilegedAssertion = "privilegedAssertion";
	/** Name of the fixture test throwing a privileged exception. */
	private final String privilegedException = "privilegedException";
	/** Name of the fixture test exceeding its timeout. */
	private final String policyDefaultTimeout = "policyDefaultTimeout";

	/** An ordinary failure shows the policy's message instead of the real one. */
	@TestTest
	void test_nonprivilegedFailure() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(nonprivilegedFailure, AssertionError.class, "Policy default message"));
	}

	/** A privileged assertion still shows its real message. */
	@TestTest
	void test_privilegedAssertion() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(privilegedAssertion, AssertionError.class, "xyz"));
	}

	/** A privileged exception still shows its real message. */
	@TestTest
	void test_privilegedException() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(privilegedException, NullPointerException.class, "xyz"));
	}

	/** A timeout is still reported as a timeout. */
	@TestTest
	void test_policyDefaultTimeout() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(policyDefaultTimeout, AssertionError.class, "execution timed out after 300 ms"));
	}
}
