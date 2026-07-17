package de.tum.cit.ase.ares.api.jqwik;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.jqwik.api.lifecycle.PropertyExecutionResult;

class JqwikSecurityExtensionTest {

	@Test
	void transformationFailureTakesPriorityAndKeepsOtherFailures() {
		PropertyExecutionResult result = mock(PropertyExecutionResult.class);
		PropertyExecutionResult failedResult = mock(PropertyExecutionResult.class);
		SecurityException transformationFailure = new SecurityException("transformation");
		IllegalStateException propertyFailure = new IllegalStateException("property");
		IllegalArgumentException resetFailure = new IllegalArgumentException("reset");
		when(result.throwable()).thenReturn(Optional.of(propertyFailure));
		when(result.mapToFailed(transformationFailure)).thenReturn(failedResult);

		PropertyExecutionResult merged = JqwikSecurityExtension.mergeLifecycleFailures(result, transformationFailure,
				resetFailure);

		assertSame(failedResult, merged);
		assertSame(resetFailure, transformationFailure.getSuppressed()[0]);
		assertSame(propertyFailure, transformationFailure.getSuppressed()[1]);
		verify(result).mapToFailed(transformationFailure);
	}
}
