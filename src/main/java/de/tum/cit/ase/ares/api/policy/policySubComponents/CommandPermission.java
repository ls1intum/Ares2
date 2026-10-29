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
 * A command the supervised code may run, and its allowed arguments.
 * <p>
 * Write the mapping {@code {executeTheCommand: git, withTheseArguments:
 * [status]}}. The bare form {@code - git} still loads but is deprecated: it
 * means the command with no arguments.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param executeTheCommand  the command, or {@code *} for every command.
 * @param withTheseArguments the arguments; a sole {@code *} permits every list,
 *                           and inside a longer list any one argument there.
 */
@SuppressWarnings("unused")
public record CommandPermission(@Nonnull String executeTheCommand, @Nonnull List<String> withTheseArguments) {

	/**
	 * The token that lifts a constraint rather than expressing one.
	 * <p>
	 * As the sole entry of {@code withTheseArguments} it permits every argument
	 * list; at one position within a longer list it permits any single argument
	 * there; and as {@code executeTheCommand} it permits every command.
	 *
	 * @since 2.1.0
	 */
	public static final String ANY_ARGUMENTS = "*";

	/**
	 * Constructs a CommandPermission instance. The arguments are copied into an
	 * unmodifiable list, so they cannot be widened later.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param executeTheCommand  the command to allow.
	 * @param withTheseArguments the allowed arguments.
	 * @throws NullPointerException     if the command, the list or an entry is
	 *                                  null.
	 * @throws IllegalArgumentException if the command is blank or malformed, or an
	 *                                  argument holds a control character or line
	 *                                  separator.
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
	 * Reads a permission from a policy file for Jackson.
	 * <p>
	 * A bare string gives the command with no arguments. A mapping must hold
	 * exactly the two fields, checked here as Jackson does not. Both then pass the
	 * constructor checks.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param node a string or a mapping.
	 * @return the permission the node denotes.
	 * @throws IllegalArgumentException if the node has another shape.
	 * @deprecated Only the bare-string form is; in code, use {@link #builder()}.
	 */
	@Deprecated(forRemoval = true)
	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	@Nonnull
	public static CommandPermission fromJson(@Nullable JsonNode node) {
		if (node == null || node.isNull()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		if (node.isTextual()) {
			return allowWithoutArguments(node.textValue());
		}
		if (!node.isObject()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		if (node.size() != 2 || !node.has("executeTheCommand") || !node.has("withTheseArguments")) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.mapping.fields"));
		}
		JsonNode command = node.get("executeTheCommand");
		if (!command.isTextual() || command.textValue().isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.blank"));
		}
		JsonNode arguments = node.get("withTheseArguments");
		if (!arguments.isArray()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.command.arguments.array"));
		}
		List<String> declared = new ArrayList<>();
		for (JsonNode argument : arguments) {
			if (!argument.isTextual()) {
				throw new IllegalArgumentException(Messages.localized("policy.permission.command.arguments.strings"));
			}
			declared.add(argument.textValue());
		}
		return new CommandPermission(command.textValue(), declared);
	}

	/**
	 * Creates a permission for the exact command without arguments.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param command the command to allow; must be neither null nor blank.
	 * @return a new CommandPermission instance with empty arguments.
	 * @throws NullPointerException     if the command is null.
	 * @throws IllegalArgumentException if the command is blank or malformed.
	 * @deprecated Use {@link #allowWithoutArguments}, whose name says what the
	 *             empty argument list means. Kept for the 2.1.0 API.
	 */
	@Deprecated(forRemoval = true)
	@Nonnull
	public static CommandPermission fromString(@Nonnull String command) {
		return allowWithoutArguments(command);
	}

	/**
	 * Allows the command with any arguments whatsoever.
	 * <p>
	 * Expressed as the single argument {@value #ANY_ARGUMENTS}. Contrast
	 * {@link #allowWithoutArguments}: an empty list is the strictest form, not the
	 * most permissive one.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param executeTheCommand the command to allow.
	 * @return a permission that accepts every argument list.
	 * @throws NullPointerException     if the command is null.
	 * @throws IllegalArgumentException if the command is blank or malformed.
	 */
	@Nonnull
	public static CommandPermission allowWithAnyArguments(@Nonnull String executeTheCommand) {
		return builder()
				.executeTheCommand(Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null"))
				.withTheseArguments(new ArrayList<>(List.of(ANY_ARGUMENTS))).build();
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
