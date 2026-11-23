package de.tum.cit.ase.ares.api.io;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Checks the output limit of the stream that records what a test prints. */
class TestOutStreamTest {

	/**
	 * Output up to the limit is taken, and the first character beyond it is refused
	 * with the Ares error template.
	 */
	@Test
	void refusesOutputBeyondTheLimit() {
		TestOutStream stream = new TestOutStream(output -> {
		}, null, 3);
		assertDoesNotThrow(() -> stream.write(new byte[] { 'a', 'b', 'c' }));
		SecurityException refused = assertThrows(SecurityException.class, () -> stream.write('d'));
		assertEquals(localized("output_tester.output_maxExceeded", 4L), refused.getMessage());
	}
}
