package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop.DesktopExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.processBuilder.ProcessBuilderExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(ExecutionMode.SAME_THREAD)
class FileSystemAccessExecuteTest extends SystemAccessTest {

    private static final String RUNTIME_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime";
    private static final String PROCESS_BUILDER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder";
    private static final String DESKTOP_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/desktop";
    private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaRuntime">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntime();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntime();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntime();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntime();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRuntimeArray">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntimeArray();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntimeArray();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntimeArray();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = RUNTIME_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation() throws Exception {
        RuntimeExecuteMain.accessFileSystemViaRuntimeArray();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktop">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktop();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktop();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopMavenWalaAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktop();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopMavenWalaInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktop();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowse">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowse();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowse();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowse();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowse();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowseFileDirectory">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowseFileDirectory();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowseFileDirectory();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowseFileDirectory();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopBrowseFileDirectory();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopEdit">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopEdit();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopEdit();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopEdit();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopEdit();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopPrint">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopPrint();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopPrint();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaAspectJ() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopPrint();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = DESKTOP_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaInstrumentation() throws Exception {
        DesktopExecuteMain.accessFileSystemViaDesktopPrint();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaProcessBuilder">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() throws Exception {
        ProcessBuilderExecuteMain.accessFileSystemViaProcessBuilder();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.accessFileSystemViaProcessBuilder();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() throws Exception {
        ProcessBuilderExecuteMain.accessFileSystemViaProcessBuilder();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = PROCESS_BUILDER_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() throws Exception {
        ProcessBuilderExecuteMain.accessFileSystemViaProcessBuilder();
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() throws Exception {
        ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage();
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() throws Exception {
        ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() throws Exception {
        ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage();
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() throws Exception {
        ExecuteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage();
    }
    // </editor-fold>

    // <editor-fold desc="Test methods for disabled methods">
    // <editor-fold desc="accessFileSystemViaRuntime">
    @Test
    void test_accessFileSystemViaRuntimeMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaRuntimeMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaRuntimeMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRuntimeArray">
    @Test
    void test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktop">
    @Test
    void test_accessFileSystemViaDesktopMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaDesktopMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowse">
    @Test
    void test_accessFileSystemViaDesktopBrowseMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowseFileDirectory">
    @Test
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopEdit">
    @Test
    void test_accessFileSystemViaDesktopEditMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopEditMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopEditMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopEditMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaDesktopEditMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopEditMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopEditMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopEditMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopPrint">
    @Test
    void test_accessFileSystemViaDesktopPrintMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopPrintMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopPrintMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopPrintMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaDesktopPrintMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopPrintMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaDesktopPrintMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaDesktopPrintMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaProcessBuilder">
    @Test
    void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaProcessBuilderMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessExecuteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }
    // </editor-fold>
    // </editor-fold>
}
