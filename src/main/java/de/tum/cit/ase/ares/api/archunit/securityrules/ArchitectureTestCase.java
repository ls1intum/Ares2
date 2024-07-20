package de.tum.cit.ase.ares.api.archunit.securityrules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public interface ArchitectureTestCase {

    Logger log = LoggerFactory.getLogger(ArchitectureTestCase.class);

    /**
     * Runs the architecture tests for given policy
     */
    void runArchitectureTestCase(Object securityPolicy) throws IOException, InterruptedException;

    /**
     * Creates architectureTestFileContent
     */
    String createArchitectureTestFileContent(Object securityPolicy);
}
