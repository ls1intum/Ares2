package org.example.subject.typeuse.values;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that a fixture names only as a nested annotation value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ValueMarker {
}
