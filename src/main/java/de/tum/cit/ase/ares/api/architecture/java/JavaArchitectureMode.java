package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;

// TODO: Add documentation
public enum JavaArchitectureMode {
    /**
     * The ArchUnit architecture mode.
     */
    ARCHUNIT,
    /**
     * The WALA architecture mode.
     */
    WALA;

    //<editor-fold desc="Multi-file methods">
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
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        }).toList();
    }

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
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }
    //</editor-fold>

    //<editor-fold desc="Single-file methods">
    @Nonnull
    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionHeader.txt"};
            // Todo: Add WALA and remove default
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
    }

    // TODO: List<?> testCases should not be <?>
    @SuppressWarnings("unchecked")
    @Nonnull
    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT ->
                    String.join("\n", ((List<JavaArchUnitSecurityTestCase>) testCases).stream().map(JavaArchUnitSecurityTestCase::writeArchitectureTestCase).toList());
            // Todo: Add WALA and remove default
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        };
    }

    @Nonnull
    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionFooter.txt"};
            // Todo: Add WALA and remove default
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
    }

    @Nonnull
    public String[] fileValue(@Nonnull String packageName) {
        return switch (this) {
            case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 2);
            // Todo: Add WALA and remove default
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        };
    }

    @Nonnull
    public Path targetToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ARCHUNIT -> new String[]{"api", "architecture", "java", "archunit", "JavaArchUnitTestCaseCollection.txt"};
            // Todo: Add WALA and remove default
            default ->  throw new SecurityException(localize("security.common.unsupported.operation", this));
        });
    }
    //</editor-fold>

    //<editor-fold desc="Reset methods">
    //</editor-fold>
}
