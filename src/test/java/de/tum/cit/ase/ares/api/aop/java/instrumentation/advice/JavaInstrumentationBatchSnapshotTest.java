package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * The tasks of {@code invokeAll} are read once: the classes, the allowances and
 * the tasks recorded for the call come from one copy, so a task another thread
 * puts into the collection after that copy is never recorded as validated.
 */
class JavaInstrumentationBatchSnapshotTest {

	/** An allowed task. */
	private final Callable<Integer> allowed = () -> 1;

	/** A task put in its place after the copy. */
	private final Callable<Integer> replacement = () -> 2;

	/** A collection class that is not a plain JDK collection. */
	private static final class OwnList extends AbstractList<Object> {

		/**
		 * Returns nothing.
		 *
		 * @param index ignored
		 * @return never
		 */
		@Override
		public Object get(int index) {
			throw new IndexOutOfBoundsException(index);
		}

		/**
		 * Holds nothing.
		 *
		 * @return zero
		 */
		@Override
		public int size() {
			return 0;
		}
	}

	/** A replacement after the copy is neither in the copy nor recorded. */
	@Test
	void replacementAfterTheCopyIsNotRecorded() {
		List<Callable<Integer>> tasks = new CopyOnWriteArrayList<>(List.of(allowed));
		Object[] copy = JavaInstrumentationAdviceThreadSystemToolbox.snapshotTaskCollections(new Object[] { tasks });
		tasks.set(0, replacement);

		IdentityHashMap<Object, int[]> validated = JavaInstrumentationAdviceThreadSystemToolbox.validatedTasks(copy,
				true);
		assertNotSame(tasks, copy[0]);
		assertTrue(validated.containsKey(allowed));
		assertFalse(validated.containsKey(replacement));
		assertEquals(1, validated.get(allowed)[0]);
	}

	/** Arguments that are no plain collection stay as they are. */
	@Test
	void otherArgumentsStayAsTheyAre() {
		OwnList own = new OwnList();
		Object[] copy = JavaInstrumentationAdviceThreadSystemToolbox
				.snapshotTaskCollections(new Object[] { own, 5L, TimeUnit.SECONDS });
		assertSame(own, copy[0]);
		assertSame(TimeUnit.SECONDS, copy[2]);
		assertNull(JavaInstrumentationAdviceThreadSystemToolbox.snapshotTaskCollections(null));
	}
}
