package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Public
@UseLocale("en")
@StrictTimeout(5)
public class MavenConfigurationUser {

    private static final Logger log = LoggerFactory.getLogger(MavenConfigurationUser.class);

    @PublicTest
    void testGetPomXmlPath() {
        log.error(ProjectSourcesFinder.getPomXmlPath());
    }
}
