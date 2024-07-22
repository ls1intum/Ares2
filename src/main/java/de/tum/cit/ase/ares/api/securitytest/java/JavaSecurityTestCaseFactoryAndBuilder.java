package de.tum.cit.ase.ares.api.securitytest.java;

import de.tum.cit.ase.ares.api.architecturetest.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.JavaSupportedArchitectureTestCase;
import de.tum.cit.ase.ares.api.aspectconfiguration.JavaSupportedAspectConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaAspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.isEmpty;

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

    Path withinPath;

    /**
     * Constructor for the Java security test case factory and builder.
     *
     * @param securityPolicy Security policy for the security test cases
     */
    public JavaSecurityTestCaseFactoryAndBuilder(SecurityPolicy securityPolicy, Path withinPath) {
        this.securityPolicy = securityPolicy;
        this.withinPath = withinPath;
        javaArchitectureTestCases = new ArrayList<>();
        javaAspectConfigurations = new ArrayList<>();
        parseTestCasesToBeCreated();
    }

    /**
     * Parses the test cases to be created.
     * If there is no allowed element in a list then an architecture test is added
     * Otherwise an Aspect configuration is added to control the allowed methods
     */
    @SuppressWarnings("unchecked")
    private void parseTestCasesToBeCreated() {
        Supplier<List<?>>[] methods = new Supplier[] {
                securityPolicy::iAllowTheFollowingFileSystemInteractionsForTheStudents,
//                securityPolicy::iAllowTheFollowingNetworkConnectionsForTheStudents,
//                securityPolicy::iAllowTheFollowingCommandExecutionsForTheStudents,
//                securityPolicy::iAllowTheFollowingThreadCreationsForTheStudents,
//                securityPolicy::iAllowTheFollowingPackageImportForTheStudents
        };

        for (int i = 0; i < methods.length; i++) {
            if (isEmpty(methods[i].get())) {
                javaArchitectureTestCases.add(new JavaArchitectureTestCase(JavaSupportedArchitectureTestCase.values()[i], securityPolicy, withinPath));
            } else {
                javaAspectConfigurations.add(new JavaAspectConfiguration(JavaSupportedAspectConfiguration.values()[i], securityPolicy, withinPath));
            }
        }
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
            Path adviceDefinitionPath = Files.copy(
                    Path.of("src/main/resources/aspectOrientedProgrammingFiles/AdviceDefinition.aj"),
                    path.resolve("AdviceDefinition.aj")
            );
            Path pointcutDefinitionPath = Files.copy(
                    Path.of("src/main/resources/aspectOrientedProgrammingFiles/PointcutDefinitions.aj"),
                    path.resolve("PointcutDefinitions.aj")
            );
            Path aspectConfigurationListsFile = Files.createFile(path.resolve("JavaAspectConfigurationLists.java"));
            Files.writeString(aspectConfigurationListsFile,
                    """
                            public class JavaAspectConfigurationLists {
                            
                            """ + String.join(
                            "\n",
                            javaAspectConfigurations
                                    .stream()
                                    .map(JavaAspectConfiguration::createAspectConfigurationFileContent)
                                    .toList()
                    ) + """
                            
                            }
                            """, StandardOpenOption.WRITE);
            return List.of(architectureTestCaseFile, aspectConfigurationListsFile, adviceDefinitionPath, pointcutDefinitionPath);
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
