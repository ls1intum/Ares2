package de.tum.cit.ase.ares.api.policy;

import java.util.List;

/**
 * This class represents the security policy that is used to define the security constraints for the students' submissions.
 * The security policy is defined in a YAML file and is read by the {@link SecurityPolicyReaderAndDirector} class.
 * The security policy is then used to generate the security test cases that are used to block dangerous executions and
 * provide respective feedback for the students' submissions.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public record SecurityPolicy(
        /**
         * The programming language that is used in this programming exercise.
         */
        SupportedProgrammingLanguage theProgrammingLanguageIUseInThisProgrammingExerciseIs,
        /**
         * The file system interactions that are allowed for the students' submissions.
         */
        List<FileSystemInteraction> iAllowTheFollowingFileSystemInteractionsForTheStudents,
        /**
         * The network connections that are allowed for the students' submissions.
         */
        List<NetworkConnection> iAllowTheFollowingNetworkConnectionsForTheStudents,
        /**
         * The command executions that are allowed for the students' submissions.
         */
        List<CommandExecution> iAllowTheFollowingCommandExecutionsForTheStudents,
        /**
         * The thread creations that are allowed for the students' submissions.
         */
        List<ThreadCreation> iAllowTheFollowingThreadCreationsForTheStudents,
        /**
         * The package imports that are allowed for the students' submissions.
         */
        List<PackageImport> iAllowTheFollowingPackageImportForTheStudents
) {
}
