package de.tum.cit.ase.ares.integration.testuser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.api.util.DependencyManager;

@Public
@UseLocale("en")
@StrictTimeout(5)
public class MavenConfigurationUser {

	private static final Path POM_XML_PATH = Path
			.of("src/test/java/de/tum/cit/ase/ares/integration/testuser/subject/example/build/tools/pom.xml");
	private static final Path COPIED_POM_XML_PATH = Path
			.of("target/test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld/pom.xml");

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyMavenConfigurationUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
	void testGetPomXmlPath() throws Exception {
		Files.copy(POM_XML_PATH, COPIED_POM_XML_PATH, StandardCopyOption.REPLACE_EXISTING);
		try {
			DependencyManager.addDependenciesAndPluginsForMaven(COPIED_POM_XML_PATH.toString());
		} finally {
			Files.deleteIfExists(COPIED_POM_XML_PATH);
		}
	}
}
