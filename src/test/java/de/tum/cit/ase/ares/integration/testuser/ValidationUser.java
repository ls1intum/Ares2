package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
// Replaces the former inert per-method @AllowLocalPort/@AllowThreads (deliberately invalid
// validation inputs, not representable as valid Ares 2 permissions) with an active @Policy
// granting no resource accesses, scanned against the benign HelloWorld subject.
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyValidationUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
public class ValidationUser {

	@Public
	@Test
	void allowAndExcludeLocalPortIntersect() {
		// nothing
	}

	@Public
	@Test
	void allowLocalPortInsideAllowAboveRange() {
		// nothing
	}

	@Public
	@Test
	void excludeLocalPortValueToSmall() {
		// nothing
	}

	@Public
	@Test
	void excludeLocalPortValueWithoutAllowAbove() {
		// nothing
	}

	@Public
	@Test
	void negativeAllowLocalPortAboveValue() {
		// nothing
	}

	@Public
	@Test
	void negativeAllowLocalPortValue() {
		// nothing
	}

	@Public
	@Test
	void negativeExcludeLocalPortValue() {
		// nothing
	}

	@Public
	@Test
	void negativeMaxActiveCount() {
		// nothing
	}

	@Public
	@Test
	void tooLargeAllowLocalPortAboveValue() {
		// nothing
	}

	@Public
	@Test
	void tooLargeAllowLocalPortValue() {
		// nothing
	}

	@Public
	@Test
	void tooLargeExcludeLocalPortValue() {
		// nothing
	}
}
