package de.tum.cit.ase;

import net.bytebuddy.ByteBuddy;

/**
 * Student code in {@code de.tum.cit.ase} that imports from the trusted
 * namespace {@code net.bytebuddy}.
 */
public final class AncestorPackageByteBuddyImport {

	/**
	 * Not instantiated; the fixture is only analysed.
	 */
	private AncestorPackageByteBuddyImport() {
	}

	/**
	 * Uses a class from Byte Buddy.
	 *
	 * @return a new Byte Buddy instance
	 */
	public static ByteBuddy byteBuddy() {
		return new ByteBuddy();
	}
}
