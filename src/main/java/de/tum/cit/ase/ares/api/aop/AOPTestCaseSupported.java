package de.tum.cit.ase.ares.api.aop;

import java.util.List;

/**
 * Supported AOP test cases in Java programming language.
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public interface AOPTestCaseSupported {

    /**
     * Dynamic code analysis AOP test cases
     */
    List<AOPTestCaseSupported> getDynamic();
}
