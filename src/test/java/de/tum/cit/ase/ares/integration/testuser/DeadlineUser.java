package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.*;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@Deadline("2200-01-01 16:00")
@TestMethodOrder(MethodName.class)
// Scope the default-policy analysis to a benign student-like subtree (no reserved
// packages) instead of scanning all of Ares's own build, which the ReservedPackageGuard
// correctly rejects. Default policy (value left blank) and runtime enforcement are
// unchanged; only the analysed classpath is narrowed.
@Policy(withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
public class DeadlineUser {

	@HiddenTest
	@Deadline("2200-01-01 16:00")
	void testHiddenCustomDeadlineFuture() {
		// nothing
	}

	@HiddenTest
	@Deadline("2000-01-01 16:00")
	void testHiddenCustomDeadlinePast() {
		// nothing
	}

	@HiddenTest
	void testHiddenNormal() {
		// nothing
	}

	@HiddenTest
	@Deadline("2000-01-01 16:00 Europe/Berlin")
	void testHiddenTimeZoneBerlin() {
		// nothing
	}

	@HiddenTest
	@Deadline("2000-01-01 16:00 UTC")
	void testHiddenTimeZoneUtc() {
		// nothing
	}

	@PublicTest
	@Deadline("2000-01-01 16:00")
	void testPublicCustomDeadline() {
		// nothing
	}

	@PublicTest
	void testPublicNormal() {
		// nothing
	}
}
