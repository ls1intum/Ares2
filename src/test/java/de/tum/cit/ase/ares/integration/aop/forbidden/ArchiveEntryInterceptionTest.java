package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive.ArchiveEntryCalls;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive.StudentJarFile;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive.StudentZipFile;

/**
 * Checks real interception of archive calls on student subclasses. The
 * instrumentation cases run first so the shared call-site class loads after its
 * package is registered for rewriting.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArchiveEntryInterceptionTest extends SystemAccessTest {

	/** The student call sites inspected by the static analyzer. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/archive";

	/** The archive prepared before a policy starts. */
	private static Path archivePath;

	/** The student archive receiver used by all mode combinations. */
	private static StudentZipFile archive;

	/** The student jar receiver used by all mode combinations. */
	private static StudentJarFile jar;

	/**
	 * Prepares an archive outside the supervised policy.
	 *
	 * @throws IOException if the fixture cannot be written or opened
	 */
	@BeforeAll
	static void prepareArchive() throws IOException {
		archivePath = Files.createTempFile("ares-archive-interception-", ".zip");
		try (OutputStream out = Files.newOutputStream(archivePath); ZipOutputStream zip = new ZipOutputStream(out)) {
			zip.putNextEntry(new ZipEntry("entry.txt"));
			zip.write(1);
			zip.closeEntry();
		}
		archive = new StudentZipFile(archivePath);
		jar = new StudentJarFile(archivePath);
	}

	/**
	 * Closes and removes the archive after every policy finishes.
	 *
	 * @throws IOException if cleanup fails
	 */
	@AfterAll
	static void removeArchive() throws IOException {
		archive.close();
		jar.close();
		Files.deleteIfExists(archivePath);
	}

	/** Student archive calls are denied through ArchUnit and AspectJ. */
	@PublicTest
	@Order(3)
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_archiveSubclassCallsMavenArchunitAspectJ() {
		assertArchiveCallsBlocked();
	}

	/** Student archive calls are denied through ArchUnit and instrumentation. */
	@PublicTest
	@Order(2)
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_archiveSubclassCallsMavenArchunitInstrumentation() {
		assertArchiveCallsBlocked();
	}

	/** Student archive calls are denied through WALA and AspectJ. */
	@PublicTest
	@Order(4)
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_archiveSubclassCallsMavenWalaAspectJ() {
		assertArchiveCallsBlocked();
	}

	/** Student archive calls are denied through WALA and instrumentation. */
	@PublicTest
	@Order(1)
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_archiveSubclassCallsMavenWalaInstrumentation() {
		assertArchiveCallsBlocked();
	}

	/** Checks that interception blocks both calls before an override runs. */
	private void assertArchiveCallsBlocked() {
		archive.clearObservations();
		assertThrows(SecurityException.class, () -> ArchiveEntryCalls.entries(archive));
		assertThrows(SecurityException.class, () -> ArchiveEntryCalls.stream(archive, new ZipEntry("entry.txt")));
		assertFalse(archive.entryOverrideCalled());
		assertFalse(archive.sizeOverrideCalled());
		jar.clearObservations();
		assertThrows(SecurityException.class, () -> ArchiveEntryCalls.entries(jar));
		assertThrows(SecurityException.class, () -> ArchiveEntryCalls.stream(jar, new ZipEntry("entry.txt")));
		assertFalse(jar.entryOverrideCalled());
		assertFalse(jar.sizeOverrideCalled());
	}
}
