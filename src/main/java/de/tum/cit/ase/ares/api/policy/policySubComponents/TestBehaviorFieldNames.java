package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every key of the test-behaviour part of a policy, written once. The names
 * follow the proposal of issue #386 section C and are not final until the
 * advisor accepts them; keeping them here makes a rename a change in one file.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public final class TestBehaviorFieldNames {

	/** The wrapper key, released in 2.2.1 and therefore fixed. */
	public static final String WRAPPER = "theFollowingTestBehaviorIsConfigured";

	/** The strict-timeout category. */
	public static final String STRICT_TIMEOUT = "regardingTheStrictTimeoutForTheTestCases";

	/** The output category. */
	public static final String OUTPUT = "regardingTheOutputOfTheTestCases";

	/** The timeout, a duration. */
	public static final String THE_TIMEOUT_IS = "theTimeoutIs";

	/** The grace period, a duration. */
	public static final String THE_TERMINATION_GRACE_IS = "theTerminationGraceIs";

	/** Whether output is echoed. */
	public static final String THE_OUTPUT_IS_MIRRORED = "theOutputIsMirrored";

	/** The output limit. */
	public static final String THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS_IS = "theMaximumNumberOfOutputCharactersIs";

	/** The I/O manager. */
	public static final String THE_INPUT_AND_OUTPUT_ARE_MANAGED_BY = "theInputAndOutputAreManagedBy";

	/** The custom I/O manager's class name. */
	public static final String THE_CUSTOM_INPUT_AND_OUTPUT_MANAGER_IS = "theCustomInputAndOutputManagerIs";

	/** The fields of each category, in the order the policy lists them. */
	public static final Map<String, List<String>> FIELDS_BY_CATEGORY = Map.of(STRICT_TIMEOUT,
			List.of(THE_TIMEOUT_IS, THE_TERMINATION_GRACE_IS), OUTPUT,
			List.of(THE_OUTPUT_IS_MIRRORED, THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS_IS,
					THE_INPUT_AND_OUTPUT_ARE_MANAGED_BY, THE_CUSTOM_INPUT_AND_OUTPUT_MANAGER_IS));

	/** The fields each category requires whenever it is present. */
	public static final Map<String, Set<String>> REQUIRED_BY_CATEGORY = Map.of(STRICT_TIMEOUT, Set.of(THE_TIMEOUT_IS),
			OUTPUT, Set.of());

	/** Not instantiable. */
	private TestBehaviorFieldNames() {
		throw new UnsupportedOperationException("TestBehaviorFieldNames is a utility class");
	}
}
