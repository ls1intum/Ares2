package de.tum.cit.ase.ares.api.aop;

import java.util.List;

/**
 * Interface for supported AOP test cases in Java.
 *
 * <p>Description: Provides a method to access a list of dynamic AOP test cases that facilitate aspect-oriented programming security testing.</p>
 *
 * <p>Design Rationale: Centralising the access to dynamic AOP test cases simplifies the integration of aspect-based security measures across different programming environments.</p>
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