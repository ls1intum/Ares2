package de.tum.cit.ase.ares.integration.aop.allowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations.CountedOperationsMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations.StudentTaskComparator;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations.StudentTaskSet;

/**
 * Checks that both runtime modes count a task handed to a thread pool or to
 * {@code CompletableFuture} by the task's class, and treat
 * {@code Arrays.parallelSort} as the implicit parallel operation.
 */
class ThreadCountedOperationsTest {

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/countedOperations";

	/**
	 * The policy allowing three tasks and one parallel operation for
	 * ArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = POLICY_BASE
			+ "archunit/aspectj/PolicyCountedThreadOperations.yaml";

	/**
	 * The policy allowing three tasks and no parallel operation for
	 * ArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ_WITHOUT_PARALLEL = POLICY_BASE
			+ "archunit/aspectj/PolicyCountedThreadOperationsWithoutParallel.yaml";

	/**
	 * The policy allowing three tasks and one parallel operation for
	 * ArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = POLICY_BASE
			+ "archunit/instrumentation/PolicyCountedThreadOperations.yaml";

	/**
	 * The policy allowing three tasks and no parallel operation for
	 * ArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION_WITHOUT_PARALLEL = POLICY_BASE
			+ "archunit/instrumentation/PolicyCountedThreadOperationsWithoutParallel.yaml";

	/**
	 * The policy allowing three tasks and one parallel operation for WalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = POLICY_BASE + "wala/aspectj/PolicyCountedThreadOperations.yaml";

	/**
	 * The policy allowing three tasks and no parallel operation for WalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ_WITHOUT_PARALLEL = POLICY_BASE
			+ "wala/aspectj/PolicyCountedThreadOperationsWithoutParallel.yaml";

	/**
	 * The policy allowing three tasks and one parallel operation for
	 * WalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = POLICY_BASE
			+ "wala/instrumentation/PolicyCountedThreadOperations.yaml";

	/**
	 * The policy allowing three tasks and no parallel operation for
	 * WalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION_WITHOUT_PARALLEL = POLICY_BASE
			+ "wala/instrumentation/PolicyCountedThreadOperationsWithoutParallel.yaml";

	/**
	 * Asserts that Ares refuses the call, directly or as the cause of the failure,
	 * with a message naming the given text.
	 *
	 * @param executable the call
	 * @param named      a part of the refusal message
	 */
	private static void assertRefusedNaming(Executable executable, String named) {
		Throwable thrown = Assertions.assertThrows(Throwable.class, executable);
		for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
			if (cause instanceof SecurityException refusal) {
				Assertions.assertTrue(String.valueOf(refusal.getMessage()).contains(named), refusal::getMessage);
				return;
			}
		}
		Assertions.fail("expected a SecurityException, got " + thrown);
	}

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
	 * Three tasks submitted to a pool are allowed by an allowance for their class
	 * in ArchunitAspectJ, whatever threads the pool starts for them.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void poolRunsAllowedTasksMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.submitToPool(3));
	}

	/**
	 * A fourth task exceeds the allowance in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void poolRefusesFourthTaskMavenArchunitAspectJ() {
		assertRefused(() -> CountedOperationsMain.submitToPool(4));
	}

	/**
	 * A task run through {@code CompletableFuture.runAsync} is allowed by the
	 * allowance for its class in ArchunitAspectJ.
	 *
	 * @throws Exception if the task fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void runAsyncRunsAllowedTaskMavenArchunitAspectJ() throws Exception {
		CountedOperationsMain.runAsync();
	}

	/**
	 * {@code Arrays.parallelSort} is allowed by the parallel token in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortAllowedByTokenMavenArchunitAspectJ() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSort());
	}

	/**
	 * {@code Arrays.parallelSort} is refused without the parallel token in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortRefusedWithoutTokenMavenArchunitAspectJ() {
		assertRefused(CountedOperationsMain::parallelSort);
	}

	/**
	 * Three tasks submitted to a pool are allowed by an allowance for their class
	 * in ArchunitInstrumentation, whatever threads the pool starts for them.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void poolRunsAllowedTasksMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.submitToPool(3));
	}

	/**
	 * A fourth task exceeds the allowance in ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void poolRefusesFourthTaskMavenArchunitInstrumentation() {
		assertRefused(() -> CountedOperationsMain.submitToPool(4));
	}

	/**
	 * A task run through {@code CompletableFuture.runAsync} is allowed by the
	 * allowance for its class in ArchunitInstrumentation.
	 *
	 * @throws Exception if the task fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void runAsyncRunsAllowedTaskMavenArchunitInstrumentation() throws Exception {
		CountedOperationsMain.runAsync();
	}

	/**
	 * {@code Arrays.parallelSort} is allowed by the parallel token in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortAllowedByTokenMavenArchunitInstrumentation() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSort());
	}

	/**
	 * {@code Arrays.parallelSort} is refused without the parallel token in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortRefusedWithoutTokenMavenArchunitInstrumentation() {
		assertRefused(CountedOperationsMain::parallelSort);
	}

	/**
	 * Three tasks submitted to a pool are allowed by an allowance for their class
	 * in WalaAspectJ, whatever threads the pool starts for them.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void poolRunsAllowedTasksMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.submitToPool(3));
	}

	/**
	 * A fourth task exceeds the allowance in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void poolRefusesFourthTaskMavenWalaAspectJ() {
		assertRefused(() -> CountedOperationsMain.submitToPool(4));
	}

	/**
	 * A task run through {@code CompletableFuture.runAsync} is allowed by the
	 * allowance for its class in WalaAspectJ.
	 *
	 * @throws Exception if the task fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void runAsyncRunsAllowedTaskMavenWalaAspectJ() throws Exception {
		CountedOperationsMain.runAsync();
	}

	/**
	 * {@code Arrays.parallelSort} is allowed by the parallel token in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortAllowedByTokenMavenWalaAspectJ() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSort());
	}

	/**
	 * {@code Arrays.parallelSort} is refused without the parallel token in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortRefusedWithoutTokenMavenWalaAspectJ() {
		assertRefused(CountedOperationsMain::parallelSort);
	}

	/**
	 * Three tasks submitted to a pool are allowed by an allowance for their class
	 * in WalaInstrumentation, whatever threads the pool starts for them.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void poolRunsAllowedTasksMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.submitToPool(3));
	}

	/**
	 * A fourth task exceeds the allowance in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void poolRefusesFourthTaskMavenWalaInstrumentation() {
		assertRefused(() -> CountedOperationsMain.submitToPool(4));
	}

	/**
	 * A task run through {@code CompletableFuture.runAsync} is allowed by the
	 * allowance for its class in WalaInstrumentation.
	 *
	 * @throws Exception if the task fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void runAsyncRunsAllowedTaskMavenWalaInstrumentation() throws Exception {
		CountedOperationsMain.runAsync();
	}

	/**
	 * {@code Arrays.parallelSort} is allowed by the parallel token in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortAllowedByTokenMavenWalaInstrumentation() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSort());
	}

	/**
	 * {@code Arrays.parallelSort} is refused without the parallel token in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortRefusedWithoutTokenMavenWalaInstrumentation() {
		assertRefused(CountedOperationsMain::parallelSort);
	}

	/**
	 * A JDK set of tasks handed to {@code invokeAll} runs in ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAJdkSetMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAJdkSet());
	}

	/**
	 * A task set the student wrote is refused in ArchunitAspectJ before Ares
	 * iterates it, so the thread its iterator starts is never started unchecked.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAStudentSetUnreadMavenArchunitAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * Student lists sorted with {@code Arrays.parallelSort} are plain data, allowed
	 * by the parallel token in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsAllowedByTokenMavenArchunitAspectJ() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSortStudentLists());
	}

	/**
	 * Sorting student lists in parallel is refused without the parallel token in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsRefusedWithoutTokenMavenArchunitAspectJ() {
		assertRefused(CountedOperationsMain::parallelSortStudentLists);
	}

	/**
	 * An array that holds itself is sorted in parallel under the parallel token in
	 * ArchunitAspectJ, without Ares looking into it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortOfACyclicArrayAllowedByTokenMavenArchunitAspectJ() {
		Assertions.assertEquals(2, CountedOperationsMain.parallelSortCyclicArray());
	}

	/**
	 * A JDK set of tasks handed to {@code invokeAll} runs in
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAJdkSetMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAJdkSet());
	}

	/**
	 * A task set the student wrote is refused in ArchunitInstrumentation before
	 * Ares iterates it, so the thread its iterator starts is never started
	 * unchecked.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAStudentSetUnreadMavenArchunitInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * Student lists sorted with {@code Arrays.parallelSort} are plain data, allowed
	 * by the parallel token in ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsAllowedByTokenMavenArchunitInstrumentation() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSortStudentLists());
	}

	/**
	 * Sorting student lists in parallel is refused without the parallel token in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsRefusedWithoutTokenMavenArchunitInstrumentation() {
		assertRefused(CountedOperationsMain::parallelSortStudentLists);
	}

	/**
	 * An array that holds itself is sorted in parallel under the parallel token in
	 * ArchunitInstrumentation, without Ares looking into it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortOfACyclicArrayAllowedByTokenMavenArchunitInstrumentation() {
		Assertions.assertEquals(2, CountedOperationsMain.parallelSortCyclicArray());
	}

	/**
	 * A JDK set of tasks handed to {@code invokeAll} runs in WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAJdkSetMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAJdkSet());
	}

	/**
	 * A task set the student wrote is refused in WalaAspectJ before Ares iterates
	 * it, so the thread its iterator starts is never started unchecked.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAStudentSetUnreadMavenWalaAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * Student lists sorted with {@code Arrays.parallelSort} are plain data, allowed
	 * by the parallel token in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsAllowedByTokenMavenWalaAspectJ() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSortStudentLists());
	}

	/**
	 * Sorting student lists in parallel is refused without the parallel token in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsRefusedWithoutTokenMavenWalaAspectJ() {
		assertRefused(CountedOperationsMain::parallelSortStudentLists);
	}

	/**
	 * An array that holds itself is sorted in parallel under the parallel token in
	 * WalaAspectJ, without Ares looking into it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void parallelSortOfACyclicArrayAllowedByTokenMavenWalaAspectJ() {
		Assertions.assertEquals(2, CountedOperationsMain.parallelSortCyclicArray());
	}

	/**
	 * A JDK set of tasks handed to {@code invokeAll} runs in WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAJdkSetMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAJdkSet());
	}

	/**
	 * A task set the student wrote is refused in WalaInstrumentation before Ares
	 * iterates it, so the thread its iterator starts is never started unchecked.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAStudentSetUnreadMavenWalaInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * Student lists sorted with {@code Arrays.parallelSort} are plain data, allowed
	 * by the parallel token in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsAllowedByTokenMavenWalaInstrumentation() {
		Assertions.assertEquals(1, CountedOperationsMain.parallelSortStudentLists());
	}

	/**
	 * Sorting student lists in parallel is refused without the parallel token in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION_WITHOUT_PARALLEL, withinPath = WITHIN_PATH)
	void parallelSortOfStudentListsRefusedWithoutTokenMavenWalaInstrumentation() {
		assertRefused(CountedOperationsMain::parallelSortStudentLists);
	}

	/**
	 * An array that holds itself is sorted in parallel under the parallel token in
	 * WalaInstrumentation, without Ares looking into it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void parallelSortOfACyclicArrayAllowedByTokenMavenWalaInstrumentation() {
		Assertions.assertEquals(2, CountedOperationsMain.parallelSortCyclicArray());
	}

	/**
	 * A set class the student wrote, handed to {@code submit} only as the result
	 * value, is not searched for tasks in ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void submitAcceptsAStudentSetAsResultMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAStudentSetResult());
	}

	/**
	 * A JDK set of tasks handed to {@code submit} only as the result value counts
	 * none of them in ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void submitCountsNoTaskInASetResultMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAJdkSetOfTasksResult());
	}

	/**
	 * A JDK set handed to {@code invokeAll} is still searched for its tasks in
	 * ArchunitAspectJ, so a task the policy does not allow is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAnUnallowedTaskInAJdkSetMavenArchunitAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAJdkSetWithAnUnallowedTask);
	}

	/**
	 * A set class the student wrote, handed to {@code submit} only as the result
	 * value, is not searched for tasks in ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void submitAcceptsAStudentSetAsResultMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAStudentSetResult());
	}

	/**
	 * A JDK set of tasks handed to {@code submit} only as the result value counts
	 * none of them in ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void submitCountsNoTaskInASetResultMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAJdkSetOfTasksResult());
	}

	/**
	 * A JDK set handed to {@code invokeAll} is still searched for its tasks in
	 * ArchunitInstrumentation, so a task the policy does not allow is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAnUnallowedTaskInAJdkSetMavenArchunitInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAJdkSetWithAnUnallowedTask);
	}

	/**
	 * A set class the student wrote, handed to {@code submit} only as the result
	 * value, is not searched for tasks in WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void submitAcceptsAStudentSetAsResultMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAStudentSetResult());
	}

	/**
	 * A JDK set of tasks handed to {@code submit} only as the result value counts
	 * none of them in WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void submitCountsNoTaskInASetResultMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAJdkSetOfTasksResult());
	}

	/**
	 * A JDK set handed to {@code invokeAll} is still searched for its tasks in
	 * WalaAspectJ, so a task the policy does not allow is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAnUnallowedTaskInAJdkSetMavenWalaAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAJdkSetWithAnUnallowedTask);
	}

	/**
	 * A set class the student wrote, handed to {@code submit} only as the result
	 * value, is not searched for tasks in WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void submitAcceptsAStudentSetAsResultMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAStudentSetResult());
	}

	/**
	 * A JDK set of tasks handed to {@code submit} only as the result value counts
	 * none of them in WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void submitCountsNoTaskInASetResultMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitWithAJdkSetOfTasksResult());
	}

	/**
	 * A JDK set handed to {@code invokeAll} is still searched for its tasks in
	 * WalaInstrumentation, so a task the policy does not allow is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAnUnallowedTaskInAJdkSetMavenWalaInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAJdkSetWithAnUnallowedTask);
	}

	/**
	 * The student's task set inside a JDK wrapper is refused in ArchunitAspectJ
	 * before Ares iterates it, so the thread its iterator starts is never started.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedStudentSetUnreadMavenArchunitAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAWrappedStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * An unmodifiable view of a JDK list of allowed tasks is refused in
	 * ArchunitAspectJ, with a message naming the view, since Ares iterates only
	 * plain lists and sets.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedListMavenArchunitAspectJ() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromAWrappedList, "java.util.Collections$Unmodifiable");
	}

	/**
	 * An {@code ArrayList} of allowed tasks handed to {@code invokeAll} runs in
	 * ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAnArrayListMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAnArrayList());
	}

	/**
	 * A {@code List.of} list of allowed tasks handed to {@code invokeAll} runs in
	 * ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAListOfMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAListOf());
	}

	/**
	 * The student's task set inside a JDK wrapper is refused in
	 * ArchunitInstrumentation before Ares iterates it, so the thread its iterator
	 * starts is never started.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedStudentSetUnreadMavenArchunitInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAWrappedStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * An unmodifiable view of a JDK list of allowed tasks is refused in
	 * ArchunitInstrumentation, with a message naming the view, since Ares iterates
	 * only plain lists and sets.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedListMavenArchunitInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromAWrappedList, "java.util.Collections$Unmodifiable");
	}

	/**
	 * An {@code ArrayList} of allowed tasks handed to {@code invokeAll} runs in
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAnArrayListMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAnArrayList());
	}

	/**
	 * A {@code List.of} list of allowed tasks handed to {@code invokeAll} runs in
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAListOfMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAListOf());
	}

	/**
	 * The student's task set inside a JDK wrapper is refused in WalaAspectJ before
	 * Ares iterates it, so the thread its iterator starts is never started.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedStudentSetUnreadMavenWalaAspectJ() {
		assertRefused(CountedOperationsMain::invokeAllFromAWrappedStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * An unmodifiable view of a JDK list of allowed tasks is refused in
	 * WalaAspectJ, with a message naming the view, since Ares iterates only plain
	 * lists and sets.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedListMavenWalaAspectJ() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromAWrappedList, "java.util.Collections$Unmodifiable");
	}

	/**
	 * An {@code ArrayList} of allowed tasks handed to {@code invokeAll} runs in
	 * WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAnArrayListMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAnArrayList());
	}

	/**
	 * A {@code List.of} list of allowed tasks handed to {@code invokeAll} runs in
	 * WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAListOfMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAListOf());
	}

	/**
	 * The student's task set inside a JDK wrapper is refused in WalaInstrumentation
	 * before Ares iterates it, so the thread its iterator starts is never started.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedStudentSetUnreadMavenWalaInstrumentation() {
		assertRefused(CountedOperationsMain::invokeAllFromAWrappedStudentSet);
		Assertions.assertFalse(StudentTaskSet.threadStarted());
	}

	/**
	 * An unmodifiable view of a JDK list of allowed tasks is refused in
	 * WalaInstrumentation, with a message naming the view, since Ares iterates only
	 * plain lists and sets.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesAWrappedListMavenWalaInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromAWrappedList, "java.util.Collections$Unmodifiable");
	}

	/**
	 * An {@code ArrayList} of allowed tasks handed to {@code invokeAll} runs in
	 * WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAnArrayListMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAnArrayList());
	}

	/**
	 * A {@code List.of} list of allowed tasks handed to {@code invokeAll} runs in
	 * WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsTasksFromAListOfMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.invokeAllFromAListOf());
	}

	/**
	 * A task that is also a list is counted as its own class in ArchunitAspectJ, so
	 * one of the allowed class runs.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskIsAllowedMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitListBackedTasks(1));
	}

	/**
	 * A second task that is also a list exceeds its allowance of one in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskBeyondItsAllowanceIsRefusedMavenArchunitAspectJ() {
		assertRefused(() -> CountedOperationsMain.submitListBackedTasks(2));
	}

	/**
	 * A task that is also a list, of a class no allowance names, is refused in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskWithoutAllowanceIsRefusedMavenArchunitAspectJ() {
		assertRefusedNaming(CountedOperationsMain::submitUnlistedListTask, "UnlistedListTask");
	}

	/**
	 * A task that is also a list is counted as its own class in
	 * ArchunitInstrumentation, so one of the allowed class runs.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskIsAllowedMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitListBackedTasks(1));
	}

	/**
	 * A second task that is also a list exceeds its allowance of one in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskBeyondItsAllowanceIsRefusedMavenArchunitInstrumentation() {
		assertRefused(() -> CountedOperationsMain.submitListBackedTasks(2));
	}

	/**
	 * A task that is also a list, of a class no allowance names, is refused in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskWithoutAllowanceIsRefusedMavenArchunitInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::submitUnlistedListTask, "UnlistedListTask");
	}

	/**
	 * A task that is also a list is counted as its own class in WalaAspectJ, so one
	 * of the allowed class runs.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskIsAllowedMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitListBackedTasks(1));
	}

	/**
	 * A second task that is also a list exceeds its allowance of one in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskBeyondItsAllowanceIsRefusedMavenWalaAspectJ() {
		assertRefused(() -> CountedOperationsMain.submitListBackedTasks(2));
	}

	/**
	 * A task that is also a list, of a class no allowance names, is refused in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void listBackedTaskWithoutAllowanceIsRefusedMavenWalaAspectJ() {
		assertRefusedNaming(CountedOperationsMain::submitUnlistedListTask, "UnlistedListTask");
	}

	/**
	 * A task that is also a list is counted as its own class in
	 * WalaInstrumentation, so one of the allowed class runs.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskIsAllowedMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(1, CountedOperationsMain.submitListBackedTasks(1));
	}

	/**
	 * A second task that is also a list exceeds its allowance of one in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskBeyondItsAllowanceIsRefusedMavenWalaInstrumentation() {
		assertRefused(() -> CountedOperationsMain.submitListBackedTasks(2));
	}

	/**
	 * A task that is also a list, of a class no allowance names, is refused in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void listBackedTaskWithoutAllowanceIsRefusedMavenWalaInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::submitUnlistedListTask, "UnlistedListTask");
	}

	/**
	 * A range view of a sorted set is refused in ArchunitAspectJ before Ares
	 * iterates it, so the student's comparator, which the iteration would call,
	 * starts no thread.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesATreeSetRangeUnreadMavenArchunitAspectJ() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromATreeSetRange, "java.util.TreeSet");
		Assertions.assertFalse(StudentTaskComparator.threadStarted());
	}

	/**
	 * A range view of a sorted set is refused in ArchunitInstrumentation before
	 * Ares iterates it, so the student's comparator, which the iteration would
	 * call, starts no thread.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesATreeSetRangeUnreadMavenArchunitInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromATreeSetRange, "java.util.TreeSet");
		Assertions.assertFalse(StudentTaskComparator.threadStarted());
	}

	/**
	 * A range view of a sorted set is refused in WalaAspectJ before Ares iterates
	 * it, so the student's comparator, which the iteration would call, starts no
	 * thread.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesATreeSetRangeUnreadMavenWalaAspectJ() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromATreeSetRange, "java.util.TreeSet");
		Assertions.assertFalse(StudentTaskComparator.threadStarted());
	}

	/**
	 * A range view of a sorted set is refused in WalaInstrumentation before Ares
	 * iterates it, so the student's comparator, which the iteration would call,
	 * starts no thread.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesATreeSetRangeUnreadMavenWalaInstrumentation() {
		assertRefusedNaming(CountedOperationsMain::invokeAllFromATreeSetRange, "java.util.TreeSet");
		Assertions.assertFalse(StudentTaskComparator.threadStarted());
	}

	/**
	 * A batch of three tasks of a class allowed three times runs through
	 * {@code invokeAll} in ArchunitAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsABatchWithinItsAllowanceMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.invokeAllBatch(3));
	}

	/**
	 * A batch of four tasks of a class allowed three times is refused in
	 * ArchunitAspectJ, as each task of a batch uses one allowance.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesABatchBeyondItsAllowanceMavenArchunitAspectJ() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllBatch(4), "CountedCall");
	}

	/**
	 * A batch of three tasks of a class allowed three times runs through
	 * {@code invokeAll} in ArchunitInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsABatchWithinItsAllowanceMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.invokeAllBatch(3));
	}

	/**
	 * A batch of four tasks of a class allowed three times is refused in
	 * ArchunitInstrumentation, as each task of a batch uses one allowance.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesABatchBeyondItsAllowanceMavenArchunitInstrumentation() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllBatch(4), "CountedCall");
	}

	/**
	 * A batch of three tasks of a class allowed three times runs through
	 * {@code invokeAll} in WalaAspectJ.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsABatchWithinItsAllowanceMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.invokeAllBatch(3));
	}

	/**
	 * A batch of four tasks of a class allowed three times is refused in
	 * WalaAspectJ, as each task of a batch uses one allowance.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesABatchBeyondItsAllowanceMavenWalaAspectJ() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllBatch(4), "CountedCall");
	}

	/**
	 * A batch of three tasks of a class allowed three times runs through
	 * {@code invokeAll} in WalaInstrumentation.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsABatchWithinItsAllowanceMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(3, CountedOperationsMain.invokeAllBatch(3));
	}

	/**
	 * A batch of four tasks of a class allowed three times is refused in
	 * WalaInstrumentation, as each task of a batch uses one allowance.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesABatchBeyondItsAllowanceMavenWalaInstrumentation() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllBatch(4), "CountedCall");
	}

	/**
	 * Two callable tasks that are also sets, of a class allowed twice, run through
	 * {@code invokeAll} in ArchunitAspectJ: each counts as a task and is not read
	 * as a collection.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsSetBackedTasksMavenArchunitAspectJ() throws Exception {
		Assertions.assertEquals(2, CountedOperationsMain.invokeAllSetBackedCalls(2));
	}

	/**
	 * A third callable task that is also a set exceeds its allowance of two in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesSetBackedTasksBeyondTheirAllowanceMavenArchunitAspectJ() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllSetBackedCalls(3), "SetBackedCall");
	}

	/**
	 * Two callable tasks that are also sets, of a class allowed twice, run through
	 * {@code invokeAll} in ArchunitInstrumentation: each counts as a task and is
	 * not read as a collection.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsSetBackedTasksMavenArchunitInstrumentation() throws Exception {
		Assertions.assertEquals(2, CountedOperationsMain.invokeAllSetBackedCalls(2));
	}

	/**
	 * A third callable task that is also a set exceeds its allowance of two in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesSetBackedTasksBeyondTheirAllowanceMavenArchunitInstrumentation() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllSetBackedCalls(3), "SetBackedCall");
	}

	/**
	 * Two callable tasks that are also sets, of a class allowed twice, run through
	 * {@code invokeAll} in WalaAspectJ: each counts as a task and is not read as a
	 * collection.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRunsSetBackedTasksMavenWalaAspectJ() throws Exception {
		Assertions.assertEquals(2, CountedOperationsMain.invokeAllSetBackedCalls(2));
	}

	/**
	 * A third callable task that is also a set exceeds its allowance of two in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void invokeAllRefusesSetBackedTasksBeyondTheirAllowanceMavenWalaAspectJ() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllSetBackedCalls(3), "SetBackedCall");
	}

	/**
	 * Two callable tasks that are also sets, of a class allowed twice, run through
	 * {@code invokeAll} in WalaInstrumentation: each counts as a task and is not
	 * read as a collection.
	 *
	 * @throws Exception if the pool fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRunsSetBackedTasksMavenWalaInstrumentation() throws Exception {
		Assertions.assertEquals(2, CountedOperationsMain.invokeAllSetBackedCalls(2));
	}

	/**
	 * A third callable task that is also a set exceeds its allowance of two in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void invokeAllRefusesSetBackedTasksBeyondTheirAllowanceMavenWalaInstrumentation() {
		assertRefusedNaming(() -> CountedOperationsMain.invokeAllSetBackedCalls(3), "SetBackedCall");
	}
}
