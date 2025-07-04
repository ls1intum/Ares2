package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder.ProcessBuilderExecuteMain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CommandSystemAccessTest extends SystemAccessTest {

    private static final String RUNTIME_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/runtime";
    private static final String PROCESS_BUILDER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/processBuilder";

    // <editor-fold desc="accessCommandSystemViaRuntime()">
    @Test
    void test_executeCommandViaRuntimeMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntime();
    }

    @Test
    void test_executeCommandViaRuntimeMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeMavenWalaInstrumentation");
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
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitAspectJ() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaAspectJ() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }

    @Test
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaProcessBuilderMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_executeCommandViaProcessBuilderMavenWalaInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeWithArgs()">
    @Test
    void test_executeCommandViaRuntimeWithArgsMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithArgsMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithArgsMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeWithArgsMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithArgsMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithArgsMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeWithArgsMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithArgsMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithArgsMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeWithArgsMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithArgsMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithArgsMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithArgs();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeWithFile()">
    @Test
    void test_executeCommandViaRuntimeWithFileMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithFileMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithFileMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeWithFileMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithFileMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithFileMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeWithFileMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithFileMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithFileMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeWithFileMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeWithFileMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeWithFileMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeWithFile();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeArray()">
    @Test
    void test_executeCommandViaRuntimeArrayMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArray();
    }

    @Test
    void test_executeCommandViaRuntimeArrayMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArray();
    }

    @Test
    void test_executeCommandViaRuntimeArrayMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArray();
    }

    @Test
    void test_executeCommandViaRuntimeArrayMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArray();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeArrayWithArgs()">
    @Test
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithArgsMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithArgsMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithArgsMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithArgs();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithArgsMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithArgs();
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeArrayWithFile()">
    @Test
    void test_executeCommandViaRuntimeArrayWithFileMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithFileMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithFileMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithFileMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithFileMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithFileMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithFileMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithFileMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithFileMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithFile();
    }

    @Test
    void test_executeCommandViaRuntimeArrayWithFileMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(CommandSystemAccessTest.class, "test_executeCommandViaRuntimeArrayWithFileMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_executeCommandViaRuntimeArrayWithFileMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.executeCommandViaRuntimeArrayWithFile();
    }
    // </editor-fold>
}
