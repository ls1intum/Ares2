package de.tum.cit.ase.ares.api.aop;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

/**
 * Enum representing the AOP modes for Java security test case configuration.
 *
 * <p>Description: Defines the different modes of aspect-oriented programming available for Java.
 * The modes determine whether Java Instrumentation or AspectJ is used for weaving aspects into the code.</p>
 *
 * <p>Design Rationale: By abstracting AOP mode selection into an enum, the implementation details are hidden,
 * enabling flexible integration of either instrumentation-based or AspectJ-based approaches with a consistent interface.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum AOPMode {

    /**
     * Instrumentation mode using Java Instrumentation for load-time weaving.
     */
    INSTRUMENTATION,

    /**
     * AspectJ mode using AspectJ for compile-time weaving.
     */
    ASPECTJ;

    //<editor-fold desc="Load configuration">
    public List<List<String>> getCopyConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadCopyData(this);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<String>> getEditConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadEditData(this);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Multi-file methods">

    /**
     * Retrieves the list of resource file paths to copy for the selected AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of paths representing the resource files to copy.
     */
    @Nonnull
    public List<Path> filesToCopy() {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();
    }

    /**
     * Retrieves the file value arrays based on the provided package name and main class name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param packageName the base package name.
     * @param mainClassInPackageName the name of the main class within the package.
     * @return a list of string arrays representing the file values.
     */
    @Nonnull
    public List<String[]> formatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(1))
                .map(Integer::parseInt)
                .map(entry -> switch (entry){
                    case 0 -> new String[]{packageName, packageName, mainClassInPackageName};
                    default -> FileTools.generatePackageNameArray(packageName, entry);
                })
                .toList();
    }

    /**
     * Determines the target paths where resource files should be copied for the selected AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return a list of paths representing the target locations.
     */
    @Nonnull
    public List<Path> targetsToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Single-file methods">

    /**
     * Retrieves the path to the header template for the three-parted security test case file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the header template.
     */
    @Nonnull
    public Path threePartedFileHeader() {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }

    /**
     * Generates the body content for the three-parted security test case file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param aopMode the AOP mode identifier.
     * @param restrictedPackage the package being restricted.
     * @param allowedListedClasses the list of allowed classes.
     * @param javaAOPTestCases the list of security test cases.
     * @return a string representing the body content.
     */
    @Nonnull
    public String threePartedFileBody(
            @Nonnull String aopMode,
            @Nonnull String restrictedPackage,
            @Nonnull List<String> allowedListedClasses,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        List<FilePermission> filePermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);
        List<NetworkPermission> networkPermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.NETWORK_CONNECTION);
        List<CommandPermission> commandPermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.COMMAND_EXECUTION);
        List<ThreadPermission> threadPermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.THREAD_CREATION);

        return JavaAOPTestCase.writeAOPTestCaseFile(
                aopMode,
                restrictedPackage,
                allowedListedClasses,
                filePermissions,
                networkPermissions,
                commandPermissions,
                threadPermissions
        );
    }

    /**
     * Extracts and flattens the permission lists for the given supported type.
     *
     * @param <T> the type of permission.
     * @param testCases the list of JavaAOPTestCase.
     * @param supported the JavaAOPTestCaseSupported filter.
     * @return a flattened list of permissions of type T.
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> extractPermissions(List<JavaAOPTestCase> testCases, JavaAOPTestCaseSupported supported) {
        return testCases.stream()
                .filter(testCase -> testCase.getAopTestCaseSupported() == supported)
                .map(JavaAOPTestCase::getResourceAccessSupplier)
                .map(Supplier::get)
                .map(permissions -> (List<T>) permissions)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Retrieves the path to the footer template for the three-parted security test case file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the footer template.
     */
    @Nonnull
    public Path threePartedFileFooter() {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.get(1).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }

    /**
     * Generates the file value array based on the provided package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param packageName the base package name.
     * @return an array of strings representing the file value.
     */
    @Nonnull
    public String[] formatValues(@Nonnull String packageName) {
        return FileTools.generatePackageNameArray(packageName, 1);
    }

    /**
     * Determines the target path for the main AOP configuration file based on the project path and package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return the target path for the AOP configuration file.
     */
    @Nonnull
    public Path targetToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }
    //</editor-fold>

    //<editor-fold desc="Reset methods">

    /**
     * Resets the AOP test case settings by invoking the reset method using reflection.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void reset() {
        try {
            Class<?> settingsBootstrapClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
            Method bootstrapMethod = settingsBootstrapClass.getDeclaredMethod("reset");
            bootstrapMethod.setAccessible(true);
            bootstrapMethod.invoke(null);
            bootstrapMethod.setAccessible(false);

            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> settingsClassloaderClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, customClassLoader);
            Method classloaderMethod = settingsClassloaderClass.getDeclaredMethod("reset");
            classloaderMethod.setAccessible(true);
            classloaderMethod.invoke(null);
            classloaderMethod.setAccessible(false);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.creation.reset.class.not.found.exception"), e);
        } catch (NoSuchMethodException e) {
            throw new SecurityException(localize("security.creation.reset.no.method.exception"), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.creation.reset.illegal.access.exception"), e);
        } catch (InvocationTargetException e) {
            throw new SecurityException(localize("security.creation.reset.invocation.target.exception"), e);
        }
    }
    //</editor-fold>
}