package de.tum.cit.ase.ares.integration.aop.forbidden;

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

class ThreadSystemAccessTest extends SystemAccessTest {

    private static final String THREAD_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/thread";
    private static final String THREAD_GROUP_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadGroup";
    private static final String THREAD_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadBuilder";
    private static final String EXECUTOR_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executor";
    private static final String EXECUTOR_SERVICE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorService";
    private static final String SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledExecutorService";
    private static final String FORK_JOIN_POOL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/forkJoinPool";
    private static final String THREAD_POOL_EXECUTOR_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadPoolExecutor";
    private static final String SCHEDULED_THREAD_POOL_EXECUTOR_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledThreadPoolExecutor";
    private static final String COMPLETABLE_FUTURE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/completableFuture";
    private static final String PARALLEL_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/parallelStream";

    // <editor-fold desc="accessThreadSystemViaStartThread">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startThreadMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::startThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startThreadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::startThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startThreadMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::startThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startThreadMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::startThread, CreateThreadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStartVirtualThread">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startVirtualThreadMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::startVirtualThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startVirtualThreadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::startVirtualThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startVirtualThreadMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::startVirtualThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_startVirtualThreadMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::startVirtualThread, CreateThreadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateThreadInGroup">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_GROUP_WITHIN_PATH)
    public void test_createThreadInGroupMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadGroupMain::createThreadInGroup, CreateThreadGroupMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_GROUP_WITHIN_PATH)
    public void test_createThreadInGroupMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadGroupMain::createThreadInGroup, CreateThreadGroupMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_GROUP_WITHIN_PATH)
    public void test_createThreadInGroupMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadGroupMain::createThreadInGroup, CreateThreadGroupMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_GROUP_WITHIN_PATH)
    public void test_createThreadInGroupMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadGroupMain::createThreadInGroup, CreateThreadGroupMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateThreadBuilder">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_BUILDER_WITHIN_PATH)
    public void test_createThreadBuilderMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadBuilderMain::createThreadBuilder, CreateThreadBuilderMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_BUILDER_WITHIN_PATH)
    public void test_createThreadBuilderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadBuilderMain::createThreadBuilder, CreateThreadBuilderMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_BUILDER_WITHIN_PATH)
    public void test_createThreadBuilderMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadBuilderMain::createThreadBuilder, CreateThreadBuilderMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_BUILDER_WITHIN_PATH)
    public void test_createThreadBuilderMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadBuilderMain::createThreadBuilder, CreateThreadBuilderMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_WITHIN_PATH)
    public void test_createExecutorMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorMain::createExecutor, CreateExecutorMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_WITHIN_PATH)
    public void test_createExecutorMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorMain::createExecutor, CreateExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_WITHIN_PATH)
    public void test_createExecutorMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorMain::createExecutor, CreateExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_WITHIN_PATH)
    public void test_createExecutorMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorMain::createExecutor, CreateExecutorMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateExecutorService">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_createExecutorServiceMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::createExecutorService,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_createExecutorServiceMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::createExecutorService,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_createExecutorServiceMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::createExecutorService,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_createExecutorServiceMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::createExecutorService,
                CreateExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSubmitCallable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitCallableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitCallableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitCallableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitCallableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable, CreateExecutorServiceMain.class);
    }
    // </editor-fold>

    //<editor-fold desc="accessThreadSystemViaSubmitRunnableWithResult">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableWithResultMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnableWithResult,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableWithResultMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnableWithResult,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableWithResultMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnableWithResult,
                CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableWithResultMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnableWithResult,
                CreateExecutorServiceMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessThreadSystemViaSubmitRunnable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_submitRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitRunnable, CreateExecutorServiceMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessThreadSystemViaInvokeAll">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessThreadSystemViaInvokeAny">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }
    //</editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleRunnable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleRunnable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleRunnable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleRunnable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleRunnable,
                CreateScheduledExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaForkJoinPool">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_executeRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::executeRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_executeRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::executeRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_executeRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::executeRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_executeRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::executeRunnable, CreateForkJoinPoolMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThreadPoolExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadPoolExecutorMain::executeRunnable,
                CreateThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadPoolExecutorMain::executeRunnable,
                CreateThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadPoolExecutorMain::executeRunnable,
                CreateThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadPoolExecutorMain::executeRunnable,
                CreateThreadPoolExecutorMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledThreadPoolExecutorMain::executeRunnable,
                CreateScheduledThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledThreadPoolExecutorMain::executeRunnable,
                CreateScheduledThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledThreadPoolExecutorMain::executeRunnable,
                CreateScheduledThreadPoolExecutorMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_THREAD_POOL_EXECUTOR_WITHIN_PATH)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledThreadPoolExecutorMain::executeRunnable,
                CreateScheduledThreadPoolExecutorMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCompletableFuture">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsync, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaParallelStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_collectionParallelStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::collectionParallelStream,
                CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_collectionParallelStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::collectionParallelStream,
                CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_collectionParallelStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::collectionParallelStream,
                CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_collectionParallelStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::collectionParallelStream,
                CreateParallelStreamMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStreamParallel">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_streamParallelMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::streamParallel, CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_streamParallelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::streamParallel, CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_streamParallelMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::streamParallel, CreateParallelStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = PARALLEL_STREAM_WITHIN_PATH)
    public void test_parallelStream_streamParallelMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateParallelStreamMain::streamParallel, CreateParallelStreamMain.class);
    }
    // </editor-fold>
}
