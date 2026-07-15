package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class JupiterSecurityExtensionResetTest {

	@Test
	void validResetIsInvoked() {
		Resettable.resetCalled = false;
		assertDoesNotThrow(() -> JupiterSecurityExtension.resetSettingsInClass(Resettable.class));
		if (!Resettable.resetCalled) {
			throw new AssertionError("reset was not invoked");
		}
	}

	@Test
	void missingResetMethodFailsClosed() {
		SecurityException failure = assertThrows(SecurityException.class,
				() -> JupiterSecurityExtension.resetSettingsInClass(MissingReset.class));
		assertInstanceOf(NoSuchMethodException.class, failure.getCause());
	}

	@Test
	void exceptionFromResetFailsClosed() {
		SecurityException failure = assertThrows(SecurityException.class,
				() -> JupiterSecurityExtension.resetSettingsInClass(ThrowingReset.class));
		assertInstanceOf(InvocationTargetException.class, failure.getCause());
	}

	@Test
	void inaccessibleResetMethodFailsClosed() throws Exception {
		Method inaccessible = mock(Method.class);
		doThrow(new IllegalAccessException("denied")).when(inaccessible).invoke(null);

		SecurityException failure = assertThrows(SecurityException.class,
				() -> JupiterSecurityExtension.resetSettingsWithMethod(inaccessible));
		assertInstanceOf(IllegalAccessException.class, failure.getCause());
	}

	private static final class Resettable {
		private static boolean resetCalled;

		public static void reset() {
			resetCalled = true;
		}
	}

	private static final class MissingReset {
	}

	private static final class ThrowingReset {
		public static void reset() {
			throw new IllegalStateException("reset failed");
		}
	}
}
