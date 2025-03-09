package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import com.google.common.collect.Streams;

import javax.annotation.Nonnull;
import java.util.List;

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
}