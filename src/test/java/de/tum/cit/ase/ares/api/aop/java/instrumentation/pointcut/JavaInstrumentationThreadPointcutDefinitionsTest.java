package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class JavaInstrumentationThreadPointcutDefinitionsTest {

	@Test
	void submissionPublisherOperationsMatchAspectJPointcut() {
		assertEquals(List.of("submit", "offer"), JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS
				.get("java.util.concurrent.SubmissionPublisher"));
	}
}
