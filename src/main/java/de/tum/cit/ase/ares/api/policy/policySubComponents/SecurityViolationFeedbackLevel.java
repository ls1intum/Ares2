package de.tum.cit.ase.ares.api.policy.policySubComponents;

/**
 * How much a student sees when Ares blocks an action of their code. A hidden
 * test shows nothing at any level.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public enum SecurityViolationFeedbackLevel {

	/** The blocked action, plus the policy entry that would allow it. */
	DEBUG,

	/** That something was blocked, without saying what. */
	MINIMIZED,

	/** The blocked action, its target and its caller, as before this setting. */
	FULL
}
