package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService.CreateExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread.CreateThreadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledExecutorService.CreateScheduledExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadPoolExecutor.CreateThreadPoolExecutorMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.completableFuture.CreateCompletableFutureMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.parallelStream.CreateParallelStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.forkJoinPool.CreateForkJoinPoolMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadFactory.CreateThreadFactoryMain;

class ThreadSystemAccessTest extends SystemAccessTest {

    private static final String THREAD_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/thread";
    private static final String EXECUTOR_SERVICE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorService";
    private static final String SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/scheduledExecutorService";
    private static final String THREAD_POOL_EXECUTOR_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadPoolExecutor";
    private static final String COMPLETABLE_FUTURE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/completableFuture";
    private static final String PARALLEL_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/parallelStream";
    private static final String FORK_JOIN_POOL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/forkJoinPool";
    private static final String THREAD_FACTORY_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/threadFactory";

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
        assertAresRuntimeExceptionThread(() -> {
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
        assertAresRuntimeExceptionThread(() -> {
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
        assertAresRuntimeExceptionThread(() -> {
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
        assertAresRuntimeExceptionThread(() -> {
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
    // <editor-fold desc="accessThreadSystemViaNotifyThread">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_notifyThreadMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::notifyThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_notifyThreadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::notifyThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_notifyThreadMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadMain::notifyThread, CreateThreadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_WITHIN_PATH)
    public void test_notifyThreadMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadMain::notifyThread, CreateThreadMain.class);
    }
    // </editor-fold>
    // <editor-fold desc="accessThreadSystemViaExecuteRunnable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_executeRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::executeRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_executeRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::executeRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_executeRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::executeRunnable, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_executeRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateExecutorServiceMain::executeRunnable, CreateExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAllWithTimeout">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllWithTimeoutMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllWithTimeoutMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllWithTimeoutMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAllWithTimeoutMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAnyWithTimeout">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyWithTimeoutMavenArchunitAspectJ() {
        assertAresRuntimeExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyWithTimeoutMavenArchunitInstrumentation() {
        assertAresRuntimeExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyWithTimeoutMavenWalaAspectJ() {
        assertAresRuntimeExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_invokeAnyWithTimeoutMavenWalaInstrumentation() {
        assertAresRuntimeExceptionThread(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CreateExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleCallable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleCallableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleCallable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleCallableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleCallable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleCallableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleCallable,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleCallableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleCallable,
                CreateScheduledExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleAtFixedRate">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleAtFixedRateMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleAtFixedRate,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleAtFixedRateMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleAtFixedRate,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleAtFixedRateMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleAtFixedRate,
                CreateScheduledExecutorServiceMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = SCHEDULED_EXECUTOR_SERVICE_WITHIN_PATH)
    public void test_scheduleAtFixedRateMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateScheduledExecutorServiceMain::scheduleAtFixedRate,
                CreateScheduledExecutorServiceMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaRunAsyncWithExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncWithExecutorMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncWithExecutorMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncWithExecutorMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_runAsyncWithExecutorMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::runAsyncWithExecutor, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSupplyAsync">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsync, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSupplyAsyncWithExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncWithExecutorMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncWithExecutorMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncWithExecutorMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_supplyAsyncWithExecutorMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::supplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenApplyAsync">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsync, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenApplyAsyncWithExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenApplyAsyncWithExecutor, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombine">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombine, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombine, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombine, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombine, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombineAsync">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsync, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsync, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombineAsyncWithExecutor">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsyncWithExecutor, CreateCompletableFutureMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = COMPLETABLE_FUTURE_WITHIN_PATH)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateCompletableFutureMain::thenCombineAsyncWithExecutor, CreateCompletableFutureMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolExecute">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolExecuteMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolExecute, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolExecuteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolExecute, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolExecuteMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolExecute, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolExecuteMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolExecute, CreateForkJoinPoolMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolSubmitCallableTask">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitCallableTask, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitCallableTask, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitCallableTask, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitCallableTask, CreateForkJoinPoolMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolSubmitRunnable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitRunnable, CreateForkJoinPoolMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = FORK_JOIN_POOL_WITHIN_PATH)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateForkJoinPoolMain::commonPoolSubmitRunnable, CreateForkJoinPoolMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaNewThread">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_FACTORY_WITHIN_PATH)
    public void test_threadFactory_newThreadMavenArchunitAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadFactoryMain::newThread, CreateThreadFactoryMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_FACTORY_WITHIN_PATH)
    public void test_threadFactory_newThreadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadFactoryMain::newThread, CreateThreadFactoryMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_FACTORY_WITHIN_PATH)
    public void test_threadFactory_newThreadMavenWalaAspectJ() {
        assertAresSecurityExceptionThread(CreateThreadFactoryMain::newThread, CreateThreadFactoryMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_FACTORY_WITHIN_PATH)
    public void test_threadFactory_newThreadMavenWalaInstrumentation() {
        assertAresSecurityExceptionThread(CreateThreadFactoryMain::newThread, CreateThreadFactoryMain.class);
    }
    // </editor-fold>
}
