package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class JavaProgrammingExerciseProjectScanner extends JavaProjectScanner {

    //<editor-fold desc="Variable Regex Patterns (defined by project)">

    /**
     * Default package name used if none is found.
     */
    @Nonnull
    private String getDefaultPackage() {
        return "de.tum.cit.ase";
    }

    /**
     * Default main class name used if no main class is detected.
     */
    @Nonnull
    private String getDefaultMainClass() {
        return "Main";
    }

    /**
     * Regex pattern to identify test annotations.
     * This pattern matches the following annotations:
     * - @Test
     * - @Property
     * - @PublicTest
     * - @PrivateTest
     */
    @Nonnull
    private static final Pattern TEST_ANNOTATION_PATTERN = Pattern.compile("@(?:Test|Property|PublicTest|PrivateTest)\\b");

    /**
     * Regex pattern to identify test annotations.
     */
    @Nonnull
    private Pattern getTestAnnotationPattern() {
        return JavaProgrammingExerciseProjectScanner.TEST_ANNOTATION_PATTERN;
    }
    //</editor-fold>

}
