package de.tum.cit.ase.ares.api.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
		for (String fileName : List.of("messages.properties", "messages_de.properties")) {
			Set<String> keys = new HashSet<>(rawKeys(fileName));
			List<String> missing = expected.stream().filter(key -> !keys.contains(key)).toList();
			assertEquals(List.of(), missing, fileName + " lacks keys the code builds at run time");
		}
	}
}
