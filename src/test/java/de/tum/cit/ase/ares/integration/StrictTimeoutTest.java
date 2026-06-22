package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.*;

import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.integration.testuser.StrictTimeoutUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(StrictTimeoutUser.class)
class StrictTimeoutTest {

	@UserTestResults
	private static Events tests;

	private final String testClassFailLoop = "testClassFailLoop";
	private final String testClassFailNormal = "testClassFailNormal";
	private final String testClassSuccess = "testClassSuccess";
	private final String testMethodFailLoop = "testMethodFailLoop";
	private final String testMethodFailNormal = "testMethodFailNormal";
	private final String testMethodSuccess = "testMethodSuccess";
	private final String testOneSecondFail = "testOneSecondFail";
	private final String testOneSecondSuccess = "testOneSecondSuccess";

	@TestTest
	void test_testClassFailLoop() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testClassFailLoop, AssertionFailedError.class));
	}

	@TestTest
	void test_testClassFailNormal() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testClassFailNormal, AssertionFailedError.class));
	}

	@TestTest
	void test_testClassSuccess() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testClassSuccess));
	}

	@TestTest
	void test_testMethodFailLoop() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testMethodFailLoop, AssertionFailedError.class));
	}

	@TestTest
	void test_testMethodFailNormal() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testMethodFailNormal, AssertionFailedError.class));
	}

	@TestTest
	void test_testMethodSuccess() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testMethodSuccess));
	}

	@TestTest
	void test_testOneSecondFail() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testOneSecondFail, AssertionFailedError.class));
	}

	@TestTest
	void test_testOneSecondSuccess() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testOneSecondSuccess));
	}

	@TestTest
	void test_time_testClassFailLoop() {
		// This is the first timed test method to run (alphabetical MethodName order) and it
		// triggers a busy loop that saturates a CPU core. The first measured invocation therefore
		// carries the one-time cold-start cost of the timeout infrastructure (class loading and JIT
		// compilation of the executor, future and security machinery), while the busy loop delays
		// the scheduler in waking the thread that waits on the timeout. Both add a fixed overhead on
		// top of the 100 ms budget that is measurement noise outside the timeout mechanism itself,
		// so a wider tolerance is used here than for the warm, sleep based cases below.
		tests.assertThatEvents().filteredOn(event(test(testClassFailLoop))).satisfies(deltaTimeIs(100, 1.0));
	}

	@TestTest
	void test_time_testClassFailNormal() {
		tests.assertThatEvents().filteredOn(event(test(testClassFailNormal))).satisfies(deltaTimeIs(100, 0.5));
	}

	@TestTest
	void test_time_testMethodFailLoop() {
		tests.assertThatEvents().filteredOn(event(test(testMethodFailLoop))).satisfies(deltaTimeIs(300, 0.5));
	}

	@TestTest
	void test_time_testMethodFailNormal() {
		tests.assertThatEvents().filteredOn(event(test(testMethodFailNormal))).satisfies(deltaTimeIs(300, 0.5));
	}

	@TestTest
	void test_time_testOneSecondFail() {
		tests.assertThatEvents().filteredOn(event(test(testOneSecondFail))).satisfies(deltaTimeIs(1000, 0.3));
	}
}
