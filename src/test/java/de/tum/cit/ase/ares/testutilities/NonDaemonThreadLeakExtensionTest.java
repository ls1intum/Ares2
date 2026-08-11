package de.tum.cit.ase.ares.testutilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

class NonDaemonThreadLeakExtensionTest {

	@Test
	void identifiesOnlyNewLiveNonDaemonThreads() throws InterruptedException {
		CountDownLatch stop = new CountDownLatch(1);
		Thread nonDaemon = new Thread(() -> await(stop), "intentional-leak-detector-fixture");
		Thread daemon = new Thread(() -> await(stop), "daemon-fixture");
		daemon.setDaemon(true);
		try {
			nonDaemon.start();
			daemon.start();
			assertThat(NonDaemonThreadLeakExtension.newLiveNonDaemonThreads(Set.of())).contains(nonDaemon)
					.doesNotContain(daemon);
			assertThat(NonDaemonThreadLeakExtension.newLiveNonDaemonThreads(Set.of(nonDaemon.getId())))
					.doesNotContain(nonDaemon);
		} finally {
			stop.countDown();
			nonDaemon.join(1_000L);
			daemon.join(1_000L);
		}
	}

	private static void await(CountDownLatch stop) {
		try {
			stop.await();
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
		}
	}
}
