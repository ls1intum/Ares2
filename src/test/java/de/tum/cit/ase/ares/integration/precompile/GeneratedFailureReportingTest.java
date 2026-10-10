package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Runs the failure reporting a precompile run generates, end to end: the real
 * generator writes it into a project, the generated sources are compiled with
 * fixture tests, and nested JUnit runs show what a student would see.
 */
class GeneratedFailureReportingTest {

	/** The message the policy sets for a failing test. */
	private static final String POLICY_MESSAGE = "Ask your tutor.";

	/** Test data a fixture tries to leak through its failure message. */
	private static final String LEAKED = "expected=42";

	/** Set by the instructor's own extension when it runs. */
	private static final String INSTRUCTOR_RAN = "ares.test.instructorExtensionRan";

	/** Set by a student-registered extension if it ever runs. */
	private static final String SPY_RAN = "ares.test.studentExtensionRan";

	/** The generated project, shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** The compiled generated code and fixtures. */
	private static Path compiled;

	/** The project's test resources, holding the copied message bundles. */
	private static Path resources;

	/** A folder standing in for student output, with a service file of its own. */
	private static Path studentOutput;

	/**
	 * Generated code nested runs load ahead of {@link #compiled}, which also holds
	 * the fixtures; the same folder unless a test swaps it.
	 */
	private static Path classes;

	/**
	 * A settings class with the setting off, standing in for one a student
	 * replaced.
	 */
	private static Path shadowSettings;

	/**
	 * Whether nested runs put {@link #shadowSettings} first on their class path.
	 */
	private static boolean shadowed;

	/**
	 * The include value the generator wrote into {@code junit-platform.properties}.
	 */
	private static String include;

	/**
	 * Generates the precompile output for a policy with the setting on, then
	 * compiles the generated code with the fixtures.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		Path project = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("target/classes"));
		resources = Files.createDirectories(project.resolve("src/test/resources"));
		Path instructorServices = resources.resolve("META-INF/services/org.junit.jupiter.api.extension.Extension");
		Files.createDirectories(instructorServices.getParent());
		Files.writeString(instructorServices,
				"com.example.fixtures.InstructorExtension # setup" + System.lineSeparator());
		Path policy = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policy, policyText());
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		Path fixtures = writeFixtures(tempDir.resolve("fixtures"));
		writeHiddenResultFixture(testSources);
		compiled = Files.createDirectories(tempDir.resolve("compiled"));
		studentOutput = Files.createDirectories(tempDir.resolve("student"));
		Path studentServices = studentOutput.resolve("META-INF/services/org.junit.jupiter.api.extension.Extension");
		Files.createDirectories(studentServices.getParent());
		Files.writeString(studentServices, "com.example.fixtures.StudentExtension" + System.lineSeparator());
		compile(compiled,
				Stream.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
						testSources.resolve("com/example/ares/api/localization"), fixtures).toList(),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
		Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
				.resolve("GeneratedTestBehaviorSettings.java");
		Files.writeString(shadowSource,
				Files.readString(
						testSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"))
						.replace("= true;", "= false;"));
		compile(shadowSettings, List.of(), shadowSource);
		classes = compiled;
	}

	/** Writes the generated hidden-result types used by the combined hook. */
	private static void writeHiddenResultFixture(Path testSources) throws IOException {
		Path folder = Files.createDirectories(testSources.resolve("de/tum/cit/ase/ares/generated"));
		Files.writeString(folder.resolve("GeneratedHiddenTests.java"),
				"""
						package de.tum.cit.ase.ares.generated;

						public final class GeneratedHiddenTests implements org.junit.jupiter.api.extension.InvocationInterceptor {
							/** Hides one failing test invocation. */
							@Override public void interceptTestMethod(Invocation<Void> invocation,
									org.junit.jupiter.api.extension.ReflectiveInvocationContext<java.lang.reflect.Method> method,
									org.junit.jupiter.api.extension.ExtensionContext context) throws Throwable {
								try { invocation.proceed(); } catch (Throwable failure) { throw new HiddenTestFailure(); }
							}
							public static final class HiddenTestFailure extends AssertionError {
								public HiddenTestFailure() { setStackTrace(new StackTraceElement[0]); }
								@Override public String toString() { return ""; }
							}
							public static final class HiddenTestAbort extends org.opentest4j.TestAbortedException {
								public HiddenTestAbort() { setStackTrace(new StackTraceElement[0]); }
								@Override public String toString() { return ""; }
							}
							public static final class HiddenTestScheduled extends org.opentest4j.AssertionFailedError {
								public HiddenTestScheduled() { super("hidden tests will be executed after the deadline."); }
							}
						}
						""");
	}

	/**
	 * A policy for the {@code com.example} exercise with the setting on.
	 *
	 * @return the policy text
	 */
	private static String policyText() {
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
				    regardingPrivilegedExceptions:
				      onlyPrivilegedExceptionsAreReported: true
				      theFailureMessageIs: "Ask your tutor."
				""";
	}

	/** An ordinary failure shows the policy's message instead of its own. */
	@Test
	void anOrdinaryFailureShowsThePolicyMessage() throws Exception {
		assertThat(failureOf(runJupiter("FailingFixture", true, Locale.ENGLISH), "ordinaryFailure"))
				.isEqualTo(POLICY_MESSAGE);
	}

	/**
	 * Replacing the generated settings class, as student code with write access to
	 * the test output could, changes nothing: the hook carries the settings as
	 * compile-time constants and never reads that class while reporting.
	 */
	@Test
	void aReplacedSettingsClassChangesNothing() throws Exception {
		shadowed = true;
		try {
			assertThat(failureOf(runJupiter("FailingFixture", true, Locale.ENGLISH), "ordinaryFailure"))
					.isEqualTo(POLICY_MESSAGE);
		} finally {
			shadowed = false;
		}
	}

	/** A forged Ares-looking security error gets no pass-through. */
	@Test
	void aForgedSecurityErrorShowsThePolicyMessage() throws Exception {
		assertThat(failureOf(runJupiter("FailingFixture", true, Locale.ENGLISH), "forgedSecurityError"))
				.isEqualTo(POLICY_MESSAGE);
	}

	/** A JUnit timeout shows the fixed timeout text, in English and in German. */
	@Test
	void aTimeoutShowsTheFixedTextInBothLanguages() throws Exception {
		assertThat(failureOf(runJupiter("FailingFixture", true, Locale.ENGLISH), "timesOut"))
				.isEqualTo("The test timed out.");
		assertThat(failureOf(runJupiter("FailingFixture", true, Locale.GERMAN), "timesOut"))
				.isEqualTo("Der Test hat das Zeitlimit u\u0308berschritten.");
	}

	/** A student-thrown timeout shows the fixed text, never its own message. */
	@Test
	void aForgedTimeoutShowsOnlyTheFixedText() throws Exception {
		assertThat(failureOf(runJupiter("FailingFixture", true, Locale.ENGLISH), "forgedTimeout"))
				.isEqualTo("The test timed out.");
	}

	/** The reporting hook preserves hidden results from either hook order. */
	@Test
	void generatedHiddenMarkersKeepTheirStatusAndMessage() throws Exception {
		EngineExecutionResults results = runJupiter("HiddenMarkerFixture", true, Locale.ENGLISH);
		assertThat(failureOf(results, "failed")).isEmpty();
		assertThat(failureOf(results, "scheduled")).isEqualTo("hidden tests will be executed after the deadline.");
		assertThat(results.testEvents().aborted().count()).isEqualTo(1);
		assertThat(results.testEvents().failed().count()).isEqualTo(2);
	}

	/**
	 * Hidden results stay opaque with either generated extension registration
	 * order.
	 */
	@Test
	void hiddenReportingHookOrdersKeepFailuresOpaque() throws Exception {
		assertThat(failureOf(runJupiter("HiddenFirstFixture", false, Locale.ENGLISH), "secret")).isEmpty();
		assertThat(failureOf(runJupiter("ReportingFirstFixture", false, Locale.ENGLISH), "secret")).isEmpty();
	}

	/**
	 * A failing dynamic test, which no exception handler sees, shows the policy's
	 * message.
	 */
	@Test
	void aFailingDynamicTestShowsThePolicyMessage() throws Exception {
		assertThat(failureOf(runJupiter("DynamicFixture", true, Locale.ENGLISH), "case")).isEqualTo(POLICY_MESSAGE);
	}

	/**
	 * A factory whose tests fail while JUnit walks them, directly or inside a
	 * container, shows the policy's message, and a passing factory still runs.
	 */
	@Test
	void aFactoryFailingWhileWalkedShowsThePolicyMessage() throws Exception {
		EngineExecutionResults results = runJupiter("LazyFactoryFixture", true, Locale.ENGLISH);

		assertThat(results.allEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf)).hasSize(2)
				.allMatch(POLICY_MESSAGE::equals);
		assertThat(results.testEvents().succeeded().stream().map(event -> event.getTestDescriptor().getDisplayName()))
				.containsExactly("ok");
	}

	/** A test class that fails while it is created shows the policy's message. */
	@Test
	void aFailingTestClassConstructorShowsThePolicyMessage() throws Exception {
		EngineExecutionResults results = runJupiter("ConstructorFixture", true, Locale.ENGLISH);

		assertThat(results.allEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf)).isNotEmpty()
				.allMatch(POLICY_MESSAGE::equals);
	}

	/** A failing {@code @BeforeEach} method shows the policy's message. */
	@Test
	void aFailingSetupMethodShowsThePolicyMessage() throws Exception {
		assertThat(failureOf(runJupiter("BeforeEachFixture", true, Locale.ENGLISH), "test")).isEqualTo(POLICY_MESSAGE);
	}

	/** A failing {@code @AfterAll} method shows the policy's message. */
	@Test
	void aFailingTeardownMethodShowsThePolicyMessage() throws Exception {
		EngineExecutionResults results = runJupiter("AfterAllFixture", true, Locale.ENGLISH);

		assertThat(results.containerEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf))
				.containsExactly(POLICY_MESSAGE);
	}

	/** The sentinel passes with the hook active, even when every test passes. */
	@Test
	void theSentinelPassesWithTheHookActive() throws Exception {
		EngineExecutionResults results = runJupiter(
				"de.tum.cit.ase.ares.generated.GeneratedFailureReportingSentinelTest", true, Locale.ENGLISH);

		assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
	}

	/** The sentinel fails when JUnit does not load the hook. */
	@Test
	void theSentinelFailsWithoutTheHook() throws Exception {
		EngineExecutionResults results = runJupiter(
				"de.tum.cit.ase.ares.generated.GeneratedFailureReportingSentinelTest", false, Locale.ENGLISH);

		assertThat(results.testEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf)).singleElement()
				.asString().contains("not active");
	}

	/**
	 * The include filter keeps a student-registered extension out, while the
	 * instructor's own extension still runs.
	 */
	@Test
	void onlyTheGeneratedAndTheInstructorsExtensionsAreLoaded() throws Exception {
		System.clearProperty(INSTRUCTOR_RAN);
		System.clearProperty(SPY_RAN);
		try {
			runJupiter("PassingFixture", true, Locale.ENGLISH);

			assertThat(System.getProperty(INSTRUCTOR_RAN)).isEqualTo("true");
			assertThat(System.getProperty(SPY_RAN)).isNull();
		} finally {
			System.clearProperty(INSTRUCTOR_RAN);
			System.clearProperty(SPY_RAN);
		}
	}

	/**
	 * A failure raised while JUnit closes a factory's stream, or a container's
	 * child stream, after the factory returned, shows the policy's message.
	 */
	@Test
	void aFailureWhileAFactoryStreamClosesShowsThePolicyMessage() throws Exception {
		EngineExecutionResults results = runJupiter("ClosingFactoryFixture", true, Locale.ENGLISH);

		assertThat(results.allEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf)).hasSize(2)
				.allMatch(POLICY_MESSAGE::equals);
	}

	/**
	 * A known limit, pinned so a change to it is noticed: a failure thrown by
	 * another extension's callback, such as an instructor's
	 * {@code BeforeEachCallback}, keeps its own message, because JUnit hands such
	 * failures to no extension that could replace them.
	 */
	@Test
	void anExtensionCallbackFailureKeepsItsOwnMessage() throws Exception {
		assertThat(failureOf(runJupiter("ThrowingCallbackFixture", true, Locale.ENGLISH), "test")).contains(LEAKED);
	}

	/**
	 * Reusing a class loader cannot make a sentinel pass after its hook is
	 * disabled.
	 *
	 * @throws Exception if either nested run cannot be set up
	 */
	@Test
	void theSentinelNeedsEvidenceFromItsOwnRun() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(nestedRunClassPath(),
				Thread.currentThread().getContextClassLoader())) {
			assertThat(runIn(loader, "de.tum.cit.ase.ares.generated.GeneratedFailureReportingSentinelTest", true,
					Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);

			EngineExecutionResults withoutExtension = runIn(loader,
					"de.tum.cit.ase.ares.generated.GeneratedFailureReportingSentinelTest", false, Locale.ENGLISH);

			assertThat(withoutExtension.testEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf))
					.singleElement().asString().contains("not active");
		}
	}

	/**
	 * Runs a fixture class through a nested JUnit Jupiter session.
	 *
	 * @param fixture       the fixture's simple or qualified name.
	 * @param autodetection whether JUnit loads extensions from service files.
	 * @param locale        the locale messages are shown in.
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults runJupiter(String fixture, boolean autodetection, Locale locale)
			throws Exception {
		String qualified = fixture.contains(".") ? fixture : "com.example.fixtures." + fixture;
		return run(qualified, autodetection, locale);
	}

	/**
	 * Runs a class through a nested JUnit Jupiter session with a fresh class
	 * loader, keeping generated classes separate from other fixtures.
	 *
	 * @param className     the class to run.
	 * @param autodetection whether JUnit loads extensions from service files.
	 * @param locale        the locale messages are shown in.
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults run(String className, boolean autodetection, Locale locale) throws Exception {
		try (URLClassLoader loader = new URLClassLoader(nestedRunClassPath(),
				Thread.currentThread().getContextClassLoader())) {
			return runIn(loader, className, autodetection, locale);
		}
	}

	/**
	 * Runs a class using an existing loader, restoring the caller's locale and
	 * loader.
	 *
	 * @param loader        the loader shared by the nested runs
	 * @param className     the test class
	 * @param autodetection whether JUnit loads registered extensions
	 * @param locale        the locale for messages
	 * @return the run's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults runIn(ClassLoader loader, String className, boolean autodetection,
			Locale locale) throws Exception {
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			return EngineTestKit.engine("junit-jupiter")
					.configurationParameter("junit.jupiter.extensions.autodetection.enabled",
							String.valueOf(autodetection))
					.configurationParameter("junit.jupiter.extensions.autodetection.include", include)
					.selectors(DiscoverySelectors.selectClass(loader.loadClass(className))).execute();
		} finally {
			Thread.currentThread().setContextClassLoader(originalLoader);
			Locale.setDefault(Locale.Category.DISPLAY, originalLocale);
		}
	}

	/**
	 * The class path of a nested run: the generated code, its resources and the
	 * student output.
	 *
	 * @return the class path entries
	 * @throws IOException if a path cannot be turned into a URL
	 */
	private static URL[] nestedRunClassPath() throws IOException {
		List<URL> urls = new ArrayList<>();
		if (shadowed) {
			urls.add(shadowSettings.toUri().toURL());
		}
		for (Path root : List.of(classes, compiled, resources, studentOutput)) {
			urls.add(root.toUri().toURL());
		}
		return urls.toArray(URL[]::new);
	}

	/**
	 * The failure message of one failed test of a run.
	 *
	 * @param results  the run's results.
	 * @param testName the failed test method's name.
	 * @return its failure message
	 */
	private static String failureOf(EngineExecutionResults results, String testName) {
		return results.testEvents().failed().stream()
				.filter(event -> event.getTestDescriptor().getDisplayName().startsWith(testName))
				.map(GeneratedFailureReportingTest::messageOf).findFirst()
				.orElseThrow(() -> new AssertionError("no failed test named " + testName));
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
	 * Writes the fixture tests and extensions.
	 *
	 * @param root the folder to write them into.
	 * @return the folder
	 * @throws IOException if a file cannot be written
	 */
	private static Path writeFixtures(Path root) throws IOException {
		Path folder = Files.createDirectories(root.resolve("com/example/fixtures"));
		Files.writeString(folder.resolve("FailingFixture.java"), """
				package com.example.fixtures;

				import java.util.concurrent.TimeUnit;
				import java.util.concurrent.TimeoutException;

				import org.junit.jupiter.api.Assertions;
				import org.junit.jupiter.api.Test;
				import org.junit.jupiter.api.Timeout;

				class FailingFixture {
					@Test
					void ordinaryFailure() {
						Assertions.assertEquals(42, 41, "expected=42");
					}

					@Test
					void forgedSecurityError() {
						throw new SecurityException("Ares Security Error (Reason: Student-Code) expected=42");
					}

					@Test
					@Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
					void timesOut() throws InterruptedException {
						Thread.sleep(5000);
					}

					@Test
					void forgedTimeout() throws TimeoutException {
						throw new TimeoutException("expected=42");
					}
				}
				""");
		Files.writeString(folder.resolve("HiddenMarkerFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;
				import de.tum.cit.ase.ares.generated.GeneratedHiddenTests;

				class HiddenMarkerFixture {
					@Test void failed() { throw new GeneratedHiddenTests.HiddenTestFailure(); }
					@Test void aborted() { throw new GeneratedHiddenTests.HiddenTestAbort(); }
					@Test void scheduled() { throw new GeneratedHiddenTests.HiddenTestScheduled(); }
				}
				""");
		Files.writeString(folder.resolve("HiddenFirstFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;
				import org.junit.jupiter.api.extension.ExtendWith;
				import de.tum.cit.ase.ares.generated.GeneratedHiddenTests;
				import de.tum.cit.ase.ares.generated.GeneratedFailureReporting;

				@ExtendWith({ GeneratedHiddenTests.class, GeneratedFailureReporting.class })
				class HiddenFirstFixture {
					@Test void secret() { throw new AssertionError("SECRET_HIDDEN_ORDER"); }
				}
				""");
		Files.writeString(folder.resolve("ReportingFirstFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;
				import org.junit.jupiter.api.extension.ExtendWith;
				import de.tum.cit.ase.ares.generated.GeneratedHiddenTests;
				import de.tum.cit.ase.ares.generated.GeneratedFailureReporting;

				@ExtendWith({ GeneratedFailureReporting.class, GeneratedHiddenTests.class })
				class ReportingFirstFixture {
					@Test void secret() { throw new AssertionError("SECRET_HIDDEN_ORDER"); }
				}
				""");
		Files.writeString(folder.resolve("DynamicFixture.java"), """
				package com.example.fixtures;

				import java.util.stream.Stream;

				import org.junit.jupiter.api.DynamicTest;
				import org.junit.jupiter.api.TestFactory;

				class DynamicFixture {
					@TestFactory
					Stream<DynamicTest> cases() {
						return Stream.of(DynamicTest.dynamicTest("case", () -> {
							throw new AssertionError("expected=42");
						}));
					}
				}
				""");
		Files.writeString(folder.resolve("LazyFactoryFixture.java"), """
				package com.example.fixtures;

				import java.util.List;
				import java.util.stream.Stream;

				import org.junit.jupiter.api.DynamicContainer;
				import org.junit.jupiter.api.DynamicNode;
				import org.junit.jupiter.api.DynamicTest;
				import org.junit.jupiter.api.TestFactory;

				class LazyFactoryFixture {
					@TestFactory
					Stream<DynamicTest> failsWhileWalked() {
						return Stream.<DynamicTest>generate(() -> {
							throw new AssertionError("expected=42");
						}).limit(1);
					}

					@TestFactory
					Stream<DynamicNode> failsInsideAContainer() {
						return Stream.of(DynamicContainer.dynamicContainer("box", Stream.<DynamicTest>generate(() -> {
							throw new AssertionError("expected=42");
						}).limit(1)));
					}

					@TestFactory
					List<DynamicTest> passes() {
						return List.of(DynamicTest.dynamicTest("ok", () -> {
						}));
					}
				}
				""");
		Files.writeString(folder.resolve("ConstructorFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;

				class ConstructorFixture {
					private final int answer = Integer.parseInt("expected=42");

					@Test
					void test() {
					}
				}
				""");
		Files.writeString(folder.resolve("BeforeEachFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.BeforeEach;
				import org.junit.jupiter.api.Test;

				class BeforeEachFixture {
					@BeforeEach
					void setUp() {
						throw new IllegalStateException("expected=42");
					}

					@Test
					void test() {
					}
				}
				""");
		Files.writeString(folder.resolve("AfterAllFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.AfterAll;
				import org.junit.jupiter.api.Test;

				class AfterAllFixture {
					@AfterAll
					static void tearDown() {
						throw new IllegalStateException("expected=42");
					}

					@Test
					void test() {
					}
				}
				""");
		Files.writeString(folder.resolve("PassingFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;

				class PassingFixture {
					@Test
					void passes() {
					}
				}
				""");
		Files.writeString(folder.resolve("InstructorExtension.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.extension.BeforeEachCallback;
				import org.junit.jupiter.api.extension.ExtensionContext;

				public class InstructorExtension implements BeforeEachCallback {
					@Override
					public void beforeEach(ExtensionContext context) {
						System.setProperty("ares.test.instructorExtensionRan", "true");
					}
				}
				""");
		Files.writeString(folder.resolve("StudentExtension.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.extension.BeforeEachCallback;
				import org.junit.jupiter.api.extension.ExtensionContext;

				public class StudentExtension implements BeforeEachCallback {
					@Override
					public void beforeEach(ExtensionContext context) {
						System.setProperty("ares.test.studentExtensionRan", "true");
					}
				}
				""");
		Files.writeString(folder.resolve("ClosingFactoryFixture.java"),
				"""
						package com.example.fixtures;

						import java.util.stream.Stream;

						import org.junit.jupiter.api.DynamicContainer;
						import org.junit.jupiter.api.DynamicNode;
						import org.junit.jupiter.api.DynamicTest;
						import org.junit.jupiter.api.TestFactory;

						class ClosingFactoryFixture {
							@TestFactory
							Stream<DynamicTest> failsWhenClosed() {
								return Stream.of(DynamicTest.dynamicTest("ok", () -> {
								})).onClose(() -> {
									throw new AssertionError("expected=42");
								});
							}

							@TestFactory
							Stream<DynamicNode> failsWhenAContainerCloses() {
								return Stream.of(DynamicContainer.dynamicContainer("box", Stream.of(DynamicTest.dynamicTest("ok", () -> {
								})).onClose(() -> {
									throw new AssertionError("expected=42");
								})));
							}
						}
						""");
		Files.writeString(folder.resolve("ThrowingCallback.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.extension.BeforeEachCallback;
				import org.junit.jupiter.api.extension.ExtensionContext;

				public class ThrowingCallback implements BeforeEachCallback {
					@Override
					public void beforeEach(ExtensionContext context) {
						throw new IllegalStateException("expected=42");
					}
				}
				""");
		Files.writeString(folder.resolve("ThrowingCallbackFixture.java"), """
				package com.example.fixtures;

				import org.junit.jupiter.api.Test;
				import org.junit.jupiter.api.extension.ExtendWith;

				@ExtendWith(ThrowingCallback.class)
				class ThrowingCallbackFixture {
					@Test
					void test() {
					}
				}
				""");
		return root;
	}

	/**
	 * Compiles every Java file below the given folders, plus single files, against
	 * this test run's own classpath.
	 *
	 * @param output      the folder to compile into.
	 * @param folders     folders whose Java files to compile.
	 * @param singleFiles further files to compile.
	 * @throws IOException if a folder cannot be listed
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
		int result = compiler.run(null, null, null, arguments.toArray(String[]::new));
		if (result != 0) {
			throw new IllegalStateException("Compiling the generated failure reporting failed");
		}
	}
}
