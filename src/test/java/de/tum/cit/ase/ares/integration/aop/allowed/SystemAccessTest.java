package de.tum.cit.ase.ares.integration.aop.allowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

/**
 * Base class for system access tests with common methods and constants.
 */
public abstract class SystemAccessTest {

    // Error messages for assertion failures
    protected static final String ERROR_SECURITY_EXCEPTION = "A SecurityException was thrown, but the operation should be allowed.";

    // Policy paths - Maven Archunit AspectJ
    protected static final String POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_ASPECTJ_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOneThreadAllowedCreate.yaml";

    // Policy paths - Maven Archunit Instrumentation
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOneThreadAllowedCreate.yaml";

    // Policy paths - Maven Wala AspectJ
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOneThreadAllowedCreate.yaml";

    // Policy paths - Maven Wala Instrumentation
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOneThreadAllowedCreate.yaml";

    // WithinPath constants for thread system tests
    protected static final String WITHIN_PATH_THREAD = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/thread";
    protected static final String WITHIN_PATH_THREAD_GROUP = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadGroup";
    protected static final String WITHIN_PATH_THREAD_BUILDER = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadBuilder";
    protected static final String WITHIN_PATH_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/executor";
    protected static final String WITHIN_PATH_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/executorService";
    protected static final String WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/scheduledExecutorService";
    protected static final String WITHIN_PATH_FORK_JOIN_POOL = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/forkJoinPool";
    protected static final String WITHIN_PATH_THREAD_POOL_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/threadPoolExecutor";
    protected static final String WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/scheduledThreadPoolExecutor";
    protected static final String WITHIN_PATH_COMPLETABLE_FUTURE = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/completableFuture";
    protected static final String WITHIN_PATH_PARALLEL_STREAM = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/create/parallelStream";

    /**
     * Test that the given executable does NOT throw a SecurityException.
     *
     * @param executable The executable that should NOT throw a SecurityException
     */
    protected void assertNoAresSecurityException(Executable executable) {
        Assertions.assertDoesNotThrow(executable, ERROR_SECURITY_EXCEPTION);
    }
}
