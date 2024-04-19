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
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(allowAndExcludeLocalPortIntersect, ConfigurationException.class));
	}

	@TestTest
	void test_allowLocalPortInsideAllowAboveRange() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(allowLocalPortInsideAllowAboveRange, ConfigurationException.class));
	}

	@TestTest
	void test_excludeLocalPortValueToSmall() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(excludeLocalPortValueToSmall, ConfigurationException.class));
	}

	@TestTest
	void test_excludeLocalPortValueWithoutAllowAbove() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(excludeLocalPortValueWithoutAllowAbove, ConfigurationException.class));
	}

	@TestTest
	void test_negativeAllowLocalPortAboveValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(negativeAllowLocalPortAboveValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeAllowLocalPortValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(negativeAllowLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeExcludeLocalPortValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(negativeExcludeLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_negativeMaxActiveCount() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(negativeMaxActiveCount, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeAllowLocalPortAboveValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(tooLargeAllowLocalPortAboveValue, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeAllowLocalPortValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(tooLargeAllowLocalPortValue, ConfigurationException.class));
	}

	@TestTest
	void test_tooLargeExcludeLocalPortValue() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(tooLargeExcludeLocalPortValue, ConfigurationException.class));
	}
}
