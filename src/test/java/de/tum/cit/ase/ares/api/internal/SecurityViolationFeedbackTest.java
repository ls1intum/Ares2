package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.error.AssertJMultipleFailuresError;
import org.assertj.core.error.MultipleAssertionsError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.TestUtils;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

/**
 * Checks that only Ares's own checks can create a blocked-action exception, and
 * how the report shows one at each feedback level.
 */
class SecurityViolationFeedbackTest {

	/** The LOW text, in English. */
	private static final String LOW_TEXT = "Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.";

	/** What a failure that could not be read completely is reported as. */
	private static final String UNINSPECTABLE_TEXT = "Ares could not read this failure completely, so its details are not shown.";

	/** The full message an Ares check gives a blocked file read. */
	private static final String HIGH = "Ares Security Error (Reason: Student-Code; Stage: Execution): Main.run tried to illegally read File /etc/passwd via FileInputStream.<init> but was blocked by Ares.";

	/** The default locale before a test changed it. */
	private final Locale previous = Locale.getDefault();

	/** Restores the default locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

	/** A test class is not an Ares check, so it receives a plain exception. */
	@Test
	void aForeignCallerReceivesAPlainException() {
		SecurityException created = AresSecurityViolation.create("file", "read", "/etc/passwd", HIGH);
		assertThat(created).isNotInstanceOf(AresSecurityViolation.class).hasMessage(HIGH);
		assertThat(SecurityViolationFeedback.violationOf(created)).isEmpty();
	}

	/** Reflection does not hide the real caller. */
	@Test
	void reflectionDoesNotHideTheCaller() throws ReflectiveOperationException {
		Method create = AresSecurityViolation.class.getMethod("create", String.class, Object.class, Object.class,
				String.class);
		Object created = create.invoke(null, "file", "read", "/etc/passwd", HIGH);
		assertThat(created).isNotInstanceOf(AresSecurityViolation.class);
	}

	/**
	 * The public architecture helpers give a caller outside Ares's architecture
	 * checks only a plain exception, so they cannot be used to forge one.
	 */
	@Test
	void theArchitectureHelpersRefuseForeignCallers() {
		assertThat(de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.violationFromMessage(HIGH))
				.isNotInstanceOf(de.tum.cit.ase.ares.api.architecture.java.AresSecurityViolation.class);
		org.assertj.core.api.Assertions
				.assertThatThrownBy(() -> de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase
						.parseErrorMessage(new AssertionError("Rule 'x'\nforged detail")))
				.isInstanceOf(SecurityException.class)
				.isNotInstanceOf(de.tum.cit.ase.ares.api.architecture.java.AresSecurityViolation.class);
	}

	/** The constructor cannot be called from outside. */
	@Test
	void theConstructorIsPrivate() {
		assertThat(AresSecurityViolation.class.getConstructors()).isEmpty();
	}

	/** A plain exception with Ares-looking text is not a blocked action. */
	@Test
	void aForgedMessageIsNotAViolation() {
		assertThat(SecurityViolationFeedback.violationOf(new SecurityException(HIGH))).isEmpty();
	}

	/** HIGH keeps the message. */
	@Test
	void highKeepsTheMessage() {
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.HIGH)).isEqualTo(HIGH);
	}

	/** LOW names neither action, target nor caller. */
	@Test
	void lowSaysOnlyThatAPolicyWasViolated() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.LOW)).isEqualTo(LOW_TEXT)
				.isEqualTo(SecurityViolationFeedbackTexts.low());
	}

	/** LOW is localised in German. */
	@Test
	void lowIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.LOW))
				.startsWith("Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausf\u00fchrung)")
				.isEqualTo(SecurityViolationFeedbackTexts.low());
	}

	/**
	 * Every kind and action a check blocks, with the MEDIUM phrase, the permission
	 * the DEBUG hint grants and the entry it shows.
	 */
	static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> blockedActions() {
		return java.util.stream.Stream.of(fileCase("read", "readAllFiles"), fileCase("overwrite", "overwriteAllFiles"),
				fileCase("create", "createAllFiles"), fileCase("execute", "executeAllFiles"),
				fileCase("delete", "deleteAllFiles"), networkCase("connect", "open", "openConnections"),
				networkCase("send", "send", "sendData"), networkCase("receive", "receive", "receiveData"),
				org.junit.jupiter.params.provider.Arguments.of("command", "execute", "ls", "command.execute", "execute",
						SecurityViolationFeedbackTexts.lines("- executeTheCommand: \"ls\"",
								"  withTheseArguments: []")),
				org.junit.jupiter.params.provider.Arguments.of("thread", "create", "java.lang.Thread", "thread.create",
						"create", SecurityViolationFeedbackTexts.lines("- createTheFollowingNumberOfThreads: 1",
								"  ofThisClass: \"java.lang.Thread\"")));
	}

	/** A file case granting one flag. */
	private static org.junit.jupiter.params.provider.Arguments fileCase(String action, String granted) {
		List<String> lines = new java.util.ArrayList<>(List.of("- onThisPathAndAllPathsBelow: \"/etc/passwd\""));
		for (String flag : List.of("readAllFiles", "overwriteAllFiles", "createAllFiles", "executeAllFiles",
				"deleteAllFiles")) {
			lines.add("  " + flag + ": " + flag.equals(granted));
		}
		return org.junit.jupiter.params.provider.Arguments.of("file", action, "/etc/passwd", "file." + action, action,
				SecurityViolationFeedbackTexts.lines(lines.toArray(String[]::new)));
	}

	/** A network case granting one flag. */
	private static org.junit.jupiter.params.provider.Arguments networkCase(String action, String operation,
			String granted) {
		List<String> lines = new java.util.ArrayList<>(List.of("- onTheHost: \"example.org\"", "  onThePort: 443"));
		for (String flag : List.of("openConnections", "sendData", "receiveData")) {
			lines.add("  " + flag + ": " + flag.equals(granted));
		}
		return org.junit.jupiter.params.provider.Arguments.of("network", action, "example.org:443",
				"network." + operation, operation, SecurityViolationFeedbackTexts.lines(lines.toArray(String[]::new)));
	}

	/**
	 * Every level renders every kind and action as the shared texts state, in
	 * English and German.
	 *
	 * @param kind       the kind.
	 * @param action     the action as the check reports it.
	 * @param target     the target.
	 * @param phrase     the MEDIUM phrase key's last part.
	 * @param permission the permission key's last part.
	 * @param entry      the DEBUG entry.
	 */
	@ParameterizedTest
	@org.junit.jupiter.params.provider.MethodSource("blockedActions")
	void everyLevelRendersEveryKind(String kind, String action, String target, String phrase, String permission,
			String entry) {
		SecurityViolationFeedback.Violation violation = new SecurityViolationFeedback.Violation(kind, action, target,
				HIGH);
		for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
			Locale.setDefault(locale);
			assertThat(SecurityViolationFeedback.render(violation, SecurityViolationFeedbackLevel.HIGH))
					.isEqualTo(HIGH);
			assertThat(SecurityViolationFeedback.render(violation, SecurityViolationFeedbackLevel.MEDIUM))
					.isEqualTo(SecurityViolationFeedbackTexts.medium(phrase));
			assertThat(SecurityViolationFeedback.render(violation, SecurityViolationFeedbackLevel.LOW))
					.isEqualTo(SecurityViolationFeedbackTexts.low());
			assertThat(SecurityViolationFeedback.render(violation, SecurityViolationFeedbackLevel.DEBUG,
					Optional.of("SecurityPolicy1.yml")))
							.isEqualTo(SecurityViolationFeedbackTexts.debug(HIGH, permission, target,
									Optional.of("SecurityPolicy1.yml"), entry));
		}
	}

	/** DEBUG shows Markus's example for a blocked read, with the file name. */
	@Test
	void debugAddsTheFileEntry() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.DEBUG,
				Optional.of("SecurityPolicy1.yml")))
						.isEqualTo(HIGH + System.lineSeparator()
								+ "Allow it by granting the read permission on /etc/passwd in the security policy "
								+ "SecurityPolicy1.yml as:" + System.lineSeparator()
								+ SecurityViolationFeedbackTexts.lines("- onThisPathAndAllPathsBelow: \"/etc/passwd\"",
										"  readAllFiles: true", "  overwriteAllFiles: false", "  createAllFiles: false",
										"  executeAllFiles: false", "  deleteAllFiles: false"));
	}

	/** Without a file name the hint names the security policy alone. */
	@Test
	void debugWithoutAFileNameNamesThePolicyAlone() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(SecurityViolationFeedback.render(fileRead(), SecurityViolationFeedbackLevel.DEBUG))
				.contains("on /etc/passwd in the security policy as:");
	}

	/**
	 * An architecture finding, whose action at run time is not known, shows LOW's
	 * sentence at MEDIUM and only the category at DEBUG, in both languages.
	 */
	@Test
	void anArchitectureFindingShowsLowAtMediumAndTheCategoryAtDebug() {
		SecurityViolationFeedback.Violation finding = new SecurityViolationFeedback.Violation("architecture",
				"file.system.access", "java.io.File", HIGH);
		for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
			Locale.setDefault(locale);
			assertThat(SecurityViolationFeedback.render(finding, SecurityViolationFeedbackLevel.MEDIUM))
					.isEqualTo(SecurityViolationFeedbackTexts.low());
			assertThat(SecurityViolationFeedback.render(finding, SecurityViolationFeedbackLevel.DEBUG,
					Optional.of("Policy.yaml")))
							.isEqualTo(SecurityViolationFeedbackTexts.debugCategory(HIGH,
									"regardingFileSystemInteractions", Optional.of("Policy.yaml")));
		}
	}

	/** A forbidden import names the package, and only that package. */
	@Test
	void aForbiddenImportGetsAnExactPackageEntry() {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation finding = new SecurityViolationFeedback.Violation("architecture", "import",
				"java.net", HIGH);
		assertThat(SecurityViolationFeedback.render(finding, SecurityViolationFeedbackLevel.DEBUG))
				.isEqualTo(SecurityViolationFeedbackTexts.debug(HIGH, "import", "java.net", Optional.empty(),
						SecurityViolationFeedbackTexts.lines("- importTheFollowingPackage: \"java.net\"",
								"  exactMatchOnly: true")));
		assertThat(SecurityViolationFeedback.render(finding, SecurityViolationFeedbackLevel.MEDIUM))
				.isEqualTo(SecurityViolationFeedbackTexts.low());
	}

	/**
	 * A command with arguments, whose boundaries the target text no longer shows,
	 * and any target holding the policy's wildcard get the category only, so the
	 * hint never suggests an entry that allows a different action or every one.
	 *
	 * @param kind     the kind.
	 * @param action   the action.
	 * @param target   the target.
	 * @param category the category the hint names.
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = { "command|execute|echo hello world|regardingCommandExecutions",
			"command|execute|echo *|regardingCommandExecutions", "command|execute|*|regardingCommandExecutions",
			"file|read|*|regardingFileSystemInteractions", "network|connect|*:443|regardingNetworkConnections",
			"thread|create|*|regardingThreadCreations", "architecture|import|*|regardingPackageImports" })
	void anInexactEntryFallsBackToTheCategory(String kind, String action, String target, String category) {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation violation = new SecurityViolationFeedback.Violation(kind, action, target,
				HIGH);
		assertThat(SecurityViolationFeedback.entryOf(violation)).isEmpty();
		assertThat(SecurityViolationFeedback.render(violation, SecurityViolationFeedbackLevel.DEBUG))
				.isEqualTo(SecurityViolationFeedbackTexts.debugCategory(HIGH, category, Optional.empty()));
	}

	/**
	 * A fixed architecture rule, which no policy entry switches off, gets no hint
	 * at all: DEBUG shows the HIGH report alone.
	 *
	 * @param rule the rule key the finding carries.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "reflection.uses", "serialize", "terminate.jvm", "class.loading", "native.code.access",
			"agent.attach", "environment.access", "module.system.access", "jndi.injection", "reach" })
	void aFixedArchitectureRuleGetsNoHint(String rule) {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation finding = new SecurityViolationFeedback.Violation("architecture", rule,
				"java.lang.reflect.Method", HIGH);
		assertThat(SecurityViolationFeedback.render(finding, SecurityViolationFeedbackLevel.DEBUG)).isEqualTo(HIGH);
	}

	/**
	 * DEBUG keeps the blocked action's own stack, so the student's call chain is
	 * still there, while LOW shows none.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void debugKeepsTheBlockedActionsStack() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		StackTraceElement[] frames = { new StackTraceElement("org.example.Student", "readSecret", "Student.java", 7),
				new StackTraceElement("org.example.StudentTest", "readsTheSecret", "StudentTest.java", 12) };
		SecurityException violation = realViolation();
		violation.setStackTrace(frames);
		Throwable reported = ReportingUtils.processThrowable(violation, contextFor("debugPolicy"));
		assertThat(reported.getMessage()).startsWith(HIGH).contains("Allow it by granting the read permission");
		assertThat(reported.getStackTrace()).containsExactly(frames);
	}

	/** A thread manipulation no entry allows names the category only. */
	@Test
	void aThreadManipulationNamesTheCategoryOnly() {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation manipulation = new SecurityViolationFeedback.Violation("thread",
				"manipulate", "java.lang.Thread", HIGH);
		assertThat(SecurityViolationFeedback.render(manipulation, SecurityViolationFeedbackLevel.DEBUG)).isEqualTo(
				SecurityViolationFeedbackTexts.debugCategory(HIGH, "regardingThreadCreations", Optional.empty()));
		assertThat(SecurityViolationFeedback.render(manipulation, SecurityViolationFeedbackLevel.MEDIUM))
				.isEqualTo(SecurityViolationFeedbackTexts.low());
	}

	/**
	 * An argument Ares refused to inspect cannot be allowed, so DEBUG shows the
	 * report without a hint and LOW shows only the fixed text.
	 */
	@Test
	void anUninspectableArgumentGetsNoHint() {
		Locale.setDefault(Locale.ENGLISH);
		SecurityViolationFeedback.Violation untrusted = new SecurityViolationFeedback.Violation("uninspectable",
				"inspect", "org.example.StudentFile", HIGH);
		assertThat(SecurityViolationFeedback.render(untrusted, SecurityViolationFeedbackLevel.DEBUG)).isEqualTo(HIGH);
		assertThat(SecurityViolationFeedback.render(untrusted, SecurityViolationFeedbackLevel.LOW))
				.doesNotContain("org.example.StudentFile").isEqualTo(LOW_TEXT);
		assertThat(SecurityViolationFeedback.render(untrusted, SecurityViolationFeedbackLevel.MEDIUM))
				.doesNotContain("org.example.StudentFile").isEqualTo(LOW_TEXT);
	}

	/**
	 * The runtime-type guard of each backend throws a real violation, which the
	 * report recognises, while the same text thrown by a student is not one.
	 *
	 * @throws ReflectiveOperationException if a guard cannot be reached
	 */
	@Test
	void theRuntimeTypeGuardsThrowRealViolations() throws ReflectiveOperationException {
		Object studentValue = new SelfCausingException();
		for (Class<?> owner : List.of(Class.forName(
				"de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox"),
				Class.forName(
						"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJAbstractAdviceDefinitions"))) {
			Method guard = owner.getDeclaredMethod("requireTrustedRuntimeType", Object.class);
			guard.setAccessible(true);
			Throwable thrown = invocationFailure(guard, studentValue);
			assertThat(SecurityViolationFeedback.violationOf(thrown)).get()
					.extracting(SecurityViolationFeedback.Violation::kind).isEqualTo("uninspectable");
		}
		assertThat(SecurityViolationFeedback.violationOf(new SecurityException(HIGH))).isEmpty();
	}

	/**
	 * Calls a static guard and returns what it threw.
	 *
	 * @param guard the guard.
	 * @param value the value it checks.
	 * @return the thrown failure.
	 * @throws IllegalAccessException if the guard cannot be called
	 */
	private static Throwable invocationFailure(Method guard, Object value) throws IllegalAccessException {
		try {
			guard.invoke(null, value);
		} catch (java.lang.reflect.InvocationTargetException thrown) {
			return thrown.getCause();
		}
		throw new AssertionError("the guard accepted a student type: " + guard.getDeclaringClass());
	}

	/**
	 * Every kind maps to its policy category.
	 *
	 * @param kind     the kind.
	 * @param action   the action.
	 * @param category the expected category.
	 */
	@ParameterizedTest
	@CsvSource({ "file, delete, regardingFileSystemInteractions", "network, connect, regardingNetworkConnections",
			"command, execute, regardingCommandExecutions", "thread, create, regardingThreadCreations",
			"architecture, file.system.access, regardingFileSystemInteractions",
			"architecture, network.access, regardingNetworkConnections",
			"architecture, execute.command, regardingCommandExecutions",
			"architecture, manipulate.threads, regardingThreadCreations",
			"architecture, access the file system, theFollowingResourceAccessesArePermitted",
			"architecture, import, regardingPackageImports",
			"architecture, reach, theFollowingResourceAccessesArePermitted" })
	void everyKindNamesItsCategory(String kind, String action, String category) {
		assertThat(SecurityViolationFeedback
				.categoryOf(new SecurityViolationFeedback.Violation(kind, action, "target", HIGH))).isEqualTo(category);
	}

	/** An architecture-stage action gets the category only, not a made-up entry. */
	@Test
	void anArchitectureHintNamesNoEntry() {
		assertThat(SecurityViolationFeedback
				.entryOf(new SecurityViolationFeedback.Violation("architecture", "file.system.access", "x", HIGH)))
						.isEmpty();
	}

	/** Without a policy the level is HIGH. */
	@Test
	void withoutAPolicyTheLevelIsFull() throws NoSuchMethodException {
		assertThat(ConfigurationUtils.getSecurityViolationFeedbackLevel(contextFor("noPolicy")))
				.isEqualTo(SecurityViolationFeedbackLevel.HIGH);
	}

	/** A plain student exception with Ares text keeps its ordinary path. */
	@Test
	void aForgedViolationIsReportedAsAnOrdinaryFailure() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new SecurityException(HIGH), contextFor("noPolicy"));
		assertThat(reported).hasMessage(HIGH);
	}

	/**
	 * The DEBUG file entry is one the policy reader accepts as written, and a
	 * Windows path with a quote reads back unchanged.
	 *
	 * @param tempDir where the policy is written.
	 * @throws IOException if the policy cannot be written
	 */
	@Test
	void theDebugFileEntryIsReadBackByThePolicyReader(@TempDir Path tempDir) throws IOException {
		String target = "C:\\temp\\new \"draft\".txt";
		FilePermission read = readBack(tempDir, "regardingFileSystemInteractions",
				new SecurityViolationFeedback.Violation("file", "create", target, HIGH))
						.regardingFileSystemInteractions().get(0);
		assertThat(read.onThisPathAndAllPathsBelow()).isEqualTo(target);
		assertThat(read.createAllFiles()).isTrue();
		assertThat(read.readAllFiles() || read.overwriteAllFiles() || read.executeAllFiles() || read.deleteAllFiles())
				.isFalse();
	}

	/**
	 * The DEBUG network, command and thread entries read back and grant exactly the
	 * blocked action.
	 *
	 * @param tempDir where the policies are written.
	 * @throws IOException if a policy cannot be written
	 */
	@Test
	void theOtherDebugEntriesAreReadBackByThePolicyReader(@TempDir Path tempDir) throws IOException {
		var network = readBack(tempDir, "regardingNetworkConnections",
				new SecurityViolationFeedback.Violation("network", "send", "example.org:443", HIGH))
						.regardingNetworkConnections().get(0);
		assertThat(network.onTheHost()).isEqualTo("example.org");
		assertThat(network.onThePort()).isEqualTo(443);
		assertThat(network.sendData()).isTrue();
		assertThat(network.openConnections() || network.receiveData()).isFalse();
		var command = readBack(tempDir, "regardingCommandExecutions",
				new SecurityViolationFeedback.Violation("command", "execute", "ls", HIGH)).regardingCommandExecutions()
						.get(0);
		assertThat(command.executeTheCommand()).isEqualTo("ls");
		assertThat(command.withTheseArguments()).isEmpty();
		var thread = readBack(tempDir, "regardingThreadCreations",
				new SecurityViolationFeedback.Violation("thread", "create", "java.lang.Thread", HIGH))
						.regardingThreadCreations().get(0);
		assertThat(thread.createTheFollowingNumberOfThreads()).isEqualTo(1);
		assertThat(thread.ofThisClass()).isEqualTo("java.lang.Thread");
	}

	/**
	 * The DEBUG import entry reads back, allows the blocked package and still
	 * refuses a package below it.
	 *
	 * @param tempDir where the policy is written.
	 * @throws Exception if the policy cannot be written or the check not reached
	 */
	@Test
	void theDebugImportEntryAllowsOnlyThatPackage(@TempDir Path tempDir) throws Exception {
		var permission = readBack(tempDir, "regardingPackageImports",
				new SecurityViolationFeedback.Violation("architecture", "import", "java.net", HIGH))
						.regardingPackageImports().get(0);
		assertThat(permission.importTheFollowingPackage()).isEqualTo("java.net");
		assertThat(permission.exactMatchOnly()).isTrue();
		Method permits = Class
				.forName("de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection")
				.getDeclaredMethod("permits", permission.getClass(), String.class);
		permits.setAccessible(true);
		assertThat(permits.invoke(null, permission, "java.net")).isEqualTo(true);
		assertThat(permits.invoke(null, permission, "java.net.http")).isEqualTo(false);
	}

	/**
	 * Writes the DEBUG entry of a violation into a policy and reads it back.
	 *
	 * @param tempDir   where the policy is written.
	 * @param category  the category the entry goes under.
	 * @param violation the violation.
	 * @return the resource accesses read back.
	 * @throws IOException if the policy cannot be written
	 */
	private static de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses readBack(Path tempDir,
			String category, SecurityViolationFeedback.Violation violation) throws IOException {
		String entry = SecurityViolationFeedback.entryOf(violation).orElseThrow().replace(System.lineSeparator(),
				"\n      ");
		StringBuilder categories = new StringBuilder();
		for (String each : List.of("regardingFileSystemInteractions", "regardingNetworkConnections",
				"regardingCommandExecutions", "regardingThreadCreations", "regardingPackageImports",
				"regardingTimeouts")) {
			categories.append("    ").append(each)
					.append(each.equals(category) ? ":\n      " + entry + "\n" : ": []\n");
		}
		Path policy = tempDir.resolve(category + ".yaml");
		Files.writeString(policy, """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				""" + categories);
		return new SecurityPolicyYAMLReader(new YAMLMapper(), tempDir).readSecurityPolicyFrom(policy)
				.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
	}

	/**
	 * A blocked action carried as a suppressed failure, inside a privileged
	 * exception, inside a multiple-failures error, as the cause of a member of an
	 * AssertJ multiple-assertions error or of JUnit 4's array comparison failure is
	 * shortened at LOW.
	 *
	 * @param carrier how the blocked action is carried.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(strings = { "suppressed", "privileged", "multiple", "assertj", "array" })
	void aCarriedViolationIsShortened(String carrier) throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		SecurityException violation = realViolation();
		Throwable failure = switch (carrier) {
		case "suppressed" -> {
			IllegalStateException outer = new IllegalStateException("closing failed");
			outer.addSuppressed(violation);
			yield outer;
		}
		case "privileged" -> new PrivilegedException(violation);
		case "assertj" -> new MultipleAssertionsError(List.of(new AssertionError(violation)));
		case "array" -> new org.junit.internal.ArrayComparisonFailure("array", new AssertionError(violation), 0);
		default -> new MultipleFailuresError("two failures", List.of(new AssertionError("first"), violation));
		};
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("lowPolicy"));
		assertThat(reported).hasMessage(LOW_TEXT).hasNoSuppressedExceptions();
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * With only privileged failures reported, a student exception whose own
	 * getCause throws gets the policy's message, and its getCause never runs.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aStudentGetCauseIsNeverCalled() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new SelfCausingException(),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
		assertThat(SelfCausingException.asked).isFalse();
	}

	/**
	 * A student exception whose own getCause hands back a real blocked action is
	 * not shown in full at LOW: the search skips that getter, so the report says
	 * only that the failure could not be read completely.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aWrapperWithItsOwnGetCauseIsRedactedAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT);
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * Without a feedback level below HIGH, the same wrapper is reported as before.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aWrapperWithItsOwnGetCauseKeepsItsReportAtHigh() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("noPolicy"));
		assertThat(reported).hasMessageNotContaining(UNINSPECTABLE_TEXT);
	}

	/**
	 * At HIGH with only privileged failures reported, a blocked action suppressed
	 * on a student exception whose own getCause never returns is reported by
	 * itself, and that getCause is never called.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aViolationOnAWrapperWithItsOwnGetCauseIsReportedByItselfAtHigh() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		HangingCauseException wrapper = new HangingCauseException();
		wrapper.addSuppressed(realViolation());
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(wrapper, contextFor("privilegedHighPolicy")));
			assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(HIGH);
			assertThat(reported.getStackTrace()).isNotEmpty();
			assertThat(HangingCauseException.asked).isFalse();
		} finally {
			HangingCauseException.asked = false;
		}
	}

	/**
	 * At LOW with only privileged failures reported, a privileged exception
	 * carrying a student exception whose own getCause hands back a blocked action
	 * is not shown in full.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aPrivilegedCarrierWithItsOwnGetCauseIsRedactedAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new PrivilegedException(new WrappingException(realViolation())), contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT);
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * With only privileged failures reported, an ordinary student wrapper whose own
	 * getCause hands back a blocked action still gets the policy's message.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void anOrdinaryWrapperWithItsOwnGetCauseGetsThePolicyMessage() throws ReflectiveOperationException {
		Throwable reported = ReportingUtils.processThrowable(new WrappingException(realViolation()),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
	}

	/**
	 * A blocked action behind more failures than the search reads is not shown in
	 * full: the report says only that the failure could not be read completely.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aViolationPastTheSearchLimitIsRedacted() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		RuntimeException failure = new RuntimeException("many failures");
		for (int index = 0; index < 256; index++) {
			failure.addSuppressed(new IllegalStateException("harmless " + index));
		}
		failure.addSuppressed(realViolation());
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("lowPolicy"));
		assertThat(reported).hasMessage(UNINSPECTABLE_TEXT).hasNoSuppressedExceptions();
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * A failure whose reading ignores the interruption after the bound, a
	 * getMessage that loops for ever or a lock student code keeps, ends the test
	 * run with the strict timeout's exit code instead of running on into later
	 * tests.
	 *
	 * @param kind what keeps the reading from ending, as the probe names it.
	 * @throws Exception if the child JVM cannot be started
	 */
	@ParameterizedTest
	@ValueSource(strings = { "message", "lock" })
	void aReadingThatIgnoresTheInterruptionEndsTheRun(String kind) throws Exception {
		String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")); //$NON-NLS-1$ //$NON-NLS-2$
		Process process = new ProcessBuilder(javaExecutable, "-cp", classPath, //$NON-NLS-1$
				ReportingUtilsForkProbe.class.getName(), kind).redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(20, TimeUnit.SECONDS)).isTrue();
			assertThat(process.exitValue()).isEqualTo(124);
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * At LOW, a student exception whose own getMessage adds a blocked action it
	 * caught is redacted, so reading the message for the report cannot reveal that
	 * blocked action.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void anOwnMessageAddingAViolationIsRedactedAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new MessageAddingException(realViolation()),
				contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT)
				.hasNoSuppressedExceptions();
		assertThat(reported.getStackTrace()).isEmpty();
	}

	/**
	 * At LOW, a student exception that overrides any other method the report reads,
	 * and adds a blocked action it caught from there, is redacted too.
	 *
	 * @param method the method the student exception overrides.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(strings = { "getStackTrace", "toString", "getLocalizedMessage" })
	void anyOverriddenReadMethodAddingAViolationIsRedactedAtLow(String method) throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(AddingException.overriding(method, realViolation()),
				contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT)
				.hasNoSuppressedExceptions();
	}

	/**
	 * At LOW, a student exception that overrides only a callback an inherited
	 * runtime method calls, here the reason of a file-system failure, and adds a
	 * blocked action from there, is redacted.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void anOverriddenRuntimeCallbackAddingAViolationIsRedactedAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(new ReasonAddingException(realViolation()),
				contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT)
				.hasNoSuppressedExceptions();
	}

	/**
	 * At LOW, an AssertJ multiple-assertions error whose member list is student
	 * code, directly or wrapped in a runtime list, is redacted, since reading the
	 * list could add a blocked action to the error.
	 *
	 * @param wrapped whether the student list is wrapped in an unmodifiable list.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aStudentMemberListIsRedactedAtLow(boolean wrapped) throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		AddingList<AssertionError> members = new AddingList<>(realViolation());
		members.add(new AssertionError("soft"));
		MultipleAssertionsError failure = new MultipleAssertionsError(
				wrapped ? java.util.Collections.unmodifiableList(members) : members);
		members.addTo(failure);
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT)
				.hasNoSuppressedExceptions();
	}

	/**
	 * At LOW, an AssertJ soft-assertion error whose error list is student code is
	 * redacted, since copying the list could add a blocked action to it.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aStudentSoftAssertionListIsRedactedAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		AddingList<String> errors = new AddingList<>(realViolation());
		errors.add("soft");
		SoftAssertionError failure = new SoftAssertionError(errors);
		errors.addTo(failure);
		Throwable reported = ReportingUtils.processThrowable(failure, contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT)
				.hasNoSuppressedExceptions();
	}

	/**
	 * At LOW, an AssertJ soft-assertion error with a plain list keeps its report.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aSoftAssertionErrorWithAPlainListKeepsItsReportAtLow() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new SoftAssertionError(new java.util.ArrayList<>(List.of("soft"))), contextFor("lowPolicy"));
		assertThat(reported).isInstanceOf(SoftAssertionError.class).hasMessageContaining("soft");
	}

	/**
	 * At LOW, an AssertJ multiple-failures error carrying a member of a student
	 * class is redacted, since formatting its message calls that class's
	 * constructor.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anAssertJErrorWithAStudentMemberIsRedactedAtLow() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new AssertJMultipleFailuresError("soft", List.of(new StudentAssertion("first"))),
				contextFor("lowPolicy"));
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(UNINSPECTABLE_TEXT);
	}

	/**
	 * A thread-local value whose copy for a new thread never returns does not stop
	 * the report, since the report's worker does not inherit thread-local values.
	 *
	 * @throws Exception if the thread is interrupted or the context cannot be made
	 */
	@Test
	void aBlockingThreadLocalCopyDoesNotStopTheReport() throws Exception {
		TestContext context = contextFor("lowPolicy");
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.atomic.AtomicReference<Throwable> reported = new java.util.concurrent.atomic.AtomicReference<>();
		Thread caller = new Thread(null, () -> {
			new BlockingCopy(release).set("student value");
			reported.set(ReportingUtils.processThrowable(new IllegalStateException("plain failure"), context));
		}, "student-caller", 0, false);
		caller.setDaemon(true);
		caller.start();
		caller.join(TimeUnit.SECONDS.toMillis(10));
		release.countDown();
		assertThat(reported.get()).isInstanceOf(IllegalStateException.class).hasMessage("plain failure");
	}

	/**
	 * At LOW, an ordinary failure, an opentest4j assertion failure and the
	 * multiple-failure errors of opentest4j and AssertJ keep their report, since
	 * the methods they override are not student code.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */

	@Test
	void failuresWithoutAStudentMessageKeepTheirReportAtLow() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("lowPolicy");
		assertThat(ReportingUtils.processThrowable(new IllegalStateException("plain failure"), context))
				.isInstanceOf(IllegalStateException.class).hasMessage("plain failure");
		assertThat(ReportingUtils.processThrowable(
				new MultipleFailuresError("two failures", List.of(new AssertionError("first"))), context))
						.hasMessageNotContaining(UNINSPECTABLE_TEXT);
		assertThat(ReportingUtils.processThrowable(new MultipleAssertionsError(List.of(new AssertionError("soft"))),
				context)).hasMessageNotContaining(UNINSPECTABLE_TEXT);
		assertThat(ReportingUtils.processThrowable(
				new MultipleAssertionsError(new java.util.ArrayList<>(List.of(new AssertionError("soft")))), context))
						.hasMessageNotContaining(UNINSPECTABLE_TEXT);
		assertThat(
				ReportingUtils.processThrowable(new AssertionFailedError("expected: <1> but was: <2>", 1, 2), context))
						.hasMessage("expected: <1> but was: <2>");
		assertThat(ReportingUtils.processThrowable(
				new AssertJMultipleFailuresError("soft", List.of(new AssertionError("first"))), context))
						.hasMessageNotContaining(UNINSPECTABLE_TEXT);
	}

	/**
	 * At LOW, JUnit 4's comparison failure keeps its expected and actual values,
	 * since Ares copies it as a known safe type, while a student subclass whose own
	 * message adds a blocked action is still redacted.
	 *
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@Test
	void aJUnit4ComparisonKeepsItsValuesAtLow() throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("lowPolicy");
		AssertionError comparison = assertThrows(AssertionError.class, () -> org.junit.Assert.assertEquals("a", "b"));
		assertThat(ReportingUtils.processThrowable(comparison, context)).isInstanceOf(org.junit.ComparisonFailure.class)
				.hasMessageContaining("expected:<[a]>").hasMessageContaining("but was:<[b]>");
		AssertionError arrays = assertThrows(AssertionError.class,
				() -> org.junit.Assert.assertArrayEquals(new int[] { 1 }, new int[] { 2 }));
		assertThat(ReportingUtils.processThrowable(arrays, context)).hasMessageContaining("arrays first differed");
		assertThat(ReportingUtils.processThrowable(new StudentComparisonFailure(realViolation()), context))
				.hasMessage(UNINSPECTABLE_TEXT).hasNoSuppressedExceptions();
	}

	/**
	 * A privileged failure of a setup method keeps its explanation, as it does in
	 * the test itself, whichever failures the policy reports.
	 *
	 * @param policy the method carrying the policy.
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@ParameterizedTest
	@ValueSource(strings = { "privilegedOnlyPolicy", "privilegedHighPolicy", "lowPolicy", "noPolicy" })
	void aPrivilegedLifecycleFailureKeepsItsExplanation(String policy) throws NoSuchMethodException {
		PrivilegedException failure = assertThrows(PrivilegedException.class,
				() -> TestUtils.privilegedFail("explanation"));
		assertThat(ReportingUtils.processLifecycleFailure(failure, contextFor(policy)))
				.isNotInstanceOf(PrivilegedException.class).hasMessage("explanation");
	}

	/**
	 * A student security exception whose own getMessage runs until interrupted
	 * cannot stop the report through the check for Ares's own setup failures, which
	 * runs before the bound.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aLoopingStudentSecurityExceptionCannotStopTheReport() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("lowPolicy");
		LoopingSecurityException failure = new LoopingSecurityException();
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(failure, context));
			assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
		} finally {
			failure.looping = false;
		}
	}

	/**
	 * A failure whose own getMessage runs until interrupted, carrying a suppressed
	 * blocked action, cannot stop the report at HIGH: after the bound, the blocked
	 * action is reported by itself, also with only privileged failures reported.
	 *
	 * @param policy the method carrying the policy.
	 * @throws ReflectiveOperationException if the violation cannot be created
	 */
	@ParameterizedTest
	@ValueSource(strings = { "noPolicy", "privilegedHighPolicy" })
	void aLoopingMessageOnAViolationCarrierReportsTheViolationAtHigh(String policy)
			throws ReflectiveOperationException {
		Locale.setDefault(Locale.ENGLISH);
		LoopingMessageException wrapper = new LoopingMessageException();
		wrapper.addSuppressed(realViolation());
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(wrapper, contextFor(policy)));
			assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(HIGH)
					.hasNoSuppressedExceptions();
			assertThat(reported.getStackTrace()).isNotEmpty();
		} finally {
			wrapper.looping = false;
		}
	}

	/**
	 * Without a blocked action in it, a failure whose own getMessage runs until
	 * interrupted is reported at HIGH as a failure that could not be read.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void aLoopingMessageWithoutAViolationIsRedactedAtHigh() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		TestContext context = contextFor("noPolicy");
		LoopingMessageException failure = new LoopingMessageException();
		try {
			Throwable reported = assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> ReportingUtils.processThrowable(failure, context));
			assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
		} finally {
			failure.looping = false;
		}
	}

	/**
	 * An ordinary failure at HIGH keeps its report.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anOrdinaryFailureKeepsItsReportAtHigh() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(new IllegalStateException("plain failure"),
				contextFor("noPolicy"));
		assertThat(reported).isInstanceOf(IllegalStateException.class).hasMessage("plain failure");
	}

	/**
	 * With only privileged failures reported, an ordinary failure the search could
	 * not finish still gets the policy's message, which reveals nothing of it.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anIncompleteSearchOfAnOrdinaryFailureGetsThePolicyMessage() throws NoSuchMethodException {
		Throwable reported = ReportingUtils.processThrowable(withMoreFailuresThanTheSearchReads(),
				contextFor("privilegedOnlyPolicy"));
		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage("Policy message");
	}

	/**
	 * With only privileged failures reported, a privileged failure the search could
	 * not finish is still redacted, since its own report would be shown.
	 *
	 * @throws NoSuchMethodException if the fixture method is missing
	 */
	@Test
	void anIncompleteSearchOfAPrivilegedFailureIsRedacted() throws NoSuchMethodException {
		Locale.setDefault(Locale.ENGLISH);
		Throwable reported = ReportingUtils.processThrowable(
				new PrivilegedException(withMoreFailuresThanTheSearchReads()), contextFor("privilegedOnlyPolicy"));
		assertThat(reported).hasMessage(UNINSPECTABLE_TEXT);
	}

	/**
	 * An ordinary failure carrying more harmless failures than the search reads.
	 *
	 * @return the failure.
	 */
	private static RuntimeException withMoreFailuresThanTheSearchReads() {
		RuntimeException failure = new RuntimeException("many failures");
		for (int index = 0; index <= 256; index++) {
			failure.addSuppressed(new IllegalStateException("harmless " + index));
		}
		return failure;
	}

	/**
	 * Creates a violation the way Ares's checks do.
	 *
	 * @return the violation.
	 * @throws ReflectiveOperationException if it cannot be created
	 */
	private static SecurityException realViolation() throws ReflectiveOperationException {
		java.lang.reflect.Constructor<AresSecurityViolation> constructor = AresSecurityViolation.class
				.getDeclaredConstructor(String.class, String.class, String.class, String.class);
		constructor.setAccessible(true);
		return constructor.newInstance("file", "read", "/etc/passwd", HIGH);
	}

	/** A student exception whose getCause never returns normally. */
	private static final class SelfCausingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether anything asked for the cause. */
		private static volatile boolean asked;

		/**
		 * Records the call and fails, as a hostile override could.
		 *
		 * @return never.
		 */
		@Override
		public synchronized Throwable getCause() {
			asked = true;
			throw new AssertionError("student getCause");
		}
	}

	/** A student exception whose own getCause would never return. */
	private static final class HangingCauseException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether anything asked for the cause. */
		private static volatile boolean asked;

		/**
		 * Records the call and then waits for ever, as a hostile override could.
		 *
		 * @return never.
		 */
		@Override
		public synchronized Throwable getCause() {
			asked = true;
			while (asked) {
				Thread.onSpinWait();
			}
			return null;
		}
	}

	/**
	 * A student exception that keeps the Java runtime's getCause but whose own
	 * getMessage runs until the test lets it end or its thread is interrupted.
	 */
	private static final class LoopingMessageException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether getMessage keeps looping; a test sets it to false to let it end. */
		private transient volatile boolean looping = true;

		/**
		 * Waits while looping and not interrupted, as a slow override could.
		 *
		 * @return a fixed text once it ends.
		 */
		@Override
		public String getMessage() {
			while (looping && !Thread.currentThread().isInterrupted()) {
				Thread.onSpinWait();
			}
			return "looped";
		}
	}

	/** A student security exception whose getMessage loops until interrupted. */
	private static final class LoopingSecurityException extends SecurityException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** Whether getMessage keeps looping; a test sets it to false to let it end. */
		private transient volatile boolean looping = true;

		/**
		 * Waits while looping and not interrupted, as a slow override could.
		 *
		 * @return a text naming Ares's setup failure once it ends.
		 */
		@Override
		public String getMessage() {
			while (looping && !Thread.currentThread().isInterrupted()) {
				Thread.onSpinWait();
			}
			return "Reason: Ares-Code JavaAOPTestCaseSettings";
		}
	}

	/**
	 * A student comparison failure whose message adds a blocked action it caught.
	 */
	private static final class StudentComparisonFailure extends org.junit.ComparisonFailure {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The blocked action this failure adds when its message is read. */
		private final transient Throwable caught;

		/**
		 * Keeps a blocked action without adding it yet.
		 *
		 * @param caught the blocked action to add later.
		 */
		StudentComparisonFailure(Throwable caught) {
			super("student", "a", "b");
			this.caught = caught;
		}

		/**
		 * Adds the blocked action, then returns JUnit's message.
		 *
		 * @return JUnit's message.
		 */
		@Override
		public String getMessage() {
			if (getSuppressed().length == 0) {
				addSuppressed(caught);
			}
			return super.getMessage();
		}
	}

	/** A student exception whose own getMessage adds a blocked action it caught. */
	private static final class MessageAddingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The blocked action this exception adds when its message is read. */
		private final transient Throwable caught;

		/**
		 * Keeps a blocked action without adding it yet.
		 *
		 * @param caught the blocked action to add later.
		 */
		MessageAddingException(Throwable caught) {
			this.caught = caught;
		}

		/**
		 * Adds the blocked action, as a hostile override could.
		 *
		 * @return a harmless text.
		 */
		@Override
		public String getMessage() {
			addSuppressed(caught);
			return "student message";
		}
	}

	/**
	 * A student exception that keeps a blocked action it caught, for a subclass to
	 * add from the one method it overrides.
	 */
	private abstract static class AddingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The blocked action to add. */
		private final transient Throwable caught;

		/**
		 * Keeps a blocked action without adding it yet.
		 *
		 * @param caught the blocked action to add later.
		 */
		AddingException(Throwable caught) {
			super("student failure");
			this.caught = caught;
		}

		/** Adds the blocked action, as a hostile override could. */
		final void addCaught() {
			if (caught != null && getSuppressed().length == 0) {
				addSuppressed(caught);
			}
		}

		/**
		 * Creates the exception overriding the named method.
		 *
		 * @param method the method to override.
		 * @param caught the blocked action to add from it.
		 * @return the exception.
		 */
		static AddingException overriding(String method, Throwable caught) {
			return switch (method) {
			case "getStackTrace" -> new StackAddingException(caught);
			case "toString" -> new TextAddingException(caught);
			default -> new LocalizedAddingException(caught);
			};
		}
	}

	/** Adds a caught blocked action from its own getStackTrace. */
	private static final class StackAddingException extends AddingException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Keeps a blocked action.
		 *
		 * @param caught the blocked action.
		 */
		StackAddingException(Throwable caught) {
			super(caught);
		}

		/**
		 * Adds the blocked action first.
		 *
		 * @return the stack trace.
		 */
		@Override
		public StackTraceElement[] getStackTrace() {
			addCaught();
			return super.getStackTrace();
		}
	}

	/** Adds a caught blocked action from its own toString. */
	private static final class TextAddingException extends AddingException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Keeps a blocked action.
		 *
		 * @param caught the blocked action.
		 */
		TextAddingException(Throwable caught) {
			super(caught);
		}

		/**
		 * Adds the blocked action first.
		 *
		 * @return the text.
		 */
		@Override
		public String toString() {
			addCaught();
			return super.toString();
		}
	}

	/** Adds a caught blocked action from its own getLocalizedMessage. */
	private static final class LocalizedAddingException extends AddingException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Keeps a blocked action.
		 *
		 * @param caught the blocked action.
		 */
		LocalizedAddingException(Throwable caught) {
			super(caught);
		}

		/**
		 * Adds the blocked action first.
		 *
		 * @return the message.
		 */
		@Override
		public String getLocalizedMessage() {
			addCaught();
			return super.getLocalizedMessage();
		}
	}

	/**
	 * A student file-system failure that adds a blocked action it caught from its
	 * reason, which the runtime's own message calls.
	 */
	private static final class ReasonAddingException extends java.nio.file.FileSystemException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The blocked action to add. */
		private final transient Throwable caught;

		/**
		 * Keeps a blocked action.
		 *
		 * @param caught the blocked action.
		 */
		ReasonAddingException(Throwable caught) {
			super("student.txt");
			this.caught = caught;
		}

		/**
		 * Adds the blocked action first.
		 *
		 * @return the reason.
		 */
		@Override
		public String getReason() {
			if (getSuppressed().length == 0) {
				addSuppressed(caught);
			}
			return "student reason";
		}
	}

	/**
	 * A student member list that adds a blocked action it caught to the error
	 * holding it when the list is copied.
	 */
	private static final class AddingList<E> extends java.util.ArrayList<E> {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The blocked action to add. */
		private final transient Throwable caught;

		/** The error to add it to, once known. */
		private transient Throwable holder;

		/**
		 * Keeps a blocked action.
		 *
		 * @param caught the blocked action.
		 */
		AddingList(Throwable caught) {
			this.caught = caught;
		}

		/**
		 * Names the error to add the blocked action to.
		 *
		 * @param holder the error holding this list.
		 */
		void addTo(Throwable holder) {
			this.holder = holder;
		}

		/**
		 * Adds the blocked action to the holding error first.
		 *
		 * @return the members.
		 */
		@Override
		public Object[] toArray() {
			if (holder != null && holder.getSuppressed().length == 0) {
				holder.addSuppressed(caught);
			}
			return super.toArray();
		}
	}

	/** A student assertion class with nothing overridden. */
	private static final class StudentAssertion extends AssertionError {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates the assertion.
		 *
		 * @param message its message.
		 */
		StudentAssertion(String message) {
			super(message);
		}
	}

	/**
	 * A student thread-local whose copy for a new thread waits until released.
	 */
	private static final class BlockingCopy extends InheritableThreadLocal<String> {

		/** Releases the waiting copy. */
		private final java.util.concurrent.CountDownLatch release;

		/**
		 * Creates the thread-local.
		 *
		 * @param release releases the waiting copy.
		 */
		BlockingCopy(java.util.concurrent.CountDownLatch release) {
			this.release = release;
		}

		/**
		 * Waits until released, as a hostile copy could wait forever.
		 *
		 * @param parentValue the value of the creating thread.
		 * @return that value.
		 */
		@Override
		protected String childValue(String parentValue) {
			try {
				release.await();
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
			return parentValue;
		}
	}

	/** A student exception whose own getCause returns a failure it was given. */
	private static final class WrappingException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/** The failure this exception claims as its cause. */
		private final transient Throwable wrapped;

		/**
		 * Wraps a failure without handing it to the superclass.
		 *
		 * @param wrapped the failure to return as the cause.
		 */
		WrappingException(Throwable wrapped) {
			super("wrapped");
			this.wrapped = wrapped;
		}

		/**
		 * Returns the wrapped failure, as an override student code writes could.
		 *
		 * @return the wrapped failure.
		 */
		@Override
		public synchronized Throwable getCause() {
			return wrapped;
		}
	}

	/** A file read as an Ares check reports it. */
	private static SecurityViolationFeedback.Violation fileRead() {
		return new SecurityViolationFeedback.Violation("file", "read", "/etc/passwd", HIGH);
	}

	/** A mocked public test context for a method of this class. */
	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = SecurityViolationFeedbackTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(SecurityViolationFeedbackTest.class));
		when(context.annotatedElement()).thenReturn(Optional.of(method));
		when(context.findTestType()).thenReturn(Optional.of(TestType.PUBLIC));
		return context;
	}

	/** Carries a policy at DEBUG; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelDEBUG.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void debugPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/** Carries a policy at LOW; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelLOW.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void lowPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/**
	 * Carries a privileged-only policy at LOW; used only through the mocked
	 * context.
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelPrivilegedLow.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void privilegedOnlyPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/**
	 * Carries a privileged-only policy at the default HIGH level; used only through
	 * the mocked context.
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPrivilegedExceptionUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	private static void privilegedHighPolicy() {
		// A policy only, no test behaviour of its own.
	}

	/** Carries no policy; used only through the mocked context. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@Policy(activated = false)
	private static void noPolicy() {
		// No policy, so no test behaviour.
	}
}
