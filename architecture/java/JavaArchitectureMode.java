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
            case ARCHUNIT -> Stream.<String[]>of();
        }).map(FileTools::resolveOnResources).toList();
    }

    public List<String[]> fileValues(String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.<String[]>of();
        }).toList();
    }

    public List<Path> targetsToCopyTo(Path projectPath, String packageName) {
        return (switch (this) {
            case ARCHUNIT -> Stream.<String[]>of();
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ARCHUNIT -> new String[]{"templates", "java", "archunit", "JavaArchUnitTestCaseCollectionHeader.txt"};
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
            case ARCHUNIT -> new String[]{"templates", "java", "archunit", "JavaArchUnitTestCaseCollectionFooter.txt"};
        });
    }

    public String[] fileValue(String packageName) {
        return switch (this) {
            case ARCHUNIT -> new String[]{packageName, packageName};
        };
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ARCHUNIT -> new String[]{"JavaArchitectureTestCaseCollection.java"};
        });
    }
}
