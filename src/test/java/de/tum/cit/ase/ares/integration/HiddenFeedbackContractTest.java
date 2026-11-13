package de.tum.cit.ase.ares.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.ReportingUtils;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.integration.testuser.FutureHiddenConstructorUser;
import de.tum.cit.ase.ares.integration.testuser.HiddenCallbackFailureUser;
import de.tum.cit.ase.ares.integration.testuser.HiddenFeedbackUser;
import de.tum.cit.ase.ares.integration.testuser.HiddenLifecycleOutputUser;
import de.tum.cit.ase.ares.integration.testuser.HiddenSecurityCallbackFailureUser;
import de.tum.cit.ase.ares.integration.testuser.InheritedMixedVisibilityOutputUser;
import de.tum.cit.ase.ares.integration.testuser.InstructorCallbackHiddenUser;
import de.tum.cit.ase.ares.integration.testuser.MixedLifecycleRegressionUser;
import de.tum.cit.ase.ares.integration.testuser.MixedVisibilityOutputUser;
import de.tum.cit.ase.ares.integration.testuser.NestedHiddenLifecycleUser;
import de.tum.cit.ase.ares.integration.testuser.SharedHiddenVisibilityUsers;
import de.tum.cit.ase.ares.integration.testuser.UnsupervisedFailureUser;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Verifies the shared hidden-result contract through Jupiter and direct
 * reporting.
 */
@UserBased(HiddenFeedbackUser.class)
class HiddenFeedbackContractTest {

	/** Results of the supervised fixture class. */
	@UserTestResults
	private static Events tests;

	/** Hidden failures keep failed status and drop their throwable details. */
	@Test
	void hiddenFailureHasNoDiagnostics() {
		assertHiddenFailure("hiddenTestLeakingSecret");
		assertHiddenFailure("hiddenTestWithInternalAresError");
		assertHiddenFailure("hiddenTimeoutWithSecret");
	}

	/** A hidden abort keeps its result status without its private reason. */
	@Test
	void hiddenAbortKeepsStatusWithoutReason() {
		TestExecutionResult result = result("hiddenAbortWithSecret");
		assertEquals(TestExecutionResult.Status.ABORTED, result.getStatus());
		assertNoThrowableDetails(result.getThrowable().orElseThrow());
	}

	/**
	 * Constructor, lifecycle, and custom-manager output stay off standard streams.
	 */
	@Test
	void hiddenLifecycleAndCustomIoHaveNoStreams() {
		PrintStream previousOut = System.out;
		PrintStream previousErr = System.err;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			System.setErr(capture);
			Events lifecycle = EngineTestKit.engine("junit-jupiter")
					.selectors(selectClass(HiddenLifecycleOutputUser.class)).execute().testEvents();
			assertThat(lifecycle.failed().count()).isEqualTo(1);
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
			capture.close();
		}
		assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain("SECRET_HIDDEN_");
	}

	/** A mixed class with hidden tests suppresses shared lifecycle output. */
	@Test
	void mixedClassKeepsPublicBodyButHidesSharedLifecycle() {
		PrintStream previousOut = System.out;
		PrintStream previousErr = System.err;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			System.setErr(capture);
			Events mixed = EngineTestKit.engine("junit-jupiter").selectors(selectClass(MixedVisibilityOutputUser.class))
					.execute().testEvents();
			assertThat(mixed.failed().count()).isEqualTo(1);
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
			capture.close();
		}
		assertThat(output.toString(StandardCharsets.UTF_8)).contains("VISIBLE_PUBLIC_MIXED_BODY")
				.doesNotContain("SECRET_HIDDEN_MIXED");
	}

	/**
	 * Public setup diagnostics and hidden I/O assertions survive in a mixed class.
	 */
	@Test
	void mixedPerTestSetupKeepsItsOwnVisibilityAndRecording() {
		PrintStream previousOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		Events mixed;
		try {
			System.setOut(capture);
			mixed = EngineTestKit.engine("junit-jupiter").selectors(selectClass(MixedLifecycleRegressionUser.class))
					.execute().testEvents();
		} finally {
			System.setOut(previousOut);
			capture.close();
		}
		assertThat(mixed.succeeded().count()).isEqualTo(1);
		assertThat(mixed.failed().count()).isEqualTo(1);
		assertThat(mixed.failed().stream().findFirst().orElseThrow().getRequiredPayload(TestExecutionResult.class)
				.getThrowable().orElseThrow().getMessage()).contains("VISIBLE_PUBLIC_SETUP_FAILURE");
		assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain("SECRET_SHARED_SETUP");
	}

	/** Composed and inherited hidden methods protect shared class output. */
	@Test
	void composedAndInterfaceHiddenMethodsProtectSharedOutput() {
		assertSharedOutputHidden(selectClass(SharedHiddenVisibilityUsers.Composed.class), "SECRET_COMPOSED_SHARED");
		assertSharedOutputHidden(selectClass(SharedHiddenVisibilityUsers.Interface.class), "SECRET_INTERFACE_SHARED");
	}

	/** Runs one mixed fixture and checks that its shared marker stayed private. */
	private static void assertSharedOutputHidden(org.junit.platform.engine.discovery.ClassSelector fixture,
			String marker) {
		PrintStream previousOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			Events events = EngineTestKit.engine("junit-jupiter").selectors(fixture).execute().testEvents();
			assertThat(events.succeeded().count()).isEqualTo(2);
		} finally {
			System.setOut(previousOut);
			capture.close();
		}
		assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain(marker);
	}

	/** Inherited hidden methods also protect shared class lifecycle output. */
	@Test
	void inheritedHiddenMethodHidesSharedLifecycle() {
		PrintStream previousOut = System.out;
		PrintStream previousErr = System.err;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			System.setErr(capture);
			Events inherited = EngineTestKit.engine("junit-jupiter")
					.selectors(selectClass(InheritedMixedVisibilityOutputUser.class)).execute().testEvents();
			assertThat(inherited.failed().count()).isEqualTo(1);
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
			capture.close();
		}
		assertThat(output.toString(StandardCharsets.UTF_8)).contains("VISIBLE_PUBLIC_MIXED_BODY")
				.doesNotContain("SECRET_HIDDEN_MIXED");
	}

	/**
	 * A hidden test two {@code @Nested} levels down makes the outer class setup and
	 * teardown hidden too.
	 */
	@Test
	void nestedHiddenTestHidesTheOuterLifecycle() {
		PrintStream previousOut = System.out;
		PrintStream previousErr = System.err;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			System.setErr(capture);
			Events nested = EngineTestKit.engine("junit-jupiter")
					.selectors(selectClass(NestedHiddenLifecycleUser.class)).execute().testEvents();
			assertThat(nested.failed().count()).isEqualTo(1);
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
			capture.close();
		}
		assertThat(output.toString(StandardCharsets.UTF_8)).contains("VISIBLE_NESTED_PUBLIC_BODY")
				.doesNotContain("SECRET_NESTED");
	}

	/** A future hidden body stays skipped if its constructor fails first. */
	@Test
	void futureConstructorFailureIsOpaque() {
		FutureHiddenConstructorUser.bodyRan = false;
		PrintStream previousOut = System.out;
		PrintStream previousErr = System.err;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8);
		try {
			System.setOut(capture);
			System.setErr(capture);
			Events future = EngineTestKit.engine("junit-jupiter")
					.selectors(selectClass(FutureHiddenConstructorUser.class)).execute().testEvents();
			assertThat(future.failed().count()).isEqualTo(1);
			assertNoThrowableDetails(future.failed().stream().findFirst().orElseThrow()
					.getPayload(TestExecutionResult.class).orElseThrow().getThrowable().orElseThrow());
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
			capture.close();
		}
		assertThat(FutureHiddenConstructorUser.bodyRan).isFalse();
		assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain("SECRET_FUTURE_CONSTRUCTOR");
	}

	/** A hidden callback failure is redacted before JUnit receives it. */
	@Test
	void hiddenIoCallbackFailureHasNoDiagnostics() {
		Events events = EngineTestKit.engine("junit-jupiter").selectors(selectClass(HiddenCallbackFailureUser.class))
				.execute().testEvents();
		assertThat(events.failed().count()).isEqualTo(1);
		assertNoThrowableDetails(events.failed().stream().findFirst().orElseThrow()
				.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow());
	}

	/** A hidden policy callback failure is redacted before JUnit receives it. */
	@Test
	void hiddenSecurityCallbackFailureHasNoDiagnostics() {
		Events events = EngineTestKit.engine("junit-jupiter")
				.selectors(selectClass(HiddenSecurityCallbackFailureUser.class)).execute().testEvents();
		assertThat(events.failed().count()).isEqualTo(1);
		assertNoThrowableDetails(events.failed().stream().findFirst().orElseThrow()
				.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow());
	}

	/**
	 * An instructor's own extension that fails before a hidden test keeps its text,
	 * because Ares does not wrap other extensions' callbacks.
	 */
	@Test
	void instructorCallbackFailureKeepsItsText() {
		Events events = EngineTestKit.engine("junit-jupiter").selectors(selectClass(InstructorCallbackHiddenUser.class))
				.execute().testEvents();
		assertThat(events.failed().count()).isEqualTo(1);
		assertThat(events.failed().stream().findFirst().orElseThrow().getRequiredPayload(TestExecutionResult.class)
				.getThrowable().orElseThrow().getMessage()).isEqualTo("INSTRUCTOR_CALLBACK_TEXT");
	}

	/** A plain JUnit test without Ares keeps its failure text and its output. */
	@Test
	void unsupervisedTestIsNotRedacted() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream previousOut = System.out;
		Events events;
		try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			events = EngineTestKit.engine("junit-jupiter").selectors(selectClass(UnsupervisedFailureUser.class))
					.execute().testEvents();
		} finally {
			System.setOut(previousOut);
		}
		assertThat(events.failed().stream().findFirst().orElseThrow().getRequiredPayload(TestExecutionResult.class)
				.getThrowable().orElseThrow().getMessage()).isEqualTo("PLAIN_FAILURE_TEXT");
		assertThat(output.toString(StandardCharsets.UTF_8)).contains("PLAIN_OUTPUT");
	}

	/** A public result retains the message needed by students. */
	@Test
	void publicFailureRetainsDiagnostics() {
		TestExecutionResult result = result("publicTestWithVisibleMessage");
		assertEquals(TestExecutionResult.Status.FAILED, result.getStatus());
		assertThat(result.getThrowable().orElseThrow().getMessage()).contains("VISIBLE_PUBLIC_MESSAGE");
		assertThat(result("publicTestWithInternalAresError").getThrowable().orElseThrow().getMessage())
				.contains("Please contact your instructor");
		assertThat(result("publicTestWithStudentSecurityError").getThrowable().orElseThrow().getMessage())
				.contains("Unable to make field accessible");
	}

	/** A future hidden test keeps the existing scheduling message. */
	@Test
	void futureDeadlineReportsSchedule() {
		TestExecutionResult result = result("futureHiddenMethod");
		assertEquals(TestExecutionResult.Status.FAILED, result.getStatus());
		assertThat(result.getThrowable().orElseThrow().getMessage())
				.isEqualTo(Messages.localized("test_guard.hidden_test_before_deadline_message"));
	}

	/** A hidden internal fault never reaches the existing build logger. */
	@Test
	void internalErrorDoesNotLogHiddenThrowable() {
		Logger logger = (Logger) LoggerFactory.getLogger(ReportingUtils.class);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		try {
			TestContext context = mock(TestContext.class);
			when(context.findTestType()).thenReturn(Optional.of(TestType.HIDDEN));
			Throwable safe = ReportingUtils.processThrowable(
					new SecurityException("Reason: Ares-Code; JavaAOPTestCaseSettings; SECRET_HIDDEN_INTERNAL"),
					context);
			assertNoThrowableDetails(safe);
			assertThat(appender.list).isEmpty();
		} finally {
			logger.detachAppender(appender);
			appender.stop();
		}
	}

	/** Checks a hidden failure's result and sanitized throwable. */
	private static void assertHiddenFailure(String methodName) {
		TestExecutionResult result = result(methodName);
		assertEquals(TestExecutionResult.Status.FAILED, result.getStatus());
		assertNoThrowableDetails(result.getThrowable().orElseThrow());
	}

	/** Checks the detail fields of a student-visible hidden throwable. */
	private static void assertNoThrowableDetails(Throwable throwable) {
		assertNotNull(throwable);
		assertNull(throwable.getMessage());
		assertNull(throwable.getCause());
		assertThat(throwable.getStackTrace()).isEmpty();
		assertThat(throwable.toString()).isEmpty();
	}

	/** Finds the finished result for a fixture method by its display name. */
	private static TestExecutionResult result(String methodName) {
		return tests.finished().stream()
				.filter(event -> event.getTestDescriptor().getDisplayName().startsWith(methodName)).findFirst()
				.orElseThrow().getRequiredPayload(TestExecutionResult.class);
	}
}
