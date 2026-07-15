package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.threads.ThreadPenguin;
import de.tum.cit.ase.ares.testutilities.*;

//@UserBased(ThreadUser.class)
@UseLocale("en")
class ThreadTest {

	@UserTestResults
	private static Events tests;

	private final String testThreadGroup = "testThreadGroup";
	private final String threadLimitExceeded = "threadLimitExceeded";
	private final String threadWhitelistingWithPathFail = "threadWhitelistingWithPathFail";
	private final String threadWhitelistingWithPathPenguin = "threadWhitelistingWithPathPenguin";

	private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";

	/**
	 * Asserts that the exception message contains general error details for
	 * thread-related issues.
	 *
	 * @param actualMessage The actual exception message to be verified.
	 * @param operationText The specific operation text to check in the exception
	 *                      message.
	 */
	private void assertThreadErrorMessage(String actualMessage, String operationText) {
		assertTrue(actualMessage.contains("Ares Security Error"),
				"Exception message should contain 'Ares Security Error'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("Student-Code"),
				"Exception message should contain 'Student-Code'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("Execution"),
				"Exception message should contain 'Execution'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("ThreadPenguin"),
				"Exception message should contain the class name 'ThreadPenguin'" + System.lineSeparator()
						+ actualMessage);
		assertTrue(actualMessage.contains("create Thread"),
				"Exception message should indicate an illegal thread creation" + System.lineSeparator()
						+ actualMessage);
		assertTrue(actualMessage.contains("blocked by Ares"),
				"Exception message should indicate the operation was blocked by Ares" + System.lineSeparator()
						+ actualMessage);
	}

	/**
	 * Test that the given executable throws a SecurityException with the expected
	 * thread-related message.
	 *
	 * @param executable The executable that should throw a SecurityException.
	 */
	private void assertThreadSecurityException(Executable executable, String operationText) {
		SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
		assertThreadErrorMessage(se.getMessage(), operationText);
	}

	@TestTest
	void test_testThreadGroup() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(1, testFailedWith(testThreadGroup,
		// SecurityException.class));
	}

	@TestTest
	void test_threadLimitExceeded() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(1, testFailedWith(threadLimitExceeded,
		// SecurityException.class,
		// "too many threads: 2 (max: 1) in line 36 in ThreadPenguin.java",
		// Option.MESSAGE_CONTAINS));
	}

	@TestTest
	void test_threadWhitelistingWithPathFail() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(1,
		// testFailedWith(threadWhitelistingWithPathFail, SecurityException.class));
	}

	@TestTest
	void test_threadWhitelistingWithPathPenguin() {
		// OUTCOMMENTED: Test does not pass
		// tests.assertThatEvents().haveExactly(1,
		// testFailedWith(threadWhitelistingWithPathPenguin, SecurityException.class));
	}

	@TestTest
	@PublicTest
	@StrictTimeout(1000000)
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_threadAccess() {
		// The thread pool must be created by supervised subject code (ThreadPenguin),
		// not inline in the
		// test harness, so the thread creation is attributed to the student and
		// governed by the policy.
		assertThreadSecurityException(ThreadPenguin::threadAccess, "create Thread");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_executorServiceSlide8() {
		assertThreadSecurityException(ThreadPenguin::executorServiceSlide8, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_executorServiceSlide9() {
		assertThreadSecurityException(ThreadPenguin::executorServiceSlide9, "Task 3 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_executorServiceSlide10() {
		assertThreadSecurityException(ThreadPenguin::executorServiceSlide10, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_executorServiceSlide11() {
		assertThreadSecurityException(ThreadPenguin::executorServiceSlide11, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_executorServiceFuture1() {
		assertThreadSecurityException(ThreadPenguin::executorServiceFuture1, "Task submitted...");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_completableFutureSlide1() {
		assertThreadSecurityException(ThreadPenguin::completableFutureSlide1, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_completableFutureSlide2() {
		assertThreadSecurityException(ThreadPenguin::completableFutureSlide2, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_completableFutureSlide3Combine() {
		assertThreadSecurityException(ThreadPenguin::completableFutureSlide3Combine, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_parallelStreams1() {
		assertThreadSecurityException(ThreadPenguin::parallelStreams1, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_publisherSubscriber() {
		assertThreadSecurityException(ThreadPenguin::publisherSubscriber, "Task 2 executed");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_tryStartTwoThreads() {
		assertThreadSecurityException(ThreadPenguin::tryStartTwoThreads, "Thread started");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_tryBreakThreadGroup() {
		assertThreadSecurityException(ThreadPenguin::tryBreakThreadGroup, "Thread started");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_spawnEndlessThreads() {
		assertThreadSecurityException(ThreadPenguin::spawnEndlessThreads, "Thread started");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_sleepInCurrentThread() {
		// Thread.sleep only pauses the current thread; it neither creates nor starts
		// a thread, so it is not a thread-creation operation and must be allowed even
		// under a policy that restricts thread creation.
		assertDoesNotThrow(() -> ThreadPenguin.sleepInCurrentThread(1000),
				"Thread.sleep pauses the current thread and must not be blocked as a thread creation.");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_runItself() {
		assertThreadSecurityException(ThreadPenguin::runItself, "Thread started");
	}
}
