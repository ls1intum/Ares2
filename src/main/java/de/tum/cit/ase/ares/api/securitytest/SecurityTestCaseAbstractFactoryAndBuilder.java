package de.tum.cit.ase.ares.api.securitytest;

import java.nio.file.Path;
import java.util.List;

/**
 * Produces and executes or writes security test cases in any programming language
 * and the abstract factory of the abstract factory design pattern
 * as well as the abstract builder of the builder design pattern.
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
     * @param projectPath Path to the directory where the files should be written to
     * @return List of paths of the written files
     */
    List<Path> writeTestCasesToFiles(Path projectPath);

    /**
     * Runs the security test cases in any programming language.
     */
    void runSecurityTestCases();
}
