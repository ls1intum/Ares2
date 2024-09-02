package de.tum.cit.ase.ares.api.architecturetest.java.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.archunit.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecturetest.java.archunit.postcompile.JavaArchitectureTestCaseCollection.getArchitectureRuleFileContent;

/**
 * Architecture test case for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaArchUnitTestCase implements ArchitectureTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    private final Set<String> allowedPackages;

    /**
     * Constructor for JavaArchUnitTestCase.
     *
     * @param javaArchitectureTestCaseSupported Selects the supported architecture test case in the Java programming language
     */
    public JavaArchUnitTestCase(JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported) {
        super();
        this.allowedPackages = new HashSet<>();
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
    }

    public JavaArchUnitTestCase(JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported, Set<PackagePermission> packages) {
        super();
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.allowedPackages = packages.stream().map(PackagePermission::importTheFollowingPackage).collect(Collectors.toSet());
    }

    public static String createArchitectureTestCaseFileFullContent(List<JavaArchUnitTestCase> javaArchUnitTestCases) {
        return String.join("\n", javaArchUnitTestCases.stream().map(javaArchUnitTestCase -> javaArchUnitTestCase.createArchitectureTestCaseFileContent()).toList());
    }

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    public String createArchitectureTestCaseFileContent() {
        try {
            return getArchitectureRuleFileContent(this.javaArchitectureTestCaseSupported.name());
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
            switch (this.javaArchitectureTestCaseSupported) {
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
