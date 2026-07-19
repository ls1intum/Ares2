package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.MavenConfigurationUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(MavenConfigurationUser.class)
class MavenConfigurationTest {

	@UserTestResults
	private static Events tests;

	private final String testGetPomXmlPath = "testGetPomXmlPath";

	@TestTest
	void test_testGetPomXmlPath() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testGetPomXmlPath));
	}
}
