package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the essential packages required for the security test.
 *
 * <p>Description: This record encapsulates lists of essential package names for Java, ArchUnit, Wala, AspectJ, Instrumentation, Ares and JUnit.
 * It provides a method to aggregate all these packages into a single list.</p>
 *
 * <p>Design Rationale: Grouping essential package lists into a record leverages immutability and clear data encapsulation,
 * facilitating streamlined configuration and security testing.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @param essentialJavaPackages the essential Java packages
 * @param essentialArchUnitPackages the essential ArchUnit packages
 * @param essentialWalaPackages the essential Wala packages
 * @param essentialAspectJPackages the essential AspectJ packages
 * @param essentialInstrumentationPackages the essential Instrumentation packages
 * @param essentialAresPackages the essential Ares packages
 * @param essentialJUnitPackages the essential JUnit packages
 */
public record EssentialPackages(
        @Nonnull List<String> essentialJavaPackages,
        @Nonnull List<String> essentialArchUnitPackages,
        @Nonnull List<String> essentialWalaPackages,
        @Nonnull List<String> essentialAspectJPackages,
        @Nonnull List<String> essentialInstrumentationPackages,
        @Nonnull List<String> essentialAresPackages,
        @Nonnull List<String> essentialJUnitPackages
) {
    /**
     * Canonical constructor for EssentialPackages.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public EssentialPackages {
        Objects.requireNonNull(essentialJavaPackages, "essentialJavaPackages must not be null");
        Objects.requireNonNull(essentialArchUnitPackages, "essentialArchUnitPackages must not be null");
        Objects.requireNonNull(essentialWalaPackages, "essentialWalaPackages must not be null");
        Objects.requireNonNull(essentialAspectJPackages, "essentialAspectJPackages must not be null");
        Objects.requireNonNull(essentialInstrumentationPackages, "essentialInstrumentationPackages must not be null");
        Objects.requireNonNull(essentialAresPackages, "essentialAresPackages must not be null");
        Objects.requireNonNull(essentialJUnitPackages, "essentialJUnitPackages must not be null");
    }

    /**
     * Retrieves the aggregated list of essential packages.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a List containing all essential packages
     */
    @Nonnull
    public List<String> getEssentialPackages() {
        return Streams.concat(
                essentialJavaPackages.stream(),
                essentialArchUnitPackages.stream(),
                essentialWalaPackages.stream(),
                essentialAspectJPackages.stream(),
                essentialInstrumentationPackages.stream(),
                essentialAresPackages.stream(),
                essentialJUnitPackages.stream()
        ).toList();
    }

    /**
     * Returns a builder for creating an EssentialPackages instance.
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
         * Essential Java packages.
         */
        @Nullable
        private List<String> essentialJavaPackages;

        /**
         * Essential ArchUnit packages.
         */
        @Nullable
        private List<String> essentialArchUnitPackages;

        /**
         * Essential Wala packages.
         */
        @Nullable
        private List<String> essentialWalaPackages;

        /**
         * Essential AspectJ packages.
         */
        @Nullable
        private List<String> essentialAspectJPackages;

        /**
         * Essential Instrumentation packages.
         */
        @Nullable
        private List<String> essentialInstrumentationPackages;

        /**
         * Essential Ares packages.
         */
        @Nullable
        private List<String> essentialAresPackages;

        /**
         * Essential JUnit packages.
         */
        @Nullable
        private List<String> essentialJUnitPackages;

        /**
         * Configures the essential Java packages.
         *
         * @param essentialJavaPackages the essential Java packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialJavaPackages(@Nonnull List<String> essentialJavaPackages) {
            this.essentialJavaPackages = Objects.requireNonNull(essentialJavaPackages, "essentialJavaPackages must not be null");
            return this;
        }

        /**
         * Configures the essential ArchUnit packages.
         *
         * @param essentialArchUnitPackages the essential ArchUnit packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialArchUnitPackages(@Nonnull List<String> essentialArchUnitPackages) {
            this.essentialArchUnitPackages = Objects.requireNonNull(essentialArchUnitPackages, "essentialArchUnitPackages must not be null");
            return this;
        }

        /**
         * Configures the essential Wala packages.
         *
         * @param essentialWalaPackages the essential Wala packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialWalaPackages(@Nonnull List<String> essentialWalaPackages) {
            this.essentialWalaPackages = Objects.requireNonNull(essentialWalaPackages, "essentialWalaPackages must not be null");
            return this;
        }

        /**
         * Configures the essential AspectJ packages.
         *
         * @param essentialAspectJPackages the essential AspectJ packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialAspectJPackages(@Nonnull List<String> essentialAspectJPackages) {
            this.essentialAspectJPackages = Objects.requireNonNull(essentialAspectJPackages, "essentialAspectJPackages must not be null");
            return this;
        }

        /**
         * Configures the essential Instrumentation packages.
         *
         * @param essentialInstrumentationPackages the essential Instrumentation packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialInstrumentationPackages(@Nonnull List<String> essentialInstrumentationPackages) {
            this.essentialInstrumentationPackages = Objects.requireNonNull(essentialInstrumentationPackages, "essentialInstrumentationPackages must not be null");
            return this;
        }

        /**
         * Configures the essential Ares packages.
         *
         * @param essentialAresPackages the essential Ares packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialAresPackages(@Nonnull List<String> essentialAresPackages) {
            this.essentialAresPackages = Objects.requireNonNull(essentialAresPackages, "essentialAresPackages must not be null");
            return this;
        }

        /**
         * Configures the essential JUnit packages.
         *
         * @param essentialJUnitPackages the essential JUnit packages
         * @return the Builder instance
         */
        @Nonnull
        public Builder essentialJUnitPackages(@Nonnull List<String> essentialJUnitPackages) {
            this.essentialJUnitPackages = Objects.requireNonNull(essentialJUnitPackages, "essentialJUnitPackages must not be null");
            return this;
        }

        /**
         * Builds the EssentialPackages instance.
         *
         * @return the EssentialPackages instance
         */
        @Nonnull
        public EssentialPackages build() {
            return new EssentialPackages(
                    Objects.requireNonNull(essentialJavaPackages, "essentialJavaPackages must not be null"),
                    Objects.requireNonNull(essentialArchUnitPackages, "essentialArchUnitPackages must not be null"),
                    Objects.requireNonNull(essentialWalaPackages, "essentialWalaPackages must not be null"),
                    Objects.requireNonNull(essentialAspectJPackages, "essentialAspectJPackages must not be null"),
                    Objects.requireNonNull(essentialInstrumentationPackages, "essentialInstrumentationPackages must not be null"),
                    Objects.requireNonNull(essentialAresPackages, "essentialAresPackages must not be null"),
                    Objects.requireNonNull(essentialJUnitPackages, "essentialJUnitPackages must not be null")
            );
        }
    }
}