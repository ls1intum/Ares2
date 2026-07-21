package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allowed package import.
 * <p>
 * Description: Names a package the supervised code may import, covering that
 * package and everything below it.
 * <p>
 * Design Rationale: Explicitly declaring permitted package imports prevents
 * unauthorised dependencies. The name is validated on construction, so a
 * malformed entry is rejected when the policy is read rather than silently
 * matching nothing during enforcement.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param importTheFollowingPackage the package that is permitted to be
 *                                  imported, or {@code *} for every package;
 *                                  must not be null.
 */
public record PackagePermission(@Nonnull String importTheFollowingPackage) {

	/**
	 * Constructs a PackagePermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException     if the package name is null.
	 * @throws IllegalArgumentException if the package name is neither a
	 *                                  dot-separated Java package name nor
	 *                                  {@code *}.
	 */
	public PackagePermission {
		Objects.requireNonNull(importTheFollowingPackage, "Package name must not be null");
		PolicyValueValidator.requirePackageImport(importTheFollowingPackage);
	}

	/**
	 * Allows importing a package.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param importTheFollowingPackage the package name to allow, or {@code *} for
	 *                                  every package; must not be null.
	 * @return a new PackagePermission instance.
	 * @throws NullPointerException     if the package name is null.
	 * @throws IllegalArgumentException if the package name is not valid.
	 */
	@Nonnull
	public static PackagePermission allowPackage(@Nonnull String importTheFollowingPackage) {
		return builder()
				.importTheFollowingPackage(
						Objects.requireNonNull(importTheFollowingPackage, "importTheFollowingPackage must not be null"))
				.build();
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
	 * <p>
	 * Description: Provides a fluent API to construct a PackagePermission instance.
	 * <p>
	 * Design Rationale: This builder allows for flexible configuration of package
	 * import permissions.
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
		 * @param importTheFollowingPackage the package name; must not be null, and is
		 *                                  validated for shape by {@link #build()}.
		 * @return the updated Builder.
		 * @throws NullPointerException if the package name is null.
		 */
		@Nonnull
		public Builder importTheFollowingPackage(@Nonnull String importTheFollowingPackage) {
			this.importTheFollowingPackage = Objects.requireNonNull(importTheFollowingPackage,
					"importTheFollowingPackage must not be null");
			return this;
		}

		/**
		 * Builds a new PackagePermission instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new PackagePermission instance.
		 * @throws NullPointerException     if no package name was set.
		 * @throws IllegalArgumentException if the package name is not valid.
		 */
		@Nonnull
		public PackagePermission build() {
			return new PackagePermission(
					Objects.requireNonNull(importTheFollowingPackage, "importTheFollowingPackage must not be null"));
		}
	}
}
