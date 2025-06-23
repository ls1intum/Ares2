package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Supervised code details.
 *
 * <p>Description: Contains the details about the supervised code, including its programming language configuration,
 * package information, main class, test classes, and permitted resource accesses.
 *
 * <p>Design Rationale: Encapsulating all aspects of supervised code in an immutable record ensures clarity and consistency.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration used by the code; must not be null.
 * @param theSupervisedCodeUsesTheFollowingPackage the base package used by the code; may be null if not applicable.
 * @param theMainClassInsideThisPackageIs the main class name; may be null if not applicable.
 * @param theFollowingClassesAreTestClasses an array of test class names; must not be null.
 * @param theFollowingResourceAccessesArePermitted the permitted resource accesses; must not be null.
 */
public record SupervisedCode(
        @Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
        @Nullable String theSupervisedCodeUsesTheFollowingPackage,
        @Nullable String theMainClassInsideThisPackageIs,
        @Nonnull String[] theFollowingClassesAreTestClasses,
        @Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted
) {

    /**
     * Constructs a SupervisedCode instance with the provided details.
     *
     * @since 2.0.0
     * @author Markus Paulsen         */
    public SupervisedCode {
        Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed, "ProgrammingLanguageConfiguration must not be null");
        Objects.requireNonNull(theFollowingClassesAreTestClasses, "Test classes array must not be null");
        Objects.requireNonNull(theFollowingResourceAccessesArePermitted, "ResourceAccesses must not be null");
    }

    /**
     * Creates a restrictive supervised code configuration.
     *
     * @since 2.0.0
     * @author Markus Paulsen * @param config the programming language configuration for the restrictive code.
     * @return a new SupervisedCode instance with restrictive settings.
     */
    @Nonnull
    public static SupervisedCode createRestrictive(@Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed) {
        return builder()
                .theFollowingProgrammingLanguageConfigurationIsUsed(Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed, "theFollowingProgrammingLanguageConfigurationIsUsed must not be null"))
                .theSupervisedCodeUsesTheFollowingPackage(null)
                .theMainClassInsideThisPackageIs(null)
                .theFollowingClassesAreTestClasses(new String[0])
                .theFollowingResourceAccessesArePermitted(ResourceAccesses.createRestrictive())
                .build();
    }

    /**
     * Returns a builder for creating a SupervisedCode instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen * @return a new SupervisedCode.Builder instance.
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SupervisedCode.
     *
     * <p>Description: Provides a fluent API to construct a SupervisedCode instance.
     *
     * <p>Design Rationale: The builder pattern allows for flexible and readable construction of a SupervisedCode with various options.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static class Builder {

        /**
         * Constructs a new Builder instance.
         *
         * @since 2.0.0
         */
        @Nullable
        private ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed;

        /**
         * Constructs a new Builder instance.
         *
         * @since 2.0.0
         */
        @Nullable
        private String theSupervisedCodeUsesTheFollowingPackage;

        /* Constructs a new Builder instance.
         *
         * @since 2.0.0
         */
        @Nullable
        private String theMainClassInsideThisPackageIs;

        /* Constructs a new Builder instance.
         *
         * @since 2.0.0
         */
        @Nullable
        private String[] theFollowingClassesAreTestClasses = new String[0];

        /* Constructs a new Builder instance.
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
         * @param theFollowingProgrammingLanguageConfigurationIsUsed the programming language configuration.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder theFollowingProgrammingLanguageConfigurationIsUsed(@Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed) {
            this.theFollowingProgrammingLanguageConfigurationIsUsed = theFollowingProgrammingLanguageConfigurationIsUsed;
            return this;
        }

        /**
         * Sets the package name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param theSupervisedCodeUsesTheFollowingPackage the base package name.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder theSupervisedCodeUsesTheFollowingPackage(@Nullable String theSupervisedCodeUsesTheFollowingPackage) {
            this.theSupervisedCodeUsesTheFollowingPackage = Objects.requireNonNull(theSupervisedCodeUsesTheFollowingPackage, "theSupervisedCodeUsesTheFollowingPackage must not be null");
            return this;
        }

        /**
         * Sets the main class name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param theMainClassInsideThisPackageIs the main class name.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder theMainClassInsideThisPackageIs(@Nullable String theMainClassInsideThisPackageIs) {
            this.theMainClassInsideThisPackageIs = Objects.requireNonNull(theMainClassInsideThisPackageIs, "theMainClassInsideThisPackageIs must not be null");
            return this;
        }

        /**
         * Sets the test classes.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param theFollowingClassesAreTestClasses an array of test class names.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder theFollowingClassesAreTestClasses(@Nonnull String[] theFollowingClassesAreTestClasses) {
            this.theFollowingClassesAreTestClasses = Objects.requireNonNull(theFollowingClassesAreTestClasses, "theFollowingClassesAreTestClasses must not be null");
            return this;
        }

        /**
         * Sets the resource accesses.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param theFollowingResourceAccessesArePermitted the permitted resource accesses.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder theFollowingResourceAccessesArePermitted(@Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted) {
            this.theFollowingResourceAccessesArePermitted = Objects.requireNonNull(theFollowingResourceAccessesArePermitted, "theFollowingResourceAccessesArePermitted must not be null");
            return this;
        }

        /**
         * Builds a new SupervisedCode instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new SupervisedCode instance.
         */
        @Nonnull
        public SupervisedCode build() {
            return new SupervisedCode(
                    Objects.requireNonNull(theFollowingProgrammingLanguageConfigurationIsUsed, "theFollowingProgrammingLanguageConfigurationIsUsed must not be null"),
                    theSupervisedCodeUsesTheFollowingPackage,
                    theMainClassInsideThisPackageIs,
                    Objects.requireNonNull(theFollowingClassesAreTestClasses, "theFollowingClassesAreTestClasses must not be null"),
                    Objects.requireNonNull(theFollowingResourceAccessesArePermitted, "theFollowingResourceAccessesArePermitted must not be null")
            );
        }
    }
}
