package de.tum.cit.ase.ares.api.structural;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class StructuralOracleIsolationTest {

	@Test
	void concurrentProvidersRetainTheirOwnOracle() throws Exception {
		OracleProvider first = new OracleProvider("[{\"class\":{\"name\":\"First\"}}]");
		OracleProvider second = new OracleProvider("[{\"class\":{\"name\":\"Second\"}}]");
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch completed = new CountDownLatch(2);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread firstReader = new Thread(() -> readRepeatedly(first, "First", start, completed, failure));
		Thread secondReader = new Thread(() -> readRepeatedly(second, "Second", start, completed, failure));
		firstReader.start();
		secondReader.start();
		start.countDown();
		if (!completed.await(5, TimeUnit.SECONDS)) {
			throw new AssertionError("Concurrent structural-oracle readers did not finish");
		}
		if (failure.get() != null) {
			throw new AssertionError("A provider observed another provider's structural oracle", failure.get());
		}
	}

	private static void readRepeatedly(OracleProvider provider, String expectedName, CountDownLatch start,
			CountDownLatch completed, AtomicReference<Throwable> failure) {
		try {
			start.await();
			for (int index = 0; index < 10_000; index++) {
				assertEquals(expectedName, provider.className());
			}
		} catch (Throwable throwable) {
			failure.compareAndSet(null, throwable);
			if (throwable instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		} finally {
			completed.countDown();
		}
	}

	private static final class OracleProvider extends StructuralTestProvider {

		private OracleProvider(String json) {
			structureOracleJSON = parseJsonArray(json);
		}

		private String className() {
			JsonNode classNode = structureOracleJSON.get(0).get(JSON_PROPERTY_CLASS);
			return classNode.get(JSON_PROPERTY_NAME).asText();
		}
	}
}
