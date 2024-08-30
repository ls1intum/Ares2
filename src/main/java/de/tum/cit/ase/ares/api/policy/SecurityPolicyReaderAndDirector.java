package de.tum.cit.ase.ares.api.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.securitytest.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.securitytest.java.JavaBuildTool;
import de.tum.cit.ase.ares.api.securitytest.java.JavaSecurityTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    /**
     * The security policy used for generating security test cases.
     */
    private final SecurityPolicy securityPolicy;

    /**
     * The factory and builder for creating and managing security test cases.
     */
    private final SecurityTestCaseAbstractFactoryAndBuilder testCaseManager;

    /**
     * Constructs a new {@link SecurityPolicyReaderAndDirector} to read the security policy and configure the test case manager.
     *
     * @param path            the path to the security policy file.
     * @param policyWithinPath the path within the project where the policy is applied.
     * @throws IOException if there is an error reading the security policy file.
     */
    public SecurityPolicyReaderAndDirector(Path path, Path policyWithinPath) throws IOException {
        securityPolicy = new ObjectMapper(new YAMLFactory()).readValue(Files.readString(path), SecurityPolicy.class);
        testCaseManager = switch (securityPolicy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed()) {
            case JAVA_USING_MAVEN_AND_INSTRUMENTATION -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildTool.MAVEN, JavaAOPMode.INSTRUMENTATION, securityPolicy, policyWithinPath
            );
            case JAVA_USING_MAVEN_AND_ASPECTJ -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildTool.MAVEN, JavaAOPMode.ASPECTJ, securityPolicy, policyWithinPath
            );
            case JAVA_USING_GRADLE_AND_INSTRUMENTATION -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildTool.GRADLE, JavaAOPMode.INSTRUMENTATION, securityPolicy, policyWithinPath
            );
            case JAVA_USING_GRADLE_AND_ASPECTJ -> new JavaSecurityTestCaseFactoryAndBuilder(
                    JavaBuildTool.GRADLE, JavaAOPMode.ASPECTJ, securityPolicy, policyWithinPath
            );
        };
    }

    /**
     * Writes the security test cases to the specified directory.
     *
     * @param path the directory where the test case files should be written.
     * @return a list of paths representing the files that were written.
     */
    public List<Path> writeTestCasesToFiles(Path path) {
        return testCaseManager.writeTestCasesToFiles(path);
    }

    /**
     * Executes the generated security test cases.
     */
    public void runSecurityTestCases() {
        testCaseManager.runSecurityTestCases();
    }

}