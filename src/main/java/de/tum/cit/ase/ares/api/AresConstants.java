package de.tum.cit.ase.ares.api;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Constants shared across Ares.
 * <p>
 * Description: Holds values that more than one subsystem has to agree on. A
 * value used by a single subsystem belongs in that subsystem, not here.
 * <p>
 * Design Rationale: The policy-format range is read by the policy record, its
 * builder and the schema validator, in packages that must not disagree. Loading
 * this class fails if the minimum exceeds the maximum, which would otherwise
 * silently reject every policy version.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
public final class AresConstants {

	/**
	 * The earliest policy-format version supported by this Ares release. Version 1
	 * is the format without test-behaviour categories; version 2, the first that
	 * may carry them, is {@link #TEST_BEHAVIOR_POLICY_VERSION}.
	 *
	 * @since 2.1.0
	 */
	public static final int MINIMUM_POLICY_VERSION = 1;

	/**
	 * The latest policy-format version supported by this Ares release.
	 * <p>
	 * This is also the version assumed for policies that are built programmatically
	 * without declaring one, as such policies are by construction expressed in the
	 * newest format this release understands. Version 2 is the first format that
	 * may carry test-behaviour categories.
	 *
	 * @since 2.1.0
	 */
	public static final int MAXIMUM_POLICY_VERSION = 2;

	/**
	 * The first policy-format version that may carry test-behaviour categories.
	 *
	 * @since 2.3.0
	 */
	public static final int TEST_BEHAVIOR_POLICY_VERSION = 2;

	static {
		if (MINIMUM_POLICY_VERSION > MAXIMUM_POLICY_VERSION) {
			throw new ExceptionInInitializerError("MINIMUM_POLICY_VERSION must not exceed MAXIMUM_POLICY_VERSION");
		}
	}

	/** Prevents instances of this utility. */
	private AresConstants() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "AresConstants"));
	}
}
