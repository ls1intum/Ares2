package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resource accesses permitted for the supervised code.
 * <p>
 * Description: Specifies the file system interactions, network connections,
 * command executions, thread creations, package imports and execution limits
 * permitted. Every list is exhaustive: an access that no entry permits is
 * denied, so an empty list denies that kind of access entirely.
 * <p>
 * Design Rationale: Encapsulating allowed resource accesses in a separate
 * record facilitates granular control over code execution security. Each list
 * is copied defensively and exposed unmodifiable, so no caller can widen a
 * policy after it has been read.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param regardingFileSystemInteractions permitted file system interactions;
 *                                        must not be null.
 * @param regardingNetworkConnections     permitted network connections; must
 *                                        not be null.
 * @param regardingCommandExecutions      permitted command executions; must not
 *                                        be null.
 * @param regardingThreadCreations        permitted thread creations; must not
 *                                        be null.
 * @param regardingPackageImports         permitted package imports; must not be
 *                                        null.
 * @param regardingTimeouts               permitted execution limits; must not
 *                                        be null. This list is the one
 *                                        exception to the rule above: it does
 *                                        not permit accesses but bounds them,
 *                                        and where it holds several entries the
 *                                        smallest value applies.
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
	 * Creates restrictive resource accesses with every access denied.
	 * <p>
	 * Every permission list is empty, with the sole exception of the execution
	 * limit: see the comment in the method body and
	 * {@link ResourceLimitsPermission#createRestrictive()} for why denying that one
	 * outright would be the less restrictive choice.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new ResourceAccesses instance that permits no access and bounds the
	 *         execution time.
	 */
	@Nonnull
	public static ResourceAccesses createRestrictive() {
		// A restrictive policy denies every resource but must still enforce the
		// anti-DoS execution timeout; an empty timeout list would leave the no-policy
		// fallback with no time limit, contrary to ResourceLimitsPermission's own
		// restrictive default.
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
		 * This is the one field that does not default to empty. For the five permission
		 * lists, empty means "permit nothing", which is the safe reading. For this one
		 * it would mean "run without a time limit", so an unset field would relax the
		 * policy rather than tighten it. It therefore starts at the same limit
		 * {@link #createRestrictive()} grants. A caller who genuinely wants no limit
		 * can still pass an empty list, which makes that an explicit choice rather than
		 * an omission.
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
