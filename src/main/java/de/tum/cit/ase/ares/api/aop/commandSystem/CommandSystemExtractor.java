package de.tum.cit.ase.ares.api.aop.commandSystem;

import javax.annotation.Nonnull;
import java.util.List;

public interface CommandSystemExtractor {

    /**
     * Retrieves the list of commands that are permitted to be executed.
     *
     * @return a list of permitted commands, must not be null.
     */
    @Nonnull
    public abstract List<String> getPermittedCommands();

    /**
     * Retrieves the list of arguments permitted for execution with commands.
     *
     * @return a list of arguments permitted for command execution, must not be null.
     */
    @Nonnull
    public abstract List<List<String>> getPermittedArguments();
}
