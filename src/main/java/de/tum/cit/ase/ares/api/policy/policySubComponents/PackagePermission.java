package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Allowed package import.
 * <p>
 * Description: Specifies the package that is permitted to be imported.
 * <p>
 * Design Rationale: Explicitly declaring permitted package imports prevents
 * unauthorised dependencies.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param importTheFollowingPackage the package that is permitted to be
 *                                  imported; must not be null.
 * @param exactMatchOnly            whether the permission covers only this
 *                                  exact package ({@code true}) or the package
 *                                  and its subpackages ({@code false}).
 */
public record PackagePermission(@Nonnull String importTheFollowingPackage, boolean exactMatchOnly) {

	/**
	 * Constructs a PackagePermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public PackagePermission {
		Objects.requireNonNull(importTheFollowingPackage, "Package name must not be null");
		if (importTheFollowingPackage.isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.package.blank"));
		}
	}

	/**
	 * Constructs a subpackage-inclusive PackagePermission.
	 * <p>
	 * This is the default for permissions declared in a security policy: granting
	 * {@code java.util} also grants {@code java.util.concurrent}. Jackson binds
	 * policy YAML through the canonical constructor and supplies {@code false} for
	 * an absent {@code exactMatchOnly} key, so policies written before this flag
	 * existed keep their original meaning.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public PackagePermission(@Nonnull String importTheFollowingPackage) {
		this(importTheFollowingPackage, false);
	}

	/**
	 * Creates a restrictive package permission.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param importTheFollowingPackage the package name for which import is
	 *                                  restricted.
	 * @return a new PackagePermission instance.
	 */
	@Nonnull
	public static PackagePermission createRestrictive(@Nonnull String importTheFollowingPackage) {
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
		 * Whether the permission covers only the exact package; defaults to
		 * subpackage-inclusive.
		 */
		private boolean exactMatchOnly;

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
			this.importTheFollowingPackage = Objects.requireNonNull(importTheFollowingPackage,
					"@Nonnull String importTheFollowingPackage must not be null");
			return this;
		}

		/**
		 * Sets whether only the exact package is covered.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param exactMatchOnly true to exclude subpackages.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder exactMatchOnly(boolean exactMatchOnly) {
			this.exactMatchOnly = exactMatchOnly;
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
			return new PackagePermission(
					Objects.requireNonNull(importTheFollowingPackage, "packageName must not be null"), exactMatchOnly);
		}
	}
}
