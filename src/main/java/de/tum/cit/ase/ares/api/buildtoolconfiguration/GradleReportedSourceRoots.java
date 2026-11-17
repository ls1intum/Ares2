package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.util.StartUpProperties;

/**
 * The source and output folders Gradle reports for this test run, through the
 * JVM properties that {@code GradleSourceRoots.gradle} gives every Test task.
 * Ares reads them from the JVM's start-up values, which setting a system
 * property does not change, and trusts them only for the project they were
 * reported for.
 */
@API(status = Status.INTERNAL)
public final class GradleReportedSourceRoots {

	/** The prefix of every property the snippet writes. */
	static final String PREFIX = "ares.gradle.";

	/** The one report version this reader understands. */
	static final String VERSION = "1";

	/** What a report says, or why there is none Ares may use. */
	public enum Outcome {
		/** The JVM was started without a report. */
		ABSENT,
		/** The JVM's start-up values could not be read. */
		UNREADABLE,
		/** Gradle could not say which folders it compiles and runs, and said why. */
		PROBLEM,
		/** A report is there, but incomplete, unknown or not decodable. */
		MALFORMED,
		/** A valid report, but for another project than the one Ares checks. */
		FOREIGN,
		/** A valid report for this project. */
		VALID
	}

	/**
	 * One reading of the report.
	 *
	 * @param outcome     what the report says
	 * @param detail      the reason or the problem, empty where valid
	 * @param mainSources the main Java source folders that exist
	 * @param mainOutput  the folder main is compiled into, or null
	 * @param testSources the test set's Java source folders that exist
	 * @param testOutput  the folder the test set is compiled into, or null
	 */
	public record Report(@Nonnull Outcome outcome, @Nonnull String detail, @Nonnull List<Path> mainSources,
			@Nullable Path mainOutput, @Nonnull List<Path> testSources, @Nullable Path testOutput) {

		/** Whether Ares may take the folders of this report. */
		public boolean isValid() {
			return outcome == Outcome.VALID;
		}

		/**
		 * Refuses a run without a policy, which takes its exempt test classes from
		 * these folders, unless the report is valid.
		 *
		 * @param projectRoot the canonical project root
		 * @throws SecurityException naming the cause and the fix, unless valid
		 */
		public void requireValid(@Nonnull Path projectRoot) {
			if (!isValid()) {
				throw new SecurityException(
						Messages.localized("security.gradle.sourceroots." + outcome.name().toLowerCase(Locale.ROOT),
								projectRoot.toString(), detail));
			}
		}

		/**
		 * The build configuration of this report: both source sets and outputs as
		 * Gradle uses them, main known to be complete.
		 *
		 * @param projectRoot the canonical project root
		 * @return the configuration
		 */
		@Nonnull
		public BuildToolConfiguration toConfiguration(@Nonnull Path projectRoot) {
			return new BuildToolConfiguration(BuildMode.GRADLE, projectRoot, mainSources, testSources,
					Objects.requireNonNull(mainOutput), Objects.requireNonNull(testOutput), true);
		}
	}

	/** Not instantiable: it only offers static methods. */
	private GradleReportedSourceRoots() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "GradleReportedSourceRoots"));
	}

	/**
	 * Reads the report this JVM was started with, for this project.
	 *
	 * @param projectRoot the canonical project root
	 * @return the report
	 */
	@Nonnull
	public static Report read(@Nonnull Path projectRoot) {
		StartUpProperties.Snapshot snapshot = StartUpProperties.snapshot();
		if (snapshot.values() == null) {
			return failed(Outcome.UNREADABLE, Objects.requireNonNullElse(snapshot.unreadable(), ""));
		}
		return parse(snapshot.values(), projectRoot);
	}

	/**
	 * Reads a report from these values. Each list is split at commas before its
	 * entries are decoded, so an encoded comma stays inside its path.
	 *
	 * @param values      the start-up values
	 * @param projectRoot the canonical project root
	 * @return the report
	 */
	@Nonnull
	static Report parse(@Nonnull Map<String, String> values, @Nonnull Path projectRoot) {
		if (values.keySet().stream().noneMatch(key -> key.startsWith(PREFIX))) {
			return failed(Outcome.ABSENT, "");
		}
		if (!VERSION.equals(values.get(PREFIX + "sourceRoots.version"))) {
			return failed(Outcome.MALFORMED, "report version " + values.get(PREFIX + "sourceRoots.version"));
		}
		try {
			Path reported = absolute(required(values, "projectDir"));
			if (!BuildToolConfiguration.canonicalise(reported).equals(projectRoot)) {
				return failed(Outcome.FOREIGN, reported.toString());
			}
			if (values.containsKey(PREFIX + "sourceRoots.problem")) {
				return failed(Outcome.PROBLEM, required(values, "sourceRoots.problem"));
			}
			required(values, "testSet");
			return new Report(Outcome.VALID, "", existing(list(values, "mainSourceRoots")),
					absolute(required(values, "mainOutputRoot")), existing(list(values, "testSourceRoots")),
					absolute(required(values, "testOutputRoot")));
		} catch (IllegalArgumentException malformed) {
			return failed(Outcome.MALFORMED, malformed.getMessage());
		}
	}

	/** A report that gives no folders. */
	@Nonnull
	private static Report failed(@Nonnull Outcome outcome, @Nonnull String detail) {
		return new Report(outcome, detail, List.of(), null, List.of(), null);
	}

	/**
	 * One decoded value of the report.
	 *
	 * @throws IllegalArgumentException where it is missing or not decodable
	 */
	@Nonnull
	private static String required(@Nonnull Map<String, String> values, @Nonnull String key) {
		String raw = values.get(PREFIX + key);
		if (raw == null) {
			throw new IllegalArgumentException("missing " + PREFIX + key);
		}
		return decode(raw);
	}

	/**
	 * The decoded paths of one list value, none for an empty value.
	 *
	 * @throws IllegalArgumentException where it is missing or not decodable
	 */
	@Nonnull
	private static List<Path> list(@Nonnull Map<String, String> values, @Nonnull String key) {
		String raw = values.get(PREFIX + key);
		if (raw == null) {
			throw new IllegalArgumentException("missing " + PREFIX + key);
		}
		List<Path> paths = new ArrayList<>();
		for (String entry : raw.isEmpty() ? List.<String>of() : Arrays.asList(raw.split(",", -1))) {
			paths.add(absolute(decode(entry)));
		}
		return paths;
	}

	/**
	 * A value as {@code URLEncoder} wrote it, decoded strictly: only ASCII, every
	 * escape complete, and the bytes valid UTF-8.
	 *
	 * @throws IllegalArgumentException where the value is not such an encoding
	 */
	@Nonnull
	private static String decode(@Nonnull String raw) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (int index = 0; index < raw.length(); index++) {
			char character = raw.charAt(index);
			if (character == '%' && index + 2 < raw.length() && isHex(raw.charAt(index + 1))
					&& isHex(raw.charAt(index + 2))) {
				bytes.write(Integer.parseInt(raw.substring(index + 1, index + 3), 16));
				index += 2;
			} else if (character == '+') {
				bytes.write(' ');
			} else if (character < 0x80 && character != '%') {
				bytes.write(character);
			} else {
				throw new IllegalArgumentException("not percent-encoded: " + raw);
			}
		}
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray()))
					.toString();
		} catch (CharacterCodingException notUtf8) {
			throw new IllegalArgumentException("not UTF-8: " + raw, notUtf8);
		}
	}

	/** Whether this is an ASCII hexadecimal digit. */
	private static boolean isHex(char character) {
		return character >= '0' && character <= '9' || character >= 'a' && character <= 'f'
				|| character >= 'A' && character <= 'F';
	}

	/**
	 * A reported path, which Gradle always writes absolute.
	 *
	 * @throws IllegalArgumentException where it is relative
	 */
	@Nonnull
	private static Path absolute(@Nonnull String value) {
		Path path = Path.of(value);
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException("relative path " + value);
		}
		return path;
	}

	/**
	 * The folders of a list that exist. Gradle compiled exactly the Java files in
	 * the folders that exist, which the snippet checked, so a missing one holds
	 * nothing Ares has to scan.
	 */
	@Nonnull
	private static List<Path> existing(@Nonnull List<Path> folders) {
		return folders.stream().filter(Files::isDirectory).toList();
	}
}
