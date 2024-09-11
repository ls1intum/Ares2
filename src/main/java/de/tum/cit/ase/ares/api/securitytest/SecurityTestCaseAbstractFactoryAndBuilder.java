package de.tum.cit.ase.ares.api.securitytest;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;

/**
 * Factory and builder interface for producing and executing security test cases in any programming language
 * This interface combines elements of the Abstract Factory and Builder design patterns.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 * @since 2.0.0
 */
public interface SecurityTestCaseAbstractFactoryAndBuilder {

    /**
     * Writes the security test cases to files in any programming language.
     *
     * @param projectDirectory the directory where the test case files will be saved
     * @return a list of file paths pointing to the generated test cases. Each path represents a file in the specified directory, and the method will throw an exception if file generation fails.
     */
    @Nonnull List<Path> writeSecurityTestCases(@Nonnull Path projectDirectory);

    /**
     * Executes the security test cases in any programming language.
     * <p>
     * This method will log any failures encountered during execution and may throw exceptions depending on the underlying implementation.
     * </p>
     */
    void executeSecurityTestCases();
}
