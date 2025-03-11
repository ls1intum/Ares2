package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import java.util.Objects;

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
     * @author Markus Paulsen
     * @return a new ThreadPermission.Builder instance.
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
         * @author Markus Paulsen
         * @param numberOfThreads the number of threads.
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
         * @author Markus Paulsen
         * @param threadClass the fully qualified thread class name.
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
         * @author Markus Paulsen
         * @return a new ThreadPermission instance.
         */
        public ThreadPermission build() {
            return new ThreadPermission(numberOfThreads, threadClass);
        }
    }
}
