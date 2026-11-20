package de.tum.cit.ase.ares.api.policy.policySubComponents;

/**
 * How much a student sees when Ares blocks an action of their code. A hidden
 * test shows nothing at any level, and an error in Ares itself is never
 * shortened.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public enum SecurityViolationFeedbackLevel {

	/** The {@code HIGH} message, plus the policy entry that would allow it. */
	DEBUG,

	/** The blocked action, its target and its caller; the default. */
	HIGH,

	/** Only the kind of action, such as reading a file, without its target. */
	MEDIUM,

	/** Only that the code violated a policy. */
	LOW
}
