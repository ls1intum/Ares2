package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.completableFuture.CreateCompletableFutureMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executorService.CreateExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.parallelStream.CreateParallelStreamMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledExecutorService.CreateScheduledExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.thread.CreateThreadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadPoolExecutor.CreateThreadPoolExecutorMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.forkJoinPool.CreateForkJoinPoolMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadFactory.CreateThreadFactoryMain;

public class ThreadSystemAccessTest extends SystemAccessTest {
    // withinPath constants for thread system tests
    private static final String WITHIN_PATH_THREAD = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/thread";
    private static final String WITHIN_PATH_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/executorService";
    private static final String WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/scheduledExecutorService";
    private static final String WITHIN_PATH_THREAD_POOL_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadPoolExecutor";
    private static final String WITHIN_PATH_COMPLETABLE_FUTURE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/completableFuture";
    private static final String WITHIN_PATH_PARALLEL_STREAM = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/parallelStream";
    private static final String WITHIN_PATH_FORK_JOIN_POOL = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/forkJoinPool";
    private static final String WITHIN_PATH_THREAD_FACTORY = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadFactory";

    // <editor-fold desc="accessThreadSystemViaStartThread">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startThreadMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::startThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startThreadMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::startThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startThreadMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::startThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startThreadMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::startThread);
    } // </editor-fold>// <editor-fold desc="accessThreadSystemViaSubmitCallable">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitCallableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitCallableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitCallableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitCallableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitCallable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSubmitRunnableWithResult">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableWithResultMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnableWithResult);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableWithResultMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnableWithResult);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableWithResultMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnableWithResult);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableWithResultMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnableWithResult);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSubmitRunnable">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_submitRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::submitRunnable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAll">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAny">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAny();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // </editor-fold>

    // </editor-fold> // <editor-fold desc="accessThreadSystemViaScheduleRunnable">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleRunnable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleCallable">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleCallableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleCallableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleCallableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleCallableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleCallable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduleAtFixedRate">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleAtFixedRateMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleAtFixedRate);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleAtFixedRateMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleAtFixedRate);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleAtFixedRateMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleAtFixedRate);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleAtFixedRateMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleAtFixedRate);
    }
    // </editor-fold>

    // </editor-fold> // <editor-fold desc="accessThreadSystemViathreadPoolExecutor_executeRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::executeRunnable);
    } 
    
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViarunAsync">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsync);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSupplyAsync">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsync);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViathenApplyAsync">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsync);
    }

    // </editor-fold> // <editor-fold desc="accessThreadSystemViaCollectionParallelStream">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_collectionParallelStreamMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateParallelStreamMain::collectionParallelStream);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_collectionParallelStreamMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateParallelStreamMain::collectionParallelStream);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_collectionParallelStreamMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateParallelStreamMain::collectionParallelStream);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_collectionParallelStreamMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateParallelStreamMain::collectionParallelStream);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStreamParallel">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_streamParallelMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateParallelStreamMain::streamParallel);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_streamParallelMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateParallelStreamMain::streamParallel);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_streamParallelMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateParallelStreamMain::streamParallel);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_PARALLEL_STREAM)
    public void test_parallelStream_streamParallelMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateParallelStreamMain::streamParallel);
    }
    // </editor-fold>

    // </editor-fold>
    // <editor-fold desc="accessThreadSystemViaRunAsyncWithExecutor">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncWithExecutorMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncWithExecutorMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncWithExecutorMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_runAsyncWithExecutorMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::runAsyncWithExecutor);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSupplyAsyncWithExecutor">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncWithExecutorMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncWithExecutorMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncWithExecutorMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_supplyAsyncWithExecutorMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::supplyAsyncWithExecutor);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenApplyAsyncWithExecutor">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenApplyAsyncWithExecutorMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenApplyAsyncWithExecutor);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombine">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombine);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombineAsync">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsync);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsync);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaThenCombineAsyncWithExecutor">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsyncWithExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_COMPLETABLE_FUTURE)
    public void test_completableFuture_thenCombineAsyncWithExecutorMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateCompletableFutureMain::thenCombineAsyncWithExecutor);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAllWithTimeout">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllWithTimeoutMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllWithTimeoutMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllWithTimeoutMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAllWithTimeoutMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAllWithTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaInvokeAnyWithTimeout">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyWithTimeoutMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyWithTimeoutMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyWithTimeoutMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_invokeAnyWithTimeoutMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> {
            try {
                CreateExecutorServiceMain.invokeAnyWithTimeout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolExecute">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolExecuteMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolExecute);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolExecuteMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolExecute);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolExecuteMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolExecute);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolExecuteMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolExecute);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolSubmitCallableTask">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitCallableTask);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitCallableTask);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitCallableTask);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitCallableTaskMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitCallableTask);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCommonPoolSubmitRunnable">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_commonPoolSubmitRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::commonPoolSubmitRunnable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaNotifyThread">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_notifyThreadMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::notifyThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_notifyThreadMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::notifyThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_notifyThreadMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::notifyThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_notifyThreadMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::notifyThread);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaNewThread">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_FACTORY)
    public void test_threadFactory_newThreadMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadFactoryMain::newThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_FACTORY)
    public void test_threadFactory_newThreadMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadFactoryMain::newThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_FACTORY)
    public void test_threadFactory_newThreadMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadFactoryMain::newThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_FACTORY)
    public void test_threadFactory_newThreadMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadFactoryMain::newThread);
    }
    // </editor-fold>
}
