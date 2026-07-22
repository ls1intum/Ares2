package de.tum.cit.ase.ares.api;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Constants shared across Ares.
 * <p>
 * Description: Holds values that more than one subsystem has to agree on, so
 * that agreement is expressed in one place rather than repeated. A value used
 * by a single subsystem belongs in that subsystem, not here.
 * <p>
 * Design Rationale: The supported policy-format range is read by the policy
 * record, by its builder and by the schema validator that runs before the
 * record exists. Those three sit in different packages and must not be able to
 * disagree, which is what a shared constant guarantees and what a constant
 * owned by any one of them does not.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
public final class AresConstants {

	/**
	 * The earliest policy-format version supported by this Ares release.
	 *
	 * @since 2.1.0
	 */
	public static final int MINIMUM_POLICY_VERSION = 1;

	/**
	 * The latest policy-format version supported by this Ares release.
	 * <p>
	 * This is also the version assumed for policies that are built programmatically
	 * without declaring one, as such policies are by construction expressed in the
	 * newest format this release understands.
	 *
	 * @since 2.1.0
	 */
	public static final int MAXIMUM_POLICY_VERSION = 1;

	private AresConstants() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "AresConstants"));
	}
}
