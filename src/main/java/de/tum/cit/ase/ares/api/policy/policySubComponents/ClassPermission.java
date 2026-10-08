package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Class with elevated Privileges.
 * <p>
 * Description: Specifies the class, which is not restricted by Ares 2.
 * <p>
 * Design Rationale: Explicitly declaring elevated classes enables minimal and
 * restricted opening of the general protection.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param className the name of the class that receives elevated privileges;
 *                  must not be null.
 */
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
			throw new IllegalArgumentException(Messages.localized("policy.permission.class.blank"));
		}
	}

	/**
	 * Permits an exact class name and its nested classes from the supplied
	 * exemptions.
	 */
	public static boolean isAllowedClass(@Nullable String fullyQualifiedClassName,
			@Nonnull Set<ClassPermission> allowedClasses) {
		if (fullyQualifiedClassName == null || allowedClasses == null || allowedClasses.isEmpty()) {
			return false;
		}
		for (ClassPermission allowed : allowedClasses) {
			String name = allowed.className();
			if (fullyQualifiedClassName.equals(name) || fullyQualifiedClassName.startsWith(name + "$")) {
				return true;
			}
		}
		return false;
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
	 * <p>
	 * Description: Provides a fluent API to construct a ClassPermission instance.
	 * <p>
	 * Design Rationale: This builder allows for flexible configuration of class
	 * privilege elevation.
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
