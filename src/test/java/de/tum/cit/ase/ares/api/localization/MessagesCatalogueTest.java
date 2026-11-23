package de.tum.cit.ase.ares.api.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that the English and German message catalogues agree with each other
 * and keep the shape every Ares error message is meant to have. Each check
 * guards a defect the catalogues once had: keys present in one language only,
 * keys written twice, placeholders that do not match, broken umlauts, and
 * prefixes that leave out who is at fault or when it happened.
 */
class MessagesCatalogueTest {

	/** Where both catalogues live on the class path. */
	private static final String CATALOGUE_DIRECTORY = "/de/tum/cit/ase/ares/api/localization/";

	/** A format placeholder such as {@code %s}, {@code %d} or {@code %2$s}. */
	private static final Pattern PLACEHOLDER = Pattern.compile("%(?:(\\d+)\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?([a-zA-Z%])");

	/** The English prefix, with the only two reasons and stages Ares uses. */
	private static final Pattern ENGLISH_PREFIX = Pattern.compile(
			"^Ares Security Error \\(Reason: (Ares-Code|Student-Code); Stage: (Creation|Execution)\\): .+",
			Pattern.DOTALL);

	/** The German prefix, matching {@link #ENGLISH_PREFIX} part for part. */
	private static final Pattern GERMAN_PREFIX = Pattern.compile(
			"^Ares Sicherheitsfehler \\(Grund: (Ares-Code|Student-Code); Phase: (Erstellung|Ausführung)\\): .+",
			Pattern.DOTALL);

	/** The raw action names that the advice code translates before reporting. */
	private static final List<String> ADVICE_ACTIONS = List.of("read", "overwrite", "create", "delete", "execute",
			"connect", "send", "receive", "manipulate");

	/** The architecture rules, by the key part after their message key prefix. */
	private static final List<String> ARCHITECTURE_RULES = List.of("file.system.access", "network.access",
			"terminate.jvm", "reflection.uses", "execute.command", "manipulate.threads", "package.import", "serialize",
			"class.loading", "native.code.access", "agent.attach", "environment.access", "module.system.access",
			"jndi.injection");

	/**
	 * The key families whose values are Ares's own failures and so must carry the
	 * prefix.
	 */
	private static final List<String> PREFIXED_FAMILIES = List.of("security.", "policy.", "timeout.", "test_guard.",
			"reporting.");

	/**
	 * Keys outside those families that are Ares failures too and so must carry the
	 * prefix.
	 */
	private static final List<String> PREFIXED_KEYS = List.of("output_tester.output_maxExceeded",
			"io_tester.parameter_not_provided", "io_tester.manager_creation_failed");

	/**
	 * The reason clauses the advice builds per resource type and inserts after
	 * "because".
	 */
	private static final List<String> DENIAL_REASON_RESOURCES = List.of("file", "command", "network", "thread");

	/**
	 * The resource types that have a rule list per action, so "no rule for this
	 * action" exists.
	 */
	private static final List<String> DENIAL_REASON_ACTION_RESOURCES = List.of("file", "network");

	/**
	 * Fragments inserted into other messages, which therefore carry no prefix of
	 * their own.
	 */
	private static final List<String> FRAGMENT_KEYS = List.of("security.advice.called.by",
			"security.advice.resolved.runtime.declaration", "security.advice.thread.lambda",
			"security.advice.denial.reason.command.executable.path.not.allowed", "reporting.problem_location_hint",
			"security.gradle.sourceroots.detail.version", "security.gradle.sourceroots.detail.missing",
			"security.gradle.sourceroots.detail.encoding", "security.gradle.sourceroots.detail.charset",
			"security.gradle.sourceroots.detail.relative");

	/**
	 * Validator messages, which the policy reader's first line prefixes once for
	 * all of them.
	 */
	private static final List<String> SCHEMA_KEYS = List.of("root", "object", "unknown.field", "unknown.fields",
			"unknown.field.none", "unknown.fields.none", "missing.field", "missing.fields", "null", "text",
			"text.optional", "text.any", "text.list", "list", "integer", "boolean", "version.single", "version.range",
			"configuration", "package", "main.class", "test.class", "file.path", "host", "command", "command.argument",
			"thread.class", "package.import", "port", "thread.count", "timeout", "null.list");

	/**
	 * Ares 1 texts that no source references any more; kept until a later pull
	 * request removes them.
	 */
	private static final List<String> UNUSED_ARES_1_KEYS = List.of("security.access_token_invalid",
			"security.already_created", "security.already_disabled", "security.already_installed",
			"security.not_installed", "security.error_awt", "security.error_blacklist", "security.error_classloader",
			"security.error_disallowed_package", "security.error_execute", "security.error_link",
			"security.error_management", "security.error_modify_auth", "security.error_modify_security",
			"security.error_modify_serialization", "security.error_modify_ssl", "security.error_network_accept",
			"security.error_network_connect", "security.error_network_connect_with_context",
			"security.error_network_listen", "security.error_network_multicast", "security.error_networking",
			"security.error_printer", "security.error_security_manager", "security.error_system_exit",
			"security.error_threads_still_active", "security.error_threads_not_stoppable",
			"security.error_thread_whitelisting_failed", "security.error_path_access", "security.error_thread_access",
			"security.error_thread_maxExceeded", "security.error_threadgroup_access",
			"security.error_package_definition", "security.stackframe_add_info",
			"security.privileged_throw_not_allowed", "security.configuration_invalid_negative_threads",
			"security.configuration_invalid_port_rule_intersection", "security.configuration_invalid_port_negative",
			"security.configuration_invalid_port_over_max", "security.configuration_invalid_port_allowed_in_rage",
			"security.configuration_invalid_port_exclude_outside_rage");

	/**
	 * Where the production sources live, relative to the project root the tests run
	 * in.
	 */
	private static final Path MAIN_SOURCES = Path.of("src", "main", "java");

	/** A string literal that starts like an Ares prefix, in either language. */
	private static final Pattern PREFIX_LITERAL = Pattern
			.compile("\"(Ares (?:Security Error|Sicherheitsfehler)[^\"]*)\"");

	/**
	 * A prefix literal that is complete, alone or followed by more text in the same
	 * literal.
	 */
	private static final Pattern PREFIX_LITERAL_SHAPE = Pattern
			.compile("^Ares Security Error \\(Reason: (Ares-Code|Student-Code); Stage: (Creation|Execution)\\): .*"
					+ "|^Ares Sicherheitsfehler \\(Grund: (Ares-Code|Student-Code); Phase: (Erstellung|Ausf\u00fchrung)\\): .*");

	/**
	 * Reads a catalogue's raw bytes.
	 *
	 * @param fileName the catalogue file name
	 * @return the file content as bytes
	 * @throws IOException if the catalogue cannot be read
	 */
	private static byte[] readBytes(String fileName) throws IOException {
		try (InputStream in = Objects.requireNonNull(
				MessagesCatalogueTest.class.getResourceAsStream(CATALOGUE_DIRECTORY + fileName), fileName)) {
			return in.readAllBytes();
		}
	}

	/**
	 * Lists the keys of a catalogue in file order, duplicates included, which
	 * {@link Properties} would hide.
	 *
	 * @param fileName the catalogue file name
	 * @return every key line's key, in order
	 * @throws IOException if the catalogue cannot be read
	 */
	private static List<String> rawKeys(String fileName) throws IOException {
		List<String> keys = new ArrayList<>();
		for (String line : new String(readBytes(fileName), StandardCharsets.UTF_8).split("\\R")) {
			String trimmed = line.strip();
			if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!") && trimmed.contains("=")) {
				keys.add(trimmed.substring(0, trimmed.indexOf('=')).strip());
			}
		}
		return keys;
	}

	/**
	 * Loads a catalogue with its escapes decoded, keyed in file order.
	 *
	 * @param fileName the catalogue file name
	 * @return the decoded values by key
	 * @throws IOException if the catalogue cannot be read
	 */
	private static Map<String, String> values(String fileName) throws IOException {
		Properties properties = new Properties();
		properties.load(new StringReader(new String(readBytes(fileName), StandardCharsets.UTF_8)));
		Map<String, String> result = new LinkedHashMap<>();
		for (String key : rawKeys(fileName)) {
			result.put(key, properties.getProperty(key));
		}
		return result;
	}

	/**
	 * Counts the arguments a template consumes, honouring numbered placeholders.
	 *
	 * @param template the message template
	 * @return the highest argument position the template reads
	 */
	private static int arity(String template) {
		int sequential = 0;
		int highest = 0;
		Matcher matcher = PLACEHOLDER.matcher(template);
		while (matcher.find()) {
			if ("%".equals(matcher.group(2)) || "n".equals(matcher.group(2))) {
				continue;
			}
			int position = matcher.group(1) == null ? ++sequential : Integer.parseInt(matcher.group(1));
			highest = Math.max(highest, position);
		}
		return highest;
	}

	/**
	 * Lists every key exempt from the required prefix, by name.
	 *
	 * @return the exempt keys
	 */
	private static Set<String> exemptKeys() {
		Set<String> exempt = new TreeSet<>(FRAGMENT_KEYS);
		ADVICE_ACTIONS.forEach(action -> exempt.add("security.advice.action." + action));
		ARCHITECTURE_RULES.forEach(rule -> {
			exempt.add("security.architecture." + rule);
			exempt.add("security.archunit.action." + rule);
		});
		exempt.addAll(denialReasonKeys());
		SCHEMA_KEYS.forEach(key -> exempt.add("policy.schema." + key));
		exempt.addAll(UNUSED_ARES_1_KEYS);
		return exempt;
	}

	/**
	 * Lists the denial reason keys the advice builds from a resource type.
	 *
	 * @return the reason keys
	 */
	private static List<String> denialReasonKeys() {
		List<String> keys = new ArrayList<>();
		DENIAL_REASON_RESOURCES.forEach(resource -> {
			keys.add("security.advice.denial.reason.no.rule." + resource);
			keys.add("security.advice.denial.reason.target.not.allowed." + resource);
		});
		DENIAL_REASON_ACTION_RESOURCES
				.forEach(resource -> keys.add("security.advice.denial.reason.no.action.rule." + resource));
		return keys;
	}

	/**
	 * Lists the keys that must carry the prefix but do not.
	 *
	 * @param catalogue the decoded catalogue
	 * @param exempt    the keys exempt by name
	 * @return the offending keys
	 */
	static List<String> keysMissingThePrefix(Map<String, String> catalogue, Set<String> exempt) {
		return catalogue.entrySet().stream()
				.filter(entry -> PREFIXED_FAMILIES.stream().anyMatch(entry.getKey()::startsWith)
						|| PREFIXED_KEYS.contains(entry.getKey()))
				.filter(entry -> !exempt.contains(entry.getKey()))
				.filter(entry -> !entry.getValue().startsWith("Ares Security Error")).map(Map.Entry::getKey).toList();
	}

	/**
	 * Lists the prefix literals in a source text whose prefix is malformed.
	 *
	 * @param source the source text
	 * @return the malformed literals
	 */
	static List<String> malformedPrefixLiterals(String source) {
		List<String> malformed = new ArrayList<>();
		Matcher literal = PREFIX_LITERAL.matcher(source);
		while (literal.find()) {
			if (!PREFIX_LITERAL_SHAPE.matcher(literal.group(1)).matches()) {
				malformed.add(literal.group(1));
			}
		}
		return malformed;
	}

	/**
	 * Reads a source file as text.
	 *
	 * @param file the source file
	 * @return its content
	 */
	private static String readSource(Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Both catalogues must define exactly the same keys, so no message falls back
	 * to the other language or to the bare key.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void bothCataloguesDefineTheSameKeys() throws IOException {
		Set<String> english = new TreeSet<>(rawKeys("messages.properties"));
		Set<String> german = new TreeSet<>(rawKeys("messages_de.properties"));
		Set<String> onlyEnglish = new TreeSet<>(english);
		onlyEnglish.removeAll(german);
		Set<String> onlyGerman = new TreeSet<>(german);
		onlyGerman.removeAll(english);
		assertTrue(onlyEnglish.isEmpty() && onlyGerman.isEmpty(),
				"only English: " + onlyEnglish + ", only German: " + onlyGerman);
	}

	/**
	 * No key may be written twice; the later entry silently wins at run time.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void noCatalogueDefinesAKeyTwice() throws IOException {
		for (String fileName : List.of("messages.properties", "messages_de.properties")) {
			Set<String> seen = new HashSet<>();
			Set<String> duplicates = new TreeSet<>();
			for (String key : rawKeys(fileName)) {
				if (!seen.add(key)) {
					duplicates.add(key);
				}
			}
			assertTrue(duplicates.isEmpty(), fileName + " defines these keys twice: " + duplicates);
		}
	}

	/**
	 * A key must consume the same number of arguments in both languages, or one of
	 * them fails to format or drops an argument.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void bothLanguagesConsumeTheSameArguments() throws IOException {
		Map<String, String> english = values("messages.properties");
		Map<String, String> german = values("messages_de.properties");
		List<String> mismatches = new ArrayList<>();
		english.forEach((key, value) -> {
			if (german.containsKey(key) && arity(value) != arity(german.get(key))) {
				mismatches.add(key);
			}
		});
		assertTrue(mismatches.isEmpty(), "argument count differs between languages: " + mismatches);
	}

	/**
	 * The German catalogue is stored as plain ASCII with escaped umlauts and holds
	 * no replacement character and no diaeresis without its vowel.
	 *
	 * @throws IOException if the catalogue cannot be read
	 */
	@Test
	void germanCatalogueHasIntactUmlauts() throws IOException {
		byte[] bytes = readBytes("messages_de.properties");
		for (byte b : bytes) {
			assertTrue(b >= 0, "the German catalogue must be plain ASCII with \\uXXXX escapes");
		}
		values("messages_de.properties")
				.forEach((key, value) -> assertTrue(value.indexOf('�') < 0 && value.indexOf('̈') < 0,
						"broken umlaut in " + key + ": " + value));
	}

	/**
	 * Every message that starts like an Ares error carries the full prefix in both
	 * languages, and both languages name the same reason and stage.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void everyErrorPrefixIsCompleteAndAgreesAcrossLanguages() throws IOException {
		Map<String, String> english = values("messages.properties");
		Map<String, String> german = values("messages_de.properties");
		Map<String, String> stageNames = Map.of("Creation", "Erstellung", "Execution", "Ausführung");
		List<String> problems = new ArrayList<>();
		english.forEach((key, value) -> {
			if (!value.startsWith("Ares Security Error")) {
				return;
			}
			Matcher englishMatch = ENGLISH_PREFIX.matcher(value);
			Matcher germanMatch = GERMAN_PREFIX.matcher(german.getOrDefault(key, ""));
			if (!englishMatch.matches() || !germanMatch.matches()) {
				problems.add(key);
			} else if (!englishMatch.group(1).equals(germanMatch.group(1))
					|| !stageNames.get(englishMatch.group(2)).equals(germanMatch.group(2))) {
				problems.add(key + " (reason or stage differs)");
			}
		});
		german.forEach((key, value) -> {
			if (value.startsWith("Ares") && value.contains("Sicherheitsfehler")
					&& !GERMAN_PREFIX.matcher(value).matches()) {
				problems.add(key + " (German prefix)");
			}
		});
		assertTrue(problems.isEmpty(), "incomplete or inconsistent prefixes: " + problems);
	}

	/**
	 * Every key Ares uses for its own failures carries the prefix, unless the test
	 * names it as a fragment, a validator message or an unused Ares 1 text. Exempt
	 * keys must exist, so the list cannot hide a typo.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void everyAresFailureKeyCarriesThePrefix() throws IOException {
		Map<String, String> english = values("messages.properties");
		Set<String> exempt = exemptKeys();
		assertEquals(List.of(), keysMissingThePrefix(english, exempt), "Ares failure keys without the prefix");
		assertEquals(List.of(), exempt.stream().filter(key -> !english.containsKey(key)).toList(),
				"exempt keys that do not exist");
	}

	/**
	 * The prefix check reports an unprefixed failure key and lets an exempt one
	 * pass, so a green run means the rule really ran.
	 */
	@Test
	void anUnprefixedFailureKeyIsReported() {
		Map<String, String> catalogue = Map.of("security.x", "Something failed.", "security.y",
				"Ares Security Error (Reason: Ares-Code; Stage: Creation): Something failed.", "security.z", "a part",
				"io_tester.other", "not an Ares failure");
		assertEquals(List.of("security.x"), keysMissingThePrefix(catalogue, Set.of("security.z")));
	}

	/**
	 * Every string literal in the production sources that starts like an Ares
	 * prefix is a complete one. It checks literals, not the concatenated message,
	 * so a literal that is only the prefix passes.
	 *
	 * @throws IOException if the sources cannot be listed
	 */
	@Test
	void everyPrefixLiteralInTheSourcesMatchesThePattern() throws IOException {
		List<String> problems = new ArrayList<>();
		try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
			files.filter(file -> file.toString().endsWith(".java") || file.toString().endsWith(".aj"))
					.forEach(file -> malformedPrefixLiterals(readSource(file))
							.forEach(literal -> problems.add(file + ": " + literal)));
		}
		assertEquals(List.of(), problems, "malformed Ares prefix literals");
	}

	/**
	 * The literal scan accepts a literal that is only the prefix and one that goes
	 * on, and reports a malformed one.
	 */
	@Test
	void aMalformedPrefixLiteralIsReported() {
		String source = """
				String a = "Ares Security Error (Reason: Student-Code; Stage: Execution): ";
				String b = "Ares Security Error (Reason: Ares-Code; Stage: Creation): The file is missing.";
				String c = "Ares Security Error (Reason: Student Code; Stage: Execution): broken";
				""";
		assertEquals(List.of("Ares Security Error (Reason: Student Code; Stage: Execution): broken"),
				malformedPrefixLiterals(source));
	}

	/**
	 * Every action and architecture rule the code translates has its keys in both
	 * catalogues, since those keys are built at run time and would otherwise only
	 * fail when a violation is reported.
	 *
	 * @throws IOException if a catalogue cannot be read
	 */
	@Test
	void everyKeyBuiltAtRunTimeExists() throws IOException {
		List<String> expected = new ArrayList<>();
		ADVICE_ACTIONS.forEach(action -> expected.add("security.advice.action." + action));
		ARCHITECTURE_RULES.forEach(rule -> {
			expected.add("security.architecture." + rule);
			expected.add("security.archunit.action." + rule);
		});
		expected.add("security.advice.called.by");
		expected.add("security.advice.resolved.runtime.declaration");
		expected.add("security.advice.thread.lambda");
		expected.addAll(denialReasonKeys());
		for (String fileName : List.of("messages.properties", "messages_de.properties")) {
			Set<String> keys = new HashSet<>(rawKeys(fileName));
			List<String> missing = expected.stream().filter(key -> !keys.contains(key)).toList();
			assertEquals(List.of(), missing, fileName + " lacks keys the code builds at run time");
		}
	}
}
