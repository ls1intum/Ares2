package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes, or removes again, the failure-reporting hooks of a precompile run:
 * the hook and sentinel sources, the service-file entries that let JUnit and
 * jqwik load the hooks by themselves, and the JUnit settings that restrict that
 * loading to the generated hook. Lines it adds sit between marker comments, so
 * an instructor's own lines are never touched.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class FailureReportingWriter {

	/** First line of every block this writer adds to a shared file. */
	static final String BLOCK_BEGIN = "# BEGIN Ares generated failure reporting (regenerate instead of editing)";

	/** Last line of every block this writer adds to a shared file. */
	static final String BLOCK_END = "# END Ares generated failure reporting";

	/** Service file JUnit Jupiter reads to load extensions by itself. */
	static final String JUPITER_SERVICE_FILE = "META-INF/services/org.junit.jupiter.api.extension.Extension";

	/** Service file jqwik reads to load lifecycle hooks by itself. */
	static final String JQWIK_SERVICE_FILE = "META-INF/services/net.jqwik.api.lifecycle.LifecycleHook";

	/** JUnit's settings file at the root of the test resources. */
	static final String PLATFORM_PROPERTIES = "junit-platform.properties";

	/** Where Gradle copies test resources, relative to the project root. */
	private static final String GRADLE_TEST_RESOURCES_OUTPUT = "build/resources/test";

	/** JUnit setting that switches on loading extensions from service files. */
	static final String AUTODETECTION_ENABLED = "junit.jupiter.extensions.autodetection.enabled";

	/** JUnit setting naming the only extensions that loading may pick up. */
	static final String AUTODETECTION_INCLUDE = "junit.jupiter.extensions.autodetection.include";

	/** JUnit setting naming extensions that loading must skip. */
	static final String AUTODETECTION_EXCLUDE = "junit.jupiter.extensions.autodetection.exclude";

	/** The project root, whose build file tells whether jqwik is used. */
	@Nonnull
	private final Path projectRoot;

	/** The discovered build layout, or null when only the project root is known. */
	@Nullable
	private final BuildToolConfiguration buildConfiguration;

	/** Confines every path this writer touches to the project. */
	@Nonnull
	private final UnaryOperator<Path> confine;

	/**
	 * Creates a writer for one project.
	 *
	 * @param projectRoot        the project root; must not be null.
	 * @param buildConfiguration the discovered build layout, or null.
	 * @param confine            confines a path to the project; must not be null.
	 */
	FailureReportingWriter(@Nonnull Path projectRoot, @Nullable BuildToolConfiguration buildConfiguration,
			@Nonnull UnaryOperator<Path> confine) {
		this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot must not be null");
		this.buildConfiguration = buildConfiguration;
		this.confine = Objects.requireNonNull(confine, "confine must not be null");
	}

	/**
	 * Writes the hooks when the policy switches privileged-exceptions-only
	 * reporting on, and removes everything an earlier run wrote otherwise.
	 *
	 * @param configuration  the policy's behaviour configuration; must not be null.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root; must not be null.
	 * @param resourcesPath  the test resources root; must not be null.
	 * @return the written files, or an empty list when nothing was written
	 */
	@Nonnull
	List<Path> write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath, @Nonnull Path resourcesPath) {
		if (!isEnabled(configuration)) {
			removeAll(testFolderPath, resourcesPath);
			return List.of();
		}
		boolean jqwik = usesJqwik();
		Path properties = confine.apply(resourcesPath.resolve(PLATFORM_PROPERTIES));
		Path jupiterServices = confine.apply(resourcesPath.resolve(JUPITER_SERVICE_FILE));
		List<String> instructorExtensions = linesOutsideBlock(jupiterServices).stream().map(String::strip)
				.filter(line -> !line.isEmpty() && !line.startsWith("#")).toList();
		requireNoConflictingSetting(properties);
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = new ArrayList<>();
		written.add(writeSource(testFolderPath, FailureReportingSources.JUPITER_HOOK,
				FailureReportingSources.jupiterHook(messagesClass)));
		written.add(writeSource(testFolderPath, FailureReportingSources.JUPITER_SENTINEL,
				FailureReportingSources.jupiterSentinel(messagesClass, jqwik)));
		written.add(writeBlock(jupiterServices, List.of(qualified(FailureReportingSources.JUPITER_HOOK))));
		written.add(writeBlock(properties, List.of(AUTODETECTION_ENABLED + "=true",
				AUTODETECTION_INCLUDE + "=" + includeValue(instructorExtensions))));
		if (jqwik) {
			written.add(writeSource(testFolderPath, FailureReportingSources.JQWIK_HOOK,
					FailureReportingSources.jqwikHook(messagesClass)));
			written.add(writeSource(testFolderPath, FailureReportingSources.JQWIK_SENTINEL,
					FailureReportingSources.jqwikSentinel(messagesClass)));
			written.add(writeBlock(confine.apply(resourcesPath.resolve(JQWIK_SERVICE_FILE)),
					List.of(qualified(FailureReportingSources.JQWIK_HOOK))));
		} else {
			removeJqwik(testFolderPath, resourcesPath);
		}
		return written;
	}

	/**
	 * Whether the policy switches privileged-exceptions-only reporting on.
	 *
	 * @param configuration the policy's behaviour configuration.
	 * @return true only for a present category set to true
	 */
	private static boolean isEnabled(@Nonnull TestBehaviorConfiguration configuration) {
		PrivilegedExceptionsConfiguration category = configuration.regardingPrivilegedExceptions();
		return category != null && category.onlyPrivilegedExceptionsAreReported();
	}

	/**
	 * Whether the exercise's build file or Gradle version catalogue names jqwik's
	 * group id, so the jqwik hook compiles.
	 *
	 * @return true when any of them names {@code net.jqwik}
	 */
	private boolean usesJqwik() {
		return Stream.of("pom.xml", "build.gradle", "build.gradle.kts", "gradle/libs.versions.toml")
				.map(projectRoot::resolve).filter(Files::isRegularFile).anyMatch(FailureReportingWriter::mentionsJqwik);
	}

	/**
	 * Whether a build file names jqwik's group id. A miss, such as jqwik from a
	 * parent POM, is caught by the generated JUnit sentinel instead.
	 *
	 * @param buildFile the build file to read.
	 * @return true when the file contains "net.jqwik"
	 */
	private static boolean mentionsJqwik(@Nonnull Path buildFile) {
		try {
			return Files.readString(buildFile).contains("net.jqwik");
		} catch (IOException failure) {
			throw new SecurityException(
					Messages.localized("security.writer.failure.reporting.io", buildFile.toString()), failure);
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
		for (String line : linesOutsideBlock(properties)) {
			String stripped = line.strip();
			int separator = stripped.indexOf('=');
			if (stripped.startsWith("#") || separator < 0) {
				continue;
			}
			String key = stripped.substring(0, separator).strip();
			String value = stripped.substring(separator + 1).strip();
			boolean conflicting = AUTODETECTION_INCLUDE.equals(key) || AUTODETECTION_EXCLUDE.equals(key)
					|| AUTODETECTION_ENABLED.equals(key) && !"true".equals(value);
			if (conflicting) {
				throw new SecurityException(
						Messages.localized("security.writer.failure.reporting.conflict", properties.toString(), key));
			}
		}
	}

	/**
	 * The include setting: the generated extension plus the instructor's own.
	 *
	 * @param instructorExtensions extensions the instructor already registered.
	 * @return the comma-separated include value
	 */
	@Nonnull
	private static String includeValue(@Nonnull List<String> instructorExtensions) {
		return Stream.concat(Stream.of(qualified(FailureReportingSources.JUPITER_HOOK)), instructorExtensions.stream())
				.distinct().reduce((left, right) -> left + "," + right).orElseThrow();
	}

	/**
	 * Removes everything an earlier run wrote: sources, compiled classes and
	 * blocks.
	 *
	 * @param testFolderPath the test source root.
	 * @param resourcesPath  the test resources root.
	 */
	private void removeAll(@Nonnull Path testFolderPath, @Nonnull Path resourcesPath) {
		deleteGenerated(testFolderPath, FailureReportingSources.JUPITER_HOOK);
		deleteGenerated(testFolderPath, FailureReportingSources.JUPITER_SENTINEL);
		removeGeneratedResource(resourcesPath, JUPITER_SERVICE_FILE);
		removeGeneratedResource(resourcesPath, PLATFORM_PROPERTIES);
		removeJqwik(testFolderPath, resourcesPath);
	}

	/**
	 * Removes the jqwik hook, its sentinel and its service-file block.
	 *
	 * @param testFolderPath the test source root.
	 * @param resourcesPath  the test resources root.
	 */
	private void removeJqwik(@Nonnull Path testFolderPath, @Nonnull Path resourcesPath) {
		deleteGenerated(testFolderPath, FailureReportingSources.JQWIK_HOOK);
		deleteGenerated(testFolderPath, FailureReportingSources.JQWIK_SENTINEL);
		removeGeneratedResource(resourcesPath, JQWIK_SERVICE_FILE);
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
	 * Writes one generated source file into the reserved package.
	 *
	 * @param testFolderPath the test source root.
	 * @param simpleName     the class's simple name.
	 * @param source         the source text.
	 * @return the written file
	 */
	@Nonnull
	private Path writeSource(@Nonnull Path testFolderPath, @Nonnull String simpleName, @Nonnull String source) {
		Path target = sourcePath(testFolderPath, simpleName);
		writeFile(target, source);
		return target;
	}

	/**
	 * Deletes a generated class's source and, when the build layout is known, its
	 * compiled class, so a removed hook cannot stay in force.
	 *
	 * @param testFolderPath the test source root.
	 * @param simpleName     the class's simple name.
	 */
	private void deleteGenerated(@Nonnull Path testFolderPath, @Nonnull String simpleName) {
		deleteIfPresent(sourcePath(testFolderPath, simpleName));
		if (buildConfiguration != null) {
			deleteIfPresent(confine.apply(buildConfiguration.testOutputRoot()
					.resolve(FailureReportingSources.GENERATED_PACKAGE.replace('.', '/'))
					.resolve(simpleName + ".class")));
		}
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
		return confine.apply(testFolderPath.resolve(FailureReportingSources.GENERATED_PACKAGE.replace('.', '/'))
				.resolve(simpleName + ".java"));
	}

	/**
	 * A generated class's fully qualified name.
	 *
	 * @param simpleName the class's simple name.
	 * @return the qualified name
	 */
	@Nonnull
	private static String qualified(@Nonnull String simpleName) {
		return FailureReportingSources.GENERATED_PACKAGE + "." + simpleName;
	}

	/**
	 * Replaces this writer's block in a shared file, keeping every other line.
	 *
	 * @param file  the shared file.
	 * @param lines the block's content lines.
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
	 * Removes this writer's block from a shared file, deleting the file when
	 * nothing of the instructor's remains.
	 *
	 * @param file the shared file.
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
	 * The lines of a file that lie outside this writer's block.
	 *
	 * @param file the file, which may not exist.
	 * @return the lines outside the block, or none for a missing file
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
	 * Reads a file's lines.
	 *
	 * @param file the file.
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
			throw new SecurityException(Messages.localized("security.writer.failure.reporting.io", file.toString()),
					failure);
		}
	}

	/**
	 * Writes a file, creating its folder.
	 *
	 * @param file    the file.
	 * @param content the full content.
	 */
	private static void writeFile(@Nonnull Path file, @Nonnull String content) {
		try {
			Files.createDirectories(Objects.requireNonNull(file.getParent(), "file has no parent: " + file));
			Files.writeString(file, content);
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.failure.reporting.io", file.toString()),
					failure);
		}
	}

	/**
	 * Deletes a file if it exists. Failing to delete stops generation, since the
	 * old file would otherwise stay in force.
	 *
	 * @param file the file.
	 */
	private static void deleteIfPresent(@Nonnull Path file) {
		try {
			Files.deleteIfExists(file);
		} catch (IOException failure) {
			throw new SecurityException(Messages.localized("security.writer.failure.reporting.io", file.toString()),
					failure);
		}
	}
}
