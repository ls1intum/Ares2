package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder.ProcessBuilderExecuteMain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CommandSystemAccessTest extends SystemAccessTest {

    private static final String RUNTIME_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/runtime";
    private static final String PROCESS_BUILDER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/processBuilder";

    // <editor-fold desc="accessCommandSystemViaRuntime()">
    @Test
    void test_executeCommandViaRuntimeMavenArchunitAspectJ_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenWalaAspectJ_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenWalaInstrumentation_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaProcessBuilder()">
    @Test
    void test_executeCommandViaProcessBuilderMavenArchunitAspectJ_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitAspectJ() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation_test() {
        testtest(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }
    // </editor-fold>
}
