package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
    public static ResourceAccesses createRestrictive() {
        return builder()
                .regardingFileSystemInteractions(new ArrayList<>())
                .regardingNetworkConnections(new ArrayList<>())
                .regardingCommandExecutions(new ArrayList<>())
                .regardingThreadCreations(new ArrayList<>())
                .regardingPackageImports(new ArrayList<>())
                .build();
    }

    /**
     * Returns a builder for creating a ResourceAccesses instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new ResourceAccesses.Builder instance.
     */
    @Nonnull
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

        /**
         * The file system permissions.
         */
        @Nullable
        private List<FilePermission> regardingFileSystemInteractions = new ArrayList<>();

        /**
         * The network permissions.
         */
        @Nullable
        private List<NetworkPermission> regardingNetworkConnections = new ArrayList<>();

        /**
         * The command permissions.
         */
        @Nullable
        private List<CommandPermission> regardingCommandExecutions = new ArrayList<>();

        /**
         * The thread permissions.
         */
        @Nullable
        private List<ThreadPermission> regardingThreadCreations = new ArrayList<>();

        /**
         * The package permissions.
         */
        @Nullable
        private List<PackagePermission> regardingPackageImports = new ArrayList<>();

        /**
         * Sets the file system permissions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingFileSystemInteractions the list of file permissions.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingFileSystemInteractions(@Nonnull List<FilePermission> regardingFileSystemInteractions) {
            this.regardingFileSystemInteractions = new ArrayList<>(Objects.requireNonNull(regardingFileSystemInteractions,"File system interactions list must not be null"));
            return this;
        }

        /**
         * Sets the network permissions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingNetworkConnections the list of network permissions.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingNetworkConnections(@Nonnull List<NetworkPermission> regardingNetworkConnections) {
            this.regardingNetworkConnections = new ArrayList<>(Objects.requireNonNull(regardingNetworkConnections,"Network connections list must not be null"));
            return this;
        }

        /**
         * Sets the command permissions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingCommandExecutions the list of command permissions.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingCommandExecutions(@Nonnull List<CommandPermission> regardingCommandExecutions) {
            this.regardingCommandExecutions = new ArrayList<>(Objects.requireNonNull(regardingCommandExecutions,"Command executions list must not be null"));
            return this;
        }

        /**
         * Sets the thread permissions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingThreadCreations the list of thread permissions.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingThreadCreations(@Nonnull List<ThreadPermission> regardingThreadCreations) {
            this.regardingThreadCreations = new ArrayList<>(Objects.requireNonNull(regardingThreadCreations,"Thread creations list must not be null"));
            return this;
        }

        /**
         * Sets the package permissions.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingPackageImports the list of package permissions.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingPackageImports(@Nonnull List<PackagePermission> regardingPackageImports) {
            this.regardingPackageImports = new ArrayList<>(Objects.requireNonNull(regardingPackageImports,"Package imports list must not be null"));
            return this;
        }

        /**
         * Builds a new ResourceAccesses instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new ResourceAccesses instance.
         */
        @Nonnull
        public ResourceAccesses build() {
            return new ResourceAccesses(
                    Objects.requireNonNull(regardingFileSystemInteractions, "File system interactions list must not be null"),
                    Objects.requireNonNull(regardingNetworkConnections, "Network connections list must not be null"),
                    Objects.requireNonNull(regardingCommandExecutions, "Command executions list must not be null"),
                    Objects.requireNonNull(regardingThreadCreations, "Thread creations list must not be null"),
                    Objects.requireNonNull(regardingPackageImports, "Package imports list must not be null")
            );
        }
    }
}
