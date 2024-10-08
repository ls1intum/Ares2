package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public enum JavaArchitectureMode {
    ARCHUNIT;

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
        }).map(FileTools::resolveOnResources).toList();
    }

    public List<String[]> fileValues(String packageName) {
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
        }).toList();
    }

    public List<Path> targetsToCopyTo(Path projectPath, String packageName) {
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
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionHeader.txt"};
        });
    }

    @SuppressWarnings("unchecked")
    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT ->
                    String.join("\n", ((List<JavaArchUnitSecurityTestCase>) testCases).stream().map(JavaArchUnitSecurityTestCase::writeArchitectureTestCase).toList());
        };
    }

    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT ->
                    new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionFooter.txt"};
        });
    }

    public String[] fileValue(String packageName) {
        return switch (this) {
            case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 2);
        };
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ARCHUNIT -> new String[]{"api", "architecture", "java", "archunit", "JavaArchUnitTestCaseCollection.txt"};
        });
    }
}
