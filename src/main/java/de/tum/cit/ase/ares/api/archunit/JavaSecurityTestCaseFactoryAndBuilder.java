package de.tum.cit.ase.ares.api.archunit;

import de.tum.cit.ase.ares.api.archunit.securityrules.ArchitectureTestCase;

import java.util.ArrayList;
import java.util.List;

public class JavaSecurityTestCaseFactoryAndBuilder {

    List<ArchitectureTestCase> architectureTestCases;

    // TODO we definitely need Security Policy here to be able to manage Architecture Test Cases
    public void createTestCases() {
        // Create test cases for the security rules
    }

    // Why do we return a list of strings here?????
    public List<String> writeTestCasesToFile(String path) {
        // Write the test cases to a file
        return new ArrayList<>();
    }

    private void runTestCases() {
        // TODO: How do we run the tests of other languages here?????
        // Run the test cases
    }
}
