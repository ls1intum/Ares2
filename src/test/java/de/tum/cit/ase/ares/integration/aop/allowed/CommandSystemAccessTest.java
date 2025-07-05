package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.processBuilder.ExecuteProcessBuilderMain;

class CommandSystemAccessTest extends SystemAccessTest {

    protected static final String WITHIN_PATH_RUNTIME = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/commandSystem/execute/runtime";
    protected static final String WITHIN_PATH_PROCESS_BUILDER = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/commandSystem/execute/processBuilder";

    // <editor-fold desc="accessCommandSystemViaRuntimeWithParam">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithParamMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithParamMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithParamMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithParamMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaProcessBuilderWithParam">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderWithParamMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderWithParamMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderWithParamMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderWithParamMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeNoParams">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeNoParamsMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeNoParamsMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeNoParamsMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeNoParamsMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntime);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeWithArgs">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithArgsMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithArgsMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithArgsMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeWithArgsMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeWithArgs);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeArray">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArray);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaRuntimeArrayWithArgs">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArrayWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArrayWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArrayWithArgs);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_RUNTIME)
    void test_executeCommandViaRuntimeArrayWithArgsMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteRuntimeMain::executeCommandViaRuntimeArrayWithArgs);
    }
    // </editor-fold>

    // <editor-fold desc="accessCommandSystemViaProcessBuilderNoParams">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderNoParamsMavenArchunitAspectJ() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderNoParamsMavenArchunitInstrumentation() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderNoParamsMavenWalaAspectJ() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = WITHIN_PATH_PROCESS_BUILDER)
    void test_executeCommandViaProcessBuilderNoParamsMavenWalaInstrumentation() {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::executeCommandViaProcessBuilder);
    }
    // </editor-fold>
}
