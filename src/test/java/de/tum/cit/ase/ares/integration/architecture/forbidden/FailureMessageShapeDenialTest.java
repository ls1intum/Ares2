package de.tum.cit.ase.ares.integration.architecture.forbidden;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;
import de.tum.cit.ase.ares.integration.architecture.forbidden.subject.typeannotation.TypeAnnotationParameterMain;
import de.tum.cit.ase.ares.testutilities.MessageTemplates;

/**
 * Checks, in all four mode combinations, the message a denial leaves in the
 * test report: exactly a {@link SecurityException}, one prefix naming the
 * student code while it runs, and the concrete cause in the first line, for a
 * file read refused while the test runs and for a forbidden import refused by
 * the analysis before it. No raw message key may be left in either. Each case
 * runs as a disabled inner test, as the other architecture tests do.
 */
class FailureMessageShapeDenialTest extends SystemAccessTest {

	/** The folder of the student code that reads a file. */
	private static final String FILE_READ_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream";

	/** The folder of the student code that imports a forbidden package. */
	private static final String IMPORT_PATH = "test-classes/de/tum/cit/ase/ares/integration/architecture/forbidden/subject/typeannotation";

	/** The file the student code reads, which no rule allows. */
	private static final String READ_FILE = "nottrusted.txt";

	/** The package the student code imports, which no rule allows. */
	private static final String FORBIDDEN_PACKAGE = "org.example.subject.typeuse.foreign";

	/** The start of every raw key of the runtime advice. */
	private static final String RAW_ADVICE_KEY = "security.advice.";

	/** Finds any Ares prefix, in English or German, to count them. */
	private static final Pattern ANY_PREFIX = Pattern.compile("Ares (Security Error|Sicherheitsfehler) \\(");

	/** The policy allowing one other file to be read, for ArchUnit and AspectJ. */
	private static final String READ_POLICY_ARCHUNIT_ASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml";

	/**
	 * The policy allowing no import of the foreign package, for ArchUnit and
	 * AspectJ.
	 */
	private static final String IMPORT_POLICY_ARCHUNIT_ASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * The policy allowing one other file to be read, for ArchUnit and
	 * instrumentation.
	 */
	private static final String READ_POLICY_ARCHUNIT_INSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml";

	/**
	 * The policy allowing no import of the foreign package, for ArchUnit and
	 * instrumentation.
	 */
	private static final String IMPORT_POLICY_ARCHUNIT_INSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyTypeAnnotationImportForbidden.yaml";

	/** The policy allowing one other file to be read, for WALA and AspectJ. */
	private static final String READ_POLICY_WALA_ASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml";

	/**
	 * The policy allowing no import of the foreign package, for WALA and AspectJ.
	 */
	private static final String IMPORT_POLICY_WALA_ASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * The policy allowing one other file to be read, for WALA and instrumentation.
	 */
	private static final String READ_POLICY_WALA_INSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml";

	/**
	 * The policy allowing no import of the foreign package, for WALA and
	 * instrumentation.
	 */
	private static final String IMPORT_POLICY_WALA_INSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyTypeAnnotationImportForbidden.yaml";

	// <editor-fold desc="ArchUnit and AspectJ">
	/**
	 * A refused file read states its cause in its first line, in ArchUnit and
	 * AspectJ.
	 */
	@Test
	void test_fileReadMessageShapeMavenArchunitAspectJ_test() {
		assertFileReadShape(failureOf("test_fileReadMessageShapeMavenArchunitAspectJ"));
	}

	/**
	 * A refused import states its cause in its first line, in ArchUnit and AspectJ.
	 */
	@Test
	void test_forbiddenImportMessageShapeMavenArchunitAspectJ_test() {
		assertImportShape(failureOf("test_forbiddenImportMessageShapeMavenArchunitAspectJ"));
	}

	/**
	 * Probe: student code reads a file no rule allows, in ArchUnit and AspectJ.
	 *
	 * @throws IOException if the read fails other than by Ares
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = READ_POLICY_ARCHUNIT_ASPECTJ, withinPath = FILE_READ_PATH)
	void test_fileReadMessageShapeMavenArchunitAspectJ() throws IOException {
		FileinputStreamMain.accessFileSystemViaFileInputStream();
	}

	/** Probe: student code imports a forbidden package, in ArchUnit and AspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = IMPORT_POLICY_ARCHUNIT_ASPECTJ, withinPath = IMPORT_PATH)
	void test_forbiddenImportMessageShapeMavenArchunitAspectJ() {
		TypeAnnotationParameterMain.toText("text");
	}
	// </editor-fold>

	// <editor-fold desc="ArchUnit and instrumentation">
	/**
	 * A refused file read states its cause in its first line, in ArchUnit and
	 * instrumentation.
	 */
	@Test
	void test_fileReadMessageShapeMavenArchunitInstrumentation_test() {
		assertFileReadShape(failureOf("test_fileReadMessageShapeMavenArchunitInstrumentation"));
	}

	/**
	 * A refused import states its cause in its first line, in ArchUnit and
	 * instrumentation.
	 */
	@Test
	void test_forbiddenImportMessageShapeMavenArchunitInstrumentation_test() {
		assertImportShape(failureOf("test_forbiddenImportMessageShapeMavenArchunitInstrumentation"));
	}

	/**
	 * Probe: student code reads a file no rule allows, in ArchUnit and
	 * instrumentation.
	 *
	 * @throws IOException if the read fails other than by Ares
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = READ_POLICY_ARCHUNIT_INSTRUMENTATION, withinPath = FILE_READ_PATH)
	void test_fileReadMessageShapeMavenArchunitInstrumentation() throws IOException {
		FileinputStreamMain.accessFileSystemViaFileInputStream();
	}

	/**
	 * Probe: student code imports a forbidden package, in ArchUnit and
	 * instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = IMPORT_POLICY_ARCHUNIT_INSTRUMENTATION, withinPath = IMPORT_PATH)
	void test_forbiddenImportMessageShapeMavenArchunitInstrumentation() {
		TypeAnnotationParameterMain.toText("text");
	}
	// </editor-fold>

	// <editor-fold desc="WALA and AspectJ">
	/**
	 * A refused file read states its cause in its first line, in WALA and AspectJ.
	 */
	@Test
	void test_fileReadMessageShapeMavenWalaAspectJ_test() {
		assertFileReadShape(failureOf("test_fileReadMessageShapeMavenWalaAspectJ"));
	}

	/** A refused import states its cause in its first line, in WALA and AspectJ. */
	@Test
	void test_forbiddenImportMessageShapeMavenWalaAspectJ_test() {
		assertImportShape(failureOf("test_forbiddenImportMessageShapeMavenWalaAspectJ"));
	}

	/**
	 * Probe: student code reads a file no rule allows, in WALA and AspectJ.
	 *
	 * @throws IOException if the read fails other than by Ares
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = READ_POLICY_WALA_ASPECTJ, withinPath = FILE_READ_PATH)
	void test_fileReadMessageShapeMavenWalaAspectJ() throws IOException {
		FileinputStreamMain.accessFileSystemViaFileInputStream();
	}

	/** Probe: student code imports a forbidden package, in WALA and AspectJ. */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = IMPORT_POLICY_WALA_ASPECTJ, withinPath = IMPORT_PATH)
	void test_forbiddenImportMessageShapeMavenWalaAspectJ() {
		TypeAnnotationParameterMain.toText("text");
	}
	// </editor-fold>

	// <editor-fold desc="WALA and instrumentation">
	/**
	 * A refused file read states its cause in its first line, in WALA and
	 * instrumentation.
	 */
	@Test
	void test_fileReadMessageShapeMavenWalaInstrumentation_test() {
		assertFileReadShape(failureOf("test_fileReadMessageShapeMavenWalaInstrumentation"));
	}

	/**
	 * A refused import states its cause in its first line, in WALA and
	 * instrumentation.
	 */
	@Test
	void test_forbiddenImportMessageShapeMavenWalaInstrumentation_test() {
		assertImportShape(failureOf("test_forbiddenImportMessageShapeMavenWalaInstrumentation"));
	}

	/**
	 * Probe: student code reads a file no rule allows, in WALA and instrumentation.
	 *
	 * @throws IOException if the read fails other than by Ares
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = READ_POLICY_WALA_INSTRUMENTATION, withinPath = FILE_READ_PATH)
	void test_fileReadMessageShapeMavenWalaInstrumentation() throws IOException {
		FileinputStreamMain.accessFileSystemViaFileInputStream();
	}

	/**
	 * Probe: student code imports a forbidden package, in WALA and instrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = IMPORT_POLICY_WALA_INSTRUMENTATION, withinPath = IMPORT_PATH)
	void test_forbiddenImportMessageShapeMavenWalaInstrumentation() {
		TypeAnnotationParameterMain.toText("text");
	}
	// </editor-fold>

	/**
	 * Checks a refused file read: the one denial sentence, naming the student
	 * class, the read, the file and the reason that a rule exists but allows
	 * another file.
	 *
	 * @param failure what the probe failed with
	 */
	private static void assertFileReadShape(Throwable failure) {
		String firstLine = assertCommonShape(failure);
		Optional<List<String>> values = MessageTemplates.argumentsOf("security.advice.illegal.file.execution", 5,
				firstLine);
		assertThat(values).as(firstLine).isPresent();
		List<String> arguments = values.orElseThrow();
		assertThat(arguments.get(0)).as(firstLine).contains(FileinputStreamMain.class.getName());
		assertThat(arguments.get(1)).as(firstLine).isEqualTo(Messages.localized("security.advice.action.read"));
		assertThat(arguments.get(2)).as(firstLine).contains(READ_FILE);
		assertThat(arguments.get(4)).as(firstLine)
				.isEqualTo(Messages.localized("security.advice.denial.reason.target.not.allowed.file"));
	}

	/**
	 * Checks a refused import: the import sentence, in the singular or the plural,
	 * naming the forbidden package and, when the rule reports it, the importing
	 * student class. The text without a class is tried first, since the text with
	 * one would also read it, taking its opening words for a class.
	 *
	 * @param failure what the probe failed with
	 */
	private static void assertImportShape(Throwable failure) {
		String firstLine = assertCommonShape(failure);
		Optional<List<String>> withoutClass = MessageTemplates
				.argumentsOf("security.archunit.package.import.violation.without.class", 1, firstLine)
				.or(() -> MessageTemplates
						.argumentsOf("security.archunit.package.import.violation.without.class.plural", 1, firstLine));
		Optional<List<String>> withClass = withoutClass.isPresent() ? Optional.empty()
				: MessageTemplates.argumentsOf("security.archunit.package.import.violation", 2, firstLine)
						.or(() -> MessageTemplates.argumentsOf("security.archunit.package.import.violation.plural", 2,
								firstLine));
		assertThat(withClass.isPresent() || withoutClass.isPresent()).as(firstLine).isTrue();
		withClass.ifPresent(arguments -> {
			assertThat(arguments.get(0)).as(firstLine).isEqualTo(TypeAnnotationParameterMain.class.getName());
			assertThat(List.of(arguments.get(1).split(", "))).as(firstLine).contains(FORBIDDEN_PACKAGE);
		});
		withoutClass.ifPresent(arguments -> assertThat(List.of(arguments.get(0).split(", "))).as(firstLine)
				.contains(FORBIDDEN_PACKAGE));
	}

	/**
	 * Checks what every denial shares: exactly a {@link SecurityException}, one
	 * prefix naming the student code while it runs, and no raw advice key.
	 *
	 * @param failure what the probe failed with
	 * @return the first line of the message
	 */
	private static String assertCommonShape(Throwable failure) {
		assertThat(failure.getClass()).as(String.valueOf(failure)).isEqualTo(SecurityException.class);
		String message = String.valueOf(failure.getMessage());
		String prefix = Messages.localized("security.archunit.illegal.execution", "");
		assertThat(message).startsWith(prefix);
		assertThat(ANY_PREFIX.matcher(message).results().count()).as(message).isEqualTo(1);
		assertThat(message).doesNotContain(RAW_ADVICE_KEY);
		return message.lines().findFirst().orElse("");
	}

	/**
	 * Runs one probe with the disabled condition switched off and returns what it
	 * failed with, after checking that it was the only test and that it failed.
	 *
	 * @param methodName the probe
	 * @return the failure
	 */
	private static Throwable failureOf(String methodName) {
		Events events = EngineTestKit.engine("junit-jupiter")
				.configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
				.selectors(selectMethod(FailureMessageShapeDenialTest.class, methodName)).execute().testEvents();
		events.assertStatistics(stats -> stats.failed(1).aborted(0).succeeded(0));
		return events.failed().stream().findFirst().flatMap(event -> event.getPayload(TestExecutionResult.class))
				.flatMap(TestExecutionResult::getThrowable).orElseThrow();
	}
}
