package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.desktop.ExecuteDesktopMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.nativeCommand.ExecuteNativeCommandMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.processBuilder.ExecuteProcessBuilderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.reflectionLoader.ExecuteReflectionLoaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.scriptEngine.ExecuteScriptEngineMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.serviceLoader.ExecuteServiceLoaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.urlClassLoader.ExecuteURLClassLoaderMain;

public class FileSystemAccessExecuteTest extends SystemAccessTest {

    private static final String RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/runtime";
    private static final String DESKTOP_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/desktop";
    private static final String PROCESS_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/processBuilder";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/thirdPartyPackage";
    private static final String NATIVE_COMMAND_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/nativeCommand";
    private static final String SCRIPT_ENGINE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/scriptEngine";
    private static final String URL_CLASS_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/urlClassLoader";
    private static final String REFLECTION_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/reflectionLoader";
    private static final String SERVICE_LOADER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/serviceLoader";

    // <editor-fold desc="accessFileSystemViaRuntime">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntime);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntime);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktop">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktop);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktop);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktop);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktop);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRuntimeArray">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntimeArray);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = RUNTIME_WITHIN_PATH)
    void test_accessFileSystemViaRuntimeArrayMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteRuntimeMain::accessFileSystemViaRuntimeArray);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowse">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowse);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowse);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowse);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowse);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopBrowseFileDirectory">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowseFileDirectory);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowseFileDirectory);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowseFileDirectory);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopBrowseFileDirectoryMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopBrowseFileDirectory);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopEdit">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopEdit);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopEdit);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopEdit);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopEditMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopEdit);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDesktopPrint">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopPrint);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopPrint);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopPrint);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = DESKTOP_WITHIN_PATH)
    void test_accessFileSystemViaDesktopPrintMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteDesktopMain::accessFileSystemViaDesktopPrint);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaProcessBuilder">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = PROCESS_BUILDER_WITHIN_PATH)
    void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaNativeCommand">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = NATIVE_COMMAND_WITHIN_PATH)
    void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = NATIVE_COMMAND_WITHIN_PATH)
    void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = NATIVE_COMMAND_WITHIN_PATH)
    void test_accessFileSystemViaNativeCommandMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = NATIVE_COMMAND_WITHIN_PATH)
    void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScriptEngine">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
    void test_accessFileSystemViaScriptEngineMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteScriptEngineMain::accessFileSystemViaScriptEngine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
    void test_accessFileSystemViaScriptEngineMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteScriptEngineMain::accessFileSystemViaScriptEngine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
    void test_accessFileSystemViaScriptEngineMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteScriptEngineMain::accessFileSystemViaScriptEngine);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = SCRIPT_ENGINE_WITHIN_PATH)
    void test_accessFileSystemViaScriptEngineMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteScriptEngineMain::accessFileSystemViaScriptEngine);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaURLClassLoader">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaURLClassLoaderMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteURLClassLoaderMain::accessFileSystemViaURLClassLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaURLClassLoaderMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteURLClassLoaderMain::accessFileSystemViaURLClassLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaURLClassLoaderMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteURLClassLoaderMain::accessFileSystemViaURLClassLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = URL_CLASS_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaURLClassLoaderMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteURLClassLoaderMain::accessFileSystemViaURLClassLoader);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaReflectionLoader">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = REFLECTION_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaReflectionLoaderMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteReflectionLoaderMain::accessFileSystemViaReflectionLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = REFLECTION_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaReflectionLoaderMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteReflectionLoaderMain::accessFileSystemViaReflectionLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = REFLECTION_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaReflectionLoaderMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteReflectionLoaderMain::accessFileSystemViaReflectionLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = REFLECTION_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaReflectionLoaderMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteReflectionLoaderMain::accessFileSystemViaReflectionLoader);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaServiceLoader">
    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_ASPECTJ_COMMAND, withinPath = SERVICE_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaServiceLoaderMavenArchunitAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteServiceLoaderMain::accessFileSystemViaServiceLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_ARCHUNIT_INSTRUMENTATION_COMMAND, withinPath = SERVICE_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaServiceLoaderMavenArchunitInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteServiceLoaderMain::accessFileSystemViaServiceLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_ASPECTJ_COMMAND, withinPath = SERVICE_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaServiceLoaderMavenWalaAspectJ() throws Exception {
        assertNoAresSecurityException(ExecuteServiceLoaderMain::accessFileSystemViaServiceLoader);
    }

    @PublicTest
    @Policy(value = POLICY_MAVEN_WALA_INSTRUMENTATION_COMMAND, withinPath = SERVICE_LOADER_WITHIN_PATH)
    void test_accessFileSystemViaServiceLoaderMavenWalaInstrumentation() throws Exception {
        assertNoAresSecurityException(ExecuteServiceLoaderMain::accessFileSystemViaServiceLoader);
    }
    // </editor-fold>
}
