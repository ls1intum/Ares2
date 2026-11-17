package de.tum.cit.ase.ares.integration.architecture.forbidden;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.dumpStack.DumpStackMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.holdsLock.HoldsLockMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.interrupt.InterruptCurrentThreadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.read.ReadCurrentThreadMain;

/**
 * Checks the static thread rule around {@code Thread.currentThread()} in all
 * four mode combinations, under a policy that grants no thread creation.
 * Reading the current thread passes; interrupting a thread and the
 * {@code Thread} members once covered only by a bare {@code java.lang.Thread}
 * entry are still refused, naming the forbidden call.
 */
class CurrentThreadStaticTest extends SystemAccessTest {

	/** Folder of the student code that only reads the current thread. */
	private static final String READ_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/currentThread/read";

	/** Folder of the student code restoring the interrupt flag. */
	private static final String INTERRUPT_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/currentThread/interrupt";

	/** Folder of the student code calling {@code Thread.holdsLock}. */
	private static final String HOLDS_LOCK_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/currentThread/holdsLock";

	/** Folder of the student code calling {@code Thread.dumpStack}. */
	private static final String DUMP_STACK_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/currentThread/dumpStack";

	/** The forbidden call a refusal of the interrupt idiom names. */
	private static final String INTERRUPT_CALL = "interrupt()";

	/** The forbidden call a refusal of {@code holdsLock} names. */
	private static final String HOLDS_LOCK_CALL = "java.lang.Thread.holdsLock";

	/** The forbidden call a refusal of {@code dumpStack} names. */
	private static final String DUMP_STACK_CALL = "java.lang.Thread.dumpStack()";

	/** How an English refusal by the thread rule names the action. */
	private static final String THREAD_RULE_EN = "manipulate threads";

	/** How a German refusal by the thread rule names the action. */
	private static final String THREAD_RULE_DE = "Threads zu manipulieren";

	// <editor-fold desc="readCurrentThread">
	/** Reading the current thread passes in MavenArchunitAspectJ. */
	@Test
	void test_readCurrentThreadMavenArchunitAspectJ_test() {
		executeTestAndExpectSuccess("test_readCurrentThreadMavenArchunitAspectJ");
	}

	/** Reading the current thread passes in MavenArchunitInstrumentation. */
	@Test
	void test_readCurrentThreadMavenArchunitInstrumentation_test() {
		executeTestAndExpectSuccess("test_readCurrentThreadMavenArchunitInstrumentation");
	}

	/** Reading the current thread passes in MavenWalaAspectJ. */
	@Test
	void test_readCurrentThreadMavenWalaAspectJ_test() {
		executeTestAndExpectSuccess("test_readCurrentThreadMavenWalaAspectJ");
	}

	/** Reading the current thread passes in MavenWalaInstrumentation. */
	@Test
	void test_readCurrentThreadMavenWalaInstrumentation_test() {
		executeTestAndExpectSuccess("test_readCurrentThreadMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="interruptCurrentThread">
	/** Interrupting a thread is still refused in MavenArchunitAspectJ. */
	@Test
	void test_interruptCurrentThreadMavenArchunitAspectJ_test() {
		executeTestAndExpectThreadDenial("test_interruptCurrentThreadMavenArchunitAspectJ", INTERRUPT_CALL);
	}

	/** Interrupting a thread is still refused in MavenArchunitInstrumentation. */
	@Test
	void test_interruptCurrentThreadMavenArchunitInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_interruptCurrentThreadMavenArchunitInstrumentation", INTERRUPT_CALL);
	}

	/** Interrupting a thread is still refused in MavenWalaAspectJ. */
	@Test
	void test_interruptCurrentThreadMavenWalaAspectJ_test() {
		executeTestAndExpectThreadDenial("test_interruptCurrentThreadMavenWalaAspectJ", null);
	}

	/** Interrupting a thread is still refused in MavenWalaInstrumentation. */
	@Test
	void test_interruptCurrentThreadMavenWalaInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_interruptCurrentThreadMavenWalaInstrumentation", null);
	}
	// </editor-fold>

	// <editor-fold desc="holdsLock">
	/** {@code Thread.holdsLock} is still refused in MavenArchunitAspectJ. */
	@Test
	void test_holdsLockMavenArchunitAspectJ_test() {
		executeTestAndExpectThreadDenial("test_holdsLockMavenArchunitAspectJ", HOLDS_LOCK_CALL);
	}

	/**
	 * {@code Thread.holdsLock} is still refused in MavenArchunitInstrumentation.
	 */
	@Test
	void test_holdsLockMavenArchunitInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_holdsLockMavenArchunitInstrumentation", HOLDS_LOCK_CALL);
	}

	/** {@code Thread.holdsLock} is still refused in MavenWalaAspectJ. */
	@Test
	void test_holdsLockMavenWalaAspectJ_test() {
		executeTestAndExpectThreadDenial("test_holdsLockMavenWalaAspectJ", null);
	}

	/** {@code Thread.holdsLock} is still refused in MavenWalaInstrumentation. */
	@Test
	void test_holdsLockMavenWalaInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_holdsLockMavenWalaInstrumentation", null);
	}
	// </editor-fold>

	// <editor-fold desc="dumpStack">
	/** {@code Thread.dumpStack} is still refused in MavenArchunitAspectJ. */
	@Test
	void test_dumpStackMavenArchunitAspectJ_test() {
		executeTestAndExpectThreadDenial("test_dumpStackMavenArchunitAspectJ", DUMP_STACK_CALL);
	}

	/**
	 * {@code Thread.dumpStack} is still refused in MavenArchunitInstrumentation.
	 */
	@Test
	void test_dumpStackMavenArchunitInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_dumpStackMavenArchunitInstrumentation", DUMP_STACK_CALL);
	}

	/** {@code Thread.dumpStack} is still refused in MavenWalaAspectJ. */
	@Test
	void test_dumpStackMavenWalaAspectJ_test() {
		executeTestAndExpectThreadDenial("test_dumpStackMavenWalaAspectJ", null);
	}

	/** {@code Thread.dumpStack} is still refused in MavenWalaInstrumentation. */
	@Test
	void test_dumpStackMavenWalaInstrumentation_test() {
		executeTestAndExpectThreadDenial("test_dumpStackMavenWalaInstrumentation", null);
	}
	// </editor-fold>

	/**
	 * Runs a disabled probe and expects it to pass.
	 *
	 * @param methodName the probe in this class
	 */
	private static void executeTestAndExpectSuccess(String methodName) {
		Events testEvents = EngineTestKit.engine("junit-jupiter")
				.configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
				.selectors(selectMethod(CurrentThreadStaticTest.class, methodName)).execute().testEvents();
		testEvents.assertStatistics(stats -> stats.failed(0).aborted(0).succeeded(1));
	}

	/**
	 * Runs a disabled probe and expects the static analysis to refuse it with a
	 * security exception from the thread rule. WALA names the student method rather
	 * than the forbidden call, so only ArchUnit probes check the call.
	 *
	 * @param methodName    the probe in this class
	 * @param forbiddenCall text of the forbidden call the message has to name, or
	 *                      {@code null} for a WALA probe
	 */
	private static void executeTestAndExpectThreadDenial(String methodName, String forbiddenCall) {
		Events testEvents = EngineTestKit.engine("junit-jupiter")
				.configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
				.selectors(selectMethod(CurrentThreadStaticTest.class, methodName)).execute().testEvents();
		testEvents.assertStatistics(stats -> stats.failed(1).aborted(0).succeeded(0));
		testEvents.assertThatEvents().haveExactly(1,
				event(test(methodName),
						finishedWithFailure(instanceOf(SecurityException.class),
								message(text -> mentionsThreadManipulation(text)
										&& (forbiddenCall == null || text.contains(forbiddenCall))))));
	}

	/**
	 * Tells whether a message comes from the thread rule, in English or German.
	 *
	 * @param text the message
	 * @return {@code true} if it reports manipulating threads
	 */
	private static boolean mentionsThreadManipulation(String text) {
		return text.contains(THREAD_RULE_EN) || text.contains(THREAD_RULE_DE);
	}

	// ===================== DISABLED TESTS BELOW =====================

	/** Reads the current thread in MavenArchunitAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = READ_PATH)
	void test_readCurrentThreadMavenArchunitAspectJ() {
		Assertions.assertTrue(ReadCurrentThreadMain.readsTheSameThreadTwice());
	}

	/** Reads the current thread in MavenArchunitInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = READ_PATH)
	void test_readCurrentThreadMavenArchunitInstrumentation() {
		Assertions.assertTrue(ReadCurrentThreadMain.readsTheSameThreadTwice());
	}

	/** Reads the current thread in MavenWalaAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = READ_PATH)
	void test_readCurrentThreadMavenWalaAspectJ() {
		Assertions.assertTrue(ReadCurrentThreadMain.readsTheSameThreadTwice());
	}

	/** Reads the current thread in MavenWalaInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = READ_PATH)
	void test_readCurrentThreadMavenWalaInstrumentation() {
		Assertions.assertTrue(ReadCurrentThreadMain.readsTheSameThreadTwice());
	}

	/** Restores the interrupt flag in MavenArchunitAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = INTERRUPT_PATH)
	void test_interruptCurrentThreadMavenArchunitAspectJ() {
		InterruptCurrentThreadMain.awaitResult(CompletableFuture.completedFuture("done"));
	}

	/** Restores the interrupt flag in MavenArchunitInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = INTERRUPT_PATH)
	void test_interruptCurrentThreadMavenArchunitInstrumentation() {
		InterruptCurrentThreadMain.awaitResult(CompletableFuture.completedFuture("done"));
	}

	/** Restores the interrupt flag in MavenWalaAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = INTERRUPT_PATH)
	void test_interruptCurrentThreadMavenWalaAspectJ() {
		InterruptCurrentThreadMain.awaitResult(CompletableFuture.completedFuture("done"));
	}

	/** Restores the interrupt flag in MavenWalaInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = INTERRUPT_PATH)
	void test_interruptCurrentThreadMavenWalaInstrumentation() {
		InterruptCurrentThreadMain.awaitResult(CompletableFuture.completedFuture("done"));
	}

	/** Calls {@code Thread.holdsLock} in MavenArchunitAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = HOLDS_LOCK_PATH)
	void test_holdsLockMavenArchunitAspectJ() {
		HoldsLockMain.holdsLock(new Object());
	}

	/** Calls {@code Thread.holdsLock} in MavenArchunitInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = HOLDS_LOCK_PATH)
	void test_holdsLockMavenArchunitInstrumentation() {
		HoldsLockMain.holdsLock(new Object());
	}

	/** Calls {@code Thread.holdsLock} in MavenWalaAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = HOLDS_LOCK_PATH)
	void test_holdsLockMavenWalaAspectJ() {
		HoldsLockMain.holdsLock(new Object());
	}

	/** Calls {@code Thread.holdsLock} in MavenWalaInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = HOLDS_LOCK_PATH)
	void test_holdsLockMavenWalaInstrumentation() {
		HoldsLockMain.holdsLock(new Object());
	}

	/** Calls {@code Thread.dumpStack} in MavenArchunitAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DUMP_STACK_PATH)
	void test_dumpStackMavenArchunitAspectJ() {
		DumpStackMain.dumpStack();
	}

	/** Calls {@code Thread.dumpStack} in MavenArchunitInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DUMP_STACK_PATH)
	void test_dumpStackMavenArchunitInstrumentation() {
		DumpStackMain.dumpStack();
	}

	/** Calls {@code Thread.dumpStack} in MavenWalaAspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = DUMP_STACK_PATH)
	void test_dumpStackMavenWalaAspectJ() {
		DumpStackMain.dumpStack();
	}

	/** Calls {@code Thread.dumpStack} in MavenWalaInstrumentation. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DUMP_STACK_PATH)
	void test_dumpStackMavenWalaInstrumentation() {
		DumpStackMain.dumpStack();
	}
}
