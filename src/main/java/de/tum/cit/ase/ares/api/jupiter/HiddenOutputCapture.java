package de.tum.cit.ase.ares.api.jupiter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Discards hidden-test output until the enclosing Jupiter phase completes. */
final class HiddenOutputCapture implements AutoCloseable {

	/** The standard output stream that was in place before capture. */
	private final PrintStream previousOut;
	/** The standard error stream that was in place before capture. */
	private final PrintStream previousErr;
	/** The private sink installed for both standard streams. */
	private final PrintStream sink;

	/** Installs the sink and remembers the streams to restore. */
	private HiddenOutputCapture() {
		previousOut = System.out;
		previousErr = System.err;
		sink = new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
		System.setOut(sink);
		System.setErr(sink);
	}

	/** Starts a new capture for one hidden Jupiter phase. */
	static HiddenOutputCapture start() {
		return new HiddenOutputCapture();
	}

	/** Restores the streams that surrounded this capture. */
	@Override
	public void close() {
		System.setOut(previousOut);
		System.setErr(previousErr);
		sink.close();
	}
}
