package de.tum.cit.ase.ares.api.aop;

import java.util.List;

/**
 * Interface for supported AOP test cases in Java.
 *
 * <p>Description: Provides methods to access lists of dynamic architecture test cases that enforce AOP constraints in an application.</p>
 *
 * <p>Design Rationale: Abstracting test cases into distinct categories promotes modularity and extensibility, allowing the integration of diverse dynamic analysis techniques.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface AOPTestCaseSupported {

    /**
     * Retrieves the list of dynamic code analysis AOP test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the list of dynamic AOP test cases.
     */
    List<AOPTestCaseSupported> getDynamic();
}