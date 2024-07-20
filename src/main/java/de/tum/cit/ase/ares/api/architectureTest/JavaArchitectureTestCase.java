package de.tum.cit.ase.ares.api.architectureTest;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public class JavaArchitectureTestCase implements ArchitectureTestCase{

    private JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase;
    private SecurityPolicy securityPolicy;

    public JavaArchitectureTestCase(JavaSupportedArchitectureTestCase javaSupportedArchitectureTestCase, SecurityPolicy securityPolicy) {
        this.javaSupportedArchitectureTestCase = javaSupportedArchitectureTestCase;
        this.securityPolicy = securityPolicy;
    }

    @Override
    public String createArchitectureTestCaseFileContent() {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public void runArchitectureTestCase() {
        throw new RuntimeException("Not implemented yet!");
    }
}
