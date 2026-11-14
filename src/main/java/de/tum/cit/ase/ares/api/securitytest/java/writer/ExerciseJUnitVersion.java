package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The JUnit version an exercise's build file names. JUnit reads the setting
 * that limits which extensions load only from 5.12 on, so the generated hooks
 * are refused for an older version, for a version that is not a number, and for
 * a build file that cannot be read. A version from a parent, a property, a
 * version range or a catalogue is accepted.
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
	 * dependency entry, then Gradle's coordinate text. Each captures the whole
	 * value, so a value that is not a number is seen too.
	 */
	private static final List<Pattern> VERSION_SETTINGS = List.of(Pattern.compile(
			"<artifactId>\\s*junit-(?:jupiter[\\w-]*|bom)\\s*</artifactId>\\s*<version>\\s*([^<]*?)\\s*</version>"),
			Pattern.compile("org\\.junit(?:\\.jupiter)?:junit-(?:jupiter[\\w-]*|bom):([^'\"\\s)@]*)"));

	/**
	 * A version number such as {@code 5.12.2} or {@code 5.13.0-M1}: numeric parts
	 * of at most nine digits, then any qualifier after a dash or a plus.
	 */
	private static final Pattern VERSION_NUMBER = Pattern.compile("\\d{1,9}(?:\\.\\d{1,9})*(?:[-+][\\w.\\-+]*)?");

	/** Prevents instantiation: this class only reads build files. */
	private ExerciseJUnitVersion() {
		throw new IllegalStateException("ExerciseJUnitVersion is a utility class and should not be instantiated");
	}

	/**
	 * Refuses an exercise whose build file names a JUnit version older than 5.12 or
	 * one that is not a number, or whose build file cannot be read.
	 *
	 * @param projectRoot the exercise's root folder.
	 * @throws SecurityException naming the version, or the build file
	 */
	static void requireIncludeFilterSupport(@Nonnull Path projectRoot) {
		for (String buildFile : BUILD_FILES) {
			Path path = projectRoot.resolve(buildFile);
			String text = read(path);
			for (Pattern setting : VERSION_SETTINGS) {
				Matcher matcher = setting.matcher(text);
				while (matcher.find()) {
					requireSupported(path, matcher.group(1));
				}
			}
		}
	}

	/**
	 * Refuses one version value, unless it is taken from elsewhere.
	 *
	 * @param buildFile the build file that names it.
	 * @param version   the value as written.
	 * @throws SecurityException for a version older than 5.12 or not a number
	 */
	private static void requireSupported(@Nonnull Path buildFile, @Nonnull String version) {
		if (isTakenFromElsewhere(version)) {
			return;
		}
		if (!VERSION_NUMBER.matcher(version).matches()) {
			throw new SecurityException(
					Messages.localized("security.writer.junit.version.unreadable", buildFile.toString(), version));
		}
		if (isTooOld(version)) {
			throw new SecurityException(Messages.localized("security.writer.junit.version.unsupported", version));
		}
	}

	/**
	 * Whether a version value points elsewhere: a property such as
	 * {@code ${junit.version}} or {@code $junitVersion}, or a Maven version range.
	 *
	 * @param version the value as written.
	 * @return true when the build file does not name the number itself
	 */
	private static boolean isTakenFromElsewhere(@Nonnull String version) {
		return version.startsWith("$") || version.startsWith("[") || version.startsWith("(");
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
