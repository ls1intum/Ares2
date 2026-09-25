package de.tum.cit.ase.ares.api;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.*;

import de.tum.cit.ase.ares.api.jqwik.JqwikStrictTimeoutExtension;
import de.tum.cit.ase.ares.api.jupiter.*;

/**
 * Works like
 * {@link Assertions#assertTimeoutPreemptively(java.time.Duration, org.junit.jupiter.api.function.Executable)
 * Assertions.assertTimeoutPreemptively} and {@link Assertions}, section
 * pre-emptive timeout. Its behaviour differs significantly from
 * {@link Timeout}: it can terminate tests stuck in endless loops, which
 * {@link Timeout} cannot.
 * <p>
 * To use {@link StrictTimeout} <b>without</b> any {@link Public} or
 * {@link Hidden}, you need to declare the {@link JupiterStrictTimeoutExtension}
 * for JUnit 5 Jupiter or {@link JqwikStrictTimeoutExtension} for jqwik
 * explicitly. However, this is not recommended as it is less effective.
 *
 * @author Christian Femers
 * @since 0.1.0
 * @version 2.1.0
 */
@API(status = Status.STABLE)
@Inherited
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
public @interface StrictTimeout {
	/**
	 * The duration of this timeout. <i>(per default in seconds)</i>
	 */
	long value();

	/**
	 * The time unit of this timeout, <b>defaults to seconds</b>.
	 *
	 * @see TimeUnit
	 */
	TimeUnit unit() default TimeUnit.SECONDS;

	/**
	 * The bounded period allowed for an interrupted test to terminate before Ares
	 * treats the test worker as contaminated.
	 * <p>
	 * A negative value, the default, means this is not configured and the executing
	 * extension applies its own period: 50 ms for a JUnit test, one second for a
	 * jqwik property. The two differ because a jqwik try is torn down through more
	 * of the engine, and neither may be raised for the other by making this
	 * attribute default to a number. Set it, in whatever
	 * {@link #terminationGraceUnit() unit} suits, to override whichever applies.
	 * <p>
	 * Zero is a configured value, not an absent one: it means terminate at once. A
	 * configured period must not exceed one day, which is far beyond any period a
	 * test can want and keeps the wait's arithmetic sound; a longer one is rejected
	 * when the annotation is read.
	 */
	long terminationGrace() default -1;

	/**
	 * The unit of the termination grace period, <b>defaults to milliseconds</b>.
	 * Ignored while {@link #terminationGrace()} is negative.
	 */
	TimeUnit terminationGraceUnit() default TimeUnit.MILLISECONDS;
}
