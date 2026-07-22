package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
 * A policy declares a command permission in exactly one shape, the mapping
 * {@code {executeTheCommand: git, withTheseArguments: [status]}}, so Jackson
 * binds this record through its canonical constructor like every other
 * permission. The bare scalar form {@code - git} was also accepted once, which
 * is why this record carried a {@code @JsonCreator} that the others do not; it
 * meant "this command with no arguments", the opposite of what most readers
 * assumed, and it is no longer part of the format.
 * <p>
 * This record also used to override {@code toString()} to return the command
 * alone, a remnant of serialising it as a bare string through
 * {@code @JsonValue}, which silently dropped the arguments from a written
 * policy. It now serialises through its components.
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
	 * Allows the command with any arguments whatsoever.
	 * <p>
	 * Expressed as the single argument {@value #ANY_ARGUMENTS}, which both
	 * enforcement modes read as "impose no constraint on the arguments". Note the
	 * contrast with {@link #allowWithoutArguments}, whose empty list permits the
	 * command only when it is invoked with no arguments at all: an empty list is
	 * the most restrictive form, not the most permissive one, and the difference is
	 * easy to get backwards when writing a policy by hand.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param executeTheCommand the command to allow; must be neither null nor
	 *                          blank.
	 * @return a new CommandPermission instance that accepts every argument list.
	 * @throws NullPointerException     if the command is null.
	 * @throws IllegalArgumentException if the command is blank or otherwise
	 *                                  malformed.
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
