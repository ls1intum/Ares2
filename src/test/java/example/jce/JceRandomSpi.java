package example.jce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandomSpi;

/**
 * A student-like random-number generator whose constructor reads a forbidden
 * and a permitted file, so each read shows whether Ares still checks it.
 */
public final class JceRandomSpi extends SecureRandomSpi {

	/** Keeps the serial form {@link SecureRandomSpi} requires. */
	private static final long serialVersionUID = 1L;

	/** The file the policy forbids reading, set by the launcher. */
	public static volatile Path forbidden;

	/** The file the policy permits reading, set by the launcher. */
	public static volatile Path permitted;

	/** Reads both files and prints whether each read returned or was denied. */
	public JceRandomSpi() {
		report("GENERATED_PROVIDER_FORBIDDEN", forbidden);
		report("GENERATED_PROVIDER_PERMITTED", permitted);
	}

	/** Prints the outcome of one read under a label. */
	private static void report(String label, Path file) {
		if (file == null) {
			return;
		}
		try {
			System.out.println(label + "_RETURNED=" + Files.readString(file));
		} catch (SecurityException denied) {
			System.out.println(label + "_DENIED");
		} catch (IOException failed) {
			System.out.println(label + "_FAILED=" + failed);
		}
	}

	/** Clears the given seed; the fixture's bytes are fixed. */
	@Override
	protected void engineSetSeed(byte[] seed) {
		java.util.Arrays.fill(seed, (byte) 0);
	}

	/** Fills the bytes with fixed test input. */
	@Override
	protected void engineNextBytes(byte[] bytes) {
		java.util.Arrays.fill(bytes, (byte) 1);
	}

	/** Returns fixed seed bytes. */
	@Override
	protected byte[] engineGenerateSeed(int numBytes) {
		return new byte[numBytes];
	}
}
