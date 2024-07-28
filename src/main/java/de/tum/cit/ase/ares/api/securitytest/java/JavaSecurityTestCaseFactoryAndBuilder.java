package de.tum.cit.ase.ares.api.securitytest.java;

import de.tum.cit.ase.ares.api.architecturetest.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase;
import de.tum.cit.ase.ares.api.aspectconfiguration.JavaAspectConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.JavaSupportedAspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.IllegalFormatException;
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
        Supplier<List<?>>[] methods = new Supplier[]{securityPolicy::iAllowTheFollowingFileSystemInteractionsForTheStudents,
//                securityPolicy::iAllowTheFollowingNetworkConnectionsForTheStudents,
//                securityPolicy::iAllowTheFollowingCommandExecutionsForTheStudents,
//                securityPolicy::iAllowTheFollowingThreadCreationsForTheStudents,
//                securityPolicy::iAllowTheFollowingPackageImportForTheStudents
        };

        for (int i = 0; i < methods.length; i++) {
            if (isEmpty(methods[i].get())) {
                javaArchitectureTestCases.add(new JavaArchitectureTestCase(JavaSupportedArchitectureTestCase.values()[i], withinPath));
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
        //<editor-fold desc="Create architecture test case files">
        Path javaArchitectureTestCaseCollectionFile;
        try {
            javaArchitectureTestCaseCollectionFile = Files.createFile(path.resolve("JavaArchitectureTestCaseCollection.java"));
        }
        //<editor-fold desc="Catches">
        catch (InvalidPathException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaArchitectureTestCaseCollection.java on " + path + " due to an incorrect address resolving: " + e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaArchitectureTestCaseCollection.java on " + path + " due to missing supported by this JVM: " + e);
        } catch (FileAlreadyExistsException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaArchitectureTestCaseCollection.java on " + path + ", as it already exists: " + e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaArchitectureTestCaseCollection.java on " + path + " due to an I/O exception: " + e);
        }
        //</editor-fold>
        String javaArchitectureTestCaseCollectionFileHeader;
        Path javaArchitectureTestCaseCollectionFileHeaderPath = Path.of(
                "src",
                "main",
                "resources",
                "templates",
                "javaArchitectureTestCaseCollectionFileHeader.txt"
        );
        try {
            javaArchitectureTestCaseCollectionFileHeader = String.format(
                    Files.readString(javaArchitectureTestCaseCollectionFileHeaderPath),
                    "de.tum.cit.ase", // TODO: Replace with flexible package name handling
                    (ProjectSourcesFinder.isGradleProject() ? "build" : "target") + File.separator + withinPath.toString()
            );
        }
        //<editor-fold desc="Catches">
        catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileHeaderPath + " due to an I/O exception: " + e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileHeaderPath + " due to an out of memory exception: " + e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to " + javaArchitectureTestCaseCollectionFileHeaderPath + " defining an illegal format: " + e);
        }
        //</editor-fold>
        String javaArchitectureTestCaseCollectionFileBody;
        try {
            javaArchitectureTestCaseCollectionFileBody = String.join("\n", javaArchitectureTestCases.stream().map(JavaArchitectureTestCase::createArchitectureTestCaseFileContent).toList());
        }
        //<editor-fold desc="Catches">
        catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to one of the architecture test case file contents being null: " + e);
        }
        //</editor-fold>
        String javaArchitectureTestCaseCollectionFileFooter;
        Path javaArchitectureTestCaseCollectionFileFooterPath = Path.of(
                "src",
                "main",
                "resources",
                "templates",
                "javaArchitectureTestCaseCollectionFileFooter.txt"
        );
        try {
            javaArchitectureTestCaseCollectionFileFooter = String.format(
                    Files.readString(javaArchitectureTestCaseCollectionFileFooterPath)
            );
        }
        //<editor-fold desc="Catches">
        catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileFooterPath + " due to an I/O exception: " + e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileFooterPath + " due to an out of memory exception: " + e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to " + javaArchitectureTestCaseCollectionFileFooterPath + " defining an illegal format: " + e);
        }
        //</editor-fold>
        try {
            Files.writeString(
                    javaArchitectureTestCaseCollectionFile,
                    String.join(
                            "\n",
                            List.of(
                                    javaArchitectureTestCaseCollectionFileHeader,
                                    javaArchitectureTestCaseCollectionFileBody,
                                    javaArchitectureTestCaseCollectionFileFooter
                            )
                    ),
                    StandardOpenOption.WRITE
            );
        }
        //<editor-fold desc="Catches">
        catch (IllegalArgumentException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to an illegal argument: " + e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to an I/O exception: " + e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to one of the architecture test case file parts being null: " + e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to missing supported by this JVM: " + e);
        }
        //</editor-fold>

        //</editor-fold>
        //<editor-fold desc="Create aspect configuration files">
        Path javaAspectConfigurationCollectionFile;
        try {
            javaAspectConfigurationCollectionFile = Files.createFile(path.resolve("JavaAspectConfigurationCollection.java"));
        }
        //<editor-fold desc="Catches">
        catch (InvalidPathException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaAspectConfigurationCollection.java on " + path + " due to an incorrect address resolving: " + e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaAspectConfigurationCollection.java on " + path + " due to missing supported by this JVM: " + e);
        } catch (FileAlreadyExistsException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaAspectConfigurationCollection.java on " + path + ", as it already exists: " + e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot create JavaAspectConfigurationCollection.java on " + path + " due to an I/O exception: " + e);
        }
        //</editor-fold>
        String javaAspectConfigurationCollectionFileHeader;
        try {
            javaAspectConfigurationCollectionFileHeader = String.format(
                    Files.readString(
                            Path.of(
                                    "src",
                                    "main",
                                    "resources",
                                    "templates",
                                    "javaAspectConfigurationCollectionFileHeader.txt"
                            )
                    ),
                    "de.tum.cit.ase", // TODO: Replace with flexible package name handling
                    (ProjectSourcesFinder.isGradleProject() ? "build" : "target") + File.separator + withinPath.toString()
            );
        }
        //<editor-fold desc="Catches">
        catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileHeaderPath + " due to an I/O exception: " + e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileHeaderPath + " due to an out of memory exception: " + e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to " + javaArchitectureTestCaseCollectionFileHeaderPath + " defining an illegal format: " + e);
        }
        //</editor-fold>
        String javaAspectConfigurationCollectionFileBody;
        try {
            javaAspectConfigurationCollectionFileBody = String.join(
                    "\n",
                    javaAspectConfigurations.stream().map(JavaAspectConfiguration::createAspectConfigurationFileContent).toList()
            );
        }
        //<editor-fold desc="Catches">
        catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to one of the architecture test case file contents being null: " + e);
        }
        //</editor-fold>
        String javaAspectConfigurationCollectionFileFooter;
        try {
            javaAspectConfigurationCollectionFileFooter = String.format(
                    Files.readString(
                            Path.of(
                                    "src",
                                    "main",
                                    "resources",
                                    "templates",
                                    "javaAspectConfigurationCollectionFileFooter.txt"
                            )
                    )
            );
        }
        //<editor-fold desc="Catches">
        catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileFooterPath + " due to an I/O exception: " + e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to not being able to read " + javaArchitectureTestCaseCollectionFileFooterPath + " due to an out of memory exception: " + e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaAspectConfigurationCollection.java on " + path + " due to " + javaArchitectureTestCaseCollectionFileFooterPath + " defining an illegal format: " + e);
        }
        //</editor-fold>
        try {
            Files.writeString(
                    javaAspectConfigurationCollectionFile,
                    String.join(
                            "\n",
                            List.of(
                                    javaAspectConfigurationCollectionFileHeader,
                                    javaAspectConfigurationCollectionFileBody,
                                    javaAspectConfigurationCollectionFileFooter
                            )
                    ),
                    StandardOpenOption.WRITE
            );
        }
        //<editor-fold desc="Catches">
        catch (IllegalArgumentException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to an illegal argument: " + e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to an I/O exception: " + e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to one of the architecture test case file parts being null: " + e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Cannot write to JavaArchitectureTestCaseCollection.java on " + path + " due to missing supported by this JVM: " + e);
        }
        //</editor-fold>
        //</editor-fold>
        return List.of(javaArchitectureTestCaseCollectionFile, javaAspectConfigurationCollectionFile);

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
