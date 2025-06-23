package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.completableFuture.CreateCompletableFutureMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executor.CreateExecutorMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executorService.CreateExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.forkJoinPool.CreateForkJoinPoolMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.parallelStream.CreateParallelStreamMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledExecutorService.CreateScheduledExecutorServiceMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledThreadPoolExecutor.CreateScheduledThreadPoolExecutorMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.thread.CreateThreadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadBuilder.CreateThreadBuilderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadGroup.CreateThreadGroupMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadPoolExecutor.CreateThreadPoolExecutorMain;

public class ThreadSystemAccessTest extends SystemAccessTest {
    // withinPath constants for thread system tests
    private static final String WITHIN_PATH_THREAD = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/thread";
    private static final String WITHIN_PATH_THREAD_GROUP = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadGroup";
    private static final String WITHIN_PATH_THREAD_BUILDER = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadBuilder";
    private static final String WITHIN_PATH_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/executor";
    private static final String WITHIN_PATH_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/executorService";
    private static final String WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/scheduledExecutorService";
    private static final String WITHIN_PATH_THREAD_POOL_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadPoolExecutor";
    private static final String WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/scheduledThreadPoolExecutor";
    private static final String WITHIN_PATH_FORK_JOIN_POOL = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/forkJoinPool";
    private static final String WITHIN_PATH_COMPLETABLE_FUTURE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/completableFuture";
    private static final String WITHIN_PATH_PARALLEL_STREAM = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/parallelStream";

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
    } // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaStartVirtualThread">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startVirtualThreadMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::startVirtualThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startVirtualThreadMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::startVirtualThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startVirtualThreadMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadMain::startVirtualThread);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD)
    public void test_startVirtualThreadMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadMain::startVirtualThread);
    } // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaCreateThreadInGroup">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_GROUP)
    public void test_createThreadInGroupMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadGroupMain::createThreadInGroup);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_GROUP)
    public void test_createThreadInGroupMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadGroupMain::createThreadInGroup);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_GROUP)
    public void test_createThreadInGroupMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadGroupMain::createThreadInGroup);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_GROUP)
    public void test_createThreadInGroupMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadGroupMain::createThreadInGroup);
    }

    // </editor-fold>
    
    // <editor-fold desc="accessThreadSystemViaCreateThreadBuilder">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_BUILDER)
    public void test_createThreadBuilderMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadBuilderMain::createThreadBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_BUILDER)
    public void test_createThreadBuilderMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadBuilderMain::createThreadBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_BUILDER)
    public void test_createThreadBuilderMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadBuilderMain::createThreadBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_BUILDER)
    public void test_createThreadBuilderMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadBuilderMain::createThreadBuilder);
    }

    // </editor-fold> 

    // <editor-fold desc="accessThreadSystemViaCreateExecutor">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR)
    public void test_createExecutorMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateExecutorMain::createExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR)
    public void test_createExecutorMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateExecutorMain::createExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR)
    public void test_createExecutorMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateExecutorMain::createExecutor);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR)
    public void test_createExecutorMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateExecutorMain::createExecutor);
    }

    // </editor-fold>

    // </editor-fold> // <editor-fold desc="accessThreadSystemViaCreateExecutorService">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_createExecutorServiceMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::createExecutorService);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_createExecutorServiceMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::createExecutorService);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_createExecutorServiceMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateExecutorServiceMain::createExecutorService);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_EXECUTOR_SERVICE)
    public void test_createExecutorServiceMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateExecutorServiceMain::createExecutorService);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaSubmitCallable">
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

    // <editor-fold desc="accessThreadSystemViaScheduleWithFixedDelay">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleWithFixedDelayMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleWithFixedDelay);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleWithFixedDelayMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleWithFixedDelay);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleWithFixedDelayMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleWithFixedDelay);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE)
    public void test_scheduleWithFixedDelayMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledExecutorServiceMain::scheduleWithFixedDelay);
    }

    // </editor-fold>

    // </editor-fold> // <editor-fold desc="accessThreadSystemViaforkJoinPool_executeRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_executeRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_executeRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_executeRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_executeRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::executeRunnable);
    } 
    // </editor-fold> 
    
    // <editor-fold desc="accessThreadSystemViaforkJoinPool_submitRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitRunnable);
    }

    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaforkJoinPool_submitCallable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitCallableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitCallableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitCallableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_FORK_JOIN_POOL)
    public void test_forkJoinPool_submitCallableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateForkJoinPoolMain::submitCallable);
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
    
    // <editor-fold desc="accessThreadSystemViathreadPoolExecutor_submitRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitRunnable);
    } 
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViathreadPoolExecutor_submitCallable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitCallableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitCallableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitCallableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_THREAD_POOL_EXECUTOR)
    public void test_threadPoolExecutor_submitCallableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateThreadPoolExecutorMain::submitCallable);
    }
    // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutor_executeRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::executeRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_executeRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::executeRunnable);
    } // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutor_submitRunnable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitRunnable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitRunnableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitRunnable);
    } // </editor-fold>

    // <editor-fold desc="accessThreadSystemViaScheduledThreadPoolExecutor_submitCallable">

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenArchunitInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenWalaAspectJ() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitCallable);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD, withinPath = WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR)
    public void test_scheduledThreadPoolExecutor_submitCallableMavenWalaInstrumentation() {
        assertNoAresSecurityException(CreateScheduledThreadPoolExecutorMain::submitCallable);
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
}
