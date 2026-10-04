package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.HiddenTestsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the hidden-test hook of a precompile run and its sentinel test, only
 * while the policy sets {@code regardingHiddenTests}, and removes them again
 * once it does not. Every hidden-test entry must match a test source.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class HiddenTestsWriter {

	/** The shared generated-file handling of this exercise. */
	@Nonnull
	private final GeneratedHookFiles hookFiles;

	/**
	 * Creates the writer for one exercise.
	 *
	 * @param hookFiles the shared generated-file handling.
	 */
	HiddenTestsWriter(@Nonnull GeneratedHookFiles hookFiles) {
		this.hookFiles = Objects.requireNonNull(hookFiles, "hookFiles must not be null");
	}

	/**
	 * Writes or removes the hidden-test hook for the policy's setting.
	 *
	 * @param configuration  the policy's behaviour configuration.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root.
	 * @return what was written and which hook to register; nothing when the setting
	 *         is absent
	 * @throws SecurityException naming the first entry no test source matches
	 */
	@Nonnull
	GeneratedHookFiles.Contribution write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath) {
		HiddenTestsConfiguration hiddenTests = configuration.regardingHiddenTests();
		if (hiddenTests == null) {
			hookFiles.deleteGenerated(testFolderPath, HiddenTestsSources.JUPITER_HOOK);
			hookFiles.deleteGenerated(testFolderPath, HiddenTestsSources.JUPITER_SENTINEL);
			return GeneratedHookFiles.Contribution.NONE;
		}
		for (String entry : hiddenTests.theFollowingTestsAreHidden()) {
			requireMatch(entry, testFolderPath);
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = List.of(
				hookFiles.writeSource(testFolderPath, HiddenTestsSources.JUPITER_HOOK,
						HiddenTestsSources.jupiterHook(messagesClass)),
				hookFiles.writeSource(testFolderPath, HiddenTestsSources.JUPITER_SENTINEL,
						HiddenTestsSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(HiddenTestsSources.JUPITER_HOOK), List.of());
	}

	/**
	 * Fails unless an entry names a class in the test sources and, if it names a
	 * method, one declared in that class's source file.
	 *
	 * @param entry          the entry, {@code pkg.Class} or {@code pkg.Class#m}.
	 * @param testFolderPath the test source root.
	 * @throws SecurityException naming the entry
	 */
	private static void requireMatch(@Nonnull String entry, @Nonnull Path testFolderPath) {
		int hash = entry.indexOf('#');
		List<String> segments = Arrays.asList((hash < 0 ? entry : entry.substring(0, hash)).split("\\."));
		Optional<String> source = classSource(segments, testFolderPath);
		boolean matches = source.isPresent()
				&& (hash < 0 || declares(source.get(), "\\b" + Pattern.quote(entry.substring(hash + 1)) + "\\s*\\("));
		if (!matches) {
			throw new SecurityException(Messages.localized("security.writer.hidden.tests.unmatched", entry));
		}
	}

	/**
	 * The source of the file declaring a class given by its canonical name: the
	 * shortest prefix that is a file, with every later segment declared in it as a
	 * nested class.
	 *
	 * @param segments       the name's dot-separated segments.
	 * @param testFolderPath the test source root.
	 * @return the file's text, or empty when no file and nesting match
	 */
	@Nonnull
	private static Optional<String> classSource(@Nonnull List<String> segments, @Nonnull Path testFolderPath) {
		for (int fileSegment = 0; fileSegment < segments.size(); fileSegment++) {
			Path file = testFolderPath.resolve(String.join("/", segments.subList(0, fileSegment + 1)) + ".java");
			if (Files.isRegularFile(file)) {
				String text = read(file);
				boolean nestedDeclared = segments.subList(fileSegment + 1, segments.size()).stream()
						.allMatch(nested -> declares(text,
								"\\b(?:class|interface|record|enum)\\s+" + Pattern.quote(nested) + "\\b"));
				return nestedDeclared ? Optional.of(text) : Optional.empty();
			}
		}
		return Optional.empty();
	}

	/**
	 * Whether a source text contains a declaration matching a pattern.
	 *
	 * @param text    the source text.
	 * @param pattern the declaration's pattern.
	 * @return true when it does
	 */
	private static boolean declares(@Nonnull String text, @Nonnull String pattern) {
		return Pattern.compile(pattern).matcher(text).find();
	}

	/**
	 * Reads a test source.
	 *
	 * @param file the file.
	 * @return its text
	 * @throws SecurityException if it cannot be read
	 */
	@Nonnull
	private static String read(@Nonnull Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException unreadable) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", file.toString()),
					unreadable);
		}
	}
}
