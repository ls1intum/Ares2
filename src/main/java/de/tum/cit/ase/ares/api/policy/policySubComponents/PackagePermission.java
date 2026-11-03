package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allowed package import.
 * <p>
 * Description: A package the supervised code may import, with everything below
 * it.
 * <p>
 * Design Rationale: Not knowing the language, the record only refuses null. The
 * name is checked where the language is known: in {@link SupervisedCode}, the
 * schema validator and {@code JavaCreator}.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param importTheFollowingPackage the package, or {@code *} for every package.
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
	 * @throws NullPointerException if the package name is null.
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
		 * @param importTheFollowingPackage the package name; must not be null.
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
		 * @throws NullPointerException if no package name was set.
		 */
		@Nonnull
		public PackagePermission build() {
			return new PackagePermission(
					Objects.requireNonNull(importTheFollowingPackage, "importTheFollowingPackage must not be null"));
		}
	}
}
