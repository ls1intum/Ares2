package de.tum.cit.ase.ares.api.architectureTest;

/**
 * Interface for the architecture test cases in any programming language and abstract product of the abstract factory design pattern.
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
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
    void runArchitectureTestCase();
}
