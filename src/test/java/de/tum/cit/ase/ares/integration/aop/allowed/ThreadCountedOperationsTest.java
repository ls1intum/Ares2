package de.tum.cit.ase.ares.integration.aop.allowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations.CountedOperationsMain;

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
}
