package org.example.subject.typeuse.foreign;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A repeatable type annotation, so that two uses are stored inside a container
 * annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@Repeatable(ForeignRepeatableContainer.class)
public @interface ForeignRepeatable {
}
