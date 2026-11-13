package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestExecutionResult;
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
	 * A class in the generated package compiled somewhere else, with a fixture
	 * throwing it.
	 */
	private static Path forged;

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
						      theFollowingTestsAreHidden: ["com.example.fixtures.HiddenFixture", "com.example.fixtures.HiddenTimeoutFixture", "com.example.fixtures.HiddenLoudFixture", "com.example.fixtures.HiddenFactoryFixture", "com.example.fixtures.HiddenUnflushedFixture", "com.example.fixtures.HiddenAbortFixture"]
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
		fixture(fixtures, "FailingFactoryFixture",
				"@TestFactory java.util.stream.Stream<DynamicNode> cases() { return java.util.stream.Stream.<DynamicNode>of("
						+ "DynamicContainer.dynamicContainer(\"box\", java.util.stream.Stream.<DynamicNode>of("
						+ "DynamicTest.dynamicTest(\"inner\", () -> { })).map(node -> { if (node != null) {"
						+ " throw new IllegalStateException(\"broken read\"); } return node; })"
						+ ".onClose(() -> { throw new IllegalStateException(\"CLOSED_INNER\"); })))"
						+ ".onClose(() -> { throw new IllegalStateException(\"CLOSED_OUTER\"); }); }");
		fixture(fixtures, "ParallelFactoryFixture",
				"@TestFactory java.util.stream.Stream<DynamicTest> cases() { return java.util.stream.IntStream.range(0, 500)"
						+ ".parallel().mapToObj(number -> DynamicTest.dynamicTest(\"case\" + number, () -> { })); }");
		fixture(fixtures, "LoopingCauseFixture",
				"@Test void fails() throws Exception { throw new java.util.concurrent.ExecutionException(\"looping\", null) {"
						+ " @Override public synchronized Throwable getCause() { while (true) { Thread.onSpinWait(); } } }; }");
		fixture(fixtures, "LoopingMessageFixture", "@Test void fails() { throw new IllegalStateException(\"looping\") {"
				+ " @Override public String getMessage() { while (true) { Thread.onSpinWait(); } } }; }");
		fixture(fixtures, "LoopingStackFixture", "@Test void fails() { throw new IllegalStateException(\"looping\") {"
				+ " @Override public StackTraceElement[] getStackTrace() { while (true) { Thread.onSpinWait(); } } }; }");
		fixture(fixtures, "LoopingReasonFixture",
				"@Test void fails() throws Exception { throw new java.nio.file.FileSystemException(\"file\") {"
						+ " @Override public synchronized Throwable getCause() { return null; }"
						+ " @Override public String getReason() { while (true) { Thread.onSpinWait(); } } }; }");
		fixture(fixtures, "LoopingReasonOnlyFixture",
				"@Test void fails() throws Exception { throw new java.nio.file.FileSystemException(\"file\") {"
						+ " @Override public String getReason() { while (true) { Thread.onSpinWait(); } } }; }");
		fixture(fixtures, "PlainStudentFailureFixture",
				"@Test void fails() { throw new IllegalStateException(\"plain\") { }; }");
		fixture(fixtures, "SuppressedLoopingFixture",
				"@Test void fails() { IllegalStateException outer = new IllegalStateException(\"outer\");"
						+ " outer.addSuppressed(new IllegalStateException(\"inner\") { @Override public String toString() {"
						+ " while (true) { Thread.onSpinWait(); } } }); throw outer; }");
		fixture(fixtures, "AssertAllFixture",
				"@Test void fails() { Assertions.assertAll(() -> Assertions.fail(\"first\"), () -> Assertions.fail(\"second\")); }");
		fixture(fixtures, "HiddenUnflushedFixture",
				"@Test void writes() { for (byte part : \"SECRET_UNFLUSHED\".getBytes(java.nio.charset.StandardCharsets.UTF_8)) {"
						+ " System.out.write(part); } System.out.write(0x80); }");
		fixture(fixtures, "HiddenAbortFixture",
				"@Test void aborts() { System.out.print(\"SECRET_ABORT\"); Assumptions.abort(\"SECRET_ABORT\"); }");
		fixture(fixtures, "AssertAllLoopingFixture",
				"@Test void fails() { Assertions.assertAll(() -> Assertions.fail(\"first\"),"
						+ " () -> { throw new IllegalStateException(\"looping\") { @Override public String getMessage() {"
						+ " while (true) { Thread.onSpinWait(); } } }; }); }");
		fixture(fixtures, "AggregateLoopingFixture",
				"@Test void fails() { throw new org.opentest4j.MultipleFailuresError(\"heading\","
						+ " java.util.List.of(new IllegalStateException(\"looping\") { @Override public String getMessage() {"
						+ " while (true) { Thread.onSpinWait(); } } })); }");
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
		forged = forgedMarker(tempDir.resolve("forged-sources"), tempDir.resolve("forged"));
	}

	/**
	 * Compiles a failure class in the generated package whose {@code toString}
	 * never returns, outside the generated output, and a fixture throwing it.
	 *
	 * @param sources the sources' folder.
	 * @param output  the compiled classes' folder.
	 * @return the compiled classes' folder
	 * @throws IOException if writing or compiling fails
	 */
	private static Path forgedMarker(Path sources, Path output) throws IOException {
		Path marker = Files.createDirectories(sources.resolve("de/tum/cit/ase/ares/generated"));
		Files.writeString(marker.resolve("ForgedMarker.java"), """
				package de.tum.cit.ase.ares.generated;

				public class ForgedMarker extends AssertionError {
					public ForgedMarker() { super("forged"); }
					@Override public String toString() { while (true) { Thread.onSpinWait(); } }
				}
				""");
		fixture(Files.createDirectories(sources.resolve("com/example/fixtures")), "ForgedMarkerFixture",
				"@Test void fails() { throw new de.tum.cit.ase.ares.generated.ForgedMarker(); }");
		compile(Files.createDirectories(output), List.of(sources));
		return output;
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
	 * When reading a factory's tests fails, the streams opened so far are closed
	 * anyway, nested ones included, since JUnit never receives them to close. Each
	 * close throws a marker, kept as suppressed on the reported failure.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aFailedFactoryReadClosesItsStreams(boolean reversed) throws Exception {
		List<String> closed = run("com.example.fixtures.FailingFactoryFixture", reversed).allEvents().failed().stream()
				.flatMap(event -> event.getPayload(TestExecutionResult.class).flatMap(TestExecutionResult::getThrowable)
						.stream())
				.flatMap(failure -> Arrays.stream(failure.getSuppressed())).map(Throwable::getMessage).toList();
		assertThat(closed).contains("CLOSED_INNER", "CLOSED_OUTER");
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
	 * A failure whose own {@code getCause} never returns is still reported with its
	 * text, under JUnit's default stack-trace pruning: the hooks never call it and
	 * hand JUnit a copy it can walk, in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aLoopingGetCauseIsNeverCalled(boolean reversed) throws Exception {
		EngineExecutionResults results = assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.LoopingCauseFixture", reversed));
		assertThat(messages(results)).containsExactly("com.example.fixtures.LoopingCauseFixture$1");
	}

	/**
	 * A failure whose own reporting methods never return is reported as a copy
	 * JUnit reads without calling them, whether the override sits on the failure
	 * itself or on a failure suppressed in it, in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void loopingReportingMethodsAreNeverCalled(boolean reversed) throws Exception {
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.LoopingMessageFixture", reversed))))
						.containsExactly("com.example.fixtures.LoopingMessageFixture$1");
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.LoopingStackFixture", reversed))))
						.containsExactly("com.example.fixtures.LoopingStackFixture$1");
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.SuppressedLoopingFixture", reversed))))
						.containsExactly("java.lang.IllegalStateException: outer");
	}

	/**
	 * A student {@code FileSystemException} whose {@code getReason}, which the Java
	 * runtime's own {@code getMessage} calls, never returns is reported by its type
	 * alone, with or without its own {@code getCause}, in either registration
	 * order, in a JVM of its own so a hang cannot take this run with it.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @param probe    where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aLoopingDelegatedReasonIsNeverCalled(boolean reversed, @TempDir Path probe) throws Exception {
		Path probeClasses = compileProbe(probe);
		assertThat(runInOwnJvm(probeClasses, "com.example.fixtures.LoopingReasonFixture", reversed))
				.containsExactly("com.example.fixtures.LoopingReasonFixture$1");
		assertThat(runInOwnJvm(probeClasses, "com.example.fixtures.LoopingReasonOnlyFixture", reversed))
				.containsExactly("com.example.fixtures.LoopingReasonOnlyFixture$1");
	}

	/**
	 * A student failure class that overrides nothing is handed to JUnit as it is
	 * and keeps its text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aPlainStudentFailureKeepsItsText() throws Exception {
		assertThat(messages(run("com.example.fixtures.PlainStudentFailureFixture"))).containsExactly("plain");
	}

	/**
	 * A failure from JUnit's own {@code assertAll} keeps its full text, since its
	 * reporting methods come from opentest4j itself.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void assertAllKeepsItsText() throws Exception {
		assertThat(messages(run("com.example.fixtures.AssertAllFixture"))).singleElement().asString().contains("first")
				.contains("second");
	}

	/**
	 * A failure from {@code assertAll} whose member's message never returns is
	 * reported by its type alone, whether the member is also suppressed in it or
	 * only listed, in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aLoopingAggregateMemberIsNeverCalled(boolean reversed) throws Exception {
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.AssertAllLoopingFixture", reversed))))
						.containsExactly("org.opentest4j.MultipleFailuresError");
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.AggregateLoopingFixture", reversed))))
						.containsExactly("org.opentest4j.MultipleFailuresError");
	}

	/**
	 * A hidden test that aborts stays aborted, not failed, with no text or output,
	 * in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenAbortStaysAnAbort(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenAbortFixture", reversed));
		assertThat(captured.results().testEvents().failed().count()).isZero();
		assertThat(captured.results().testEvents().aborted().stream().map(GeneratedAllHooksTest::messageOf))
				.containsExactly("");
		assertThat(captured.output()).doesNotContain("SECRET_ABORT");
	}

	/**
	 * A hidden failure has no text at all, in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenFailureHasNoText(boolean reversed) throws Exception {
		assertThat(messages(run("com.example.fixtures.HiddenFixture", reversed))).containsExactly("");
	}

	/**
	 * A failure class in the generated package that does not come from the
	 * generated output is not trusted as a hook's marker, so its own
	 * {@code toString} is never handed to JUnit.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aForgedMarkerIsDetached() throws Exception {
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.ForgedMarkerFixture", false, forged))))
						.containsExactly("de.tum.cit.ase.ares.generated.ForgedMarker");
	}

	/**
	 * Output a hidden test leaves unfinished fails the test without showing it, in
	 * either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void unfinishedHiddenOutputFailsSilently(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenUnflushedFixture", reversed));
		assertThat(captured.results().testEvents().failed().count()).isEqualTo(1);
		assertThat(messages(captured.results())).containsExactly("");
		assertThat(captured.output()).doesNotContain("SECRET_UNFLUSHED");
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
	 * @param extra     further class folders to load from.
	 * @return the results.
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults run(String className, boolean reversed, Path... extra) throws Exception {
		List<Path> roots = new ArrayList<>(List.of(compiled, reversed ? reversedResources : resources));
		roots.addAll(Arrays.asList(extra));
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(roots),
				Thread.currentThread().getContextClassLoader())) {
			return GeneratedCodeRuns.runIn(loader, className, Locale.ENGLISH,
					Map.of(GeneratedCodeRuns.AUTODETECTION, "true", GeneratedCodeRuns.AUTODETECTION_INCLUDE, include));
		}
	}

	/** Printed by the probe before each failure message. */
	private static final String PROBE_FAILURE = "ares.probe.failure=";

	/** Printed by the probe once the run has finished. */
	private static final String PROBE_DONE = "ares.probe.done";

	/**
	 * Compiles a probe that runs one fixture class through JUnit with the generated
	 * hooks and prints each failure message.
	 *
	 * @param root the probe's folder.
	 * @return the compiled probe's folder
	 * @throws IOException if writing or compiling fails
	 */
	private static Path compileProbe(Path root) throws IOException {
		Path sources = Files.createDirectories(root.resolve("sources/com/example/probe"));
		Files.writeString(sources.resolve("HooksProbe.java"),
				"""
						package com.example.probe;

						import org.junit.platform.engine.TestExecutionResult;
						import org.junit.platform.engine.discovery.DiscoverySelectors;
						import org.junit.platform.testkit.engine.EngineTestKit;

						public final class HooksProbe {
							public static void main(String[] arguments) {
								EngineTestKit.engine("junit-jupiter").selectors(DiscoverySelectors.selectClass(arguments[0]))
										.configurationParameter("%s", "true").configurationParameter("%s", arguments[1])
										.execute().testEvents().failed().stream()
										.map(event -> event.getPayload(TestExecutionResult.class)
												.flatMap(TestExecutionResult::getThrowable).map(Throwable::getMessage).orElse(""))
										.forEach(message -> System.out.println("%s" + message));
								System.out.println("%s");
								System.exit(0);
							}
						}
						"""
						.formatted(GeneratedCodeRuns.AUTODETECTION, GeneratedCodeRuns.AUTODETECTION_INCLUDE,
								PROBE_FAILURE, PROBE_DONE));
		Path classes = Files.createDirectories(root.resolve("classes"));
		compile(classes, List.of(root.resolve("sources")));
		return classes;
	}

	/**
	 * Runs one fixture class in a JVM of its own and returns its failure messages,
	 * failing if that JVM does not finish in time.
	 *
	 * @param probeClasses the compiled probe.
	 * @param className    the fixture class.
	 * @param reversed     whether the hooks are registered the other way round.
	 * @return the failure messages
	 * @throws Exception if the JVM cannot be started
	 */
	private static List<String> runInOwnJvm(Path probeClasses, String className, boolean reversed) throws Exception {
		String classPath = String.join(File.pathSeparator, compiled.toString(),
				(reversed ? reversedResources : resources).toString(), probeClasses.toString(),
				System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
		Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
				classPath, "com.example.probe.HooksProbe", className, include).redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			assertThat(output).as(output).contains(PROBE_DONE);
			return output.lines().filter(line -> line.startsWith(PROBE_FAILURE))
					.map(line -> line.substring(PROBE_FAILURE.length())).toList();
		} finally {
			process.destroyForcibly();
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
