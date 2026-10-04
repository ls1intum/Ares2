package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Checks the files every generated hook shares: one registration with JUnit and
 * jqwik for all features, merged with the instructor's own entries, refused on
 * conflict, and removed again exactly.
 */
class GeneratedHookFilesTest {

	/** Fully qualified name of a first feature's JUnit hook. */
	private static final String FIRST_HOOK = "de.tum.cit.ase.ares.generated.FirstHook";

	/** Fully qualified name of a second feature's JUnit hook. */
	private static final String SECOND_HOOK = "de.tum.cit.ase.ares.generated.SecondHook";

	/** An extension the instructor registered before generation. */
	private static final String INSTRUCTOR_EXTENSION = "com.example.InstructorExtension";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test resources root. */
	private Path resources;

	/**
	 * Creates the exercise's resources folder and a build without jqwik.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		resources = Files.createDirectories(projectRoot.resolve("src/test/resources"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/**
	 * One feature's JUnit hook is registered, and auto-detection is switched on for
	 * it only.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void registersAJupiterHook() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK);
		assertThat(Files.readString(properties())).contains("junit.jupiter.extensions.autodetection.enabled=true")
				.contains("junit.jupiter.extensions.autodetection.include=" + FIRST_HOOK);
		assertThat(resources.resolve(GeneratedHookFiles.JQWIK_SERVICE_FILE)).doesNotExist();
	}

	/**
	 * Two features share one include filter, since JUnit reads it as one setting.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void twoFeaturesShareOneRegistration() throws IOException {
		GeneratedHookFiles.Contribution both = contribution(List.of("FirstHook"), List.of())
				.and(contribution(List.of("SecondHook"), List.of("SecondJqwikHook")));

		files(null).register(resources, both);

		assertThat(Files.readString(properties())).contains("autodetection.include=" + FIRST_HOOK + "," + SECOND_HOOK);
		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK).contains(SECOND_HOOK);
		assertThat(Files.readString(resources.resolve(GeneratedHookFiles.JQWIK_SERVICE_FILE)))
				.contains("de.tum.cit.ase.ares.generated.SecondJqwikHook");
	}

	/**
	 * Dropping one feature leaves the other's hook registered.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void droppingOneFeatureKeepsTheOther() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook", "SecondHook"), List.of()));

		files(null).register(resources, contribution(List.of("SecondHook"), List.of()));

		assertThat(Files.readString(properties())).contains("autodetection.include=" + SECOND_HOOK)
				.doesNotContain(FIRST_HOOK);
		assertThat(Files.readString(jupiterServices())).contains(SECOND_HOOK).doesNotContain(FIRST_HOOK);
	}

	/**
	 * Registering twice duplicates nothing.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunDuplicatesNothing() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook"), List.of("FirstJqwikHook")));
		String services = Files.readString(jupiterServices());
		String settings = Files.readString(properties());

		files(null).register(resources, contribution(List.of("FirstHook"), List.of("FirstJqwikHook")));

		assertThat(Files.readString(jupiterServices())).isEqualTo(services);
		assertThat(Files.readString(properties())).isEqualTo(settings);
	}

	/**
	 * The instructor's own extension, registered with a trailing comment, stays
	 * registered and is included, so it keeps running.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsAndIncludesTheInstructorsExtension() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + " # setup" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).contains(FIRST_HOOK);
		assertThat(Files.readString(properties()))
				.contains("autodetection.include=" + FIRST_HOOK + "," + INSTRUCTOR_EXTENSION + System.lineSeparator())
				.doesNotContain("# setup");
	}

	/**
	 * The instructor's other JUnit settings stay, and auto-detection they already
	 * switched on is no conflict.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsTheInstructorsOtherSettings() throws IOException {
		Files.writeString(properties(), "junit.jupiter.execution.parallel.enabled=false" + System.lineSeparator()
				+ "junit.jupiter.extensions.autodetection.enabled : TRUE" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		assertThat(Files.readString(properties())).contains("junit.jupiter.execution.parallel.enabled=false");
	}

	/**
	 * Auto-detection the instructor already switched on loaded every provider on
	 * the test class path, so the generated block adds no include filter that would
	 * narrow it.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void autodetectionTheInstructorSwitchedOnGetsNoIncludeFilter() throws IOException {
		Files.writeString(properties(),
				"junit.jupiter.extensions.autodetection.enabled : TRUE" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		assertThat(Files.readString(properties())).contains("junit.jupiter.extensions.autodetection.enabled=true")
				.doesNotContain("autodetection.include");
		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK);
	}

	/**
	 * An extension a test dependency registers through its own service file keeps
	 * loading when the instructor had switched auto-detection on: a nested JUnit
	 * run with the generated settings resolves a parameter only that extension
	 * provides.
	 *
	 * @throws Exception if the nested run cannot be set up
	 */
	@Test
	void aDependencyExtensionKeepsLoadingWhenTheInstructorEnabledAutodetection() throws Exception {
		Files.writeString(properties(), "junit.jupiter.extensions.autodetection.enabled=true" + System.lineSeparator());
		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		runWithDependencyExtension().testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
	}

	/**
	 * Without the instructor's switch, a dependency's extension did not load before
	 * generation and does not load after it: the include filter keeps it off.
	 *
	 * @throws Exception if the nested run cannot be set up
	 */
	@Test
	void aDependencyExtensionStaysOffWhenTheInstructorHadNotEnabledAutodetection() throws Exception {
		files(null).register(resources, contribution(List.of("FirstHook"), List.of()));

		runWithDependencyExtension().testEvents().assertStatistics(stats -> stats.succeeded(0).failed(1));
	}

	/**
	 * A build file setting one of JUnit's auto-detection keys stops generation,
	 * naming the file and the key, since that setting overrides the generated one
	 * and cannot be read.
	 *
	 * @param buildFile the build file.
	 * @throws IOException if the build file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "pom.xml", "build.gradle", "build.gradle.kts" })
	void anAutodetectionSettingInABuildFileStopsGeneration(String buildFile) throws IOException {
		Files.writeString(projectRoot.resolve(buildFile),
				"systemProperty 'junit.jupiter.extensions.autodetection.include', 'com.example.*'");

		SecurityException failure = assertThrows(SecurityException.class,
				() -> files(null).register(resources, contribution(List.of("FirstHook"), List.of())));

		assertThat(failure.getMessage()).contains(buildFile).contains("junit.jupiter.extensions.autodetection.include");
		assertThat(jupiterServices()).doesNotExist();
		assertThat(properties()).doesNotExist();
	}

	/**
	 * The build-file refusal is localised: in German it still names the file and
	 * the key.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void theBuildFileRefusalIsLocalisedInGerman() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"),
				"<junit.jupiter.extensions.autodetection.enabled>true</junit.jupiter.extensions.autodetection.enabled>");
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			SecurityException failure = assertThrows(SecurityException.class,
					() -> files(null).register(resources, contribution(List.of("FirstHook"), List.of())));

			assertThat(failure.getMessage()).startsWith("Ares Sicherheitsfehler").contains("pom.xml")
					.contains("junit.jupiter.extensions.autodetection.enabled");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * A build file setting is no obstacle while nothing is registered, since
	 * nothing generated depends on it then.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void aBuildFileSettingDoesNotMatterWithoutHooks() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"),
				"systemProperty 'junit.jupiter.extensions.autodetection.enabled', 'true'");

		assertThat(files(null).register(resources, contribution(List.of(), List.of()))).isEmpty();
	}

	/**
	 * A setting that contradicts the registration stops generation, in any
	 * separator JUnit accepts, and names the file and the key.
	 *
	 * @param conflictingLine the instructor's conflicting setting.
	 * @throws IOException if the settings file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "junit.jupiter.extensions.autodetection.enabled=false",
			"junit.jupiter.extensions.autodetection.include=com.example.Other",
			"junit.jupiter.extensions.autodetection.exclude:com.example.Other",
			"junit.jupiter.extensions.autodetection.enabled false" })
	void refusesAConflictingSetting(String conflictingLine) throws IOException {
		Files.writeString(properties(), conflictingLine + System.lineSeparator());

		SecurityException failure = assertThrows(SecurityException.class,
				() -> files(null).register(resources, contribution(List.of("FirstHook"), List.of())));

		assertThat(failure.getMessage()).contains("junit-platform.properties")
				.contains(conflictingLine.split("[:= ]")[0]);
		assertThat(jupiterServices()).doesNotExist();
	}

	/**
	 * The refusal is localised: in German it still names the file and the key.
	 *
	 * @throws IOException if the settings file cannot be written
	 */
	@Test
	void theRefusalIsLocalisedInGerman() throws IOException {
		Files.writeString(properties(),
				"junit.jupiter.extensions.autodetection.enabled=false" + System.lineSeparator());
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			SecurityException failure = assertThrows(SecurityException.class,
					() -> files(null).register(resources, contribution(List.of("FirstHook"), List.of())));

			assertThat(failure.getMessage()).startsWith("Ares Sicherheitsfehler").contains("junit-platform.properties")
					.contains("junit.jupiter.extensions.autodetection.enabled");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Registering nothing removes the generated blocks and keeps the instructor's
	 * lines.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void registeringNothingRemovesOnlyWhatWasGenerated() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());
		files(null).register(resources, contribution(List.of("FirstHook"), List.of("FirstJqwikHook")));

		files(null).register(resources, GeneratedHookFiles.Contribution.NONE);

		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).doesNotContain(FIRST_HOOK)
				.doesNotContain(GeneratedHookFiles.BLOCK_BEGIN);
		assertThat(properties()).doesNotExist();
		assertThat(resources.resolve(GeneratedHookFiles.JQWIK_SERVICE_FILE)).doesNotExist();
	}

	/**
	 * Registering nothing also cleans the copies an earlier build left, in Maven's
	 * test output and in Gradle's test resources output.
	 *
	 * @param mode the build tool whose layout is used.
	 * @throws IOException if a file cannot be written or read
	 */
	@ParameterizedTest
	@ValueSource(strings = { "MAVEN", "GRADLE" })
	void registeringNothingCleansTheCopiedResources(String mode) throws IOException {
		BuildMode buildMode = BuildMode.valueOf(mode);
		BuildToolConfiguration layout = layout(buildMode);
		String copies = buildMode == BuildMode.MAVEN ? "target/test-classes" : "build/resources/test";
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());
		files(layout).register(resources, contribution(List.of("FirstHook"), List.of()));
		Path copiedServices = projectRoot.resolve(copies).resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
		Path copiedProperties = projectRoot.resolve(copies).resolve("junit-platform.properties");
		Files.createDirectories(copiedServices.getParent());
		Files.copy(jupiterServices(), copiedServices);
		Files.copy(properties(), copiedProperties);

		files(layout).register(resources, GeneratedHookFiles.Contribution.NONE);

		assertThat(Files.readString(copiedServices)).contains(INSTRUCTOR_EXTENSION).doesNotContain(FIRST_HOOK);
		assertThat(copiedProperties).doesNotExist();
	}

	/**
	 * Deleting a generated class removes its source and its compiled class.
	 *
	 * @param mode the build tool whose layout is used.
	 * @throws IOException if a file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "MAVEN", "GRADLE" })
	void deletingAGeneratedClassRemovesSourceAndCompiledClass(String mode) throws IOException {
		BuildToolConfiguration layout = layout(BuildMode.valueOf(mode));
		Path testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Path source = files(layout).writeSource(testFolder, "FirstHook", "class FirstHook {}");
		Path compiled = layout.testOutputRoot().resolve("de/tum/cit/ase/ares/generated/FirstHook.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		files(layout).deleteGenerated(testFolder, "FirstHook");

		assertThat(source).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/**
	 * jqwik counts only where its group id is named, in a build file or the Gradle
	 * version catalogue.
	 *
	 * @throws IOException if a build file cannot be written
	 */
	@Test
	void jqwikIsDetectedByItsGroupId() throws IOException {
		assertThat(files(null).usesJqwik()).isFalse();
		Files.writeString(projectRoot.resolve("build.gradle"), "// jqwik later, maybe");
		assertThat(files(null).usesJqwik()).isFalse();
		Files.createDirectories(projectRoot.resolve("gradle"));
		Files.writeString(projectRoot.resolve("gradle/libs.versions.toml"),
				"[libraries]\njqwik = { module = \"net.jqwik:jqwik\", version = \"1.9.3\" }\n");
		assertThat(files(null).usesJqwik()).isTrue();
	}

	/**
	 * Runs {@link DependencyProbe} in a nested JUnit session configured with the
	 * generated settings file, as JUnit would read it, with a class path entry
	 * standing in for a dependency JAR that registers {@link DependencyExtension}
	 * through its own service file.
	 *
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private EngineExecutionResults runWithDependencyExtension() throws Exception {
		Path dependency = Files.createDirectories(projectRoot.resolve("dependency"));
		Path services = dependency.resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
		Files.createDirectories(services.getParent());
		Files.writeString(services, DependencyExtension.class.getName() + System.lineSeparator());
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try (URLClassLoader loader = new URLClassLoader(new URL[] { dependency.toUri().toURL() }, original)) {
			Thread.currentThread().setContextClassLoader(loader);
			return EngineTestKit.engine("junit-jupiter").configurationParameters(generatedSettings())
					.selectors(DiscoverySelectors.selectClass(DependencyProbe.class)).execute();
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	/**
	 * The settings in the generated JUnit settings file, read the way JUnit reads
	 * the file.
	 *
	 * @return every key and its value
	 * @throws IOException if the file cannot be read
	 */
	private Map<String, String> generatedSettings() throws IOException {
		Properties settings = new Properties();
		settings.load(new StringReader(Files.readString(properties())));
		return settings.stringPropertyNames().stream().collect(Collectors.toMap(key -> key, settings::getProperty));
	}

	/**
	 * The shared file handling for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the file handling
	 */
	private GeneratedHookFiles files(BuildToolConfiguration layout) {
		return new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity());
	}

	/**
	 * A contribution with no written files.
	 *
	 * @param jupiterHooks the JUnit hooks' simple names.
	 * @param jqwikHooks   the jqwik hooks' simple names.
	 * @return the contribution
	 */
	private static GeneratedHookFiles.Contribution contribution(List<String> jupiterHooks, List<String> jqwikHooks) {
		return new GeneratedHookFiles.Contribution(List.of(), jupiterHooks, jqwikHooks);
	}

	/**
	 * A build layout of this exercise, with its folders created.
	 *
	 * @param buildMode the build tool.
	 * @return the layout
	 * @throws IOException if a folder cannot be created
	 */
	private BuildToolConfiguration layout(BuildMode buildMode) throws IOException {
		boolean maven = buildMode == BuildMode.MAVEN;
		Path main = Files.createDirectories(projectRoot.resolve(maven ? "target/classes" : "build/classes/java/main"));
		Path test = Files
				.createDirectories(projectRoot.resolve(maven ? "target/test-classes" : "build/classes/java/test"));
		Path sources = Files.createDirectories(projectRoot.resolve("src/main/java"));
		Path testSources = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		return new BuildToolConfiguration(buildMode, projectRoot, List.of(sources), List.of(testSources), main, test);
	}

	/**
	 * The JUnit service file in the test resources.
	 *
	 * @return its path
	 */
	private Path jupiterServices() {
		return resources.resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
	}

	/**
	 * JUnit's settings file in the test resources.
	 *
	 * @return its path
	 */
	private Path properties() {
		return resources.resolve(GeneratedHookFiles.PLATFORM_PROPERTIES);
	}

	/** What only {@link DependencyExtension} provides to a test. */
	public static final class DependencyMarker {
	}

	/**
	 * An extension a test dependency registers through its own service file,
	 * standing in for one inside a JAR.
	 */
	public static final class DependencyExtension implements ParameterResolver {

		/**
		 * Resolves only the marker.
		 *
		 * @param parameterContext the parameter.
		 * @param extensionContext the test.
		 * @return true for the marker
		 */
		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == DependencyMarker.class;
		}

		/**
		 * Provides a marker.
		 *
		 * @param parameterContext the parameter.
		 * @param extensionContext the test.
		 * @return a new marker
		 */
		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return new DependencyMarker();
		}
	}

	/**
	 * A test that passes only when {@link DependencyExtension} was loaded. Run only
	 * through a nested session, never on its own.
	 */
	public static final class DependencyProbe {

		/**
		 * Needs the dependency's parameter.
		 *
		 * @param marker provided only by the dependency's extension.
		 */
		@Test
		void receivesTheDependencysParameter(DependencyMarker marker) {
			assertThat(marker).isNotNull();
		}
	}
}
