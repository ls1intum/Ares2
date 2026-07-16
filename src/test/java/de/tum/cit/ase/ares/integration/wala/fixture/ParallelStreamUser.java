package de.tum.cit.ase.ares.integration.wala.fixture;

import java.util.List;
import java.util.stream.Stream;

/** Test fixture for declared JDK interface targets. */
public final class ParallelStreamUser {
	private ParallelStreamUser() {
	}

	public static void useCollectionParallelStream() {
		List.of("value").parallelStream().forEach(value -> {
		});
	}

	public static void useStreamParallel() {
		Stream.of("value").parallel().forEach(value -> {
		});
	}
}
