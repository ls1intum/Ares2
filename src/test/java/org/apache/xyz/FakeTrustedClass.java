package org.apache.xyz;

import java.util.concurrent.*;

import de.tum.cit.ase.ares.api.io.IOTester;

public class FakeTrustedClass {

	public static void useCommonPoolBad() throws InterruptedException, ExecutionException {
		CompletableFuture.runAsync(() -> IOTester.class.getDeclaredFields()[0].setAccessible(true)).get();
	}
}
