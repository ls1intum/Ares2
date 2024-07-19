package de.tum.cit.ase.ares.api.policy;

import java.nio.file.Path;
import java.util.List;

public interface SecurityTestCaseAbstractFactoryAndBuilder {
    void createTestCases(SecurityPolicy securityPolicy);
    List<String> writeTestCasesToFiles(Path path);
    void runSecurityTestCases();
}
