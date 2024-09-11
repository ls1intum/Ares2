package de.tum.cit.ase.ares.api.policy;

//<editor-fold desc="Imports">

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.securitytest.java.JavaSecurityTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ProgrammingLanguageConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
//</editor-fold>

/**
 * Handles the reading of security policy files and manages the creation and execution of security test cases.
 * <p>
 * This class serves as a client of the Abstract Factory Design Pattern and also acts as the director in the Builder Design Pattern.
 * It reads a security policy from a file, configures the appropriate test case factory and builder,
 * and manages the writing and execution of the security test cases.
 * </p>
 *
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 * @since 2.0.0
 */
public class SecurityPolicyReaderAndDirector {

    //<editor-fold desc="Attributes">
    /**
     * The security policy used for generating security test cases.
     */
    private final SecurityPolicy securityPolicy;

    /**
     * The factory and builder for creating and managing security test cases.
     */
    private final SecurityTestCaseAbstractFactoryAndBuilder testCaseManager;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new {@link SecurityPolicyReaderAndDirector} to read the security policy and configure the test case manager.
     *
     * @param securityPolicyPath the path to the security policy file.
     * @param projectPath        the path within the project where the policy is applied.
     * @throws IOException if there is an error reading the security policy file.
     */
    public SecurityPolicyReaderAndDirector(Path securityPolicyPath, Path projectPath) throws IOException {
        try {
            securityPolicy = new ObjectMapper(new YAMLFactory()).readValue(securityPolicyPath.toFile(), SecurityPolicy.class);
            testCaseManager = createSecurityTestCases(
                    securityPolicy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed(),
                    projectPath
            );
        } catch (StreamReadException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Failed to read the security policy file '" + securityPolicyPath + "'.", e);
        } catch (DatabindException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Failed to bind data from the security policy file '" + securityPolicyPath + "'.", e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): The security policy contains unsupported operations.", e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Error occurred while accessing the security policy file '" + securityPolicyPath + "'.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Create security test cases methods">

    /**
     * Creates the appropriate security test case factory and builder based on the programming language configuration.
     *
     * @param programmingLanguageConfiguration the programming language configuration used for the security test cases.
     * @param projectPath                      the path within the project where the test cases will be applied.
     * @return the factory and builder for creating and managing security test cases.
     */
    public JavaSecurityTestCaseFactoryAndBuilder createSecurityTestCases(ProgrammingLanguageConfiguration programmingLanguageConfiguration, Path projectPath) {
        return switch (programmingLanguageConfiguration) {
            case JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildMode.MAVEN, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.ASPECTJ, securityPolicy, projectPath
            );
            case JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildMode.MAVEN, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.INSTRUMENTATION, securityPolicy, projectPath
            );
            case JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildMode.GRADLE, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.ASPECTJ, securityPolicy, projectPath
            );
            case JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildMode.GRADLE, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.INSTRUMENTATION, securityPolicy, projectPath
            );
        };
    }
    //</editor-fold>

    //<editor-fold desc="Write security test cases methods">

    /**
     * Writes the security test cases to the specified directory.
     *
     * @param projectPath the directory where the test case files should be written.
     * @return a list of paths representing the files that were written.
     */
    public List<Path> writeSecurityTestCases(Path projectPath) {
        return testCaseManager.writeSecurityTestCases(projectPath);
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test cases methods">

    /**
     * Executes the generated security test cases.
     */
    public void executeSecurityTestCases() {
        testCaseManager.executeSecurityTestCases();
    }
    //</editor-fold>

}