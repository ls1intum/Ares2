package de.tum.cit.ase.ares.api.aop.fileSystem;

import javax.annotation.Nonnull;
import java.util.List;

public interface FileSystemExtractor {

    /**
     * Retrieves the list of file paths that are permitted for the given permission type.
     *
     * @param filePermission the type of file permission to filter by (e.g., "read", "overwrite"), must not be null.
     * @return a list of permitted file paths for the specified file permission type.
     */
    @Nonnull
    public abstract List<String> getPermittedFilePaths(@Nonnull String filePermission);
}
