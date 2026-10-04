package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The files every generated precompile hook shares: its source in the reserved
 * package, and its registration with JUnit and jqwik. A feature writer
 * contributes hook names; the registration is written once for all of them,
 * since JUnit reads its include filter as one setting.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class GeneratedHookFiles {

	/** The reserved package every generated class lives in. */
	static final String GENERATED_PACKAGE = "de.tum.cit.ase.ares.generated";

	/** First line of the block the generator owns in a shared file. */
	static final String BLOCK_BEGIN = "# BEGIN Ares generated hooks (regenerate instead of editing)";

	/** Last line of the block the generator owns in a shared file. */
	static final String BLOCK_END = "# END Ares generated hooks";

	/** Where JUnit finds auto-detected extensions, below the test resources. */
	static final String JUPITER_SERVICE_FILE = "META-INF/services/org.junit.jupiter.api.extension.Extension";

	/** Where jqwik finds global lifecycle hooks, below the test resources. */
	static final String JQWIK_SERVICE_FILE = "META-INF/services/net.jqwik.api.lifecycle.LifecycleHook";

	/** JUnit's settings file at the root of the test resources. */
	static final String PLATFORM_PROPERTIES = "junit-platform.properties";

	/** Where Gradle copies test resources, relative to the project root. */
	private static final String GRADLE_TEST_RESOURCES_OUTPUT = "build/resources/test";

	/** JUnit's switch for loading extensions from service files. */
	static final String AUTODETECTION_ENABLED = "junit.jupiter.extensions.autodetection.enabled";

	/** JUnit's filter of which auto-detected extensions may load. */
	static final String AUTODETECTION_INCLUDE = "junit.jupiter.extensions.autodetection.include";

	/** JUnit's filter of which auto-detected extensions may not load. */
	static final String AUTODETECTION_EXCLUDE = "junit.jupiter.extensions.autodetection.exclude";

	/** Any of JUnit's auto-detection settings, as a build file would name it. */
	private static final Pattern AUTODETECTION_SETTING = Pattern
			.compile("junit\\.jupiter\\.extensions\\.autodetection\\.[A-Za-z]+");

	/** The build files a setting for the test run can live in, below the root. */
	private static final List<String> BUILD_FILES = List.of("pom.xml", "build.gradle", "build.gradle.kts");

	/** The exercise's root folder. */
	@Nonnull
	private final Path projectRoot;

	/** The exercise's build layout, or null when unknown. */
	@Nullable
	private final BuildToolConfiguration buildConfiguration;

	/** Keeps every path this class writes inside the project. */
	@Nonnull
	private final UnaryOperator<Path> confine;

	/**
	 * What one feature generated: its written files and the hooks it asks JUnit and
	 * jqwik to load, by simple name.
	 *
	 * @param written      the files the feature wrote.
	 * @param jupiterHooks its JUnit extensions; empty when none.
	 * @param jqwikHooks   its jqwik hooks; empty when none.
	 */
	record Contribution(@Nonnull List<Path> written, @Nonnull List<String> jupiterHooks,
			@Nonnull List<String> jqwikHooks) {

		/** A feature that generated nothing. */
		static final Contribution NONE = new Contribution(List.of(), List.of(), List.of());

		/**
		 * This contribution followed by another.
		 *
		 * @param other the other feature's contribution.
		 * @return both together
		 */
		@Nonnull
		Contribution and(@Nonnull Contribution other) {
			return new Contribution(Stream.concat(written.stream(), other.written.stream()).toList(),
					Stream.concat(jupiterHooks.stream(), other.jupiterHooks.stream()).toList(),
					Stream.concat(jqwikHooks.stream(), other.jqwikHooks.stream()).toList());
		}
	}

	/**
	 * Creates the shared file handling for one exercise.
	 *
	 * @param projectRoot        the exercise's root folder.
	 * @param buildConfiguration the build layout, or null when unknown.
	 * @param confine            keeps a path inside the project or throws.
	 */
	GeneratedHookFiles(@Nonnull Path projectRoot, @Nullable BuildToolConfiguration buildConfiguration,
			@Nonnull UnaryOperator<Path> confine) {
		this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot must not be null");
		this.buildConfiguration = buildConfiguration;
		this.confine = Objects.requireNonNull(confine, "confine must not be null");
	}

	/**
	 * Registers every contributed hook with JUnit and jqwik, or removes the
	 * registration where nothing is contributed. The instructor's own lines stay.
	 *
	 * @param resourcesPath the test resources root.
	 * @param contribution  what every feature generated together.
	 * @return the registration files written
	 * @throws SecurityException naming file and key when the instructor's JUnit
	 *                           settings contradict the registration
	 */
	@Nonnull
	List<Path> register(@Nonnull Path resourcesPath, @Nonnull Contribution contribution) {
		List<Path> written = new ArrayList<>();
		Path properties = confine.apply(resourcesPath.resolve(PLATFORM_PROPERTIES));
		Path jupiterServices = confine.apply(resourcesPath.resolve(JUPITER_SERVICE_FILE));
		if (contribution.jupiterHooks().isEmpty()) {
			removeGeneratedResource(resourcesPath, JUPITER_SERVICE_FILE);
			removeGeneratedResource(resourcesPath, PLATFORM_PROPERTIES);
		} else {
			List<String> instructorExtensions = linesOutsideBlock(jupiterServices).stream()
					.map(GeneratedHookFiles::providerName).filter(name -> !name.isEmpty()).toList();
			requireNoConflictingSetting(properties);
			requireNoAutodetectionSettingInBuildFiles();
			List<String> generated = contribution.jupiterHooks().stream().map(GeneratedHookFiles::qualified).toList();
			written.add(writeBlock(jupiterServices, generated));
			written.add(writeBlock(properties, autodetectionSettings(properties, generated, instructorExtensions)));
		}
		if (contribution.jqwikHooks().isEmpty()) {
			removeGeneratedResource(resourcesPath, JQWIK_SERVICE_FILE);
		} else {
			written.add(writeBlock(confine.apply(resourcesPath.resolve(JQWIK_SERVICE_FILE)),
					contribution.jqwikHooks().stream().map(GeneratedHookFiles::qualified).toList()));
		}
		return written;
	}

	/**
	 * Whether the exercise's build file or Gradle version catalogue names jqwik's
	 * group id, so a jqwik hook compiles.
	 *
	 * @return true when any of them names {@code net.jqwik}
	 */
	boolean usesJqwik() {
		return Stream.concat(BUILD_FILES.stream(), Stream.of("gradle/libs.versions.toml")).map(projectRoot::resolve)
				.filter(Files::isRegularFile).anyMatch(GeneratedHookFiles::mentionsJqwik);
	}

	/**
	 * Writes one generated source file into the reserved package.
	 *
	 * @param testFolderPath the test source root.
	 * @param simpleName     the class's simple name.
	 * @param source         the source text.
	 * @return the written file
	 */
	@Nonnull
	Path writeSource(@Nonnull Path testFolderPath, @Nonnull String simpleName, @Nonnull String source) {
		Path target = sourcePath(testFolderPath, simpleName);
		writeFile(target, source);
		return target;
	}

	/**
	 * Deletes a generated class's source and, when the build layout is known, its
	 * compiled class in the test output.
	 *
	 * @param testFolderPath the test source root.
	 * @param simpleName     the class's simple name.
	 */
	void deleteGenerated(@Nonnull Path testFolderPath, @Nonnull String simpleName) {
		deleteIfPresent(sourcePath(testFolderPath, simpleName));
		if (buildConfiguration != null) {
			deleteIfPresent(confine.apply(buildConfiguration.testOutputRoot()
					.resolve(GENERATED_PACKAGE.replace('.', '/')).resolve(simpleName + ".class")));
		}
	}

	/**
	 * The fully qualified name of a generated class.
	 *
	 * @param simpleName the class's simple name.
	 * @return its name in the reserved package
	 */
	@Nonnull
	static String qualified(@Nonnull String simpleName) {
		return GENERATED_PACKAGE + "." + simpleName;
	}

	/**
	 * The auto-detection settings of the generated block. When the instructor
	 * already switched auto-detection on, every provider on the test class path
	 * loaded before, those registered inside dependency JARs included, so no
	 * include filter narrows that. Otherwise the filter admits the generated
	 * extensions and the instructor's own, keeping every other provider off as
	 * before.
	 *
	 * @param properties           the JUnit settings file.
	 * @param generated            the generated extensions' names.
	 * @param instructorExtensions extensions the instructor registered.
	 * @return the block's lines
	 */
	@Nonnull
	private static List<String> autodetectionSettings(@Nonnull Path properties, @Nonnull List<String> generated,
			@Nonnull List<String> instructorExtensions) {
		String enabled = AUTODETECTION_ENABLED + "=true";
		if (instructorEnablesAutodetection(properties)) {
			return List.of(enabled);
		}
		return List.of(enabled, AUTODETECTION_INCLUDE + "=" + includeValue(generated, instructorExtensions));
	}

	/**
	 * Whether the instructor's own JUnit settings switch auto-detection on.
	 *
	 * @param properties the JUnit settings file.
	 * @return true when they set it to {@code true}
	 */
	private static boolean instructorEnablesAutodetection(@Nonnull Path properties) {
		return "true".equalsIgnoreCase(instructorSettings(properties).getProperty(AUTODETECTION_ENABLED, "").strip());
	}

	/**
	 * Refuses to continue when a build file sets one of JUnit's auto-detection
	 * settings, since such a setting overrides the generated one and cannot be read
	 * from here, so the generator could not tell which extensions load.
	 *
	 * @throws SecurityException naming the build file and the setting
	 */
	private void requireNoAutodetectionSettingInBuildFiles() {
		for (String buildFile : BUILD_FILES) {
			Path file = projectRoot.resolve(buildFile);
			Optional<String> setting = readLines(file).map(lines -> String.join("\n", lines))
					.flatMap(GeneratedHookFiles::autodetectionSetting);
			if (setting.isPresent()) {
				throw new SecurityException(Messages.localized("security.writer.generated.hooks.build.conflict",
						file.toString(), setting.get()));
			}
		}
	}

	/**
	 * The first JUnit auto-detection setting a text names.
	 *
	 * @param text the text to search.
	 * @return the setting's key, or empty when it names none
	 */
	@Nonnull
	private static Optional<String> autodetectionSetting(@Nonnull String text) {
		Matcher matcher = AUTODETECTION_SETTING.matcher(text);
		return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
	}

	/**
	 * Whether a build file names jqwik's group id.
	 *
	 * @param buildFile the build file to read.
	 * @return true when the file contains "net.jqwik"
	 */
	private static boolean mentionsJqwik(@Nonnull Path buildFile) {
		try {
			return Files.readString(buildFile).contains("net.jqwik");
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", buildFile.toString()),
					failure);
		}
	}

	/**
	 * Refuses to continue when the instructor's JUnit settings set auto-detection
	 * differently, since writing ours would silently override theirs.
	 *
	 * @param properties the JUnit settings file.
	 * @throws SecurityException naming the file and the conflicting setting
	 */
	private static void requireNoConflictingSetting(@Nonnull Path properties) {
		Properties instructorSettings = instructorSettings(properties);
		for (String key : instructorSettings.stringPropertyNames()) {
			String value = instructorSettings.getProperty(key).strip();
			boolean conflicting = AUTODETECTION_INCLUDE.equals(key) || AUTODETECTION_EXCLUDE.equals(key)
					|| AUTODETECTION_ENABLED.equals(key) && !"true".equalsIgnoreCase(value);
			if (conflicting) {
				throw new SecurityException(
						Messages.localized("security.writer.generated.hooks.conflict", properties.toString(), key));
			}
		}
	}

	/**
	 * The instructor's JUnit settings, read the way JUnit reads them, so every
	 * separator, escape and continuation line counts.
	 *
	 * @param properties the JUnit settings file.
	 * @return the settings outside the generated block
	 */
	@Nonnull
	private static Properties instructorSettings(@Nonnull Path properties) {
		Properties settings = new Properties();
		try {
			settings.load(new StringReader(String.join("\n", linesOutsideBlock(properties))));
		} catch (IOException | IllegalArgumentException failure) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", properties.toString()),
					failure);
		}
		return settings;
	}

	/**
	 * The provider class a service-file line names, without a trailing comment, the
	 * way {@code ServiceLoader} reads it.
	 *
	 * @param line one line of a service file.
	 * @return the class name, or an empty string for a blank or comment line
	 */
	@Nonnull
	private static String providerName(@Nonnull String line) {
		int comment = line.indexOf('#');
		return (comment < 0 ? line : line.substring(0, comment)).strip();
	}

	/**
	 * The include filter: the generated extensions first, then the instructor's
	 * own, so both keep loading.
	 *
	 * @param generated            the generated extensions' names.
	 * @param instructorExtensions extensions the instructor registered.
	 * @return the comma-separated filter value
	 */
	@Nonnull
	private static String includeValue(@Nonnull List<String> generated, @Nonnull List<String> instructorExtensions) {
		return Stream.concat(generated.stream(), instructorExtensions.stream()).distinct()
				.reduce((left, right) -> left + "," + right).orElseThrow();
	}

	/**
	 * Removes the generated block from a test resource and from the copies an
	 * earlier build left in the test output, so a stale copy cannot name a hook
	 * that no longer exists.
	 *
	 * @param resourcesPath the test resources root.
	 * @param relativePath  the resource's path below that root.
	 */
	private void removeGeneratedResource(@Nonnull Path resourcesPath, @Nonnull String relativePath) {
		removeBlock(confine.apply(resourcesPath.resolve(relativePath)));
		for (Path copyRoot : resourceCopyRoots()) {
			removeBlock(confine.apply(copyRoot.resolve(relativePath)));
		}
	}

	/**
	 * Where the build copies test resources: Maven's test output, or Gradle's test
	 * resources output. Empty when the build layout is unknown.
	 *
	 * @return the folders holding copied test resources
	 */
	@Nonnull
	private List<Path> resourceCopyRoots() {
		if (buildConfiguration == null) {
			return List.of();
		}
		return switch (buildConfiguration.buildMode()) {
		case MAVEN -> List.of(buildConfiguration.testOutputRoot());
		case GRADLE -> List.of(buildConfiguration.projectRoot().resolve(GRADLE_TEST_RESOURCES_OUTPUT));
		};
	}

	/**
	 * Where a generated class's source belongs.
	 *
	 * @param testFolderPath the test source root.
	 * @param simpleName     the class's simple name.
	 * @return the confined source path
	 */
	@Nonnull
	private Path sourcePath(@Nonnull Path testFolderPath, @Nonnull String simpleName) {
		return confine.apply(testFolderPath.resolve(GENERATED_PACKAGE.replace('.', '/')).resolve(simpleName + ".java"));
	}

	/**
	 * Writes the generated block into a file, keeping every line outside it.
	 *
	 * @param file  the file to write.
	 * @param lines the block's content.
	 * @return the file
	 */
	@Nonnull
	private static Path writeBlock(@Nonnull Path file, @Nonnull List<String> lines) {
		List<String> content = new ArrayList<>(linesOutsideBlock(file));
		while (!content.isEmpty() && content.get(content.size() - 1).isBlank()) {
			content.remove(content.size() - 1);
		}
		if (!content.isEmpty()) {
			content.add("");
		}
		content.add(BLOCK_BEGIN);
		content.addAll(lines);
		content.add(BLOCK_END);
		writeFile(file, String.join(System.lineSeparator(), content) + System.lineSeparator());
		return file;
	}

	/**
	 * Removes the generated block from a file, deleting the file when nothing of
	 * the instructor's remains.
	 *
	 * @param file the file to clean.
	 */
	private static void removeBlock(@Nonnull Path file) {
		if (!Files.isRegularFile(file)) {
			return;
		}
		List<String> remaining = linesOutsideBlock(file);
		if (remaining.stream().allMatch(String::isBlank)) {
			deleteIfPresent(file);
			return;
		}
		writeFile(file, String.join(System.lineSeparator(), remaining).strip() + System.lineSeparator());
	}

	/**
	 * The lines of a file outside the generated block.
	 *
	 * @param file the file to read; may not exist.
	 * @return its lines outside the block, empty for a missing file
	 */
	@Nonnull
	private static List<String> linesOutsideBlock(@Nonnull Path file) {
		List<String> outside = new ArrayList<>();
		boolean inside = false;
		for (String line : readLines(file).orElse(List.of())) {
			if (BLOCK_BEGIN.equals(line.strip())) {
				inside = true;
			} else if (BLOCK_END.equals(line.strip())) {
				inside = false;
			} else if (!inside) {
				outside.add(line);
			}
		}
		return outside;
	}

	/**
	 * A file's lines, if it exists.
	 *
	 * @param file the file to read.
	 * @return its lines, or empty when it does not exist
	 */
	@Nonnull
	private static Optional<List<String>> readLines(@Nonnull Path file) {
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		try {
			return Optional.of(Files.readAllLines(file));
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", file.toString()),
					failure);
		}
	}

	/**
	 * Writes a file, creating its folders.
	 *
	 * @param file    the file to write.
	 * @param content its content.
	 */
	private static void writeFile(@Nonnull Path file, @Nonnull String content) {
		try {
			Files.createDirectories(Objects.requireNonNull(file.getParent(), "file has no parent: " + file));
			Files.writeString(file, content);
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", file.toString()),
					failure);
		}
	}

	/**
	 * Deletes a file if it exists.
	 *
	 * @param file the file to delete.
	 */
	private static void deleteIfPresent(@Nonnull Path file) {
		try {
			Files.deleteIfExists(file);
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.generated.hooks.io", file.toString()),
					failure);
		}
	}
}
