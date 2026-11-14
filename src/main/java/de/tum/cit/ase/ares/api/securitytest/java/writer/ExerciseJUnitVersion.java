package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
 * looked up first; one from a parent or a range is accepted.
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

	/** A catalogue's {@code [versions]} table, up to the next table. */
	private static final Pattern VERSIONS_TABLE = Pattern.compile("^\\s*\\[versions]\\s*$(.*?)(?=^\\s*\\[|\\z)",
			Pattern.MULTILINE | Pattern.DOTALL);

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
		Map<String, Definition> definitions = definitions(projectRoot);
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
	 * The first one set under a name wins.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @return every value by its name
	 */
	@Nonnull
	private static Map<String, Definition> definitions(@Nonnull Path projectRoot) {
		Map<String, Definition> definitions = new HashMap<>();
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
	private static void collectCatalogue(@Nonnull Path catalogue, @Nonnull Map<String, Definition> definitions) {
		String text = HASH_COMMENT.matcher(read(catalogue)).replaceAll(" ");
		Matcher table = VERSIONS_TABLE.matcher(text);
		if (!table.find()) {
			return;
		}
		Matcher entry = TOML_ENTRY.matcher(table.group(1));
		while (entry.find()) {
			definitions.putIfAbsent(catalogueLookup(entry.group(1)), new Definition(entry.group(2), catalogue));
		}
	}

	/**
	 * Adds every name and value a pattern finds in a file's text.
	 *
	 * @param pattern     captures a name, then a value.
	 * @param text        the text to search.
	 * @param source      the file the text comes from.
	 * @param definitions where the values are added, keeping the first per name.
	 */
	private static void collect(@Nonnull Pattern pattern, @Nonnull String text, @Nonnull Path source,
			@Nonnull Map<String, Definition> definitions) {
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			definitions.putIfAbsent(matcher.group(1), new Definition(matcher.group(2), source));
		}
	}

	/**
	 * A version value with its references replaced by the values the exercise sets,
	 * a few times over. A reference Ares cannot look up, or a loop, stays in it.
	 *
	 * @param version     the value as written.
	 * @param definitions the values the exercise sets.
	 * @return the value and the files used
	 */
	@Nonnull
	private static Resolution resolved(@Nonnull String version, @Nonnull Map<String, Definition> definitions) {
		String current = version;
		Set<String> sources = new TreeSet<>();
		for (int lookup = 0; lookup < MAXIMUM_LOOKUPS && current.contains("$"); lookup++) {
			Matcher reference = REFERENCE.matcher(current);
			StringBuilder replaced = new StringBuilder();
			while (reference.find()) {
				String name = reference.group(1) == null ? reference.group(2) : reference.group(1);
				Definition definition = definitions.get(name.strip());
				String value = definition == null ? reference.group() : definition.value();
				if (definition != null) {
					sources.add(definition.source().toString());
				}
				reference.appendReplacement(replaced, Matcher.quoteReplacement(value));
			}
			reference.appendTail(replaced);
			current = replaced.toString();
		}
		return new Resolution(current, sources);
	}

	/**
	 * Refuses one version value after looking up its references, unless it is still
	 * taken from elsewhere.
	 *
	 * @param buildFile   the file that names it.
	 * @param version     the value as written.
	 * @param definitions the values the exercise sets.
	 * @throws SecurityException for a version older than 5.12 or not a number
	 */
	private static void requireSupported(@Nonnull Path buildFile, @Nonnull String version,
			@Nonnull Map<String, Definition> definitions) {
		Resolution resolution = resolved(version, definitions);
		String value = resolution.value();
		if (isTakenFromElsewhere(value)) {
			return;
		}
		if (!VERSION_NUMBER.matcher(value).matches()) {
			throw new SecurityException(
					Messages.localized("security.writer.junit.version.unreadable", buildFile.toString(), value));
		}
		if (!isTooOld(value)) {
			return;
		}
		if (value.equals(version)) {
			throw new SecurityException(Messages.localized("security.writer.junit.version.unsupported", value));
		}
		throw new SecurityException(Messages.localized("security.writer.junit.version.unsupported.indirect",
				buildFile.toString(), version, String.join(", ", resolution.sources()), value));
	}

	/**
	 * Whether a version value points elsewhere: a property anywhere in it, such as
	 * {@code ${junit.version}}, {@code 5.${junit.minor}.0} or
	 * {@code $junitVersion}, or a Maven version range.
	 *
	 * @param version the value as written.
	 * @return true when the build file does not name the number itself
	 */
	private static boolean isTakenFromElsewhere(@Nonnull String version) {
		return version.contains("$") || version.startsWith("[") || version.startsWith("(");
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
