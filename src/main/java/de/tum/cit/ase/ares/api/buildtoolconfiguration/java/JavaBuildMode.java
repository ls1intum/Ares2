package de.tum.cit.ase.ares.api.buildtoolconfiguration.java;

/**
 * Enum representing the Java build tools supported by Ares.
 *
 * <p>This enum provides two modes:</p>
 * <ul>
 *   <li>{@link #MAVEN} - A mode using Maven for running Ares.</li>
 *   <li>{@link #GRADLE} - A mode using Gradle for running Ares.</li>
 * </ul>
 */
public enum JavaBuildMode {

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
    GRADLE;

    public String[] threePartedFileHeader() {
        return switch (this) {
            case MAVEN -> new String[]{"templates", "java", "maven", "pomHeader.txt"};
            case GRADLE -> new String[]{"templates", "java", "gradle", "buildHeader.txt"};
        };
    }

    public String[] threePartedFileFooter() {
        return switch (this) {
            case MAVEN -> new String[]{"templates", "java", "maven", "pomFooter.txt"};
            case GRADLE -> new String[]{"templates", "java", "gradle", "buildFooter.txt"};
        };
    }

    public String[] fileName() {
        return switch (this) {
            case MAVEN -> new String[]{"pom.xml"};
            case GRADLE -> new String[]{"build.gradle"};
        };
    }
}