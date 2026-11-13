package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

/**
 * Runs all four hooks a precompile run generates together, from one policy that
 * sets every category: one registration loads them all, every sentinel passes,
 * hidden results stay silent, and public failures keep their text.
 */
class GeneratedAllHooksTest {

	/** The generated package. */
	private static final String GENERATED = "de.tum.cit.ase.ares.generated.";

	/** The project shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** The compiled generated code and fixtures. */
	private static Path compiled;

	/** The generated resources, holding the bundles and the registration. */
	private static Path resources;

	/** The include value the generator wrote. */
	private static String include;

	/** The generated resources with the hooks registered the other way round. */
	private static Path reversedResources;

	/**
	 * Generates the precompile output for a policy setting all four categories and
	 * compiles it with the fixtures.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		Path project = Files.createDirectory(tempDir.resolve("project"));
		Path policy = project.resolve("SecurityPolicy.yaml");
		Files.writeString(policy,
				"""
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
						    regardingTheStrictTimeoutForTheTestCases:
						      theTimeoutIs: "2s"
						    regardingTheOutputOfTheTestCases:
						      theMaximumNumberOfOutputCharactersIs: 100000
						    regardingTheFailureFeedbackForTheTestCases:
						      theSecurityViolationFeedbackLevelIs: MINIMIZED
						    regardingTheVisibilityAndScheduleOfTheTestCases:
						      theDeadlineIs: "2000-01-01 00:00 UTC"
						      testsNotListedHereAreHidden: false
						      theFollowingTestsAreHidden: ["com.example.fixtures.HiddenFixture", "com.example.fixtures.HiddenTimeoutFixture", "com.example.fixtures.HiddenLoudFixture", "com.example.fixtures.HiddenFactoryFixture"]
						""");
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Files.createDirectories(project.resolve("target/classes"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Path fixtures = Files.createDirectories(testSources.resolve("com/example/fixtures"));
		fixture(fixtures, "PublicFixture", "@Test void fails() { throw new IllegalStateException(\"visible\"); }");
		fixture(fixtures, "HiddenFixture", "@Test void fails() { System.out.print(\"SECRET_ALL_HOOKS\");"
				+ " throw new IllegalStateException(\"SECRET_ALL_HOOKS\"); }");
		fixture(fixtures, "HiddenTimeoutFixture", "@Test void loops() { System.out.print(\"SECRET_TIMEOUT\");"
				+ " while (!Thread.currentThread().isInterrupted()) { Thread.onSpinWait(); } }");
		fixture(fixtures, "HiddenLoudFixture", "@Test void loud() { System.out.print(\"A\".repeat(100001)); }"
				+ " @Test void quiet() { System.out.print(\"A\".repeat(10)); }");
		fixture(fixtures, "HiddenFactoryFixture",
				"@TestFactory java.util.stream.Stream<DynamicTest> cases() { return java.util.stream.Stream.of(1)"
						+ ".map(number -> { System.out.print(\"SECRET_FACTORY\");"
						+ " return DynamicTest.dynamicTest(\"case\", () -> { }); }); }");
		fixture(fixtures, "ResourceFactoryFixture",
				"static final java.util.concurrent.atomic.AtomicBoolean OUTER = new java.util.concurrent.atomic.AtomicBoolean();"
						+ " static final java.util.concurrent.atomic.AtomicBoolean INNER = new java.util.concurrent.atomic.AtomicBoolean();"
						+ " @TestFactory java.util.stream.Stream<DynamicNode> cases() { return java.util.stream.Stream.<DynamicNode>of("
						+ "DynamicTest.dynamicTest(\"outer\", () -> Assertions.assertFalse(OUTER.get(), \"outer closed early\")),"
						+ " DynamicContainer.dynamicContainer(\"box\", java.util.stream.Stream.of(DynamicTest.dynamicTest(\"inner\","
						+ " () -> Assertions.assertFalse(INNER.get(), \"inner closed early\"))).onClose(() -> INNER.set(true))))"
						+ ".onClose(() -> OUTER.set(true)); }");
		fixture(fixtures, "ParallelFactoryFixture",
				"@TestFactory java.util.stream.Stream<DynamicTest> cases() { return java.util.stream.IntStream.range(0, 500)"
						+ ".parallel().mapToObj(number -> DynamicTest.dynamicTest(\"case\" + number, () -> { })); }");
		fixture(fixtures, "LoopingCauseFixture",
				"@Test void fails() throws Exception { throw new java.util.concurrent.ExecutionException(\"looping\", null) {"
						+ " @Override public synchronized Throwable getCause() { while (true) { Thread.onSpinWait(); } } }; }");
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		compiled = Files.createDirectories(tempDir.resolve("compiled"));
		compile(compiled,
				List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
						testSources.resolve("com/example/ares/api/localization"), fixtures),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		resources = testSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		reversedResources = reversedRegistration(resources, tempDir.resolve("reversed-resources"));
	}

	/**
	 * A copy of the generated resources whose service file lists the hooks in the
	 * opposite order, so JUnit registers them the other way round.
	 *
	 * @param from the generated resources.
	 * @param to   the copy's folder.
	 * @return the copy
	 * @throws IOException if copying fails
	 */
	private static Path reversedRegistration(Path from, Path to) throws IOException {
		try (var files = Files.walk(from)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				Path target = to.resolve(from.relativize(file).toString());
				Files.createDirectories(target.getParent());
				Files.copy(file, target);
			}
		}
		Path services = to.resolve("META-INF/services/org.junit.jupiter.api.extension.Extension");
		List<String> lines = new ArrayList<>(Files.readAllLines(services));
		List<String> hooks = lines.stream().filter(line -> line.startsWith(GENERATED)).toList();
		List<String> reversed = new ArrayList<>(hooks);
		Collections.reverse(reversed);
		for (int index = 0, next = 0; index < lines.size(); index++) {
			if (lines.get(index).startsWith(GENERATED)) {
				lines.set(index, reversed.get(next++));
			}
		}
		Files.write(services, lines);
		return to;
	}

	/**
	 * A hidden test that runs too long is stopped and reports nothing, with the
	 * real generated hooks registered in either order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenTimeoutStaysSilentInEitherOrder(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenTimeoutFixture", reversed));
		assertThat(captured.results().testEvents().failed().count()).isEqualTo(1);
		assertThat(messages(captured.results())).containsExactly("");
		assertThat(captured.output()).doesNotContain("SECRET_TIMEOUT");
	}

	/**
	 * A hidden test is held to the output limit like a public one, in either order,
	 * while its output stays out of sight.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenTestIsHeldToTheOutputLimit(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenLoudFixture", reversed));
		assertThat(captured.results().testEvents().failed().count()).isEqualTo(1);
		assertThat(captured.results().testEvents().succeeded().count()).isEqualTo(1);
		assertThat(captured.output()).doesNotContain("AAAAAAAAAA");
	}

	/**
	 * A hidden test factory whose tests print while JUnit reads them shows nothing,
	 * since the hooks read them while still guarding the factory.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenFactoryPrintsNothing(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenFactoryFixture", reversed));
		assertThat(captured.output()).doesNotContain("SECRET_FACTORY");
	}

	/**
	 * A factory's tests run before the streams that produced them are closed, so a
	 * resource the factory releases on close is still open while its tests use it.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aFactorysStreamsCloseAfterItsTestsRan(boolean reversed) throws Exception {
		EngineExecutionResults results = run("com.example.fixtures.ResourceFactoryFixture", reversed);
		assertThat(results.testEvents().failed().count()).isZero();
		assertThat(results.testEvents().succeeded().count()).isEqualTo(2);
	}

	/**
	 * A factory returning a parallel stream keeps every test, in its order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aParallelFactoryKeepsEveryTestInOrder(boolean reversed) throws Exception {
		EngineExecutionResults results = run("com.example.fixtures.ParallelFactoryFixture", reversed);
		List<String> expected = new ArrayList<>();
		for (int number = 0; number < 500; number++) {
			expected.add("case" + number);
		}
		assertThat(results.testEvents().started().stream().map(event -> event.getTestDescriptor().getDisplayName()))
				.containsExactlyElementsOf(expected);
	}

	/**
	 * A failure whose own {@code getCause} never returns is still reported: the
	 * timeout and failure hooks never call it.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aLoopingGetCauseIsNeverCalled() throws Exception {
		EngineExecutionResults results = assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.LoopingCauseFixture", false));
		assertThat(results.testEvents().failed().count()).isEqualTo(1);
	}

	/**
	 * A run's results with what it printed to standard output.
	 *
	 * @param results the run's results.
	 * @param output  what it printed.
	 */
	private record Captured(EngineExecutionResults results, String output) {
	}

	/**
	 * Runs something while capturing standard output.
	 *
	 * @param run the run.
	 * @return its results and output
	 * @throws Exception if the run fails to start
	 */
	private static Captured captured(Callable<EngineExecutionResults> run) throws Exception {
		PrintStream previousOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			EngineExecutionResults results = run.call();
			return new Captured(results, output.toString(StandardCharsets.UTF_8));
		} finally {
			System.setOut(previousOut);
		}
	}

	/** One registration names all four hooks. */
	@Test
	void oneRegistrationLoadsAllFourHooks() {
		assertThat(include).contains(GENERATED + "GeneratedStrictTimeout")
				.contains(GENERATED + "GeneratedOutputMirroring").contains(GENERATED + "GeneratedFailureReporting")
				.contains(GENERATED + "GeneratedHiddenTests");
	}

	/**
	 * Every sentinel passes, so every hook ran.
	 *
	 * @param sentinel the sentinel's simple name.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(strings = { "GeneratedStrictTimeoutSentinelTest", "GeneratedOutputMirroringSentinelTest",
			"GeneratedFailureReportingSentinelTest", "GeneratedHiddenTestsSentinelTest" })
	void everySentinelPasses(String sentinel) throws Exception {
		EngineExecutionResults results = run(GENERATED + sentinel);
		assertThat(results.testEvents().failed().count()).isZero();
		assertThat(results.testEvents().succeeded().count()).isPositive();
	}

	/**
	 * A hidden failure stays failed with no text or output, although every other
	 * hook also handles it.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aHiddenFailureStaysSilent() throws Exception {
		PrintStream previousOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		EngineExecutionResults results;
		try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			results = run("com.example.fixtures.HiddenFixture");
		} finally {
			System.setOut(previousOut);
		}
		assertThat(results.testEvents().failed().count()).isEqualTo(1);
		assertThat(messages(results)).allMatch(message -> !message.contains("SECRET_ALL_HOOKS"));
		assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain("SECRET_ALL_HOOKS");
	}

	/**
	 * A public failure that is not a blocked action keeps its text: the feedback
	 * level touches only blocked actions.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aPublicFailureKeepsItsText() throws Exception {
		assertThat(messages(run("com.example.fixtures.PublicFixture"))).containsExactly("visible");
	}

	/**
	 * Runs one class in a nested JUnit session on the generated output.
	 *
	 * @param className the class to run.
	 * @return the results.
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults run(String className) throws Exception {
		return run(className, false);
	}

	/**
	 * Runs one class in a nested JUnit session on the generated output, with the
	 * hooks registered in their generated or the reversed order.
	 *
	 * @param className the class to run.
	 * @param reversed  whether the hooks are registered the other way round.
	 * @return the results.
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults run(String className, boolean reversed) throws Exception {
		Path registration = reversed ? reversedResources : resources;
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiled, registration)),
				Thread.currentThread().getContextClassLoader())) {
			return GeneratedCodeRuns.runIn(loader, className, Locale.ENGLISH,
					Map.of(GeneratedCodeRuns.AUTODETECTION, "true", GeneratedCodeRuns.AUTODETECTION_INCLUDE, include,
							"junit.platform.stacktrace.pruning.enabled", "false"));
		}
	}

	/**
	 * The failure messages of a run.
	 *
	 * @param results the run's results.
	 * @return the messages, empty text for none.
	 */
	private static List<String> messages(EngineExecutionResults results) {
		return results.testEvents().failed().stream().map(GeneratedAllHooksTest::messageOf).toList();
	}

	/**
	 * The failure message of an event.
	 *
	 * @param event the event.
	 * @return the message, empty text for none.
	 */
	private static String messageOf(Event event) {
		return GeneratedCodeRuns.messageOf(event);
	}

	/**
	 * Writes a fixture test class.
	 *
	 * @param folder the fixture folder.
	 * @param name   the class name.
	 * @param body   the class body.
	 * @throws IOException if writing fails
	 */
	private static void fixture(Path folder, String name, String body) throws IOException {
		Files.writeString(folder.resolve(name + ".java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.*;

				class %s {
					%s
				}
				""".formatted(name, body));
	}

	/**
	 * Compiles source folders and single files.
	 *
	 * @param output      the class output.
	 * @param folders     the folders to compile.
	 * @param singleFiles further files.
	 * @throws IOException if compiling fails
	 */
	private static void compile(Path output, List<Path> folders, Path... singleFiles) throws IOException {
		GeneratedCodeRuns.compile(output, folders, singleFiles);
	}
}
