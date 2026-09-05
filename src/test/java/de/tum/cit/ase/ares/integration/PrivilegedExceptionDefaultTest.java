package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PrivilegedExceptionDefaultUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

@UserBased(PrivilegedExceptionDefaultUser.class)
class PrivilegedExceptionDefaultTest {

	@UserTestResults
	private static Events tests;

	private final String nonprivilegedFailure = "nonprivilegedFailure";
	private final String privilegedAssertion = "privilegedAssertion";
	private final String privilegedException = "privilegedException";
	private final String policyDefaultTimeout = "policyDefaultTimeout";

	@TestTest
	void test_nonprivilegedFailure() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(nonprivilegedFailure, AssertionError.class, "Policy default message"));
	}

	@TestTest
	void test_privilegedAssertion() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(privilegedAssertion, AssertionError.class, "xyz"));
	}

	@TestTest
	void test_privilegedException() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(privilegedException, NullPointerException.class, "xyz"));
	}

	@TestTest
	void test_policyDefaultTimeout() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(policyDefaultTimeout, AssertionError.class, "execution timed out after 300 ms"));
	}
}
