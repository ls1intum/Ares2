package de.tum.cit.ase.ares.integration.testuser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * Hello-World-style user test class that verifies @Policy behaviour.
 * Covers AspectJ modes (ArchUnit and WALA) and deactivated policy.
 * Instrumentation modes are intentionally excluded: when run via @UserBased,
 * the Java agent is not available in the nested test JVM, causing
 * JavaAOPTestCaseSettings to be unloadable from the bootstrap class loader.
 */
@Public
@UseLocale("en")
@MirrorOutput(MirrorOutput.MirrorOutputPolicy.DISABLED)
@StrictTimeout(5)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class HelloWorldUser {

	// -----------------------------------------------------------------------
	// 1. @Policy with JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
	// -----------------------------------------------------------------------

	@Test
	@Policy(
		value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyHelloWorld.yaml",
		withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld"
	)
	void helloWorld_mavenArchunitAspectJ() {
		assertEquals("Hello, World!", "Hello, World!");
	}

	// -----------------------------------------------------------------------
	// 2. @Policy with JAVA_USING_MAVEN_WALA_AND_ASPECTJ
	// -----------------------------------------------------------------------

	@Test
	@Policy(
		value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyHelloWorld.yaml",
		withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld"
	)
	void helloWorld_mavenWalaAspectJ() {
		assertEquals("Hello, World!", "Hello, World!");
	}

	// -----------------------------------------------------------------------
	// 3. @Policy with activated=false — Ares explicitly deactivated
	// -----------------------------------------------------------------------

	@Test
	@Policy(activated = false)
	void helloWorld_policyDeactivated() {
		assertEquals("Hello, World!", "Hello, World!");
	}

	@Test
	void helloWorld_noPolicy() {
		assertEquals("Hello, World!", "Hello, World!");
	}
}
