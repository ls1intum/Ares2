package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.files.FilesReadMain;

/**
 * Reads a file the policy forbids, once per feedback level and mode
 * combination, each test with its own method-level policy, so the report shows
 * what each level lets a student see.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(10)
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class FeedbackLevelPolicyUser {

	/** Where every policy of this class lives, below its mode folder. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/** The student code reading a file. */
	private static final String WITHIN = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/oldMethods/files";

	/** A blocked read at FULL keeps today's message. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelFULL.yaml", withinPath = WITHIN)
	void fullArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MINIMIZED, ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelMINIMIZED.yaml", withinPath = WITHIN)
	void minimizedArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MINIMIZED, ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelMINIMIZED.yaml", withinPath = WITHIN)
	void minimizedArchunitInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MINIMIZED, WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyFeedbackLevelMINIMIZED.yaml", withinPath = WITHIN)
	void minimizedWalaAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MINIMIZED, WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyFeedbackLevelMINIMIZED.yaml", withinPath = WITHIN)
	void minimizedWalaInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at DEBUG, ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = WITHIN)
	void debugArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at DEBUG, ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelDEBUG.yaml", withinPath = WITHIN)
	void debugArchunitInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at DEBUG, WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = WITHIN)
	void debugWalaAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at DEBUG, WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyFeedbackLevelDEBUG.yaml", withinPath = WITHIN)
	void debugWalaInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** Privileged-only reporting on: a blocked read still follows the level. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelPrivilegedMinimized.yaml", withinPath = WITHIN)
	void privilegedOnlyKeepsTheLevel() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/**
	 * Privileged-only reporting on: a student's own SecurityException gets the
	 * policy's message.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelPrivilegedMinimized.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
	void privilegedOnlyHidesAForgedViolation() {
		throw new SecurityException(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): forged detail that must not show");
	}
}
