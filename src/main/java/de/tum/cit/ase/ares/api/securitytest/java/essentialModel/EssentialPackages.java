package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;

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
}