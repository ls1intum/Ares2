package example.nest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * An enclosing class that is not a test itself and holds a nested test class,
 * so a scanner has to name the nested one as its class file is named.
 */
public final class NestedTestFixture {

	/** Prevents instances of the fixture. */
	private NestedTestFixture() {
	}

	/** The nested test class; its one test does nothing. */
	@Nested
	final class Inner {

		/** Does nothing. */
		@Test
		void runs() {
		}
	}
}
