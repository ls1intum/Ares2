package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;

/**
 * Test user whose every test fails, each with one kind of Ares failure, so
 * {@code FailureMessageShapeTest} can check the shape of the message in the
 * test report. ArchUnit and AspectJ only, like the other users; the denials in
 * all four modes are checked by {@code FailureMessageShapeDenialTest}.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@TestMethodOrder(MethodName.class)
@Policy(value = FailureMessageShapeUser.DEFAULT_RESTRICTIVE_POLICY, withinPath = FailureMessageShapeUser.HELLO_WORLD_PATH)
@SuppressWarnings("static-method")
public class FailureMessageShapeUser {

	/**
	 * The pinned stand-in for running without a policy: everything forbidden, in
	 * the modes the no-policy path picks, ArchUnit and AspectJ.
	 */
	public static final String DEFAULT_RESTRICTIVE_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicySelfTestDefaultRestrictive.yaml";

	/** The same policy with {@code regardingFileSystemInteractions} misspelled. */
	public static final String MISSPELLED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFailureMessageShapeMisspelledField.yaml";

	/** A harmless student folder, for the tests that do not test a denial. */
	public static final String HELLO_WORLD_PATH = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld";

	/** The student folder whose only class reads a file. */
	public static final String FILE_READ_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream";

	/** Runs longer than its strict timeout of 100 milliseconds. */
	@PublicTest
	@StrictTimeout(value = 100, unit = TimeUnit.MILLISECONDS)
	void strictTimeout() throws InterruptedException {
		Thread.sleep(2_000);
	}

	/** A hidden test run long before its deadline. */
	@HiddenTest
	@Deadline("2200-01-01 16:00")
	void hiddenTestBeforeDeadline() {
	}

	/** Runs under a policy with a misspelled field. */
	@PublicTest
	@Policy(value = MISSPELLED_POLICY, withinPath = HELLO_WORLD_PATH)
	void misspelledPolicyField() {
	}

	/**
	 * Lets student code read a file under the restrictive default policy.
	 *
	 * @throws IOException if the read fails other than by Ares
	 */
	@PublicTest
	@Policy(value = DEFAULT_RESTRICTIVE_POLICY, withinPath = FILE_READ_PATH)
	void defaultPolicyDenial() throws IOException {
		FileinputStreamMain.accessFileSystemViaFileInputStream();
	}

	/**
	 * Asks for an {@link IOTester} under an I/O manager that provides none.
	 *
	 * @param tester the tester, which Ares cannot provide
	 */
	@PublicTest
	@WithIOManager(WithIOManager.None.class)
	void ioTesterWithoutManager(IOTester tester) {
	}
}
