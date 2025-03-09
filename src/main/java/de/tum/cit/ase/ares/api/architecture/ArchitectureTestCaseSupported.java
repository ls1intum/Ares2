package de.tum.cit.ase.ares.api.architecture;

import java.util.List;

/**
 * Supported architecture test cases in Java programming language.
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public interface ArchitectureTestCaseSupported {

    /**
     * Static code analysis architecture test cases
     */
    List<ArchitectureTestCaseSupported> getStatic();

    /**
     * Dynamic code analysis architecture test cases
     */
    List<ArchitectureTestCaseSupported> getDynamic();

}
