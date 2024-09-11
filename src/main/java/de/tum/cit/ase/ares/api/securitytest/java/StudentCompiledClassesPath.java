package de.tum.cit.ase.ares.api.securitytest.java;

import javax.annotation.Nonnull;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify the path of the compiled student classes.
 * <p>
 * This annotation is used to mark a field or method with the file path
 * where the compiled student classes are located.
 * </p>
 */
@Retention(RUNTIME)
public @interface StudentCompiledClassesPath {

    /**
     * Specifies the path to the compiled student classes.
     * <p>
     * The value returned by this method should represent a valid file system
     * path pointing to the location where the student's compiled Java classes
     * are stored.
     * </p>
     *
     * @return the path of the compiled student classes as a {@link String}.
     */
    @Nonnull String value();
}