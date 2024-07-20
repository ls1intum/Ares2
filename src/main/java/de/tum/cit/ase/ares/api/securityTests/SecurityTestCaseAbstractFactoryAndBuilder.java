package de.tum.cit.ase.ares.api.securityTests;

import java.nio.file.Path;
import java.util.List;

public interface SecurityTestCaseAbstractFactoryAndBuilder {

    List<Path> writeTestCasesToFiles(Path path);

    void runSecurityTestCases();
}
