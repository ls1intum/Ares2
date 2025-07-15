package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

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
public record PackagePermission(@Nonnull String importTheFollowingPackage) {

    /**
     * Constructs a PackagePermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
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
     * @author Markus Paulsen
     * @param importTheFollowingPackage the package name for which import is restricted.
     * @return a new PackagePermission instance.
     */
    @Nonnull
    public static PackagePermission createRestrictive(@Nonnull String importTheFollowingPackage) {
        return builder().importTheFollowingPackage(Objects.requireNonNull(importTheFollowingPackage, "importTheFollowingPackage must not be null")).build();
    }

    /**
     * Returns a builder for creating a PackagePermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new PackagePermission.Builder instance.
     */
    @Nonnull
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

        /**
         * The package name.
         */
        @Nullable
        private String importTheFollowingPackage;

        /**
         * Sets the package name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param importTheFollowingPackage the package name.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder importTheFollowingPackage(@Nonnull String importTheFollowingPackage) {
            this.importTheFollowingPackage = Objects.requireNonNull(importTheFollowingPackage, "@Nonnull String importTheFollowingPackage must not be null");
            return this;
        }

        /**
         * Builds a new PackagePermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new PackagePermission instance.
         */
        @Nonnull
        public PackagePermission build() {
            return new PackagePermission(Objects.requireNonNull(importTheFollowingPackage, "packageName must not be null"));
        }
    }
}
