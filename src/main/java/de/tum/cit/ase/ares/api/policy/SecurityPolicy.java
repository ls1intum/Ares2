package de.tum.cit.ase.ares.api.policy;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public record SecurityPolicy(@Nonnull SupervisedCode regardingTheSupervisedCode) {

    /**
     * Constructs a SecurityPolicy instance with validated supervised code.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicy {
        Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null");
    }

    /**
     * Creates a restrictive security policy with all permissions denied by default.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param programmingLanguageConfiguration the programming language configuration for the restrictive policy.
     * @return a new SecurityPolicy instance.
     */
    @Nonnull
    public static SecurityPolicy createRestrictive(@Nonnull ProgrammingLanguageConfiguration programmingLanguageConfiguration) {
        return builder().regardingTheSupervisedCode(SupervisedCode.createRestrictive(Objects.requireNonNull(programmingLanguageConfiguration, "programmingLanguageConfiguration must not be null"))).build();
    }

    /**
     * Returns a builder for creating a SecurityPolicy instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new SecurityPolicy.Builder instance.
     */
    @Nonnull
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

        /**
         * The supervised code for the SecurityPolicy.
         */
        @Nullable
        private SupervisedCode regardingTheSupervisedCode;

        /**
         * Sets the supervised code for the SecurityPolicy.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param regardingTheSupervisedCode the supervised code instance.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder regardingTheSupervisedCode(@Nonnull SupervisedCode regardingTheSupervisedCode) {
            this.regardingTheSupervisedCode = Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null");
            return this;
        }

        /**
         * Builds a new SecurityPolicy instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new SecurityPolicy instance.
         */
        @Nonnull
        public SecurityPolicy build() {
            return new SecurityPolicy(Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null"));
        }
    }
}