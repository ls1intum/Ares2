package de.tum.cit.ase.ares.integration.architecture.forbidden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

/**
 * Checks which failures count as Ares having denied a forbidden test subject.
 * The method names end in the suffix the ArchUnit and AspectJ CI jobs select.
 */
class ExpectedDenialRecognitionTest {

	/** An English report about student code counts as a denial. */
	@Test
	void test_englishStudentCodeReportAcceptedMavenArchunitAspectJ_test() {
		assertTrue(SystemAccessTest.isExpectedDenial(new SecurityException(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked.")));
	}

	/** A German report about student code counts as a denial. */
	@Test
	void test_germanStudentCodeReportAcceptedMavenArchunitAspectJ_test() {
		assertTrue(SystemAccessTest.isExpectedDenial(
				new SecurityException("Ares Sicherheitsfehler (Grund: Student-Code; Phase: Erstellung): blockiert.")));
	}

	/** A report about student code inside a failed static initialiser counts. */
	@Test
	void test_wrappedStudentCodeReportAcceptedMavenArchunitAspectJ_test() {
		assertTrue(SystemAccessTest.isExpectedDenial(new ExceptionInInitializerError(new SecurityException(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."))));
	}

	/** A refusal because no classes were found is not a denial of the code. */
	@Test
	void test_setupRefusalRejectedMavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest.isExpectedDenial(new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Creation): Ares found no compiled classes in target.")));
	}

	/** Mentioning student code later in a message does not make it a denial. */
	@Test
	void test_laterStudentCodeMentionRejectedMavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest.isExpectedDenial(
				new SecurityException("Setup failed before (Reason: Student-Code; Stage: Execution) was reported.")));
	}

	/** An assertion failure is not a denial, whatever its message says. */
	@Test
	void test_assertionFailureRejectedMavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest
				.isExpectedDenial(new AssertionError("Exception message should contain the path of the file.")));
	}

	/**
	 * A rule that looked at no classes is not a denial, even after Ares has turned
	 * ArchUnit's complaint into a report about student code.
	 */
	@Test
	void test_emptyRuleReportRejectedMavenArchunitAspectJ_test() {
		JavaClasses noClasses = new ClassFileImporter().importClasses();
		ArchRule rule = ArchRuleDefinition.noClasses().should().bePublic().allowEmptyShould(false);
		AssertionError emptyRule = assertThrows(AssertionError.class, () -> rule.check(noClasses));
		SecurityException converted = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(emptyRule));
		assertFalse(SystemAccessTest.isExpectedDenial(converted), converted::getMessage);
	}

	/** A wrapper other than a failed static initialiser is not accepted. */
	@Test
	void test_unsupportedWrapperRejectedMavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest.isExpectedDenial(new IllegalStateException(new SecurityException(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."))));
	}
}
