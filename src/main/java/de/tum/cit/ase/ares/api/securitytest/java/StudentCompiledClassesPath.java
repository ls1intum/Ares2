package de.tum.cit.ase.ares.api.securitytest.java;

import javax.annotation.Nonnull;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the path of the compiled student classes.
 *
 * <p>Description:
 * Exercise Instructors use this annotation
 * to mark a field or method with the file system path
 * where the respective Learning Management Systems locates compiled student classes.</p>
 *
 * <p>Design Rationale: This annotation facilitates the automated detection and utilisation of compiled student classes, supporting efficient class loading and evaluation processes.</p>
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
@Retention(RUNTIME)
public @interface StudentCompiledClassesPath {

    /**
     * Returns the file system path to the compiled student classes.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @return the path of the compiled student classes as a {@link String}
     */
    @Nonnull
    String value();
}