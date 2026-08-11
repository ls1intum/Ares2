package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;

class JupiterWithinPathContractTest {
	static class Fixtures {
		@Policy(withinPath = "classes/example")
		void production() {
		}

		@Policy(withinPath = "test-classes/example")
		void tests() {
		}

		@Policy(withinPath = "classes/../trusted-instructor-value")
		void traversalOutsideStudentThreatModel() {
		}

		@Policy(withinPath = "/classes/absolute")
		void absolute() {
		}

		@Policy(withinPath = "other/example")
		void unsupported() {
		}
	}

	@Test
	void acceptsSupportedPrefixesDocumentsTraversalAndRejectsUnsupportedOrAbsoluteValues() throws Exception {
		assertEquals(Path.of("classes/example"), resolve("production"));
		assertEquals(Path.of("test-classes/example"), resolve("tests"));
		assertEquals(Path.of("classes/../trusted-instructor-value"), resolve("traversalOutsideStudentThreatModel"));
		assertThrows(SecurityException.class, () -> resolve("absolute"));
		assertThrows(SecurityException.class, () -> resolve("unsupported"));
	}

	private Path resolve(String methodName) throws Exception {
		Method method = Fixtures.class.getDeclaredMethod(methodName);
		return JupiterSecurityExtension.testAndGetPolicyWithinPath(method.getAnnotation(Policy.class));
	}
}
