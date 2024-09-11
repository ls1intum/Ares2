package de.tum.cit.ase.ares.api.policy;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Defines the security policy for supervised code execution.
 *
 * @param regardingTheSupervisedCode the supervised code details, including its programming language configuration and permitted resource accesses.
 */
@Nonnull public record SecurityPolicy(
        @Nonnull SupervisedCode regardingTheSupervisedCode
) {

    /**
     * Specifies details about the supervised code, including programming language configuration and permitted resource accesses.
     *
     * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration used by the supervised code.
     * @param theProgrammingLanguageUsesTheFollowingPackage      the package used by the programming language.
     * @param theMainClassInsideThisPackageIs                    the main class inside the package.
     * @param theFollowingClassesAreTestClasses                  the test classes for the supervised code (these are ignored in the analysis).
     * @param theFollowingResourceAccessesArePermitted           the permitted resource accesses for the supervised code.
     */
    @Nonnull public record SupervisedCode(
            @Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
            @Nullable String theProgrammingLanguageUsesTheFollowingPackage,
            @Nullable String theMainClassInsideThisPackageIs,
            @Nonnull String[] theFollowingClassesAreTestClasses,
            @Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted
    ) {
    }

    /**
     * Supported programming language configurations for the policy.
     */
    @Nonnull public enum ProgrammingLanguageConfiguration {
        /**
         * Java using Maven build tool, ArchUnit for architecture tests and instrumentation-based aspect-oriented programming.
         */
        JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION,

        /**
         * Java using Maven build tool, ArchUnit for architecture tests and AspectJ for aspect-oriented programming.
         */
        JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,

        /**
         * Java using Gradle build tool, ArchUnit for architecture tests and instrumentation-based aspect-oriented programming.
         */
        JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION,

        /**
         * Java using Gradle build tool, ArchUnit for architecture tests and AspectJ for aspect-oriented programming.
         */
        JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ
    }


    /**
     * Specifies the resource accesses that are permitted for the supervised code.
     *
     * @param regardingFileSystemInteractions permitted file system interactions.
     * @param regardingNetworkConnections     permitted network connections.
     * @param regardingCommandExecutions      permitted command executions.
     * @param regardingThreadCreations        permitted thread creations.
     * @param regardingPackageImports         permitted package imports.
     */
    @Nonnull public record ResourceAccesses(
            @Nonnull List<FilePermission> regardingFileSystemInteractions,
            @Nonnull List<NetworkPermission> regardingNetworkConnections,
            @Nonnull List<CommandPermission> regardingCommandExecutions,
            @Nonnull List<ThreadPermission> regardingThreadCreations,
            @Nonnull List<PackagePermission> regardingPackageImports
    ) {
    }

    /**
     * Specifies allowed file operations.
     *
     * @param readAllFiles               whether reading all files is permitted.
     * @param overwriteAllFiles          whether overwriting all files is permitted.
     * @param executeAllFiles            whether executing all files is permitted.
     * @param deleteAllFiles             whether deleting all files is permitted.
     * @param onThisPathAndAllPathsBelow the path and its sub-paths where these permissions apply.
     */
    @Nonnull public record FilePermission(
            boolean readAllFiles,
            boolean overwriteAllFiles,
            boolean executeAllFiles,
            boolean deleteAllFiles,
            @Nonnull String onThisPathAndAllPathsBelow
    ) {
    }

    /**
     * Specifies allowed network operations.
     *
     * @param openConnections whether opening network connections is permitted.
     * @param sendData        whether sending data is permitted.
     * @param receiveData     whether receiving data is permitted.
     * @param onTheHost       the host where these operations are permitted.
     * @param onThePort       the port where these operations are permitted.
     */
    @Nonnull public record NetworkPermission(
            boolean openConnections,
            boolean sendData,
            boolean receiveData,
            @Nonnull String onTheHost,
            int onThePort
    ) {
    }

    /**
     * Specifies allowed command execution operations.
     *
     * @param executeTheCommand  the command that is permitted to be executed.
     * @param withTheseArguments the arguments that are permitted to be attached to the command when executing it. These arguments are predefined and cannot be altered by the user at runtime.
     */
    @Nonnull public record CommandPermission(
            @Nonnull String executeTheCommand,
            @Nonnull List<String> withTheseArguments
    ) {
    }

    /**
     * Specifies allowed thread creation operations.
     *
     * @param createTheFollowingNumberOfThreads the number of threads that are permitted to be created.
     * @param ofThisClass                       the class of the threads that are permitted to be created.
     */
    @Nonnull public record ThreadPermission(
            int createTheFollowingNumberOfThreads,
            @Nonnull String ofThisClass
    ) {
    }

    /**
     * Specifies allowed package imports.
     *
     * @param importTheFollowingPackage the package that is permitted to be imported.
     */
    @Nonnull public record PackagePermission(
            @Nonnull String importTheFollowingPackage
    ) {
    }

}