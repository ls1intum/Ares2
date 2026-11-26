package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A channel the supervised code wrote itself, which records whether anything
 * asked for its hash code. Ares must not call into it while it checks a
 * transfer to it.
 */
public final class HashRecordingChannel implements WritableByteChannel {

	/**
	 * Whether any instance was asked for its hash code.
	 */
	private static volatile boolean hashed;

	/**
	 * Forgets earlier requests for a hash code.
	 */
	public static void forget() {
		hashed = false;
	}

	/**
	 * Tells whether any instance was asked for its hash code.
	 *
	 * @return whether it was
	 */
	public static boolean wasHashed() {
		return hashed;
	}

	/**
	 * Records the request and answers with a fixed value.
	 *
	 * @return a fixed hash code
	 */
	@Override
	public int hashCode() {
		hashed = true;
		return 1;
	}

	/**
	 * Compares by identity, as {@code Object} does.
	 *
	 * @param other the other object
	 * @return whether it is this channel
	 */
	@Override
	public boolean equals(Object other) {
		return this == other;
	}

	/**
	 * Takes all the bytes and keeps none.
	 *
	 * @param source the bytes
	 * @return how many were taken
	 */
	@Override
	public int write(ByteBuffer source) {
		int count = source.remaining();
		source.position(source.limit());
		return count;
	}

	/**
	 * Is always open.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/**
	 * Holds nothing to release.
	 */
	@Override
	public void close() {
	}
}
