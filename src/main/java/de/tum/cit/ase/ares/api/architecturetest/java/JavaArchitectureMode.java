package de.tum.cit.ase.ares.api.architecturetest.java;

import de.tum.cit.ase.ares.api.architecturetest.java.archunit.JavaArchUnitTestCase;
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

    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT ->
                    JavaArchUnitTestCase.createArchitectureTestCaseFileFullContent((List<JavaArchUnitTestCase>) testCases
                    );
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
