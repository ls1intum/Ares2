package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder.ProcessBuilderExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.files.FilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.untrustedFile.UntrustedFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.socket.SocketConnectMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread.CreateThreadMain;

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

	/** A blocked read at HIGH keeps today's message. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelHIGH.yaml", withinPath = WITHIN)
	void highArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at HIGH, ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelHIGH.yaml", withinPath = WITHIN)
	void highArchunitInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at HIGH, WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyFeedbackLevelHIGH.yaml", withinPath = WITHIN)
	void highWalaAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at HIGH, WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyFeedbackLevelHIGH.yaml", withinPath = WITHIN)
	void highWalaInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MEDIUM, ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelMEDIUM.yaml", withinPath = WITHIN)
	void mediumArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MEDIUM, ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelMEDIUM.yaml", withinPath = WITHIN)
	void mediumArchunitInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MEDIUM, WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyFeedbackLevelMEDIUM.yaml", withinPath = WITHIN)
	void mediumWalaAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at MEDIUM, WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyFeedbackLevelMEDIUM.yaml", withinPath = WITHIN)
	void mediumWalaInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A forbidden import of the reading code at DEBUG, aspectj. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelDebugImport.yaml", withinPath = WITHIN)
	void debugImportAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/**
	 * A read the runtime check denies inside the test, at DEBUG, instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelDebugImport.yaml", withinPath = WITHIN)
	void debugImportInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at LOW, ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelLOW.yaml", withinPath = WITHIN)
	void lowArchunitAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at LOW, ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyFeedbackLevelLOW.yaml", withinPath = WITHIN)
	void lowArchunitInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at LOW, WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyFeedbackLevelLOW.yaml", withinPath = WITHIN)
	void lowWalaAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** A blocked read at LOW, WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyFeedbackLevelLOW.yaml", withinPath = WITHIN)
	void lowWalaInstrumentation() throws Exception {
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

	/** A blocked network connection at DEBUG names the network category. */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/connect/socket")
	void debugNetwork() throws Exception {
		SocketConnectMain.connectViaSocket();
	}

	/** A blocked command at DEBUG names the command category. */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/processBuilder")
	void debugCommand() throws Exception {
		ProcessBuilderExecuteMain.executeCommandViaProcessBuilder();
	}

	/** A blocked thread start at DEBUG names the thread category. */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/thread")
	void debugThread() {
		CreateThreadMain.startThread();
	}

	/** Privileged-only reporting on: a blocked read still follows the level. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelPrivilegedLow.yaml", withinPath = WITHIN)
	void privilegedOnlyKeepsTheLevel() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/**
	 * Privileged-only reporting on, with a file permission so the runtime check
	 * denies the read inside the test: it still follows the level, AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelPrivilegedLowRuntime.yaml", withinPath = WITHIN)
	void runtimeDenialKeepsTheLevelWhenPrivilegedOnlyAspectj() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/**
	 * Privileged-only reporting on, a runtime denial inside the test,
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/instrumentation/PolicyFeedbackLevelPrivilegedLowRuntime.yaml", withinPath = WITHIN)
	void runtimeDenialKeepsTheLevelWhenPrivilegedOnlyInstrumentation() throws Exception {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** Where the student code with its own {@code File} subclass lives. */
	private static final String UNTRUSTED_WITHIN = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/untrustedFile";

	/**
	 * A student {@code File} subclass Ares refuses to inspect follows LOW, AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelLowRuntime.yaml", withinPath = UNTRUSTED_WITHIN)
	void untrustedArgumentIsShortenedAspectj() throws Exception {
		UntrustedFileReadMain.readThroughAStudentFile();
	}

	/**
	 * A student {@code File} subclass Ares refuses to inspect follows LOW,
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/instrumentation/PolicyFeedbackLevelLowRuntime.yaml", withinPath = UNTRUSTED_WITHIN)
	void untrustedArgumentIsShortenedInstrumentation() throws Exception {
		UntrustedFileReadMain.readThroughAStudentFile();
	}

	/**
	 * With privileged-only reporting on, the refused argument still follows the
	 * level, AspectJ.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelPrivilegedLowRuntime.yaml", withinPath = UNTRUSTED_WITHIN)
	void untrustedArgumentKeepsTheLevelWhenPrivilegedOnlyAspectj() throws Exception {
		UntrustedFileReadMain.readThroughAStudentFile();
	}

	/**
	 * With privileged-only reporting on, the refused argument still follows the
	 * level, instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/instrumentation/PolicyFeedbackLevelPrivilegedLowRuntime.yaml", withinPath = UNTRUSTED_WITHIN)
	void untrustedArgumentKeepsTheLevelWhenPrivilegedOnlyInstrumentation() throws Exception {
		UntrustedFileReadMain.readThroughAStudentFile();
	}

	/**
	 * Privileged-only reporting on: a student's own SecurityException gets the
	 * policy's message.
	 */
	@PublicTest
	@Policy(value = POLICIES
			+ "archunit/aspectj/PolicyFeedbackLevelPrivilegedLow.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
	void privilegedOnlyHidesAForgedViolation() {
		throw new SecurityException(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): forged detail that must not show");
	}
}
