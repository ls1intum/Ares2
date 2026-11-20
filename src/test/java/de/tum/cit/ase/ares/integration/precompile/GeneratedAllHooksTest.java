package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
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
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

/**
 * Runs all four hooks a precompile run generates together, from one policy that
 * sets every category: one registration loads them all, every sentinel passes,
 * hidden results stay silent, and public failures keep their text.
 */
class GeneratedAllHooksTest {

	/** The generated package. */
	private static final String GENERATED = "de.tum.cit.ase.ares.generated.";

	/**
	 * The package the generated output copies the AspectJ blocked-action class
	 * into.
	 */
	private static final String VIOLATION_PACKAGE = "com.example.ares.api.aop.java.aspectj.adviceandpointcut";

	/**
	 * The path the blocked-action fixture names, which a LOW report must not show.
	 */
	private static final String BLOCKED_PATH = "/secret/solution.txt";

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
	 * A student class named like the generated visibility hook, compiled outside
	 * the generated output, whose visibility check never returns.
	 */
	private static Path lookalikeVisibility;

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
						thisPolicyFileCompliesToThePolicyVersion: 2
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
						      theTimeoutIs: "2 SECONDS"
						    regardingTheOutputOfTheTestCases:
						      theMaximumNumberOfOutputCharactersIs: 100000
						    regardingTheFailureFeedbackForTheTestCases:
						      theSecurityViolationFeedbackLevelIs: LOW
						    regardingTheVisibilityAndScheduleOfTheTestCases:
						      theDeadlineIs: "2000-01-01 00:00 UTC"
						      testsNotListedHereAreHidden: false
						      theFollowingTestsAreHidden: ["com.example.fixtures.HiddenFixture", "com.example.fixtures.HiddenTimeoutFixture", "com.example.fixtures.HiddenLoudFixture", "com.example.fixtures.HiddenFactoryFixture", "com.example.fixtures.HiddenIteratorFactoryFixture", "com.example.fixtures.HiddenUnflushedFixture", "com.example.fixtures.HiddenAbortFixture"]
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
		fixture(fixtures, "HiddenIteratorFactoryFixture",
				"public static final class Cases { public java.util.Iterator<DynamicTest> iterator() {"
						+ " return java.util.stream.Stream.of(1).map(number -> { System.out.print(\"SECRET_ITERATOR\");"
						+ " return DynamicTest.dynamicTest(\"case\", () -> { }); }).iterator(); } }"
						+ " @TestFactory Cases cases() { return new Cases(); }");
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
		fixture(fixtures, "LoopingNameFixture",
				"@Test void fails() throws Exception { javax.naming.NamingException failure = new javax.naming.NamingException(\"naming\");"
						+ " failure.setRemainingName(new javax.naming.CompositeName() { @Override public Object clone() { return this; }"
						+ " @Override public String toString() { while (true) { Thread.onSpinWait(); } } }); throw failure; }");
		fixture(fixtures, "RedactedMarkerFixture",
				"@Test void fails() { Throwable marker = de.tum.cit.ase.ares.generated.GeneratedHiddenTests"
						+ ".redactFailure(new AssertionError(\"x\")); marker.addSuppressed(new IllegalStateException(\"inner\") {"
						+ " @Override public String toString() { while (true) { Thread.onSpinWait(); } } });"
						+ " throw (AssertionError) marker; }");
		fixture(fixtures, "AssertEqualsFixture", "@Test void fails() { Assertions.assertEquals(\"a\", \"b\"); }");
		fixture(fixtures, "BlockedActionFixture",
				"@Test void reads() { throw " + VIOLATION_PACKAGE + ".ViolationSource.blocked(); }");
		fixture(fixtures, "LockedFailureFixture",
				"@Test void fails() throws Exception { IllegalStateException failure = new IllegalStateException(\"locked\");"
						+ " java.util.concurrent.CountDownLatch held = new java.util.concurrent.CountDownLatch(1);"
						+ " Thread holder = new Thread(() -> { synchronized (failure) { held.countDown();"
						+ " while (true) { Thread.onSpinWait(); } } }); holder.setDaemon(true); holder.start(); held.await();"
						+ " throw failure; }");
		fixture(fixtures, "InterruptedFailureFixture",
				"@Test void fails() throws Exception { IllegalStateException failure = new IllegalStateException(\"briefly locked\");"
						+ " java.util.concurrent.CountDownLatch held = new java.util.concurrent.CountDownLatch(1);"
						+ " Thread holder = new Thread(() -> { synchronized (failure) { held.countDown();"
						+ " try { Thread.sleep(500); } catch (InterruptedException stop) { Thread.currentThread().interrupt(); } } });"
						+ " holder.setDaemon(true); holder.start(); held.await(); Thread.currentThread().interrupt(); throw failure; }");
		fixture(fixtures, "ManySuppressedFixture",
				"@Test void fails() { IllegalStateException outer = new IllegalStateException(\"outer\");"
						+ " for (int i = 0; i < 300; i++) { outer.addSuppressed(new IllegalStateException(\"inner \" + i)); }"
						+ " throw outer; }");
		fixture(fixtures, "CompletionViolationFixture",
				"@Test void reads() { throw new java.util.concurrent.CompletionException(" + VIOLATION_PACKAGE
						+ ".ViolationSource.blocked()); }");
		fixture(fixtures, "GroupedViolationFixture", "@Test void reads() { Assertions.assertAll(() -> { throw "
				+ VIOLATION_PACKAGE + ".ViolationSource.blocked(); }); }");
		fixture(fixtures, "MemberOnlyViolationFixture",
				"@Test void reads() { throw new org.opentest4j.MultipleFailuresError(\"group\", java.util.List.of("
						+ VIOLATION_PACKAGE + ".ViolationSource.blocked())); }");
		fixture(fixtures, "ClassNotFoundViolationFixture",
				"@Test void reads() throws Exception { SecurityException violation = " + VIOLATION_PACKAGE
						+ ".ViolationSource.blocked(); throw new ClassNotFoundException(violation.toString(), violation); }");
		fixture(fixtures, "DeepViolationFixture", "@Test void reads() { SecurityException violation = "
				+ VIOLATION_PACKAGE + ".ViolationSource.blocked();"
				+ " RuntimeException outer = new RuntimeException(violation.toString());"
				+ " for (int i = 0; i < 255; i++) { outer.addSuppressed(new IllegalStateException(\"inner \" + i)); }"
				+ " outer.addSuppressed(new RuntimeException(violation)); throw outer; }");
		fixture(fixtures, "QuotedUnreadableViolationFixture",
				"@Test void reads() { RuntimeException unreadable = new RuntimeException(" + VIOLATION_PACKAGE
						+ ".ViolationSource.blocked()) { @Override public synchronized Throwable getCause() {"
						+ " return super.getCause(); } }; throw new RuntimeException(unreadable); }");
		fixture(fixtures, "SuppressedViolationFixture",
				"@Test void reads() { IllegalStateException outer = new IllegalStateException(\"outer\");"
						+ " outer.addSuppressed(" + VIOLATION_PACKAGE + ".ViolationSource.blocked()); throw outer; }");
		Path violationSources = Files
				.createDirectories(tempDir.resolve("violation-sources").resolve(VIOLATION_PACKAGE.replace('.', '/')));
		Files.writeString(violationSources.resolve("ViolationSource.java"), """
				package %s;

				public final class ViolationSource {
					public static SecurityException blocked() {
						return AresSecurityViolation.create("file", "read", "%s", "Ares blocked reading %s");
					}
				}
				""".formatted(VIOLATION_PACKAGE, BLOCKED_PATH, BLOCKED_PATH));
		fixture(fixtures, "AbortFixture", "@Test void aborts() { Assumptions.abort(\"skipped\"); }");
		fixture(fixtures, "AbortWithStudentCauseFixture",
				"@Test void aborts() { throw new org.opentest4j.TestAbortedException(\"skip\", new IllegalStateException(\"cause\")"
						+ " { @Override public String getMessage() { return \"student\"; } }); }");
		fixture(fixtures, "RepeatedGroupFixture",
				"@Test void fails() { org.opentest4j.MultipleFailuresError group = new org.opentest4j.MultipleFailuresError(\"g\","
						+ " java.util.List.of(new AssertionError(\"x\"))); for (int i = 0; i < 40; i++) { group ="
						+ " new org.opentest4j.MultipleFailuresError(\"g\", java.util.List.of(group, group)); } throw group; }");
		fixture(fixtures, "WrappedLoopingCauseFixture",
				"@Test void fails() throws Exception { throw new java.rmi.RemoteException(\"outer\","
						+ " new IllegalStateException(\"cause\") { @Override public String toString() {"
						+ " while (true) { Thread.onSpinWait(); } } }); }");
		fixture(fixtures, "RepeatedSuppressedFixture",
				"@Test void fails() { IllegalStateException outer = new IllegalStateException(\"REPEATED_OUTER\");"
						+ " IllegalStateException inner = new IllegalStateException(\"inner\");"
						+ " for (int i = 0; i < 300; i++) { outer.addSuppressed(inner); } throw outer; }");
		fixture(fixtures, "LoopingRootCauseFixture",
				"@Test void fails() throws Exception { throw new javax.naming.NamingException(\"NAMING_OUTER\") {"
						+ " @Override public Throwable getRootCause() { while (true) { Thread.onSpinWait(); } } }; }");
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
						testSources.resolve("com/example/ares/api/localization"), fixtures,
						tempDir.resolve("violation-sources")),
				testSources.resolve("com/example/ares/api/util/LruCache.java"),
				testSources.resolve(VIOLATION_PACKAGE.replace('.', '/')).resolve("AresSecurityViolation.java"));
		resources = testSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		reversedResources = reversedRegistration(resources, tempDir.resolve("reversed-resources"));
		forged = forgedMarker(tempDir.resolve("forged-sources"), tempDir.resolve("forged"));
		lookalikeVisibility = lookalikeVisibility(tempDir.resolve("lookalike-sources"), tempDir.resolve("lookalike"));
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
		Files.writeString(marker.resolve("GeneratedHiddenTests$HiddenTestFailure.java"), """
				package de.tum.cit.ase.ares.generated;

				public class GeneratedHiddenTests$HiddenTestFailure extends AssertionError {
					public GeneratedHiddenTests$HiddenTestFailure() { super("lookalike"); }
					@Override public String toString() { while (true) { Thread.onSpinWait(); } }
				}
				""");
		Path fixtures = Files.createDirectories(sources.resolve("com/example/fixtures"));
		fixture(fixtures, "ForgedMarkerFixture",
				"@Test void fails() { throw new de.tum.cit.ase.ares.generated.ForgedMarker(); }");
		fixture(fixtures, "LookalikeHiddenFixture",
				"@Test void fails() { throw new de.tum.cit.ase.ares.generated.GeneratedHiddenTests$HiddenTestFailure(); }");
		compile(Files.createDirectories(output), List.of(sources));
		return output;
	}

	/**
	 * Compiles a student class named like the generated visibility hook whose
	 * visibility check never returns, outside the generated output.
	 *
	 * @param sources the sources' folder.
	 * @param output  the compiled classes' folder.
	 * @return the compiled classes' folder
	 * @throws IOException if writing or compiling fails
	 */
	private static Path lookalikeVisibility(Path sources, Path output) throws IOException {
		Path hook = Files.createDirectories(sources.resolve("de/tum/cit/ase/ares/generated"));
		Files.writeString(hook.resolve("GeneratedHiddenTests.java"), """
				package de.tum.cit.ase.ares.generated;

				import org.junit.jupiter.api.extension.Extension;
				import org.junit.jupiter.api.extension.ExtensionContext;

				public final class GeneratedHiddenTests implements Extension {
					public static boolean isHiddenContext(ExtensionContext context) {
						while (true) { Thread.onSpinWait(); }
					}
				}
				""");
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
	 * since the hooks read them while still guarding the factory; its dynamic test
	 * still runs and passes, so the silence is not a factory that never ran.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenFactoryPrintsNothing(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenFactoryFixture", reversed));
		assertThat(captured.results().containerEvents().failed().count()).isZero();
		assertThat(captured.results().testEvents().succeeded().stream()
				.map(event -> event.getTestDescriptor().getDisplayName())).containsExactly("case");
		assertThat(captured.output()).doesNotContain("SECRET_FACTORY");
	}

	/**
	 * A hidden factory whose result only has a public {@code iterator()}, which
	 * JUnit also accepts, prints nothing either, since the hooks read it while
	 * still guarding the factory; its dynamic test still runs and passes.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aHiddenIteratorOnlyFactoryPrintsNothing(boolean reversed) throws Exception {
		Captured captured = captured(() -> run("com.example.fixtures.HiddenIteratorFactoryFixture", reversed));
		assertThat(captured.results().containerEvents().failed().count()).isZero();
		assertThat(captured.results().testEvents().succeeded().stream()
				.map(event -> event.getTestDescriptor().getDisplayName())).containsExactly("case");
		assertThat(captured.output()).doesNotContain("SECRET_ITERATOR");
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
				() -> run("com.example.fixtures.SuppressedLoopingFixture", reversed)))).containsExactly("outer");
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
	 * A student failure class that overrides nothing keeps its text in the copy
	 * JUnit gets, after its type.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aPlainStudentFailureKeepsItsText() throws Exception {
		assertThat(messages(run("com.example.fixtures.PlainStudentFailureFixture")))
				.containsExactly("com.example.fixtures.PlainStudentFailureFixture$1: plain");
	}

	/**
	 * A runtime {@code NamingException} holding a student name whose
	 * {@code toString} never returns is reported with its text, and printing the
	 * report runs no student code, with all hooks in either order and with failure
	 * reporting alone, each in a JVM of its own.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @param probe    where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aLoopingNameInARuntimeFailureIsNeverCalled(boolean reversed, @TempDir Path probe) throws Exception {
		Path probeClasses = compileProbe(probe);
		assertThat(runInOwnJvm(probeClasses, "com.example.fixtures.LoopingNameFixture", reversed, include))
				.containsExactly("javax.naming.NamingException: naming");
		assertThat(runInOwnJvm(probeClasses, "com.example.fixtures.LoopingNameFixture", reversed,
				GENERATED + "GeneratedFailureReporting")).containsExactly("javax.naming.NamingException: naming");
	}

	/**
	 * A failed {@code assertEquals} keeps its exact text, since the copy rebuilds
	 * the same opentest4j failure.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void anAssertEqualsFailureKeepsItsText() throws Exception {
		assertThat(messages(run("com.example.fixtures.AssertEqualsFixture")))
				.containsExactly("expected: <a> but was: <b>");
	}

	/**
	 * A blocked action thrown in a timed test still follows the feedback level, so
	 * at LOW the report shows the fixed text and never the blocked path, with the
	 * hooks in either order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aBlockedActionInATimedTestFollowsTheFeedbackLevel(boolean reversed) throws Exception {
		assertThat(messages(run("com.example.fixtures.BlockedActionFixture", reversed))).containsExactly(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.");
	}

	/**
	 * With failure reporting alone, a failure another thread keeps locked ends the
	 * test run once the time limit passes, instead of hanging it, in a JVM of its
	 * own.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aLockedFailureEndsTheRun(@TempDir Path probe) throws Exception {
		assertThat(exitCodeInOwnJvm(compileProbe(probe), "com.example.fixtures.LockedFailureFixture",
				GENERATED + "GeneratedFailureReporting")).isEqualTo(124);
	}

	/**
	 * With failure reporting alone, a test that interrupts itself and then fails is
	 * still reported once its failure can be read, instead of ending the run, in a
	 * JVM of its own.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void anInterruptedFailureIsStillReported(@TempDir Path probe) throws Exception {
		assertThat(runInOwnJvm(compileProbe(probe), "com.example.fixtures.InterruptedFailureFixture", false,
				GENERATED + "GeneratedFailureReporting")).containsExactly("briefly locked");
	}

	/**
	 * A failure with more suppressed failures than the copy budget is reported with
	 * at most that many, in either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aCopyKeepsWithinItsBudget(boolean reversed) throws Exception {
		List<Throwable> failures = run("com.example.fixtures.ManySuppressedFixture", reversed).testEvents().failed()
				.stream().map(event -> event.getPayload(TestExecutionResult.class)
						.flatMap(TestExecutionResult::getThrowable).orElseThrow())
				.toList();
		assertThat(failures).singleElement()
				.satisfies(failure -> assertThat(failure.getSuppressed()).hasSizeLessThan(256));
	}

	/**
	 * A blocked action wrapped in a {@code CompletionException} still follows the
	 * feedback level, since its copy keeps the blocked action as its cause.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aWrappedBlockedActionFollowsTheFeedbackLevel(boolean reversed) throws Exception {
		assertThat(messages(run("com.example.fixtures.CompletionViolationFixture", reversed))).containsExactly(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.");
	}

	/**
	 * A blocked action inside an {@code assertAll} group, inside a group built
	 * directly, attached as a suppressed failure, or the cause of a failure whose
	 * class fixes its cause when built, still follows the feedback level, so its
	 * path never reaches the report.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @param fixture  the fixture carrying the blocked action
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@CsvSource({ "false, GroupedViolationFixture", "true, GroupedViolationFixture", "false, SuppressedViolationFixture",
			"true, SuppressedViolationFixture", "false, MemberOnlyViolationFixture", "true, MemberOnlyViolationFixture",
			"false, ClassNotFoundViolationFixture", "true, ClassNotFoundViolationFixture" })
	void anAttachedBlockedActionFollowsTheFeedbackLevel(boolean reversed, String fixture) throws Exception {
		assertThat(messages(run("com.example.fixtures." + fixture, reversed))).containsExactly(
				"Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.");
	}

	/**
	 * A failure too large to search to the end shows no text of its own, in either
	 * registration order, since the blocked action it holds may lie past the
	 * budget.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aFailureTooLargeToSearchShowsNoText(boolean reversed) throws Exception {
		List<String> messages = messages(run("com.example.fixtures.DeepViolationFixture", reversed));
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0)).doesNotContain(BLOCKED_PATH);
	}

	/**
	 * A failure whose message quotes a failure with an unreadable cause, which
	 * holds a blocked action, shows no text of its own in either registration
	 * order: the search cannot pass the unreadable cause, so it is not complete.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aQuotedUnreadableCauseShowsNoText(boolean reversed) throws Exception {
		List<String> messages = messages(run("com.example.fixtures.QuotedUnreadableViolationFixture", reversed));
		assertThat(messages).containsExactly("java.lang.RuntimeException");
	}

	/**
	 * A failure listing one suppressed failure more often than the search budget
	 * allows shows no text of its own when failure reporting runs alone, since the
	 * search stopped at the budget while queuing. With the timeout first, its copy
	 * keeps each suppressed failure once, so the search completes.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aFailureRepeatingOneSuppressedFailureShowsNoText() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiled, resources)),
				Thread.currentThread().getContextClassLoader())) {
			List<String> messages = messages(
					GeneratedCodeRuns.runIn(loader, "com.example.fixtures.RepeatedSuppressedFixture", Locale.ENGLISH,
							Map.of(GeneratedCodeRuns.AUTODETECTION, "true", GeneratedCodeRuns.AUTODETECTION_INCLUDE,
									GENERATED + "GeneratedFailureReporting")));
			assertThat(messages).hasSize(1);
			assertThat(messages.get(0)).doesNotContain("REPEATED_OUTER");
		}
		assertThat(messages(run("com.example.fixtures.RepeatedSuppressedFixture"))).containsExactly("REPEATED_OUTER");
	}

	/**
	 * A runtime failure whose {@code getCause} calls a student override that never
	 * returns, as {@code NamingException}'s does, is reported quickly without
	 * reading its cause or its text.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aCauseReadThroughAStudentOverrideIsNotRead(boolean reversed) throws Exception {
		List<String> messages = messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.LoopingRootCauseFixture", reversed)));
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0)).doesNotContain("NAMING_OUTER");
	}

	/**
	 * An abort whose cause is a student failure stays an abort with its reason, in
	 * either registration order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void anAbortWithAStudentCauseStaysAnAbort(boolean reversed) throws Exception {
		EngineExecutionResults results = run("com.example.fixtures.AbortWithStudentCauseFixture", reversed);
		assertThat(results.testEvents().failed().count()).isZero();
		assertThat(results.testEvents().aborted().stream().map(GeneratedAllHooksTest::messageOf))
				.containsExactly("skip");
	}

	/**
	 * A group listing the same group many times is reported by its type alone and
	 * quickly, since reading its text would repeat each shared member.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aRepeatedGroupIsReportedByItsType(boolean reversed) throws Exception {
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.RepeatedGroupFixture", reversed))))
						.containsExactly("org.opentest4j.MultipleFailuresError");
	}

	/**
	 * A runtime failure whose text is built from a student cause that never returns
	 * is reported by its type alone, without reading that text.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void aWrapperOfALoopingCauseIsReportedByItsType(boolean reversed) throws Exception {
		assertThat(messages(assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> run("com.example.fixtures.WrappedLoopingCauseFixture", reversed))))
						.containsExactly("java.rmi.RemoteException");
	}

	/**
	 * A test that aborts stays aborted with its reason, in either registration
	 * order.
	 *
	 * @param reversed whether the hooks are registered the other way round.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void anAbortStaysAnAbort(boolean reversed) throws Exception {
		EngineExecutionResults results = run("com.example.fixtures.AbortFixture", reversed);
		assertThat(results.testEvents().failed().count()).isZero();
		assertThat(results.testEvents().aborted().stream().map(GeneratedAllHooksTest::messageOf))
				.containsExactly("skipped");
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
	 * With failure reporting alone, a student class named like a hidden-test marker
	 * but loaded from the student's own output is copied like any other failure, so
	 * its looping {@code toString} never runs when the build tool prints it.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aLookalikeHiddenMarkerIsCopied(@TempDir Path probe) throws Exception {
		Process process = startInOwnJvm(compileProbe(probe), "com.example.fixtures.LookalikeHiddenFixture", false,
				GENERATED + "GeneratedFailureReporting", forged);
		try {
			java.util.concurrent.CompletableFuture<String> drained = GeneratedCodeRuns.drain(process);
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
			String output = drained.get(10, TimeUnit.SECONDS);
			assertThat(output).as(output).contains(PROBE_DONE)
					.contains(PROBE_FAILURE + "de.tum.cit.ase.ares.generated.GeneratedHiddenTests$HiddenTestFailure");
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * With failure reporting and visibility but no strict timeout, a genuine hidden
	 * marker a public test obtained and attached a looping failure to is reported
	 * as a new marker, so printing it the way a build tool does never runs that
	 * failure.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aHiddenMarkerWithAttachmentsIsReportedAnew(@TempDir Path probe) throws Exception {
		List<String> failures = runInOwnJvm(compileProbe(probe), "com.example.fixtures.RedactedMarkerFixture", false,
				GENERATED + "GeneratedFailureReporting," + GENERATED + "GeneratedHiddenTests");
		assertThat(failures).containsExactly("");
	}

	/**
	 * A student class named like the visibility hook, placed before the generated
	 * output, is never called after a timeout: the strict timeout reports the
	 * timeout instead of hanging on it.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aLookalikeVisibilityHookIsNotCalledAfterATimeout(@TempDir Path probe) throws Exception {
		assertLookalikeIgnored(probe, "com.example.fixtures.HiddenTimeoutFixture",
				GENERATED + "GeneratedStrictTimeout");
	}

	/**
	 * A student class named like the visibility hook, placed before the generated
	 * output, is never called before a test by the output hook.
	 *
	 * @param probe where the probe is compiled.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aLookalikeVisibilityHookIsNotCalledBeforeATest(@TempDir Path probe) throws Exception {
		assertLookalikeIgnored(probe, "com.example.fixtures.PublicFixture", GENERATED + "GeneratedOutputMirroring");
	}

	/**
	 * With the visibility hook generated but its classes missing, the output hook
	 * treats a test as hidden: unfinished output fails it without any text.
	 *
	 * @param withoutVisibility where the output without the visibility hook goes.
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aMissingVisibilityHookHidesOutput(@TempDir Path withoutVisibility) throws Exception {
		Captured captured = captured(() -> runWithout(withoutVisibility, "com.example.fixtures.HiddenUnflushedFixture",
				GENERATED + "GeneratedOutputMirroring"));
		assertThat(captured.results().testEvents().failed().count()).isEqualTo(1);
		assertThat(messages(captured.results())).containsExactly("");
		assertThat(captured.output()).doesNotContain("SECRET_UNFLUSHED");
	}

	/**
	 * With the visibility hook generated but its classes missing, the timeout hook
	 * treats a test as hidden and its timeout carries no text.
	 *
	 * @param withoutVisibility where the output without the visibility hook goes.
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aMissingVisibilityHookHidesATimeout(@TempDir Path withoutVisibility) throws Exception {
		EngineExecutionResults results = runWithout(withoutVisibility, "com.example.fixtures.HiddenTimeoutFixture",
				GENERATED + "GeneratedStrictTimeout");
		assertThat(results.testEvents().failed().count()).isEqualTo(1);
		assertThat(messages(results)).containsExactly("");
	}

	/**
	 * No public field of the generated settings or visibility classes holds a test
	 * name, so student code cannot read which tests are hidden.
	 *
	 * @throws Exception if the classes cannot be loaded
	 */
	@Test
	void noPublicFieldNamesAHiddenTest() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiled)),
				Thread.currentThread().getContextClassLoader())) {
			for (String name : List.of("GeneratedTestBehaviorSettings", "GeneratedHiddenTests")) {
				for (java.lang.reflect.Field field : Class.forName(GENERATED + name, true, loader).getFields()) {
					Object value = java.lang.reflect.Modifier.isStatic(field.getModifiers()) ? field.get(null) : null;
					assertThat(contentsOf(value)).as(name + "." + field.getName())
							.doesNotContain("com.example.fixtures.HiddenFixture");
				}
			}
		}
	}

	/**
	 * The text a field value holds, element by element for an array, a collection
	 * or a map, so a test name inside one is found.
	 *
	 * @param value the value.
	 * @return its text
	 */
	private static String contentsOf(Object value) {
		if (value instanceof char[] characters) {
			return new String(characters);
		}
		if (value != null && value.getClass().isArray()) {
			StringBuilder text = new StringBuilder();
			for (int index = 0; index < java.lang.reflect.Array.getLength(value); index++) {
				text.append(contentsOf(java.lang.reflect.Array.get(value, index))).append('\n');
			}
			return text.toString();
		}
		if (value instanceof Iterable<?> elements) {
			StringBuilder text = new StringBuilder();
			elements.forEach(element -> text.append(contentsOf(element)).append('\n'));
			return text.toString();
		}
		if (value instanceof Map<?, ?> map) {
			return contentsOf(map.keySet()) + contentsOf(map.values());
		}
		return String.valueOf(value);
	}

	/**
	 * Every generated hook checks the JUnit engine running the tests: it loads
	 * under this run's engine, accepts 5.12 and newer, and refuses an older or
	 * unreadable version, such as 5.11.4 from a parent, naming it in both locales.
	 *
	 * @param hook the generated hook's simple name.
	 * @throws Exception if the hook cannot be loaded
	 */
	@ParameterizedTest
	@ValueSource(strings = { "GeneratedStrictTimeout", "GeneratedHiddenTests", "GeneratedOutputMirroring",
			"GeneratedFailureReporting" })
	void everyHookRefusesAnOldJUnitEngine(String hook) throws Exception {
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiled, resources)),
				Thread.currentThread().getContextClassLoader())) {
			Class<?> generated = Class.forName(GENERATED + hook, true, loader);
			java.lang.reflect.Method reads = generated.getDeclaredMethod("readsExtensionFilter", String.class);
			reads.setAccessible(true);
			java.lang.reflect.Method loaded = generated.getDeclaredMethod("loadedJUnitEngineVersion");
			loaded.setAccessible(true);
			assertThat(reads.invoke(null, loaded.invoke(null))).isEqualTo(true);
			for (String accepted : List.of("5.12.0", "5.12.0-M1", "5.14.1", "6.1.3", "10.0")) {
				assertThat(reads.invoke(null, accepted)).as(accepted).isEqualTo(true);
			}
			for (String refused : Arrays.asList("5.11.4", "5.9", "4.13.2", "5", "5.x", "", "unknown", null)) {
				assertThat(reads.invoke(null, refused)).as(String.valueOf(refused)).isEqualTo(false);
			}
			java.lang.reflect.Method require = generated.getDeclaredMethod("requireSupportedJUnitEngine", String.class);
			require.setAccessible(true);
			assertThat(engineRefusal(require, "5.11.4", Locale.ENGLISH)).contains("version 5.11.4",
					"JUnit 5.12 or newer");
			assertThat(engineRefusal(require, null, Locale.GERMAN)).contains("die Version ?", "JUnit 5.12 oder neuer");
		}
	}

	/**
	 * The message a generated hook's engine check fails with for a version.
	 *
	 * @param require the hook's check.
	 * @param version the engine version to check.
	 * @param locale  the locale to fail in.
	 * @return the failure's message
	 * @throws Exception if the check accepts the version
	 */
	private static String engineRefusal(java.lang.reflect.Method require, String version, Locale locale)
			throws Exception {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			require.invoke(null, version);
		} catch (java.lang.reflect.InvocationTargetException refusal) {
			assertThat(refusal.getCause()).isInstanceOf(IllegalStateException.class);
			return refusal.getCause().getMessage();
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
		throw new AssertionError("The engine check accepted " + version);
	}

	/**
	 * Runs one class with only one hook registered, on a copy of the generated
	 * output and its registration without the visibility hook.
	 *
	 * @param target    where the copy goes.
	 * @param className the class to run.
	 * @param hook      the one hook to register.
	 * @return the results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults runWithout(Path target, String className, String hook) throws Exception {
		Path classes = target.resolve("classes");
		Path registration = target.resolve("resources");
		copyWithoutVisibility(compiled, classes);
		copyWithoutVisibility(resources, registration);
		Path services = registration.resolve("META-INF/services/org.junit.jupiter.api.extension.Extension");
		Files.write(services, Files.readAllLines(services).stream()
				.filter(line -> !line.startsWith(GENERATED) || line.equals(hook)).toList());
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(classes, registration)),
				Thread.currentThread().getContextClassLoader())) {
			return GeneratedCodeRuns.runIn(loader, className, Locale.ENGLISH,
					Map.of(GeneratedCodeRuns.AUTODETECTION, "true", GeneratedCodeRuns.AUTODETECTION_INCLUDE, hook));
		}
	}

	/**
	 * Copies a folder, leaving out every file of the visibility hook.
	 *
	 * @param from the folder to copy.
	 * @param to   where the copy goes.
	 * @throws IOException if copying fails
	 */
	private static void copyWithoutVisibility(Path from, Path to) throws IOException {
		try (java.util.stream.Stream<Path> files = Files.walk(from)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				if (!file.getFileName().toString().startsWith("GeneratedHiddenTests")) {
					Path copy = to.resolve(from.relativize(file).toString());
					Files.createDirectories(copy.getParent());
					Files.copy(file, copy);
				}
			}
		}
	}

	/**
	 * Runs one fixture with one hook and the lookalike visibility hook first on the
	 * class path, and checks that the run finished with one failure.
	 *
	 * @param probe     where the probe is compiled.
	 * @param className the fixture class.
	 * @param hooks     the hook to load.
	 * @throws Exception if compiling or starting the JVM fails
	 */
	private static void assertLookalikeIgnored(Path probe, String className, String hooks) throws Exception {
		Process process = startInOwnJvm(compileProbe(probe), className, false, hooks, lookalikeVisibility);
		try {
			java.util.concurrent.CompletableFuture<String> drained = GeneratedCodeRuns.drain(process);
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
			String output = drained.get(10, TimeUnit.SECONDS);
			assertThat(output).as(output).contains(PROBE_DONE).containsOnlyOnce(PROBE_FAILURE);
		} finally {
			process.destroyForcibly();
		}
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
												.flatMap(TestExecutionResult::getThrowable).map(HooksProbe::rendered).orElse(""))
										.forEach(message -> System.out.println("%s" + message));
								System.out.println("%s");
								System.exit(0);
							}

							private static String rendered(Throwable failure) {
								failure.printStackTrace(new java.io.PrintWriter(new java.io.StringWriter()));
								return failure.getMessage();
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
		return runInOwnJvm(probeClasses, className, reversed, include);
	}

	/**
	 * Runs one fixture class in a JVM of its own with only the hooks a pattern
	 * names, printing each failure as a build tool would, and returns its failure
	 * messages.
	 *
	 * @param probeClasses the compiled probe.
	 * @param className    the fixture class.
	 * @param reversed     whether the hooks are registered the other way round.
	 * @param hooks        the hooks to load, as JUnit's autodetection pattern.
	 * @return the failure messages
	 * @throws Exception if the JVM cannot be started
	 */
	private static List<String> runInOwnJvm(Path probeClasses, String className, boolean reversed, String hooks)
			throws Exception {
		Process process = startInOwnJvm(probeClasses, className, reversed, hooks);
		try {
			java.util.concurrent.CompletableFuture<String> drained = GeneratedCodeRuns.drain(process);
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
			String output = drained.get(10, TimeUnit.SECONDS);
			assertThat(output).as(output).contains(PROBE_DONE);
			return output.lines().filter(line -> line.startsWith(PROBE_FAILURE))
					.map(line -> line.substring(PROBE_FAILURE.length())).toList();
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * Runs one fixture class in a JVM of its own with only the hooks a pattern
	 * names and returns that JVM's exit code, failing if it does not end in time.
	 *
	 * @param probeClasses the compiled probe.
	 * @param className    the fixture class.
	 * @param hooks        the hooks to load, as JUnit's autodetection pattern.
	 * @return the exit code
	 * @throws Exception if the JVM cannot be started
	 */
	private static int exitCodeInOwnJvm(Path probeClasses, String className, String hooks) throws Exception {
		Process process = startInOwnJvm(probeClasses, className, false, hooks);
		GeneratedCodeRuns.drain(process);
		try {
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe ended").isTrue();
			return process.exitValue();
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * Starts the probe on one fixture class in a JVM of its own.
	 *
	 * @param probeClasses the compiled probe.
	 * @param className    the fixture class.
	 * @param reversed     whether the hooks are registered the other way round.
	 * @param hooks        the hooks to load, as JUnit's autodetection pattern.
	 * @param first        class folders placed before the generated output.
	 * @return the started process
	 * @throws IOException if the JVM cannot be started
	 */
	private static Process startInOwnJvm(Path probeClasses, String className, boolean reversed, String hooks,
			Path... first) throws IOException {
		List<String> entries = new ArrayList<>(Arrays.stream(first).map(Path::toString).toList());
		entries.addAll(List.of(compiled.toString(), (reversed ? reversedResources : resources).toString(),
				probeClasses.toString(),
				System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"))));
		String classPath = String.join(File.pathSeparator, entries);
		return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", classPath,
				"com.example.probe.HooksProbe", className, hooks).redirectErrorStream(true).start();
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

	/**
	 * Runs the output mirroring a precompile run generates, end to end: the real
	 * generator writes it into a project, the generated sources are compiled with
	 * fixture tests, and nested JUnit runs show the same limit, echo and errors as
	 * {@code IOTester} gives in postcompile.
	 */
	@Nested
	class OutputMirroring {

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
			writeHiddenVisibilityFixture(silentSources);
			resources = silentSources.resolveSibling("resources");
			include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
					.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
					.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
			silent = compileProject("compiled-silent", silentSources, fixtures);
			Path mirroredSources = generate("mirrored", true);
			writeHiddenVisibilityFixture(mirroredSources);
			mirrored = compileProject("compiled-mirrored", mirroredSources, fixtures);
			shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
			Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
					.resolve("GeneratedTestBehaviorSettings.java");
			Files.writeString(shadowSource,
					Files.readString(
							silentSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"))
							.replace("= 10L;", "= 100000000L;"));
			compile(shadowSettings, List.of(), shadowSource);
		}

		/** Writes the generated visibility type used by the combined output hook. */
		private static void writeHiddenVisibilityFixture(Path testSources) throws IOException {
			Path folder = Files.createDirectories(testSources.resolve("de/tum/cit/ase/ares/generated"));
			Files.writeString(folder.resolve("GeneratedHiddenTests.java"),
					"""
							package de.tum.cit.ase.ares.generated;

							public final class GeneratedHiddenTests implements org.junit.jupiter.api.extension.BeforeEachCallback,
									org.junit.jupiter.api.extension.AfterEachCallback {
								public static boolean isHiddenContext(org.junit.jupiter.api.extension.ExtensionContext context) {
									return context.getRequiredTestClass().getSimpleName().startsWith("Hidden");
								}
								@Override public void beforeEach(org.junit.jupiter.api.extension.ExtensionContext context) {}
								@Override public void afterEach(org.junit.jupiter.api.extension.ExtensionContext context) {}
							}
							""");
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

		/** Generated hidden visibility suppresses both streams with mirroring on. */
		@Test
		void hiddenVisibilityOverridesMirroring() throws Exception {
			NestedRun run = run(mirrored, "HiddenFixture", Locale.ENGLISH);
			assertThat(run.results().testEvents().succeeded().count()).isEqualTo(1);
			assertThat(run.console()).doesNotContain("HOUT", "HERR");
		}

		/** Hidden output stays absent with either extension registration order. */
		@Test
		void hiddenMirroringHookOrdersAreSilent() throws Exception {
			Map<String, String> noAutodetection = Map.of("junit.jupiter.extensions.autodetection.enabled", "false");
			for (String fixture : List.of("HiddenMirrorHiddenFirst", "HiddenMirrorMirrorFirst")) {
				NestedRun run = run(mirrored, fixture, Locale.ENGLISH, noAutodetection);
				assertThat(run.results().testEvents().succeeded().count()).as(fixture).isEqualTo(1);
				assertThat(run.results().testEvents().failed().count()).as(fixture).isZero();
				assertThat(run.console()).as(fixture).doesNotContain("HOUT", "HERR");
			}
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
		 * A character whose two UTF-8 bytes arrive in separate flushed writes is valid,
		 * since the first byte is kept until the second arrives.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aCharacterSplitAcrossFlushesIsValid() throws Exception {
			EngineExecutionResults results = run(silent, "SplitUtf8Fixture", Locale.ENGLISH).results();

			assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
			assertThat(results.allEvents().failed().count()).isZero();
		}

		/**
		 * A character still unfinished when the test ends fails the test with the
		 * localised message, while text printed without a final flush passes.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aCharacterUnfinishedAtTheEndFails() throws Exception {
			String prefix = bundle(Locale.ENGLISH).getString("output_tester.output_is_invalid_utf8").split("%s")[0];

			assertThat(failureMessages(run(silent, "UnfinishedUtf8Fixture", Locale.ENGLISH).results())).singleElement()
					.asString().startsWith(prefix);
			EngineExecutionResults unflushed = run(silent, "UnflushedTextFixture", Locale.ENGLISH).results();

			assertThat(unflushed.testEvents().succeeded().count()).isEqualTo(1);
			assertThat(unflushed.allEvents().failed().count()).isZero();
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
		 * refused with the localised message, and the console is intact afterwards. The
		 * test that installed them stays inside its body until the other test's attempt
		 * has finished, waiting at most ten seconds, so the two overlap.
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
			NestedRun run = run(List.of(shadowSettings, silent), "com.example.fixtures.TestMethodFixture",
					Locale.ENGLISH, Map.of());

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
		private static NestedRun runIn(ClassLoader loader, String className, Locale locale,
				Map<String, String> parameters) throws Exception {
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
			List<Path> entries = new ArrayList<>(roots);
			entries.add(resources);
			return GeneratedCodeRuns.classPath(entries);
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
			return GeneratedCodeRuns.failureMessages(results);
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
					thisPolicyFileCompliesToThePolicyVersion: 2
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
					    regardingTheOutputOfTheTestCases:
					      theOutputIsMirrored: %s
					      theMaximumNumberOfOutputCharactersIs: 10
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
			fixture(folder, "BeforeEachFixture", "@BeforeEach void setUp() { " + longLine + " }",
					"@Test void test() {}");
			fixture(folder, "AfterEachFixture", "@AfterEach void tearDown() { " + longLine + " }",
					"@Test void test() {}");
			fixture(folder, "BeforeAllFixture", "@BeforeAll static void setUp() { " + longLine + " }",
					"@Test void test() {}");
			fixture(folder, "ShortFixture", "", "@Test void writes() { System.out.println(\"hi\"); }");
			fixture(folder, "HiddenFixture", "",
					"@Test void writes() { System.out.print(\"HOUT\"); System.err.print(\"HERR\"); }");
			fixture(folder, "HiddenMirrorHiddenFirst", "",
					"@org.junit.jupiter.api.extension.ExtendWith({ de.tum.cit.ase.ares.generated.GeneratedHiddenTests.class, de.tum.cit.ase.ares.generated.GeneratedOutputMirroring.class }) @Test void writes() { System.out.print(\"HOUT\"); System.err.print(\"HERR\"); }");
			fixture(folder, "HiddenMirrorMirrorFirst", "",
					"@org.junit.jupiter.api.extension.ExtendWith({ de.tum.cit.ase.ares.generated.GeneratedOutputMirroring.class, de.tum.cit.ase.ares.generated.GeneratedHiddenTests.class }) @Test void writes() { System.out.print(\"HOUT\"); System.err.print(\"HERR\"); }");
			fixture(folder, "InvalidUtf8Fixture", "",
					"@Test void writes() { System.out.write(new byte[] { (byte) 0xC3, (byte) 0x28 }, 0, 2); System.out.flush(); }");
			fixture(folder, "UnfinishedUtf8Fixture", "",
					"@Test void writes() { System.out.write(new byte[] { (byte) 0xC3 }, 0, 1); System.out.flush(); }");
			fixture(folder, "UnflushedTextFixture", "", "@Test void writes() { System.out.print(\"\u00e4 done\"); }");
			fixture(folder, "SplitUtf8Fixture", "",
					"@Test void writes() { System.out.write(new byte[] { (byte) 0xC3 }, 0, 1); System.out.flush();"
							+ " System.out.write(new byte[] { (byte) 0xA4 }, 0, 1); System.out.flush(); }");
			fixture(folder, "ClosedFixture", "",
					"@Test void writes() { System.out.close(); System.out.print(\"x\"); Assertions.assertTrue(System.out.checkError()); }");
			fixture(folder, "ParallelFixture",
					"""
							static final java.util.concurrent.CountDownLatch ATTEMPTED = new java.util.concurrent.CountDownLatch(1);
							@org.junit.jupiter.api.extension.RegisterExtension
							static final org.junit.jupiter.api.extension.AfterEachCallback ATTEMPT_DONE = context -> ATTEMPTED.countDown();""",
					"@Test void first() throws InterruptedException { ATTEMPTED.await(10, java.util.concurrent.TimeUnit.SECONDS); }"
							+ " @Test void second() throws InterruptedException { ATTEMPTED.await(10, java.util.concurrent.TimeUnit.SECONDS); }");
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
			GeneratedCodeRuns.compile(output, folders, singleFiles);
		}
	}

	/**
	 * Runs the hidden-test hook a precompile run generates, end to end: the real
	 * generator writes it into projects whose policies put the deadline in the
	 * past, in the future, or ahead of an always-run-before date, some hiding
	 * unlisted tests, the output is compiled with fixture tests, and nested JUnit
	 * runs show hidden tests held back and their failures hidden on every path
	 * {@code @HiddenTest} covers, and public tests left alone.
	 */
	@Nested
	class HiddenTests {

		/** The generated extension's sentinel test. */
		private static final String SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedHiddenTestsSentinelTest";

		/** The entries every policy of this class lists. */
		private static final List<String> HIDDEN = List.of("com.example.fixtures.MethodFixture",
				"com.example.fixtures.TemplateFixture", "com.example.fixtures.FactoryFixture",
				"com.example.fixtures.LifecycleFixture", "com.example.fixtures.ConstructorFixture",
				"com.example.fixtures.ForgedFixture", "com.example.fixtures.Outer",
				"com.example.fixtures.Nesting.Inner", "com.example.fixtures.Methods#listed",
				"com.example.fixtures.Shown", "com.example.fixtures.Inherits#glides",
				"com.example.fixtures.MixedFixture#secret", "com.example.fixtures.DivingBase#dives",
				"com.example.fixtures.SourcedFixture", "com.example.fixtures.CrossingFactoryFixture");

		/** The public entries of the policies that do not hide unlisted tests. */
		private static final List<String> PUBLIC = List.of("com.example.fixtures.Shown#shown");

		/** The hidden entries of the policies that hide unlisted tests. */
		private static final List<String> VISIBILITY_HIDDEN = List.of("com.example.fixtures.Shown#secret");

		/** The public entries of the policies that hide unlisted tests. */
		private static final List<String> VISIBILITY_PUBLIC = List.of("com.example.fixtures.Shown",
				"com.example.fixtures.Layered.Inner");

		/**
		 * The generated ArchUnit security test class of the {@code com.example}
		 * exercise.
		 */
		private static final String ARCHUNIT_TEST = "com.example.ares.api.architecture.java.archunit.JavaArchunitTestCase";

		/**
		 * The generated WALA security test class of the {@code com.example} exercise.
		 */
		private static final String WALA_TEST = "com.example.ares.api.architecture.java.wala.JavaWalaTestCase";

		/** The projects shared by every test of this class. */
		@TempDir
		static Path tempDir;

		/** Generated code and fixtures, from a policy whose deadline has passed. */
		private static Path past;

		/** Generated code and fixtures, from a policy whose deadline lies ahead. */
		private static Path future;

		/**
		 * Generated code and fixtures, from a policy still before its always-run date.
		 */
		private static Path active;

		/**
		 * A settings class with a passed deadline and no list, as a student could
		 * replace it.
		 */
		private static Path shadowSettings;

		/** A project whose hidden list is too long for one generated literal. */
		private static Path longFuture;

		/**
		 * The long project's settings class, replaced with a passed deadline and empty
		 * lists.
		 */
		private static Path longShadowSettings;

		/**
		 * Generated code and fixtures, from a policy hiding unlisted tests whose
		 * deadline lies ahead.
		 */
		private static Path visibility;

		/**
		 * Generated code and fixtures, from a policy hiding unlisted tests whose
		 * deadline has passed.
		 */
		private static Path visibilityPast;

		/**
		 * A settings class saying unlisted tests are not hidden, as a student could
		 * replace it.
		 */
		private static Path shadowVisibility;

		/**
		 * Generated code and fixtures whose always-run-before date starts far ahead and
		 * can be moved into the past by the fixture itself, so a run crosses it at a
		 * known point.
		 */
		private static Path crossing;

		/** The resources of the future project, holding the bundles. */
		private static Path resources;

		/**
		 * The include value the generator wrote into {@code junit-platform.properties}.
		 */
		private static String include;

		/**
		 * Generates the precompile output for three policies and compiles each with the
		 * fixtures.
		 *
		 * @throws IOException if writing or compiling fails
		 */
		@BeforeAll
		static void generateAndCompile() throws IOException {
			past = compileProject("past",
					generate("past", policyText("2000-01-01 00:00 UTC", null, false, HIDDEN, PUBLIC)));
			Path futureSources = generate("future", policyText("2200-01-01 00:00 UTC", null, false, HIDDEN, PUBLIC));
			future = compileProject("future", futureSources);
			active = compileProject("active", generate("active",
					policyText("2200-01-01 00:00 UTC", "2199-01-01 00:00 UTC", false, HIDDEN, PUBLIC)));
			Path visibilitySources = generate("visibility",
					policyText("2200-01-01 00:00 UTC", null, true, VISIBILITY_HIDDEN, VISIBILITY_PUBLIC));
			visibility = compileProject("visibility", visibilitySources);
			visibilityPast = compileProject("visibility-past", generate("visibility-past",
					policyText("2000-01-01 00:00 UTC", null, true, VISIBILITY_HIDDEN, VISIBILITY_PUBLIC)));
			compileArchitectureStandIns(visibility);
			Path crossingSources = generate("crossing",
					policyText("2200-01-01 00:00 UTC", "2199-01-01 00:00 UTC", false, HIDDEN, PUBLIC));
			Path crossingSettings = crossingSources
					.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java");
			Files.writeString(crossingSettings, Files.readString(crossingSettings).replaceAll(
					"final(\\s+long\\s+\\w*THE_HIDDEN_TESTS_ARE_RUN_BEFORE\\s*=\\s*)-?\\d+L;", "$1Long.MAX_VALUE;"));
			crossing = compileProject("crossing", crossingSources);
			resources = futureSources.resolveSibling("resources");
			include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
					.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
					.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
			shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
			Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
					.resolve("GeneratedTestBehaviorSettings.java");
			String settings = Files.readString(
					futureSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"));
			Files.writeString(shadowSource,
					settings.replaceAll("THE_DEADLINE = -?\\d+L;", "THE_DEADLINE = 0L;").replaceAll(
							"THE_FOLLOWING_TESTS_ARE_HIDDEN = \"[^\"]*\";", "THE_FOLLOWING_TESTS_ARE_HIDDEN = \"\";"));
			compile(shadowSettings, List.of(), shadowSource);
			List<String> longHidden = new ArrayList<>(HIDDEN);
			while (String.join("\n", longHidden).length() <= 45_000) {
				longHidden.add(HIDDEN.get(0));
			}
			Path longSources = generate("long-future",
					policyText("2200-01-01 00:00 UTC", null, false, longHidden, PUBLIC));
			longFuture = compileProject("long-future", longSources);
			longShadowSettings = Files.createDirectories(tempDir.resolve("long-shadow"));
			Path longShadowSource = Files
					.createDirectories(tempDir.resolve("long-shadow-source/de/tum/cit/ase/ares/generated"))
					.resolve("GeneratedTestBehaviorSettings.java");
			String longSettings = Files.readString(
					longSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"));
			assertThat(Files.readString(longSources.resolve("de/tum/cit/ase/ares/generated/GeneratedHiddenTests.java")))
					.as("the long list must be joined from several literals")
					.contains("HIDDEN = List.of((String.join(");
			Files.writeString(longShadowSource,
					longSettings.replaceAll("THE_DEADLINE = -?\\d+L;", "THE_DEADLINE = 0L;").replaceAll(
							"THE_FOLLOWING_TESTS_ARE_HIDDEN(_PART_\\d+)? = (\"[^\"]*\"|String\\.join\\([^;]*\\));",
							"THE_FOLLOWING_TESTS_ARE_HIDDEN$1 = \"\";"));
			compile(longShadowSettings, List.of(), longShadowSource);
			shadowVisibility = Files.createDirectories(tempDir.resolve("shadow-visibility"));
			Path shadowVisibilitySource = Files
					.createDirectories(tempDir.resolve("shadow-visibility-source/de/tum/cit/ase/ares/generated"))
					.resolve("GeneratedTestBehaviorSettings.java");
			String visibilitySettings = Files.readString(
					visibilitySources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"));
			Files.writeString(shadowVisibilitySource, visibilitySettings
					.replace("TESTS_NOT_LISTED_HERE_ARE_HIDDEN = true;", "TESTS_NOT_LISTED_HERE_ARE_HIDDEN = false;"));
			compile(shadowVisibility, List.of(), shadowVisibilitySource);
		}

		/**
		 * Before the deadline, a listed test method fails with the localised message
		 * and does not run.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aListedTestIsHeldBackBeforeTheDeadline() throws Exception {
			assertThat(failureMessages(run(future, "MethodFixture", Locale.ENGLISH))).hasSize(3)
					.allMatch(beforeDeadline(Locale.ENGLISH)::equals);
		}

		/**
		 * The before-deadline message is localised.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theBeforeDeadlineMessageIsLocalised() throws Exception {
			assertThat(failureMessages(run(future, "MethodFixture", Locale.GERMAN))).hasSize(3)
					.allMatch(beforeDeadline(Locale.GERMAN)::equals);
		}

		/**
		 * A listed test template is skipped as a whole before the deadline, with the
		 * localised message.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aListedTemplateIsHeldBack() throws Exception {
			EngineExecutionResults results = run(future, "TemplateFixture", Locale.ENGLISH);
			assertThat(failureMessages(results)).isEmpty();
			assertThat(skipReasons(results)).containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * Before the deadline, a listed parameterised test's argument source never
		 * runs, so neither its output nor its failure reaches the report.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aHiddenTemplatesArgumentSourceDoesNotRunBeforeTheDeadline() throws Exception {
			Captured captured = captured(() -> run(future, "SourcedFixture", Locale.ENGLISH));
			assertThat(failureMessages(captured.results())).isEmpty();
			assertThat(skipReasons(captured.results())).containsExactly(beforeDeadline(Locale.ENGLISH));
			assertThat(captured.output()).doesNotContain("SECRET_GENERATED_SOURCE");
		}

		/**
		 * After the deadline a listed parameterised test runs, its argument source
		 * included. The source runs before the extension can watch it, so its output
		 * and failure text reach the report, as the manual states.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aHiddenTemplatesArgumentSourceRunsAfterTheDeadline() throws Exception {
			Captured captured = captured(() -> run(past, "SourcedFixture", Locale.ENGLISH));
			assertThat(skipReasons(captured.results())).isEmpty();
			assertThat(captured.results().containerEvents().failed().count()).isEqualTo(1);
			assertThat(failureMessages(captured.results())).containsExactly("SECRET_GENERATED_SOURCE");
			assertThat(captured.output()).contains("SECRET_GENERATED_SOURCE");
		}

		/**
		 * A listed test factory is held back before it produces any dynamic test.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aListedFactoryIsHeldBack() throws Exception {
			assertThat(failureMessages(run(future, "FactoryFixture", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * A factory that started before the early run ended still has its stream closed
		 * once the early run is over, while its dynamic test is held back. The factory
		 * itself ends the early run, so the order does not depend on timing.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aFactoryStreamIsClosedAfterTheEarlyRunEnds() throws Exception {
			System.clearProperty("ares.test.crossing.closed");
			try {
				EngineExecutionResults results = run(crossing, "CrossingFactoryFixture", Locale.ENGLISH);
				assertThat(System.getProperty("ares.test.crossing.closed")).isEqualTo("true");
				assertThat(failureMessages(results)).containsExactly(beforeDeadline(Locale.ENGLISH));
			} finally {
				System.clearProperty("ares.test.crossing.closed");
			}
		}

		/**
		 * A class entry covers the listed class's nested classes, and a canonical
		 * nested entry covers that nested class only.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void nestedClassesFollowTheirEntries() throws Exception {
			assertThat(failureMessages(run(future, "Outer", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
			EngineExecutionResults nesting = run(future, "Nesting", Locale.ENGLISH);
			assertThat(failureMessages(nesting)).containsExactly(beforeDeadline(Locale.ENGLISH));
			assertThat(nesting.testEvents().succeeded().count()).isEqualTo(1);
		}

		/**
		 * Only the listed method of a class is held back.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void onlyTheListedMethodIsHeldBack() throws Exception {
			assertThat(failureMessages(run(future, "Methods", Locale.ENGLISH)))
					.containsExactlyInAnyOrder(beforeDeadline(Locale.ENGLISH), "visible");
		}

		/**
		 * After the deadline ordinary hidden failures stay failed without details,
		 * while a hidden abort keeps its aborted status.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void afterTheDeadlineFailuresAreHidden() throws Exception {
			EngineExecutionResults results = run(past, "MethodFixture", Locale.ENGLISH);

			assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
			assertThat(results.testEvents().aborted().count()).isEqualTo(1);
			assertThat(failureMessages(results)).containsExactly("");
		}

		/**
		 * After the deadline, template invocations and dynamic tests hide their
		 * failures too.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void templatesAndDynamicTestsHideTheirFailures() throws Exception {
			assertThat(failureMessages(run(past, "TemplateFixture", Locale.ENGLISH))).hasSize(2)
					.allMatch(hiddenFailure()::equals);
			assertThat(failureMessages(run(past, "FactoryFixture", Locale.ENGLISH))).containsExactly(hiddenFailure());
		}

		/**
		 * A student error that imitates an Ares message is hidden all the same.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aForgedAresMessageIsHidden() throws Exception {
			assertThat(failureMessages(run(past, "ForgedFixture", Locale.ENGLISH))).containsExactly(hiddenFailure());
		}

		/**
		 * Before the always-run-before date a listed test runs despite a future
		 * deadline.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theAlwaysRunBeforeDateReleasesListedTests() throws Exception {
			assertThat(run(active, "MethodFixture", Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);
		}

		/**
		 * An unlisted test is neither held back nor hidden.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void anUnlistedTestIsLeftAlone() throws Exception {
			assertThat(failureMessages(run(future, "UnlistedFixture", Locale.ENGLISH))).containsExactly("visible");
		}

		/**
		 * Constructor and lifecycle failures of a hidden class expose no message.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void hiddenLifecycleAndConstructorsAreOpaque() throws Exception {
			assertThat(failureMessages(run(future, "ConstructorFixture", Locale.ENGLISH))).containsExactly("");
			assertThat(failureMessages(run(past, "LifecycleFixture", Locale.ENGLISH))).containsExactly("");
			assertThat(failureMessages(run(past, "ConstructorFixture", Locale.ENGLISH))).containsExactly("");
		}

		/**
		 * Before the deadline, a hidden test's own setup does not run either: the test
		 * reports only that it is held back, not the setup's failure.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aHiddenTestsSetupIsHeldBackBeforeTheDeadline() throws Exception {
			assertThat(failureMessages(run(future, "LifecycleFixture", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * Hidden test, constructor, and lifecycle output is absent from both streams.
		 */
		@Test
		void hiddenStreamsAreAbsentAcrossPaths() throws Exception {
			PrintStream previousOut = System.out;
			PrintStream previousErr = System.err;
			ByteArrayOutputStream captured = new ByteArrayOutputStream();
			PrintStream sink = new PrintStream(captured, true, StandardCharsets.UTF_8);
			try {
				System.setOut(sink);
				System.setErr(sink);
				run(past, "MethodFixture", Locale.ENGLISH);
				run(past, "LifecycleFixture", Locale.ENGLISH);
				run(past, "ConstructorFixture", Locale.ENGLISH);
				run(past, "MixedFixture", Locale.ENGLISH);
			} finally {
				System.setOut(previousOut);
				System.setErr(previousErr);
				sink.close();
			}
			assertThat(captured.toString(StandardCharsets.UTF_8)).contains("VISIBLE_GENERATED_MIXED")
					.doesNotContain("SECRET_GENERATED_");
		}

		/**
		 * The sentinel passes when the extension intercepted it.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theSentinelPassesWithTheExtension() throws Exception {
			assertThat(run(future, SENTINEL, Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);
		}

		/**
		 * The sentinel fails when JUnit does not load the extension.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theSentinelFailsWithoutTheExtension() throws Exception {
			EngineExecutionResults results = run(List.of(future), SENTINEL, Locale.ENGLISH,
					Map.of("junit.jupiter.extensions.autodetection.enabled", "false"));

			assertThat(failureMessages(results)).singleElement().asString().contains("not active");
		}

		/**
		 * A sentinel that passed once does not pass again in the same class loader once
		 * the extension is gone, since it needs evidence from its own run.
		 *
		 * @throws Exception if the runs cannot be set up
		 */
		@Test
		void theSentinelNeedsEvidenceFromItsOwnRun() throws Exception {
			ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
			try (URLClassLoader loader = new URLClassLoader(classPath(List.of(future)), originalLoader)) {
				assertThat(runIn(loader, SENTINEL, Locale.ENGLISH, Map.of()).testEvents().succeeded().count())
						.isEqualTo(1);

				EngineExecutionResults withoutExtension = runIn(loader, SENTINEL, Locale.ENGLISH,
						Map.of("junit.jupiter.extensions.autodetection.enabled", "false"));

				assertThat(failureMessages(withoutExtension)).singleElement().asString().contains("not active");
			}
		}

		/** A parallel configuration fails before any hidden test body can run. */
		@Test
		void parallelExecutionIsRejectedBeforeHiddenTestsRun() throws Exception {
			EngineExecutionResults results = run(List.of(past), "com.example.fixtures.MethodFixture", Locale.ENGLISH,
					Map.of("junit.jupiter.execution.parallel.enabled", "true"));
			assertThat(results.testEvents().started().count()).isZero();
			assertThat(results.containerEvents().failed().count()).isPositive();
		}

		/**
		 * Under a policy that hides unlisted tests, an unlisted test is held back
		 * before the deadline on the method, template and factory paths.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void underUnlistedHiddenAnUnlistedTestIsHeldBack() throws Exception {
			assertThat(failureMessages(run(visibility, "UnlistedFixture", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
			assertThat(failureMessages(run(visibility, "MethodFixture", Locale.ENGLISH))).hasSize(3)
					.allMatch(beforeDeadline(Locale.ENGLISH)::equals);
			assertThat(skipReasons(run(visibility, "TemplateFixture", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
			assertThat(failureMessages(run(visibility, "FactoryFixture", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * Under a policy that hides unlisted tests, after the deadline an unlisted
		 * test's failures are hidden, dynamic tests and aborted tests included.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void underUnlistedHiddenAnUnlistedTestsFailuresAreHidden() throws Exception {
			assertThat(failureMessages(run(visibilityPast, "MethodFixture", Locale.ENGLISH))).containsExactly("");
			assertThat(failureMessages(run(visibilityPast, "FactoryFixture", Locale.ENGLISH)))
					.containsExactly(hiddenFailure());
		}

		/**
		 * A test on the public list runs and shows its real failure, while a method
		 * entry on the hidden list wins over its class's public entry.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aPublicTestRunsAndShowsItsFailure() throws Exception {
			assertThat(failureMessages(run(visibility, "Shown", Locale.ENGLISH))).containsExactlyInAnyOrder("visible",
					beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * A public nested class wins over its enclosing class, which unlisted is
		 * hidden.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aPublicNestedClassWinsOverItsHiddenEnclosingClass() throws Exception {
			assertThat(failureMessages(run(visibility, "Layered", Locale.ENGLISH))).containsExactlyInAnyOrder("visible",
					beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * Under a policy that does not hide unlisted tests, a public method entry wins
		 * over its class's hidden entry.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aPublicMethodWinsOverItsHiddenClass() throws Exception {
			assertThat(failureMessages(run(future, "Shown", Locale.ENGLISH))).containsExactlyInAnyOrder("visible",
					beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * An interface default test method listed through the class inheriting it is
		 * held back.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void anInheritedInterfaceDefaultMethodIsHeldBack() throws Exception {
			assertThat(failureMessages(run(future, "Inherits", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * A test inherited from a superclass and listed through the class that declares
		 * it is held back before the deadline and reports nothing after it, its setup
		 * output included.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void anInheritedTestListedThroughItsDeclaringClassIsHidden() throws Exception {
			PrintStream previousOut = System.out;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			List<String> before;
			List<String> after;
			try (PrintStream sink = new PrintStream(output, true, StandardCharsets.UTF_8)) {
				System.setOut(sink);
				before = failureMessages(run(future, "Dives", Locale.ENGLISH));
				after = failureMessages(run(past, "Dives", Locale.ENGLISH));
			} finally {
				System.setOut(previousOut);
			}
			assertThat(before).containsExactly(beforeDeadline(Locale.ENGLISH));
			assertThat(after).containsExactly(hiddenFailure());
			assertThat(output.toString(StandardCharsets.UTF_8)).doesNotContain("SECRET_DIVING_SETUP");
		}

		/**
		 * The sentinel passes under a policy that hides unlisted tests, since a
		 * generated class is never hidden.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theSentinelPassesWhenUnlistedTestsAreHidden() throws Exception {
			assertThat(run(visibility, SENTINEL, Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);
		}

		/**
		 * A class outside the reserved package named like the sentinel gets no
		 * exemption: under a policy that hides unlisted tests it is held back.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aSentinelLookalikeOutsideTheReservedPackageIsHeldBack() throws Exception {
			assertThat(failureMessages(run(visibility, "GeneratedHiddenTestsSentinelTest", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * A class in the generated package, beside the hook, that is not one of the
		 * tests Ares generates gets no exemption: under a policy that hides unlisted
		 * tests it is held back like any other.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aStudentClassInTheGeneratedPackageIsHeldBack() throws Exception {
			assertThat(failureMessages(run(visibility, "de.tum.cit.ase.ares.generated.StudentTest", Locale.ENGLISH)))
					.containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * The exercise's generated ArchUnit and WALA security tests run and report
		 * their real failure under a policy that hides unlisted tests.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void theGeneratedArchitectureTestsAreNeverHidden() throws Exception {
			assertThat(failureMessages(run(visibility, ARCHUNIT_TEST, Locale.ENGLISH)))
					.containsExactly("architecture violation");
			assertThat(failureMessages(run(visibility, WALA_TEST, Locale.ENGLISH)))
					.containsExactly("architecture violation");
		}

		/**
		 * Replacing the generated settings class so unlisted tests are not hidden
		 * changes nothing: the extension carries the switch as a constant.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aReplacedSettingsClassCannotRevealUnlistedTests() throws Exception {
			EngineExecutionResults results = run(List.of(shadowVisibility, visibility),
					"com.example.fixtures.UnlistedFixture", Locale.ENGLISH, Map.of());

			assertThat(failureMessages(results)).containsExactly(beforeDeadline(Locale.ENGLISH));
		}

		/**
		 * Replacing the generated settings class with a passed deadline and an empty
		 * list changes nothing: the extension carries the settings as constants.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aReplacedSettingsClassChangesNothing() throws Exception {
			EngineExecutionResults results = run(List.of(shadowSettings, future), "com.example.fixtures.MethodFixture",
					Locale.ENGLISH, Map.of());

			assertThat(failureMessages(results)).hasSize(3).allMatch(beforeDeadline(Locale.ENGLISH)::equals);
		}

		/**
		 * A hidden list too long for one literal is joined inside the extension, so
		 * replacing the settings class with a passed deadline changes nothing either.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aReplacedSettingsClassChangesNothingForALongList() throws Exception {
			EngineExecutionResults results = run(List.of(longShadowSettings, longFuture),
					"com.example.fixtures.MethodFixture", Locale.ENGLISH, Map.of());

			assertThat(failureMessages(results)).hasSize(3).allMatch(beforeDeadline(Locale.ENGLISH)::equals);
		}

		/**
		 * Runs one fixture through a nested JUnit session.
		 *
		 * @param project the compiled project.
		 * @param fixture the fixture's simple name, or a fully qualified class.
		 * @param locale  the locale messages are shown in.
		 * @return the session's results
		 * @throws Exception if the run cannot be set up
		 */
		private static EngineExecutionResults run(Path project, String fixture, Locale locale) throws Exception {
			String className = fixture.contains(".") ? fixture : "com.example.fixtures." + fixture;
			return run(List.of(project), className, locale, Map.of());
		}

		/**
		 * Runs a class through a nested JUnit session with a fresh class loader.
		 *
		 * @param roots      the compiled class roots, first wins.
		 * @param className  the class to run.
		 * @param locale     the locale messages are shown in.
		 * @param parameters further JUnit configuration parameters.
		 * @return the session's results
		 * @throws Exception if the run cannot be set up
		 */
		private static EngineExecutionResults run(List<Path> roots, String className, Locale locale,
				Map<String, String> parameters) throws Exception {
			try (URLClassLoader loader = new URLClassLoader(classPath(roots),
					Thread.currentThread().getContextClassLoader())) {
				return runIn(loader, className, locale, parameters);
			}
		}

		/**
		 * Runs a class through a nested JUnit session in a given class loader.
		 *
		 * @param loader     the class loader to load the class from.
		 * @param className  the class to run.
		 * @param locale     the locale messages are shown in.
		 * @param parameters further JUnit configuration parameters.
		 * @return the session's results
		 * @throws Exception if the run cannot be set up
		 */
		private static EngineExecutionResults runIn(URLClassLoader loader, String className, Locale locale,
				Map<String, String> parameters) throws Exception {
			Map<String, String> all = new java.util.HashMap<>(parameters);
			all.putIfAbsent(GeneratedCodeRuns.AUTODETECTION, "true");
			all.putIfAbsent(GeneratedCodeRuns.AUTODETECTION_INCLUDE, include);
			return GeneratedCodeRuns.runIn(loader, className, locale, all);
		}

		/**
		 * The class path of a nested run: the compiled roots and the resources.
		 *
		 * @param roots the compiled class roots, first wins.
		 * @return the class path entries
		 * @throws IOException if a path cannot be turned into a URL
		 */
		private static URL[] classPath(List<Path> roots) throws IOException {
			List<Path> entries = new ArrayList<>(roots);
			entries.add(resources);
			return GeneratedCodeRuns.classPath(entries);
		}

		/**
		 * The before-deadline message for a locale.
		 *
		 * @param locale the locale.
		 * @return the message
		 */
		private static String beforeDeadline(Locale locale) {
			return bundle(locale).getString("test_guard.hidden_test_before_deadline_message");
		}

		/** The absence of a hidden-failure message in a generated result. */
		private static String hiddenFailure() {
			return "";
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
		 * The failure messages of every failed test and container of a run.
		 *
		 * @param results the run's results.
		 * @return the messages
		 */
		private static List<String> failureMessages(EngineExecutionResults results) {
			return GeneratedCodeRuns.failureMessages(results);
		}

		/**
		 * The reasons of every skipped test and container of a run.
		 *
		 * @param results the run's results.
		 * @return the reasons
		 */
		private static List<String> skipReasons(EngineExecutionResults results) {
			return results.allEvents().skipped().stream().map(event -> event.getPayload(String.class).orElse(""))
					.toList();
		}

		/**
		 * Runs an action while both standard streams are captured.
		 *
		 * @param action the run.
		 * @return the run's results and what it printed
		 * @throws Exception if the run cannot be set up
		 */
		private static Captured captured(Callable<EngineExecutionResults> action) throws Exception {
			PrintStream previousOut = System.out;
			PrintStream previousErr = System.err;
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			PrintStream sink = new PrintStream(bytes, true, StandardCharsets.UTF_8);
			try {
				System.setOut(sink);
				System.setErr(sink);
				EngineExecutionResults results = action.call();
				return new Captured(results, bytes.toString(StandardCharsets.UTF_8));
			} finally {
				System.setOut(previousOut);
				System.setErr(previousErr);
				sink.close();
			}
		}

		/**
		 * A run's results and what it printed.
		 *
		 * @param results the run's results.
		 * @param output  what it printed on both standard streams.
		 */
		private record Captured(EngineExecutionResults results, String output) {
		}

		/**
		 * Writes the fixtures into a fresh project, then runs the real generator on it.
		 *
		 * @param folder     the project's folder name.
		 * @param policyText the project's policy.
		 * @return the project's test source root
		 * @throws IOException if the project cannot be created
		 */
		private static Path generate(String folder, String policyText) throws IOException {
			Path project = Files.createDirectory(tempDir.resolve(folder));
			Path policy = project.resolve("SecurityPolicy.yaml");
			Files.writeString(policy, policyText);
			Files.writeString(project.resolve("pom.xml"), "<project/>");
			Files.createDirectories(project.resolve("src/main/java"));
			Path testSources = Files.createDirectories(project.resolve("src/test/java"));
			Files.createDirectories(project.resolve("target/classes"));
			writeFixtures(testSources);
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
					.createTestCases().writeTestCases(testSources);
			return testSources;
		}

		/**
		 * A policy for the {@code com.example} exercise listing this class's fixtures.
		 *
		 * @param deadline                    the deadline.
		 * @param alwaysRunBefore             the always-run-before date, or null.
		 * @param testsNotListedHereAreHidden whether unlisted tests are hidden.
		 * @param hidden                      the hidden entries.
		 * @param shown                       the public entries.
		 * @return the policy text
		 */
		private static String policyText(String deadline, String alwaysRunBefore, boolean testsNotListedHereAreHidden,
				List<String> hidden, List<String> shown) {
			StringBuilder policy = new StringBuilder("""
					thisPolicyFileCompliesToThePolicyVersion: 2
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
					    regardingTheVisibilityAndScheduleOfTheTestCases:
					""");
			policy.append("      theDeadlineIs: \"").append(deadline).append("\"\n");
			if (alwaysRunBefore != null) {
				policy.append("      theHiddenTestsAreRunBefore: \"").append(alwaysRunBefore).append("\"\n");
			}
			policy.append("      testsNotListedHereAreHidden: ").append(testsNotListedHereAreHidden).append('\n');
			policy.append("      theFollowingTestsAreHidden:\n");
			hidden.forEach(entry -> policy.append("        - \"").append(entry).append("\"\n"));
			policy.append("      theFollowingTestsArePublic:\n");
			shown.forEach(entry -> policy.append("        - \"").append(entry).append("\"\n"));
			return policy.toString();
		}

		/**
		 * Compiles one generated project, fixtures included.
		 *
		 * @param folder      the output folder's name.
		 * @param testSources the project's test source root.
		 * @return the output folder
		 * @throws IOException if compiling fails
		 */
		private static Path compileProject(String folder, Path testSources) throws IOException {
			Path output = Files.createDirectories(tempDir.resolve("compiled-" + folder));
			compile(output,
					List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
							testSources.resolve("com/example/ares/api/localization"),
							testSources.resolve("com/example/fixtures")),
					testSources.resolve("com/example/ares/api/util/LruCache.java"));
			return output;
		}

		/**
		 * Writes the fixture tests, one per path and behaviour the extension must show.
		 *
		 * @param testSources the test source root to write them into.
		 * @throws IOException if a file cannot be written
		 */
		private static void writeFixtures(Path testSources) throws IOException {
			Path folder = Files.createDirectories(testSources.resolve("com/example/fixtures"));
			String secret = "System.out.print(\"SECRET_GENERATED_BODY\"); throw new IllegalStateException(\"secret\");";
			fixture(folder, "MethodFixture", "", "@Test void passes() {} @Test void fails() { " + secret
					+ " } @Test void aborts() { Assumptions.abort(\"secret\"); }");
			fixture(folder, "TemplateFixture", "",
					"@org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(ints = { 1, 2 }) void each(int value) { "
							+ secret + " }");
			fixture(folder, "SourcedFixture",
					"static java.util.stream.Stream<Integer> values() { System.out.print(\"SECRET_GENERATED_SOURCE\");"
							+ " throw new IllegalStateException(\"SECRET_GENERATED_SOURCE\"); }",
					"@org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.MethodSource(\"values\") void each(int value) { }");
			fixture(folder, "FactoryFixture", "",
					"@TestFactory java.util.List<DynamicTest> cases() { return java.util.List.of(DynamicTest.dynamicTest(\"case\", () -> { "
							+ secret + " })); }");
			fixture(folder, "CrossingFactoryFixture", "",
					"@TestFactory java.util.stream.Stream<DynamicTest> cases() throws ReflectiveOperationException { "
							+ "java.lang.reflect.Field cutOff = Class.forName(\""
							+ TestBehaviorConfiguration.GENERATED_CLASS_NAME + "\").getDeclaredField(\""
							+ TestBehaviorConfiguration.VISIBILITY_RUN_BEFORE_FIELD_NAME + "\"); "
							+ "cutOff.setAccessible(true); cutOff.setLong(null, 0L); "
							+ "return java.util.stream.Stream.of(DynamicTest.dynamicTest(\"case\", () -> { " + secret
							+ " })).onClose(() -> System.setProperty(\"ares.test.crossing.closed\", \"true\")); }");
			fixture(folder, "LifecycleFixture",
					"@BeforeEach void setUp() { System.err.print(\"SECRET_GENERATED_SETUP\"); throw new IllegalStateException(\"secret setup\"); }",
					"@Test void test() {}");
			fixture(folder, "ConstructorFixture",
					"ConstructorFixture() { System.out.print(\"SECRET_GENERATED_CONSTRUCTOR\"); throw new IllegalStateException(\"secret constructor\"); }",
					"@Test void test() {}");
			fixture(folder, "ForgedFixture", "",
					"@Test void forges() { throw new SecurityException(\"Ares Security Error (Reason: Student-Code; Stage: Execution): secret\"); }");
			fixture(folder, "UnlistedFixture", "",
					"@Test void fails() { throw new IllegalStateException(\"visible\"); }");
			fixture(folder, "Outer", "", "@Nested class Inner { @Test void deep() { " + secret + " } }");
			fixture(folder, "Nesting", "@Test void top() {}",
					"@Nested class Inner { @Test void deep() { " + secret + " } }");
			fixture(folder, "Methods", "", "@Test void listed() { " + secret
					+ " } @Test void unlisted() { throw new IllegalStateException(\"visible\"); }");
			fixture(folder, "MixedFixture",
					"MixedFixture() { System.out.print(\"SECRET_GENERATED_MIXED_CONSTRUCTOR\"); } @BeforeAll static void setup() { System.err.print(\"SECRET_GENERATED_MIXED_SETUP\"); } @AfterAll static void teardown() { System.err.print(\"SECRET_GENERATED_MIXED_TEARDOWN\"); }",
					"@Test void publicTest() { System.out.print(\"VISIBLE_GENERATED_MIXED\"); } @Test void secret() { "
							+ secret + " }");
			fixture(folder, "Shown", "", "@Test void shown() { throw new IllegalStateException(\"visible\"); }"
					+ " @Test void secret() { " + secret + " }");
			fixture(folder, "Layered", "@Test void top() { " + secret + " }",
					"@Nested class Inner { @Test void deep() { throw new IllegalStateException(\"visible\"); } }");
			fixture(folder, "GeneratedHiddenTestsSentinelTest", "",
					"@Test void hiddenTestsAreActive() { throw new IllegalStateException(\"visible\"); }");
			Files.writeString(Files.createDirectories(testSources.resolve("de/tum/cit/ase/ares/generated"))
					.resolve("StudentTest.java"), """
							package de.tum.cit.ase.ares.generated;

							import org.junit.jupiter.api.Test;

							class StudentTest {
								@Test
								void fails() {
									throw new IllegalStateException("visible");
								}
							}
							""");
			Files.writeString(folder.resolve("GlidingTests.java"), """
					package com.example.fixtures;

					import org.junit.jupiter.api.Test;

					interface GlidingTests {
						@Test
						default void glides() {
							throw new IllegalStateException("secret");
						}
					}
					""");
			Files.writeString(folder.resolve("Inherits.java"), """
					package com.example.fixtures;

					class Inherits implements GlidingTests {
					}
					""");
			Files.writeString(folder.resolve("DivingBase.java"), """
					package com.example.fixtures;

					import org.junit.jupiter.api.BeforeEach;
					import org.junit.jupiter.api.Test;

					abstract class DivingBase {
						@BeforeEach
						void prepare() {
							System.out.print("SECRET_DIVING_SETUP");
						}

						@Test
						void dives() {
							throw new IllegalStateException("secret");
						}
					}
					""");
			Files.writeString(folder.resolve("Dives.java"), """
					package com.example.fixtures;

					class Dives extends DivingBase {
					}
					""");
		}

		/**
		 * Compiles stand-ins for the security tests Ares generates into the exercise,
		 * under their exact names, into a compiled project. Each fails with a visible
		 * text, so a run shows whether the hook left it alone.
		 *
		 * @param output the compiled project.
		 * @throws IOException if writing or compiling fails
		 */
		private static void compileArchitectureStandIns(Path output) throws IOException {
			Path sources = Files.createDirectories(tempDir.resolve("architecture-stand-ins"));
			List<Path> files = new ArrayList<>();
			for (String className : List.of(ARCHUNIT_TEST, WALA_TEST)) {
				int lastDot = className.lastIndexOf('.');
				Path file = Files.createDirectories(sources.resolve(className.substring(0, lastDot).replace('.', '/')))
						.resolve(className.substring(lastDot + 1) + ".java");
				Files.writeString(file, """
						package %s;

						import org.junit.jupiter.api.Test;

						public class %s {
							@Test
							public void reflectionShouldNotBeAccessed() {
								throw new IllegalStateException("architecture violation");
							}
						}
						""".formatted(className.substring(0, lastDot), className.substring(lastDot + 1)));
				files.add(file);
			}
			compile(output, List.of(), files.toArray(Path[]::new));
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
			GeneratedCodeRuns.compile(output, folders, singleFiles);
		}
	}
}
