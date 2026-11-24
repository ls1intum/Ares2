package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Checks that {@link ReflectionTestUtils} passes a {@code SecurityException}
 * thrown by the called code on unchanged, so an Ares refusal stays visible as
 * itself, while other exceptions are still reported as test failures.
 */
public class ReflectionTestUtilsSecurityExceptionTest {

	/**
	 * The refusal the called code throws.
	 */
	private static final SecurityException REFUSAL = new SecurityException("Ares Security Error (test)");

	/**
	 * A class whose constructor and methods throw.
	 */
	public static final class Throwing {

		/**
		 * Throws the refusal when asked to.
		 *
		 * @param refuse whether to throw the refusal
		 */
		public Throwing(Boolean refuse) {
			if (refuse) {
				throw REFUSAL;
			}
		}

		/**
		 * Throws the refusal.
		 */
		public void refuse() {
			throw REFUSAL;
		}

		/**
		 * Throws an exception that is not a refusal.
		 */
		public void fail() {
			throw new IllegalStateException("not a refusal");
		}
	}

	/**
	 * A refusal thrown by a constructor reaches the test unchanged.
	 */
	@Test
	void newInstanceRethrowsTheRefusalUnchanged() {
		assertSame(REFUSAL, assertThrows(SecurityException.class,
				() -> ReflectionTestUtils.newInstance(Throwing.class.getName(), Boolean.TRUE)));
	}

	/**
	 * A refusal thrown by a method reaches the test unchanged.
	 */
	@Test
	void invokeMethodRethrowsTheRefusalUnchanged() {
		Object instance = ReflectionTestUtils.newInstance(Throwing.class.getName(), Boolean.FALSE);
		assertSame(REFUSAL,
				assertThrows(SecurityException.class, () -> ReflectionTestUtils.invokeMethod(instance, "refuse")));
	}

	/**
	 * Any other exception is still reported as a failed test.
	 */
	@Test
	void otherExceptionsStillFailTheTest() {
		Object instance = ReflectionTestUtils.newInstance(Throwing.class.getName(), Boolean.FALSE);
		assertThrows(AssertionFailedError.class, () -> ReflectionTestUtils.invokeMethod(instance, "fail"));
	}
}
