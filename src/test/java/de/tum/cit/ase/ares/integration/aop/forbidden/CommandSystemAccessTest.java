package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

// Imports for command execution operations
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder.ProcessBuilderExecuteMain;

/**
 * Integration tests to verify that command system execution is forbidden
 * when trying to execute commands that are not allowed by the security policy,
 * even when some commands are permitted.
 */
class CommandSystemAccessTest extends SystemAccessTest {

    private static final String RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/runtime";
    private static final String PROCESS_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/processBuilder";

    // <editor-fold desc="accessCommandSystemViaRuntime">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_executeCommandViaRuntimeMavenArchunitAspectJ() {
        assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation() {
        assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_executeCommandViaRuntimeMavenWalaAspectJ() {
        assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_executeCommandViaRuntimeMavenWalaInstrumentation() {
        assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaProcessBuilder">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitAspectJ() {
        assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ() {
        assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation() {
        assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }
    // </editor-fold>
}
