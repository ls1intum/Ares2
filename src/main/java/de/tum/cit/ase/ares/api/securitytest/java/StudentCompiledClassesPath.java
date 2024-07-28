package de.tum.cit.ase.ares.api.securitytest.java;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for the path of the compiled student classes.
 */
@Retention(RUNTIME)
public @interface StudentCompiledClassesPath {
    String value();
}