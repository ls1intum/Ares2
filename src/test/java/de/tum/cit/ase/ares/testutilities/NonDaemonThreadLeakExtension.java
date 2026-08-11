package de.tum.cit.ase.ares.testutilities;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Fails a test that leaves newly created non-daemon threads alive. */
public final class NonDaemonThreadLeakExtension implements BeforeEachCallback, AfterEachCallback {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(NonDaemonThreadLeakExtension.class);
	private static final Duration GRACE_PERIOD = Duration.ofSeconds(3);

	@Override
	public void beforeEach(ExtensionContext context) {
		context.getStore(NAMESPACE).put(context.getUniqueId(), liveNonDaemonThreadIds());
	}

	@Override
	public void afterEach(ExtensionContext context) throws InterruptedException {
		@SuppressWarnings("unchecked")
		Set<Long> baseline = context.getStore(NAMESPACE).remove(context.getUniqueId(), Set.class);
		List<Thread> leaked = awaitLeaksToFinish(baseline == null ? Set.of() : baseline, GRACE_PERIOD);
		if (leaked.isEmpty()) {
			return;
		}
		StringBuilder diagnostic = new StringBuilder("Test leaked non-daemon threads:");
		for (Thread thread : leaked) {
			diagnostic.append(System.lineSeparator()).append("- ").append(thread.getName()).append(" [id=")
					.append(thread.getId()).append(", state=").append(thread.getState()).append("]");
			for (StackTraceElement frame : thread.getStackTrace()) {
				diagnostic.append(System.lineSeparator()).append("    at ").append(frame);
			}
			thread.interrupt();
		}
		throw new AssertionError(diagnostic.toString());
	}

	static List<Thread> newLiveNonDaemonThreads(Set<Long> baseline) {
		Thread current = Thread.currentThread();
		return Thread.getAllStackTraces().keySet().stream().filter(Thread::isAlive).filter(thread -> !thread.isDaemon())
				.filter(thread -> thread != current).filter(thread -> !baseline.contains(thread.getId()))
				.sorted(Comparator.comparing(Thread::getName).thenComparingLong(Thread::getId)).toList();
	}

	private static Set<Long> liveNonDaemonThreadIds() {
		Set<Long> ids = new LinkedHashSet<>();
		Thread.getAllStackTraces().keySet().stream().filter(Thread::isAlive).filter(thread -> !thread.isDaemon())
				.map(Thread::getId).forEach(ids::add);
		return Set.copyOf(ids);
	}

	private static List<Thread> awaitLeaksToFinish(Set<Long> baseline, Duration gracePeriod)
			throws InterruptedException {
		long deadline = System.nanoTime() + gracePeriod.toNanos();
		List<Thread> leaked;
		do {
			leaked = newLiveNonDaemonThreads(baseline);
			if (leaked.isEmpty()) {
				return leaked;
			}
			Thread.sleep(25L);
		} while (System.nanoTime() < deadline);
		return leaked;
	}
}
