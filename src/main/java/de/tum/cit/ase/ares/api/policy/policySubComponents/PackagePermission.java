package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
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
@Nonnull
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
     * @param packageName the package name for which import is restricted.
     * @return a new PackagePermission instance.
     */
    public static PackagePermission createRestrictive(String packageName) {
        return new PackagePermission(packageName);
    }

    /**
     * Returns a builder for creating a PackagePermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new PackagePermission.Builder instance.
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
         * @author Markus Paulsen
         * @param packageName the package name.
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
         * @author Markus Paulsen
         * @return a new PackagePermission instance.
         */
        public PackagePermission build() {
            return new PackagePermission(packageName);
        }
    }
}
