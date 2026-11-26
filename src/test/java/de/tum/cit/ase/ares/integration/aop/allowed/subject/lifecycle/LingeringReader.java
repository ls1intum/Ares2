package de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code that a test leaves running: a thread that waits until it is
 * released and then reads the file the policy allows, as a thread or shutdown
 * hook could once its test has ended.
 */
public final class LingeringReader extends Thread {

	/** The file the policy allows to be read. */
	private static final Path TRUSTED_FILE = Path
			.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");

	/**
	 * How long the thread waits for its release before it gives up, in nanoseconds.
	 */
	private static final long WAIT_LIMIT_NANOS = 60_000_000_000L;

	/** Whether the waiting thread may read now. */
	private static volatile boolean released;

	/**
	 * What the last read gave: {@code read}, {@code refused: ...} or
	 * {@code failed: ...}.
	 */
	private static volatile String outcome = "not run";

	/** The thread started last. */
	private static volatile LingeringReader started;

	/** Creates the thread as a daemon, so it never keeps the test JVM alive. */
	private LingeringReader() {
		setDaemon(true);
	}

	/** Starts a thread that waits for {@link #release()} before it reads. */
	public static void startWaiting() {
		released = false;
		outcome = "not run";
		LingeringReader reader = new LingeringReader();
		started = reader;
		reader.start();
	}

	/** Lets the waiting thread read. */
	public static void release() {
		released = true;
	}

	/**
	 * Waits until the thread started last has finished.
	 *
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static void awaitFinished() throws InterruptedException {
		started.join(WAIT_LIMIT_NANOS / 1_000_000L);
	}

	/**
	 * Says what the last read gave.
	 *
	 * @return {@code read}, {@code refused: } with the message, or {@code failed: }
	 */
	public static String outcome() {
		return outcome;
	}

	/** Waits for the release, then reads the allowed file once. */
	@Override
	public void run() {
		long deadline = System.nanoTime() + WAIT_LIMIT_NANOS;
		while (!released && System.nanoTime() < deadline) {
			Math.sqrt(deadline);
		}
		try {
			Files.readAllBytes(TRUSTED_FILE);
			outcome = "read";
		} catch (SecurityException refusal) {
			outcome = "refused: " + refusal.getMessage();
		} catch (Exception failure) {
			outcome = "failed: " + failure;
		}
	}
}
