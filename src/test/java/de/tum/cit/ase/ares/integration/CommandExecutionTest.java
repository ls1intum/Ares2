package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.CommandExecutionUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

@UserBased(CommandExecutionUser.class)
public class CommandExecutionTest {

	@UserTestResults
	private static Events tests;

	@TestTest
	void test_testExecuteCommand() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testExecuteCommand", SecurityException.class));
	}
}
