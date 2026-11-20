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

	/** The visibility-and-schedule category. */
	public static final String VISIBILITY_AND_SCHEDULE = "regardingTheVisibilityAndScheduleOfTheTestCases";

	/** The deadline of hidden tests. */
	public static final String THE_DEADLINE_IS = "theDeadlineIs";

	/** The deadline's extension, a duration. */
	public static final String THE_DEADLINE_IS_EXTENDED_BY = "theDeadlineIsExtendedBy";

	/** The date before which hidden tests run early. */
	public static final String THE_HIDDEN_TESTS_ARE_RUN_BEFORE = "theHiddenTestsAreRunBefore";

	/** Whether a test no entry names is hidden. */
	public static final String TESTS_NOT_LISTED_HERE_ARE_HIDDEN = "testsNotListedHereAreHidden";

	/** The hidden tests. */
	public static final String THE_FOLLOWING_TESTS_ARE_HIDDEN = "theFollowingTestsAreHidden";

	/** The public tests. */
	public static final String THE_FOLLOWING_TESTS_ARE_PUBLIC = "theFollowingTestsArePublic";

	/** The fields of each category, in the order the policy lists them. */
	public static final Map<String, List<String>> FIELDS_BY_CATEGORY = Map.of(VISIBILITY_AND_SCHEDULE,
			List.of(THE_DEADLINE_IS, THE_DEADLINE_IS_EXTENDED_BY, THE_HIDDEN_TESTS_ARE_RUN_BEFORE,
					TESTS_NOT_LISTED_HERE_ARE_HIDDEN, THE_FOLLOWING_TESTS_ARE_HIDDEN, THE_FOLLOWING_TESTS_ARE_PUBLIC));

	/** The fields each category requires whenever it is present. */
	public static final Map<String, Set<String>> REQUIRED_BY_CATEGORY = Map.of(VISIBILITY_AND_SCHEDULE,
			Set.of(THE_DEADLINE_IS, TESTS_NOT_LISTED_HERE_ARE_HIDDEN));

	/** Not instantiable. */
	private TestBehaviorFieldNames() {
		throw new UnsupportedOperationException("TestBehaviorFieldNames is a utility class");
	}
}
