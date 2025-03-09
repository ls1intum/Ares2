package de.tum.cit.ase.ares.api.policy;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable security policy for supervised code execution.
 *
 * <p>Description: This record encapsulates all necessary details regarding supervised code, including its programming language
 * configuration, permitted resource accesses, and additional metadata. By using the immutable record pattern, it guarantees
 * thread-safety and reduces boilerplate. Input validation is performed in factory methods to ensure that every instance meets its invariants.
 *
 * <p>Design Rationale: Leveraging modern Java features such as records and nullability annotations enforces immutability and clarity.
 * The use of a factory method for construction and in‑constructor validation ensures that only valid, consistent instances are created,
 * aligning with best practices for secure and maintainable design. The clear separation into nested records reflects the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param regardingTheSupervisedCode the details of the supervised code; must not be null.
 */
@SuppressWarnings("unused")
@Nonnull
public record SecurityPolicy(@Nonnull SupervisedCode regardingTheSupervisedCode) {

    /**
     * Constructs a SecurityPolicy instance with validated supervised code.
     *
     * @since 2.0.0
     * @author Markus Paulsen     */
    public SecurityPolicy {
        Objects.requireNonNull(regardingTheSupervisedCode, "Supervised code must not be null");
    }

    /**
     * Creates a restrictive security policy with all permissions denied by default.
     *
     * @since 2.0.0
     * @author Markus Paulsen     * @param programmingLanguageConfiguration the programming language configuration for the restrictive policy.
     * @return a new SecurityPolicy instance.
     */
    public static SecurityPolicy createRestrictive(SupervisedCode.ProgrammingLanguageConfiguration programmingLanguageConfiguration) {
        return new SecurityPolicy(SupervisedCode.createRestrictive(programmingLanguageConfiguration));
    }

    /**
     * Returns a builder for creating a SecurityPolicy instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen     * @return a new SecurityPolicy.Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SecurityPolicy.
     *
     * <p>Description: Provides a fluent API to construct a SecurityPolicy instance.
     *
     * <p>Design Rationale: The builder pattern here allows for step-by-step configuration of a SecurityPolicy, ensuring immutability.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static class Builder {

        private SupervisedCode supervisedCode;

        /**
         * Sets the supervised code for the SecurityPolicy.
         *
         * @since 2.0.0
         * @author Markus Paulsen         * @param supervisedCode the supervised code instance.
         * @return the updated Builder.
         */
        public Builder supervisedCode(SupervisedCode supervisedCode) {
            this.supervisedCode = supervisedCode;
            return this;
        }

        /**
         * Builds a new SecurityPolicy instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen         * @return a new SecurityPolicy instance.
         */
        public SecurityPolicy build() {
            return new SecurityPolicy(supervisedCode);
        }
    }

    /**
     * Supervised code details.
     *
     * <p>Description: Contains the details about the supervised code, including its programming language configuration,
     * package information, main class, test classes, and permitted resource accesses.
     *
     * <p>Design Rationale: Encapsulating all aspects of supervised code in an immutable record ensures clarity and consistency.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @since 2.0.0
     * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration used by the code; must not be null.
     * @param theProgrammingLanguageUsesTheFollowingPackage the base package used by the code; may be null if not applicable.
     * @param theMainClassInsideThisPackageIs the main class name; may be null if not applicable.
     * @param theFollowingClassesAreTestClasses an array of test class names; must not be null.
     * @param theFollowingResourceAccessesArePermitted the permitted resource accesses; must not be null.
     */
    @Nonnull
    public record SupervisedCode(
            @Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
            @Nullable String theProgrammingLanguageUsesTheFollowingPackage,
            @Nullable String theMainClassInsideThisPackageIs, @Nonnull String[] theFollowingClassesAreTestClasses,
            @Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted) {

        /**
         * Constructs a SupervisedCode instance with the provided details.
         *
         * @since 2.0.0
         * @author Markus Paulsen         */
        public SupervisedCode {
            Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed, "ProgrammingLanguageConfiguration must not be null");
            Objects.requireNonNull(theFollowingClassesAreTestClasses, "Test classes array must not be null");
            Objects.requireNonNull(theFollowingResourceAccessesArePermitted, "ResourceAccesses must not be null");
        }

        /**
         * Creates a restrictive supervised code configuration.
         *
         * @since 2.0.0
         * @author Markus Paulsen         * @param config the programming language configuration for the restrictive code.
         * @return a new SupervisedCode instance with restrictive settings.
         */
        public static SupervisedCode createRestrictive(@Nonnull ProgrammingLanguageConfiguration config) {
            return new SupervisedCode(config, null, null, new String[0], ResourceAccesses.createRestrictive());
        }

        /**
         * Returns a builder for creating a SupervisedCode instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen         * @return a new SupervisedCode.Builder instance.
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for SupervisedCode.
         *
         * <p>Description: Provides a fluent API to construct a SupervisedCode instance.
         *
         * <p>Design Rationale: The builder pattern allows for flexible and readable construction of a SupervisedCode with various options.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         */
        public static class Builder {

            private ProgrammingLanguageConfiguration configuration;
            private String packageName;
            private String mainClass;
            private String[] testClasses = new String[0];
            private ResourceAccesses resourceAccesses;

            /**
             * Sets the programming language configuration.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @param configuration the programming language configuration.
             * @return the updated Builder.
             */
            public Builder configuration(ProgrammingLanguageConfiguration configuration) {
                this.configuration = configuration;
                return this;
            }

            /**
             * Sets the package name.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @param packageName the base package name.
             * @return the updated Builder.
             */
            public Builder packageName(String packageName) {
                this.packageName = packageName;
                return this;
            }

            /**
             * Sets the main class name.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @param mainClass the main class name.
             * @return the updated Builder.
             */
            public Builder mainClass(String mainClass) {
                this.mainClass = mainClass;
                return this;
            }

            /**
             * Sets the test classes.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @param testClasses an array of test class names.
             * @return the updated Builder.
             */
            public Builder testClasses(String[] testClasses) {
                this.testClasses = testClasses;
                return this;
            }

            /**
             * Sets the resource accesses.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @param resourceAccesses the permitted resource accesses.
             * @return the updated Builder.
             */
            public Builder resourceAccesses(ResourceAccesses resourceAccesses) {
                this.resourceAccesses = resourceAccesses;
                return this;
            }

            /**
             * Builds a new SupervisedCode instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @return a new SupervisedCode instance.
             */
            public SupervisedCode build() {
                return new SupervisedCode(configuration, packageName, mainClass, testClasses, resourceAccesses);
            }
        }

        /**
         * Enumerates the supported programming language configurations.
         *
         * <p>Description: Defines various Java project configurations, including build tools and aspect-oriented programming frameworks.
         *
         * <p>Design Rationale: This enumeration provides a standardised set of configurations for supervised code execution.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         */
        @Nonnull
        public enum ProgrammingLanguageConfiguration {
            JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION, JAVA_USING_MAVEN_WALA_AND_ASPECTJ, JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION, JAVA_USING_GRADLE_WALA_AND_ASPECTJ
        }

        /**
         * Resource accesses permitted for the supervised code.
         *
         * <p>Description: Specifies the file system interactions, network connections, command executions, thread creations, and package imports permitted.
         *
         * <p>Design Rationale: Encapsulating allowed resource accesses in a separate record facilitates granular control over code execution security.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @since 2.0.0
         * @param regardingFileSystemInteractions permitted file system interactions; must not be null.
         * @param regardingNetworkConnections permitted network connections; must not be null.
         * @param regardingCommandExecutions permitted command executions; must not be null.
         * @param regardingThreadCreations permitted thread creations; must not be null.
         * @param regardingPackageImports permitted package imports; must not be null.
         */
        @Nonnull
        public record ResourceAccesses(@Nonnull List<FilePermission> regardingFileSystemInteractions,
                                       @Nonnull List<NetworkPermission> regardingNetworkConnections,
                                       @Nonnull List<CommandPermission> regardingCommandExecutions,
                                       @Nonnull List<ThreadPermission> regardingThreadCreations,
                                       @Nonnull List<PackagePermission> regardingPackageImports) {

            /**
             * Constructs a ResourceAccesses instance with the specified permissions.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public ResourceAccesses {
                Objects.requireNonNull(regardingFileSystemInteractions, "File system interactions list must not be null");
                Objects.requireNonNull(regardingNetworkConnections, "Network connections list must not be null");
                Objects.requireNonNull(regardingCommandExecutions, "Command executions list must not be null");
                Objects.requireNonNull(regardingThreadCreations, "Thread creations list must not be null");
                Objects.requireNonNull(regardingPackageImports, "Package imports list must not be null");
            }

            /**
             * Creates restrictive resource accesses with all permissions denied.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @return a new ResourceAccesses instance with empty permissions lists.
             */
            public static ResourceAccesses createRestrictive() {
                return new ResourceAccesses(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }

            /**
             * Returns a builder for creating a ResourceAccesses instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @return a new ResourceAccesses.Builder instance.
             */
            public static Builder builder() {
                return new Builder();
            }

            /**
             * Builder for ResourceAccesses.
             *
             * <p>Description: Provides a fluent API to construct a ResourceAccesses instance.
             *
             * <p>Design Rationale: The builder pattern here allows for flexible configuration of resource accesses.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public static class Builder {

                private List<FilePermission> filePermissions = new ArrayList<>();
                private List<NetworkPermission> networkPermissions = new ArrayList<>();
                private List<CommandPermission> commandPermissions = new ArrayList<>();
                private List<ThreadPermission> threadPermissions = new ArrayList<>();
                private List<PackagePermission> packagePermissions = new ArrayList<>();

                /**
                 * Sets the file system permissions.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permissions the list of file permissions.
                 * @return the updated Builder.
                 */
                public Builder filePermissions(List<FilePermission> permissions) {
                    this.filePermissions = new ArrayList<>(permissions);
                    return this;
                }

                /**
                 * Adds a file permission.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permission the file permission to add.
                 * @return the updated Builder.
                 */
                public Builder addFilePermission(FilePermission permission) {
                    this.filePermissions.add(permission);
                    return this;
                }

                /**
                 * Sets the network permissions.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permissions the list of network permissions.
                 * @return the updated Builder.
                 */
                public Builder networkPermissions(List<NetworkPermission> permissions) {
                    this.networkPermissions = new ArrayList<>(permissions);
                    return this;
                }

                /**
                 * Adds a network permission.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permission the network permission to add.
                 * @return the updated Builder.
                 */
                public Builder addNetworkPermission(NetworkPermission permission) {
                    this.networkPermissions.add(permission);
                    return this;
                }

                /**
                 * Sets the command permissions.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permissions the list of command permissions.
                 * @return the updated Builder.
                 */
                public Builder commandPermissions(List<CommandPermission> permissions) {
                    this.commandPermissions = new ArrayList<>(permissions);
                    return this;
                }

                /**
                 * Adds a command permission.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permission the command permission to add.
                 * @return the updated Builder.
                 */
                public Builder addCommandPermission(CommandPermission permission) {
                    this.commandPermissions.add(permission);
                    return this;
                }

                /**
                 * Sets the thread permissions.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permissions the list of thread permissions.
                 * @return the updated Builder.
                 */
                public Builder threadPermissions(List<ThreadPermission> permissions) {
                    this.threadPermissions = new ArrayList<>(permissions);
                    return this;
                }

                /**
                 * Adds a thread permission.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permission the thread permission to add.
                 * @return the updated Builder.
                 */
                public Builder addThreadPermission(ThreadPermission permission) {
                    this.threadPermissions.add(permission);
                    return this;
                }

                /**
                 * Sets the package permissions.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permissions the list of package permissions.
                 * @return the updated Builder.
                 */
                public Builder packagePermissions(List<PackagePermission> permissions) {
                    this.packagePermissions = new ArrayList<>(permissions);
                    return this;
                }

                /**
                 * Adds a package permission.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param permission the package permission to add.
                 * @return the updated Builder.
                 */
                public Builder addPackagePermission(PackagePermission permission) {
                    this.packagePermissions.add(permission);
                    return this;
                }

                /**
                 * Builds a new ResourceAccesses instance.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @return a new ResourceAccesses instance.
                 */
                public ResourceAccesses build() {
                    return new ResourceAccesses(filePermissions, networkPermissions, commandPermissions, threadPermissions, packagePermissions);
                }
            }
        }

        /**
         * Allowed file operations.
         *
         * <p>Description: Specifies whether reading, overwriting, executing, and deleting files is permitted on a given path.
         *
         * <p>Design Rationale: Explicitly defining file operation permissions helps ensure secure code execution.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @since 2.0.0
         * @param readAllFiles whether reading all files is permitted.
         * @param overwriteAllFiles whether overwriting all files is permitted.
         * @param executeAllFiles whether executing all files is permitted.
         * @param deleteAllFiles whether deleting all files is permitted.
         * @param onThisPathAndAllPathsBelow the path where these permissions apply; must not be null.
         */
        @Nonnull
        public record FilePermission(boolean readAllFiles, boolean overwriteAllFiles, boolean executeAllFiles,
                                     boolean deleteAllFiles, @Nonnull String onThisPathAndAllPathsBelow) {

            /**
             * Constructs a FilePermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public FilePermission {
                Objects.requireNonNull(onThisPathAndAllPathsBelow, "Path must not be null");
                if (onThisPathAndAllPathsBelow.isBlank()) {
                    throw new IllegalArgumentException("onThisPathAndAllPathsBelow must not be blank");
                }
            }

            /**
             * Creates a restrictive file permission with all operations denied.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @param path the path where the restrictions apply.
             * @return a new FilePermission instance with all operations denied.
             */
            public static FilePermission createRestrictive(String path) {
                return new FilePermission(false, false, false, false, path);
            }

            /**
             * Returns a builder for creating a FilePermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @return a new FilePermission.Builder instance.
             */
            public static Builder builder() {
                return new Builder();
            }

            /**
             * Builder for FilePermission.
             *
             * <p>Description: Provides a fluent API to construct a FilePermission instance.
             *
             * <p>Design Rationale: The builder pattern facilitates the step-by-step construction of file permissions.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public static class Builder {

                private boolean read;
                private boolean overwrite;
                private boolean execute;
                private boolean delete;
                private String path;

                /**
                 * Sets whether reading files is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param read whether reading is permitted.
                 * @return the updated Builder.
                 */
                public Builder read(boolean read) {
                    this.read = read;
                    return this;
                }

                /**
                 * Sets whether overwriting files is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param overwrite whether overwriting is permitted.
                 * @return the updated Builder.
                 */
                public Builder overwrite(boolean overwrite) {
                    this.overwrite = overwrite;
                    return this;
                }

                /**
                 * Sets whether executing files is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param execute whether executing is permitted.
                 * @return the updated Builder.
                 */
                public Builder execute(boolean execute) {
                    this.execute = execute;
                    return this;
                }

                /**
                 * Sets whether deleting files is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param delete whether deleting is permitted.
                 * @return the updated Builder.
                 */
                public Builder delete(boolean delete) {
                    this.delete = delete;
                    return this;
                }

                /**
                 * Sets the path where these permissions apply.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param path the file path.
                 * @return the updated Builder.
                 */
                public Builder path(String path) {
                    this.path = path;
                    return this;
                }

                /**
                 * Builds a new FilePermission instance.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @return a new FilePermission instance.
                 */
                public FilePermission build() {
                    return new FilePermission(read, overwrite, execute, delete, path);
                }
            }
        }

        /**
         * Allowed network operations.
         *
         * <p>Description: Specifies permissions for opening connections, sending data, and receiving data on a specified host and port.
         *
         * <p>Design Rationale: Clearly defining network permissions helps protect against unauthorised network interactions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @since 2.0.0
         * @param openConnections whether opening network connections is permitted.
         * @param sendData whether sending data is permitted.
         * @param receiveData whether receiving data is permitted.
         * @param onTheHost the host where these operations are permitted; must not be null.
         * @param onThePort the port number where these operations are permitted.
         */
        @Nonnull
        public record NetworkPermission(boolean openConnections, boolean sendData, boolean receiveData,
                                        @Nonnull String onTheHost, int onThePort) {

            /**
             * Constructs a NetworkPermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public NetworkPermission {
                Objects.requireNonNull(onTheHost, "Host must not be null");
                if (onTheHost.isBlank()) {
                    throw new IllegalArgumentException("onTheHost must not be blank");
                }
                if (onThePort < 0 || onThePort > 65535) {
                    throw new IllegalArgumentException("onThePort must be between 0 and 65535");
                }
            }

            /**
             * Creates a restrictive network permission with all operations denied.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @param host the host where the restriction applies.
             * @param port the port number where the restriction applies.
             * @return a new NetworkPermission instance with all operations denied.
             */
            public static NetworkPermission createRestrictive(String host, int port) {
                return new NetworkPermission(false, false, false, host, port);
            }

            /**
             * Returns a builder for creating a NetworkPermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @return a new NetworkPermission.Builder instance.
             */
            public static Builder builder() {
                return new Builder();
            }

            /**
             * Builder for NetworkPermission.
             *
             * <p>Description: Provides a fluent API to construct a NetworkPermission instance.
             *
             * <p>Design Rationale: This builder facilitates stepwise configuration of network permissions.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public static class Builder {

                private boolean openConnections;
                private boolean sendData;
                private boolean receiveData;
                private String host;
                private int port;

                /**
                 * Sets whether opening connections is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param openConnections whether opening connections is permitted.
                 * @return the updated Builder.
                 */
                public Builder openConnections(boolean openConnections) {
                    this.openConnections = openConnections;
                    return this;
                }

                /**
                 * Sets whether sending data is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param sendData whether sending data is permitted.
                 * @return the updated Builder.
                 */
                public Builder sendData(boolean sendData) {
                    this.sendData = sendData;
                    return this;
                }

                /**
                 * Sets whether receiving data is permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param receiveData whether receiving data is permitted.
                 * @return the updated Builder.
                 */
                public Builder receiveData(boolean receiveData) {
                    this.receiveData = receiveData;
                    return this;
                }

                /**
                 * Sets the host where these operations are permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param host the host.
                 * @return the updated Builder.
                 */
                public Builder host(String host) {
                    this.host = host;
                    return this;
                }

                /**
                 * Sets the port number where these operations are permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @param port the port number.
                 * @return the updated Builder.
                 */
                public Builder port(int port) {
                    this.port = port;
                    return this;
                }

                /**
                 * Builds a new NetworkPermission instance.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen
                 * @return a new NetworkPermission instance.
                 */
                public NetworkPermission build() {
                    return new NetworkPermission(openConnections, sendData, receiveData, host, port);
                }
            }
        }

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

        /**
         * Allowed thread creation operations.
         *
         * <p>Description: Specifies the number of threads permitted to be created and the thread class allowed.
         *
         * <p>Design Rationale: Limiting thread creation is crucial to control concurrency and resource usage.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @since 2.0.0
         * @param createTheFollowingNumberOfThreads the number of threads permitted to be created.
         * @param ofThisClass the fully qualified name of the allowed thread class; must not be null.
         */
        @Nonnull
        public record ThreadPermission(int createTheFollowingNumberOfThreads, @Nonnull String ofThisClass) {

            /**
             * Constructs a ThreadPermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public ThreadPermission {
                Objects.requireNonNull(ofThisClass, "Thread class must not be null");
                if (createTheFollowingNumberOfThreads < 0) {
                    throw new IllegalArgumentException("createTheFollowingNumberOfThreads threads must not be negative");
                }
                if (ofThisClass.isBlank()) {
                    throw new IllegalArgumentException("ofThisClass must not be blank");
                }
            }

            /**
             * Creates a restrictive thread permission with zero threads allowed.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             * @param threadClass the thread class for which no threads are permitted.
             * @return a new ThreadPermission instance with zero threads allowed.
             */
            public static ThreadPermission createRestrictive(String threadClass) {
                return new ThreadPermission(0, threadClass);
            }

            /**
             * Returns a builder for creating a ThreadPermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @return a new ThreadPermission.Builder instance.
             */
            public static Builder builder() {
                return new Builder();
            }

            /**
             * Builder for ThreadPermission.
             *
             * <p>Description: Provides a fluent API to construct a ThreadPermission instance.
             *
             * <p>Design Rationale: This builder allows for configurable thread creation restrictions.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public static class Builder {

                private int numberOfThreads;
                private String threadClass;

                /**
                 * Sets the number of threads permitted.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen                 * @param numberOfThreads the number of threads.
                 * @return the updated Builder.
                 */
                public Builder numberOfThreads(int numberOfThreads) {
                    this.numberOfThreads = numberOfThreads;
                    return this;
                }

                /**
                 * Sets the thread class allowed.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen                 * @param threadClass the fully qualified thread class name.
                 * @return the updated Builder.
                 */
                public Builder threadClass(String threadClass) {
                    this.threadClass = threadClass;
                    return this;
                }

                /**
                 * Builds a new ThreadPermission instance.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen                 * @return a new ThreadPermission instance.
                 */
                public ThreadPermission build() {
                    return new ThreadPermission(numberOfThreads, threadClass);
                }
            }
        }

        /**
         * Allowed package import.
         *
         * <p>Description: Specifies the package that is permitted to be imported.
         *
         * <p>Design Rationale: Explicitly declaring permitted package imports prevents unauthorised dependencies.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @since 2.0.0
         * @param importTheFollowingPackage the package that is permitted to be imported; must not be null.
         */
        @Nonnull
        public record PackagePermission(@Nonnull String importTheFollowingPackage) {

            /**
             * Constructs a PackagePermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen             */
            public PackagePermission {
                Objects.requireNonNull(importTheFollowingPackage, "Package name must not be null");
                if (importTheFollowingPackage.isBlank()) {
                    throw new IllegalArgumentException("importTheFollowingPackage must not be blank");
                }
            }

            /**
             * Creates a restrictive package permission.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @param packageName the package name for which import is restricted.
             * @return a new PackagePermission instance.
             */
            public static PackagePermission createRestrictive(String packageName) {
                return new PackagePermission(packageName);
            }

            /**
             * Returns a builder for creating a PackagePermission instance.
             *
             * @since 2.0.0
             * @author Markus Paulsen             * @return a new PackagePermission.Builder instance.
             */
            public static Builder builder() {
                return new Builder();
            }

            /**
             * Builder for PackagePermission.
             *
             * <p>Description: Provides a fluent API to construct a PackagePermission instance.
             *
             * <p>Design Rationale: This builder allows for flexible configuration of package import permissions.
             *
             * @since 2.0.0
             * @author Markus Paulsen
             */
            public static class Builder {

                private String packageName;

                /**
                 * Sets the package name.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen                 * @param packageName the package name.
                 * @return the updated Builder.
                 */
                public Builder packageName(String packageName) {
                    this.packageName = packageName;
                    return this;
                }

                /**
                 * Builds a new PackagePermission instance.
                 *
                 * @since 2.0.0
                 * @author Markus Paulsen                 * @return a new PackagePermission instance.
                 */
                public PackagePermission build() {
                    return new PackagePermission(packageName);
                }
            }
        }
    }
}