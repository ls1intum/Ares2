package de.tum.cit.ase.ares.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventType;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FeedbackLevelLifecycleUsers;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Proves that a blocked action in a test's constructor or lifecycle method
 * follows the MINIMIZED feedback level like one in the test itself, and that a
 * failing setup gets the policy's message when only privileged failures are
 * reported.
 */
@UserBased({ FeedbackLevelLifecycleUsers.ConstructorRead.class, FeedbackLevelLifecycleUsers.BeforeEachRead.class,
		FeedbackLevelLifecycleUsers.AfterEachRead.class, FeedbackLevelLifecycleUsers.AfterAllRead.class })
class FeedbackLevelLifecycleTest {

	/** Results of the fixture classes. */
	@UserTestResults
	private static Events tests;

	/** The MINIMIZED text, in English. */
	private static final String MINIMIZED = "Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.";

	/**
	 * A failing constructor, setup or teardown shows the MINIMIZED text and no
	 * stack.
	 */
	@TestTest
	void test_everyPlaceShowsTheMinimizedText() {
		assertMinimized(failuresIn(tests), 4);
	}

	/**
	 * A failing class setup fails the class rather than a test, so its own run is
	 * read from the class's events.
	 */
	@TestTest
	void test_classSetupShowsTheMinimizedText() {
		assertMinimized(failuresIn(containerEventsOf(FeedbackLevelLifecycleUsers.BeforeAllRead.class)), 1);
	}

	/**
	 * A failing class teardown fails the class after its test, so its own run is
	 * read from the class's events too.
	 */
	@TestTest
	void test_classTeardownShowsTheMinimizedText() {
		assertMinimized(failuresIn(containerEventsOf(FeedbackLevelLifecycleUsers.AfterAllRead.class)), 1);
	}

	/**
	 * With only privileged failures reported, a failing setup shows the policy's
	 * message, not its own text.
	 */
	@TestTest
	void test_aFailingSetupShowsThePolicyMessage() {
		List<Throwable> failures = failuresIn(EngineTestKit.engine(JupiterEngineDescriptor.ENGINE_ID)
				.selectors(DiscoverySelectors.selectClass(FeedbackLevelLifecycleUsers.PrivilegedOnlySetupFails.class))
				.execute().testEvents());
		assertThat(failures).singleElement().satisfies(
				failure -> assertThat(failure).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message"));
	}

	/**
	 * With only privileged failures reported, a setup stopped by an assumption
	 * still skips its test rather than failing it.
	 */
	@TestTest
	void test_anAbortedSetupStillSkipsTheTest() {
		Events events = EngineTestKit.engine(JupiterEngineDescriptor.ENGINE_ID)
				.selectors(DiscoverySelectors.selectClass(FeedbackLevelLifecycleUsers.PrivilegedOnlySetupAborts.class))
				.execute().testEvents();
		assertThat(events.aborted().count()).isEqualTo(1);
		assertThat(events.failed().count()).isZero();
	}

	/**
	 * Runs one fixture class and returns its container events.
	 *
	 * @param fixture the fixture class.
	 * @return its container events.
	 */
	private static Events containerEventsOf(Class<?> fixture) {
		return EngineTestKit.engine(JupiterEngineDescriptor.ENGINE_ID)
				.selectors(DiscoverySelectors.selectClass(fixture)).execute().containerEvents();
	}

	/**
	 * The failures reported in some events.
	 *
	 * @param events the events.
	 * @return the failures.
	 */
	private static List<Throwable> failuresIn(Events events) {
		return events.stream().filter(event -> event.getType() == EventType.FINISHED).map(Event::getPayload)
				.flatMap(payload -> payload.stream()).map(TestExecutionResult.class::cast)
				.filter(result -> result.getStatus() == TestExecutionResult.Status.FAILED)
				.map(result -> result.getThrowable().orElseThrow()).collect(Collectors.toList());
	}

	/**
	 * Checks that every failure is the MINIMIZED text without a stack.
	 *
	 * @param failures the failures.
	 * @param expected how many there must be.
	 */
	private static void assertMinimized(List<Throwable> failures, int expected) {
		assertThat(failures).hasSize(expected).allSatisfy(failure -> {
			assertThat(failure).isExactlyInstanceOf(SecurityException.class).hasMessage(MINIMIZED);
			assertThat(failure.getStackTrace()).isEmpty();
		});
	}
}
