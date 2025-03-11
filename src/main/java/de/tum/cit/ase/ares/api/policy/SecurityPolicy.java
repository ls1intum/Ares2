package de.tum.cit.ase.ares.api.policy;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Immutable security policy for supervised code execution.
 *
 * <p>Description: This record encapsulates all necessary details regarding supervised code, including its programming language
 * configuration, permitted resource accesses, and additional metadata. By using the immutable record pattern, it guarantees
 * thread-safety and reduces boilerplate. Input validation is performed in factory methods to ensure that every instance meets its invariants.
 *
 * <p>Design Rationale: Leveraging modern Java features such as records and nullability annotations enforces immutability and clarity.
 * The use of a factory method for construction and in‑constructor validation ensures that only valid, consistent instances are created,
 * aligning with best practices for secure and maintainable design. The clear separation into nested records reflects the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param regardingTheSupervisedCode the details of the supervised code; must not be null.
 */
@SuppressWarnings("unused")
@Nonnull
public record SecurityPolicy(@Nonnull SupervisedCode regardingTheSupervisedCode) {

    /**
     * Constructs a SecurityPolicy instance with validated supervised code.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicy {
        Objects.requireNonNull(regardingTheSupervisedCode, "Supervised code must not be null");
    }

    /**
     * Creates a restrictive security policy with all permissions denied by default.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param programmingLanguageConfiguration the programming language configuration for the restrictive policy.
     * @return a new SecurityPolicy instance.
     */
    public static SecurityPolicy createRestrictive(ProgrammingLanguageConfiguration programmingLanguageConfiguration) {
        return new SecurityPolicy(SupervisedCode.createRestrictive(programmingLanguageConfiguration));
    }

    /**
     * Returns a builder for creating a SecurityPolicy instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new SecurityPolicy.Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SecurityPolicy.
     *
     * <p>Description: Provides a fluent API to construct a SecurityPolicy instance.
     *
     * <p>Design Rationale: The builder pattern here allows for step-by-step configuration of a SecurityPolicy, ensuring immutability.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @version 2.0.0
     */
    public static class Builder {

        private SupervisedCode supervisedCode;

        /**
         * Sets the supervised code for the SecurityPolicy.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param supervisedCode the supervised code instance.
         * @return the updated Builder.
         */
        public Builder supervisedCode(SupervisedCode supervisedCode) {
            this.supervisedCode = supervisedCode;
            return this;
        }

        /**
         * Builds a new SecurityPolicy instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new SecurityPolicy instance.
         */
        public SecurityPolicy build() {
            if (supervisedCode == null) {
                throw new IllegalStateException("Supervised code must be set");
            }
            return new SecurityPolicy(supervisedCode);
        }
    }
}