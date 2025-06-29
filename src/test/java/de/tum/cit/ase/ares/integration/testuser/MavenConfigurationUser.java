package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.api.util.DependencyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Public
@UseLocale("en")
@StrictTimeout(5)
public class MavenConfigurationUser {

    private static final Logger log = LoggerFactory.getLogger(MavenConfigurationUser.class);

    private static final String POM_XML_PATH = "src/test/java/de/tum/cit/ase/ares/integration/testuser/subject/example/build/tools/pom.xml";

    @PublicTest
    void testGetPomXmlPath() {
        try {
            File copiedFile = Files.createTempFile(
                    "pom",
                    ".xml"
            ).toFile();

            try (FileOutputStream fos = new FileOutputStream(copiedFile)) {
                // copy the content of the POM file to the temporary file
                fos.write(Files.readAllBytes(new File(POM_XML_PATH).toPath()));
            }
            DependencyManager.addDependenciesAndPluginsForMaven(copiedFile.getAbsolutePath());

            copiedFile.deleteOnExit();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
