package de.tum.cit.ase.ares.api.architecturetest;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.nio.file.Path;

/**
 * Architecture test case for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaArchitectureTestCase implements ArchitectureTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase;
    /**
     * Security policy for the architecture test case.
     */
    private final SecurityPolicy securityPolicy;

    private final Path withinPath;

    /**
     * Constructor for JavaArchitectureTestCase.
     *
     * @param javaSupportedArchitectureTestCase Selects the supported architecture test case in the Java programming language
     * @param securityPolicy                    Security policy for the architecture test case
     * @param withinPath
     */
    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase, SecurityPolicy securityPolicy, Path withinPath) {
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
        this.securityPolicy = securityPolicy;
        this.withinPath = withinPath;
    }

    /**
     * Creates the content of the architecture test case file in the Java programming language.
     *
     * @return Content of the architecture test case file
     */
    @Override
    public String createArchitectureTestCaseFileContent() {
        throw new RuntimeException("Not implemented yet!");
    }

    /**
     * Runs the architecture test case in the Java programming language.
     */
    @Override
    public void runArchitectureTestCase() {
        throw new RuntimeException("Not implemented yet!");
    }
}
