package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.completableFuture.CreateCompletableFutureMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService.CreateExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.forkJoinPool.CreateForkJoinPoolMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.parallelStream.CreateParallelStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledExecutorService.CreateScheduledExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread.CreateThreadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadFactory.CreateThreadFactoryMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadPoolExecutor.CreateThreadPoolExecutorMain;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(ExecutionMode.SAME_THREAD)
public class ThreadSystemAccessTest extends SystemAccessTest {

    private static final String THREAD_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/thread";
    private static final String EXECUTOR_SERVICE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorService";
    private static final String SCHEDULED_EXECUTOR_SERVICE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledExecutorService";
    private static final String THREAD_POOL_EXECUTOR_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadPoolExecutor";
    private static final String COMPLETABLE_FUTURE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/completableFuture";
    private static final String FORK_JOIN_POOL_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/forkJoinPool";
    private static final String PARALLEL_STREAM_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/parallelStream";
    private static final String THREAD_FACTORY_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadFactory";

    // <editor-fold desc="accessThreadSystemViaStartThread">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenArchunitAspectJ() {
        CreateThreadMain.startThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenArchunitInstrumentation() {
        CreateThreadMain.startThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenWalaAspectJ() {
        CreateThreadMain.startThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenWalaInstrumentation() {
        CreateThreadMain.startThread();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitCallable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnableWithResult">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAll">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenArchunitAspectJ() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenArchunitInstrumentation() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenWalaAspectJ() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenWalaInstrumentation() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAny">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenArchunitAspectJ() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenArchunitInstrumentation() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenWalaAspectJ() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenWalaInstrumentation() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleRunnable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAnyWithTimeout">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyWithTimeoutMavenArchunitAspectJ() {
        try {
            CreateExecutorServiceMain.invokeAnyWithTimeout();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyWithTimeoutMavenArchunitInstrumentation() {
        try {
            CreateExecutorServiceMain.invokeAnyWithTimeout();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyWithTimeoutMavenWalaAspectJ() {
        try {
            CreateExecutorServiceMain.invokeAnyWithTimeout();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyWithTimeoutMavenWalaInstrumentation() {
        try {
            CreateExecutorServiceMain.invokeAnyWithTimeout();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleCallable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleAtFixedRate">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutorExecuteRunnable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaNotifyThread">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_notifyThreadMavenArchunitAspectJ() {
        CreateThreadMain.notifyThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_notifyThreadMavenArchunitInstrumentation() {
        CreateThreadMain.notifyThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_notifyThreadMavenWalaAspectJ() {
        CreateThreadMain.notifyThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_notifyThreadMavenWalaInstrumentation() {
        CreateThreadMain.notifyThread();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureRunAsync">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.runAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.runAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.runAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.runAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureRunAsyncWithExecutor">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncWithExecutorMavenArchunitAspectJ() {
        CreateCompletableFutureMain.runAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncWithExecutorMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.runAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncWithExecutorMavenWalaAspectJ() {
        CreateCompletableFutureMain.runAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_runAsyncWithExecutorMavenWalaInstrumentation() {
        CreateCompletableFutureMain.runAsyncWithExecutor();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureSupplyAsync">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.supplyAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureSupplyAsyncWithExecutor">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncWithExecutorMavenArchunitAspectJ() {
        CreateCompletableFutureMain.supplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncWithExecutorMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.supplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncWithExecutorMavenWalaAspectJ() {
        CreateCompletableFutureMain.supplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_supplyAsyncWithExecutorMavenWalaInstrumentation() {
        CreateCompletableFutureMain.supplyAsyncWithExecutor();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenApplyAsync">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenApplyAsyncWithExecutor">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncWithExecutorMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenApplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncWithExecutorMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncWithExecutorMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenApplyAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenApplyAsyncWithExecutorMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsyncWithExecutor();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombine">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenCombine();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenCombine();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenCombine();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenCombine();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombineAsync">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenCombineAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenCombineAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenCombineAsync();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenCombineAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombineAsyncWithExecutor">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncWithExecutorMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenCombineAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncWithExecutorMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenCombineAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncWithExecutorMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenCombineAsyncWithExecutor();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_thenCombineAsyncWithExecutorMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenCombineAsyncWithExecutor();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolExecute">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolExecuteMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.commonPoolExecute();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolExecuteMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.commonPoolExecute();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolExecuteMavenWalaAspectJ() {
        CreateForkJoinPoolMain.commonPoolExecute();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolExecuteMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.commonPoolExecute();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolSubmitCallableTask">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitCallableTaskMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.commonPoolSubmitCallableTask();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitCallableTaskMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.commonPoolSubmitCallableTask();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitCallableTaskMavenWalaAspectJ() {
        CreateForkJoinPoolMain.commonPoolSubmitCallableTask();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitCallableTaskMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.commonPoolSubmitCallableTask();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolSubmitRunnable">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitRunnableMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.commonPoolSubmitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitRunnableMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.commonPoolSubmitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitRunnableMavenWalaAspectJ() {
        CreateForkJoinPoolMain.commonPoolSubmitRunnable();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_commonPoolSubmitRunnableMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.commonPoolSubmitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamCollectionParallelStream">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_collectionParallelStreamMavenArchunitAspectJ() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_collectionParallelStreamMavenArchunitInstrumentation() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_collectionParallelStreamMavenWalaAspectJ() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_collectionParallelStreamMavenWalaInstrumentation() {
        CreateParallelStreamMain.collectionParallelStream();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamStreamParallel">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_streamParallelMavenArchunitAspectJ() {
        CreateParallelStreamMain.streamParallel();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_streamParallelMavenArchunitInstrumentation() {
        CreateParallelStreamMain.streamParallel();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_streamParallelMavenWalaAspectJ() {
        CreateParallelStreamMain.streamParallel();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_streamParallelMavenWalaInstrumentation() {
        CreateParallelStreamMain.streamParallel();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadFactoryNewThread">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_FACTORY_PATH)
    public void test_newThreadMavenArchunitAspectJ() {
        CreateThreadFactoryMain.newThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_FACTORY_PATH)
    public void test_newThreadMavenArchunitInstrumentation() {
        CreateThreadFactoryMain.newThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_FACTORY_PATH)
    public void test_newThreadMavenWalaAspectJ() {
        CreateThreadFactoryMain.newThread();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_FACTORY_PATH)
    public void test_newThreadMavenWalaInstrumentation() {
        CreateThreadFactoryMain.newThread();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStartThread">
    @Test
    void test_createThreadViaStartMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenArchunitAspectJ");
    }

    @Test
    void test_createThreadViaStartMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenArchunitInstrumentation");
    }

    @Test
    void test_createThreadViaStartMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenWalaAspectJ");
    }

    @Test
    void test_createThreadViaStartMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitCallable">
    @Test
    void test_submitCallableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitCallableMavenArchunitAspectJ");
    }

    @Test
    void test_submitCallableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitCallableMavenArchunitInstrumentation");
    }

    @Test
    void test_submitCallableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitCallableMavenWalaAspectJ");
    }

    @Test
    void test_submitCallableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitCallableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnableWithResult">
    @Test
    void test_submitRunnableWithResultMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenArchunitAspectJ");
    }

    @Test
    void test_submitRunnableWithResultMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenArchunitInstrumentation");
    }

    @Test
    void test_submitRunnableWithResultMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenWalaAspectJ");
    }

    @Test
    void test_submitRunnableWithResultMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnable">
    @Test
    void test_submitRunnableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableMavenArchunitAspectJ");
    }

    @Test
    void test_submitRunnableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableMavenArchunitInstrumentation");
    }

    @Test
    void test_submitRunnableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableMavenWalaAspectJ");
    }

    @Test
    void test_submitRunnableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_submitRunnableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAll">
    @Test
    void test_invokeAllMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAllMavenArchunitAspectJ");
    }

    @Test
    void test_invokeAllMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAllMavenArchunitInstrumentation");
    }

    @Test
    void test_invokeAllMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAllMavenWalaAspectJ");
    }

    @Test
    void test_invokeAllMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAllMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAny">
    @Test
    void test_invokeAnyMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyMavenArchunitAspectJ");
    }

    @Test
    void test_invokeAnyMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyMavenArchunitInstrumentation");
    }

    @Test
    void test_invokeAnyMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyMavenWalaAspectJ");
    }

    @Test
    void test_invokeAnyMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleRunnable">
    @Test
    void test_scheduleRunnableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenArchunitAspectJ");
    }

    @Test
    void test_scheduleRunnableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenArchunitInstrumentation");
    }

    @Test
    void test_scheduleRunnableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenWalaAspectJ");
    }

    @Test
    void test_scheduleRunnableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAnyWithTimeout">
    @Test
    void test_invokeAnyWithTimeoutMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyWithTimeoutMavenArchunitAspectJ");
    }

    @Test
    void test_invokeAnyWithTimeoutMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyWithTimeoutMavenArchunitInstrumentation");
    }

    @Test
    void test_invokeAnyWithTimeoutMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyWithTimeoutMavenWalaAspectJ");
    }

    @Test
    void test_invokeAnyWithTimeoutMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_invokeAnyWithTimeoutMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureRunAsync">
    @Test
    void test_runAsyncMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncMavenArchunitAspectJ");
    }

    @Test
    void test_runAsyncMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncMavenArchunitInstrumentation");
    }

    @Test
    void test_runAsyncMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncMavenWalaAspectJ");
    }

    @Test
    void test_runAsyncMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureRunAsyncWithExecutor">
    @Test
    void test_runAsyncWithExecutorMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncWithExecutorMavenArchunitAspectJ");
    }

    @Test
    void test_runAsyncWithExecutorMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncWithExecutorMavenArchunitInstrumentation");
    }

    @Test
    void test_runAsyncWithExecutorMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncWithExecutorMavenWalaAspectJ");
    }

    @Test
    void test_runAsyncWithExecutorMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_runAsyncWithExecutorMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureSupplyAsync">
    @Test
    void test_supplyAsyncMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncMavenArchunitAspectJ");
    }

    @Test
    void test_supplyAsyncMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncMavenArchunitInstrumentation");
    }

    @Test
    void test_supplyAsyncMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncMavenWalaAspectJ");
    }

    @Test
    void test_supplyAsyncMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureSupplyAsyncWithExecutor">
    @Test
    void test_supplyAsyncWithExecutorMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncWithExecutorMavenArchunitAspectJ");
    }

    @Test
    void test_supplyAsyncWithExecutorMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncWithExecutorMavenArchunitInstrumentation");
    }

    @Test
    void test_supplyAsyncWithExecutorMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncWithExecutorMavenWalaAspectJ");
    }

    @Test
    void test_supplyAsyncWithExecutorMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_supplyAsyncWithExecutorMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenApplyAsync">
    @Test
    void test_thenApplyAsyncMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncMavenArchunitAspectJ");
    }

    @Test
    void test_thenApplyAsyncMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncMavenArchunitInstrumentation");
    }

    @Test
    void test_thenApplyAsyncMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncMavenWalaAspectJ");
    }

    @Test
    void test_thenApplyAsyncMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenApplyAsyncWithExecutor">
    @Test
    void test_thenApplyAsyncWithExecutorMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncWithExecutorMavenArchunitAspectJ");
    }

    @Test
    void test_thenApplyAsyncWithExecutorMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncWithExecutorMavenArchunitInstrumentation");
    }

    @Test
    void test_thenApplyAsyncWithExecutorMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncWithExecutorMavenWalaAspectJ");
    }

    @Test
    void test_thenApplyAsyncWithExecutorMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenApplyAsyncWithExecutorMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombine">
    @Test
    void test_thenCombineMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineMavenArchunitAspectJ");
    }

    @Test
    void test_thenCombineMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineMavenArchunitInstrumentation");
    }

    @Test
    void test_thenCombineMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineMavenWalaAspectJ");
    }

    @Test
    void test_thenCombineMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombineAsync">
    @Test
    void test_thenCombineAsyncMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncMavenArchunitAspectJ");
    }

    @Test
    void test_thenCombineAsyncMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncMavenArchunitInstrumentation");
    }

    @Test
    void test_thenCombineAsyncMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncMavenWalaAspectJ");
    }

    @Test
    void test_thenCombineAsyncMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenCombineAsyncWithExecutor">
    @Test
    void test_thenCombineAsyncWithExecutorMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncWithExecutorMavenArchunitAspectJ");
    }

    @Test
    void test_thenCombineAsyncWithExecutorMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncWithExecutorMavenArchunitInstrumentation");
    }

    @Test
    void test_thenCombineAsyncWithExecutorMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncWithExecutorMavenWalaAspectJ");
    }

    @Test
    void test_thenCombineAsyncWithExecutorMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_thenCombineAsyncWithExecutorMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolExecute">
    @Test
    void test_commonPoolExecuteMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolExecuteMavenArchunitAspectJ");
    }

    @Test
    void test_commonPoolExecuteMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolExecuteMavenArchunitInstrumentation");
    }

    @Test
    void test_commonPoolExecuteMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolExecuteMavenWalaAspectJ");
    }

    @Test
    void test_commonPoolExecuteMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolExecuteMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolSubmitCallableTask">
    @Test
    void test_commonPoolSubmitCallableTaskMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitCallableTaskMavenArchunitAspectJ");
    }

    @Test
    void test_commonPoolSubmitCallableTaskMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitCallableTaskMavenArchunitInstrumentation");
    }

    @Test
    void test_commonPoolSubmitCallableTaskMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitCallableTaskMavenWalaAspectJ");
    }

    @Test
    void test_commonPoolSubmitCallableTaskMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitCallableTaskMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolCommonPoolSubmitRunnable">
    @Test
    void test_commonPoolSubmitRunnableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitRunnableMavenArchunitAspectJ");
    }

    @Test
    void test_commonPoolSubmitRunnableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitRunnableMavenArchunitInstrumentation");
    }

    @Test
    void test_commonPoolSubmitRunnableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitRunnableMavenWalaAspectJ");
    }

    @Test
    void test_commonPoolSubmitRunnableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_commonPoolSubmitRunnableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamCollectionParallelStream">
    @Test
    void test_collectionParallelStreamMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_collectionParallelStreamMavenArchunitAspectJ");
    }

    @Test
    void test_collectionParallelStreamMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_collectionParallelStreamMavenArchunitInstrumentation");
    }

    @Test
    void test_collectionParallelStreamMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_collectionParallelStreamMavenWalaAspectJ");
    }

    @Test
    void test_collectionParallelStreamMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_collectionParallelStreamMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamStreamParallel">
    @Test
    void test_streamParallelMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_streamParallelMavenArchunitAspectJ");
    }

    @Test
    void test_streamParallelMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_streamParallelMavenArchunitInstrumentation");
    }

    @Test
    void test_streamParallelMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_streamParallelMavenWalaAspectJ");
    }

    @Test
    void test_streamParallelMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_streamParallelMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadFactoryNewThread">
    @Test
    void test_newThreadMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_newThreadMavenArchunitAspectJ");
    }

    @Test
    void test_newThreadMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_newThreadMavenArchunitInstrumentation");
    }

    @Test
    void test_newThreadMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_newThreadMavenWalaAspectJ");
    }

    @Test
    void test_newThreadMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_newThreadMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleCallable">
    @Test
    void test_scheduleCallableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleCallableMavenArchunitAspectJ");
    }

    @Test
    void test_scheduleCallableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleCallableMavenArchunitInstrumentation");
    }

    @Test
    void test_scheduleCallableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleCallableMavenWalaAspectJ");
    }

    @Test
    void test_scheduleCallableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleCallableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleAtFixedRate">
    @Test
    void test_scheduleAtFixedRateMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenArchunitAspectJ");
    }

    @Test
    void test_scheduleAtFixedRateMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenArchunitInstrumentation");
    }

    @Test
    void test_scheduleAtFixedRateMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenWalaAspectJ");
    }

    @Test
    void test_scheduleAtFixedRateMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutorExecuteRunnable">
    @Test
    void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ");
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation");
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenWalaAspectJ");
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaNotifyThread">
    @Test
    void test_notifyThreadMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_notifyThreadMavenArchunitAspectJ");
    }

    @Test
    void test_notifyThreadMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_notifyThreadMavenArchunitInstrumentation");
    }

    @Test
    void test_notifyThreadMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_notifyThreadMavenWalaAspectJ");
    }

    @Test
    void test_notifyThreadMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(ThreadSystemAccessTest.class, "test_notifyThreadMavenWalaInstrumentation");
    }
    // </editor-fold>
}
