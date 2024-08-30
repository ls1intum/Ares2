package de.tum.cit.ase.ares.api.securitytest.java;

/**
 * Enum representing the Java build tools supported by Ares.
 *
 * <p>This enum provides two modes:</p>
 * <ul>
 *   <li>{@link #MAVEN} - A mode using Maven for running Ares.</li>
 *   <li>{@link #GRADLE} - A mode using Gradle for running Ares.</li>
 * </ul>
 */
public enum JavaBuildTool {

    /**
     * Maven mode.
     * <p>
     * In this mode, Maven is used to run Ares.
     * </p>
     */
    MAVEN,

    /**
     * Gradle mode.
     * <p>
     * In this mode, Gradle is used to run Ares.
     * </p>
     */
    GRADLE
}