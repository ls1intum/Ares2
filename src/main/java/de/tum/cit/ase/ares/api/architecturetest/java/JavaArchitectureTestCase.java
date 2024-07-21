package de.tum.cit.ase.ares.api.architecturetest.java;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCaseStorage;
import de.tum.cit.ase.ares.api.architecturetest.java.postcompile.SecurityRules;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

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
    private JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase;

    /**
     * Constructor for JavaArchitectureTestCase.
     *
     * @param javaSupportedArchitectureTestCase Selects the supported architecture test case in the Java programming language
     */
    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase) {
        super();
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
    }

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    public String createArchitectureTestCaseFileContent() {
        return ArchitectureTestCaseStorage.ARCHITECTURAL_RULES_CONTENT_MAP.build().get(this.javaSupportedArchitectureTestCase.name());
    }

    /**
     * Runs the architecture test case in the Java programming language.
     */
    @Override
    public void runArchitectureTestCase() {
        JavaClasses classes = new ClassFileImporter().importPath(ProjectSourcesFinder.isGradleProject() ? "build/classes" : "target/classes");
        switch (this.javaSupportedArchitectureTestCase) {
            case FILESYSTEM_INTERACTION -> SecurityRules.FILE_SYSTEM_INTERACTION_RULE.check(classes);
            case PACKAGE_IMPORT -> throw new UnsupportedOperationException("Package import not implemented yet");
            case THREAD_CREATION -> throw new UnsupportedOperationException("Thread creation not implemented yet");
            case COMMAND_EXECUTION -> throw new UnsupportedOperationException("Command execution not implemented yet");
            case NETWORK_CONNECTION -> throw new UnsupportedOperationException("Network connection not implemented yet");
            default -> throw new UnsupportedOperationException("Not implemented yet");
        }
    }
}
