package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Runs the output mirroring a precompile run generates, end to end: the real
 * generator writes it into a project, the generated sources are compiled with
 * fixture tests, and nested JUnit runs show the same limit, echo and errors as
 * {@code IOTester} gives in postcompile.
 */
class GeneratedOutputMirroringTest {

	/** The generated extension's sentinel test. */
	private static final String SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedOutputMirroringSentinelTest";

	/** A line longer than the policy's limit of 10. */
	private static final String LONG_LINE = "Pinguine sind die Besten!";

	/** The project shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** Generated code and fixtures, from a policy that does not mirror. */
	private static Path silent;

	/** Generated code and fixtures, from a policy that mirrors. */
	private static Path mirrored;

	/** A settings class with a far larger limit, as a student could replace it. */
	private static Path shadowSettings;

	/** The project's test resources, holding the bundles. */
	private static Path resources;

	/**
	 * The include value the generator wrote into {@code junit-platform.properties}.
	 */
	private static String include;

	/**
	 * Generates the precompile output for a silent and a mirroring policy, each
	 * with a limit of 10, and compiles both with the fixtures.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		Path fixtures = writeFixtures(tempDir.resolve("fixtures"));
		Path silentSources = generate("silent", false);
		resources = silentSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		silent = compileProject("compiled-silent", silentSources, fixtures);
		mirrored = compileProject("compiled-mirrored", generate("mirrored", true), fixtures);
		shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
		Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
				.resolve("GeneratedTestBehaviorSettings.java");
		Files.writeString(shadowSource,
				Files.readString(
						silentSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"))
						.replace("= 10L;", "= 100000000L;"));
		compile(shadowSettings, List.of(), shadowSource);
	}

	/**
	 * Output beyond the limit fails with the localised message, in every phase the
	 * extension covers.
	 *
	 * @param fixture the fixture writing the long line in one phase.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(strings = { "TestMethodFixture", "BeforeEachFixture", "AfterEachFixture" })
	void tooMuchOutputFails(String fixture) throws Exception {
		assertThat(failureMessages(run(silent, fixture, Locale.ENGLISH).results())).singleElement().asString()
				.startsWith(limitMessagePrefix(Locale.ENGLISH));
	}

	/**
	 * The limit message is localised: in German it is the German bundle's text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theLimitMessageIsLocalised() throws Exception {
		assertThat(failureMessages(run(silent, "TestMethodFixture", Locale.GERMAN).results())).singleElement()
				.asString().startsWith(limitMessagePrefix(Locale.GERMAN));
	}

	/**
	 * Output in {@code @BeforeAll} is outside the extension's reach, so it is
	 * neither counted nor held back.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void outputInBeforeAllIsNotCounted() throws Exception {
		NestedRun run = run(silent, "BeforeAllFixture", Locale.ENGLISH);

		assertThat(run.results().allEvents().failed().count()).isZero();
		assertThat(run.console()).contains(LONG_LINE);
	}

	/**
	 * Mirroring on echoes a test's output to the console; off keeps it back.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void mirroringDecidesWhetherOutputReachesTheConsole() throws Exception {
		NestedRun mirroredRun = run(mirrored, "ShortFixture", Locale.ENGLISH);
		NestedRun silentRun = run(silent, "ShortFixture", Locale.ENGLISH);

		assertThat(mirroredRun.results().testEvents().succeeded().count()).isEqualTo(1);
		assertThat(mirroredRun.console()).contains("hi");
		assertThat(silentRun.results().testEvents().succeeded().count()).isEqualTo(1);
		assertThat(silentRun.console()).doesNotContain("hi");
	}

	/**
	 * Output that is not valid UTF-8 fails on flush with the localised message.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void invalidUtf8Fails() throws Exception {
		String prefix = bundle(Locale.ENGLISH).getString("output_tester.output_is_invalid_utf8").split("%s")[0];

		assertThat(failureMessages(run(silent, "InvalidUtf8Fixture", Locale.ENGLISH).results())).singleElement()
				.asString().startsWith(prefix);
	}

	/**
	 * A test closing {@code System.out} sees its later writes fail, while the real
	 * console stays open.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void closingTheStreamLeavesTheConsoleOpen() throws Exception {
		NestedRun run = run(mirrored, "ClosedFixture", Locale.ENGLISH);

		assertThat(run.results().testEvents().succeeded().count()).isEqualTo(1);
		assertThat(run.consoleStillOpen()).isTrue();
	}

	/**
	 * The console streams are back in place after a failing test.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theStreamsAreRestoredAfterAFailingTest() throws Exception {
		assertThat(run(silent, "TestMethodFixture", Locale.ENGLISH).streamsRestored()).isTrue();
	}

	/**
	 * Two tests running in parallel cannot both install the streams: the second is
	 * refused with the localised message, and the console is intact afterwards.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aSecondParallelInstallationIsRefused() throws Exception {
		NestedRun run = run(silent, "ParallelFixture", Locale.ENGLISH,
				Map.of("junit.jupiter.execution.parallel.enabled", "true",
						"junit.jupiter.execution.parallel.mode.default", "concurrent",
						"junit.jupiter.execution.parallel.config.strategy", "fixed",
						"junit.jupiter.execution.parallel.config.fixed.parallelism", "2"));

		assertThat(failureMessages(run.results()))
				.containsExactly(bundle(Locale.ENGLISH).getString("io_tester.already_installed"));
		assertThat(run.results().testEvents().succeeded().count()).isEqualTo(1);
		assertThat(run.streamsRestored()).isTrue();
	}

	/**
	 * An instructor capturing {@code System.out} in {@code @BeforeEach} takes over
	 * for that test, past the limit.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void anInstructorCaptureInBeforeEachTakesOver() throws Exception {
		assertThat(run(silent, "CaptureInBeforeEachFixture", Locale.ENGLISH).results().allEvents().failed().count())
				.isZero();
	}

	/**
	 * An instructor capturing {@code System.out} in {@code @BeforeAll} receives
	 * nothing while mirroring is off, since the extension treats it as the console.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void anInstructorCaptureInBeforeAllReceivesNothing() throws Exception {
		assertThat(run(silent, "CaptureInBeforeAllFixture", Locale.ENGLISH).results().allEvents().failed().count())
				.isZero();
	}

	/**
	 * The sentinel passes when the extension ran around it.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelPassesWithTheExtension() throws Exception {
		assertThat(run(silent, SENTINEL, Locale.ENGLISH).results().testEvents().succeeded().count()).isEqualTo(1);
	}

	/**
	 * The sentinel fails when JUnit does not load the extension.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelFailsWithoutTheExtension() throws Exception {
		NestedRun run = run(silent, SENTINEL, Locale.ENGLISH,
				Map.of("junit.jupiter.extensions.autodetection.enabled", "false"));

		assertThat(failureMessages(run.results())).singleElement().asString().contains("not active");
	}

	/**
	 * Replacing the generated settings class with a far larger limit changes
	 * nothing: the extension carries the settings as compile-time constants.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aReplacedSettingsClassChangesNothing() throws Exception {
		NestedRun run = run(List.of(shadowSettings, silent), "com.example.fixtures.TestMethodFixture", Locale.ENGLISH,
				Map.of());

		assertThat(failureMessages(run.results())).singleElement().asString()
				.startsWith(limitMessagePrefix(Locale.ENGLISH));
	}

	/**
	 * Reusing a class loader cannot make a sentinel pass after its hook is
	 * disabled.
	 *
	 * @throws Exception if either nested run cannot be set up
	 */
	@Test
	void theSentinelNeedsEvidenceFromItsOwnRun() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(classPath(List.of(silent)),
				Thread.currentThread().getContextClassLoader())) {
			assertThat(runIn(loader, SENTINEL, Locale.ENGLISH, Map.of()).results().testEvents().succeeded().count())
					.isEqualTo(1);

			EngineExecutionResults withoutExtension = runIn(loader, SENTINEL, Locale.ENGLISH,
					Map.of("junit.jupiter.extensions.autodetection.enabled", "false")).results();

			assertThat(failureMessages(withoutExtension)).singleElement().asString().contains("not active");
		}
	}

	/**
	 * What one nested run produced.
	 *
	 * @param results          the run's results.
	 * @param console          what reached the console during the run.
	 * @param streamsRestored  whether both console streams were back afterwards.
	 * @param consoleStillOpen whether the console could still be written to.
	 */
	private record NestedRun(EngineExecutionResults results, String console, boolean streamsRestored,
			boolean consoleStillOpen) {
	}

	/**
	 * Runs one fixture through a nested JUnit session.
	 *
	 * @param project the compiled project.
	 * @param fixture the fixture's simple name, or a fully qualified class.
	 * @param locale  the locale messages are shown in.
	 * @return what the run produced
	 * @throws Exception if the run cannot be set up
	 */
	private static NestedRun run(Path project, String fixture, Locale locale) throws Exception {
		return run(project, fixture, locale, Map.of());
	}

	/**
	 * Runs one fixture through a nested JUnit session with extra settings.
	 *
	 * @param project    the compiled project.
	 * @param fixture    the fixture's simple name, or a fully qualified class.
	 * @param locale     the locale messages are shown in.
	 * @param parameters further JUnit configuration parameters.
	 * @return what the run produced
	 * @throws Exception if the run cannot be set up
	 */
	private static NestedRun run(Path project, String fixture, Locale locale, Map<String, String> parameters)
			throws Exception {
		String className = fixture.contains(".") ? fixture : "com.example.fixtures." + fixture;
		return run(List.of(project), className, locale, parameters);
	}

	/**
	 * Runs a class through a nested JUnit session with a fresh class loader, while
	 * a capture stands in for the console.
	 *
	 * @param roots      the compiled class roots, first wins.
	 * @param className  the class to run.
	 * @param locale     the locale messages are shown in.
	 * @param parameters further JUnit configuration parameters.
	 * @return what the run produced
	 * @throws Exception if the run cannot be set up
	 */
	private static NestedRun run(List<Path> roots, String className, Locale locale, Map<String, String> parameters)
			throws Exception {
		try (URLClassLoader loader = new URLClassLoader(classPath(roots),
				Thread.currentThread().getContextClassLoader())) {
			return runIn(loader, className, locale, parameters);
		}
	}

	/**
	 * Runs a class in an existing loader and restores the caller's streams and
	 * locale.
	 *
	 * @param loader     the loader shared by the nested runs
	 * @param className  the test class
	 * @param locale     the locale for messages
	 * @param parameters further JUnit settings
	 * @return the results and captured output
	 * @throws Exception if the run cannot be set up
	 */
	private static NestedRun runIn(ClassLoader loader, String className, Locale locale, Map<String, String> parameters)
			throws Exception {
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;
		ByteArrayOutputStream console = new ByteArrayOutputStream();
		PrintStream consoleStream = new PrintStream(console, true, StandardCharsets.UTF_8);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			System.setOut(consoleStream);
			System.setErr(consoleStream);
			EngineTestKit.Builder builder = EngineTestKit.engine("junit-jupiter")
					.configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
					.configurationParameter("junit.jupiter.extensions.autodetection.include", include);
			parameters.forEach(builder::configurationParameter);
			EngineExecutionResults results = builder
					.selectors(DiscoverySelectors.selectClass(loader.loadClass(className))).execute();
			boolean restored = System.out == consoleStream && System.err == consoleStream;
			consoleStream.print("");
			return new NestedRun(results, console.toString(StandardCharsets.UTF_8), restored,
					!consoleStream.checkError());
		} finally {
			System.setOut(originalOut);
			System.setErr(originalErr);
			Thread.currentThread().setContextClassLoader(originalLoader);
			Locale.setDefault(Locale.Category.DISPLAY, originalLocale);
		}
	}

	/**
	 * The class path of a nested run: the compiled roots and the resources.
	 *
	 * @param roots the compiled class roots, first wins.
	 * @return the class path entries
	 * @throws IOException if a path cannot be turned into a URL
	 */
	private static URL[] classPath(List<Path> roots) throws IOException {
		List<URL> urls = new ArrayList<>();
		for (Path root : roots) {
			urls.add(root.toUri().toURL());
		}
		urls.add(resources.toUri().toURL());
		return urls.toArray(URL[]::new);
	}

	/**
	 * The copied bundle for a locale.
	 *
	 * @param locale the locale.
	 * @return the bundle
	 */
	private static ResourceBundle bundle(Locale locale) {
		return ResourceBundle.getBundle("de.tum.cit.ase.ares.api.localization.messages", locale);
	}

	/**
	 * The limit message for a locale, up to the count it reports.
	 *
	 * @param locale the locale.
	 * @return the message's fixed start
	 */
	private static String limitMessagePrefix(Locale locale) {
		return bundle(locale).getString("output_tester.output_maxExceeded").split("%s")[0];
	}

	/**
	 * The failure messages of every failed test and container of a run.
	 *
	 * @param results the run's results.
	 * @return the messages
	 */
	private static List<String> failureMessages(EngineExecutionResults results) {
		return results.allEvents().failed().stream().map(GeneratedOutputMirroringTest::messageOf).toList();
	}

	/**
	 * The failure message carried by a finished event.
	 *
	 * @param event a finished event.
	 * @return the message, or an empty string when there is none
	 */
	private static String messageOf(Event event) {
		return event.getPayload(TestExecutionResult.class).flatMap(TestExecutionResult::getThrowable)
				.map(Throwable::getMessage).map(String::valueOf).orElse("");
	}

	/**
	 * Runs the real generator on a fresh project.
	 *
	 * @param folder        the project's folder name.
	 * @param mirroringIsOn whether the policy mirrors output.
	 * @return the project's test source root
	 * @throws IOException if the project cannot be created
	 */
	private static Path generate(String folder, boolean mirroringIsOn) throws IOException {
		Path project = Files.createDirectory(tempDir.resolve(folder));
		Path policy = project.resolve("SecurityPolicy.yaml");
		Files.writeString(policy, policyText(mirroringIsOn));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("target/classes"));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		return testSources;
	}

	/**
	 * A policy for the {@code com.example} exercise with a limit of 10.
	 *
	 * @param mirroringIsOn whether the policy mirrors output.
	 * @return the policy text
	 */
	private static String policyText(boolean mirroringIsOn) {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theSupervisedCodeUsesTheFollowingPackage: "com.example"
				  theMainClassInsideThisPackageIs: "Main"
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				  theFollowingTestBehaviorIsConfigured:
				    regardingOutputMirroring:
				      theOutputIsMirrored: %s
				      theMaximumCharacterCountIs: 10
				""".formatted(mirroringIsOn);
	}

	/**
	 * Compiles one generated project together with the fixtures.
	 *
	 * @param folder      the output folder's name.
	 * @param testSources the project's test source root.
	 * @param fixtures    the fixtures' source root.
	 * @return the output folder
	 * @throws IOException if compiling fails
	 */
	private static Path compileProject(String folder, Path testSources, Path fixtures) throws IOException {
		Path output = Files.createDirectories(tempDir.resolve(folder));
		compile(output,
				List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
						testSources.resolve("com/example/ares/api/localization"), fixtures),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		return output;
	}

	/**
	 * Writes the fixture tests, one per behaviour the extension must show.
	 *
	 * @param root the folder to write them into.
	 * @return the folder
	 * @throws IOException if a file cannot be written
	 */
	private static Path writeFixtures(Path root) throws IOException {
		Path folder = Files.createDirectories(root.resolve("com/example/fixtures"));
		String longLine = "System.out.println(\"" + LONG_LINE + "\");";
		fixture(folder, "TestMethodFixture", "", "@Test void writes() { " + longLine + " }");
		fixture(folder, "BeforeEachFixture", "@BeforeEach void setUp() { " + longLine + " }", "@Test void test() {}");
		fixture(folder, "AfterEachFixture", "@AfterEach void tearDown() { " + longLine + " }", "@Test void test() {}");
		fixture(folder, "BeforeAllFixture", "@BeforeAll static void setUp() { " + longLine + " }",
				"@Test void test() {}");
		fixture(folder, "ShortFixture", "", "@Test void writes() { System.out.println(\"hi\"); }");
		fixture(folder, "InvalidUtf8Fixture", "",
				"@Test void writes() { System.out.write(new byte[] { (byte) 0xC3 }, 0, 1); System.out.flush(); }");
		fixture(folder, "ClosedFixture", "",
				"@Test void writes() { System.out.close(); System.out.print(\"x\"); Assertions.assertTrue(System.out.checkError()); }");
		fixture(folder, "ParallelFixture", "",
				"@Test void first() throws InterruptedException { Thread.sleep(1000); } @Test void second() throws InterruptedException { Thread.sleep(1000); }");
		fixture(folder, "CaptureInBeforeEachFixture", """
				private final java.io.ByteArrayOutputStream capture = new java.io.ByteArrayOutputStream();
				@BeforeEach void capture() { System.setOut(new java.io.PrintStream(capture, true)); }""",
				"@Test void writes() { " + longLine + " Assertions.assertTrue(capture.toString().contains(\""
						+ LONG_LINE + "\")); }");
		fixture(folder, "CaptureInBeforeAllFixture",
				"""
						private static final java.io.ByteArrayOutputStream CAPTURE = new java.io.ByteArrayOutputStream();
						private static java.io.PrintStream saved;
						@BeforeAll static void capture() { saved = System.out; System.setOut(new java.io.PrintStream(CAPTURE, true)); }
						@AfterAll static void release() { System.setOut(saved); }""",
				"@Test void writes() { System.out.println(\"hi\"); Assertions.assertEquals(0, CAPTURE.size()); }");
		return root;
	}

	/**
	 * Writes one JUnit fixture class.
	 *
	 * @param folder the fixtures' package folder.
	 * @param name   the class's simple name.
	 * @param setup  members to declare first, or an empty string.
	 * @param test   the test members.
	 * @throws IOException if the file cannot be written
	 */
	private static void fixture(Path folder, String name, String setup, String test) throws IOException {
		Files.writeString(folder.resolve(name + ".java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.*;

				class %s {
					%s

					%s
				}
				""".formatted(name, setup, test));
	}

	/**
	 * Compiles every Java file below the given folders, plus single files, against
	 * this test run's own classpath.
	 *
	 * @param output      the folder to compile into.
	 * @param folders     folders whose Java files to compile.
	 * @param singleFiles further files to compile.
	 * @throws IOException if a folder cannot be listed or compiling fails
	 */
	private static void compile(Path output, List<Path> folders, Path... singleFiles) throws IOException {
		List<String> arguments = new ArrayList<>(
				List.of("-d", output.toString(), "-classpath", System.getProperty("java.class.path"), "-proc:none"));
		for (Path folder : folders) {
			try (Stream<Path> files = Files.walk(folder)) {
				files.filter(file -> file.toString().endsWith(".java")).map(Path::toString).forEach(arguments::add);
			}
		}
		Stream.of(singleFiles).map(Path::toString).forEach(arguments::add);
		JavaCompiler compiler = Optional.ofNullable(ToolProvider.getSystemJavaCompiler())
				.orElseThrow(() -> new IllegalStateException("No Java compiler available"));
		if (compiler.run(null, null, null, arguments.toArray(String[]::new)) != 0) {
			throw new IOException("Compiling the generated output mirroring failed");
		}
	}
}
