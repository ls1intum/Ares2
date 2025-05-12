package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;

/**
 * Allowed timeout parameters
 * <p>Description: Specifies a value in ms which is the timeout value of the programm execution</p>
 * <p>Design Rationale: Restricting the execution time aids in preventing DDOS attacks and academic dishonesty</p>
 *
 * @param timeout the maximum amount of time of execution that is premitted
 * @author Ajayvir Singh
 * @since 2.0.0
 */

public record ResourceLimitsPermission(long timeout) {

    /**
     * Constructs a TimeoutPermission instance
     *
     * @author Ajayvir Singh
     * @since 2.0.0
     */
    public ResourceLimitsPermission {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
    }

    /**
     * Creates a restrictive timeout permission with all .
     *
     * @return a new TimeoutPermission instance with a restrictive timeout value.
     * @author Markus Paulsen
     * @since 2.0.0
     */
    @Nonnull
    public static ResourceLimitsPermission createRestrictive() {
        return builder().withTimeout(10000).build();
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Builder for TimeoutPermission.
     *
     * <p>Description: Provides a fluent API to construct a TimeoutPermission instance.
     *
     * <p>Design Rationale: This builder enables step-by-step configuration of timeout permission.
     *
     * @author Ajayvir Singh
     * @since 2.0.0
     */
    public static class Builder {
        private long timeout;


        /**
         * Sets the timeout parameter
         *
         * @since 2.0.0
         * @author Ajayvir Singh
         * @param timeout the timeout parameter to set.
         * @return the updated  Builder.
         */
        public Builder withTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }


        /**
         * Builds a new TimeoutPermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new TimeoutPermission instance.
         */
        public ResourceLimitsPermission build() {
            return new ResourceLimitsPermission(timeout);
        }
    }

}
