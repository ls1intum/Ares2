package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService.CreateExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread.CreateThreadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadGroup.CreateThreadGroupMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadBuilder.CreateThreadBuilderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executor.CreateExecutorMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledExecutorService.CreateScheduledExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.forkJoinPool.CreateForkJoinPoolMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadPoolExecutor.CreateThreadPoolExecutorMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledThreadPoolExecutor.CreateScheduledThreadPoolExecutorMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.completableFuture.CreateCompletableFutureMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.parallelStream.CreateParallelStreamMain;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ThreadSystemAccessTest extends SystemAccessTest {

    private static final String THREAD_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/thread";
    private static final String THREAD_GROUP_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadGroup";
    private static final String THREAD_BUILDER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadBuilder";
    private static final String EXECUTOR_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executor";
    private static final String EXECUTOR_SERVICE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorService";
    private static final String SCHEDULED_EXECUTOR_SERVICE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledExecutorService";
    private static final String FORK_JOIN_POOL_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/forkJoinPool";
    private static final String THREAD_POOL_EXECUTOR_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadPoolExecutor";
    private static final String SCHEDULED_THREAD_POOL_EXECUTOR_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledThreadPoolExecutor";
    private static final String COMPLETABLE_FUTURE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/completableFuture";
    private static final String PARALLEL_STREAM_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/parallelStream";

    // <editor-fold desc="accessThreadSystemViaStartThread">
    @Test
    void test_createThreadViaStartMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenArchunitAspectJ() {
        CreateThreadMain.startThread();
    }

    @Test
    void test_createThreadViaStartMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenArchunitInstrumentation() {
        CreateThreadMain.startThread();
    }

    @Test
    void test_createThreadViaStartMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenWalaAspectJ() {
        CreateThreadMain.startThread();
    }

    @Test
    void test_createThreadViaStartMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaStartMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createThreadViaStartMavenWalaInstrumentation() {
        CreateThreadMain.startThread();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStartVirtualThread">
    @Test
    void test_createVirtualThreadMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createVirtualThreadMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createVirtualThreadMavenArchunitAspectJ() {
        CreateThreadMain.startVirtualThread();
    }


    @Test
    void test_createVirtualThreadMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createVirtualThreadMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createVirtualThreadMavenArchunitInstrumentation() {
        CreateThreadMain.startVirtualThread();
    }


    @Test
    void test_createVirtualThreadMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createVirtualThreadMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_PATH)
    public void test_createVirtualThreadMavenWalaAspectJ() {
        CreateThreadMain.startVirtualThread();
    }


    @Test
    void test_createVirtualThreadMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createVirtualThreadMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_PATH)
    public void test_createVirtualThreadMavenWalaInstrumentation() {
        CreateThreadMain.startVirtualThread();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateThreadInGroup">
    @Test
    void test_createThreadInGroupMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadInGroupMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_GROUP_PATH)
    public void test_createThreadInGroupMavenArchunitAspectJ() {
        CreateThreadGroupMain.createThreadInGroup();
    }


    @Test
    void test_createThreadInGroupMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadInGroupMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_GROUP_PATH)
    public void test_createThreadInGroupMavenArchunitInstrumentation() {
        CreateThreadGroupMain.createThreadInGroup();
    }


    @Test
    void test_createThreadInGroupMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadInGroupMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_GROUP_PATH)
    public void test_createThreadInGroupMavenWalaAspectJ() {
        CreateThreadGroupMain.createThreadInGroup();
    }


    @Test
    void test_createThreadInGroupMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadInGroupMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_GROUP_PATH)
    public void test_createThreadInGroupMavenWalaInstrumentation() {
        CreateThreadGroupMain.createThreadInGroup();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateThreadBuilder">
    @Test
    void test_createThreadViaBuilderMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaBuilderMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_BUILDER_PATH)
    public void test_createThreadViaBuilderMavenArchunitAspectJ() {
        CreateThreadBuilderMain.createThreadBuilder();
    }

    @Test
    void test_createThreadViaBuilderMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaBuilderMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_BUILDER_PATH)
    public void test_createThreadViaBuilderMavenArchunitInstrumentation() {
        CreateThreadBuilderMain.createThreadBuilder();
    }

    @Test
    void test_createThreadViaBuilderMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaBuilderMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_BUILDER_PATH)
    public void test_createThreadViaBuilderMavenWalaAspectJ() {
        CreateThreadBuilderMain.createThreadBuilder();
    }

    @Test
    void test_createThreadViaBuilderMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaBuilderMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_BUILDER_PATH)
    public void test_createThreadViaBuilderMavenWalaInstrumentation() {
        CreateThreadBuilderMain.createThreadBuilder();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateExecutor">
    @Test
    void test_createThreadViaExecutorMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaExecutorMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_PATH)
    public void test_createThreadViaExecutorMavenArchunitAspectJ() {
        CreateExecutorMain.createExecutor();
    }

    @Test
    void test_createThreadViaExecutorMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaExecutorMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_PATH)
    public void test_createThreadViaExecutorMavenArchunitInstrumentation() {
        CreateExecutorMain.createExecutor();
    }

    @Test
    void test_createThreadViaExecutorMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaExecutorMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_PATH)
    public void test_createThreadViaExecutorMavenWalaAspectJ() {
        CreateExecutorMain.createExecutor();
    }

    @Test
    void test_createThreadViaExecutorMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createThreadViaExecutorMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_PATH)
    public void test_createThreadViaExecutorMavenWalaInstrumentation() {
        CreateExecutorMain.createExecutor();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateExecutorService">
    @Test
    void test_createExecutorServiceMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createExecutorServiceMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_createExecutorServiceMavenArchunitAspectJ() {
        CreateExecutorServiceMain.createExecutorService();
    }

    @Test
    void test_createExecutorServiceMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createExecutorServiceMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_createExecutorServiceMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.createExecutorService();
    }

    @Test
    void test_createExecutorServiceMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_createExecutorServiceMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_createExecutorServiceMavenWalaAspectJ() {
        CreateExecutorServiceMain.createExecutorService();
    }

    @Test
    void test_createExecutorServiceMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_createExecutorServiceMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_createExecutorServiceMavenWalaInstrumentation() {
        CreateExecutorServiceMain.createExecutorService();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitCallable">
    @Test
    void test_submitCallableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitCallableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Test
    void test_submitCallableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitCallableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Test
    void test_submitCallableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitCallableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitCallable();
    }

    @Test
    void test_submitCallableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitCallableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitCallableMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnableWithResult">
    @Test
    void test_submitRunnableWithResultMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Test
    void test_submitRunnableWithResultMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Test
    void test_submitRunnableWithResultMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }

    @Test
    void test_submitRunnableWithResultMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableWithResultMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableWithResultMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitRunnableWithResult();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceSubmitRunnable">
    @Test
    void test_submitRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenArchunitAspectJ() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Test
    void test_submitRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenArchunitInstrumentation() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Test
    void test_submitRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenWalaAspectJ() {
        CreateExecutorServiceMain.submitRunnable();
    }

    @Test
    void test_submitRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_submitRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_submitRunnableMavenWalaInstrumentation() {
        CreateExecutorServiceMain.submitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAll">
    @Test
    void test_invokeAllMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAllMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenArchunitAspectJ() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Test
    void test_invokeAllMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAllMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenArchunitInstrumentation() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Test
    void test_invokeAllMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAllMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenWalaAspectJ() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }

    @Test
    void test_invokeAllMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAllMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAllMavenWalaInstrumentation() throws InterruptedException {
        CreateExecutorServiceMain.invokeAll();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaExecutorServiceInvokeAny">
    @Test
    void test_invokeAnyMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAnyMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenArchunitAspectJ() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Test
    void test_invokeAnyMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAnyMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenArchunitInstrumentation() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Test
    void test_invokeAnyMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAnyMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenWalaAspectJ() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }

    @Test
    void test_invokeAnyMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_invokeAnyMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXECUTOR_SERVICE_PATH)
    public void test_invokeAnyMavenWalaInstrumentation() throws Exception {
        CreateExecutorServiceMain.invokeAny();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleRunnable">
    @Test
    void test_scheduleRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Test
    void test_scheduleRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Test
    void test_scheduleRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }

    @Test
    void test_scheduleRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleRunnableMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleCallable">
    @Test
    void test_scheduleCallableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleCallableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Test
    void test_scheduleCallableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleCallableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Test
    void test_scheduleCallableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleCallableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }

    @Test
    void test_scheduleCallableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleCallableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleCallableMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleAtFixedRate">
    @Test
    void test_scheduleAtFixedRateMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Test
    void test_scheduleAtFixedRateMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Test
    void test_scheduleAtFixedRateMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }

    @Test
    void test_scheduleAtFixedRateMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleAtFixedRateMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleAtFixedRateMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleAtFixedRate();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledExecutorServiceScheduleWithFixedDelay">
    @Test
    void test_scheduleWithFixedDelayMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleWithFixedDelayMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleWithFixedDelayMavenArchunitAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleWithFixedDelay();
    }

    @Test
    void test_scheduleWithFixedDelayMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleWithFixedDelayMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleWithFixedDelayMavenArchunitInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleWithFixedDelay();
    }

    @Test
    void test_scheduleWithFixedDelayMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleWithFixedDelayMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleWithFixedDelayMavenWalaAspectJ() {
        CreateScheduledExecutorServiceMain.scheduleWithFixedDelay();
    }

    @Test
    void test_scheduleWithFixedDelayMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduleWithFixedDelayMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_EXECUTOR_SERVICE_PATH)
    public void test_scheduleWithFixedDelayMavenWalaInstrumentation() {
        CreateScheduledExecutorServiceMain.scheduleWithFixedDelay();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolExecuteRunnable">
    @Test
    void test_forkJoinPool_executeRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_executeRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_executeRunnableMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.executeRunnable();
    }

    @Test
    void test_forkJoinPool_executeRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_executeRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_executeRunnableMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.executeRunnable();
    }

    @Test
    void test_forkJoinPool_executeRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_executeRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_executeRunnableMavenWalaAspectJ() {
        CreateForkJoinPoolMain.executeRunnable();
    }

    @Test
    void test_forkJoinPool_executeRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_executeRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_executeRunnableMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.executeRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolSubmitRunnable">
    @Test
    void test_forkJoinPool_submitRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitRunnableMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.submitRunnable();
    }

    @Test
    void test_forkJoinPool_submitRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitRunnableMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.submitRunnable();
    }

    @Test
    void test_forkJoinPool_submitRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitRunnableMavenWalaAspectJ() {
        CreateForkJoinPoolMain.submitRunnable();
    }

    @Test
    void test_forkJoinPool_submitRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitRunnableMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.submitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPoolSubmitCallable">
    @Test
    void test_forkJoinPool_submitCallableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitCallableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitCallableMavenArchunitAspectJ() {
        CreateForkJoinPoolMain.submitCallable();
    }

    @Test
    void test_forkJoinPool_submitCallableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitCallableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitCallableMavenArchunitInstrumentation() {
        CreateForkJoinPoolMain.submitCallable();
    }

    @Test
    void test_forkJoinPool_submitCallableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitCallableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitCallableMavenWalaAspectJ() {
        CreateForkJoinPoolMain.submitCallable();
    }

    @Test
    void test_forkJoinPool_submitCallableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_forkJoinPool_submitCallableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORK_JOIN_POOL_PATH)
    public void test_forkJoinPool_submitCallableMavenWalaInstrumentation() {
        CreateForkJoinPoolMain.submitCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutorExecuteRunnable">
    @Test
    void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        CreateThreadPoolExecutorMain.executeRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutorSubmitRunnable">
    @Test
    void test_threadPoolExecutor_submitRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitRunnableMavenArchunitAspectJ() {
        CreateThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_threadPoolExecutor_submitRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitRunnableMavenArchunitInstrumentation() {
        CreateThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_threadPoolExecutor_submitRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitRunnableMavenWalaAspectJ() {
        CreateThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_threadPoolExecutor_submitRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitRunnableMavenWalaInstrumentation() {
        CreateThreadPoolExecutorMain.submitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutorSubmitCallable">
    @Test
    void test_threadPoolExecutor_submitCallableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitCallableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitCallableMavenArchunitAspectJ() {
        CreateThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_threadPoolExecutor_submitCallableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitCallableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitCallableMavenArchunitInstrumentation() {
        CreateThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_threadPoolExecutor_submitCallableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitCallableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitCallableMavenWalaAspectJ() {
        CreateThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_threadPoolExecutor_submitCallableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_threadPoolExecutor_submitCallableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THREAD_POOL_EXECUTOR_PATH)
    public void test_threadPoolExecutor_submitCallableMavenWalaInstrumentation() {
        CreateThreadPoolExecutorMain.submitCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutorExecuteRunnable">
    @Test
    void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        CreateScheduledThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_executeRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        CreateScheduledThreadPoolExecutorMain.executeRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_executeRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.executeRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutorSubmitRunnable">
    @Test
    void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitAspectJ() {
        CreateScheduledThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_submitRunnableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaAspectJ() {
        CreateScheduledThreadPoolExecutorMain.submitRunnable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_submitRunnableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.submitRunnable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutorSubmitCallable">
    @Test
    void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_submitCallableMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitAspectJ() {
        CreateScheduledThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_submitCallableMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitCallableMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_scheduledThreadPoolExecutor_submitCallableMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenWalaAspectJ() {
        CreateScheduledThreadPoolExecutorMain.submitCallable();
    }

    @Test
    void test_scheduledThreadPoolExecutor_submitCallableMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_scheduledThreadPoolExecutor_submitCallableMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_PATH)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenWalaInstrumentation() {
        CreateScheduledThreadPoolExecutorMain.submitCallable();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureRunAsync">
    @Test
    void test_completableFuture_runAsyncMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_runAsyncMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_runAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.runAsync();
    }

    @Test
    void test_completableFuture_runAsyncMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_runAsyncMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_runAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.runAsync();
    }

    @Test
    void test_completableFuture_runAsyncMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_runAsyncMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_runAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.runAsync();
    }

    @Test
    void test_completableFuture_runAsyncMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_runAsyncMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_runAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.runAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureSupplyAsync">
    @Test
    void test_completableFuture_supplyAsyncMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_supplyAsyncMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_supplyAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Test
    void test_completableFuture_supplyAsyncMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_supplyAsyncMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_supplyAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Test
    void test_completableFuture_supplyAsyncMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_supplyAsyncMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_supplyAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.supplyAsync();
    }

    @Test
    void test_completableFuture_supplyAsyncMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_supplyAsyncMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_supplyAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.supplyAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFutureThenApplyAsync">
    @Test
    void test_completableFuture_thenApplyAsyncMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_thenApplyAsyncMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_thenApplyAsyncMavenArchunitAspectJ() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Test
    void test_completableFuture_thenApplyAsyncMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_thenApplyAsyncMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_thenApplyAsyncMavenArchunitInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Test
    void test_completableFuture_thenApplyAsyncMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_thenApplyAsyncMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_thenApplyAsyncMavenWalaAspectJ() {
        CreateCompletableFutureMain.thenApplyAsync();
    }

    @Test
    void test_completableFuture_thenApplyAsyncMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_completableFuture_thenApplyAsyncMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = COMPLETABLE_FUTURE_PATH)
    public void test_completableFuture_thenApplyAsyncMavenWalaInstrumentation() {
        CreateCompletableFutureMain.thenApplyAsync();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamCollectionParallelStream">
    @Test
    void test_parallelStream_collectionParallelStreamMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_collectionParallelStreamMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_collectionParallelStreamMavenArchunitAspectJ() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Test
    void test_parallelStream_collectionParallelStreamMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class,
                "test_parallelStream_collectionParallelStreamMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_collectionParallelStreamMavenArchunitInstrumentation() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Test
    void test_parallelStream_collectionParallelStreamMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_collectionParallelStreamMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_collectionParallelStreamMavenWalaAspectJ() {
        CreateParallelStreamMain.collectionParallelStream();
    }

    @Test
    void test_parallelStream_collectionParallelStreamMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_collectionParallelStreamMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_collectionParallelStreamMavenWalaInstrumentation() {
        CreateParallelStreamMain.collectionParallelStream();
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStreamStreamParallel">
    @Test
    void test_parallelStream_streamParallelMavenArchunitAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_streamParallelMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_streamParallelMavenArchunitAspectJ() {
        CreateParallelStreamMain.streamParallel();
    }

    @Test
    void test_parallelStream_streamParallelMavenArchunitInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_streamParallelMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_streamParallelMavenArchunitInstrumentation() {
        CreateParallelStreamMain.streamParallel();
    }

    @Test
    void test_parallelStream_streamParallelMavenWalaAspectJ_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_streamParallelMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_streamParallelMavenWalaAspectJ() {
        CreateParallelStreamMain.streamParallel();
    }

    @Test
    void test_parallelStream_streamParallelMavenWalaInstrumentation_test() {
        testtest(ThreadSystemAccessTest.class, "test_parallelStream_streamParallelMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PARALLEL_STREAM_PATH)
    public void test_parallelStream_streamParallelMavenWalaInstrumentation() {
        CreateParallelStreamMain.streamParallel();
    }
    // </editor-fold>

}
