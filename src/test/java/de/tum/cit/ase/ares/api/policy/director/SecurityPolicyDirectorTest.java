package de.tum.cit.ase.ares.api.policy.director;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;

class SecurityPolicyDirectorTest {

	@Test
	void defaultWithinPathOverloadPreservesProjectRoot() {
		SecurityPolicyDirector director = mock(SecurityPolicyDirector.class, CALLS_REAL_METHODS);
		SecurityPolicy policy = mock(SecurityPolicy.class);
		TestCaseAbstractFactoryAndBuilder factory = mock(TestCaseAbstractFactoryAndBuilder.class);
		Path projectRoot = Path.of("project-root");
		Path withinPath = Path.of("classes", "student");
		when(director.createTestCases(policy, projectRoot)).thenReturn(factory);

		assertSame(factory, director.createTestCases(policy, projectRoot, withinPath));
		verify(director).createTestCases(policy, projectRoot);
	}
}
