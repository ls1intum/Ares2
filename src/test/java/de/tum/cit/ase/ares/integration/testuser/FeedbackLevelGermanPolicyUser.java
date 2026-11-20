package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.fileReader.ReadFileReaderMain;

/**
 * Reads a file the policy forbids at DEBUG in German, so the hint shows that
 * the kind of an architecture finding does not depend on the language.
 */
@UseLocale("de")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(10)
@SuppressWarnings("static-method")
public class FeedbackLevelGermanPolicyUser {

	/** Where the policy lives, below its mode folder. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The student code reading a file: a folder no English test checks, because the
	 * architecture check caches its findings, message included, per set of checked
	 * classes.
	 */
	private static final String WITHIN = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/oldMethods/fileReader";

	/** A read the architecture check blocks, at DEBUG, in German. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = WITHIN)
	void debugArchitectureInGerman() throws Exception {
		ReadFileReaderMain.accessFileSystemViaFileReaderRead();
	}
}
