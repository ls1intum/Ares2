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
    protected static final String POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMPLETABLE_FUTURE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyCompletableFutureAllowed.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_ASPECTJ_ALL_THREAD_OPERATIONS = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyAllThreadOperationsAllowed.yaml";

    // Policy paths - Maven Archunit Instrumentation
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOneThreadAllowedCreate.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMPLETABLE_FUTURE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyCompletableFutureAllowed.yaml";
    protected static final String POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_ALL_THREAD_OPERATIONS = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyAllThreadOperationsAllowed.yaml";

    // Policy paths - Maven Wala AspectJ
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOneThreadAllowedCreate.yaml";
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_COMPLETABLE_FUTURE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyCompletableFutureAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_ASPECTJ_ALL_THREAD_OPERATIONS = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyAllThreadOperationsAllowed.yaml";

    // Policy paths - Maven Wala Instrumentation
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOneCommandExecutionAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_THREAD = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOneThreadAllowedCreate.yaml";
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_COMPLETABLE_FUTURE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyCompletableFutureAllowed.yaml";
    protected static final String POLICY_MAVEN_WALA_INSTRUMENTATION_ALL_THREAD_OPERATIONS = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyAllThreadOperationsAllowed.yaml";

    // WithinPath constants for system access tests - use specific paths for thread operations
    protected static final String WITHIN_PATH_ALLOWED = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject";
    protected static final String WITHIN_PATH_THREAD = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_THREAD_GROUP = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_THREAD_BUILDER = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_EXECUTOR = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_EXECUTOR_SERVICE = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_SCHEDULED_EXECUTOR_SERVICE = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_FORK_JOIN_POOL = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_THREAD_POOL_EXECUTOR = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_SCHEDULED_THREAD_POOL_EXECUTOR = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_COMPLETABLE_FUTURE = WITHIN_PATH_ALLOWED;
    protected static final String WITHIN_PATH_PARALLEL_STREAM = WITHIN_PATH_ALLOWED;

    /**
     * Test that the given executable does NOT throw a SecurityException.
     *
     * @param executable The executable that should NOT throw a SecurityException
     */
    protected void assertNoAresSecurityException(Executable executable) {
        Assertions.assertDoesNotThrow(executable, ERROR_SECURITY_EXCEPTION);
    }
}
