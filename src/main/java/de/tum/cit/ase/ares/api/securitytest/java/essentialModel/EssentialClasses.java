package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the essential classes required for the security test.
 *
 * <p>Description: This record encapsulates lists of essential classes for Java, ArchUnit, Wala, AspectJ, Instrumentation, Ares and JUnit.
 * It provides a method to aggregate all these classes into a single list.</p>
 *
 * <p>Design Rationale: Grouping essential classes in a record leverages immutability and clear data encapsulation,
 * facilitating streamlined configuration and security testing.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @param essentialJavaClasses the essential Java classes
 * @param essentialArchUnitClasses the essential ArchUnit classes
 * @param essentialWalaClasses the essential Wala classes
 * @param essentialAspectJClasses the essential AspectJ classes
 * @param essentialInstrumentationClasses the essential Instrumentation classes
 * @param essentialAresClasses the essential Ares classes
 * @param essentialJUnitClasses the essential JUnit classes
 */
public record EssentialClasses(
        @Nonnull List<String> essentialJavaClasses,
        @Nonnull List<String> essentialArchUnitClasses,
        @Nonnull List<String> essentialWalaClasses,
        @Nonnull List<String> essentialAspectJClasses,
        @Nonnull List<String> essentialInstrumentationClasses,
        @Nonnull List<String> essentialAresClasses,
        @Nonnull List<String> essentialJUnitClasses
) {
    /**
     * Retrieves the aggregated list of essential classes.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a List containing all essential classes
     */
    @Nonnull
    public List<String> getEssentialClasses() {
        return Streams.concat(
                essentialJavaClasses.stream(),
                essentialArchUnitClasses.stream(),
                essentialWalaClasses.stream(),
                essentialAspectJClasses.stream(),
                essentialInstrumentationClasses.stream(),
                essentialAresClasses.stream(),
                essentialJUnitClasses.stream()
        ).toList();
    }

    /**
     * Returns a builder for creating an EssentialClasses instance.
     *
     * @since 2.0.0
     * @return a new builder instance
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /**
         * Essential Java classes.
         */
        @Nullable
        private List<String> essentialJavaClasses;

        /**
         * Essential ArchUnit classes.
         */
        @Nullable
        private List<String> essentialArchUnitClasses;

        /**
         * Essential Wala classes.
         */
        @Nullable
        private List<String> essentialWalaClasses;

        /**
         * Essential AspectJ classes.
         */
        @Nullable
        private List<String> essentialAspectJClasses;

        /**
         * Essential Instrumentation classes.
         */
        @Nullable
        private List<String> essentialInstrumentationClasses;

        /**
         * Essential Ares classes.
         */
        @Nullable
        private List<String> essentialAresClasses;

        /**
         * Essential JUnit classes.
         */
        @Nullable
        private List<String> essentialJUnitClasses;

        /**
         * Configures the essential Java classes.
         */
        @Nonnull
        public Builder essentialJavaClasses(@Nonnull List<String> essentialJavaClasses) {
            this.essentialJavaClasses = Objects.requireNonNull(essentialJavaClasses, "essentialJavaClasses must not be null");
            return this;
        }

        /**
         * Configures the essential ArchUnit classes.
         */
        @Nonnull
        public Builder essentialArchUnitClasses(@Nonnull List<String> essentialArchUnitClasses) {
            this.essentialArchUnitClasses = Objects.requireNonNull(essentialArchUnitClasses, "essentialArchUnitClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Wala classes.
         */
        @Nonnull
        public Builder essentialWalaClasses(@Nonnull List<String> essentialWalaClasses) {
            this.essentialWalaClasses = Objects.requireNonNull(essentialWalaClasses, "essentialWalaClasses must not be null");
            return this;
        }

        /**
         * Configures the essential AspectJ classes.
         */
        @Nonnull
        public Builder essentialAspectJClasses(@Nonnull List<String> essentialAspectJClasses) {
            this.essentialAspectJClasses = Objects.requireNonNull(essentialAspectJClasses, "essentialAspectJClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Instrumentation classes.
         */
        @Nonnull
        public Builder essentialInstrumentationClasses(@Nonnull List<String> essentialInstrumentationClasses) {
            this.essentialInstrumentationClasses = Objects.requireNonNull(essentialInstrumentationClasses, "essentialInstrumentationClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Ares classes.
         */
        @Nonnull
        public Builder essentialAresClasses(@Nonnull List<String> essentialAresClasses) {
            this.essentialAresClasses = Objects.requireNonNull(essentialAresClasses, "essentialAresClasses must not be null");
            return this;
        }

        /**
         * Configures the essential JUnit classes.
         */
        @Nonnull
        public Builder essentialJUnitClasses(@Nonnull List<String> essentialJUnitClasses) {
            this.essentialJUnitClasses = Objects.requireNonNull(essentialJUnitClasses, "essentialJUnitClasses must not be null");
            return this;
        }

        /**
         * Builds the EssentialClasses instance.
         */
        @Nonnull
        public EssentialClasses build() {
            return new EssentialClasses(
                    Objects.requireNonNull(essentialJavaClasses, "essentialJavaClasses must not be null"),
                    Objects.requireNonNull(essentialArchUnitClasses, "essentialArchUnitClasses must not be null"),
                    Objects.requireNonNull(essentialWalaClasses, "essentialWalaClasses must not be null"),
                    Objects.requireNonNull(essentialAspectJClasses, "essentialAspectJClasses must not be null"),
                    Objects.requireNonNull(essentialInstrumentationClasses, "essentialInstrumentationClasses must not be null"),
                    Objects.requireNonNull(essentialAresClasses, "essentialAresClasses must not be null"),
                    Objects.requireNonNull(essentialJUnitClasses, "essentialJUnitClasses must not be null")
            );
        }
    }
}