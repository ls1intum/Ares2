package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.MultipleFailuresError;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

/**
 * Checks that only Ares's own checks can create a blocked-action exception, and
 * how the report shows one at each feedback level.
 */
class SecurityViolationFeedbackTest {

	/** The MINIMIZED text, in English. */
	private static final String MINIMIZED_TEXT = "Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.";

	/** What a failure that could not be read completely is reported as. */
	private static final String UNINSPECTABLE_TEXT = "Ares could not read this failure completely, so its details are not shown.";

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
						+ "{ onThisPathAndAllPathsBelow: \"/etc/passwd\", readAllFiles: true, overwriteAllFiles: false, "
						+ "createAllFiles: false, executeAllFiles: false, deleteAllFiles: false }");
	}

	/**
	 * An argument Ares refused to inspect cannot be allowed, so DEBUG shows the
	 * report without a hint and MINIMIZED shows only the fixed text.
	 */
	@Test
	void anUninspectableArgumentGetsNoHint() {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation untrusted = new SecurityViolationFeedback.Violation("uninspectable",
				"inspect", "org.example.StudentFile", FULL);
		assertThat(SecurityViolationFeedback.render(untrusted, SecurityViolationFeedbackLevel.DEBUG)).isEqualTo(FULL);
		assertThat(SecurityViolationFeedback.render(untrusted, SecurityViolationFeedbackLevel.MINIMIZED))
				.doesNotContain("org.example.StudentFile").contains("An operation was blocked by Ares.");
	}

	/**
	 * The runtime-type guard of each backend throws a real violation, which the
	 * report recognises, while the same text thrown by a student is not one.
	 *
	 * @throws ReflectiveOperationException if a guard cannot be reached
	 */
	@Test
	void theRuntimeTypeGuardsThrowRealViolations() throws ReflectiveOperationException {
		Object studentValue = new SelfCausingException();
		for (Class<?> owner : List.of(Class.forName(
				"de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox"),
				Class.forName(
						"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJAbstractAdviceDefinitions"))) {
			Method guard = owner.getDeclaredMethod("requireTrustedRuntimeType", Object.class);
			guard.setAccessible(true);
			Throwable thrown = invocationFailure(guard, studentValue);
			assertThat(SecurityViolationFeedback.violationOf(thrown)).get()
					.extracting(SecurityViolationFeedback.Violation::kind).isEqualTo("uninspectable");
		}
		assertThat(SecurityViolationFeedback.violationOf(new SecurityException(FULL))).isEmpty();
	}

	/**
	 * Calls a static guard and returns what it threw.
	 *
	 * @param guard the guard.
	 * @param value the value it checks.
	 * @return the thrown failure.
	 * @throws IllegalAccessException if the guard cannot be called
	 */
	private static Throwable invocationFailure(Method guard, Object value) throws IllegalAccessException {
		try {
			guard.invoke(null, value);
		} catch (java.lang.reflect.InvocationTargetException thrown) {
			return thrown.getCause();
		}
		throw new AssertionError("the guard accepted a student type: " + guard.getDeclaringClass());
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
			"architecture, import, regardingPackageImports",
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

	/**
	 * The DEBUG file entry is one the policy reader accepts as written, and a
	 * Windows path with a quote reads back unchanged.
	 *
	 * @param tempDir where the policy is written.
	 * @throws IOException if the policy cannot be written
	 */
	@Test
	void theDebugFileEntryIsReadBackByThePolicyReader(@TempDir Path tempDir) throws IOException {
		String target = "C:\\temp\\new \"draft\".txt";
		String entry = SecurityViolationFeedback
				.entryOf(new SecurityViolationFeedback.Violation("file", "create", target, FULL));
		Path policy = tempDir.resolve("Policy.yaml");
		Files.writeString(policy, """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions:
				      - %s
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				""".formatted(entry));
		FilePermission read = new SecurityPolicyYAMLReader(new YAMLMapper(), tempDir).readSecurityPolicyFrom(policy)
				.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted()
				.regardingFileSystemInteractions().get(0);
		assertThat(read.onThisPathAndAllPathsBelow()).isEqualTo(target);
		assertThat(read.createAllFiles()).isTrue();
		assertThat(read.readAllFiles() || read.overwriteAllFiles() || read.executeAllFiles() || read.deleteAllFiles())
				.isFalse();
	}

	/**
	 * A blocked action carried as a suppressed failure, inside a privileged
	 * exception or inside a multiple-failures error is shortened at MINIMIZED.
	 *
	 * @param carrier how the blocked action is carried.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(strings = { "suppressed", "privileged", "multiple" })
	void aCarriedViolationIsShortened(String carrier) throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		SecurityException violation = realViolation();
		Throwable failure = switch (carrier) {
		case "suppressed" -> {
			IllegalStateException outer = new IllegalStateException("closing failed");
			outer.addSuppressed(violation);
			yield outer;
		}
		case "privileged" -> new PrivilegedException(violation);
		default -> new MultipleFailuresError("two failures", List.of(new AssertionError("first"), violation));
		};
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("minimizedPolicy"));
		assertThat(reported).hasMessage(MINIMIZED_TEXT).hasNoSuppressedExceptions();
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * With only privileged failures reported, a student exception whose own
	 * getCause throws gets the policy's message, and its getCause never runs.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aStudentGetCauseIsNeverCalled() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new SelfCausingException(),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
		assertThat(SelfCausingException.asked).isFalse();
	}

	/**
	 * A student exception whose own getCause hands back a real blocked action is
	 * not shown in full at MINIMIZED: the search skips that getter, so the report
	 * says only that the failure could not be read completely.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aWrapperWithItsOwnGetCauseIsRedactedAtMinimized() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("minimizedPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT);
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * Without a feedback level below FULL, the same wrapper is reported as before.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aWrapperWithItsOwnGetCauseKeepsItsReportAtFull() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("noPolicy"));
		assertThat(reported).hasMessageNotContaining(UNINSPECTABLE_TEXT);
	}

	/**
	 * At FULL with only privileged failures reported, a blocked action suppressed
	 * on a student exception whose own getCause never returns is reported by
	 * itself, and that getCause is never called.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aViolationOnAWrapperWithItsOwnGetCauseIsReportedByItselfAtFull() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		HangingCauseException wrapper = new HangingCauseException();
		wrapper.addSuppressed(realViolation());
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(wrapper, contextFor("privilegedFullPolicy")));
			assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(FULL);
			assertThat(reported.getStackTrace()).isNotEmpty();
			assertThat(HangingCauseException.asked).isFalse();
		} finally {
			HangingCauseException.asked = false;
		}
	}

	/**
	 * At MINIMIZED with only privileged failures reported, a privileged exception
	 * carrying a student exception whose own getCause hands back a blocked action
	 * is not shown in full.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aPrivilegedCarrierWithItsOwnGetCauseIsRedactedAtMinimized() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new PrivilegedException(new WrappingException(realViolation())), contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT);
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * With only privileged failures reported, an ordinary student wrapper whose own
	 * getCause hands back a blocked action still gets the policy's message.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void anOrdinaryWrapperWithItsOwnGetCauseGetsThePolicyMessage() throws ReflectiveOperationException {
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
	}

	/**
	 * A blocked action behind more failures than the search reads is not shown in
	 * full: the report says only that the failure could not be read completely.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aViolationPastTheSearchLimitIsRedacted() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		RuntimeException failure = new RuntimeException("many failures");
		for (int index = 0; index < 256; index++) {
			failure.addSuppressed(new IllegalStateException("harmless " + index));
		}
		failure.addSuppressed(realViolation());
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("minimizedPolicy"));
		assertThat(reported).hasMessage(UNINSPECTABLE_TEXT).hasNoSuppressedExceptions();
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * A failure whose lock another thread holds cannot stop the report: Ares gives
	 * up reading it and shows only that it could not be read.
	 *
	 * @throws Exception if the fixture cannot be set up
	 */
	@Test
	void aLockedFailureDoesNotStopTheReport() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		RuntimeException failure = new RuntimeException("locked");
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Thread holder = new Thread(() -> holdLock(failure, locked, release));
		holder.setDaemon(true);
		holder.start();
		locked.await();
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(failure, contextFor("minimizedPolicy")));
			assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
			assertThat(reported.getStackTrace()).isEmpty();
		} finally {
			release.countDown();
		}
	}

	/**
	 * A failure whose own getMessage never returns, carrying a suppressed blocked
	 * action, cannot stop the report at FULL: after the bound, the blocked action
	 * is reported by itself, also with only privileged failures reported.
	 *
	 * @param policy the method carrying the policy.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(strings = { "noPolicy", "privilegedFullPolicy" })
	void aLoopingMessageOnAViolationCarrierReportsTheViolationAtFull(String policy)
			throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		LoopingMessageException wrapper = new LoopingMessageException();
		wrapper.addSuppressed(realViolation());
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(wrapper, contextFor(policy)));
			assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(FULL)
					.hasNoSuppressedExceptions();
			assertThat(reported.getStackTrace()).isNotEmpty();
		} finally {
			wrapper.looping = false;
		}
	}

	/**
	 * Without a blocked action in it, a failure whose own getMessage never returns
	 * is reported at FULL as a failure that could not be read.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aLoopingMessageWithoutAViolationIsRedactedAtFull() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("noPolicy");
		LoopingMessageException failure = new LoopingMessageException();
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(failure, context));
			assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
		} finally {
			failure.looping = false;
		}
	}

	/**
	 * An ordinary failure at FULL keeps its report.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anOrdinaryFailureKeepsItsReportAtFull() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new IllegalStateException("plain failure"),
				contextFor("noPolicy"));
		assertThat(reported).isInstanceOf(IllegalStateException.class).hasMessage("plain failure");
	}

	/**
	 * With only privileged failures reported, an ordinary failure the search could
	 * not finish still gets the policy's message, which reveals nothing of it.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anIncompleteSearchOfAnOrdinaryFailureGetsThePolicyMessage() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(withMoreFailuresThanTheSearchReads(),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
	}

	/**
	 * With only privileged failures reported, a privileged failure the search could
	 * not finish is still redacted, since its own report would be shown.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anIncompleteSearchOfAPrivilegedFailureIsRedacted() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new PrivilegedException(withMoreFailuresThanTheSearchReads()), contextFor("privilegedOnlyPolicy"));
		assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
	}

	/**
	 * Failures whose lock stays held cannot make Ares start ever more threads: once
	 * every worker is taken, a report is redacted at once, and reports work again
	 * when the workers are free.
	 *
	 * @throws Exception if the fixture cannot be set up
	 */
	@Test
	void stalledWorkersAreBounded() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("minimizedPolicy");
		CountDownLatch release = new CountDownLatch(1);
		List<Thread> reporters = new java.util.ArrayList<>();
		try {
			for (int index = 0; index < ReportingUtils.MAX_INSPECTION_THREADS; index++) {
				RuntimeException failure = new RuntimeException("locked " + index);
				CountDownLatch locked = new CountDownLatch(1);
				Thread holder = new Thread(() -> holdLock(failure, locked, release));
				holder.setDaemon(true);
				holder.start();
				locked.await();
				Thread reporter = new Thread(() -> ReportingUtils.processThrowable(failure, context));
				reporter.setDaemon(true);
				reporter.start();
				reporters.add(reporter);
			}
			for (Thread reporter : reporters) {
				reporter.join(10_000);
			}
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(1),
					() -> ReportingUtils.processThrowable(new IllegalStateException("free"), context));
			assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
		} finally {
			release.countDown();
		}
		assertTimeoutPreemptively(Duration.ofSeconds(10), () -> awaitFreeWorkers(context));
	}

	/**
	 * Waits until a report is no longer redacted for want of a free worker.
	 *
	 * @param context the test context.
	 * @throws InterruptedException if interrupted while waiting
	 */
	private static void awaitFreeWorkers(TestContext context) throws InterruptedException {
		while (UNINSPECTABLE_TEXT
				.equals(ReportingUtils.processThrowable(new IllegalStateException("free"), context).getMessage())) {
			Thread.sleep(50);
		}
	}

	/**
	 * An ordinary failure carrying more harmless failures than the search reads.
	 *
	 * @return the failure.
	 */
	private static RuntimeException withMoreFailuresThanTheSearchReads() {
		RuntimeException failure = new RuntimeException("many failures");
		for (int index = 0; index <= 256; index++) {
			failure.addSuppressed(new IllegalStateException("harmless " + index));
		}
		return failure;
	}

	/**
	 * Holds a failure's lock until released, as student code on an allowed thread
	 * could.
	 *
	 * @param failure the failure to lock.
	 * @param locked  counted down once the lock is held.
	 * @param release awaited while holding the lock.
	 */
	private static void holdLock(Throwable failure, CountDownLatch locked, CountDownLatch release) {
		synchronized (failure) {
			locked.countDown();
			try {
				release.await();
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Creates a violation the way Ares's checks do.
	 *
	 * @return the violation.
	 * @throws ReflectiveOperationException if it cannot be created
	 */
	private static SecurityException realViolation() throws ReflectiveOperationException {
		java.lang.reflect.Constructor<AresSecurityViolation> constructor = AresSecurityViolation.class
				.getDeclaredConstructor(String.class, String.class, String.class, String.class);
		constructor.setAccessible(true);
		return constructor.newInstance("file", "read", "/etc/passwd", FULL);
	}

	/** A student exception whose getCause never returns normally. */
	private static final class SelfCausingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether anything asked for the cause. */
		private static volatile boolean asked;

		/**
		 * Records the call and fails, as a hostile override could.
		 *
		 * @return never.
		 */
		@Override
		public synchronized Throwable getCause() {
			asked = true;
			throw new AssertionError("student getCause");
		}
	}

	/** A student exception whose own getCause would never return. */
	private static final class HangingCauseException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether anything asked for the cause. */
		private static volatile boolean asked;

		/**
		 * Records the call and then waits for ever, as a hostile override could.
		 *
		 * @return never.
		 */
		@Override
		public synchronized Throwable getCause() {
			asked = true;
			while (asked) {
				Thread.onSpinWait();
			}
			return null;
		}
	}

	/**
	 * A student exception that keeps the Java runtime's getCause but whose own
	 * getMessage would never return.
	 */
	private static final class LoopingMessageException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether getMessage keeps looping; a test sets it to false to let it end. */
		private transient volatile boolean looping = true;

		/**
		 * Waits while looping, as a hostile override could.
		 *
		 * @return a fixed text once the test lets it end.
		 */
		@Override
		public String getMessage() {
			while (looping) {
				Thread.onSpinWait();
			}
			return "looped";
		}
	}

	/** A student exception whose own getCause returns a failure it was given. */
	private static final class WrappingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The failure this exception claims as its cause. */
		private final transient Throwable wrapped;

		/**
		 * Wraps a failure without handing it to the superclass.
		 *
		 * @param wrapped the failure to return as the cause.
		 */
		WrappingException(Throwable wrapped) {
			super("wrapped");
			this.wrapped = wrapped;
		}

		/**
		 * Returns the wrapped failure, as an override student code writes could.
		 *
		 * @return the wrapped failure.
		 */
		@Override
		public synchronized Throwable getCause() {
			return wrapped;
		}
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

	/** Carries a policy at MINIMIZED; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelMINIMIZED.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void minimizedPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/**
	 * Carries a privileged-only policy at MINIMIZED; used only through the mocked
	 * context.
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelPrivilegedMinimized.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void privilegedOnlyPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/**
	 * Carries a privileged-only policy at the default FULL level; used only through
	 * the mocked context.
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPrivilegedExceptionUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void privilegedFullPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/** Carries no policy; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(activated = false)
	private static void noPolicy() {
		// No policy, so no test behaviour.
	}
}
