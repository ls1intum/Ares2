package de.tum.cit.ase.ares.api.architecturetest.java;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.PackageImport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecturetest.java.postcompile.JavaArchitectureTestCaseCollection.getArchitectureRuleFileContent;

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
     * List of allowed packages to be imported.
     */
    private final Set<String> allowedPackages;

    /**
     * Constructor for JavaArchitectureTestCase.
     *
     * @param javaSupportedArchitectureTestCase Selects the supported architecture test case in the Java programming language
     */
    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase) {
        super();
        this.allowedPackages = new HashSet<>();
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
    }

    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase, Set<PackageImport> packages) {
        super();
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
        this.allowedPackages = packages.stream().map(PackageImport::iAllowTheStudentsToImportTheFollowingPackage).collect(Collectors.toSet());
    }

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    public String createArchitectureTestCaseFileContent() {
        try {
            return getArchitectureRuleFileContent(this.javaSupportedArchitectureTestCase.name());
        } catch (AssertionError e) {
            throw new SecurityException("Ares Security Error (Stage: Execution): Illegal Statement found: " + e.getMessage());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the architecture rule file content", e);
        }
    }

    /**
     * Runs the architecture test case in the Java programming language.
     */
    @Override
    public void runArchitectureTestCase(JavaClasses classes) {
        try {
            switch (this.javaSupportedArchitectureTestCase) {
                case FILESYSTEM_INTERACTION ->
                        JavaArchitectureTestCaseCollection.NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM.check(classes);
                case PACKAGE_IMPORT ->  JavaArchitectureTestCaseCollection.noClassesShouldImportForbiddenPackages(allowedPackages).check(classes);
                case THREAD_CREATION -> throw new UnsupportedOperationException("Thread creation not implemented yet");
                case COMMAND_EXECUTION ->
                        throw new UnsupportedOperationException("Command execution not implemented yet");
                case NETWORK_CONNECTION ->
                        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_ACCESS_NETWORK.check(classes);
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            throw new SecurityException("Ares Security Error (Stage: Execution): Illegal Statement found: " + e.getMessage());
        }
    }
}
