package de.tum.cit.ase.ares.api.util;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Checks that the dependency manager refuses to start without a build file. */
class DependencyManagerTest {

	/** A null path is refused with the Ares error template. */
	@Test
	void refusesANullPath() {
		SecurityException refused = assertThrows(SecurityException.class, () -> new DependencyManager(null));
		assertEquals(localized("security.dependency.manager.path.empty"), refused.getMessage());
	}

	/** An empty path is refused with the Ares error template. */
	@Test
	void refusesAnEmptyPath() {
		SecurityException refused = assertThrows(SecurityException.class, () -> new DependencyManager(""));
		assertEquals(localized("security.dependency.manager.path.empty"), refused.getMessage());
	}
}
