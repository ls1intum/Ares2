package de.tum.cit.ase.ares.api.policy.director;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;

class SecurityPolicyDirectorTest {

	/**
	 * The three-argument {@code createTestCases} overload must stay abstract. An
	 * earlier default implementation forwarded to the two-argument version and
	 * silently dropped {@code withinPath}, so a subclass that forgot to override it
	 * lost the analysis scope with no compile-time warning. Making the overload
	 * abstract forces every subclass to decide; this test guards against
	 * reintroducing the scope-losing default.
	 */
	@Test
	void withinPathOverloadIsAbstractSoSubclassesCannotDropIt() throws NoSuchMethodException {
		Method overload = SecurityPolicyDirector.class.getDeclaredMethod("createTestCases", SecurityPolicy.class,
				Path.class, Path.class);
		assertTrue(Modifier.isAbstract(overload.getModifiers()));
	}

	/**
	 * With no policy supplied, director selection must still yield the Java
	 * director so the no-policy path builds a (restrictive) run rather than
	 * failing.
	 */
	@Test
	void selectSecurityPolicyDirectorReturnsJavaDirectorWhenPolicyIsNull() {
		assertInstanceOf(SecurityPolicyJavaDirector.class, SecurityPolicyDirector.selectSecurityPolicyDirector(null));
	}
}
