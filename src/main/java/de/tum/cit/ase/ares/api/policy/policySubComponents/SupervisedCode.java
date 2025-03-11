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
@Nonnull
public record SupervisedCode(
        @Nonnull ProgrammingLanguageConfiguration theFollowingProgrammingLanguageConfigurationIsUsed,
        @Nullable String theSupervisedCodeUsesTheFollowingPackage,
        @Nullable String theMainClassInsideThisPackageIs,
        @Nonnull String[] theFollowingClassesAreTestClasses,
        @Nonnull ResourceAccesses theFollowingResourceAccessesArePermitted) {

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
     * @author Markus Paulsen         * @param config the programming language configuration for the restrictive code.
     * @return a new SupervisedCode instance with restrictive settings.
     */
    public static SupervisedCode createRestrictive(@Nonnull ProgrammingLanguageConfiguration config) {
        return new SupervisedCode(config, null, null, new String[0], ResourceAccesses.createRestrictive());
    }

    /**
     * Returns a builder for creating a SupervisedCode instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen         * @return a new SupervisedCode.Builder instance.
     */
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

        private ProgrammingLanguageConfiguration configuration;
        private String packageName;
        private String mainClass;
        private String[] testClasses = new String[0];
        private ResourceAccesses resourceAccesses;

        /**
         * Sets the programming language configuration.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param configuration the programming language configuration.
         * @return the updated Builder.
         */
        public Builder configuration(ProgrammingLanguageConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Sets the package name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param packageName the base package name.
         * @return the updated Builder.
         */
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        /**
         * Sets the main class name.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param mainClass the main class name.
         * @return the updated Builder.
         */
        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        /**
         * Sets the test classes.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param testClasses an array of test class names.
         * @return the updated Builder.
         */
        public Builder testClasses(String[] testClasses) {
            this.testClasses = testClasses;
            return this;
        }

        /**
         * Sets the resource accesses.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param resourceAccesses the permitted resource accesses.
         * @return the updated Builder.
         */
        public Builder resourceAccesses(ResourceAccesses resourceAccesses) {
            this.resourceAccesses = resourceAccesses;
            return this;
        }

        /**
         * Builds a new SupervisedCode instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new SupervisedCode instance.
         */
        public SupervisedCode build() {
            return new SupervisedCode(configuration, packageName, mainClass, testClasses, resourceAccesses);
        }
    }
}
