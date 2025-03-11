package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
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
        Objects.requireNonNull(executeTheCommand, "Command must not be null");
        Objects.requireNonNull(withTheseArguments, "Arguments list must not be null");
        if (executeTheCommand.isBlank()) {
            throw new IllegalArgumentException("executeTheCommand must not be blank");
        }
    }

    /**
     * Creates a restrictive command permission with empty arguments.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param command the command to restrict.
     * @return a new CommandPermission instance with empty arguments.
     */
    public static CommandPermission createRestrictive(String command) {
        return new CommandPermission(command, new ArrayList<>());
    }

    /**
     * Returns a builder for creating a CommandPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new CommandPermission.Builder instance.
     */
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

        private String command;
        private List<String> arguments = new ArrayList<>();

        /**
         * Sets the command.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param command the command to execute.
         * @return the updated Builder.
         */
        public Builder command(String command) {
            this.command = command;
            return this;
        }

        /**
         * Sets the command arguments.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param arguments the list of arguments.
         * @return the updated Builder.
         */
        public Builder arguments(List<String> arguments) {
            this.arguments = new ArrayList<>(arguments);
            return this;
        }

        /**
         * Adds a command argument.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param argument the argument to add.
         * @return the updated Builder.
         */
        public Builder addArgument(String argument) {
            this.arguments.add(argument);
            return this;
        }

        /**
         * Builds a new CommandPermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new CommandPermission instance.
         */
        public CommandPermission build() {
            return new CommandPermission(command, arguments);
        }
    }
}
