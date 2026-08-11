package de.tum.cit.ase.ares.api.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

class ExampleConfigurationTest {

	@Test
	void filePermissionFlagsBelongToTheExampleListItem() throws Exception {
		try (InputStream resource = ExampleConfigurationTest.class.getResourceAsStream("/ExampleConfiguration.yaml")) {
			assertThat(resource).isNotNull();
			JsonNode root = new YAMLMapper().readTree(resource);
			JsonNode permission = root.path("regardingTheSupervisedCode")
					.path("theFollowingResourceAccessesArePermitted").path("regardingFileSystemInteractions").path(0);
			assertThat(permission.path("onThisPathAndAllPathsBelow").asText()).isEqualTo("pom.xml");
			assertThat(permission.path("readAllFiles").asBoolean()).isTrue();
			assertThat(permission.path("overwriteAllFiles").asBoolean()).isTrue();
			assertThat(permission.path("createAllFiles").asBoolean()).isTrue();
			assertThat(permission.path("executeAllFiles").asBoolean()).isTrue();
			assertThat(permission.path("deleteAllFiles").asBoolean()).isTrue();
		}
	}
}
