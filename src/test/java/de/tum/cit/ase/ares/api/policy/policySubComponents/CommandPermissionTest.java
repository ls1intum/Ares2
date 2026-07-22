package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Verifies how {@link CommandPermission} binds from a policy file.
 * <p>
 * A command permission has exactly one shape, the mapping carrying the command
 * and its arguments, so this record binds through its canonical constructor
 * like every other permission. The bare scalar form {@code - git} was accepted
 * once, through a {@code @JsonCreator} the other permissions never needed; it
 * meant "this command with no arguments", which read as the opposite to most
 * authors, and it is no longer part of the format.
 * <p>
 * These tests bind with a bare mapper, which is one layer below how a policy is
 * actually read: {@code SecurityPolicyYAMLReader} validates the parsed tree
 * before binding it. Anything the schema rejects is therefore covered by
 * {@code SecurityPolicyStrictSchemaTest} rather than here, which matters for
 * values Jackson would otherwise coerce, such as a numeric argument.
 */
class CommandPermissionTest {

	private final YAMLMapper yamlMapper = new YAMLMapper();

	@Test
	void mappingFormPreservesDeclaredArguments() throws Exception {
		String yaml = "executeTheCommand: git\nwithTheseArguments:\n  - status\n  - --short\n";
		CommandPermission permission = yamlMapper.readValue(yaml, CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		assertThat(permission.withTheseArguments()).containsExactly("status", "--short");
	}

	@Test
	void bareStringIsNoLongerAPolicyForm() {
		assertThatThrownBy(() -> yamlMapper.readValue("\"git\"", CommandPermission.class))
				.hasMessageContaining("CommandPermission");
		assertThatThrownBy(() -> yamlMapper.readValue("\"\"", CommandPermission.class))
				.hasMessageContaining("CommandPermission");
	}

	@Test
	void mappingFormWithoutArgumentsIsRejected() {
		// The record refuses to be built without an argument list, so omitting the
		// field cannot silently mean "no constraint".
		assertThatThrownBy(() -> yamlMapper.readValue("executeTheCommand: git\n", CommandPermission.class))
				.hasRootCauseInstanceOf(NullPointerException.class);
	}

	@Test
	void mappingArgumentsMustBeAnArray() {
		String yaml = "executeTheCommand: git\nwithTheseArguments: status\n";
		assertThatThrownBy(() -> yamlMapper.readValue(yaml, CommandPermission.class)).isInstanceOf(Exception.class);
	}

	@Test
	void blankCommandIsRejected() {
		String yaml = "executeTheCommand: \" \"\nwithTheseArguments: []\n";
		assertThatThrownBy(() -> yamlMapper.readValue(yaml, CommandPermission.class))
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
