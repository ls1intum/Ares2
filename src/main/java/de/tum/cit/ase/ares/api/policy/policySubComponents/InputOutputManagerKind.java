package de.tum.cit.ase.ares.api.policy.policySubComponents;

/**
 * Which I/O manager a test uses, the policy counterpart of
 * {@code @WithIOManager}.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public enum InputOutputManagerKind {

	/** Ares's own I/O tester, which records output and enforces its limit. */
	IO_TESTER,

	/** No I/O manager: output goes to the console unrecorded. */
	NONE,

	/**
	 * An instructor's own manager, named by
	 * {@code theCustomInputAndOutputManagerIs}.
	 */
	CUSTOM
}
