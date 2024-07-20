package de.tum.cit.ase.ares.api.architectureTest;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public interface ArchitectureTestCase {
    String createArchitectureTestCaseFileContent();
    void runArchitectureTestCase();
}
