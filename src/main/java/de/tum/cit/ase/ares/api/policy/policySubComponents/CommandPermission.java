package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Allowed command execution operations.
 *
 * <p>Description: Specifies a command that is permitted to be executed along with its predefined arguments.
 *
 * <p>Design Rationale: Explicitly defining command execution permissions helps prevent unauthorised or harmful commands.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param executeTheCommand the command that is permitted to be executed; must not be null.
 * @param withTheseArguments the predefined arguments for the command; must not be null.
 */
@Nonnull
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
            throw new IllegalArgumentException("executeTheCommand must not be blank");
        }
        Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null");
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
        return builder().executeTheCommand(Objects.requireNonNull(executeTheCommand, "executeTheCommand must not be null")).withTheseArguments(new ArrayList<>()).build();
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
     *
     * <p>Description: Provides a fluent API to construct a CommandPermission instance.
     *
     * <p>Design Rationale: This builder enables step-by-step configuration of command execution permissions with variable arguments.
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
            this.withTheseArguments = new ArrayList<>(Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null"));
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
                    Objects.requireNonNull(withTheseArguments, "withTheseArguments must not be null")
            );
        }
    }
}
