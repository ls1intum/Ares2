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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import de.tum.cit.ase.ares.integration.testuser.HiddenFeedbackUser;
import de.tum.cit.ase.ares.integration.testuser.HiddenLifecycleOutputUser;
import de.tum.cit.ase.ares.integration.testuser.InheritedMixedVisibilityOutputUser;
import de.tum.cit.ase.ares.integration.testuser.MixedVisibilityOutputUser;
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

	/** Source importing the removed annotation fails to compile. */
	@Test
	void removedAnnotationDoesNotCompile(@TempDir Path tempDir) throws Exception {
		Path source = tempDir.resolve("OldAnnotationImport.java");
		Files.writeString(source,
				"import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly; class OldAnnotationImport {} ");
		ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
		int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-classpath",
				System.getProperty("java.class.path"), source.toString());
		assertThat(exitCode).isNotZero();
		assertThat(diagnostics.toString(StandardCharsets.UTF_8)).contains("PrivilegedExceptionsOnly");
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
