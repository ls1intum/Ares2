package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Checks the behaviour configuration and the constants it contributes to the
 * generated settings class.
 */
class TestBehaviorConfigurationTest {

	/** A configuration built empty has no privileged-exceptions category. */
	@Test
	void builderDefaultsToNullCategory() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNull(configuration.regardingPrivilegedExceptions());
	}

	/** The builder keeps the privileged-exceptions category it was given. */
	@Test
	void builderRoundTripsPrivilegedExceptionsCategory() {
		PrivilegedExceptionsConfiguration privilegedExceptions = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).theFailureMessageIs("Test failed.").build();

		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingPrivilegedExceptions(privilegedExceptions).build();

		assertNotNull(configuration.regardingPrivilegedExceptions());
		assertSame(privilegedExceptions, configuration.regardingPrivilegedExceptions());
	}

	@Test
	void literalFieldAssignmentsIsEmptyWhenNoCategoryIsConfigured() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertTrue(configuration.literalFieldAssignments().isEmpty());
	}

	/**
	 * The generated class's package holds nothing from Ares itself. The Ares JAR
	 * seals its packages, so a class compiled into the exercise could not be loaded
	 * in one of them next to the JAR.
	 *
	 * @throws URISyntaxException if the location of Ares's own classes is not a
	 *                            valid URI.
	 */
	@Test
	void generatedClassLivesOutsideEveryPackageAresShips() throws URISyntaxException {
		Path aresOutput = Path
				.of(TestBehaviorConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		String generatedPackage = TestBehaviorConfiguration.GENERATED_CLASS_NAME.substring(0,
				TestBehaviorConfiguration.GENERATED_CLASS_NAME.lastIndexOf('.'));

		assertFalse(Files.exists(aresOutput.resolve(generatedPackage.replace('.', '/'))),
				"the generated settings class must not share a package with Ares's own classes");
	}
}
