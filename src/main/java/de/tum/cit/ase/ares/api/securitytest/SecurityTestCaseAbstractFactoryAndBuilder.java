package de.tum.cit.ase.ares.api.securitytest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

/**
 * Security test case factory and builder.
 *
 * <p>Description: Factory and builder interface for producing and executing security test cases in any programming language.
 * This interface combines elements of the Abstract Factory and Builder design patterns to generate, configure, and execute security test cases.
 * Implementations are responsible for creating security test case instances, writing them to files, and executing them according to a specified security policy.
 *
 * <p>Design Rationale: Using a combined factory and builder approach provides flexibility in test case creation and execution,
 * while promoting modular design and adherence to the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface SecurityTestCaseAbstractFactoryAndBuilder {

    /**
     * Writes the generated security test cases to files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectDirectory the target directory where test case files will be saved; may be null.
     * @return a non-null list of Path objects representing the generated test case files.
     */
    @Nonnull
    List<Path> writeSecurityTestCases(@Nullable Path projectDirectory);

    /**
     * Executes the generated security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    void executeSecurityTestCases();
}