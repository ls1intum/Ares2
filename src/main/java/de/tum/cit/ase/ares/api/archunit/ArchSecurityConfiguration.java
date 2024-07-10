package de.tum.cit.ase.ares.api.archunit;

import org.apiguardian.api.API;

import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.util.Map;

/**
 * This class represents the security configuration of the application.
 */
@API(status = API.Status.INTERNAL)
public record ArchSecurityConfiguration(
        String programmingLanguage,
        boolean allowFileSystemInteractions,
        Map<Path, AccessMode> allowedFileSystemInteractionsPaths) {}
