package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Class with elevated Privilegues.
 *
 * <p>Description: Specifies the class, which is not restricted by Ares 2.
 *
 * <p>Design Rationale: Explicitly declaring elevated classes enables minimal and restricted opening of the general protection.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param className the name of the class that recieves elevated privilegues; must not be null.
 */
@Nonnull
public record ClassPermission(@Nonnull String className) {

    /**
     * Constructs a ClassPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public ClassPermission {
        Objects.requireNonNull(className, "className name must not be null");
        if (className.isBlank()) {
            throw new IllegalArgumentException("className must not be blank");
        }
    }

    /**
     * Returns a builder for creating a ClassPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new ClassPermission.Builder instance.
     */
    @Nonnull
    public static ClassPermission.Builder builder() {
        return new ClassPermission.Builder();
    }

    /**
     * Builder for ClassPermission.
     *
     * <p>Description: Provides a fluent API to construct a ClassPermission instance.
     *
     * <p>Design Rationale: This builder allows for flexible configuration of class privilegue elevation.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static class Builder {

        /**
         * The class name.
         */
        @Nullable
        private String className;

        /**
         * Sets the class name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param className the class name.
         * @return the updated Builder.
         */
        @Nonnull
        public ClassPermission.Builder className(@Nonnull String className) {
            this.className = Objects.requireNonNull(className, "className must not be null");
            return this;
        }

        /**
         * Builds a new ClassPermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new ClassPermission instance.
         */
        @Nonnull
        public ClassPermission build() {
            return new ClassPermission(Objects.requireNonNull(className, "className must not be null"));
        }
    }
}
