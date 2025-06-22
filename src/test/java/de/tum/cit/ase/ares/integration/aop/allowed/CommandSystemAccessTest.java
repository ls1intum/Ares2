package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.processBuilder.ExecuteProcessBuilderMain;

class CommandSystemAccessTest extends SystemAccessTest {

    protected static final String WITHIN_PATH_RUNTIME = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/commandSystem/execute/runtime";
    protected static final String WITHIN_PATH_PROCESS_BUILDER = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/commandSystem/execute/processBuilder";

    // <editor-fold desc="accessCommandSystemViaRuntime">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> ExecuteRuntimeMain.executeCommandViaRuntime("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> ExecuteRuntimeMain.executeCommandViaRuntime("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> ExecuteRuntimeMain.executeCommandViaRuntime("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> ExecuteRuntimeMain.executeCommandViaRuntime("echo hello"));
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaProcessBuilder">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderMavenArchunitAspectJ() {
        assertNoAresSecurityException(() -> ExecuteProcessBuilderMain.executeCommandViaProcessBuilder("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation() {
        assertNoAresSecurityException(() -> ExecuteProcessBuilderMain.executeCommandViaProcessBuilder("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ() {
        assertNoAresSecurityException(() -> ExecuteProcessBuilderMain.executeCommandViaProcessBuilder("echo hello"));
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation() {
        assertNoAresSecurityException(() -> ExecuteProcessBuilderMain.executeCommandViaProcessBuilder("echo hello"));
    }
    // </editor-fold>
}
