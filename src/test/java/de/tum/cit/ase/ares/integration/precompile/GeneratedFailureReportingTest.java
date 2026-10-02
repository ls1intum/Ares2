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
import java.util.Optional;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Runs the failure reporting a precompile run generates, end to end: the real
 * generator writes it into a project, the generated sources are compiled with
 * fixture tests, and nested JUnit and jqwik runs show what a student would see.
 */
class GeneratedFailureReportingTest {

	/** The message the policy sets for a failing test. */
	private static final String POLICY_MESSAGE = "Ask your tutor.";

	/** Test data a fixture tries to leak through its failure message. */
	private static final String LEAKED = "expected=42";

	/** The jqwik engine, loaded afresh for each nested jqwik run. */
	private static final String JQWIK_ENGINE = "net.jqwik.engine.JqwikTestEngine";

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

	/** Generated code compiled from a build that does not name jqwik. */
	private static Path withoutJqwikHook;

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
		Files.writeString(project.resolve("pom.xml"), "<project><!-- net.jqwik:jqwik --></project>");
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
		withoutJqwikHook = generateWithoutJqwik(policy);
		classes = compiled;
	}

	/**
	 * Generates and compiles a second project whose build does not name jqwik, so
	 * its sentinel is told no jqwik hook exists.
	 *
	 * @param policy the policy file to generate from.
	 * @return the folder holding the compiled generated code
	 * @throws IOException if writing or compiling fails
	 */
	private static Path generateWithoutJqwik(Path policy) throws IOException {
		Path project = Files.createDirectory(tempDir.resolve("project-without-jqwik"));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("target/classes"));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		Path output = Files.createDirectories(tempDir.resolve("compiled-without-jqwik"));
		compile(output,
				List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
						testSources.resolve("com/example/ares/api/localization")),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		return output;
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

	/** A jqwik hook that throws instead of returning a result is redacted too. */
	@Test
	void aThrowingInnerJqwikHookShowsThePolicyMessage() throws Exception {
		EngineExecutionResults results = run("jqwik", "com.example.fixtures.ThrowingHookFixture", true, Locale.ENGLISH);

		assertThat(results.allEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf))
				.containsExactly(POLICY_MESSAGE);
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

		assertThat(results.testEvents().succeeded().count()).isEqualTo(2);
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
	 * With jqwik on the class path but no jqwik hook generated, the JUnit sentinel
	 * fails, so jqwik properties cannot quietly show their real errors.
	 */
	@Test
	void theSentinelFailsWhenJqwikIsPresentWithoutItsHook() throws Exception {
		classes = withoutJqwikHook;
		try {
			EngineExecutionResults results = runJupiter(
					"de.tum.cit.ase.ares.generated.GeneratedFailureReportingSentinelTest", true, Locale.ENGLISH);

			assertThat(results.testEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf))
					.singleElement().asString().contains("without its jqwik hook");
		} finally {
			classes = compiled;
		}
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
	 * A failing jqwik property shows the policy's message, and jqwik's printed
	 * report does not contain the original message.
	 */
	@Test
	void aFailingJqwikPropertyShowsThePolicyMessage() throws Exception {
		ByteArrayOutputStream printed = new ByteArrayOutputStream();
		PrintStream original = System.out;
		EngineExecutionResults results;
		System.setOut(new PrintStream(printed, true, StandardCharsets.UTF_8));
		try {
			results = run("jqwik", "com.example.fixtures.JqwikFixture", true, Locale.ENGLISH);
		} finally {
			System.setOut(original);
		}

		assertThat(results.testEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf))
				.containsExactly(POLICY_MESSAGE);
		assertThat(printed.toString(StandardCharsets.UTF_8)).doesNotContain(LEAKED);
		assertThat(results.allEvents().reportingEntryPublished().stream().map(Event::toString))
				.noneMatch(entry -> entry.contains(LEAKED));
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
		return run("junit-jupiter", qualified, autodetection, locale);
	}

	/**
	 * Runs a class through a nested session of one engine, with a fresh class
	 * loader, so no earlier run leaves a hook marked active. jqwik itself is loaded
	 * afresh in that loader too, because it caches the hooks it finds once per
	 * loaded engine, and the outer test run has usually loaded it already.
	 *
	 * @param engine        the engine id.
	 * @param className     the class to run.
	 * @param autodetection whether JUnit loads extensions from service files.
	 * @param locale        the locale messages are shown in.
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults run(String engine, String className, boolean autodetection, Locale locale)
			throws Exception {
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
		try (URLClassLoader loader = new JqwikIsolatingClassLoader(nestedRunClassPath(), originalLoader)) {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			EngineTestKit.Builder builder = "jqwik".equals(engine)
					? EngineTestKit
							.engine((TestEngine) loader.loadClass(JQWIK_ENGINE).getDeclaredConstructor().newInstance())
					: EngineTestKit.engine(engine);
			return builder
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
	 * The class path of a nested run: the generated code, its resources, the
	 * student output and this run's jqwik JARs.
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
		for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
			if (Path.of(entry).getFileName().toString().startsWith("jqwik")) {
				urls.add(Path.of(entry).toUri().toURL());
			}
		}
		return urls.toArray(URL[]::new);
	}

	/**
	 * Loads jqwik's classes from its own class path first, so each nested run gets
	 * a jqwik that has not yet looked for hooks. Everything else comes from the
	 * outer test run as usual.
	 */
	private static final class JqwikIsolatingClassLoader extends URLClassLoader {

		/**
		 * Creates the loader.
		 *
		 * @param urls   the nested run's class path.
		 * @param parent the outer test run's class loader.
		 */
		JqwikIsolatingClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		/**
		 * Loads a jqwik class from this loader's own class path, anything else parent
		 * first.
		 *
		 * @param name    the binary class name.
		 * @param resolve whether to link the class.
		 * @return the class
		 * @throws ClassNotFoundException if no loader has it
		 */
		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (!name.startsWith("net.jqwik.")) {
				return super.loadClass(name, resolve);
			}
			synchronized (getClassLoadingLock(name)) {
				Class<?> loaded = findLoadedClass(name);
				Class<?> found = loaded != null ? loaded : findClass(name);
				if (resolve) {
					resolveClass(found);
				}
				return found;
			}
		}
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
		Files.writeString(folder.resolve("ThrowingHook.java"),
				"""
						package com.example.fixtures;

						import net.jqwik.api.lifecycle.AroundPropertyHook;
						import net.jqwik.api.lifecycle.PropertyExecutionResult;
						import net.jqwik.api.lifecycle.PropertyExecutor;
						import net.jqwik.api.lifecycle.PropertyLifecycleContext;

						public class ThrowingHook implements AroundPropertyHook {
							@Override
							public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) {
								throw new IllegalStateException("expected=42");
							}
						}
						""");
		Files.writeString(folder.resolve("ThrowingHookFixture.java"), """
				package com.example.fixtures;

				import net.jqwik.api.Example;
				import net.jqwik.api.lifecycle.AddLifecycleHook;

				class ThrowingHookFixture {
					@Example
					@AddLifecycleHook(ThrowingHook.class)
					void setUpByAThrowingHook() {
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
		Files.writeString(folder.resolve("JqwikFixture.java"), """
				package com.example.fixtures;

				import net.jqwik.api.Example;

				class JqwikFixture {
					@Example
					void fails() {
						throw new IllegalStateException("expected=42");
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
