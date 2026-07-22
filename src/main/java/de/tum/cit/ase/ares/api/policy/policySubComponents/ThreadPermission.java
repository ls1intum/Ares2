package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Allowed thread creation operations.
 * <p>
 * Description: Specifies how many threads of one kind the supervised code may
 * create. A count of zero names the kind without permitting any of it, which is
 * how a restrictive policy denies it explicitly.
 * <p>
 * Design Rationale: Limiting thread creation is crucial to control concurrency
 * and resource usage. Not every concurrent construct is a named class, so
 * besides a fully qualified class name and {@code *} for every class, the kind
 * may also be one of the tokens Ares uses for concurrency that no class name
 * describes: {@code Lambda-Expression}, and the implicit thread operations
 * {@code <implicit-thread-op:parallelStream>},
 * {@code <implicit-thread-op:parallel>},
 * {@code <implicit-thread-op:Thread.sleep>},
 * {@code <implicit-thread-op:SubmissionPublisher.submit>} and
 * {@code <implicit-thread-op:SubmissionPublisher.offer>}.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param createTheFollowingNumberOfThreads the number of threads permitted to
 *                                          be created; must not be negative.
 * @param ofThisClass                       the kind of thread this permission
 *                                          covers, as a fully qualified class
 *                                          name, {@code *}, or one of the
 *                                          tokens listed above; must not be
 *                                          null.
 */
public record ThreadPermission(int createTheFollowingNumberOfThreads, @Nonnull String ofThisClass) {

	/**
	 * Constructs a ThreadPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException     if the thread class is null.
	 * @throws IllegalArgumentException if the thread count is negative, or if the
	 *                                  thread class is neither a fully qualified
	 *                                  class name nor one of the recognised tokens.
	 */
	public ThreadPermission {
		Objects.requireNonNull(ofThisClass, "Thread class must not be null");
		if (createTheFollowingNumberOfThreads < 0) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.thread.count.negative"));
		}
		PolicyValueValidator.requireMatch("ofThisClass", ofThisClass, PolicyValueValidator.THREAD_CLASS_PATTERN);
	}

	/**
	 * Creates a restrictive thread permission with zero threads allowed.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param threadClass the kind of thread for which no threads are permitted;
	 *                    must not be null.
	 * @return a new ThreadPermission instance with zero threads allowed.
	 * @throws NullPointerException     if the thread class is null.
	 * @throws IllegalArgumentException if the thread class is not valid.
	 */
	@Nonnull
	public static ThreadPermission createRestrictive(@Nonnull String threadClass) {
		return builder().createTheFollowingNumberOfThreads(0)
				.ofThisClass(Objects.requireNonNull(threadClass, "threadClass must not be null")).build();
	}

	/**
	 * Returns a builder for creating a ThreadPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new ThreadPermission.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for ThreadPermission.
	 * <p>
	 * Description: Provides a fluent API to construct a ThreadPermission instance.
	 * <p>
	 * Design Rationale: This builder allows for configurable thread creation
	 * restrictions.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static class Builder {

		/**
		 * The number of threads permitted. Defaults to zero, which permits none.
		 */
		private int createTheFollowingNumberOfThreads;

		/**
		 * The kind of thread this permission covers. Has no default, so
		 * {@link #build()} rejects a builder on which it was never set.
		 */
		@Nullable
		private String threadClass;

		/**
		 * Sets the number of threads permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param createTheFollowingNumberOfThreads the number of threads; must not be
		 *                                          negative, which {@link #build()}
		 *                                          enforces.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder createTheFollowingNumberOfThreads(int createTheFollowingNumberOfThreads) {
			this.createTheFollowingNumberOfThreads = createTheFollowingNumberOfThreads;
			return this;
		}

		/**
		 * Sets the thread class allowed.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param ofThisClass the kind of thread, as a fully qualified class name,
		 *                    {@code *}, or one of the recognised tokens; must not be
		 *                    null.
		 * @return the updated Builder.
		 * @throws NullPointerException if the thread class is null.
		 */
		@Nonnull
		public Builder ofThisClass(@Nonnull String ofThisClass) {
			this.threadClass = Objects.requireNonNull(ofThisClass, "ofThisClass must not be null");
			return this;
		}

		/**
		 * Builds a new ThreadPermission instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new ThreadPermission instance.
		 * @throws NullPointerException     if no thread class was set.
		 * @throws IllegalArgumentException if the thread count is negative or the
		 *                                  thread class is not valid.
		 */
		@Nonnull
		public ThreadPermission build() {
			return new ThreadPermission(createTheFollowingNumberOfThreads,
					Objects.requireNonNull(threadClass, "threadClass must not be null"));
		}
	}
}
