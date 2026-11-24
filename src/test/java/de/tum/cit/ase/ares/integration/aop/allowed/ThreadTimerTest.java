package de.tum.cit.ase.ares.integration.aop.allowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.timer.TimerMain;

/**
 * Checks that both runtime modes count a new {@code java.util.Timer} as one
 * thread of the class {@code java.util.TimerThread}, and do not count the tasks
 * scheduled on it.
 */
class ThreadTimerTest {

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The policy allowing one timer thread, the same name in every mode folder.
	 */
	private static final String POLICY = "/PolicyTimerThreads.yaml";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/timer";

	/**
	 * Asserts that Ares refuses the call, directly or as the cause of the failure.
	 *
	 * @param executable the call
	 */
	private static void assertRefused(Executable executable) {
		Throwable thrown = Assertions.assertThrows(Throwable.class, executable);
		for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
			if (cause instanceof SecurityException) {
				return;
			}
		}
		Assertions.fail("expected a SecurityException, got " + thrown);
	}

	/**
	 * One timer, which runs three tasks, uses the one allowed timer thread in
	 * ArchunitAspectJ; the tasks start no thread of their own.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void oneTimerRunsItsTasksMavenArchunitAspectJ() throws InterruptedException {
		Assertions.assertEquals(3, TimerMain.scheduleOnTimers(1, 3));
	}

	/**
	 * A second timer exceeds the one allowed timer thread in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void secondTimerIsRefusedMavenArchunitAspectJ() {
		assertRefused(() -> TimerMain.scheduleOnTimers(2, 1));
	}

	/**
	 * One timer, which runs three tasks, uses the one allowed timer thread in
	 * ArchunitInstrumentation; the tasks start no thread of their own.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void oneTimerRunsItsTasksMavenArchunitInstrumentation() throws InterruptedException {
		Assertions.assertEquals(3, TimerMain.scheduleOnTimers(1, 3));
	}

	/**
	 * A second timer exceeds the one allowed timer thread in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void secondTimerIsRefusedMavenArchunitInstrumentation() {
		assertRefused(() -> TimerMain.scheduleOnTimers(2, 1));
	}

	/**
	 * One timer, which runs three tasks, uses the one allowed timer thread in
	 * WalaAspectJ; the tasks start no thread of their own.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void oneTimerRunsItsTasksMavenWalaAspectJ() throws InterruptedException {
		Assertions.assertEquals(3, TimerMain.scheduleOnTimers(1, 3));
	}

	/**
	 * A second timer exceeds the one allowed timer thread in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void secondTimerIsRefusedMavenWalaAspectJ() {
		assertRefused(() -> TimerMain.scheduleOnTimers(2, 1));
	}

	/**
	 * One timer, which runs three tasks, uses the one allowed timer thread in
	 * WalaInstrumentation; the tasks start no thread of their own.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void oneTimerRunsItsTasksMavenWalaInstrumentation() throws InterruptedException {
		Assertions.assertEquals(3, TimerMain.scheduleOnTimers(1, 3));
	}

	/**
	 * A second timer exceeds the one allowed timer thread in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void secondTimerIsRefusedMavenWalaInstrumentation() {
		assertRefused(() -> TimerMain.scheduleOnTimers(2, 1));
	}
}
