package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Supervised code details.
 * <p>
 * Description: Contains the details about the supervised code, including its
 * programming language configuration, package information, main class, test
 * classes, and permitted resource accesses.
 * <p>
 * Design Rationale: Encapsulating all aspects of supervised code in an
 * immutable record ensures clarity and consistency.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming
 *                                                           language
 *                                                           configuration used
 *                                                           by the code; must
 *                                                           not be null.
 * @param theSupervisedCodeUsesTheFollowingPackage           the base package
 *                                                           used by the code;
 *                                                           may be null if not
 *                                                           applicable.
 * @param theMainClassInsideThisPackageIs                    the main class
 *                                                           name; may be null
 *                                                           if not applicable.
 * @param theFollowingClassesAreTestClasses                  the immutable list
 *                                                           of test class
 *                                                           names; must not be
 *                                                           null.
 * @param theFollowingResourceAccessesArePermitted           the permitted
 *                                                           resource accesses;
 *                                                           must not be null.
 */
public record SupervisedCode(
		@Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
		@Nullable String theSupervisedCodeUsesTheFollowingPackage, @Nullable String theMainClassInsideThisPackageIs,
		@Nonnull List<String> theFollowingClassesAreTestClasses,
		@Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted) {

	/**
	 * Constructs a SupervisedCode instance with the provided details.
	 * <p>
	 * The list of test classes is copied defensively, so a later change to the list
	 * the caller passed in cannot alter which classes this record treats as tests.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException     if the programming language configuration,
	 *                                  the list of test classes, one of its
	 *                                  entries, or the permitted resource accesses
	 *                                  is null.
	 * @throws IllegalArgumentException if the package name, the main class name or
	 *                                  one of the test class names does not have
	 *                                  the shape Java requires of it.
	 */
	public SupervisedCode {
		Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed,
				"ProgrammingLanguageConfiguration must not be null");
		if (theSupervisedCodeUsesTheFollowingPackage != null) {
			PolicyValueValidator.requireMatch("theSupervisedCodeUsesTheFollowingPackage",
					theSupervisedCodeUsesTheFollowingPackage, PolicyValueValidator.JAVA_PACKAGE_PATTERN);
		}
		if (theMainClassInsideThisPackageIs != null) {
			PolicyValueValidator.requireMatch("theMainClassInsideThisPackageIs", theMainClassInsideThisPackageIs,
					PolicyValueValidator.JAVA_CLASS_NAME_PATTERN);
		}
		Objects.requireNonNull(theFollowingClassesAreTestClasses, "Test classes list must not be null");
		for (String testClass : theFollowingClassesAreTestClasses) {
			Objects.requireNonNull(testClass, "Test class entries must not be null");
			PolicyValueValidator.requireMatch("theFollowingClassesAreTestClasses entry", testClass,
					PolicyValueValidator.JAVA_CLASS_PATH_PATTERN);
		}
		Objects.requireNonNull(theFollowingResourceAccessesArePermitted, "ResourceAccesses must not be null");
		theFollowingClassesAreTestClasses = List.copyOf(theFollowingClassesAreTestClasses);
	}

	/**
	 * Creates a restrictive supervised code configuration.
	 * <p>
	 * The result declares no package, no main class and no test classes, and
	 * permits only the resource accesses of
	 * {@link ResourceAccesses#createRestrictive()}.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming
	 *                                                           language
	 *                                                           configuration for
	 *                                                           the restrictive
	 *                                                           code; must not be
	 *                                                           null.
	 * @return a new SupervisedCode instance with restrictive settings.
	 * @throws NullPointerException if the programming language configuration is
	 *                              null.
	 */
	@Nonnull
	public static SupervisedCode createRestrictive(
			@Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed) {
		return builder()
				.theFollowingProgrammingLanguageConfigurationIsUsed(
						Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed,
								"theFollowingProgrammingLanguageConfigurationIsUsed must not be null"))
				.theSupervisedCodeUsesTheFollowingPackage(null).theMainClassInsideThisPackageIs(null)
				.theFollowingClassesAreTestClasses(List.of())
				.theFollowingResourceAccessesArePermitted(ResourceAccesses.createRestrictive()).build();
	}

	/**
	 * Returns a builder for creating a SupervisedCode instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new SupervisedCode.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for SupervisedCode.
	 * <p>
	 * Description: Provides a fluent API to construct a SupervisedCode instance.
	 * <p>
	 * Design Rationale: The builder pattern allows for flexible and readable
	 * construction of a SupervisedCode with various options.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static class Builder {

		/**
		 * The build tool, architecture analyser and aspect weaver the supervised code
		 * is exercised with. Has no default, so {@link #build()} rejects a builder on
		 * which it was never set.
		 *
		 * @since 2.0.0
		 */
		@Nullable
		private ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed;

		/**
		 * The base package of the supervised code, or null when the code declares none.
		 *
		 * @since 2.0.0
		 */
		@Nullable
		private String theSupervisedCodeUsesTheFollowingPackage;

		/**
		 * The simple name of the main class inside that package, or null when the code
		 * has no entry point.
		 *
		 * @since 2.0.0
		 */
		@Nullable
		private String theMainClassInsideThisPackageIs;

		/**
		 * The fully qualified names of the test classes, empty by default.
		 *
		 * @since 2.0.0
		 */
		@Nullable
		private List<String> theFollowingClassesAreTestClasses = List.of();

		/**
		 * The resource accesses the supervised code may perform. Has no default, so
		 * {@link #build()} rejects a builder on which it was never set.
		 *
		 * @since 2.0.0
		 */
		@Nullable
		private ResourceAccesses theFollowingResourceAccessesArePermitted;

		/**
		 * Sets the programming language configuration.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming
		 *                                                           language
		 *                                                           configuration; must
		 *                                                           not be null, which
		 *                                                           {@link #build()}
		 *                                                           enforces.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theFollowingProgrammingLanguageConfigurationIsUsed(
				@Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed) {
			this.theFollowingProgrammingLanguageConfigurationIsUsed = theFollowingProgrammingLanguageConfigurationIsUsed;
			return this;
		}

		/**
		 * Sets the package name.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param theSupervisedCodeUsesTheFollowingPackage the base package name, or
		 *                                                 null when the code declares
		 *                                                 none.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theSupervisedCodeUsesTheFollowingPackage(
				@Nullable String theSupervisedCodeUsesTheFollowingPackage) {
			this.theSupervisedCodeUsesTheFollowingPackage = theSupervisedCodeUsesTheFollowingPackage;
			return this;
		}

		/**
		 * Sets the main class name.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param theMainClassInsideThisPackageIs the simple name of the main class, or
		 *                                        null when the code has no entry point.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theMainClassInsideThisPackageIs(@Nullable String theMainClassInsideThisPackageIs) {
			this.theMainClassInsideThisPackageIs = theMainClassInsideThisPackageIs;
			return this;
		}

		/**
		 * Sets the test classes.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param theFollowingClassesAreTestClasses the fully qualified test class
		 *                                          names; must not be null.
		 * @return the updated Builder.
		 * @throws NullPointerException if the list is null.
		 */
		@Nonnull
		public Builder theFollowingClassesAreTestClasses(@Nonnull List<String> theFollowingClassesAreTestClasses) {
			this.theFollowingClassesAreTestClasses = Objects.requireNonNull(theFollowingClassesAreTestClasses,
					"theFollowingClassesAreTestClasses must not be null");
			return this;
		}

		/**
		 * Sets the resource accesses.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param theFollowingResourceAccessesArePermitted the permitted resource
		 *                                                 accesses; must not be null.
		 * @return the updated Builder.
		 * @throws NullPointerException if the resource accesses are null.
		 */
		@Nonnull
		public Builder theFollowingResourceAccessesArePermitted(
				@Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted) {
			this.theFollowingResourceAccessesArePermitted = Objects.requireNonNull(
					theFollowingResourceAccessesArePermitted,
					"theFollowingResourceAccessesArePermitted must not be null");
			return this;
		}

		/**
		 * Builds a new SupervisedCode instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new SupervisedCode instance.
		 * @throws NullPointerException     if the programming language configuration or
		 *                                  the permitted resource accesses were never
		 *                                  set.
		 * @throws IllegalArgumentException if a name set on this builder does not have
		 *                                  the shape Java requires of it.
		 */
		@Nonnull
		public SupervisedCode build() {
			return new SupervisedCode(
					Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed,
							"theFollowingProgrammingLanguageConfigurationIsUsed must not be null"),
					theSupervisedCodeUsesTheFollowingPackage, theMainClassInsideThisPackageIs,
					Objects.requireNonNull(theFollowingClassesAreTestClasses,
							"theFollowingClassesAreTestClasses must not be null"),
					Objects.requireNonNull(theFollowingResourceAccessesArePermitted,
							"theFollowingResourceAccessesArePermitted must not be null"));
		}
	}
}
