package de.tum.cit.ase.ares.api.architecture.java;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

/**
 * Enum representing the architecture modes for Java security test cases.
 *
 * <p>Description: Provides different modes for architecture test case generation and execution in Java.
 * The modes determine how files and settings are copied and resolved based on the underlying architecture analysis tool.</p>
 *
 * <p>Design Rationale: Using an enum to represent architecture modes centralises configuration and enables future extensions
 * (e.g. supporting WALA) while ensuring that file handling and resource resolution are consistently applied.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum JavaArchitectureMode {

    /**
     * ArchUnit mode for analysing Java code with TNGs ArchUnit.
     */
    ARCHUNIT,

    /**
     * WALA mode for analysing Java code with IBMs WALA.
     */
    WALA;

    //<editor-fold desc="Multi-file methods">

    /**
     * Retrieves the list of resource file paths to copy for the current architecture mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of paths representing the resource files to copy.
     */
    @Nonnull
    public List<Path> filesToCopy() {
        return (switch (this) {
            case ARCHUNIT -> Stream.of(
                    new String[]{"templates", "architecture", "java", "archunit", "methods", "file-system-access-methods.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "methods", "jvm-termination-methods.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "methods", "network-access-methods.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "methods", "reflection-methods.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "CustomClassResolver.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "JavaArchitectureTestCaseCollection.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "TransitivelyAccessesMethodsCondition.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "rules", "COMMAND_EXECUTION.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "rules", "FILESYSTEM_INTERACTION.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "rules", "NETWORK_CONNECTION.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "rules", "PACKAGE_IMPORT.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "rules", "THREAD_CREATION.txt"},
                    new String[]{"templates", "architecture", "java", "archunit", "archunit.properties"},
                    new String[]{"templates", "architecture", "java", "archunit", "FileHandlerConstants.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchUnitTestCaseSupported.java"}
            );
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        }).map(FileTools::resolveOnResources).toList();
    }

    /**
     * Retrieves the file value arrays based on the provided package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param packageName the base package name.
     * @return a list of string arrays representing file values.
     */
    @Nonnull
    public List<String[]> fileValues(@Nonnull String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.of(
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    FileTools.generatePackageNameArray(packageName, 1),
                    FileTools.generatePackageNameArray(packageName, 6),
                    FileTools.generatePackageNameArray(packageName, 1),
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    new String[]{},
                    FileTools.generatePackageNameArray(packageName, 1),
                    FileTools.generatePackageNameArray(packageName, 1)
            );
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        }).toList();
    }

    /**
     * Determines the target paths for copying resource files based on the project path and package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return a list of paths representing the target locations.
     */
    @Nonnull
    public List<Path> targetsToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.of(
                    new String[]{"api", "architecture", "java", "archunit", "methods", "file-system-access-methods.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "methods", "jvm-termination-methods.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "methods", "network-access-methods.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "methods", "reflection-methods.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "postcompile", "CustomClassResolver.java"},
                    new String[]{"api", "architecture", "java", "archunit", "postcompile", "JavaArchitectureTestCaseCollection.java"},
                    new String[]{"api", "architecture", "java", "archunit", "postcompile", "TransitivelyAccessesMethodsCondition.java"},
                    new String[]{"api", "architecture", "java", "archunit", "rules", "COMMAND_EXECUTION.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "rules", "FILESYSTEM_INTERACTION.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "rules", "NETWORK_CONNECTION.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "rules", "PACKAGE_IMPORT.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "rules", "THREAD_CREATION.txt"},
                    new String[]{"api", "architecture", "java", "archunit", "archunit.properties"},
                    new String[]{"api", "architecture", "java", "archunit", "FileHandlerConstants.java"},
                    new String[]{"api", "architecture", "java", "archunit", "JavaArchUnitTestCaseSupported.java"}
            );
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }
    //</editor-fold>

    //<editor-fold desc="Single-file methods">

    /**
     * Retrieves the path to the file header template for the three-parted file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the file header template.
     */
    @Nonnull
    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionHeader.txt"};
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
    }

    /**
     * Generates the body content for the three-parted file by concatenating the architecture test case definitions.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param testCases the list of test cases.
     * @return a string representing the body content.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT ->
                    String.join("\n", ((List<JavaArchUnitSecurityTestCase>) testCases).stream().map(JavaArchUnitSecurityTestCase::writeArchitectureTestCase).toList());
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        };
    }

    /**
     * Retrieves the path to the file footer template for the three-parted file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the file footer template.
     */
    @Nonnull
    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionFooter.txt"};
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
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
    public String[] fileValue(@Nonnull String packageName) {
        return switch (this) {
            case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 2);
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        };
    }

    /**
     * Determines the target path for copying the main architecture test case file based on the project path and package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return the target path for the main file.
     */
    @Nonnull
    public Path targetToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ARCHUNIT -> new String[]{"api", "architecture", "java", "archunit", "JavaArchUnitTestCaseCollection.txt"};
            // Todo: Add WALA and remove default
            default -> throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
    }
    //</editor-fold>

    //<editor-fold desc="Reset methods">
    // (No reset methods defined for JavaArchitectureMode)
    //</editor-fold>

    //<editor-fold desc="Other methods">
    /**
     * Imports and analyzes Java classes from the specified class path using ArchUnit's ClassFileImporter.
     * This method enables static code analysis by creating a collection of Java class metadata.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param classPath the file system path containing Java classes to import
     * @return a JavaClasses object containing all imported classes for analysis
     */
    @Nonnull
    public JavaClasses getJavaClasses(String classPath) {
        return new ClassFileImporter().importPath(classPath);
    }

    /**
     * Builds a call graph based on the current architecture mode. Returns null for ARCHUNIT mode
     * as it employs non-call-graph analysis, while in WALA mode it constructs a proper call graph
     * representing method caller-callee relationships.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param classPath the file system path containing Java classes to analyze
     * @return a CallGraph object for WALA mode, or null for ARCHUNIT mode
     */
    @Nullable
    public CallGraph getCallGraph(String classPath) {
        return switch (this) {
            case ARCHUNIT -> null;
            case WALA -> new CustomCallgraphBuilder().buildCallGraph(classPath);
        };
    }
    //</editor-fold>
}