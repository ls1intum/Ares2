package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;

class TimeoutUtilsTest {
	@Test
	void timedOutExecutionUsesOneCancellationInterrupt() {
		Future<?> future = mock(Future.class);
		ExecutorService executorService = mock(ExecutorService.class);
		when(executorService.isTerminated()).thenReturn(true);

		TimeoutUtils.terminateTimedOutExecution(future, executorService, Duration.ofSeconds(1),
				exitCode -> fail("Interruption-aware execution must not request fatal termination")); //$NON-NLS-1$

		InOrder cancellationOrder = inOrder(executorService, future);
		cancellationOrder.verify(executorService).shutdown();
		cancellationOrder.verify(future).cancel(true);
		verify(executorService, never()).shutdownNow();
	}

	@Test
	void interruptionAwareExecutionFinishesBeforeControlReturns() throws Exception {
		AtomicBoolean workerFinished = new AtomicBoolean();
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$

		assertThrows(AssertionFailedError.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.onSpinWait();
				}
			} finally {
				workerFinished.set(true);
			}
			return null;
		}, context));

		assertThat(workerFinished).isTrue();
	}

	@Test
	void interruptionIgnoringExecutionInvalidatesTheCurrentFork() throws Exception {
		AtomicBoolean releaseWorker = new AtomicBoolean();
		AtomicInteger requestedExitCode = new AtomicInteger(-1);
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$
		try {
			FatalTermination termination = assertThrows(FatalTermination.class,
					() -> TimeoutUtils.performTimeoutExecution(() -> {
						while (!releaseWorker.get()) {
							Thread.onSpinWait();
						}
						return null;
					}, context, java.time.Duration.ofMillis(10), exitCode -> {
						requestedExitCode.set(exitCode);
						throw new FatalTermination();
					}));
			assertThat(termination).isNotNull();
		} finally {
			releaseWorker.set(true);
		}

		assertThat(requestedExitCode).hasValue(124);
	}

	@Test
	void productionTerminatorStopsAContaminatedChildJvm() throws Exception {
		String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")); //$NON-NLS-1$ //$NON-NLS-2$
		Process process = new ProcessBuilder(javaExecutable, "-cp", classPath, TimeoutUtilsForkProbe.class.getName()) //$NON-NLS-1$
				.redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
			assertThat(process.exitValue()).isEqualTo(124);
		} finally {
			process.destroyForcibly();
		}
	}

	private static final class FatalTermination extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = TimeoutUtilsTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(TimeoutUtilsTest.class));
		return context;
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutTarget() {
		// Provides the annotation consumed through the mocked test context.
	}
}
