package de.tum.cit.ase.ares.api.policy;

import java.util.List;

/**
 * Defines the security policy for supervised code execution.
 *
 * @param regardingTheSupervisedCode the supervised code details, including its programming language configuration and permitted resource accesses.
 */
public record SecurityPolicy(
        SupervisedCode regardingTheSupervisedCode
) {

    /**
     * Specifies details about the supervised code, including programming language configuration and permitted resource accesses.
     *
     * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration used by the supervised code.
     * @param theProgrammingLanguageUsesTheFollowingPackage      the package used by the programming language.
     * @param theMainClassInsideThisPackageIs                    the main class inside the package.
     * @param theFollowingResourceAccessesArePermitted           the permitted resource accesses for the supervised code.
     */
    public record SupervisedCode(
            ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
            String theProgrammingLanguageUsesTheFollowingPackage,
            String theMainClassInsideThisPackageIs,
            String[] theFollowingClassesAreTestClasses,
            ResourceAccesses theFollowingResourceAccessesArePermitted
    ) {
    }

    /**
     * Supported programming language configurations for the policy.
     */
    public enum ProgrammingLanguageConfiguration {
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
    public record ResourceAccesses(
            List<FilePermission> regardingFileSystemInteractions,
            List<NetworkPermission> regardingNetworkConnections,
            List<CommandPermission> regardingCommandExecutions,
            List<ThreadPermission> regardingThreadCreations,
            List<PackagePermission> regardingPackageImports
    ) {
    }

    /**
     * Specifies allowed file operations.
     *
     * @param readAllFiles               whether reading all files is permitted.
     * @param overwriteAllFiles          whether overwriting all files is permitted.
     * @param executeAllFiles            whether executing all files is permitted.
     * @param deleteAllFiles             whether deleting all files is permitted.
     * @param onThisPathAndAllPathsBelow the path and its subpaths where these permissions apply.
     */
    public record FilePermission(
            boolean readAllFiles,
            boolean overwriteAllFiles,
            boolean executeAllFiles,
            boolean deleteAllFiles,
            String onThisPathAndAllPathsBelow
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
    public record NetworkPermission(
            boolean openConnections,
            boolean sendData,
            boolean receiveData,
            String onTheHost,
            int onThePort
    ) {
    }

    /**
     * Specifies allowed command execution operations.
     *
     * @param executeTheCommand  the command that is permitted to be executed.
     * @param withTheseArguments the arguments that are permitted with the command.
     */
    public record CommandPermission(
            String executeTheCommand,
            List<String> withTheseArguments
    ) {
    }

    /**
     * Specifies allowed thread creation operations.
     *
     * @param createTheFollowingNumberOfThreads the number of threads that are permitted to be created.
     * @param ofThisClass                       the class of the threads that are permitted to be created.
     */
    public record ThreadPermission(
            int createTheFollowingNumberOfThreads,
            String ofThisClass
    ) {
    }

    /**
     * Specifies allowed package imports.
     *
     * @param importTheFollowingPackage the package that is permitted to be imported.
     */
    public record PackagePermission(
            String importTheFollowingPackage
    ) {
    }

}