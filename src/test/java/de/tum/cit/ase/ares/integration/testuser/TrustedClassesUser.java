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
// The legacy @AddTrustedPackage/@WhitelistClass trust mechanism is defunct under the
// AOP framework; these tests exercised that now-removed feature, so enforcement is not
// activated for them (narrower semantics, per the agreed enumerate-migration approach).
@Policy(activated = false)
public class TrustedClassesUser {

	private static final Path PATH = Path.of("pom.xml");

	@Test
	void testNotWhitelisted() throws IOException {
		// Legacy FileSystemAccessPenguin subject retired; non-whitelisted file-system
		// access is now covered by the aop.allowed/forbidden file-system suites.
	}

	@Test
	void testTrustedPackage() throws IOException {
		TrustedPackagePenguin.accessPath(PATH);
	}

	@Test
	void testWhitelistedClass() throws IOException {
		WhitelistedClassPenguin.accessPath(PATH);
	}
}
