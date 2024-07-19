package de.tum.cit.ase.ares.api.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecurityPolicyReaderAndDirector {

    SecurityPolicy securityPolicy;
    Optional<SecurityTestCaseAbstractFactoryAndBuilder> testCaseManager = Optional.empty();
    List<String> createdFiles = new ArrayList<>();

    public SecurityPolicyReaderAndDirector(Path path) throws IOException {
        securityPolicy = (new ObjectMapper(new YAMLFactory())).readValue(Files.readString(path), SecurityPolicy.class);
    }

    public SecurityPolicyReaderAndDirector createTestCaseManager() {
        testCaseManager = switch (securityPolicy.theProgrammingLanguageIUseInThisProgrammingExerciseIs()) {
            case JAVA -> Optional.empty();
            case PYTHON -> Optional.empty();
            case C -> Optional.empty();
            case SWIFT -> Optional.empty();
        };
        testCaseManager
                .orElseThrow(() -> new SecurityException("Creating test cases failed."))
                .createTestCases(securityPolicy);
        return this;
    }

    public SecurityPolicyReaderAndDirector writeTestCasesToFiles(Path path) {
        createdFiles = testCaseManager
                .orElseThrow(() -> new SecurityException("TestCaseManager is not initialised."))
                .writeTestCasesToFiles(path);
        return this;
    }

    public SecurityPolicyReaderAndDirector runSecurityTestCases() {
        testCaseManager
                .orElseThrow(() -> new SecurityException("TestCaseManager is not initialised."))
                .runSecurityTestCases();
        return this;
    }


}
