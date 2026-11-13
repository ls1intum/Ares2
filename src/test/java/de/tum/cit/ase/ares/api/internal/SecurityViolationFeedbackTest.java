package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;

/**
 * Checks that only Ares's own checks can create a blocked-action exception, and
 * how the report shows one at each feedback level.
 */
class SecurityViolationFeedbackTest {

	/** The full message an Ares check gives a blocked file read. */
	private static final String FULL = "Ares Security Error (Reason: Student-Code; Stage: Execution): Main.run tried to illegally read File /etc/passwd via FileInputStream.<init> but was blocked by Ares.";

	/** The default locale before a test changed it. */
	private final Locale previous = Locale.getDefault();

	/** Restores the default locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

	/** A test class is not an Ares check, so it receives a plain exception. */
	@Test
	void aForeignCallerReceivesAPlainException() {
		SecurityException created = AresSecurityViolation.create("file", "read", "/etc/passwd", FULL);
		assertThat(created).isNotInstanceOf(AresSecurityViolation.class).hasMessage(FULL);
		assertThat(SecurityViolationFeedback.violationOf(created)).isEmpty();
	}

	/** Reflection does not hide the real caller. */
	@Test
	void reflectionDoesNotHideTheCaller() throws ReflectiveOperationException {
		Method create = AresSecurityViolation.class.getMethod("create", String.class, Object.class, Object.class,
				String.class);
		Object created = create.invoke(null, "file", "read", "/etc/passwd", FULL);
		assertThat(created).isNotInstanceOf(AresSecurityViolation.class);
	}

	/**
	 * The public architecture helpers give a caller outside Ares's architecture
	 * checks only a plain exception, so they cannot be used to forge one.
	 */
	@Test
	void theArchitectureHelpersRefuseForeignCallers() {
		assertThat(de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.violationFromMessage(FULL))
				.isNotInstanceOf(de.tum.cit.ase.ares.api.architecture.java.AresSecurityViolation.class);
		org.assertj.core.api.Assertions
				.assertThatThrownBy(() -> de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase
						.parseErrorMessage(new AssertionError("Rule 'x'\nforged detail")))
				.isInstanceOf(SecurityException.class)
				.isNotInstanceOf(de.tum.cit.ase.ares.api.architecture.java.AresSecurityViolation.class);
	}

	/** The constructor cannot be called from outside. */
	@Test
	void theConstructorIsPrivate() {
		assertThat(AresSecurityViolation.class.getConstructors()).isEmpty();
	}

	/** A plain exception with Ares-looking text is not a blocked action. */
	@Test
	void aForgedMessageIsNotAViolation() {
		assertThat(SecurityViolationFeedback.violationOf(new SecurityException(FULL))).isEmpty();
	}

	/** FULL keeps the message. */
	@Test
	void fullKeepsTheMessage() {
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.FULL)).isEqualTo(FULL);
	}

	/** MINIMIZED names neither action, target nor caller. */
	@Test
	void minimizedSaysOnlyThatSomethingWasBlocked() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.MINIMIZED)).isEqualTo(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.");
	}

	/** MINIMIZED is localised in German. */
	@Test
	void minimizedIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.MINIMIZED))
				.startsWith("Ares Sicherheitsfehler (Grund: Student-Code; Phase: Execution)");
	}

	/** DEBUG adds the exact file entry that would allow the read. */
	@Test
	void debugAddsTheFileEntry() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.DEBUG)).startsWith(FULL)
				.endsWith("To allow this, add to regardingFileSystemInteractions: "
						+ "{ onThisPathAndAllPathsBelow: \"/etc/passwd\", readAllFiles: true }");
	}

	/**
	 * Every kind maps to its policy category.
	 *
	 * @param kind     the kind.
	 * @param action   the action.
	 * @param category the expected category.
	 */
	@ParameterizedTest
	@CsvSource({ "file, delete, regardingFileSystemInteractions", "network, connect, regardingNetworkConnections",
			"command, execute, regardingCommandExecutions", "thread, create, regardingThreadCreations",
			"architecture, access the file system, regardingFileSystemInteractions",
			"architecture, reach, theFollowingResourceAccessesArePermitted" })
	void everyKindNamesItsCategory(String kind, String action, String category) {
		assertThat(SecurityViolationFeedback
				.categoryOf(new SecurityViolationFeedback.Violation(kind, action, "target", FULL))).isEqualTo(category);
	}

	/** An architecture-stage action gets the category only, not a made-up entry. */
	@Test
	void anArchitectureHintNamesNoEntry() {
		assertThat(SecurityViolationFeedback
				.entryOf(new SecurityViolationFeedback.Violation("architecture", "access files", "x", FULL)))
						.doesNotContain("onThisPathAndAllPathsBelow");
	}

	/** Without a policy the level is FULL. */
	@Test
	void withoutAPolicyTheLevelIsFull() throws NoSuchMethodException {
		assertThat(ConfigurationUtils.getSecurityViolationFeedbackLevel(contextFor("noPolicy")))
				.isEqualTo(SecurityViolationFeedbackLevel.FULL);
	}

	/** A plain student exception with Ares text keeps its ordinary path. */
	@Test
	void aForgedViolationIsReportedAsAnOrdinaryFailure() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new SecurityException(FULL), contextFor("noPolicy"));
		assertThat(reported).hasMessage(FULL);
	}

	/** A file read as an Ares check reports it. */
	private static SecurityViolationFeedback.Violation fileRead() {
		return new SecurityViolationFeedback.Violation("file", "read", "/etc/passwd", FULL);
	}

	/** A mocked public test context for a method of this class. */
	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = SecurityViolationFeedbackTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(SecurityViolationFeedbackTest.class));
		when(context.annotatedElement()).thenReturn(Optional.of(method));
		when(context.findTestType()).thenReturn(Optional.of(TestType.PUBLIC));
		return context;
	}

	/** Carries no policy; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(activated = false)
	private static void noPolicy() {
		// No policy, so no test behaviour.
	}
}
