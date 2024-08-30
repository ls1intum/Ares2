package de.tum.cit.ase.ares.api.policy;

import java.util.List;

/**
 * Represents the security policy for supervised code execution.
 * <p>
 * This record defines the overall security policy, including the programming language configuration
 * and the resource accesses that are permitted for the supervised code. It is used to enforce security
 * constraints during code execution.
 * </p>
 *
 * @param regardingTheSupervisedCode the details about the supervised code, including its programming language
 *                                   configuration and permitted resource accesses.
 */
public record SecurityPolicy(
        SupervisedCode regardingTheSupervisedCode
) {

    /**
     * Represents the supervised code details, including programming language configuration and permitted resource accesses.
     *
     * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration used by the supervised code.
     * @param theFollowingRessourceAccessesArePermitted           the resource accesses that are permitted for the supervised code.
     */
    public record SupervisedCode(
            ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
            ResourceAccesses theFollowingRessourceAccessesArePermitted
    ) {}

    /**
     * Enum representing the various programming language configurations supported by the policy.
     */
    public enum ProgrammingLanguageConfiguration {
        /**
         * Java using Maven build tool and instrumentation-based aspect-oriented programming.
         */
        JAVA_USING_MAVEN_AND_INSTRUMENTATION,

        /**
         * Java using Maven build tool and AspectJ for aspect-oriented programming.
         */
        JAVA_USING_MAVEN_AND_ASPECTJ,

        /**
         * Java using Gradle build tool and instrumentation-based aspect-oriented programming.
         */
        JAVA_USING_GRADLE_AND_INSTRUMENTATION,

        /**
         * Java using Gradle build tool and AspectJ for aspect-oriented programming.
         */
        JAVA_USING_GRADLE_AND_ASPECTJ
    }

    /**
     * Represents the resource accesses that are permitted for the supervised code.
     *
     * @param regardingFileSystemInteractions the permitted file system interactions.
     * @param regardingNetworkConnections     the permitted network connections.
     * @param regardingCommandExecutions      the permitted command executions.
     * @param regardingThreadCreations        the permitted thread creations.
     * @param regardingPackageImports         the permitted package imports.
     */
    public record ResourceAccesses(
            FileSystemInteractions regardingFileSystemInteractions,
            NetworkConnections regardingNetworkConnections,
            CommandExecutions regardingCommandExecutions,
            ThreadCreations regardingThreadCreations,
            PackageImports regardingPackageImports
    ) {}

    /**
     * Represents the file system interactions that are permitted.
     *
     * @param itIsPermittedTo a list of file permissions that are permitted.
     */
    public record FileSystemInteractions(
            List<FilePermission> itIsPermittedTo
    ) {}

    /**
     * Represents a file permission specifying the allowed file operations.
     *
     * @param readAllFiles              whether reading all files is permitted.
     * @param overwriteAllFiles         whether overwriting all files is permitted.
     * @param executeAllFiles           whether executing all files is permitted.
     * @param onThisPathAndAllPathsBelow the path and all subpaths where these permissions apply.
     */
    public record FilePermission(
            boolean readAllFiles,
            boolean overwriteAllFiles,
            boolean executeAllFiles,
            boolean deleteAllFiles,
            String onThisPathAndAllPathsBelow
    ) {}

    /**
     * Represents the network connections that are permitted.
     *
     * @param itIsPermittedTo a list of network permissions that are permitted.
     */
    public record NetworkConnections(
            List<NetworkPermission> itIsPermittedTo
    ) {}

    /**
     * Represents a network permission specifying the allowed network operations.
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
    ) {}

    /**
     * Represents the command executions that are permitted.
     *
     * @param itIsPermittedTo a list of command permissions that are permitted.
     */
    public record CommandExecutions(
            List<CommandPermission> itIsPermittedTo
    ) {}

    /**
     * Represents a command permission specifying the allowed command execution operations.
     *
     * @param executeTheCommand  the command that is permitted to be executed.
     * @param withTheseArguments the arguments that are permitted with the command.
     */
    public record CommandPermission(
            String executeTheCommand,
            List<String> withTheseArguments
    ) {}

    /**
     * Represents the thread creations that are permitted.
     *
     * @param itIsPermittedTo a list of thread permissions that are permitted.
     */
    public record ThreadCreations(
            List<ThreadPermission> itIsPermittedTo
    ) {}

    /**
     * Represents a thread permission specifying the allowed thread creation operations.
     *
     * @param createTheFollowingNumberOfThreads the number of threads that are permitted to be created.
     * @param ofThisClass                       the class of the threads that are permitted to be created.
     */
    public record ThreadPermission(
            int createTheFollowingNumberOfThreads,
            String ofThisClass
    ) {}

    /**
     * Represents the package imports that are permitted.
     *
     * @param itIsPermittedTo a list of package permissions that are permitted.
     */
    public record PackageImports(
            List<PackagePermission> itIsPermittedTo
    ) {}

    /**
     * Represents a package permission specifying the allowed package imports.
     *
     * @param importTheFollowingPackage the package that is permitted to be imported.
     */
    public record PackagePermission(
            String importTheFollowingPackage
    ) {}

}