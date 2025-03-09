package de.tum.cit.ase.ares.api.securitytest.java.scanner;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import java.nio.file.Path;

/**
 * JavaScanner interface for scanning Java project metadata.
 *
 * <p>Description: This interface defines methods to scan Java projects for build mode, test classes, package name, main class and test path.
 * It facilitates the extraction of essential metadata for configuration and testing purposes.</p>
 *
 * <p>Design Rationale: By abstracting the scanning logic into an interface, different scanning strategies can be implemented and integrated seamlessly,
 * ensuring modularity and ease of maintenance.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface JavaScanner {

    /**
     * Retrieves the build mode for the Java project.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the Java build mode for the project
     */
    JavaBuildMode scanForBuildMode();

    /**
     * Scans the project for test classes.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return an array of fully qualified test class names
     */
    String[] scanForTestClasses();

    /**
     * Scans the project for the package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the detected package name or a default value if none is found
     */
    String scanForPackageName();

    /**
     * Scans the project for the main class within a package.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the fully qualified main class name or a default value if none is found
     */
    String scanForMainClassInPackage();

    /**
     * Scans the project for the test directory path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the test directory
     */
    Path scanForTestPath();
}