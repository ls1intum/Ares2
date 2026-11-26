package de.tum.cit.ase.ares.integration.architecture.forbidden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * Checks that a policy without any file rule refuses asking whether a file
 * exists already in the static analysis. Each probe runs nothing itself, so
 * only the static analysis can make it fail.
 */
class FileSystemInspectStaticTest extends SystemAccessTest {

	/**
	 * Folder of the student code asking whether a file exists.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/inspectStatic";

	/**
	 * Refused statically with ArchUnit and AspectJ.
	 */
	@Test
	void test_fileExistsWithoutFileRulesMavenArchunitAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemInspectStaticTest.class,
				"test_fileExistsWithoutFileRulesMavenArchunitAspectJ");
	}

	/**
	 * Refused statically with ArchUnit and instrumentation.
	 */
	@Test
	void test_fileExistsWithoutFileRulesMavenArchunitInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemInspectStaticTest.class,
				"test_fileExistsWithoutFileRulesMavenArchunitInstrumentation");
	}

	/**
	 * Refused statically with WALA and AspectJ.
	 */
	@Test
	void test_fileExistsWithoutFileRulesMavenWalaAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemInspectStaticTest.class,
				"test_fileExistsWithoutFileRulesMavenWalaAspectJ");
	}

	/**
	 * Refused statically with WALA and instrumentation.
	 */
	@Test
	void test_fileExistsWithoutFileRulesMavenWalaInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemInspectStaticTest.class,
				"test_fileExistsWithoutFileRulesMavenWalaInstrumentation");
	}

	/**
	 * The probe for ArchUnit and AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_fileExistsWithoutFileRulesMavenArchunitAspectJ() {
		Assertions.assertTrue(WITHIN_PATH.endsWith("inspectStatic"));
	}

	/**
	 * The probe for ArchUnit and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_fileExistsWithoutFileRulesMavenArchunitInstrumentation() {
		Assertions.assertTrue(WITHIN_PATH.endsWith("inspectStatic"));
	}

	/**
	 * The probe for WALA and AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_fileExistsWithoutFileRulesMavenWalaAspectJ() {
		Assertions.assertTrue(WITHIN_PATH.endsWith("inspectStatic"));
	}

	/**
	 * The probe for WALA and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_fileExistsWithoutFileRulesMavenWalaInstrumentation() {
		Assertions.assertTrue(WITHIN_PATH.endsWith("inspectStatic"));
	}
}
