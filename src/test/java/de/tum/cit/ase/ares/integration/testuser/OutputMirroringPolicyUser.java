package de.tum.cit.ase.ares.integration.testuser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.InputOutputPenguin;

/**
 * Tests whose output limit comes from the policy's
 * {@code regardingOutputMirroring} rather than from {@code @MirrorOutput}: no
 * mirroring and a limit of 10 per stream.
 */
@UseLocale("en")
@TestMethodOrder(MethodName.class)
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOutputMirroringUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class OutputMirroringPolicyUser {

	/** Two long lines, beyond the policy's limit of 10. */
	@PublicTest
	void policyLimitStopsTooMuchOutput() {
		InputOutputPenguin.writeTwoLines();
	}

	/**
	 * A short line, inside the limit, still recorded for an assertion although it
	 * is not mirrored.
	 *
	 * @param tester the IO tester of this test.
	 */
	@PublicTest
	void policyLetsShortOutputPass(IOTester tester) {
		System.out.println("hi");

		assertThat(tester.out().getLinesAsString()).containsExactly("hi");
	}

	/** The same long lines, allowed by an annotation that replaces the policy. */
	@PublicTest
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	void annotationReplacesThePolicy() {
		InputOutputPenguin.writeTwoLines();
	}
}
