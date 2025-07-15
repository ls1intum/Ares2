package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop.DesktopExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.processBuilder.ProcessBuilderExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

class FileSystemAccessExecuteTest extends SystemAccessTest {

    private static final String RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime";
    private static final String PROCESS_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder";
    private static final String DESKTOP_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/desktop";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaRuntime">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntime,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntime,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntime,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntime,
                RuntimeExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRuntimeArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntimeArray,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntimeArray,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntimeArray,
                RuntimeExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(RuntimeExecuteMain::accessFileSystemViaRuntimeArray,
                RuntimeExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktop">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktop,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktop,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktop,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktop,
                DesktopExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowse">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowse,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowse,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowse,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowse,
                DesktopExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowseFileDirectory">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowseFileDirectory,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowseFileDirectory,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowseFileDirectory,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopBrowseFileDirectory,
                DesktopExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopEdit">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopEdit,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopEdit,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopEdit,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopEdit,
                DesktopExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopPrint">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopPrint,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopPrint,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopPrint,
                DesktopExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DesktopExecuteMain::accessFileSystemViaDesktopPrint,
                DesktopExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaProcessBuilder">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(ProcessBuilderExecuteMain::accessFileSystemViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(ProcessBuilderExecuteMain::accessFileSystemViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(ProcessBuilderExecuteMain::accessFileSystemViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(ProcessBuilderExecuteMain::accessFileSystemViaProcessBuilder,
                ProcessBuilderExecuteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_EXECUTE_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>
}
