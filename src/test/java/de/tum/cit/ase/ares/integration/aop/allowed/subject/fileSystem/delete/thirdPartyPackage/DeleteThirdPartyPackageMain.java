package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DeleteThirdPartyPackageMain {

    private static final Path TRUSTED_FILE = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/trusteddir/trusted.txt");

    private DeleteThirdPartyPackageMain() {
        throw new SecurityException("utility");
    }

    public static void accessFileSystemViaThirdPartyPackage() throws java.io.IOException {
        Files.deleteIfExists(TRUSTED_FILE);
    }
}
