package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Allowed package import.
 * <p>
 * Description: A package the supervised code may import, with the packages
 * below it unless {@code exactMatchOnly} is set, but never a reserved namespace
 * below it that the permission does not name.
 * <p>
 * Design Rationale: Not knowing the language, the record only refuses null. The
 * name is checked where the language is known: in {@link SupervisedCode}, the
 * schema validator and {@code JavaCreator}.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param importTheFollowingPackage the package, or {@code *} for every package.
 * @param exactMatchOnly            whether only this package is covered rather
 *                                  than also the packages below it; it changes
 *                                  nothing for {@code *}.
 */
public record PackagePermission(@Nonnull String importTheFollowingPackage, boolean exactMatchOnly) {

	/**
	 * Roots of the namespace the JDK controls. A supervised class cannot define a
	 * package there, so its own packages never include one.
	 */
	@Nonnull
	private static final java.util.List<String> JDK_NAMESPACE_ROOTS = java.util.List.of("java", "javax", "jdk", "sun",
			"com.sun");

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
	 * Constructs a subpackage-inclusive PackagePermission.
	 * <p>
	 * This is the default for permissions declared in a security policy: granting
	 * {@code java.util} also grants {@code java.util.concurrent}.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param importTheFollowingPackage the package that is permitted, with the
	 *                                  packages below it
	 */
	public PackagePermission(@Nonnull String importTheFollowingPackage) {
		this(importTheFollowingPackage, false);
	}

	/**
	 * Binds a package permission from a policy file. The policy reader refuses a
	 * missing primitive, so the flag is read as a {@code Boolean} and a missing
	 * {@code exactMatchOnly} means {@code false}, as in policies written before the
	 * flag existed.
	 *
	 * @since 2.3.0
	 * @author Markus Paulsen
	 * @param importTheFollowingPackage the package that is permitted
	 * @param exactMatchOnly            whether only this exact package is covered,
	 *                                  or {@code null} when the policy leaves it
	 *                                  out
	 * @return the permission
	 */
	@JsonCreator
	@Nonnull
	public static PackagePermission fromPolicy(
			@JsonProperty("importTheFollowingPackage") @Nonnull String importTheFollowingPackage,
			@JsonProperty("exactMatchOnly") @Nullable Boolean exactMatchOnly) {
		return new PackagePermission(importTheFollowingPackage, Boolean.TRUE.equals(exactMatchOnly));
	}

	/**
	 * Whether a package lies in the namespace the JDK controls: {@code java},
	 * {@code javax}, {@code jdk}, {@code sun}, {@code com.sun} or below them.
	 *
	 * @since 2.3.0
	 * @author Markus Paulsen
	 * @param packageName the package name; must not be null
	 * @return true if the package is in the JDK namespace
	 */
	public static boolean isInJdkNamespace(@Nonnull String packageName) {
		return JDK_NAMESPACE_ROOTS.stream()
				.anyMatch(root -> packageName.equals(root) || packageName.startsWith(root + "."));
	}

	/**
	 * Creates a permission for a package and the packages below it.
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
		 * Whether the permission covers only the exact package; defaults to
		 * subpackage-inclusive.
		 */
		private boolean exactMatchOnly;

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
		 * Sets whether only the exact package is covered.
		 *
		 * @since 2.3.0
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
		 * @throws NullPointerException if no package name was set.
		 */
		@Nonnull
		public PackagePermission build() {
			return new PackagePermission(
					Objects.requireNonNull(importTheFollowingPackage, "importTheFollowingPackage must not be null"),
					exactMatchOnly);
		}
	}
}
