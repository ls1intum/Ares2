package de.tum.cit.ase.ares.api.archunit;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface StudentPath {
    String value();
}