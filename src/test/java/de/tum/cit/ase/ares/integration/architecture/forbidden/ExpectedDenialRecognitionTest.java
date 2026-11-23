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

	/** A kept failure of an English student-code report counts as a denial. */
	@Test
	void cachedFailureFromEnglishStudentCodeIsADenial_MavenArchunitAspectJ_test() {
		assertTrue(isAcceptedAsCachedDenial(EnglishStudentCodeFixture.class));
	}

	/** A kept failure of a German student-code report counts as a denial. */
	@Test
	void cachedFailureFromGermanStudentCodeIsADenial_MavenArchunitAspectJ_test() {
		assertTrue(isAcceptedAsCachedDenial(GermanStudentCodeFixture.class));
	}

	/** A kept failure of a broken test setup is not a denial. */
	@Test
	void cachedFailureFromSetupIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(isAcceptedAsCachedDenial(SetupFixture.class));
	}

	/**
	 * A kept failure that is no security exception but quotes the header is not a
	 * denial.
	 */
	@Test
	void cachedFailureQuotingTheHeaderIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(isAcceptedAsCachedDenial(QuotedHeaderFixture.class));
	}

	/**
	 * A kept failure blaming Ares, which mentions student code later, is not a
	 * denial.
	 */
	@Test
	void cachedFailureFromAresCodeIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(isAcceptedAsCachedDenial(AresCodeFixture.class));
	}

	/** A kept failure of a rule that looked at no classes is not a denial. */
	@Test
	void cachedFailureFromEmptyRuleIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(isAcceptedAsCachedDenial(EmptyRuleFixture.class));
	}

	/**
	 * A kept student-code failure of a class other than the one asked about is not
	 * a denial.
	 */
	@Test
	void cachedFailureOfAnotherClassIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest.isCachedInitialiserFailureFromStudentCode(cachedFailureOf(OtherClassFixture.class),
				"de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase"));
	}

	/**
	 * An error that only looks like a kept failure, with no first failure inside,
	 * is not a denial.
	 */
	@Test
	void uncachedNoClassDefFoundErrorIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(SystemAccessTest.isCachedInitialiserFailureFromStudentCode(
				new NoClassDefFoundError("Could not initialize class X"), "X"));
	}

	/** A kept failure of a subclass of the security exception is not a denial. */
	@Test
	void cachedFailureFromSecurityExceptionSubclassIsNoDenial_MavenArchunitAspectJ_test() {
		assertFalse(isAcceptedAsCachedDenial(SubclassFixture.class));
	}

	/**
	 * Asks whether the failure the JVM keeps for this fixture counts as a denial of
	 * the fixture itself.
	 *
	 * @param fixture a class whose static initialiser throws
	 * @return whether the kept failure is accepted
	 */
	private static boolean isAcceptedAsCachedDenial(Class<?> fixture) {
		return SystemAccessTest.isCachedInitialiserFailureFromStudentCode(cachedFailureOf(fixture), fixture.getName());
	}

	/**
	 * Initialises a fixture twice and returns the second error. The JVM throws the
	 * first failure only once; afterwards it throws the error it keeps for the
	 * class.
	 *
	 * @param fixture a class whose static initialiser throws
	 * @return the error of the second attempt
	 */
	private static NoClassDefFoundError cachedFailureOf(Class<?> fixture) {
		assertThrows(LinkageError.class, () -> initialise(fixture));
		return assertThrows(NoClassDefFoundError.class, () -> initialise(fixture));
	}

	/**
	 * Runs the static initialiser of a class, if it has not run yet.
	 *
	 * @param fixture the class to initialise
	 * @throws ClassNotFoundException never, since the class is already loaded
	 */
	private static void initialise(Class<?> fixture) throws ClassNotFoundException {
		Class.forName(fixture.getName(), true, fixture.getClassLoader());
	}

	/**
	 * Throws the given failure. A static initialiser calls it, because the compiler
	 * refuses an initialiser that can only throw.
	 *
	 * @param failure what the initialiser fails with
	 */
	private static void failWith(RuntimeException failure) {
		throw failure;
	}

	/** A security exception of its own type, carrying the student-code header. */
	static final class StudentCodeSecurityException extends SecurityException {

		/** The version of this exception's serialised form. */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates the exception.
		 *
		 * @param message the text of the report
		 */
		StudentCodeSecurityException(String message) {
			super(message);
		}
	}

	/** Fails to initialise with an English student-code report. */
	static final class EnglishStudentCodeFixture {
		static {
			failWith(new SecurityException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."));
		}
	}

	/** Fails to initialise with a German student-code report. */
	static final class GermanStudentCodeFixture {
		static {
			failWith(new SecurityException(
					"Ares Sicherheitsfehler (Grund: Student-Code; Phase: Erstellung): blockiert."));
		}
	}

	/** Fails to initialise because a test setup step went wrong. */
	static final class SetupFixture {
		static {
			failWith(new IllegalArgumentException("bad file"));
		}
	}

	/**
	 * Fails to initialise with no security exception, whose text opens with the
	 * header.
	 */
	static final class QuotedHeaderFixture {
		static {
			failWith(new IllegalStateException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."));
		}
	}

	/**
	 * Fails to initialise with a report blaming Ares that mentions student code
	 * later.
	 */
	static final class AresCodeFixture {
		static {
			failWith(new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): no"
					+ " Ares Security Error (Reason: Student-Code; Stage: Execution) could be reported."));
		}
	}

	/**
	 * Fails to initialise with a student-code report of a rule that looked at no
	 * classes.
	 */
	static final class EmptyRuleFixture {
		static {
			failWith(new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution):"
					+ " Rule 'no classes should be public' failed to check any classes."));
		}
	}

	/**
	 * Fails to initialise with an English student-code report, to be asked about
	 * under another name.
	 */
	static final class OtherClassFixture {
		static {
			failWith(new SecurityException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."));
		}
	}

	/**
	 * Fails to initialise with a subclass of the security exception carrying the
	 * header.
	 */
	static final class SubclassFixture {
		static {
			failWith(new StudentCodeSecurityException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): the student code was blocked."));
		}
	}
}
