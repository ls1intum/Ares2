package de.tum.cit.ase.ares.api.policy;

import java.nio.file.Path;

public record FileSystemInteraction(
        Path path,
        boolean read,
        boolean write,
        boolean execute
) {
}