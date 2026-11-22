package de.tum.cit.ase.ares.api.aop.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

import example.student.BaselineAdviceProbe;
import example.student.BaselineAdviceProbe.Call;
import example.student.DenialSentenceProbe;

/**
 * Checks the one denial sentence both runtime backends write for a blocked
 * file, command, network or thread action: the caller, the action, the target,
 * the call in Java source notation, then "but was blocked by Ares, because" and
 * the reason that fits the policy. Every expected text is rendered through
 * {@link Messages}, so the test holds in any language. Each case also checks
 * that no raw message key is left in the sentence.
 */
class DenialSentenceTest {

	/** The settings that hold one allow-list per file-system action. */
	private static final String[] FILE_RULES = { "pathsAllowedToBeRead", "pathsAllowedToBeOverwritten",
			"pathsAllowedToBeCreated", "pathsAllowedToBeExecuted", "pathsAllowedToBeDeleted" };

	/** The caller every file call is judged from. */
	private static final String FILE_CALLER = "example.student.BaselineAdviceProbe.check";

	/** The caller every command call is judged from. */
	private static final String COMMAND_CALLER = "example.student.DenialSentenceProbe.runtimeExec";

	/** The caller every network call is judged from. */
	private static final String NETWORK_CALLER = "example.student.DenialSentenceProbe.socketConnect";

	/** The caller every thread call is judged from, which also holds the lambda. */
	private static final String THREAD_CALLER = "example.student.DenialSentenceProbe.threadWithLambda";

	/** A placeholder no real text contains, marking where any text may stand. */
	private static final String ANY = "\0";

	/** Loads the message bundle before any check runs. */
	@BeforeAll
	static void warmLocalizationBundle() {
		Messages.localized("security.advice.illegal.file.execution", ANY, ANY, ANY, ANY, ANY);
	}

	/** Clears the policy values before each test. */
	@BeforeEach
	void clearBefore() {
		AOPMode.ASPECTJ.reset();
	}

	/** Clears the policy values after each test, even after a failure. */
	@AfterEach
	void clearAfter() {
		AOPMode.ASPECTJ.reset();
	}

	/**
	 * A read under a policy with no file rule at all says the policy has no rule
	 * for files.
	 *
	 * @param mode      the backend
	 * @param directory a fresh folder
	 * @throws IOException if the test file cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aFileReadWithoutAnyFileRuleNamesTheMissingFileRules(AOPMode mode, @TempDir Path directory) throws IOException {
		Path file = Files.writeString(directory.resolve("secret.txt"), "secret");
		configure(mode);
		String message = denial(() -> BaselineAdviceProbe.check(mode, "read", read(file)));
		assertSentence(message, "security.advice.illegal.file.execution", FILE_CALLER, "read",
				file.toAbsolutePath().toString(), "java.nio.file.Files.readAllBytes(java.nio.file.Path)",
				Messages.localized("security.advice.denial.reason.no.rule.file"));
	}

	/**
	 * A write under a policy that only allows reading names the missing overwrite
	 * rule, not a missing file rule, and writes the array parameter as Java source.
	 *
	 * @param mode      the backend
	 * @param directory a fresh folder
	 * @throws IOException if the test file cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aWriteUnderAReadOnlyRuleNamesTheMissingOverwriteRule(AOPMode mode, @TempDir Path directory)
			throws IOException {
		Path file = Files.writeString(directory.resolve("out.txt"), "old");
		configure(mode);
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { directory.toString() },
				"ARCHUNIT", mode.name());
		String message = denial(() -> BaselineAdviceProbe.check(mode, "overwrite", writeString(file)));
		String reason = Messages.localized("security.advice.denial.reason.no.action.rule.file",
				Messages.localized("security.advice.action.overwrite"));
		assertSentence(message, "security.advice.illegal.file.execution", FILE_CALLER, "overwrite",
				file.toAbsolutePath().toString(),
				"java.nio.file.Files.writeString(java.nio.file.Path, java.lang.CharSequence, java.nio.file.OpenOption[])",
				reason);
		assertThat(message).doesNotContain(Messages.localized("security.advice.denial.reason.no.rule.file"))
				.doesNotContain("[Ljava.");
	}

	/**
	 * A read outside the folder a read rule names says no rule allows this file.
	 *
	 * @param mode      the backend
	 * @param directory a fresh folder
	 * @throws IOException if the test file cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aFileReadOutsideEveryRuleNamesTheFile(AOPMode mode, @TempDir Path directory) throws IOException {
		Path file = Files.writeString(directory.resolve("secret.txt"), "secret");
		configure(mode);
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead",
				new String[] { directory.resolve("unrelated").toString() }, "ARCHUNIT", mode.name());
		String message = denial(() -> BaselineAdviceProbe.check(mode, "read", read(file)));
		assertSentence(message, "security.advice.illegal.file.execution", FILE_CALLER, "read",
				file.toAbsolutePath().toString(), "java.nio.file.Files.readAllBytes(java.nio.file.Path)",
				Messages.localized("security.advice.denial.reason.target.not.allowed.file"));
	}

	/**
	 * A read inside the folder a read rule names is still allowed.
	 *
	 * @param mode      the backend
	 * @param directory a fresh folder
	 * @throws IOException if the test file cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aFileReadInsideARuleIsStillAllowed(AOPMode mode, @TempDir Path directory) throws IOException {
		Path file = Files.writeString(directory.resolve("allowed.txt"), "allowed");
		configure(mode);
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { directory.toString() },
				"ARCHUNIT", mode.name());
		assertDoesNotThrow(() -> BaselineAdviceProbe.check(mode, "read", read(file)));
	}

	/**
	 * A command under a policy with no command rule is quoted, its array parameter
	 * is written as {@code String[]}, and the reason names the missing command
	 * rules.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aCommandWithoutAnyCommandRuleIsQuotedAndNamesTheMissingRules(AOPMode mode) {
		configure(mode);
		allowCommands(mode, new String[0], new String[0][]);
		String message = denial(() -> DenialSentenceProbe.runtimeExec(mode, "echo", "hi"));
		assertSentence(message, "security.advice.illegal.command.execution", COMMAND_CALLER, "execute", "\"echo hi\"",
				"java.lang.Runtime.exec(java.lang.String[])",
				Messages.localized("security.advice.denial.reason.no.rule.command"));
	}

	/**
	 * A command that no command rule names says no rule allows this command.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aCommandOutsideEveryRuleNamesTheCommand(AOPMode mode) {
		configure(mode);
		allowCommands(mode, new String[] { "ls" }, new String[][] { { "*" } });
		String message = denial(() -> DenialSentenceProbe.runtimeExec(mode, "echo", "hi"));
		assertSentence(message, "security.advice.illegal.command.execution", COMMAND_CALLER, "execute", "\"echo hi\"",
				"java.lang.Runtime.exec(java.lang.String[])",
				Messages.localized("security.advice.denial.reason.target.not.allowed.command"));
	}

	/**
	 * An allowed command whose executable is not on the execute list says exactly
	 * that, naming the executable as a file.
	 *
	 * @param mode      the backend
	 * @param directory a folder the execute list names, which holds no executable
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void anAllowedCommandWithAnUnlistedExecutableNamesTheExecuteList(AOPMode mode, @TempDir Path directory) {
		configure(mode);
		allowCommands(mode, new String[] { "ares-missing-command" }, new String[][] { { "*" } });
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeExecuted", new String[] { directory.toString() },
				"ARCHUNIT", mode.name());
		String message = denial(() -> DenialSentenceProbe.runtimeExec(mode, "ares-missing-command", "hi"));
		assertSentence(message, "security.advice.illegal.file.execution", COMMAND_CALLER, "execute",
				"ares-missing-command", "java.lang.Runtime.exec(java.lang.String[])",
				Messages.localized("security.advice.denial.reason.command.executable.path.not.allowed"));
	}

	/**
	 * An allowed command still runs.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void anAllowedCommandIsStillAllowed(AOPMode mode) {
		configure(mode);
		allowCommands(mode, new String[] { "echo" }, new String[][] { { "*" } });
		assertDoesNotThrow(() -> DenialSentenceProbe.runtimeExec(mode, "echo", "hi"));
	}

	/**
	 * A connection under a policy with no network rule names the missing network
	 * rules, and the signature is spaced as in Java source.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aConnectionWithoutAnyNetworkRuleNamesTheMissingRules(AOPMode mode) {
		configure(mode);
		String message = denial(() -> DenialSentenceProbe.socketConnect(mode, "student.example.com", 9));
		assertSentence(message, "security.advice.illegal.network.execution", NETWORK_CALLER, "connect",
				"student.example.com:9", "java.net.Socket.<init>(java.lang.String, int)",
				Messages.localized("security.advice.denial.reason.no.rule.network"));
	}

	/**
	 * A connection under a policy that only allows sending names the missing
	 * connect rule.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aConnectionUnderASendOnlyRuleNamesTheMissingConnectRule(AOPMode mode) {
		configure(mode);
		JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeSentTo", new String[] { "student.example.com" },
				"ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeSentTo", new int[] { 9 }, "ARCHUNIT", mode.name());
		String message = denial(() -> DenialSentenceProbe.socketConnect(mode, "student.example.com", 9));
		String reason = Messages.localized("security.advice.denial.reason.no.action.rule.network",
				Messages.localized("security.advice.action.connect"));
		assertSentence(message, "security.advice.illegal.network.execution", NETWORK_CALLER, "connect",
				"student.example.com:9", "java.net.Socket.<init>(java.lang.String, int)", reason);
	}

	/**
	 * A connection to an address no connect rule names says no rule allows this
	 * address.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aConnectionOutsideEveryRuleNamesTheAddress(AOPMode mode) {
		configure(mode);
		allowConnection(mode, "other.example.com", 9);
		String message = denial(() -> DenialSentenceProbe.socketConnect(mode, "student.example.com", 9));
		assertSentence(message, "security.advice.illegal.network.execution", NETWORK_CALLER, "connect",
				"student.example.com:9", "java.net.Socket.<init>(java.lang.String, int)",
				Messages.localized("security.advice.denial.reason.target.not.allowed.network"));
	}

	/**
	 * An allowed connection still passes.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void anAllowedConnectionIsStillAllowed(AOPMode mode) {
		configure(mode);
		allowConnection(mode, "student.example.com", 9);
		assertDoesNotThrow(() -> DenialSentenceProbe.socketConnect(mode, "student.example.com", 9));
	}

	/**
	 * A thread running a lambda, under a policy with no thread rule, names the
	 * lambda by the method it is written in and names the missing thread rules.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aLambdaThreadWithoutAnyThreadRuleIsNamedAsALambda(AOPMode mode) {
		configure(mode);
		String message = denial(() -> DenialSentenceProbe.threadWithLambda(mode));
		assertSentence(message, "security.advice.illegal.thread.execution", THREAD_CALLER, "create",
				Messages.localized("security.advice.thread.lambda", THREAD_CALLER),
				"java.lang.Thread.<init>(java.lang.Runnable)",
				Messages.localized("security.advice.denial.reason.no.rule.thread"));
		assertThat(message).doesNotContain("Lambda-Expression");
	}

	/**
	 * A thread no thread rule names says no rule allows this thread.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void aThreadOutsideEveryRuleNamesTheThread(AOPMode mode) {
		configure(mode);
		allowThreads(mode, "java.lang.String");
		String message = denial(() -> DenialSentenceProbe.threadWithLambda(mode));
		assertSentence(message, "security.advice.illegal.thread.execution", THREAD_CALLER, "create",
				Messages.localized("security.advice.thread.lambda", THREAD_CALLER),
				"java.lang.Thread.<init>(java.lang.Runnable)",
				Messages.localized("security.advice.denial.reason.target.not.allowed.thread"));
	}

	/**
	 * An allowed lambda thread still passes.
	 *
	 * @param mode the backend
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void anAllowedLambdaThreadIsStillAllowed(AOPMode mode) {
		configure(mode);
		allowThreads(mode, "Lambda-Expression");
		assertDoesNotThrow(() -> DenialSentenceProbe.threadWithLambda(mode));
	}

	/**
	 * Every reason key the advice builds at run time from a resource type exists in
	 * both languages, since a missing one would only show as a raw key in a denial.
	 *
	 * @param locale the language to check
	 */
	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "en", "de" })
	void everyReasonKeyBuiltAtRunTimeExists(String locale) {
		List<String> keys = new ArrayList<>();
		for (String resourceType : List.of("file", "command", "network", "thread")) {
			keys.add("security.advice.denial.reason.no.rule." + resourceType);
			keys.add("security.advice.denial.reason.target.not.allowed." + resourceType);
		}
		keys.add("security.advice.denial.reason.no.action.rule.file");
		keys.add("security.advice.denial.reason.no.action.rule.network");
		keys.add("security.advice.denial.reason.command.executable.path.not.allowed");
		keys.add("security.advice.thread.lambda");
		for (String key : keys) {
			String rendered = AdviceActionWording.inLocale(Locale.forLanguageTag(locale),
					() -> Messages.localized(key, "X"));
			assertThat(rendered).as(key).doesNotContain(key).doesNotStartWith("!");
		}
	}

	/**
	 * Requires the message to be exactly the denial sentence of the given template,
	 * where only the text after the signature, the caller note, may vary. It also
	 * requires that no raw key and no old {@code |} reason splice is left in it.
	 *
	 * @param message   the message of the denial
	 * @param key       the template's key
	 * @param caller    the student method that made the call
	 * @param action    the raw action name
	 * @param target    the target as the sentence names it
	 * @param signature the call in Java source notation
	 * @param reason    the reason clause
	 */
	private static void assertSentence(String message, String key, String caller, String action, String target,
			String signature, String reason) {
		String template = Messages.localized(key, caller, Messages.localized("security.advice.action." + action),
				target, signature + ANY, reason);
		String[] parts = template.split(ANY, -1);
		Pattern sentence = Pattern.compile(Pattern.quote(parts[0]) + ".*" + Pattern.quote(parts[1]), Pattern.DOTALL);
		assertThat(message).matches(sentence).doesNotContain("security.advice.").doesNotContain(" | ");
	}

	/**
	 * Runs a call that must be denied and returns the message of the denial.
	 *
	 * @param call the call
	 * @return the message
	 */
	private static String denial(Executable call) {
		return assertThrows(SecurityException.class, call).getMessage();
	}

	/**
	 * Publishes the values every check reads, with every file rule empty.
	 *
	 * @param mode the backend
	 */
	private static void configure(AOPMode mode) {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", mode.name(), "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCHUNIT", mode.name());
		for (String setting : FILE_RULES) {
			JavaAOPTestCase.setJavaAdviceSettingValue(setting, new String[0], "ARCHUNIT", mode.name());
		}
	}

	/**
	 * Publishes the command rules.
	 *
	 * @param mode      the backend
	 * @param commands  the allowed commands
	 * @param arguments the allowed arguments per command
	 */
	private static void allowCommands(AOPMode mode, String[] commands, String[][] arguments) {
		JavaAOPTestCase.setJavaAdviceSettingValue("commandsAllowedToBeExecuted", commands, "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("argumentsAllowedToBePassed", arguments, "ARCHUNIT", mode.name());
	}

	/**
	 * Publishes one connect rule.
	 *
	 * @param mode the backend
	 * @param host the allowed host
	 * @param port the allowed port
	 */
	private static void allowConnection(AOPMode mode, String host, int port) {
		JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeConnectedTo", new String[] { host }, "ARCHUNIT",
				mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeConnectedTo", new int[] { port }, "ARCHUNIT",
				mode.name());
	}

	/**
	 * Publishes one thread rule allowing a single thread of the given class.
	 *
	 * @param mode        the backend
	 * @param threadClass the allowed thread class
	 */
	private static void allowThreads(AOPMode mode, String threadClass) {
		JavaAOPTestCase.setJavaAdviceSettingValue("threadClassAllowedToBeCreated", new String[] { threadClass },
				"ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("threadNumberAllowedToBeCreated", new int[] { 1 }, "ARCHUNIT",
				mode.name());
	}

	/**
	 * A call to {@code Files.readAllBytes(path)}.
	 *
	 * @param path the path
	 * @return the call
	 */
	private static Call read(Path path) {
		return new Call(Files.class, "readAllBytes", "(java.nio.file.Path)", new Class<?>[] { Path.class },
				new Object[] { path }, null);
	}

	/**
	 * A call to {@code Files.writeString(path, "new")} with no open options.
	 *
	 * @param path the path
	 * @return the call
	 */
	private static Call writeString(Path path) {
		return new Call(Files.class, "writeString",
				"(java.nio.file.Path,java.lang.CharSequence,[Ljava.nio.file.OpenOption;)",
				new Class<?>[] { Path.class, CharSequence.class, OpenOption[].class },
				new Object[] { path, "new", new OpenOption[0] }, null);
	}
}
