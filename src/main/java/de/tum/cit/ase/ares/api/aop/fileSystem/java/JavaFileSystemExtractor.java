package de.tum.cit.ase.ares.api.aop.fileSystem.java;

import de.tum.cit.ase.ares.api.aop.fileSystem.FileSystemExtractor;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

public class JavaFileSystemExtractor implements FileSystemExtractor {

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Constructs a new JavaFileSystemExtractor with the specified resource access supplier.
     *
     * @param resourceAccessSupplier the supplier for the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaFileSystemExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
        this.resourceAccessSupplier = resourceAccessSupplier;
    }


    //<editor-fold desc="File System Interactions related methods">

    /**
     * Extracts the permitted file paths from the provided configurations based on the given predicate.
     *
     * @param configs   the list of JavaTestCase configurations, must not be null.
     * @param predicate a filter for determining which paths are permitted, must not be null.
     * @return a list of permitted paths.
     */
    @Nonnull
    public static List<String> extractPaths(@Nonnull List<FilePermission> configs, @Nonnull Predicate<FilePermission> predicate) {
        return configs.stream()
                .filter(predicate)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }

    /**
     * Retrieves the list of file paths that are permitted for the given permission type.
     *
     * @param filePermission the type of file permission to filter by (e.g., "read", "overwrite"), must not be null.
     * @return a list of permitted file paths for the specified file permission type.
     */
    @Nonnull
    public List<String> getPermittedFilePaths(@Nonnull String filePermission) {
        @Nonnull Predicate<FilePermission> filter = switch (filePermission) {
            case "read" -> FilePermission::readAllFiles;
            case "overwrite" -> FilePermission::overwriteAllFiles;
            case "execute" -> FilePermission::executeAllFiles;
            case "delete" -> FilePermission::deleteAllFiles;
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.file.permission", filePermission));
        };
        return ((List<FilePermission>) resourceAccessSupplier.get())
                .stream()
                .filter(filter)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }

    //</editor-fold>
}
