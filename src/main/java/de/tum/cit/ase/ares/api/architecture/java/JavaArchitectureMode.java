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
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "CustomClassResolver.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "JavaArchitectureTestCaseCollection.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "postcompile", "TransitivelyAccessesMethodsCondition.java"},
                    new String[]{"templates", "architecture", "java", "archunit", "FileHandlerConstants.java"}
            );
        }).map(FileTools::resolveOnResources).toList();
    }

    public List<String[]> fileValues(String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.of(
                    FileTools.generatePackageNameArray(packageName, 1),
                    FileTools.generatePackageNameArray(packageName, 6),
                    FileTools.generatePackageNameArray(packageName, 1),
                    FileTools.generatePackageNameArray(packageName, 1)
            );
        }).toList();
    }

    public List<Path> targetsToCopyTo(Path projectPath, String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.of(
                    new String[]{"architecture", "java", "archunit", "postcompile", "CustomClassResolver.java"},
                    new String[]{"architecture", "java", "archunit", "postcompile", "JavaArchitectureTestCaseCollection.java"},
                    new String[]{"architecture", "java", "archunit", "postcompile", "TransitivelyAccessesMethodsCondition.java"},
                    new String[]{"architecture", "java", "archunit", "FileHandlerConstants.java"}
            );
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT -> new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionHeader.txt"};
        });
    }

    @SuppressWarnings("unchecked")
    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT -> String.join("\n", ((List<JavaArchUnitSecurityTestCase>) testCases).stream().map(JavaArchUnitSecurityTestCase::writeArchitectureTestCase).toList());
        };
    }

    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT -> new String[]{"templates", "architecture", "java", "archunit", "JavaArchitectureTestCaseCollectionFooter.txt"};
        });
    }

    public String[] fileValue(String packageName) {
        return switch (this) {
            case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 2);
        };
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ARCHUNIT -> new String[]{"architecture", "java", "archunit", "JavaArchUnitTestCaseCollection.txt"};
        });
    }
}
