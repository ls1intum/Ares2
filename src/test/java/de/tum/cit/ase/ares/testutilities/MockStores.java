package de.tum.cit.ase.ares.testutilities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A mocked JUnit extension store that keeps what is put into it, for tests that
 * mock an extension context.
 */
public final class MockStores {

	/** Not to be created. */
	private MockStores() {
	}

	/**
	 * A store answering {@code getOrComputeIfAbsent} and {@code get} from one map.
	 *
	 * @return the store.
	 */
	public static ExtensionContext.Store workingStore() {
		Map<Object, Object> values = new HashMap<>();
		ExtensionContext.Store store = mock(ExtensionContext.Store.class);
		when(store.getOrComputeIfAbsent(any(), any(), any())).thenAnswer(call -> {
			Function<Object, Object> creator = call.getArgument(1);
			return values.computeIfAbsent(call.getArgument(0), creator);
		});
		when(store.get(any(), any())).thenAnswer(call -> values.get(call.getArgument(0)));
		return store;
	}
}
