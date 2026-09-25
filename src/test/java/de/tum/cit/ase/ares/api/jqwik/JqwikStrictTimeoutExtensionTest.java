package de.tum.cit.ase.ares.api.jqwik;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.internal.TimeoutUtils;

/**
 * Pins how this extension bounds a try's termination.
 * <p>
 * The period reaches {@link TimeoutUtils} as the <em>default</em> a configured
 * {@link StrictTimeout#terminationGrace()} overrides, which it did not before:
 * the extension passed a hard-coded second, so an instructor who raised the
 * grace for a jqwik property still had a correct interruption-aware execution
 * terminated after a second.
 * <p>
 * That the annotation wins over the default is asserted in
 * {@code TimeoutUtilsTest}, on the one method both extensions go through.
 * Asserted here is the other half, the value this extension contributes, which
 * has to stay one second: raising it would loosen every property that
 * configures nothing, and lowering it to the Jupiter path's 50 ms would fail
 * properties whose cleanup unwinds through the property lifecycle. A live
 * property that overran its grace is deliberately not used as the test, because
 * the behaviour under test halts the JVM, which would take the surefire fork
 * down rather than report a failure.
 */
class JqwikStrictTimeoutExtensionTest {

	@Test
	void contributesTheOneSecondDefaultTerminationGracePeriod() throws ReflectiveOperationException {
		Field field = JqwikStrictTimeoutExtension.class.getDeclaredField("DEFAULT_TERMINATION_GRACE_PERIOD");
		field.setAccessible(true);

		assertThat((Duration) field.get(null)).isEqualTo(Duration.ofSeconds(1));
	}
}
