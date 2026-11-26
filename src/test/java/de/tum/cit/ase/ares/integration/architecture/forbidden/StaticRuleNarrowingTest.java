package de.tum.cit.ase.ares.integration.architecture.forbidden;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.allowed.StaticAllowedMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.fileInputStream.FileInputStreamWrappedMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.fileReader.FileReaderWrappedMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.provider.ProviderStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.rootHandler.RootLogHandlerMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.setOut.SetOutMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.threadName.ThreadRenameMain;

/**
 * Checks the static rules under a policy without file and thread allowances:
 * stream wrappers over memory, thread getters and a named logger pass, while a
 * file opened under a wrapper, renaming a thread, a root log handler and a new
 * {@code System.out} are refused with the rule and the call named.
 */
class StaticRuleNarrowingTest extends SystemAccessTest {

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The folder holding the subjects' classes.
	 */
	private static final String SUBJECTS = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/staticNarrowing/";

	/**
	 * The policy without file and thread allowances for ArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = POLICY_BASE
			+ "archunit/aspectj/PolicyStaticRuleNarrowing.yaml";

	/**
	 * The policy without file and thread allowances for ArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = POLICY_BASE
			+ "archunit/instrumentation/PolicyStaticRuleNarrowing.yaml";

	/**
	 * The policy without file and thread allowances for WalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = POLICY_BASE + "wala/aspectj/PolicyStaticRuleNarrowing.yaml";

	/**
	 * The policy without file and thread allowances for WalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = POLICY_BASE
			+ "wala/instrumentation/PolicyStaticRuleNarrowing.yaml";

	/**
	 * Where the allowed subject's classes are.
	 */
	private static final String PATH_ALLOWED = SUBJECTS + "allowed";

	/**
	 * Where the fileReader subject's classes are.
	 */
	private static final String PATH_FILEREADER = SUBJECTS + "fileReader";

	/**
	 * Where the fileInputStream subject's classes are.
	 */
	private static final String PATH_FILEINPUTSTREAM = SUBJECTS + "fileInputStream";

	/**
	 * Where the provider subject's classes are.
	 */
	private static final String PATH_PROVIDER = SUBJECTS + "provider";

	/**
	 * Where the threadName subject's classes are.
	 */
	private static final String PATH_THREADNAME = SUBJECTS + "threadName";

	/**
	 * Where the rootHandler subject's classes are.
	 */
	private static final String PATH_ROOTHANDLER = SUBJECTS + "rootHandler";

	/**
	 * Where the setOut subject's classes are.
	 */
	private static final String PATH_SETOUT = SUBJECTS + "setOut";

	// <editor-fold desc="allowed">
	/**
	 * Stream wrappers over memory, thread getters and a named logger pass the
	 * static rules in ArchunitAspectJ.
	 *
	 * @throws IOException never, the streams are in memory
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_ALLOWED)
	void test_wrappersAndGettersAllowedMavenArchunitAspectJ_test() throws IOException {
		Assertions.assertEquals("97:2:97:120:value:false:false:true", StaticAllowedMain.useWrappers());
	}

	/**
	 * Stream wrappers over memory, thread getters and a named logger pass the
	 * static rules in ArchunitInstrumentation.
	 *
	 * @throws IOException never, the streams are in memory
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_ALLOWED)
	void test_wrappersAndGettersAllowedMavenArchunitInstrumentation_test() throws IOException {
		Assertions.assertEquals("97:2:97:120:value:false:false:true", StaticAllowedMain.useWrappers());
	}

	/**
	 * Stream wrappers over memory, thread getters and a named logger pass the
	 * static rules in WalaAspectJ.
	 *
	 * @throws IOException never, the streams are in memory
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_ALLOWED)
	void test_wrappersAndGettersAllowedMavenWalaAspectJ_test() throws IOException {
		Assertions.assertEquals("97:2:97:120:value:false:false:true", StaticAllowedMain.useWrappers());
	}

	/**
	 * Stream wrappers over memory, thread getters and a named logger pass the
	 * static rules in WalaInstrumentation.
	 *
	 * @throws IOException never, the streams are in memory
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_ALLOWED)
	void test_wrappersAndGettersAllowedMavenWalaInstrumentation_test() throws IOException {
		Assertions.assertEquals("97:2:97:120:value:false:false:true", StaticAllowedMain.useWrappers());
	}

	// </editor-fold>

	// <editor-fold desc="fileReader">
	/**
	 * The static rules refuse a file read through a buffered {@code FileReader} in
	 * ArchunitAspectJ.
	 */
	@Test
	void test_fileReaderRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileReaderRefusedMavenArchunitAspectJ", "file.system.access", "java.io.FileReader.<init>");
	}

	/**
	 * Runs a file read through a buffered {@code FileReader} under the policy for
	 * ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_FILEREADER)
	void test_fileReaderRefusedMavenArchunitAspectJ() throws Exception {
		FileReaderWrappedMain.read();
	}

	/**
	 * The static rules refuse a file read through a buffered {@code FileReader} in
	 * ArchunitInstrumentation.
	 */
	@Test
	void test_fileReaderRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileReaderRefusedMavenArchunitInstrumentation", "file.system.access",
				"java.io.FileReader.<init>");
	}

	/**
	 * Runs a file read through a buffered {@code FileReader} under the policy for
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_FILEREADER)
	void test_fileReaderRefusedMavenArchunitInstrumentation() throws Exception {
		FileReaderWrappedMain.read();
	}

	/**
	 * The static rules refuse a file read through a buffered {@code FileReader} in
	 * WalaAspectJ.
	 */
	@Test
	void test_fileReaderRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_fileReaderRefusedMavenWalaAspectJ",
				"file.system.access", "java.io.FileReader.<init>");
	}

	/**
	 * Runs a file read through a buffered {@code FileReader} under the policy for
	 * WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_FILEREADER)
	void test_fileReaderRefusedMavenWalaAspectJ() throws Exception {
		FileReaderWrappedMain.read();
	}

	/**
	 * The static rules refuse a file read through a buffered {@code FileReader} in
	 * WalaInstrumentation.
	 */
	@Test
	void test_fileReaderRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileReaderRefusedMavenWalaInstrumentation", "file.system.access", "java.io.FileReader.<init>");
	}

	/**
	 * Runs a file read through a buffered {@code FileReader} under the policy for
	 * WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_FILEREADER)
	void test_fileReaderRefusedMavenWalaInstrumentation() throws Exception {
		FileReaderWrappedMain.read();
	}

	// </editor-fold>

	// <editor-fold desc="fileInputStream">
	/**
	 * The static rules refuse properties loaded from a buffered
	 * {@code FileInputStream} in ArchunitAspectJ.
	 */
	@Test
	void test_fileInputStreamRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileInputStreamRefusedMavenArchunitAspectJ", "file.system.access",
				"java.io.FileInputStream.<init>");
	}

	/**
	 * Runs properties loaded from a buffered {@code FileInputStream} under the
	 * policy for ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_FILEINPUTSTREAM)
	void test_fileInputStreamRefusedMavenArchunitAspectJ() throws Exception {
		FileInputStreamWrappedMain.read();
	}

	/**
	 * The static rules refuse properties loaded from a buffered
	 * {@code FileInputStream} in ArchunitInstrumentation.
	 */
	@Test
	void test_fileInputStreamRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileInputStreamRefusedMavenArchunitInstrumentation", "file.system.access",
				"java.io.FileInputStream.<init>");
	}

	/**
	 * Runs properties loaded from a buffered {@code FileInputStream} under the
	 * policy for ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_FILEINPUTSTREAM)
	void test_fileInputStreamRefusedMavenArchunitInstrumentation() throws Exception {
		FileInputStreamWrappedMain.read();
	}

	/**
	 * The static rules refuse properties loaded from a buffered
	 * {@code FileInputStream} in WalaAspectJ.
	 */
	@Test
	void test_fileInputStreamRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileInputStreamRefusedMavenWalaAspectJ", "file.system.access", "java.io.FileInputStream.<init>");
	}

	/**
	 * Runs properties loaded from a buffered {@code FileInputStream} under the
	 * policy for WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_FILEINPUTSTREAM)
	void test_fileInputStreamRefusedMavenWalaAspectJ() throws Exception {
		FileInputStreamWrappedMain.read();
	}

	/**
	 * The static rules refuse properties loaded from a buffered
	 * {@code FileInputStream} in WalaInstrumentation.
	 */
	@Test
	void test_fileInputStreamRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_fileInputStreamRefusedMavenWalaInstrumentation", "file.system.access",
				"java.io.FileInputStream.<init>");
	}

	/**
	 * Runs properties loaded from a buffered {@code FileInputStream} under the
	 * policy for WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_FILEINPUTSTREAM)
	void test_fileInputStreamRefusedMavenWalaInstrumentation() throws Exception {
		FileInputStreamWrappedMain.read();
	}

	// </editor-fold>

	// <editor-fold desc="provider">
	/**
	 * The static rules refuse a stream opened through the file system provider in
	 * ArchunitAspectJ.
	 */
	@Test
	void test_providerRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_providerRefusedMavenArchunitAspectJ",
				"file.system.access", "newInputStream");
	}

	/**
	 * Runs a stream opened through the file system provider under the policy for
	 * ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_PROVIDER)
	void test_providerRefusedMavenArchunitAspectJ() throws Exception {
		ProviderStreamMain.read();
	}

	/**
	 * The static rules refuse a stream opened through the file system provider in
	 * ArchunitInstrumentation.
	 */
	@Test
	void test_providerRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_providerRefusedMavenArchunitInstrumentation", "file.system.access", "newInputStream");
	}

	/**
	 * Runs a stream opened through the file system provider under the policy for
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_PROVIDER)
	void test_providerRefusedMavenArchunitInstrumentation() throws Exception {
		ProviderStreamMain.read();
	}

	/**
	 * The static rules refuse a stream opened through the file system provider in
	 * WalaAspectJ.
	 */
	@Test
	void test_providerRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_providerRefusedMavenWalaAspectJ",
				"file.system.access", "newInputStream");
	}

	/**
	 * Runs a stream opened through the file system provider under the policy for
	 * WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_PROVIDER)
	void test_providerRefusedMavenWalaAspectJ() throws Exception {
		ProviderStreamMain.read();
	}

	/**
	 * The static rules refuse a stream opened through the file system provider in
	 * WalaInstrumentation.
	 */
	@Test
	void test_providerRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_providerRefusedMavenWalaInstrumentation", "file.system.access", "newInputStream");
	}

	/**
	 * Runs a stream opened through the file system provider under the policy for
	 * WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_PROVIDER)
	void test_providerRefusedMavenWalaInstrumentation() throws Exception {
		ProviderStreamMain.read();
	}

	// </editor-fold>

	// <editor-fold desc="threadName">
	/**
	 * The static rules refuse renaming the current thread in ArchunitAspectJ.
	 */
	@Test
	void test_threadNameRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_threadNameRefusedMavenArchunitAspectJ", "manipulate.threads", "setName");
	}

	/**
	 * Runs renaming the current thread under the policy for ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_THREADNAME)
	void test_threadNameRefusedMavenArchunitAspectJ() throws Exception {
		ThreadRenameMain.rename();
	}

	/**
	 * The static rules refuse renaming the current thread in
	 * ArchunitInstrumentation.
	 */
	@Test
	void test_threadNameRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_threadNameRefusedMavenArchunitInstrumentation", "manipulate.threads", "setName");
	}

	/**
	 * Runs renaming the current thread under the policy for
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_THREADNAME)
	void test_threadNameRefusedMavenArchunitInstrumentation() throws Exception {
		ThreadRenameMain.rename();
	}

	/**
	 * The static rules refuse renaming the current thread in WalaAspectJ.
	 */
	@Test
	void test_threadNameRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_threadNameRefusedMavenWalaAspectJ",
				"manipulate.threads", "setName");
	}

	/**
	 * Runs renaming the current thread under the policy for WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_THREADNAME)
	void test_threadNameRefusedMavenWalaAspectJ() throws Exception {
		ThreadRenameMain.rename();
	}

	/**
	 * The static rules refuse renaming the current thread in WalaInstrumentation.
	 */
	@Test
	void test_threadNameRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_threadNameRefusedMavenWalaInstrumentation", "manipulate.threads", "setName");
	}

	/**
	 * Runs renaming the current thread under the policy for WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_THREADNAME)
	void test_threadNameRefusedMavenWalaInstrumentation() throws Exception {
		ThreadRenameMain.rename();
	}

	// </editor-fold>

	// <editor-fold desc="rootHandler">
	/**
	 * The static rules refuse a handler added to the root logger in
	 * ArchunitAspectJ.
	 */
	@Test
	void test_rootHandlerRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_rootHandlerRefusedMavenArchunitAspectJ", "environment.access", "addHandler");
	}

	/**
	 * Runs a handler added to the root logger under the policy for ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_ROOTHANDLER)
	void test_rootHandlerRefusedMavenArchunitAspectJ() throws Exception {
		RootLogHandlerMain.addRootHandler();
	}

	/**
	 * The static rules refuse a handler added to the root logger in
	 * ArchunitInstrumentation.
	 */
	@Test
	void test_rootHandlerRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_rootHandlerRefusedMavenArchunitInstrumentation", "environment.access", "addHandler");
	}

	/**
	 * Runs a handler added to the root logger under the policy for
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_ROOTHANDLER)
	void test_rootHandlerRefusedMavenArchunitInstrumentation() throws Exception {
		RootLogHandlerMain.addRootHandler();
	}

	/**
	 * The static rules refuse a handler added to the root logger in WalaAspectJ.
	 */
	@Test
	void test_rootHandlerRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_rootHandlerRefusedMavenWalaAspectJ",
				"environment.access", "addHandler");
	}

	/**
	 * Runs a handler added to the root logger under the policy for WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_ROOTHANDLER)
	void test_rootHandlerRefusedMavenWalaAspectJ() throws Exception {
		RootLogHandlerMain.addRootHandler();
	}

	/**
	 * The static rules refuse a handler added to the root logger in
	 * WalaInstrumentation.
	 */
	@Test
	void test_rootHandlerRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_rootHandlerRefusedMavenWalaInstrumentation", "environment.access", "addHandler");
	}

	/**
	 * Runs a handler added to the root logger under the policy for
	 * WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_ROOTHANDLER)
	void test_rootHandlerRefusedMavenWalaInstrumentation() throws Exception {
		RootLogHandlerMain.addRootHandler();
	}

	// </editor-fold>

	// <editor-fold desc="setOut">
	/**
	 * The static rules refuse replacing {@code System.out} in ArchunitAspectJ.
	 */
	@Test
	void test_setOutRefusedMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_setOutRefusedMavenArchunitAspectJ",
				"environment.access", "setOut");
	}

	/**
	 * Runs replacing {@code System.out} under the policy for ArchunitAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = PATH_SETOUT)
	void test_setOutRefusedMavenArchunitAspectJ() throws Exception {
		SetOutMain.replaceOut();
	}

	/**
	 * The static rules refuse replacing {@code System.out} in
	 * ArchunitInstrumentation.
	 */
	@Test
	void test_setOutRefusedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_setOutRefusedMavenArchunitInstrumentation", "environment.access", "setOut");
	}

	/**
	 * Runs replacing {@code System.out} under the policy for
	 * ArchunitInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = PATH_SETOUT)
	void test_setOutRefusedMavenArchunitInstrumentation() throws Exception {
		SetOutMain.replaceOut();
	}

	/**
	 * The static rules refuse replacing {@code System.out} in WalaAspectJ.
	 */
	@Test
	void test_setOutRefusedMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class, "test_setOutRefusedMavenWalaAspectJ",
				"environment.access", "setOut");
	}

	/**
	 * Runs replacing {@code System.out} under the policy for WalaAspectJ.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = PATH_SETOUT)
	void test_setOutRefusedMavenWalaAspectJ() throws Exception {
		SetOutMain.replaceOut();
	}

	/**
	 * The static rules refuse replacing {@code System.out} in WalaInstrumentation.
	 */
	@Test
	void test_setOutRefusedMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(StaticRuleNarrowingTest.class,
				"test_setOutRefusedMavenWalaInstrumentation", "environment.access", "setOut");
	}

	/**
	 * Runs replacing {@code System.out} under the policy for WalaInstrumentation.
	 *
	 * @throws Exception if the subject fails
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = PATH_SETOUT)
	void test_setOutRefusedMavenWalaInstrumentation() throws Exception {
		SetOutMain.replaceOut();
	}

	// </editor-fold>
}
