package de.tum.cit.ase.ares.api.policy.director;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Functional interface for directing the creation of security test cases.
 *
 * <p>Description: This interface defines the contract for a director that creates security test cases based on a given SecurityPolicy.
 * It abstracts the instantiation and configuration process to a concrete implementation, working in tandem with an abstract factory
 * to ensure that test cases are built in a consistent and modular manner.
 *
 * <p>Design Rationale: This interface supports loose coupling by decoupling the test case creation logic from other system components.
 * Its usage ensures adherence to the Single Responsibility Principle and makes the security test case generation process transparent and maintainable.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
@FunctionalInterface
public interface SecurityPolicyDirector {

    /**
     * Creates and configures security test cases based on the provided SecurityPolicy and project path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicy the SecurityPolicy driving the test case creation; may be null if no policy is provided.
     * @param projectPath the project directory path where test cases should be applied; may be null.
     * @return a non-null instance of SecurityTestCaseAbstractFactoryAndBuilder configured according to the security policy.
     */
    @Nonnull
    SecurityTestCaseAbstractFactoryAndBuilder createSecurityTestCases(@Nullable SecurityPolicy securityPolicy,
                                                                      @Nullable Path projectPath);
}