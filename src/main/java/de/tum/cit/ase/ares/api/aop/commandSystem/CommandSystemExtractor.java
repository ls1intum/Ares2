package de.tum.cit.ase.ares.api.aop.commandSystem;

import java.util.List;

import javax.annotation.Nonnull;

public interface CommandSystemExtractor {

	/**
	 * Retrieves the list of commands that are permitted to be executed.
	 *
	 * @return a list of permitted commands, must not be null.
	 */
	@Nonnull
	List<String> getPermittedCommands();

	/**
	 * Retrieves the list of arguments permitted for execution with commands.
	 *
	 * @return a list of arguments permitted for command execution, must not be
	 *         null.
	 */
	@Nonnull
	List<List<String>> getPermittedArguments();
}
