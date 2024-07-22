package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.security.ConfigurationException;
import de.tum.cit.ase.ares.integration.testuser.ValidationUser;
import de.tum.cit.ase.ares.testutilities.*;

@UserBased(ValidationUser.class)
class ValidationTest {

	@UserTestResults
	private static Events tests;

	private final String allowAndExcludeLocalPortIntersect = "allowAndExcludeLocalPortIntersect";
	private final String allowLocalPortInsideAllowAboveRange = "allowLocalPortInsideAllowAboveRange";
	private final String excludeLocalPortValueToSmall = "excludeLocalPortValueToSmall";
	private final String excludeLocalPortValueWithoutAllowAbove = "excludeLocalPortValueWithoutAllowAbove";
	private final String negativeAllowLocalPortAboveValue = "negativeAllowLocalPortAboveValue";
	private final String negativeAllowLocalPortValue = "negativeAllowLocalPortValue";
	private final String negativeExcludeLocalPortValue = "negativeExcludeLocalPortValue";
	private final String negativeMaxActiveCount = "negativeMaxActiveCount";
	private final String tooLargeAllowLocalPortAboveValue = "tooLargeAllowLocalPortAboveValue";
	private final String tooLargeAllowLocalPortValue = "tooLargeAllowLocalPortValue";
	private final String tooLargeExcludeLocalPortValue = "tooLargeExcludeLocalPortValue";

	@TestTest
	void test_allowAndExcludeLocalPortIntersect() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(allowAndExcludeLocalPortIntersect, ConfigurationException.class));
	}

	@TestTest
	void test_allowLocalPortInsideAllowAboveRange() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(allowLocalPortInsideAllowAboveRange, ConfigurationException.class));
	}

	@TestTest
	void test_excludeLocalPortValueToSmall() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(excludeLocalPortValueToSmall, ConfigurationException.class));
	}

	@TestTest
	void test_excludeLocalPortValueWithoutAllowAbove() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(excludeLocalPortValueWithoutAllowAbove, ConfigurationException.class));
	}

	@TestTest
	void test_negativeAllowLocalPortAboveValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(negativeAllowLocalPortAboveValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeAllowLocalPortValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(negativeAllowLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeExcludeLocalPortValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(negativeExcludeLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeMaxActiveCount() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1, testFailedWith(negativeMaxActiveCount, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeAllowLocalPortAboveValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(tooLargeAllowLocalPortAboveValue, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeAllowLocalPortValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(tooLargeAllowLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeExcludeLocalPortValue() {
		//OUTCOMMENTED: Removed configuration
		//tests.assertThatEvents().haveExactly(1,
		//		testFailedWith(tooLargeExcludeLocalPortValue, ConfigurationException.class));
	}
}
