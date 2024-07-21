package de.tum.cit.ase.ares.api.securitytest.java;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface StudentCompiledClassesPath {
    String value();
}