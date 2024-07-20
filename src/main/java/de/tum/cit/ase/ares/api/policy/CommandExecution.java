package de.tum.cit.ase.ares.api.policy;

import java.util.List;

/**
 * This class represents the command execution part of the security policy.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public record CommandExecution(
        /**
         * The command that the students are allowed to use.
         */
        String iAllowTheStudentsToUseTheCommand,
        /**
         * The arguments that the students are allowed to use with the command.
         */
        List<String> withTheFollowingArguments
) {
}
