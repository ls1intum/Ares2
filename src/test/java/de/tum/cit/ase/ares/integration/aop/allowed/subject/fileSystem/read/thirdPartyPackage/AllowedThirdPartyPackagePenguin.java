package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.thirdPartyPackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AllowedThirdPartyPackagePenguin {

	private static final Path TRUSTED_FILE = Path
			.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");

	private AllowedThirdPartyPackagePenguin() {
		throw new SecurityException("utility");
	}

	public static String readFile() throws IOException {
		return Files.readString(TRUSTED_FILE);
	}
}
