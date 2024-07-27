package de.tum.cit.ase.ares.api.architecturetest.java;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

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
    private final Path withinPath;

    /**
     * Constructor for JavaArchitectureTestCase.
     *
     * @param javaSupportedArchitectureTestCase Selects the supported architecture test case in the Java programming language
     */
    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase, Path withinPath) {
        super();
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
        this.withinPath = withinPath;
    }

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    public String createArchitectureTestCaseFileContent() {
        return ArchitectureTestCaseStorage.getArchitectureRuleFileContent(this.javaSupportedArchitectureTestCase.name());
    }

    /**
     * Runs the architecture test case in the Java programming language.
     */
    @Override
    public void runArchitectureTestCase() {
        JavaClasses classes = new ClassFileImporter().importPath((ProjectSourcesFinder.isGradleProject() ? "build/" : "target/") + withinPath.toString()); // TODO: Remove slash (will not work on Windows)
        try {
            switch (this.javaSupportedArchitectureTestCase) {
                case FILESYSTEM_INTERACTION ->
                        JavaArchitectureTestCaseCollection.NO_CLASS_SHOULD_ACCREE_FILE_SYSTEM.check(classes);
                case PACKAGE_IMPORT -> throw new UnsupportedOperationException("Package import not implemented yet");
                case THREAD_CREATION -> throw new UnsupportedOperationException("Thread creation not implemented yet");
                case COMMAND_EXECUTION ->
                        throw new UnsupportedOperationException("Command execution not implemented yet");
                case NETWORK_CONNECTION ->
                        throw new UnsupportedOperationException("Network connection not implemented yet");
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            throw new SecurityException("Ares Security Error (Stage: Execution): Illegal Statement found: " + e.getMessage());
        }
    }
}
