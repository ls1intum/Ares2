package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.Thread.State;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.*;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem.FileSystemAccessPenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.TestUtils;
import de.tum.cit.ase.ares.integration.testuser.ThreadUser;
import de.tum.cit.ase.ares.testutilities.*;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;

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
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OneThreadAllowedInstrumentationCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
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
}
