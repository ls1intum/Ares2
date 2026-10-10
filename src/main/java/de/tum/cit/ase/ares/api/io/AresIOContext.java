package de.tum.cit.ase.ares.api.io;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.context.*;
import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;

@API(status = Status.EXPERIMENTAL)
public final class AresIOContext extends AresContext {

	private final boolean mirrorOutput;
	private final long maxStdOut;

	private AresIOContext(TestContext testContext, boolean mirrorOutput, long maxStdOut) {
		super(testContext);
		this.mirrorOutput = mirrorOutput;
		this.maxStdOut = maxStdOut;
	}

	/**
	 * Returns true if the user requested to mirror recorded output to the console.
	 *
	 * @return the mirror output value.
	 * @see MirrorOutput#value()
	 */
	public boolean mirrorOutput() {
		return mirrorOutput;
	}

	/**
	 * Returns the maximal number of chars that the test should allow to be printed.
	 *
	 * @return the maximal number of chars.
	 * @see MirrorOutput#maxCharCount()
	 */
	public long maxStdOut() {
		return maxStdOut;
	}

	/**
	 * Resolves both I/O settings for one test from the same policy read.
	 *
	 * @param testContext the current test
	 * @return its I/O context
	 */
	public static AresIOContext from(TestContext testContext) {
		ConfigurationUtils.ResolvedOutputMirroring output = ConfigurationUtils.resolveOutputMirroring(testContext);
		return new AresIOContext(testContext, output.mirrored(), output.maximumCharacterCount());
	}
}
