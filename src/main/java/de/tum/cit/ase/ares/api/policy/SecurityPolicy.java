package de.tum.cit.ase.ares.api.policy;

import java.util.List;

public record SecurityPolicy(
        SupportedProgrammingLanguage theProgrammingLanguageIUseInThisProgrammingExerciseIs,
        List<FileSystemInteraction> iAllowTheFollowingFileSystemInteractionsForTheStudents
) {
}
