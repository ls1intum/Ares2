package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resource accesses permitted for the supervised code.
 * <p>
 * Each list is exhaustive, except that Ares adds its own, the supervised and
 * the test-class packages to the imports, and timeouts declare limits.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param regardingFileSystemInteractions files.
 * @param regardingNetworkConnections     network.
 * @param regardingCommandExecutions      commands.
 * @param regardingThreadCreations        threads.
 * @param regardingPackageImports         package imports.
 * @param regardingTimeouts               time limits.
 */
public record ResourceAccesses(@Nonnull List<FilePermission> regardingFileSystemInteractions,
		@Nonnull List<NetworkPermission> regardingNetworkConnections,
		@Nonnull List<CommandPermission> regardingCommandExecutions,
		@Nonnull List<ThreadPermission> regardingThreadCreations,
		@Nonnull List<PackagePermission> regardingPackageImports,
		@Nonnull List<ResourceLimitsPermission> regardingTimeouts) {

	/**
	 * Constructs a ResourceAccesses instance with the provided permissions.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException if any list, or any entry within a list, is
	 *                              null.
	 */
	public ResourceAccesses {
		Objects.requireNonNull(regardingFileSystemInteractions, "File system interactions list must not be null");
		Objects.requireNonNull(regardingNetworkConnections, "Network connections list must not be null");
		Objects.requireNonNull(regardingCommandExecutions, "Command executions list must not be null");
		Objects.requireNonNull(regardingThreadCreations, "Thread creations list must not be null");
		Objects.requireNonNull(regardingPackageImports, "Package imports list must not be null");
		Objects.requireNonNull(regardingTimeouts, "Timeout list must not be null");
		// Defensive, unmodifiable copies so the record is genuinely immutable: Jackson
		// and the builder pass mutable lists that a caller could otherwise mutate after
		// construction, and the generated accessors would expose the live reference.
		regardingFileSystemInteractions = List.copyOf(regardingFileSystemInteractions);
		regardingNetworkConnections = List.copyOf(regardingNetworkConnections);
		regardingCommandExecutions = List.copyOf(regardingCommandExecutions);
		regardingThreadCreations = List.copyOf(regardingThreadCreations);
		regardingPackageImports = List.copyOf(regardingPackageImports);
		regardingTimeouts = List.copyOf(regardingTimeouts);
	}

	/**
	 * Creates restrictive resource accesses: every permission list is empty, and
	 * the timeouts hold the limit of
	 * {@link ResourceLimitsPermission#createRestrictive()}, since an empty list
	 * would declare no limit at all. The limit is recorded, not enforced in
	 * Postcompile; use {@code @StrictTimeout} for a deadline.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return resource accesses that permit nothing and record the restrictive
	 *         limit.
	 */
	@Nonnull
	public static ResourceAccesses createRestrictive() {
		return builder().regardingFileSystemInteractions(new ArrayList<>())
				.regardingNetworkConnections(new ArrayList<>()).regardingCommandExecutions(new ArrayList<>())
				.regardingThreadCreations(new ArrayList<>()).regardingPackageImports(new ArrayList<>())
				.regardingTimeouts(new ArrayList<>(List.of(ResourceLimitsPermission.createRestrictive()))).build();
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
	 * <p>
	 * Description: Provides a fluent API to construct a ResourceAccesses instance.
	 * <p>
	 * Design Rationale: The builder pattern here allows for flexible configuration
	 * of resource accesses.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
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
		 * The execution limits.
		 * <p>
		 * The one field that does not default to empty: an empty list would declare no
		 * limit, so it starts at the limit {@link #createRestrictive()} records. A
		 * caller who wants none passes an empty list explicitly. The limit is recorded,
		 * not enforced in Postcompile.
		 */
		@Nullable
		private List<ResourceLimitsPermission> regardingTimeouts = new ArrayList<>(
				List.of(ResourceLimitsPermission.createRestrictive()));

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
			this.regardingFileSystemInteractions = new ArrayList<>(Objects
					.requireNonNull(regardingFileSystemInteractions, "File system interactions list must not be null"));
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
			this.regardingNetworkConnections = new ArrayList<>(
					Objects.requireNonNull(regardingNetworkConnections, "Network connections list must not be null"));
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
			this.regardingCommandExecutions = new ArrayList<>(
					Objects.requireNonNull(regardingCommandExecutions, "Command executions list must not be null"));
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
			this.regardingThreadCreations = new ArrayList<>(
					Objects.requireNonNull(regardingThreadCreations, "Thread creations list must not be null"));
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
			this.regardingPackageImports = new ArrayList<>(
					Objects.requireNonNull(regardingPackageImports, "Package imports list must not be null"));
			return this;
		}

		/**
		 * Sets the execution limits.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param regardingTimeouts the list of execution limits; must not be null.
		 *                          Where it holds several entries, the smallest value
		 *                          applies.
		 * @return the updated Builder.
		 * @throws NullPointerException if the list is null.
		 */
		@Nonnull
		public Builder regardingTimeouts(@Nonnull List<ResourceLimitsPermission> regardingTimeouts) {
			this.regardingTimeouts = new ArrayList<>(
					Objects.requireNonNull(regardingTimeouts, "Timeout list must not be null"));
			return this;
		}

		/**
		 * Builds a new ResourceAccesses instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new ResourceAccesses instance.
		 * @throws NullPointerException if a list was explicitly set to null, or if a
		 *                              list holds a null entry.
		 */
		@Nonnull
		public ResourceAccesses build() {
			return new ResourceAccesses(
					Objects.requireNonNull(regardingFileSystemInteractions,
							"File system interactions list must not be null"),
					Objects.requireNonNull(regardingNetworkConnections, "Network connections list must not be null"),
					Objects.requireNonNull(regardingCommandExecutions, "Command executions list must not be null"),
					Objects.requireNonNull(regardingThreadCreations, "Thread creations list must not be null"),
					Objects.requireNonNull(regardingPackageImports, "Package imports list must not be null"),
					Objects.requireNonNull(regardingTimeouts, "Timeout list must not be null"));
		}
	}
}
