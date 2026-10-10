package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The Java version an exercise's build compiles its sources for, as the
 * language level the test sources are parsed at, so newer syntax such as record
 * patterns parses wherever the exercise's compiler accepts it.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class ExerciseLanguageLevel {

	/** The level used when the build names no version, or one too new to parse. */
	static final LanguageLevel FALLBACK = LanguageLevel.JAVA_25;

	/** The build files a Java version can be set in, below the project root. */
	private static final List<String> BUILD_FILES = List.of("pom.xml", "build.gradle", "build.gradle.kts");

	/**
	 * The settings that name a Java version, most specific first: Maven's release
	 * and source, then Gradle's toolchain, release and source compatibility.
	 */
	private static final List<Pattern> VERSION_SETTINGS = List.of(
			Pattern.compile("<maven\\.compiler\\.release>\\s*(?:1\\.)?(\\d+)\\s*<"),
			Pattern.compile("<release>\\s*(?:1\\.)?(\\d+)\\s*<"),
			Pattern.compile("<maven\\.compiler\\.source>\\s*(?:1\\.)?(\\d+)\\s*<"),
			Pattern.compile("<source>\\s*(?:1\\.)?(\\d+)\\s*<"),
			Pattern.compile("JavaLanguageVersion\\.of\\(\\s*['\"]?(\\d+)"),
			Pattern.compile("options\\.release(?:\\.set\\(|\\s*=)\\s*(\\d+)"),
			Pattern.compile("sourceCompatibility\\s*=\\s*['\"]?(?:JavaVersion\\.VERSION_)?(?:1[._])?(\\d+)"));

	/** Prevents instantiation: this class only reads build files. */
	private ExerciseLanguageLevel() {
		throw new IllegalStateException("ExerciseLanguageLevel is a utility class and should not be instantiated");
	}

	/**
	 * The language level of an exercise: the version its build file names, or
	 * {@link #FALLBACK} when it names none the parser knows.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @return the level to parse its test sources at
	 */
	@Nonnull
	static LanguageLevel of(@Nonnull Path projectRoot) {
		for (Pattern setting : VERSION_SETTINGS) {
			for (String buildFile : BUILD_FILES) {
				Optional<String> version = versionIn(projectRoot.resolve(buildFile), setting);
				if (version.isPresent()) {
					return levelOf(version.get());
				}
			}
		}
		return FALLBACK;
	}

	/**
	 * The parser level of a Java version number.
	 *
	 * @param version the version, such as {@code 21}.
	 * @return its level, or {@link #FALLBACK} when the parser has none for it
	 */
	@Nonnull
	static LanguageLevel levelOf(@Nonnull String version) {
		try {
			return LanguageLevel.valueOf("JAVA_" + version.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException unknown) {
			return FALLBACK;
		}
	}

	/**
	 * The version a build file sets with one setting.
	 *
	 * @param buildFile the build file; may not exist.
	 * @param setting   the setting's pattern, with the version as its group.
	 * @return the version, or empty when the file or the setting is absent
	 * @throws SecurityException naming a build file that cannot be read
	 */
	@Nonnull
	private static Optional<String> versionIn(@Nonnull Path buildFile, @Nonnull Pattern setting) {
		if (!Files.isRegularFile(buildFile)) {
			return Optional.empty();
		}
		try {
			Matcher matcher = setting.matcher(Files.readString(buildFile));
			return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
		} catch (IOException unreadable) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", buildFile.toString()),
					unreadable);
		}
	}
}
