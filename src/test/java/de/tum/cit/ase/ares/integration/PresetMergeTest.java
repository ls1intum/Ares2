package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.PresetMergeUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

@UserBased(PresetMergeUser.class)
class PresetMergeTest {

	@UserTestResults
	private static Events tests;

	private final String readsAFileOnlyThePresetGrants = "readsAFileOnlyThePresetGrants";
	private final String stillDeniesAFileNeitherSourceGrants = "stillDeniesAFileNeitherSourceGrants";

	@TestTest
	void test_readsAFileOnlyThePresetGrants() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(readsAFileOnlyThePresetGrants));
	}

	@TestTest
	void test_stillDeniesAFileNeitherSourceGrants() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(stillDeniesAFileNeitherSourceGrants, SecurityException.class));
	}
}
