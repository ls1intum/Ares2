package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Verifies how {@link CommandPermission} binds from a policy file.
 * <p>
 * The mapping form carries the command and its arguments. The deprecated bare
 * form {@code - git}, still accepted by policy-format version 1, means "this
 * command with no arguments".
 * <p>
 * These tests use a bare mapper, one layer below
 * {@code SecurityPolicyYAMLReader}, which checks the schema first. What the
 * schema rejects, such as a numeric argument, is covered by
 * {@code SecurityPolicyStrictSchemaTest}.
 */
class CommandPermissionTest {

	private final YAMLMapper yamlMapper = new YAMLMapper();

	/**
	 * Checks that the mapping form binds the command and keeps its declared
	 * arguments in order.
	 */
	@Test
	void mappingFormPreservesDeclaredArguments() throws Exception {
		String yaml = "executeTheCommand: git\nwithTheseArguments:\n  - status\n  - --short\n";
		CommandPermission permission = yamlMapper.readValue(yaml, CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		assertThat(permission.withTheseArguments()).containsExactly("status", "--short");
	}

	/**
	 * Checks that the bare string form binds to the command with an empty argument
	 * list, not the wildcard, so it permits the command only without arguments.
	 * Widening it to any arguments would silently loosen every policy that uses it.
	 */
	@Test
	void bareStringStillBindsToTheCommandWithoutArguments() throws Exception {
		CommandPermission permission = yamlMapper.readValue("\"git\"", CommandPermission.class);
		assertThat(permission.executeTheCommand()).isEqualTo("git");
		assertThat(permission.withTheseArguments()).isEmpty();
	}

	/**
	 * Checks that an empty bare string is rejected.
	 */
	@Test
	void bareEmptyStringIsRejected() {
		assertThatThrownBy(() -> yamlMapper.readValue("\"\"", CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Checks that the deprecated fromString factory still creates the command
	 * without arguments.
	 */
	@Test
	@SuppressWarnings("removal")
	void fromStringStillCreatesTheCommandWithoutArguments() {
		CommandPermission permission = CommandPermission.fromString("git");
		assertThat(permission).isEqualTo(CommandPermission.allowWithoutArguments("git"));
	}

	/**
	 * Checks that the deprecated fromJson factory rejects a sequence node and a
	 * null node.
	 */
	@Test
	@SuppressWarnings("removal")
	void fromJsonRejectsANodeThatIsNeitherScalarNorMapping() {
		assertThatThrownBy(() -> CommandPermission.fromJson(new YAMLMapper().readTree("[git]")))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CommandPermission.fromJson(null)).isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Checks that a mapping without the arguments field is rejected, so omitting it
	 * cannot silently mean no constraint.
	 */
	@Test
	void mappingFormWithoutArgumentsIsRejected() {
		assertThatThrownBy(() -> yamlMapper.readValue("executeTheCommand: git\n", CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Checks that a mapping with a misspelt field is rejected. Jackson's own
	 * unknown-property handling never runs for this record because its creator
	 * binds the whole node, so without this check the misspelt field would be
	 * silently ignored. The schema refuses it in a policy file; this covers the
	 * layer below.
	 */
	@Test
	void mappingFormWithAnUnknownFieldIsRejected() {
		String yaml = "executeTheCommand: git\nwithTheseArguments: [status]\nwithTheseArgument: [--short]\n";
		assertThatThrownBy(() -> yamlMapper.readValue(yaml, CommandPermission.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Checks that arguments given as a single string instead of a list are
	 * rejected.
	 */
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
