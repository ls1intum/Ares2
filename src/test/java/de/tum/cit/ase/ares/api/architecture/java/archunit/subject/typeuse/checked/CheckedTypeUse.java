package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A type annotation from the checked package itself, which is always allowed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface CheckedTypeUse {
}
