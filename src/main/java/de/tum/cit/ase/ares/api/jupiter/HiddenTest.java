package de.tum.cit.ase.ares.api.jupiter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.io.IOTester;

/**
 * Marks a HIDDEN test case, can declare {@link IOTester} as parameter. This
 * annotation requires a {@link Deadline} annotation to be set either on the
 * test class or the test method. See {@link Deadline} for more information.
 *
 * @see Deadline
 * @author Christian Femers
 * @since 0.1.0
 * @version 1.1.0
 */
@API(status = Status.MAINTAINED)
@Documented()
@Retention(RUNTIME)
@Target(METHOD)
@Test
@JupiterAresTest(TestType.HIDDEN)
public @interface HiddenTest {
	// maker only
}
