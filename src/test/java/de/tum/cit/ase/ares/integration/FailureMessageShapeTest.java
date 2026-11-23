package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;
import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.assertj.core.api.Condition;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.io.IOTesterManager;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.integration.testuser.FailureMessageShapeUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.MessageTemplates;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Checks the message each kind of Ares failure leaves in the test report: a
 * {@link SecurityException}, one prefix with the right reason and stage, and
 * the concrete cause in the first line. ArchUnit and AspectJ only; the denials
 * in all four modes are in {@code FailureMessageShapeDenialTest}.
 */
@UserBased(FailureMessageShapeUser.class)
class FailureMessageShapeTest {

	/** The prefix of a failure of the student code while the test runs. */
	private static final String STUDENT_EXECUTION = "Ares Security Error (Reason: Student-Code; Stage: Execution): ";

	/** The prefix of a failure of the Ares setup while the test runs. */
	private static final String ARES_EXECUTION = "Ares Security Error (Reason: Ares-Code; Stage: Execution): ";

	/** The prefix of a failure of the Ares setup while the tests are built. */
	private static final String ARES_CREATION = "Ares Security Error (Reason: Ares-Code; Stage: Creation): ";

	/** Finds any Ares prefix, in English or German, to count them. */
	private static final Pattern ANY_PREFIX = Pattern.compile("Ares (Security Error|Sicherheitsfehler) \\(");

	/** The pattern Artemis uses to replace a timeout by its own explanation. */
	private static final Pattern ARTEMIS_TIMEOUT = Pattern.compile("^.*:(.*timed out after.*)");

	/**
	 * The fields of the object holding the misspelled one, as the reader lists
	 * them.
	 */
	private static final String KNOWN_RESOURCE_FIELDS = "regardingCommandExecutions, regardingFileSystemInteractions, "
			+ "regardingNetworkConnections, regardingPackageImports, regardingThreadCreations, regardingTimeouts";

	/** The results of the user's tests. */
	@UserTestResults
	private static Events tests;

	/**
	 * A strict timeout is the student code's failure while it runs, and keeps the
	 * words Artemis looks for after the prefix.
	 */
	@TestTest
	void strictTimeout() {
		String expected = english(() -> Messages.localized("timeout.failure_message", "100 ms"));
		tests.assertThatEvents().haveExactly(1, testFailedWith("strictTimeout", SecurityException.class, expected,
				shape(STUDENT_EXECUTION, message -> assertThat(message).matches(ARTEMIS_TIMEOUT))));
	}

	/** A hidden test before its deadline is refused by Ares while it runs. */
	@TestTest
	void hiddenTestBeforeDeadline() {
		String expected = english(() -> Messages.localized("test_guard.hidden_test_before_deadline_message"));
		tests.assertThatEvents().haveExactly(1, testFailedWith("hiddenTestBeforeDeadline", SecurityException.class,
				expected, shape(ARES_EXECUTION, message -> {
				})));
	}

	/**
	 * A misspelled policy field is refused while the tests are built, naming the
	 * file, the unknown field and the known ones in the first line, and keeping the
	 * cause.
	 */
	@TestTest
	void misspelledPolicyField() {
		String cause = english(() -> Messages.localized("policy.schema.unknown.field",
				"theFollowingResourceAccessesArePermitted", "regardingFileSystemInteraction", KNOWN_RESOURCE_FIELDS));
		Condition<Throwable> keepsTheCause = new Condition<>(failure -> failure.getCause() != null, "keeps the cause");
		Condition<Throwable> namesTheCause = shape(ARES_CREATION, firstLine -> {
			List<String> values = englishArguments("security.policy.data.bind.failed", 2, firstLine);
			assertThat(values.get(0)).endsWith("PolicyFailureMessageShapeMisspelledField.yaml");
			assertThat(values.get(1)).isEqualTo(cause);
		});
		tests.assertThatEvents().haveExactly(1, testFailedWith("misspelledPolicyField", SecurityException.class,
				ARES_CREATION, allOf(namesTheCause, keepsTheCause), Option.MESSAGE_CONTAINS));
	}

	/**
	 * Student code reading a file under the restrictive default policy is refused
	 * as the student code's failure, naming a student class in the first line,
	 * whether the analysis before the test or the check while it runs refuses it.
	 * The analysis covers the whole student package and names the first class it
	 * finds, so only the package is checked.
	 */
	@TestTest
	void defaultPolicyDenial() {
		String student = "de.tum.cit.ase.ares.integration.aop.forbidden.subject.";
		tests.assertThatEvents().haveExactly(1, testFailedWith("defaultPolicyDenial", SecurityException.class,
				STUDENT_EXECUTION, shape(STUDENT_EXECUTION, firstLine -> {
					Optional<List<String>> analysis = english(
							() -> MessageTemplates.argumentsOf("security.archunit.violation.error", 4, firstLine));
					Optional<List<String>> runtime = english(
							() -> MessageTemplates.argumentsOf("security.advice.illegal.file.execution", 5, firstLine));
					assertThat(analysis.isPresent() || runtime.isPresent()).as(firstLine).isTrue();
					analysis.ifPresent(values -> {
						assertThat(values.get(0)).contains(student);
						assertThat(values.get(1)).isEqualTo(
								english(() -> Messages.localized("security.archunit.action.file.system.access")));
					});
					runtime.ifPresent(values -> {
						assertThat(values.get(0)).contains(student);
						assertThat(values.get(4)).isEqualTo(
								english(() -> Messages.localized("security.advice.denial.reason.no.rule.file")));
					});
				}), Option.MESSAGE_CONTAINS));
	}

	/**
	 * An {@link IOTester} the I/O manager cannot provide fails with the Ares setup
	 * message. JUnit wraps it in its own parameter resolution exception, and Ares
	 * takes it out again, so the report shows the {@link SecurityException} itself.
	 */
	@TestTest
	void ioTesterWithoutManager() {
		String expected = english(() -> Messages.localized("io_tester.parameter_not_provided",
				FailureMessageShapeUser.class.getCanonicalName() + "#ioTesterWithoutManager",
				IOTester.class.getCanonicalName(), WithIOManager.None.class.getCanonicalName(),
				Messages.localized("io_tester.no_controller"), IOTesterManager.class.getCanonicalName()));
		assertThat(expected).startsWith(ARES_CREATION);
		tests.assertThatEvents().haveExactly(1,
				testFailedWith("ioTesterWithoutManager", SecurityException.class, expected));
	}

	/**
	 * Builds the checks every Ares failure shares: exactly a
	 * {@link SecurityException}, opening with the given prefix, carrying one prefix
	 * only, and a first line that passes the given check.
	 *
	 * @param prefix         the prefix with the expected reason and stage
	 * @param firstLineCheck asserts on the first line of the message
	 * @return the condition
	 */
	private static Condition<Throwable> shape(String prefix, Consumer<String> firstLineCheck) {
		return new Condition<>(failure -> {
			String message = String.valueOf(failure.getMessage());
			String firstLine = message.lines().findFirst().orElse("");
			assertThat(failure.getClass()).isEqualTo(SecurityException.class);
			assertThat(message).startsWith(prefix);
			assertThat(ANY_PREFIX.matcher(message).results().count()).as(message).isEqualTo(1);
			firstLineCheck.accept(firstLine);
			return true;
		}, "is a SecurityException opening with '%s' and stating its cause in the first line", prefix);
	}

	/**
	 * Reads the values of an English message written from a key, failing when the
	 * text does not have the key's shape.
	 *
	 * @param key       the catalogue key
	 * @param arguments how many values the key takes
	 * @param text      the text
	 * @return the values in the key's argument order
	 */
	private static List<String> englishArguments(String key, int arguments, String text) {
		Optional<List<String>> values = english(() -> MessageTemplates.argumentsOf(key, arguments, text));
		assertThat(values).as("'%s' has the shape of %s", text, key).isPresent();
		return values.orElseThrow();
	}

	/**
	 * Computes a value in English, the language the user's tests run in.
	 *
	 * @param <T>  the type of the value
	 * @param step computes the value
	 * @return the value
	 */
	private static <T> T english(Supplier<T> step) {
		Object[] result = new Object[1];
		AdviceActionWording.inLocale(Locale.ENGLISH, () -> {
			result[0] = step.get();
			return "";
		});
		@SuppressWarnings("unchecked")
		T value = (T) result[0];
		return value;
	}
}
