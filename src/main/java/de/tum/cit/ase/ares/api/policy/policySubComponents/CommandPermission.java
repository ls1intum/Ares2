package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Allowed command execution operations.
 * <p>
 * Description: Specifies a command that is permitted to be executed along with
 * its predefined arguments.
 * <p>
 * Design Rationale: Explicitly defining command execution permissions helps
 * prevent unauthorised or harmful commands.
 * <p>
 * This record used to override {@code toString()} to return the command alone.
 * That was a remnant of serialising it as a bare string through
 * {@code @JsonValue}, which silently dropped the arguments from a written
 * policy. The record now serialises through its components, and its string form
 * is the one a record generates, so a permission no longer prints as though it
 * carried no argument constraint.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param executeTheCommand  the command that is permitted to be executed, or
 *                           {@code *} for every command; must not be null.
 * @param withTheseArguments the predefined arguments for the command. A sole
 *                           {@code *} permits every argument list; within a
 *                           longer list it permits any one argument at that
 *                           position. Must not be null.
 */
@SuppressWarnings("unused")
public record CommandPermission(@Nonnull String executeTheCommand, @Nonnull List<String> withTheseArguments) {

	/**
	 * Constructs a CommandPermission instance.
	 * <p>
	 * The argument list is copied defensively, so a later change to the list the
	 * caller passed in cannot widen which arguments this permission allows.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param executeTheCommand  the command to allow; must be neither null nor
	 *                           blank, and must not contain control characters.
	 * @param withTheseArguments the arguments the command may be given; must not be
	 *                           null, and no entry may contain control characters.
	 * @throws NullPointerException     if the command, the argument list or one of
	 *                                  its entries is null.
	 * @throws IllegalArgumentException if the command is blank, or if the command
	 *                                  or one of the arguments contains a control
	 *                                  character.
	 */
	public CommandPermission {
		Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null");
		if (executeTheCommand.isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		PolicyValueValidator.requireMatch("executeTheCommand", executeTheCommand, PolicyValueValidator.COMMAND_PATTERN);
		Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null");
		for (String argument : withTheseArguments) {
			Objects.requireNonNull(argument, "withTheseArguments entries must not be null");
			PolicyValueValidator.requireMatch("withTheseArguments entry", argument,
					PolicyValueValidator.COMMAND_ARGUMENT_PATTERN);
		}
		// Defensive, unmodifiable copy so the record is genuinely immutable, matching
		// ResourceAccesses and SupervisedCode: the builder and Jackson pass a mutable
		// list whose security-relevant argument allow-list could otherwise be mutated
		// in place through the generated accessor.
		withTheseArguments = List.copyOf(withTheseArguments);
	}

	/**
	 * Allows the exact command without arguments.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param executeTheCommand the command to allow; must be neither null nor
	 *                          blank.
	 * @return a new CommandPermission instance with empty arguments.
	 * @throws NullPointerException     if the command is null.
	 * @throws IllegalArgumentException if the command is blank.
	 */
	@Nonnull
	public static CommandPermission allowWithoutArguments(@Nonnull String executeTheCommand) {
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
	 * @throws IllegalArgumentException if the node is null, is neither a string nor
	 *                                  an object, is an object without exactly the
	 *                                  two expected fields, carries a blank
	 *                                  command, or carries arguments that are not
	 *                                  an array of strings.
	 */
	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	@Nonnull
	public static CommandPermission fromJson(@Nullable JsonNode node) {
		if (node == null || node.isNull()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		if (node.isTextual()) {
			return allowWithoutArguments(node.asText());
		}
		if (node.isObject()) {
			if (node.size() != 2 || !node.has("executeTheCommand") || !node.has("withTheseArguments")) {
				throw new IllegalArgumentException(Messages.localized("policy.permission.command.mapping.fields"));
			}
			// Neither node can be null: the field-presence check above has already
			// established that both keys exist, and Jackson yields a NullNode rather than
			// a Java null for a key present with an empty value.
			JsonNode commandNode = node.get("executeTheCommand");
			if (!commandNode.isTextual() || commandNode.textValue().isBlank()) {
				throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
			}
			List<String> arguments = new ArrayList<>();
			JsonNode argumentsNode = node.get("withTheseArguments");
			if (!argumentsNode.isArray()) {
				throw new IllegalArgumentException(Messages.localized("policy.permission.command.arguments.array"));
			}
			for (JsonNode argument : argumentsNode) {
				if (!argument.isTextual()) {
					throw new IllegalArgumentException(
							Messages.localized("policy.permission.command.arguments.strings"));
				}
				arguments.add(argument.textValue());
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
	 * @param command the command string; must be neither null nor blank.
	 * @return a new CommandPermission instance with empty arguments
	 * @throws NullPointerException     if the command is null.
	 * @throws IllegalArgumentException if the command is blank.
	 */
	@Nonnull
	public static CommandPermission fromString(String command) {
		return allowWithoutArguments(command);
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
		 * The command to execute. Has no default, so {@link #build()} rejects a builder
		 * on which it was never set.
		 */
		@Nullable
		private String executeTheCommand;
		/**
		 * The arguments the command may be given. Has no default, so {@link #build()}
		 * rejects a builder on which it was never set; pass an empty list to permit the
		 * command without arguments.
		 */
		@Nullable
		private List<String> withTheseArguments;

		/**
		 * Sets the command.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param executeTheCommand the command to execute; must not be null, and is
		 *                          checked for blankness by {@link #build()}.
		 * @return the updated Builder.
		 * @throws NullPointerException if the command is null.
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
		 * @param withTheseArguments the list of arguments; must not be null. It is
		 *                           copied, so later changes to the list passed in do
		 *                           not affect this builder.
		 * @return the updated Builder.
		 * @throws NullPointerException if the list is null.
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
		 * @throws NullPointerException     if the command or the argument list was
		 *                                  never set.
		 * @throws IllegalArgumentException if the command is blank.
		 */
		@Nonnull
		public CommandPermission build() {
			return new CommandPermission(
					Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null"),
					Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null"));
		}
	}
}
