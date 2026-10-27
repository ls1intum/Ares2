package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Class with elevated privileges.
 * <p>
 * Description: Names a class that Ares 2 does not restrict. A call is judged by
 * the call stack that leads to it, so naming a class here exempts the frames it
 * contributes from the enforcement that applies to the supervised code.
 * <p>
 * Unlike its siblings in this package, this permission is not read from a
 * policy file. The security policy schema has no field for it. Ares derives the
 * set itself from the classes that must stay reachable for the run to work at
 * all, namely the essential framework classes and the exercise's own test
 * classes, so an instructor can neither grant nor revoke it directly.
 * <p>
 * Design Rationale: Explicitly declaring elevated classes enables minimal and
 * restricted opening of the general protection.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param className the name of the class that receives elevated privileges;
 *                  must be neither null nor blank.
 */
public record ClassPermission(@Nonnull String className) {

	/**
	 * Constructs a ClassPermission, refusing only a null or blank name. The record
	 * does not know the language, so {@code JavaCreator} checks the Java syntax
	 * first. Elevation covers exactly the named class; a nested class is elevated
	 * only because {@link ExemptedClassNests} lists it too.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param className the elevated class; neither null nor blank.
	 * @throws NullPointerException     if the name is null.
	 * @throws IllegalArgumentException if the name is blank.
	 */
	public ClassPermission {
		Objects.requireNonNull(className, "className must not be null");
		if (className.isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.class.blank"));
		}
	}

	/**
	 * Permits a class only if its exact binary name is exempted. Classes declared
	 * inside an exempted class are exempted by name too, because
	 * {@link ExemptedClassNests} lists them from that class's own file; a class
	 * merely named like a nested one is not.
	 *
	 * @param fullyQualifiedClassName the class to check, or {@code null}.
	 * @param allowedClasses          the exempted classes.
	 * @return {@code true} if exactly this class is exempted.
	 */
	public static boolean isAllowedClass(@Nullable String fullyQualifiedClassName,
			@Nonnull Set<ClassPermission> allowedClasses) {
		if (fullyQualifiedClassName == null || allowedClasses == null || allowedClasses.isEmpty()) {
			return false;
		}
		for (ClassPermission allowed : allowedClasses) {
			if (fullyQualifiedClassName.equals(allowed.className())) {
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
		 * @param className the class name; must not be null.
		 * @return the updated Builder.
		 * @throws NullPointerException if the class name is null.
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
		 * @throws NullPointerException     if no class name was set.
		 * @throws IllegalArgumentException if the class name is blank.
		 */
		@Nonnull
		public ClassPermission build() {
			return new ClassPermission(Objects.requireNonNull(className, "className must not be null"));
		}
	}
}
