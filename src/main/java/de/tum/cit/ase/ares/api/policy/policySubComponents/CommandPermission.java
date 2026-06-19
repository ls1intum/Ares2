package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Allowed command execution operations.
 * <p>
 * Description: Specifies a command that is permitted to be executed along with
 * its predefined arguments.
 * <p>
 * Design Rationale: Explicitly defining command execution permissions helps
 * prevent unauthorised or harmful commands.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param executeTheCommand  the command that is permitted to be executed; must
 *                           not be null.
 * @param withTheseArguments the predefined arguments for the command; must not
 *                           be null.
 */
@SuppressWarnings("unused")
public record CommandPermission(@Nonnull String executeTheCommand, @Nonnull List<String> withTheseArguments) {

	/**
	 * Constructs a CommandPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public CommandPermission {
		Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null");
		if (executeTheCommand.isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null");
		// Defensive, unmodifiable copy so the record is genuinely immutable, matching
		// ResourceAccesses and SupervisedCode: the builder and Jackson pass a mutable
		// list whose security-relevant argument allow-list could otherwise be mutated
		// in place through the generated accessor.
		withTheseArguments = List.copyOf(withTheseArguments);
	}

	/**
	 * Creates a restrictive command permission with empty arguments.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param executeTheCommand the command to restrict.
	 * @return a new CommandPermission instance with empty arguments.
	 */
	@Nonnull
	public static CommandPermission createRestrictive(@Nonnull String executeTheCommand) {
		return builder()
				.executeTheCommand(Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null"))
				.withTheseArguments(new ArrayList<>()).build();
	}

	/**
	 * Deserialises a CommandPermission from either YAML form, used by Jackson:
	 * <ul>
	 * <li>a bare string {@code "git"} &rarr; the command with no argument
	 * constraint expressible today, i.e. an empty argument list (matches the
	 * historical {@link #fromString} behaviour, NOT a widening to "any
	 * arguments");</li>
	 * <li>a mapping {@code {executeTheCommand: git, withTheseArguments: [status]}}
	 * &rarr; the command with the declared arguments. This form previously threw a
	 * {@code MismatchedInputException}, leaving the runtime argument check
	 * unreachable from a policy file.</li>
	 * </ul>
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param node the JSON/YAML node (a string scalar or an object)
	 * @return a new CommandPermission instance
	 */
	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	@Nonnull
	public static CommandPermission fromJson(@Nullable JsonNode node) {
		if (node == null || node.isNull()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		if (node.isTextual()) {
			return createRestrictive(node.asText());
		}
		if (node.isObject()) {
			JsonNode commandNode = node.get("executeTheCommand");
			if (commandNode == null || !commandNode.isTextual()) {
				throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
			}
			List<String> arguments = new ArrayList<>();
			JsonNode argumentsNode = node.get("withTheseArguments");
			if (argumentsNode != null && argumentsNode.isArray()) {
				for (JsonNode argument : argumentsNode) {
					arguments.add(argument.asText());
				}
			}
			return builder().executeTheCommand(commandNode.asText()).withTheseArguments(arguments).build();
		}
		throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
	}

	/**
	 * Creates a CommandPermission from a string command (retained for callers and
	 * tests that build a restrictive permission directly).
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param command the command string
	 * @return a new CommandPermission instance with empty arguments
	 */
	@Nonnull
	public static CommandPermission fromString(String command) {
		return createRestrictive(command);
	}

	/**
	 * Returns a human-readable representation. No longer annotated with
	 * {@code @JsonValue}: serialising to only the command silently dropped the
	 * arguments, so the record now serialises through its components.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the command string
	 */
	@Override
	@Nonnull
	public String toString() {
		return executeTheCommand;
	}

	/**
	 * Returns a builder for creating a CommandPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new CommandPermission.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for CommandPermission.
	 * <p>
	 * Description: Provides a fluent API to construct a CommandPermission instance.
	 * <p>
	 * Design Rationale: This builder enables step-by-step configuration of command
	 * execution permissions with variable arguments.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static class Builder {

		/**
		 * The command to execute.
		 */
		@Nullable
		private String executeTheCommand;
		/**
		 * The list of arguments for the command.
		 */
		@Nullable
		private List<String> withTheseArguments;

		/**
		 * Sets the command.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param executeTheCommand the command to execute.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder executeTheCommand(@Nonnull String executeTheCommand) {
			this.executeTheCommand = Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null");
			return this;
		}

		/**
		 * Sets the command arguments.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param withTheseArguments the list of arguments.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder withTheseArguments(@Nonnull List<String> withTheseArguments) {
			this.withTheseArguments = new ArrayList<>(
					Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null"));
			return this;
		}

		/**
		 * Builds a new CommandPermission instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new CommandPermission instance.
		 */
		@Nonnull
		public CommandPermission build() {
			return new CommandPermission(
					Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null"),
					Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null"));
		}
	}
}
