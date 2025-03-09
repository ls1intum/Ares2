package de.tum.cit.ase.ares.api.policy;

import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Security policy file reader and test case director.
 *
 * <p>Description: Handles reading of security policy files and manages creation and execution of security test cases.
 * Acts as a client of the Abstract Factory Design Pattern and a director in the Builder Design Pattern. It reads a security policy
 * from a file, configures the appropriate test case factory and builder, and manages the writing and execution of security test cases.
 *
 * <p>Design Rationale: By separating the responsibilities of file reading and test case management, this class adheres to the
 * Single Responsibility Principle, ensuring that each component handles its specific functionality.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 */
public class SecurityPolicyReaderAndDirector {

    /**
     * The reader for the security policy file.
     */
    @Nonnull
    private final SecurityPolicyReader securityPolicyReader;

    /**
     * The director for creating security test cases based on the security policy.
     */
    @Nonnull
    private final SecurityPolicyDirector securityPolicyDirector;

    /**
     * The path to the security policy file.
     */
    @Nullable
    private final Path securityPolicyPath;

    /**
     * The path within the project where the policy is applied.
     */
    @Nullable
    private final Path projectPath;

    /**
     * The manager for creating and handling security test cases.
     */
    @Nonnull
    private SecurityTestCaseAbstractFactoryAndBuilder testCaseManager;

    /**
     * Constructs a SecurityPolicyReaderAndDirector instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicyReaderAndDirector(
            @Nonnull SecurityPolicyReader securityPolicyReader,
            @Nonnull SecurityPolicyDirector securityPolicyDirector,
            @Nullable Path securityPolicyPath,
            @Nullable Path projectPath
    ) {
        this.securityPolicyReader = Objects.requireNonNull(securityPolicyReader, "securityPolicyReader must not be null");
        this.securityPolicyDirector = Objects.requireNonNull(securityPolicyDirector, "securityPolicyDirector must not be null");
        this.securityPolicyPath = securityPolicyPath;
        this.projectPath = projectPath;
        processSecurityPolicy();
    }

    /**
     * Processes the security policy file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void processSecurityPolicy() {
        Objects.requireNonNull(securityPolicyReader, "securityPolicyReader must not be null");
        SecurityPolicy securityPolicy = this.securityPolicyPath != null
                ? this.securityPolicyReader.readSecurityPolicyFrom(this.securityPolicyPath)
                : null;
        Objects.requireNonNull(this.securityPolicyDirector, "securityPolicyDirector must not be null");
        this.testCaseManager = this.securityPolicyDirector.createSecurityTestCases(securityPolicy, this.projectPath);
    }

    /**
     * Writes the security test cases to the project.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of Paths where the security test cases were written.
     */
    @Nonnull
    public List<Path> writeSecurityTestCases() {
        Objects.requireNonNull(this.testCaseManager, "testCaseManager must not be null");
        return this.testCaseManager.writeSecurityTestCases(this.projectPath);
    }

    /**
     * Executes the security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void executeSecurityTestCases() {
        Objects.requireNonNull(this.securityPolicyPath, "securityPolicyPath must not be null");
        this.testCaseManager.executeSecurityTestCases();
    }
}