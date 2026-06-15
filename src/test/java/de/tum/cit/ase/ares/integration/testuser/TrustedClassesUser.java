package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.*;

@Public
@UseLocale("en")
@AddTrustedPackage("**subject.TrustedPackageP*")
@WhitelistClass(WhitelistedClassPenguin.class)
@WhitelistPath("")
public class TrustedClassesUser {

	private static final Path PATH = Path.of("pom.xml");

	@Test
	void testNotWhitelisted() throws IOException {
		// FileSystemAccessPenguin.accessPath(PATH);
	}

	@Test
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyPomAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/TrustedPackagePenguin.class")
	void testTrustedPackage() throws IOException {
		TrustedPackagePenguin.accessPath(PATH);
	}

	@Test
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyPomAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/WhitelistedClassPenguin.class")
	void testWhitelistedClass() throws IOException {
		WhitelistedClassPenguin.accessPath(PATH);
	}
}
