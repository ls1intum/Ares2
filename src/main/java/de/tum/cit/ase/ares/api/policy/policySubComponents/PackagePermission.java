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
 * unauthorised dependencies. Only the universal invariant, that the name is not
 * null, is checked on construction. Whether it is a package name or {@code *}
 * for the supervised code's language is validated where the language is known:
 * the schema validator on the policy-file path, and the language-specific
 * component that scans the project (currently {@code JavaCreator}) on the
 * derived path.
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
	 * @throws NullPointerException if the package name is null.
	 */
	public PackagePermission {
		Objects.requireNonNull(importTheFollowingPackage, "Package name must not be null");
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
