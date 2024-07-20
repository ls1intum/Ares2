package de.tum.cit.ase.ares.api.policy;

import java.nio.file.Path;

/**
 * This class represents the file system interaction part of the security policy.
 */
public record FileSystemInteraction(
        /**
         * The path that the students are allowed to access.
         */
        Path onThisPathAndAllPathsBelow,
        /**
         * Whether the students are allowed to read all files at aforementioned path.
         */
        boolean studentsAreAllowedToReadAllFiles,
        /**
         * Whether the students are allowed to write to all files at aforementioned path.
         */
        boolean studentsAreAllowedToOverwriteAllFiles,
        /**
         * Whether the students are allowed to execute all files at aforementioned path.
         */
        boolean studentsAreAllowedToExecuteAllFiles,
        /**
         * Whether the students are allowed to delete all files at aforementioned path.
         */
        boolean studentsAreAllowedToDeleteAllFiles
) {
}