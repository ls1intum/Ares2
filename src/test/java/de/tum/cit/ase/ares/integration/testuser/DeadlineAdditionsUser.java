package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.jupiter.*;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@Deadline("2000-01-01 00:00")
// Scope the default-policy analysis to a benign student-like subtree (no reserved
// packages) instead of scanning all of Ares's own build, which the ReservedPackageGuard
// correctly rejects. Default policy (value left blank) and runtime enforcement are
// unchanged; only the analysed classpath is narrowed.
@Policy(withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
public class DeadlineAdditionsUser {

	@HiddenTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	@Deadline("2200-01-01 16:00")
	void testHidden_CustomDeadlineFutureActive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	@Deadline("2200-01-01 16:00")
	@ExtendedDeadline("50000d")
	void testHidden_CustomDeadlineFutureExtendedActive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2000-01-01 00:00")
	@Deadline("2200-01-01 16:00")
	@ExtendedDeadline("50000d")
	void testHidden_CustomDeadlineFutureExtendedInactive() {
		// nothing
	}

	@HiddenTest
	@ExtendedDeadline("50000d")
	@Deadline("2200-01-01 16:00")
	void testHidden_CustomDeadlineFutureExtendedNormal() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2000-01-01 00:00")
	@Deadline("2200-01-01 16:00")
	void testHidden_CustomDeadlineFutureInactive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	@ExtendedDeadline("50000d")
	@Deadline("2000-01-01 16:00")
	void testHidden_CustomDeadlinePastExtendedActive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2000-01-01 00:00")
	@ExtendedDeadline("50000d")
	@Deadline("2000-01-01 16:00")
	void testHidden_CustomDeadlinePastExtendedInactive() {
		// nothing
	}

	@HiddenTest
	@ExtendedDeadline("50000d")
	@Deadline("2000-01-01 16:00")
	void testHidden_CustomDeadlinePastExtendedNormal() {
		// nothing
	}

	@PublicTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	void testHiddenActive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	@ExtendedDeadline("50000d")
	void testHiddenExtendedActive() {
		// nothing
	}

	@HiddenTest
	@ActivateHiddenBefore("2000-01-01 00:00")
	@ExtendedDeadline("50000d")
	void testHiddenExtendedInactive() {
		// nothing
	}

	@HiddenTest
	@ExtendedDeadline("50000d")
	void testHiddenExtendedNormal() {
		// nothing
	}

	@PublicTest
	@ActivateHiddenBefore("2000-01-01 00:00")
	void testHiddenInactive() {
		// nothing
	}

	@PublicTest
	void testHiddenNormal() {
		// nothing
	}

	@PublicTest
	@ActivateHiddenBefore("2200-01-01 00:00")
	void testPublicActive() {
		// nothing
	}

	@PublicTest
	@ExtendedDeadline("50000d")
	void testPublicExtended() {
		// nothing
	}
}
