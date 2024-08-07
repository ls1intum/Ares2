package de.tum.cit.ase.ares.api;

import org.apiguardian.api.API;

import java.lang.annotation.Inherited;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * This annotation is used to specify the security policy
 * that should be used for {@link de.tum.cit.ase.ares.api.jupiter.PublicTest}
 * or {@link de.tum.cit.ase.ares.api.jupiter.HiddenTest}.
 * This annotation is optional and specified in TODO.
 * The annotation itself implements the facade of the facade design pattern
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/facade">Facade Design Pattern</a>
 * @since 2.0.0
 */
@API(status = API.Status.STABLE)
@Inherited
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface Policy {
    /**
     * The path of the policy-file as a String.
     *
     * @return the path of the policy-file as a String.
     */
    String value();

    /**
     * The path of the test-file as a String.
     *
     * @return the path of the test-file as a String.
     */
    String withinPath() default "";
}
