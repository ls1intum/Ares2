package de.tum.cit.ase.ares.integration.architecture.forbidden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * Checks that a policy without any file rule refuses two file constructors that
 * take a character set or a path already in the static analysis. WALA does not
 * look inside these JDK classes, so only their own list entry can refuse them.
 * Each probe runs nothing itself, so only the static analysis can make it fail.
 */
class FileSystemCharsetConstructorStaticTest extends SystemAccessTest {

	/**
	 * Folder of the student code calling
	 * {@code new Formatter(String, Charset, Locale)}.
	 */
	private static final String FORMATTER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/charsetStatic/formatter";

	/**
	 * Folder of the student code calling {@code new Scanner(Path)}.
	 */
	private static final String SCANNER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/charsetStatic/scanner";

	// <editor-fold desc="formatterWithCharset">

	/**
	 * {@code new Formatter(String, Charset, Locale)} is refused statically with
	 * ArchUnit and AspectJ.
	 */
	@Test
	void test_formatterWithCharsetWithoutFileRulesMavenArchunitAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_formatterWithCharsetWithoutFileRulesMavenArchunitAspectJ");
	}

	/**
	 * {@code new Formatter(String, Charset, Locale)} is refused statically with
	 * ArchUnit and instrumentation.
	 */
	@Test
	void test_formatterWithCharsetWithoutFileRulesMavenArchunitInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_formatterWithCharsetWithoutFileRulesMavenArchunitInstrumentation");
	}

	/**
	 * {@code new Formatter(String, Charset, Locale)} is refused statically with
	 * WALA and AspectJ.
	 */
	@Test
	void test_formatterWithCharsetWithoutFileRulesMavenWalaAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_formatterWithCharsetWithoutFileRulesMavenWalaAspectJ");
	}

	/**
	 * {@code new Formatter(String, Charset, Locale)} is refused statically with
	 * WALA and instrumentation.
	 */
	@Test
	void test_formatterWithCharsetWithoutFileRulesMavenWalaInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_formatterWithCharsetWithoutFileRulesMavenWalaInstrumentation");
	}

	/**
	 * The probe for {@code new Formatter(String, Charset, Locale)} with ArchUnit
	 * and AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FORMATTER_PATH)
	void test_formatterWithCharsetWithoutFileRulesMavenArchunitAspectJ() {
		Assertions.assertTrue(FORMATTER_PATH.endsWith("formatter"));
	}

	/**
	 * The probe for {@code new Formatter(String, Charset, Locale)} with ArchUnit
	 * and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FORMATTER_PATH)
	void test_formatterWithCharsetWithoutFileRulesMavenArchunitInstrumentation() {
		Assertions.assertTrue(FORMATTER_PATH.endsWith("formatter"));
	}

	/**
	 * The probe for {@code new Formatter(String, Charset, Locale)} with WALA and
	 * AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = FORMATTER_PATH)
	void test_formatterWithCharsetWithoutFileRulesMavenWalaAspectJ() {
		Assertions.assertTrue(FORMATTER_PATH.endsWith("formatter"));
	}

	/**
	 * The probe for {@code new Formatter(String, Charset, Locale)} with WALA and
	 * instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FORMATTER_PATH)
	void test_formatterWithCharsetWithoutFileRulesMavenWalaInstrumentation() {
		Assertions.assertTrue(FORMATTER_PATH.endsWith("formatter"));
	}
	// </editor-fold>

	// <editor-fold desc="scannerWithPath">

	/**
	 * {@code new Scanner(Path)} is refused statically with ArchUnit and AspectJ.
	 */
	@Test
	void test_scannerWithPathWithoutFileRulesMavenArchunitAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_scannerWithPathWithoutFileRulesMavenArchunitAspectJ");
	}

	/**
	 * {@code new Scanner(Path)} is refused statically with ArchUnit and
	 * instrumentation.
	 */
	@Test
	void test_scannerWithPathWithoutFileRulesMavenArchunitInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_scannerWithPathWithoutFileRulesMavenArchunitInstrumentation");
	}

	/**
	 * {@code new Scanner(Path)} is refused statically with WALA and AspectJ.
	 */
	@Test
	void test_scannerWithPathWithoutFileRulesMavenWalaAspectJ_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_scannerWithPathWithoutFileRulesMavenWalaAspectJ");
	}

	/**
	 * {@code new Scanner(Path)} is refused statically with WALA and
	 * instrumentation.
	 */
	@Test
	void test_scannerWithPathWithoutFileRulesMavenWalaInstrumentation_test() {
		assertRefusedStaticallyForFileAccess(FileSystemCharsetConstructorStaticTest.class,
				"test_scannerWithPathWithoutFileRulesMavenWalaInstrumentation");
	}

	/**
	 * The probe for {@code new Scanner(Path)} with ArchUnit and AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = SCANNER_PATH)
	void test_scannerWithPathWithoutFileRulesMavenArchunitAspectJ() {
		Assertions.assertTrue(SCANNER_PATH.endsWith("scanner"));
	}

	/**
	 * The probe for {@code new Scanner(Path)} with ArchUnit and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = SCANNER_PATH)
	void test_scannerWithPathWithoutFileRulesMavenArchunitInstrumentation() {
		Assertions.assertTrue(SCANNER_PATH.endsWith("scanner"));
	}

	/**
	 * The probe for {@code new Scanner(Path)} with WALA and AspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = SCANNER_PATH)
	void test_scannerWithPathWithoutFileRulesMavenWalaAspectJ() {
		Assertions.assertTrue(SCANNER_PATH.endsWith("scanner"));
	}

	/**
	 * The probe for {@code new Scanner(Path)} with WALA and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = SCANNER_PATH)
	void test_scannerWithPathWithoutFileRulesMavenWalaInstrumentation() {
		Assertions.assertTrue(SCANNER_PATH.endsWith("scanner"));
	}
	// </editor-fold>
}
