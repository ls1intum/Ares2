package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the essential packages required for the security test.
 *
 * <p>Description: This record encapsulates lists of essential package names for Java, ArchUnit, Wala, AspectJ, Instrumentation, Ares and JUnit.
 * It provides a method to aggregate all these packages into a single list.</p>
 *
 * <p>Design Rationale: Grouping essential package lists into a record leverages immutability and clear data encapsulation,
 * thereby facilitating streamlined configuration and security testing.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
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
        ).collect(Collectors.toList());
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
        private List<String> essentialJavaPackages;

        /**
         * Essential ArchUnit packages.
         */
        private List<String> essentialArchUnitPackages;

        /**
         * Essential Wala packages.
         */
        private List<String> essentialWalaPackages;

        /**
         * Essential AspectJ packages.
         */
        private List<String> essentialAspectJPackages;

        /**
         * Essential Instrumentation packages.
         */
        private List<String> essentialInstrumentationPackages;

        /**
         * Essential Ares packages.
         */
        private List<String> essentialAresPackages;

        /**
         * Essential JUnit packages.
         */
        private List<String> essentialJUnitPackages;

        /**
         * Sets the essential Java packages.
         *
         * @param essentialJavaPackages the essential Java packages
         * @return the Builder instance
         */
        public Builder essentialJavaPackages(List<String> essentialJavaPackages) {
            this.essentialJavaPackages = essentialJavaPackages;
            return this;
        }

        /**
         * Sets the essential ArchUnit packages.
         *
         * @param essentialArchUnitPackages the essential ArchUnit packages
         * @return the Builder instance
         */
        public Builder essentialArchUnitPackages(List<String> essentialArchUnitPackages) {
            this.essentialArchUnitPackages = essentialArchUnitPackages;
            return this;
        }

        /**
         * Sets the essential Wala packages.
         *
         * @param essentialWalaPackages the essential Wala packages
         * @return the Builder instance
         */
        public Builder essentialWalaPackages(List<String> essentialWalaPackages) {
            this.essentialWalaPackages = essentialWalaPackages;
            return this;
        }

        /**
         * Sets the essential AspectJ packages.
         *
         * @param essentialAspectJPackages the essential AspectJ packages
         * @return the Builder instance
         */
        public Builder essentialAspectJPackages(List<String> essentialAspectJPackages) {
            this.essentialAspectJPackages = essentialAspectJPackages;
            return this;
        }

        /**
         * Sets the essential Instrumentation packages.
         *
         * @param essentialInstrumentationPackages the essential Instrumentation packages
         * @return the Builder instance
         */
        public Builder essentialInstrumentationPackages(List<String> essentialInstrumentationPackages) {
            this.essentialInstrumentationPackages = essentialInstrumentationPackages;
            return this;
        }

        /**
         * Sets the essential Ares packages.
         *
         * @param essentialAresPackages the essential Ares packages
         * @return the Builder instance
         */
        public Builder essentialAresPackages(List<String> essentialAresPackages) {
            this.essentialAresPackages = essentialAresPackages;
            return this;
        }

        /**
         * Sets the essential JUnit packages.
         *
         * @param essentialJUnitPackages the essential JUnit packages
         * @return the Builder instance
         */
        public Builder essentialJUnitPackages(List<String> essentialJUnitPackages) {
            this.essentialJUnitPackages = essentialJUnitPackages;
            return this;
        }

        /**
         * Builds the EssentialPackages instance.
         *
         * @return the EssentialPackages instance
         */
        public EssentialPackages build() {
            return new EssentialPackages(
                    essentialJavaPackages,
                    essentialArchUnitPackages,
                    essentialWalaPackages,
                    essentialAspectJPackages,
                    essentialInstrumentationPackages,
                    essentialAresPackages,
                    essentialJUnitPackages
            );
        }
    }
}