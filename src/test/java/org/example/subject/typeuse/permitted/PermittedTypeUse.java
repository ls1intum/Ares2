package org.example.subject.typeuse.permitted;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.example.subject.typeuse.values.ValueLevel;
import org.example.subject.typeuse.values.ValueMarker;

/**
 * A type annotation from an allowed package whose values can name types from a
 * forbidden one.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface PermittedTypeUse {

	/**
	 * Class literals.
	 *
	 * @return the classes
	 */
	Class<?>[] value() default {};

	/**
	 * An enum value.
	 *
	 * @return the level
	 */
	ValueLevel level() default ValueLevel.LOW;

	/**
	 * Nested annotations.
	 *
	 * @return the markers
	 */
	ValueMarker[] markers() default {};
}
