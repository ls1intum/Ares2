package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;

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
public class HiddenPublicUser {

	@Hidden
	@Test
	@Deadline("2200-01-01 16:00")
	void testHiddenCustomDeadlineFuture() {
		// nothing
	}

	@Hidden
	@Test
	@Deadline("2000-01-01 16:00")
	void testHiddenCustomDeadlinePast() {
		// nothing
	}

	@Hidden
	void testHiddenIncomplete() {
		// nothing
	}

	@Hidden
	@Test
	void testHiddenNormal() {
		// nothing
	}

	@Public
	@Test
	@Deadline("2200-01-01 16:00")
	void testPublicCustomDeadline() {
		// nothing
	}

	@Public
	void testPublicIncomplete() {
		// nothing
	}

	@Public
	@Test
	void testPublicNormal() {
		// nothing
	}
}
