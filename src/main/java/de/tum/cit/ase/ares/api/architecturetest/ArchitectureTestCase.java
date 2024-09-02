package de.tum.cit.ase.ares.api.architecturetest;

import com.tngtech.archunit.core.domain.JavaClasses;

import java.nio.file.Path;

/**
 * Interface for the architecture test cases in any programming language and abstract product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public interface ArchitectureTestCase {
    /**
     * Creates the content of the architecture test case file in any programming language.
     *
     * @return Content of the architecture test case file
     */
    String createArchitectureTestCaseFileContent();

    /**
     * Runs the architecture test case in any programming language.
     */
    void runArchitectureTestCase(JavaClasses classes);
}
