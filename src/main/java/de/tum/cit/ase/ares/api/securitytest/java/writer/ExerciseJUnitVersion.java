package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The JUnit version an exercise's build file names. JUnit reads the setting
 * that limits which extensions load only from 5.12 on, so the generated hooks
 * are refused for an older version the build names; a version the build does
 * not name directly, such as one from a parent or a catalogue, is accepted.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
final class ExerciseJUnitVersion {

	/** The oldest JUnit major version that reads the include filter. */
	private static final int MINIMUM_MAJOR = 5;

	/** The oldest minor version of {@link #MINIMUM_MAJOR} that reads it. */
	private static final int MINIMUM_MINOR = 12;

	/** The build files a JUnit version can be set in, below the project root. */
	private static final List<String> BUILD_FILES = List.of("pom.xml", "build.gradle", "build.gradle.kts");

	/**
	 * The ways a build file names a JUnit Jupiter or JUnit BOM version: Maven's
	 * dependency entry, then Gradle's coordinate text.
	 */
	private static final List<Pattern> VERSION_SETTINGS = List.of(Pattern.compile(
			"<artifactId>\\s*junit-(?:jupiter[\\w-]*|bom)\\s*</artifactId>\\s*<version>\\s*(\\d+(?:\\.\\d+)*)[^<]*</version>"),
			Pattern.compile("org\\.junit(?:\\.jupiter)?:junit-(?:jupiter[\\w-]*|bom):(\\d+(?:\\.\\d+)*)"));

	/** Prevents instantiation: this class only reads build files. */
	private ExerciseJUnitVersion() {
		throw new IllegalStateException("ExerciseJUnitVersion is a utility class and should not be instantiated");
	}

	/**
	 * Refuses an exercise whose build file names a JUnit version older than 5.12.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @throws SecurityException naming the version, or a build file that cannot be
	 *                           read
	 */
	static void requireIncludeFilterSupport(@Nonnull Path projectRoot) {
		for (String buildFile : BUILD_FILES) {
			String text = read(projectRoot.resolve(buildFile));
			for (Pattern setting : VERSION_SETTINGS) {
				Matcher matcher = setting.matcher(text);
				while (matcher.find()) {
					if (isTooOld(matcher.group(1))) {
						throw new SecurityException(
								Messages.localized("security.writer.junit.version.unsupported", matcher.group(1)));
					}
				}
			}
		}
	}

	/**
	 * Whether a version number is older than 5.12.
	 *
	 * @param version the version, such as {@code 5.11.4}.
	 * @return true when JUnit ignores the include filter at that version; false for
	 *         a number too long to read, which no JUnit release has
	 */
	static boolean isTooOld(@Nonnull String version) {
		String[] parts = version.split("\\.");
		try {
			int major = Integer.parseInt(parts[0]);
			int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
			return major < MINIMUM_MAJOR || major == MINIMUM_MAJOR && minor < MINIMUM_MINOR;
		} catch (NumberFormatException tooLong) {
			return false;
		}
	}

	/**
	 * A build file's text.
	 *
	 * @param buildFile the build file; may not exist.
	 * @return its text, or empty when it does not exist
	 * @throws SecurityException naming a build file that cannot be read
	 */
	@Nonnull
	private static String read(@Nonnull Path buildFile) {
		if (!Files.isRegularFile(buildFile)) {
			return "";
		}
		try {
			return Files.readString(buildFile);
		} catch (IOException unreadable) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", buildFile.toString()),
					unreadable);
		}
	}
}
