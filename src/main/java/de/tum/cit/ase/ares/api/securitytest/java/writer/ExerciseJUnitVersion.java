package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The JUnit version an exercise's build names. JUnit reads the setting that
 * limits which extensions load only from 5.12 on, so the generated hooks are
 * refused for an older version, for one that is not a number, and for a build
 * file that cannot be read. A version set in the exercise's own build files is
 * looked up first; one from a parent is accepted, and a range is checked by its
 * bounds.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
final class ExerciseJUnitVersion {

	/** The oldest JUnit major version that reads the include filter. */
	private static final int MINIMUM_MAJOR = 5;

	/** The oldest minor version of {@link #MINIMUM_MAJOR} that reads it. */
	private static final int MINIMUM_MINOR = 12;

	/** How many times a reference inside a looked-up value is followed. */
	private static final int MAXIMUM_LOOKUPS = 4;

	/** The build files a JUnit version can be set in, below the project root. */
	private static final List<String> BUILD_FILES = List.of("pom.xml", "build.gradle", "build.gradle.kts");

	/** Gradle's property file, below the project root. */
	private static final String GRADLE_PROPERTIES = "gradle.properties";

	/** Gradle's default version catalogue, below the project root. */
	private static final String CATALOGUE = "gradle/libs.versions.toml";

	/**
	 * The ways a build file names a JUnit Jupiter or JUnit BOM version: Maven's
	 * dependency entry, then Gradle's coordinate text. Each captures the whole
	 * value, also a {@code ${...}} lookup, so a value that is not a number is seen
	 * too.
	 */
	private static final List<Pattern> VERSION_SETTINGS = List.of(Pattern.compile(
			"<artifactId>\\s*junit-(?:jupiter[\\w-]*|bom)\\s*</artifactId>\\s*<version>\\s*([^<]*?)\\s*</version>"),
			Pattern.compile("org\\.junit(?:\\.jupiter)?:junit-(?:jupiter[\\w-]*|bom):((?:\\$\\{[^}]*}|[^'\"\\s)@])*)"));

	/**
	 * A version number such as {@code 5.12.2} or {@code 5.13.0-M1}: numeric parts
	 * of at most nine digits, then any qualifier after a dash or a plus.
	 */
	private static final Pattern VERSION_NUMBER = Pattern.compile("\\d{1,9}(?:\\.\\d{1,9})*(?:[-+][\\w.\\-+]*)?");

	/**
	 * One set in a Maven range, capturing its lowest version, its highest version
	 * when it has a comma, and its closing bracket.
	 */
	private static final Pattern RANGE_SET = Pattern.compile("[\\[(]([^,\\])]*)(?:,([^\\])]*))?([\\])])");

	/** A comment in a Maven build file, which names no dependency. */
	private static final Pattern XML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

	/**
	 * A comment in a Gradle build file. It must start a line or follow a space, so
	 * the slashes of a URL or of a pattern such as {@code **}{@code /*.java} are
	 * kept rather than read as a comment that hides a dependency.
	 */
	private static final Pattern GRADLE_COMMENT = Pattern.compile("(?<=^|\\s)(?:/\\*.*?\\*/|//[^\\n]*)",
			Pattern.DOTALL | Pattern.MULTILINE);

	/** A comment in a properties file or a TOML catalogue. */
	private static final Pattern HASH_COMMENT = Pattern.compile("^\\s*[#!][^\\n]*", Pattern.MULTILINE);

	/** A reference to a value set elsewhere: {@code ${name}} or {@code $name}. */
	private static final Pattern REFERENCE = Pattern.compile("\\$\\{([^}$]+)\\}|\\$([A-Za-z_]\\w*)");

	/** Maven's properties block, which holds one element per property. */
	private static final Pattern MAVEN_PROPERTIES = Pattern.compile("<properties>(.*?)</properties>", Pattern.DOTALL);

	/** One Maven property: its name, then its value. */
	private static final Pattern MAVEN_PROPERTY = Pattern.compile("<([\\w.\\-]+)>\\s*([^<]*?)\\s*</\\1>");

	/**
	 * The ways a Gradle build file sets a variable, each capturing its name, then
	 * its value: an assignment (also {@code ext.}, {@code def}, {@code val} and
	 * {@code var}), {@code extra["name"]}, {@code set("name", ...)} and
	 * {@code val name by extra(...)}.
	 */
	private static final List<Pattern> GRADLE_VARIABLES = List.of(
			Pattern.compile(
					"(?:\\bext\\.|\\b(?:def|val|var)\\s+)?\\b([A-Za-z_][\\w.]*)\\s*=\\s*['\"]([^'\"\\n]*)['\"]"),
			Pattern.compile("\\bextra\\[\\s*['\"]([\\w.]+)['\"]\\s*]\\s*=\\s*['\"]([^'\"\\n]*)['\"]"),
			Pattern.compile("\\bset\\(\\s*['\"]([\\w.]+)['\"]\\s*,\\s*['\"]([^'\"\\n]*)['\"]"),
			Pattern.compile("\\bval\\s+(\\w+)\\s+by\\s+extra\\(\\s*['\"]([^'\"\\n]*)['\"]"));

	/** One line of a properties file: its name, then its value. */
	private static final Pattern PROPERTY_LINE = Pattern.compile("^\\s*([\\w.\\-]+)\\s*[=:]\\s*(\\S[^\\n]*?)\\s*$",
			Pattern.MULTILINE);

	/**
	 * A catalogue's {@code [versions]} table, up to the next table. Its header may
	 * carry spaces inside the brackets and a trailing comment.
	 */
	private static final Pattern VERSIONS_TABLE = Pattern.compile(
			"^\\s*\\[\\s*versions\\s*]\\s*(?:#[^\\n]*)?$(.*?)(?=^\\s*\\[|\\z)", Pattern.MULTILINE | Pattern.DOTALL);

	/** One entry of a TOML table: its key, then its quoted value. */
	private static final Pattern TOML_ENTRY = Pattern.compile("^\\s*([\\w.\\-]+)\\s*=\\s*\"([^\"]*)\"",
			Pattern.MULTILINE);

	/** A catalogue library naming JUnit Jupiter or the JUnit BOM as a module. */
	private static final Pattern CATALOGUE_MODULE = Pattern
			.compile("\"org\\.junit(?:\\.jupiter)?:junit-(?:jupiter[\\w-]*|bom)\"");

	/**
	 * A catalogue library naming JUnit's group, matched with
	 * {@link #CATALOGUE_NAME}.
	 */
	private static final Pattern CATALOGUE_GROUP = Pattern.compile("\\bgroup\\s*=\\s*\"org\\.junit(?:\\.jupiter)?\"");

	/** A catalogue library naming JUnit Jupiter or the JUnit BOM by name. */
	private static final Pattern CATALOGUE_NAME = Pattern.compile("\\bname\\s*=\\s*\"junit-(?:jupiter[\\w-]*|bom)\"");

	/** A catalogue library's reference to an entry of {@code [versions]}. */
	private static final Pattern CATALOGUE_REFERENCE = Pattern.compile("\\b(?:version\\.ref|ref)\\s*=\\s*\"([^\"]+)\"");

	/** A catalogue library's own version, plain or in a rich version. */
	private static final Pattern CATALOGUE_VERSION = Pattern
			.compile("\\b(?:version|strictly|require|prefer)\\s*=\\s*\"([^\"]+)\"");

	/**
	 * A value set in the exercise's own build files.
	 *
	 * @param value  the value as written there.
	 * @param source the file that sets it.
	 */
	private record Definition(@Nonnull String value, @Nonnull Path source) {
	}

	/**
	 * A version number to check against 5.12.
	 *
	 * @param number   the number as written.
	 * @param excluded true for the highest version of a range that excludes it, so
	 *                 5.12 itself is then too old too.
	 */
	private record Bound(@Nonnull String number, boolean excluded) {
	}

	/**
	 * A version value after its references were looked up.
	 *
	 * @param value   the value, still holding a reference Ares could not look up.
	 * @param sources the files whose values were used.
	 */
	private record Resolution(@Nonnull String value, @Nonnull Set<String> sources) {
	}

	/** Prevents instantiation: this class only reads build files. */
	private ExerciseJUnitVersion() {
		throw new IllegalStateException("ExerciseJUnitVersion is a utility class and should not be instantiated");
	}

	/**
	 * Refuses an exercise whose build names a JUnit version older than 5.12 or one
	 * that is not a number, directly or through a value its own build files set, or
	 * whose build file cannot be read.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @throws SecurityException naming the version, or the build file
	 */
	static void requireIncludeFilterSupport(@Nonnull Path projectRoot) {
		Map<String, List<Definition>> definitions = definitions(projectRoot);
		for (String buildFile : BUILD_FILES) {
			Path path = projectRoot.resolve(buildFile);
			String text = withoutComments(buildFile, read(path));
			for (String version : namedVersions(text)) {
				requireSupported(path, version, definitions);
			}
		}
		Path catalogue = projectRoot.resolve(CATALOGUE);
		for (String version : catalogueVersions(read(catalogue))) {
			requireSupported(catalogue, version, definitions);
		}
	}

	/**
	 * The JUnit versions a build file's text names, as written.
	 *
	 * @param text the text, without comments.
	 * @return every value a JUnit Jupiter or JUnit BOM entry names
	 */
	@Nonnull
	private static List<String> namedVersions(@Nonnull String text) {
		List<String> versions = new ArrayList<>();
		for (Pattern setting : VERSION_SETTINGS) {
			Matcher matcher = setting.matcher(text);
			while (matcher.find()) {
				versions.add(matcher.group(1));
			}
		}
		return versions;
	}

	/**
	 * The JUnit versions a version catalogue names: coordinates with a version, and
	 * libraries whose version is their own or an entry of {@code [versions]}.
	 *
	 * @param text the catalogue's text.
	 * @return every such value, a reference written as a catalogue lookup
	 */
	@Nonnull
	private static List<String> catalogueVersions(@Nonnull String text) {
		String uncommented = HASH_COMMENT.matcher(text).replaceAll(" ");
		List<String> versions = namedVersions(uncommented);
		for (String line : uncommented.split("\\R")) {
			if (namesJUnitLibrary(line)) {
				catalogueVersionOf(line, versions);
			}
		}
		return versions;
	}

	/**
	 * Whether one catalogue line declares JUnit Jupiter or the JUnit BOM.
	 *
	 * @param line the line.
	 * @return true for a module or a group and name naming it
	 */
	private static boolean namesJUnitLibrary(@Nonnull String line) {
		return CATALOGUE_MODULE.matcher(line).find()
				|| CATALOGUE_GROUP.matcher(line).find() && CATALOGUE_NAME.matcher(line).find();
	}

	/**
	 * Adds the version one catalogue library names, if it names one.
	 *
	 * @param line     the library's line.
	 * @param versions where the value is added.
	 */
	private static void catalogueVersionOf(@Nonnull String line, @Nonnull List<String> versions) {
		Matcher reference = CATALOGUE_REFERENCE.matcher(line);
		if (reference.find()) {
			versions.add("${" + catalogueLookup(reference.group(1)) + "}");
			return;
		}
		Matcher own = CATALOGUE_VERSION.matcher(line);
		if (own.find()) {
			versions.add(own.group(1));
		}
	}

	/**
	 * The name a build file uses for an entry of {@code [versions]}, such as
	 * {@code libs.versions.junit.jupiter.get()} for {@code junit-jupiter}.
	 *
	 * @param key the entry's key.
	 * @return the lookup it is found under
	 */
	@Nonnull
	private static String catalogueLookup(@Nonnull String key) {
		return "libs.versions." + key.replaceAll("[-_]", ".") + ".get()";
	}

	/**
	 * The values the exercise's own build files set: Maven properties, Gradle
	 * variables, {@code gradle.properties} and the catalogue's {@code [versions]}.
	 * Every value set under a name is kept, since a later assignment can replace an
	 * earlier one.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @return every value by its name
	 */
	@Nonnull
	private static Map<String, List<Definition>> definitions(@Nonnull Path projectRoot) {
		Map<String, List<Definition>> definitions = new HashMap<>();
		Path pom = projectRoot.resolve("pom.xml");
		Matcher block = MAVEN_PROPERTIES.matcher(withoutComments("pom.xml", read(pom)));
		while (block.find()) {
			collect(MAVEN_PROPERTY, block.group(1), pom, definitions);
		}
		for (String buildFile : List.of("build.gradle", "build.gradle.kts")) {
			Path path = projectRoot.resolve(buildFile);
			String text = withoutComments(buildFile, read(path));
			for (Pattern variable : GRADLE_VARIABLES) {
				collect(variable, text, path, definitions);
			}
		}
		Path properties = projectRoot.resolve(GRADLE_PROPERTIES);
		collect(PROPERTY_LINE, HASH_COMMENT.matcher(read(properties)).replaceAll(" "), properties, definitions);
		collectCatalogue(projectRoot.resolve(CATALOGUE), definitions);
		return definitions;
	}

	/**
	 * Adds the entries of a catalogue's {@code [versions]} table under the names a
	 * build file looks them up by.
	 *
	 * @param catalogue   the catalogue; may not exist.
	 * @param definitions where the entries are added.
	 */
	private static void collectCatalogue(@Nonnull Path catalogue, @Nonnull Map<String, List<Definition>> definitions) {
		String text = HASH_COMMENT.matcher(read(catalogue)).replaceAll(" ");
		Matcher table = VERSIONS_TABLE.matcher(text);
		if (!table.find()) {
			return;
		}
		Matcher entry = TOML_ENTRY.matcher(table.group(1));
		while (entry.find()) {
			add(definitions, catalogueLookup(entry.group(1)), new Definition(entry.group(2), catalogue));
		}
	}

	/**
	 * Adds every name and value a pattern finds in a file's text.
	 *
	 * @param pattern     captures a name, then a value.
	 * @param text        the text to search.
	 * @param source      the file the text comes from.
	 * @param definitions where the values are added, every one per name.
	 */
	private static void collect(@Nonnull Pattern pattern, @Nonnull String text, @Nonnull Path source,
			@Nonnull Map<String, List<Definition>> definitions) {
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			add(definitions, matcher.group(1), new Definition(matcher.group(2), source));
		}
	}

	/**
	 * Adds one value under its name, unless the same value is already there.
	 *
	 * @param definitions where the value is added.
	 * @param name        the name it is set under.
	 * @param definition  the value and its file.
	 */
	private static void add(@Nonnull Map<String, List<Definition>> definitions, @Nonnull String name,
			@Nonnull Definition definition) {
		List<Definition> values = definitions.computeIfAbsent(name, unused -> new ArrayList<>());
		if (!values.contains(definition)) {
			values.add(definition);
		}
	}

	/**
	 * Every way a version value reads once its references are replaced by the
	 * values the exercise sets, a few times over. A name set twice gives one
	 * reading per value; a reference Ares cannot look up, or a loop, stays in it.
	 *
	 * @param version     the value as written.
	 * @param definitions the values the exercise sets.
	 * @return each reading and the files it used
	 */
	@Nonnull
	private static Set<Resolution> resolved(@Nonnull String version,
			@Nonnull Map<String, List<Definition>> definitions) {
		Set<Resolution> readings = Set.of(new Resolution(version, new TreeSet<>()));
		for (int lookup = 0; lookup < MAXIMUM_LOOKUPS; lookup++) {
			Set<Resolution> next = new LinkedHashSet<>();
			for (Resolution reading : readings) {
				next.addAll(lookedUpOnce(reading, definitions));
			}
			readings = next;
		}
		return readings;
	}

	/**
	 * One reading with every reference replaced once, one result per combination of
	 * the values its names are set to.
	 *
	 * @param reading     the value so far and the files it used.
	 * @param definitions the values the exercise sets.
	 * @return each combination
	 */
	@Nonnull
	private static List<Resolution> lookedUpOnce(@Nonnull Resolution reading,
			@Nonnull Map<String, List<Definition>> definitions) {
		String text = reading.value();
		List<Resolution> partial = List.of(new Resolution("", reading.sources()));
		Matcher reference = REFERENCE.matcher(text);
		int end = 0;
		while (reference.find()) {
			String name = reference.group(1) == null ? reference.group(2) : reference.group(1);
			String before = text.substring(end, reference.start());
			partial = appended(partial, before, reference.group(), definitions.getOrDefault(name.strip(), List.of()));
			end = reference.end();
		}
		String tail = text.substring(end);
		return partial.stream().map(part -> new Resolution(part.value() + tail, part.sources())).toList();
	}

	/**
	 * Each partial reading continued by the text before a reference and then by
	 * each value the reference's name is set to, or by the reference itself when
	 * none is.
	 *
	 * @param partial   the readings so far.
	 * @param before    the text before the reference.
	 * @param reference the reference as written.
	 * @param values    the values its name is set to.
	 * @return the continued readings
	 */
	@Nonnull
	private static List<Resolution> appended(@Nonnull List<Resolution> partial, @Nonnull String before,
			@Nonnull String reference, @Nonnull List<Definition> values) {
		List<Resolution> continued = new ArrayList<>();
		for (Resolution part : partial) {
			if (values.isEmpty()) {
				continued.add(new Resolution(part.value() + before + reference, part.sources()));
			}
			for (Definition value : values) {
				Set<String> sources = new TreeSet<>(part.sources());
				sources.add(value.source().toString());
				continued.add(new Resolution(part.value() + before + value.value(), sources));
			}
		}
		return continued;
	}

	/**
	 * Refuses one version value if any reading of it, after looking up its
	 * references, is too old, unless that reading is still taken from elsewhere. A
	 * Maven range is checked by its bounds, so {@code [5.11.4]} and
	 * {@code (,5.11.4]} are refused like {@code 5.11.4}.
	 *
	 * @param buildFile   the file that names it.
	 * @param version     the value as written.
	 * @param definitions the values the exercise sets.
	 * @throws SecurityException for a version older than 5.12 or not a number
	 */
	private static void requireSupported(@Nonnull Path buildFile, @Nonnull String version,
			@Nonnull Map<String, List<Definition>> definitions) {
		for (Resolution resolution : resolved(version, definitions)) {
			String value = resolution.value();
			if (isTakenFromElsewhere(value)) {
				continue;
			}
			List<Bound> bounds = isRange(value) ? rangeBounds(value) : List.of(new Bound(value, false));
			for (Bound bound : bounds) {
				requireNumberSupported(buildFile, version, resolution, bound);
			}
		}
	}

	/**
	 * Refuses one version number, or a bound of a range, that is not a number or
	 * allows only versions older than 5.12.
	 *
	 * @param buildFile  the file that names it.
	 * @param version    the value as written.
	 * @param resolution the value after its references were looked up.
	 * @param bound      the number to check.
	 * @throws SecurityException naming the value as written and as looked up
	 */
	private static void requireNumberSupported(@Nonnull Path buildFile, @Nonnull String version,
			@Nonnull Resolution resolution, @Nonnull Bound bound) {
		String value = resolution.value();
		String number = bound.number();
		if (!VERSION_NUMBER.matcher(number).matches()) {
			throw new SecurityException(
					Messages.localized("security.writer.junit.version.unreadable", buildFile.toString(), value));
		}
		if (!isTooOld(number) && !(bound.excluded() && isMinimum(number))) {
			return;
		}
		if (value.equals(version)) {
			throw new SecurityException(Messages.localized("security.writer.junit.version.unsupported", value));
		}
		throw new SecurityException(Messages.localized("security.writer.junit.version.unsupported.indirect",
				buildFile.toString(), version, String.join(", ", resolution.sources()), value));
	}

	/**
	 * Whether a version value still holds a property Ares could not look up, such
	 * as {@code ${project.parent.version}} or {@code $junitVersion}.
	 *
	 * @param version the value after looking up.
	 * @return true when the build file does not name the number itself
	 */
	private static boolean isTakenFromElsewhere(@Nonnull String version) {
		return version.contains("$");
	}

	/**
	 * Whether a version value is a Maven version range, such as {@code [5.12,6)}.
	 *
	 * @param version the value after looking up.
	 * @return true when it starts with a bracket
	 */
	private static boolean isRange(@Nonnull String version) {
		return version.startsWith("[") || version.startsWith("(");
	}

	/**
	 * The bound to check in every set of a Maven range: its lowest version, or,
	 * when it has none, its highest one, so {@code (,5.11.4]} is checked too. A set
	 * with neither, or a range Ares cannot split into sets, adds itself unread.
	 *
	 * @param range the range, such as {@code [5.11,6),[7,)}.
	 * @return each bound as written
	 */
	@Nonnull
	private static List<Bound> rangeBounds(@Nonnull String range) {
		List<Bound> bounds = new ArrayList<>();
		Matcher set = RANGE_SET.matcher(range);
		while (set.find()) {
			String lower = set.group(1).strip();
			String upper = set.group(2) == null ? "" : set.group(2).strip();
			if (!lower.isEmpty()) {
				bounds.add(new Bound(lower, false));
			} else if (!upper.isEmpty()) {
				bounds.add(new Bound(upper, ")".equals(set.group(3))));
			}
		}
		if (bounds.isEmpty() && !RANGE_SET.matcher(range).find()) {
			bounds.add(new Bound(range, false));
		}
		return bounds;
	}

	/**
	 * A build file's text without its comments, so a commented-out example names no
	 * version.
	 *
	 * @param buildFile the build file's name, which tells Maven from Gradle.
	 * @param text      the build file's text.
	 * @return the text with every comment removed
	 */
	@Nonnull
	private static String withoutComments(@Nonnull String buildFile, @Nonnull String text) {
		Pattern comment = "pom.xml".equals(buildFile) ? XML_COMMENT : GRADLE_COMMENT;
		return comment.matcher(text).replaceAll(" ");
	}

	/**
	 * Whether a version number is older than 5.12.
	 *
	 * @param version the version, such as {@code 5.11.4}.
	 * @return true when JUnit ignores the include filter at that version, and for a
	 *         number too long to read, which Ares cannot compare
	 */
	static boolean isTooOld(@Nonnull String version) {
		String[] parts = version.split("[.\\-+]");
		try {
			int major = Integer.parseInt(parts[0]);
			int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
			return major < MINIMUM_MAJOR || major == MINIMUM_MAJOR && minor < MINIMUM_MINOR;
		} catch (NumberFormatException unreadable) {
			return true;
		}
	}

	/**
	 * Whether a version number is 5.12.0 itself, ignoring any qualifier, so a range
	 * that excludes it allows only older versions.
	 *
	 * @param version the version, such as {@code 5.12} or {@code 5.12.0-M1}.
	 * @return true for 5.12 with only zero parts after it
	 */
	private static boolean isMinimum(@Nonnull String version) {
		String[] parts = version.split("[-+]", 2)[0].split("\\.");
		if (parts.length < 2 || Integer.parseInt(parts[0]) != MINIMUM_MAJOR
				|| Integer.parseInt(parts[1]) != MINIMUM_MINOR) {
			return false;
		}
		for (int part = 2; part < parts.length; part++) {
			if (Integer.parseInt(parts[part]) != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A build file's text.
	 *
	 * @param buildFile the build file; may not exist.
	 * @return its text, or empty when nothing is there
	 * @throws SecurityException naming a build file that exists but cannot be read,
	 *                           such as a folder or a broken link
	 */
	@Nonnull
	private static String read(@Nonnull Path buildFile) {
		if (!Files.exists(buildFile, LinkOption.NOFOLLOW_LINKS)) {
			return "";
		}
		if (!Files.isRegularFile(buildFile)) {
			throw new SecurityException(
					Messages.localized("security.writer.junit.build.unreadable", buildFile.toString()));
		}
		try {
			return Files.readString(buildFile);
		} catch (IOException unreadable) {
			throw new SecurityException(
					Messages.localized("security.writer.junit.build.unreadable", buildFile.toString()), unreadable);
		}
	}
}
