package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.networkSystem.localUrl.LocalUrlReadMain;

/**
 * Checks how a {@code file:} URL is read under a policy that allows one file
 * and one unrelated host. Instrumentation leaves a URL on this machine to the
 * file check, which reads the allowed file and refuses another. AspectJ cannot
 * see the file the JDK opens behind the URL, so it keeps refusing a
 * {@code file:} URL as a connection. A URL naming another host is refused in
 * both.
 */
class LocalUrlReadTest {

	/** Folder of the student code the static analysis inspects. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/networkSystem/localUrl";

	/** The file the policies allow reading. */
	private static final String ALLOWED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";

	/** A file beside it that the policies do not allow. */
	private static final String OTHER_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";

	/** A file URL on a host that never resolves. */
	private static final String REMOTE_FILE_URL = "file://example.invalid/secret.txt";

	/** A jar URL whose archive lies on that host. */
	private static final String REMOTE_JAR_URL = "jar:file://example.invalid/secret.jar!/entry";

	/** A file URL without a host whose path names a network share. */
	private static final String SHARE_FILE_URL = "file:////example.invalid/share/secret.txt";

	/**
	 * Even the allowed file is refused through its file: URL in ArchunitAspectJ, as
	 * no file check sees what the URL opens.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedFileRefusedAsConnectionThroughFileUrlMavenArchunitAspectJ() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(ALLOWED_FILE));
	}

	/** Another file stays refused through its file: URL in ArchunitAspectJ. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_otherFileRefusedThroughFileUrlMavenArchunitAspectJ() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(OTHER_FILE));
	}

	/**
	 * The allowed file is readable through its file: URL in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedFileThroughFileUrlMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> LocalUrlReadMain.readThroughFileUrl(ALLOWED_FILE));
	}

	/**
	 * Another file stays refused through its file: URL in ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_otherFileRefusedThroughFileUrlMavenArchunitInstrumentation() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(OTHER_FILE));
	}

	/**
	 * Even the allowed file is refused through its file: URL in WalaAspectJ, as no
	 * file check sees what the URL opens.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedFileRefusedAsConnectionThroughFileUrlMavenWalaAspectJ() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(ALLOWED_FILE));
	}

	/** Another file stays refused through its file: URL in WalaAspectJ. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_otherFileRefusedThroughFileUrlMavenWalaAspectJ() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(OTHER_FILE));
	}

	/**
	 * The allowed file is readable through its file: URL in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedFileThroughFileUrlMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> LocalUrlReadMain.readThroughFileUrl(ALLOWED_FILE));
	}

	/** Another file stays refused through its file: URL in WalaInstrumentation. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_otherFileRefusedThroughFileUrlMavenWalaInstrumentation() {
		assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughFileUrl(OTHER_FILE));
	}

	/**
	 * A file: or jar: URL naming another host, or a network share, is refused as a
	 * connection in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_remoteFileUrlRefusedMavenArchunitAspectJ() {
		assertAll(() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_FILE_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_JAR_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(SHARE_FILE_URL)));
	}

	/**
	 * A file: or jar: URL naming another host, or a network share, is refused as a
	 * connection in ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_remoteFileUrlRefusedMavenArchunitInstrumentation() {
		assertAll(() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_FILE_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_JAR_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(SHARE_FILE_URL)));
	}

	/**
	 * A file: or jar: URL naming another host, or a network share, is refused as a
	 * connection in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_remoteFileUrlRefusedMavenWalaAspectJ() {
		assertAll(() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_FILE_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_JAR_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(SHARE_FILE_URL)));
	}

	/**
	 * A file: or jar: URL naming another host, or a network share, is refused as a
	 * connection in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalUrlRead.yaml", withinPath = WITHIN_PATH)
	void test_remoteFileUrlRefusedMavenWalaInstrumentation() {
		assertAll(() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_FILE_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(REMOTE_JAR_URL)),
				() -> assertThrows(SecurityException.class, () -> LocalUrlReadMain.readThroughUrl(SHARE_FILE_URL)));
	}
}
