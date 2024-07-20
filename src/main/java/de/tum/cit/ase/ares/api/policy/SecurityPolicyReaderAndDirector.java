package de.tum.cit.ase.ares.api.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.securityTests.JavaSecurityTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securityTests.SecurityTestCaseAbstractFactoryAndBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SecurityPolicyReaderAndDirector {

    SecurityPolicy securityPolicy;
    SecurityTestCaseAbstractFactoryAndBuilder testCaseManager;

    public SecurityPolicyReaderAndDirector(Path path) throws IOException {
        securityPolicy = (new ObjectMapper(new YAMLFactory())).readValue(Files.readString(path), SecurityPolicy.class);
        testCaseManager = switch (securityPolicy.theProgrammingLanguageIUseInThisProgrammingExerciseIs()) {
            case JAVA -> new JavaSecurityTestCaseFactoryAndBuilder((securityPolicy));
            case PYTHON -> throw new UnsupportedOperationException("Python is not supported yet.");
            case C -> throw new UnsupportedOperationException("C is not supported yet.");
            case SWIFT -> throw new UnsupportedOperationException("Swift is not supported yet.");
        };
    }

    public List<Path> writeTestCasesToFiles(Path path) {
        return testCaseManager.writeTestCasesToFiles(path);
    }

    public void runSecurityTestCases() {
        testCaseManager.runSecurityTestCases();
    }


}
