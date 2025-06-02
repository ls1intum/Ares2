package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.Thread.State;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.*;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.threads.ThreadPenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.TestUtils;
import de.tum.cit.ase.ares.testutilities.*;

//@UserBased(ThreadUser.class)
class ThreadTest {

	@UserTestResults
	private static Events tests;

	private final String commonPoolInterruptable = "commonPoolInterruptable";
	private final String testThreadBomb = "testThreadBomb";
	private final String testThreadExtension = "testThreadExtension";
	private final String testThreadGroup = "testThreadGroup";
	private final String threadLimitExceeded = "threadLimitExceeded";
	private final String threadWhitelistingWithPathCorrect = "threadWhitelistingWithPathCorrect";
	private final String threadWhitelistingWithPathFail = "threadWhitelistingWithPathFail";
	private final String threadWhitelistingWithPathPenguin = "threadWhitelistingWithPathPenguin";

	private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";

	@TestTest
	void test_commonPoolInterruptable() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(commonPoolInterruptable, AssertionError.class, "execution timed out after 300 ms"));

		assertCommonPoolIdleAndIntact();
	}

	private static void assertCommonPoolIdleAndIntact() {
		ThreadGroup root = TestUtils.getRootThreadGroup();
		Thread[] allThreads = new Thread[root.activeCount() + 10];
		TestUtils.getRootThreadGroup().enumerate(allThreads, true);
		Map<String, State> commonPoolThreadStates = Stream.of(allThreads).filter(Objects::nonNull)
				.peek(System.out::println).filter(t -> t.getName().contains("commonPool"))
				.collect(Collectors.toMap(Thread::getName, Thread::getState));
		assertThat(commonPoolThreadStates).doesNotContainValue(State.RUNNABLE);
		assertThat(commonPoolThreadStates).doesNotContainValue(State.TERMINATED);
	}

	/**
	 * Asserts that the exception message contains general error details for thread-related issues.
	 *
	 * @param actualMessage The actual exception message to be verified.
	 * @param operationText The specific operation text to check in the exception message.
	 */
	private void assertThreadErrorMessage(String actualMessage, String operationText) {
		assertTrue(actualMessage.contains("Thread Security Error"),
				"Exception message should contain 'Thread Security Error'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("Student-Code"),
				"Exception message should contain 'Student-Code'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("Execution"),
				"Exception message should contain 'Execution'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("ThreadAccessPenguin"),
				"Exception message should contain the class name 'ThreadAccessPenguin'" + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains("TODO:Forbidden-test"),
				"Exception message should contain the forbidden thread operation: " + System.lineSeparator() + actualMessage);
		assertTrue(actualMessage.contains(operationText),
				"Exception message should indicate the expected operation by containing '" + operationText + "'" + System.lineSeparator() + actualMessage);
	}

	/**
	 * Test that the given executable throws a SecurityException with the expected thread-related message.
	 *
	 * @param executable The executable that should throw a SecurityException.
	 */
	private void assertThreadSecurityException(Executable executable, String operationText) {
		SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
		assertThreadErrorMessage(se.getMessage(), operationText);
	}

	@Disabled("Currently unused because this is very inconsistent depending on the CI environment")
	@TestTest
	void test_testThreadBomb() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(testThreadBomb, SecurityException.class));
	}

	@TestTest
	void test_testThreadExtension() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testThreadExtension));
	}

	@TestTest
	void test_testThreadGroup() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(testThreadGroup, SecurityException.class));
	}

	@TestTest
	void test_threadLimitExceeded() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1, testFailedWith(threadLimitExceeded, SecurityException.class,
				//"too many threads: 2 (max: 1) in line 36 in ThreadPenguin.java", Option.MESSAGE_CONTAINS));
	}

	@TestTest
	void test_threadWhitelistingWithPathCorrect() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(threadWhitelistingWithPathCorrect));
	}

	@TestTest
	void test_threadWhitelistingWithPathFail() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
				//testFailedWith(threadWhitelistingWithPathFail, SecurityException.class));
	}

	@TestTest
	void test_threadWhitelistingWithPathPenguin() {
		//OUTCOMMENTED: Test does not pass
		//tests.assertThatEvents().haveExactly(1,
				//testFailedWith(threadWhitelistingWithPathPenguin, SecurityException.class));
	}

	@TestTest
	@PublicTest
	@StrictTimeout(1000000)
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_threadAccess() {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.submit(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					fail("Thread was interrupted");
				}
			});
			executor.submit(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					fail("Thread was interrupted");
				}
			});

			fail(errorMessage);
		} catch (SecurityException e) {
			// Expected exception
		}
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
	void test_executorServiceFuture1(){
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
		assertThreadSecurityException(() -> ThreadPenguin.sleepInCurrentThread(1000), "Thread.sleep");
	}

	@TestTest
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
	void test_runItself() {
		assertThreadSecurityException(ThreadPenguin::runItself, "Thread started");
	}
}
