package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Verifies that {@link CommandPermission} deserialises from both supported YAML
 * forms: a bare command string and the mapping form carrying arguments. The
 * mapping form previously threw a {@code MismatchedInputException}, leaving the
 * runtime argument check unreachable from a policy file.
 */
class CommandPermissionTest {

	private final YAMLMapper yamlMapper = new YAMLMapper();

	@Test
	void bareStringYieldsCommandWithNoArguments() throws Exception {
		CommandPermission permission = yamlMapper.readValue("\"git\"", CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		// Bare string keeps the historical semantics (no declared arguments), NOT a
		// widening to "any arguments".
		assertThat(permission.withTheseArguments()).isEmpty();
	}

	@Test
	void mappingFormPreservesDeclaredArguments() throws Exception {
		String yaml = "executeTheCommand: git\nwithTheseArguments:\n  - status\n  - --short\n";
		CommandPermission permission = yamlMapper.readValue(yaml, CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		assertThat(permission.withTheseArguments()).containsExactly("status", "--short");
	}

	@Test
	void mappingFormWithoutArgumentsYieldsEmptyList() throws Exception {
		CommandPermission permission = yamlMapper.readValue("executeTheCommand: git\n", CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		assertThat(permission.withTheseArguments()).isEmpty();
	}

	@Test
	void blankCommandIsRejected() {
		assertThatThrownBy(() -> yamlMapper.readValue("\"\"", CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void withTheseArgumentsIsImmutable() {
		CommandPermission permission = CommandPermission.builder().executeTheCommand("git")
				.withTheseArguments(new java.util.ArrayList<>(java.util.List.of("status"))).build();
		assertThatThrownBy(() -> permission.withTheseArguments().add("--evil"))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
