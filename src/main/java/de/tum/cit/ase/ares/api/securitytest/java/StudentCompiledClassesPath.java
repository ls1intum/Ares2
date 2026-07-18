package de.tum.cit.ase.ares.api.securitytest.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Specifies the path of the compiled student classes.
 * <p>
 * Description: Exercise instructors use this annotation on the protected test
 * class to specify the file-system path where the respective learning
 * management system locates compiled student classes.
 * </p>
 * <p>
 * Design Rationale: This annotation facilitates the automated detection and
 * utilisation of compiled student classes, supporting efficient class loading
 * and evaluation processes.
 * </p>
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
