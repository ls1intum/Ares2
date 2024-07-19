package de.tum.cit.ase.ares.api.policy;

public interface ArchitectureTestCase {
    void createArchitectureTestCase(SecurityPolicy securityPolicy);
    String createArchitectureTestCaseFileContent(SecurityPolicy securityPolicy);
    void runArchitectureTestCase(SecurityPolicy securityPolicy);
}
