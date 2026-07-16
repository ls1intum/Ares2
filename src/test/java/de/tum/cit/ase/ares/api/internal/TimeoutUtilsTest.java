package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;

class TimeoutUtilsTest {

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
	void interruptionIgnoringExecutionDoesNotDelayTimeoutByASecond() throws Exception {
		AtomicBoolean releaseWorker = new AtomicBoolean();
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$
		long startNanos = System.nanoTime();
		try {
			assertThrows(AssertionFailedError.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
				while (!releaseWorker.get()) {
					Thread.onSpinWait();
				}
				return null;
			}, context));
		} finally {
			releaseWorker.set(true);
		}
		long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

		assertThat(elapsedMillis).isLessThan(500L);
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
