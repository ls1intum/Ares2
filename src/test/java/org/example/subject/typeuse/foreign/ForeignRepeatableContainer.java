package org.example.subject.typeuse.foreign;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The container that holds repeated uses of the foreign repeatable annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface ForeignRepeatableContainer {

	/**
	 * The repeated annotations.
	 *
	 * @return the repeated annotations
	 */
	ForeignRepeatable[] value();
}
