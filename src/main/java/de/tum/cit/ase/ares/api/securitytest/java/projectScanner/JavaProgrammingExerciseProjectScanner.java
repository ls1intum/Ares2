package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import javax.annotation.Nonnull;

public class JavaProgrammingExerciseProjectScanner extends JavaProjectScanner {

    //<editor-fold desc="Variable Regex Patterns (defined by project)">

    /**
     * Default package name used if none is found.
     */
    @Nonnull
    private String getDefaultPackage() {
        return "de.tum.cit.ase";
    }    /**
     * Default main class name used if no main class is detected.
     */
    @Nonnull
    private String getDefaultMainClass() {
        return "Main";
    }
    //</editor-fold>

    //<editor-fold desc="Overridden methods with TUM-specific defaults">

    /**
     * Determines the most commonly used package name in the project.
     * Uses TUM-specific default package if none is found.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the most frequent package name or TUM default if none is found
     */
    @Override
    @Nonnull
    public String scanForPackageName() {
        String result = super.scanForPackageName();
        return result.isEmpty() ? getDefaultPackage() : result;
    }

    /**
     * Identifies the main class within the project.
     * Uses TUM-specific default main class if none is found.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the name of the class containing the main method or TUM default value if none is found
     */
    @Override
    @Nonnull
    public String scanForMainClassInPackage() {
        String result = super.scanForMainClassInPackage();
        return "Main".equals(result) ? getDefaultMainClass() : result;
    }
    //</editor-fold>

}
