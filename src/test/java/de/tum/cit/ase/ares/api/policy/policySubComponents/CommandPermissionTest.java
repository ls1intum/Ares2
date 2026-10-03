package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Verifies how {@link CommandPermission} binds from a policy file.
 * <p>
 * A command permission should be written as the mapping carrying the command
 * and its arguments. The bare scalar form {@code - git} is also bound, through
 * a {@code @JsonCreator} the other permissions never needed; it means "this
 * command with no arguments", which reads as the opposite to most authors and
 * is therefore deprecated. It is not removed, because policy-format version 1
 * accepted it and version 1 is still the only supported version.
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
	void bareStringStillBindsToTheCommandWithoutArguments() throws Exception {
		CommandPermission permission = yamlMapper.readValue("\"git\"", CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		// Empty, not the wildcard: the scalar form permits git only when it is invoked
		// with no arguments at all. Pinning this is the point of keeping the form, as a
		// widening to "any arguments" would silently loosen every policy that uses it.
		assertThat(permission.withTheseArguments()).isEmpty();
	}

	@Test
	void bareEmptyStringIsRejected() {
		assertThatThrownBy(() -> yamlMapper.readValue("\"\"", CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@SuppressWarnings("removal")
	void fromStringStillCreatesTheCommandWithoutArguments() {
		CommandPermission permission = CommandPermission.fromString("git");
		assertThat(permission).isEqualTo(CommandPermission.allowWithoutArguments("git"));
	}

	@Test
	@SuppressWarnings("removal")
	void fromJsonRejectsANodeThatIsNeitherScalarNorMapping() {
		assertThatThrownBy(() -> CommandPermission.fromJson(new YAMLMapper().readTree("[git]")))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CommandPermission.fromJson(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void mappingFormWithoutArgumentsIsRejected() {
		// Omitting the field cannot silently mean "no constraint": the mapping has to
		// carry both fields.
		assertThatThrownBy(() -> yamlMapper.readValue("executeTheCommand: git\n", CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void mappingFormWithAnUnknownFieldIsRejected() {
		// A creator binds the whole node, so Jackson's own unknown-property handling
		// never runs for this record. Without the field check here a misspelt field
		// would bind silently through a bare mapper and the declared arguments would
		// vanish. The schema gate refuses it for a policy file; this covers the layer
		// below it.
		String yaml = "executeTheCommand: git\nwithTheseArguments: [status]\nwithTheseArgument: [--short]\n";
		assertThatThrownBy(() -> yamlMapper.readValue(yaml, CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
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
