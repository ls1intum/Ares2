package %s.ares.api.architecture;

import java.util.List;

/**
 * Interface for supported architecture test cases in Java.
 *
 * <p>Description: Provides methods to access lists of static and dynamic architecture test cases that enforce architectural constraints in an application.</p>
 *
 * <p>Design Rationale: Abstracting test cases into distinct categories promotes modularity and extensibility, allowing the integration of diverse static and dynamic analysis techniques.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface ArchitectureTestCaseSupported {

    /**
     * Retrieves the list of static code analysis architecture test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the list of static architecture test cases.
     */
    List<ArchitectureTestCaseSupported> getStatic();

    /**
     * Retrieves the list of dynamic code analysis architecture test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the list of dynamic architecture test cases.
     */
    List<ArchitectureTestCaseSupported> getDynamic();
}