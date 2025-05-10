package de.tum.cit.ase.ares.api.phobos;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

// TODO Ajay: Implement this class

public class Phobos {

    private Phobos() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static List<Path> filesToCopy() {
        return null;
    }

    public static List<Path> targetsToCopyTo(
            @Nullable Path testFolderPath,
            @Nonnull String packageName
    ) {
        return List.of();
    }

    public static List<String[]> fileValues(@Nonnull String packageName) {
        return List.of();
    }

    public static Path threePartedFileHeader() {
        return Path.of("");
    }

    public static String threePartedFileBody(
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nullable Path testFolderPath
    ) {
        return "";
    }

    public static Path threePartedFileFooter() {
        return Path.of("");
    }

    public static Path targetToCopyTo(
            @Nullable Path testFolderPath,
            @Nonnull String packageName
    ) {
        return Path.of("");
    }

    public static String[] fileValue(@Nonnull String packageName) {
        return new String[0];
    }

}
