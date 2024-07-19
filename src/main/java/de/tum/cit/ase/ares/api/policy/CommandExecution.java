package de.tum.cit.ase.ares.api.policy;

import java.util.List;

public record CommandExecution(
        String iAllowTheStudentsToUseTheCommand,
        List<String> withTheFollowingArguments
) {
}
