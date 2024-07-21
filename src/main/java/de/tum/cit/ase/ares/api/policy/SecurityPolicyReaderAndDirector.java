package de.tum.cit.ase.ares.api.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.securitytest.java.JavaSecurityTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Security policy file reader, security test case creator
 * and client of the abstract factory design pattern.
 * as well as the director of the builder design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 * @since 2.0.0
 */
public class SecurityPolicyReaderAndDirector {

    /**
     * Security policy for the security test cases
     */
    SecurityPolicy securityPolicy;
    /**
     * Factory and builder for the security test cases
     */
    SecurityTestCaseAbstractFactoryAndBuilder testCaseManager;

    /**
     * Constructor for the security policy reader and director.
     *
     * @param path Path to the security policy file
     * @throws IOException If the security policy file cannot be read
     */
    public SecurityPolicyReaderAndDirector(Path path) throws IOException {
        securityPolicy = (new ObjectMapper(new YAMLFactory())).readValue(Files.readString(path), SecurityPolicy.class);
        testCaseManager = switch (securityPolicy.theProgrammingLanguageIUseInThisProgrammingExerciseIs()) {
            case JAVA -> new JavaSecurityTestCaseFactoryAndBuilder((securityPolicy));
            case PYTHON -> throw new UnsupportedOperationException("Python is not supported yet.");
            case C -> throw new UnsupportedOperationException("C is not supported yet.");
            case SWIFT -> throw new UnsupportedOperationException("Swift is not supported yet.");
        };
    }

    /**
     * Writes the security test cases to files.
     *
     * @param path Path to the directory where the files should be written to
     * @return List of paths of the written files
     */
    public List<Path> writeTestCasesToFiles(Path path) {
        return testCaseManager.writeTestCasesToFiles(path);
    }

    /**
     * Runs the security test cases.
     */
    public void runSecurityTestCases() {
        testCaseManager.runSecurityTestCases();
    }


}
