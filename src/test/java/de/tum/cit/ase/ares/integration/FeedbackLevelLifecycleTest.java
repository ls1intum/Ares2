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
 * follows the MINIMIZED feedback level like one in the test itself: the report
 * says only that something was blocked, with no path and no stack.
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
		Events classes = EngineTestKit.engine(JupiterEngineDescriptor.ENGINE_ID)
				.selectors(DiscoverySelectors.selectClass(FeedbackLevelLifecycleUsers.BeforeAllRead.class)).execute()
				.containerEvents();
		assertMinimized(failuresIn(classes), 1);
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
