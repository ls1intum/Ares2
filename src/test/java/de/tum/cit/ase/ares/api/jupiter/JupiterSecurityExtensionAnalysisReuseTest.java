package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Checks, through real JUnit runs, when the phases of one test method reuse the
 * analysis of an earlier phase: only while it passed and its input is
 * unchanged, never across test methods, and every phase is armed either way.
 */
class JupiterSecurityExtensionAnalysisReuseTest {

	/** What happened to the test cases, in order, across all phases of a run. */
	private static final List<String> EVENTS = new CopyOnWriteArrayList<>();

	/** How each created director behaves, in the order they are created. */
	private static final Deque<Behaviour> BEHAVIOURS = new ArrayDeque<>();

	/**
	 * How a fake director answers.
	 *
	 * @param analysisPasses whether its analysis passes
	 * @param stillApplies   what it answers when asked whether its analysis applies
	 */
	private record Behaviour(boolean analysisPasses, boolean stillApplies) {
	}

	/** A director that records what the extension asks of it. */
	private static final class RecordingDirector extends SecurityPolicyReaderAndDirector {

		/** The number of this director, counted from 1. */
		private final int number;

		/** How this director answers. */
		private final Behaviour behaviour;

		/**
		 * Creates the director with the next behaviour, or a passing one that applies.
		 *
		 * @param number the number of this director
		 */
		RecordingDirector(int number) {
			super(null);
			this.number = number;
			this.behaviour = BEHAVIOURS.isEmpty() ? new Behaviour(true, true) : BEHAVIOURS.removeFirst();
		}

		/** Records the analysis and arming, and fails if the analysis should fail. */
		@Override
		public SecurityPolicyReaderAndDirector executeTestCases() {
			EVENTS.add("analyse-and-arm " + number);
			if (!behaviour.analysisPasses()) {
				throw new SecurityException("analysis of director " + number + " failed");
			}
			return this;
		}

		/** Records the question and answers as configured. */
		@Override
		public boolean analysisStillApplies() {
			EVENTS.add("still-applies " + number);
			return behaviour.stillApplies();
		}

		/** Records an arming without analysis. */
		@Override
		public SecurityPolicyReaderAndDirector rearmTestCases() {
			EVENTS.add("arm " + number);
			return this;
		}
	}

	/**
	 * The extension, handing out recording directors instead of reading a policy.
	 */
	public static class RecordingExtension extends JupiterSecurityExtension {

		/** The number of directors created so far. */
		private int created;

		/** Creates a recording director and records its creation. */
		@Override
		SecurityPolicyReaderAndDirector createTestCases(@Nullable Path policyPath, Path withinPath) {
			created++;
			EVENTS.add("create " + created);
			return new RecordingDirector(created);
		}
	}

	/**
	 * A test method with a setup and a tear-down method, so it has three phases.
	 */
	@ExtendWith(RecordingExtension.class)
	static class ThreePhaseFixture {

		/** The setup phase. */
		@BeforeEach
		void setUp() {
		}

		/** The test itself, prepared together with the setup phase. */
		@Test
		void test() {
		}

		/** The tear-down phase. */
		@AfterEach
		void tearDown() {
		}
	}

	/** Two test methods, each with its constructor and a test phase. */
	@ExtendWith(RecordingExtension.class)
	static class TwoTestsFixture {

		/** The first test. */
		@Test
		void first() {
		}

		/** The second test. */
		@Test
		void second() {
		}
	}

	/** Forgets what an earlier test recorded. */
	@BeforeEach
	void forget() {
		EVENTS.clear();
		BEHAVIOURS.clear();
	}

	/**
	 * A passed analysis whose input did not change is armed again in the setup and
	 * the tear-down phase without analysing again.
	 */
	@Test
	void laterPhasesArmAPassedAnalysisAgain() {
		run(ThreePhaseFixture.class, 1, 0);
		assertEquals(List.of("create 1", "analyse-and-arm 1", "still-applies 1", "arm 1", "still-applies 1", "arm 1"),
				EVENTS);
	}

	/** A changed input makes every later phase create and analyse afresh. */
	@Test
	void aChangedInputIsAnalysedAgain() {
		BEHAVIOURS.addAll(List.of(new Behaviour(true, false), new Behaviour(true, false), new Behaviour(true, false)));
		run(ThreePhaseFixture.class, 1, 0);
		assertEquals(List.of("create 1", "analyse-and-arm 1", "still-applies 1", "create 2", "analyse-and-arm 2",
				"still-applies 2", "create 3", "analyse-and-arm 3"), EVENTS);
	}

	/**
	 * A failed analysis is never reused: the phase after it neither arms the failed
	 * test cases nor falls back to the ones it replaced, but analyses afresh.
	 */
	@Test
	void aFailedAnalysisIsNeverReused() {
		BEHAVIOURS.addAll(List.of(new Behaviour(true, false), new Behaviour(false, true), new Behaviour(true, true)));
		run(ThreePhaseFixture.class, 0, 1);
		assertEquals(List.of("create 1", "analyse-and-arm 1", "still-applies 1", "create 2", "analyse-and-arm 2",
				"create 3", "analyse-and-arm 3"), EVENTS);
	}

	/** Each test method starts with an analysis of its own. */
	@Test
	void anotherTestMethodAnalysesAfresh() {
		run(TwoTestsFixture.class, 2, 0);
		assertEquals(List.of("create 1", "analyse-and-arm 1", "still-applies 1", "arm 1", "create 2",
				"analyse-and-arm 2", "still-applies 2", "arm 2"), EVENTS);
	}

	/**
	 * Runs a fixture class and checks how many of its tests pass and fail.
	 *
	 * @param fixture   the fixture class
	 * @param succeeded the expected number of passing tests
	 * @param failed    the expected number of failing tests
	 */
	private static void run(Class<?> fixture, int succeeded, int failed) {
		EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture)).execute().testEvents()
				.assertStatistics(statistics -> statistics.succeeded(succeeded).failed(failed));
	}
}
