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

	/** The failure-feedback category. */
	public static final String FAILURE_FEEDBACK = "regardingTheFailureFeedbackForTheTestCases";

	/** Whether other failures show a fixed message. */
	public static final String ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED = "onlyPrivilegedExceptionsAreReported";

	/** The fixed message for other failures. */
	public static final String THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS_IS = "theFailureMessageForOtherExceptionsIs";

	/** How much a blocked action reveals. */
	public static final String THE_SECURITY_VIOLATION_FEEDBACK_LEVEL_IS = "theSecurityViolationFeedbackLevelIs";

	/** The fields of each category, in the order the policy lists them. */
	public static final Map<String, List<String>> FIELDS_BY_CATEGORY = Map.of(FAILURE_FEEDBACK,
			List.of(ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED, THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS_IS,
					THE_SECURITY_VIOLATION_FEEDBACK_LEVEL_IS));

	/** The fields each category requires whenever it is present. */
	public static final Map<String, Set<String>> REQUIRED_BY_CATEGORY = Map.of(FAILURE_FEEDBACK, Set.of());

	/** Not instantiable. */
	private TestBehaviorFieldNames() {
		throw new UnsupportedOperationException("TestBehaviorFieldNames is a utility class");
	}
}
