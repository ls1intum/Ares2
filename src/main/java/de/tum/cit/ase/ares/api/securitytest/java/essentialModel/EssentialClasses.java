package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the essential classes required for the security test.
 *
 * <p>Description: This record encapsulates lists of essential class names for Java, Archunit, Wala, AspectJ, Instrumentation, Ares and JUnit.
 * It provides a method to aggregate all these classes into a single list.</p>
 *
 * <p>Design Rationale: Grouping essential class lists into a record leverages immutability and clear data encapsulation,
 * facilitating streamlined configuration and security testing.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @param essentialJavaClasses the essential Java classes
 * @param essentialArchunitClasses the essential Archunit classes
 * @param essentialWalaClasses the essential Wala classes
 * @param essentialAspectJClasses the essential AspectJ classes
 * @param essentialInstrumentationClasses the essential Instrumentation classes
 * @param essentialAresClasses the essential Ares classes
 * @param essentialJUnitClasses the essential JUnit classes
 */
public record EssentialClasses(
        @Nonnull List<String> essentialJavaClasses,
        @Nonnull List<String> essentialArchunitClasses,
        @Nonnull List<String> essentialWalaClasses,
        @Nonnull List<String> essentialAspectJClasses,
        @Nonnull List<String> essentialInstrumentationClasses,
        @Nonnull List<String> essentialAresClasses,
        @Nonnull List<String> essentialJUnitClasses
) {
    /**
     * Canonical constructor for EssentialClasses.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public EssentialClasses {
        Objects.requireNonNull(essentialJavaClasses, "essentialJavaClasses must not be null");
        Objects.requireNonNull(essentialArchunitClasses, "essentialArchunitClasses must not be null");
        Objects.requireNonNull(essentialWalaClasses, "essentialWalaClasses must not be null");
        Objects.requireNonNull(essentialAspectJClasses, "essentialAspectJClasses must not be null");
        Objects.requireNonNull(essentialInstrumentationClasses, "essentialInstrumentationClasses must not be null");
        Objects.requireNonNull(essentialAresClasses, "essentialAresClasses must not be null");
        Objects.requireNonNull(essentialJUnitClasses, "essentialJUnitClasses must not be null");
    }

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
                essentialArchunitClasses.stream(),
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
         * Essential Archunit classes.
         */
        @Nullable
        private List<String> essentialArchunitClasses;

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
         *
         * @param essentialJavaClasses the essential Java classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialJavaClasses(@Nonnull List<String> essentialJavaClasses) {
            this.essentialJavaClasses = Objects.requireNonNull(essentialJavaClasses, "essentialJavaClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Archunit classes.
         *
         * @param essentialArchunitClasses the essential Archunit classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialArchunitClasses(@Nonnull List<String> essentialArchunitClasses) {
            this.essentialArchunitClasses = Objects.requireNonNull(essentialArchunitClasses, "essentialArchunitClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Wala classes.
         *
         * @param essentialWalaClasses the essential Wala classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialWalaClasses(@Nonnull List<String> essentialWalaClasses) {
            this.essentialWalaClasses = Objects.requireNonNull(essentialWalaClasses, "essentialWalaClasses must not be null");
            return this;
        }

        /**
         * Configures the essential AspectJ classes.
         *
         * @param essentialAspectJClasses the essential AspectJ classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialAspectJClasses(@Nonnull List<String> essentialAspectJClasses) {
            this.essentialAspectJClasses = Objects.requireNonNull(essentialAspectJClasses, "essentialAspectJClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Instrumentation classes.
         *
         * @param essentialInstrumentationClasses the essential Instrumentation classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialInstrumentationClasses(@Nonnull List<String> essentialInstrumentationClasses) {
            this.essentialInstrumentationClasses = Objects.requireNonNull(essentialInstrumentationClasses, "essentialInstrumentationClasses must not be null");
            return this;
        }

        /**
         * Configures the essential Ares classes.
         *
         * @param essentialAresClasses the essential Ares classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialAresClasses(@Nonnull List<String> essentialAresClasses) {
            this.essentialAresClasses = Objects.requireNonNull(essentialAresClasses, "essentialAresClasses must not be null");
            return this;
        }

        /**
         * Configures the essential JUnit classes.
         *
         * @param essentialJUnitClasses the essential JUnit classes
         * @return the builder instance
         */
        @Nonnull
        public Builder essentialJUnitClasses(@Nonnull List<String> essentialJUnitClasses) {
            this.essentialJUnitClasses = Objects.requireNonNull(essentialJUnitClasses, "essentialJUnitClasses must not be null");
            return this;
        }

        /**
         * Builds the EssentialClasses instance.
         *
         * @return the EssentialClasses instance
         */
        @Nonnull
        public EssentialClasses build() {
            return new EssentialClasses(
                    Objects.requireNonNull(essentialJavaClasses, "essentialJavaClasses must not be null"),
                    Objects.requireNonNull(essentialArchunitClasses, "essentialArchunitClasses must not be null"),
                    Objects.requireNonNull(essentialWalaClasses, "essentialWalaClasses must not be null"),
                    Objects.requireNonNull(essentialAspectJClasses, "essentialAspectJClasses must not be null"),
                    Objects.requireNonNull(essentialInstrumentationClasses, "essentialInstrumentationClasses must not be null"),
                    Objects.requireNonNull(essentialAresClasses, "essentialAresClasses must not be null"),
                    Objects.requireNonNull(essentialJUnitClasses, "essentialJUnitClasses must not be null")
            );
        }
    }
}