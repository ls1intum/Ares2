package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public record ResourceAccesses(
        @Nonnull List<FilePermission> regardingFileSystemInteractions,
        @Nonnull List<NetworkPermission> regardingNetworkConnections,
        @Nonnull List<CommandPermission> regardingCommandExecutions,
        @Nonnull List<ThreadPermission> regardingThreadCreations,
        @Nonnull List<PackagePermission> regardingPackageImports
) {

    /**
     * Constructs a ResourceAccesses instance with the provided permissions.
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
     * @version 2.0.0
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
