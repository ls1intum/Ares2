package de.tum.cit.ase.ares.api.policy;

import java.nio.file.Path;

public record FileSystemInteraction(
        Path onThisPathAndAllPathsBelow,
        boolean studentsAreAllowedToReadAllFiles,
        boolean studentsAreAllowedToOverwriteAllFiles,
        boolean studentsAreAllowedToExecuteAllFiles,
        boolean studentsAreAllowedToDeleteAllFiles
) {
}