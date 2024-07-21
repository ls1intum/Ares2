package de.tum.cit.ase.ares.api.securitytest;

import de.tum.cit.ase.ares.api.architecturetest.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaAspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Produces and executes or writes security test cases in the Java programming language
 * and the concrete factory of the abstract factory design pattern
 * as well as the concrete builder of the builder design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 * @since 2.0.0
 */
public class JavaSecurityTestCaseFactoryAndBuilder implements SecurityTestCaseAbstractFactoryAndBuilder {

    /**
     * Security policy for the security test cases
     */
    SecurityPolicy securityPolicy;
    /**
     * List of Java architecture test cases
     */
    List<JavaArchitectureTestCase> javaArchitectureTestCases;
    /**
     * List of Java aspect configurations
     */
    List<JavaAspectConfiguration> javaAspectConfigurations;

    /**
     * Constructor for the Java security test case factory and builder.
     *
     * @param securityPolicy Security policy for the security test cases
     */
    public JavaSecurityTestCaseFactoryAndBuilder(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
        javaArchitectureTestCases = new ArrayList<>();
        javaAspectConfigurations = new ArrayList<>();
    }

    /**
     * Writes the security test cases to files in the Java programming language.
     *
     * @param path Path to the directory where the files should be written to
     * @return List of paths of the written files
     */
    @Override
    public List<Path> writeTestCasesToFiles(Path path) {
        try {
            Path architectureTestCaseFile = Files.createFile(path.resolve("ArchitectureTestCase.java"));
            Files.writeString(architectureTestCaseFile,
                    """
                            public class ArchitectureTestCase {
                            
                            """ + String.join(
                            "\n",
                            javaArchitectureTestCases
                                    .stream()
                                    .map(JavaArchitectureTestCase::createArchitectureTestCaseFileContent)
                                    .toList()
                    ) + """
                            
                            }
                            """, StandardOpenOption.WRITE);
            Path aspectConfigurationFile = Files.createFile(path.resolve("AspectConfiguration.java"));
            Files.writeString(aspectConfigurationFile,
                    """
                            """ + String.join(
                            "\n",
                            javaAspectConfigurations
                                    .stream()
                                    .map(JavaAspectConfiguration::createAspectConfigurationFileContent)
                                    .toList()
                    ) + """
                            """, StandardOpenOption.WRITE);
            return List.of(architectureTestCaseFile, aspectConfigurationFile);
        } catch (Exception e) {
            throw new SecurityException("Creating test case file failed.");
        }
    }

    /**
     * Runs the security test cases in the Java programming language.
     */
    @Override
    public void runSecurityTestCases() {
        javaArchitectureTestCases.forEach(JavaArchitectureTestCase::runArchitectureTestCase);
        javaAspectConfigurations.forEach(JavaAspectConfiguration::runAspectConfiguration);
    }
}
