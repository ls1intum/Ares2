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
		Files.writeString(instructorServices, "com.example.fixtures.InstructorExtension" + System.lineSeparator());
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
		compile(Stream.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
				testSources.resolve("com/example/ares/api/localization"), fixtures).toList(),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
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
	 * loader, so no earlier run leaves a hook marked active.
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
		try (URLClassLoader loader = new URLClassLoader(
				new URL[] { compiled.toUri().toURL(), resources.toUri().toURL(), studentOutput.toUri().toURL() },
				originalLoader)) {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			return EngineTestKit.engine(engine)
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
	 * Compiles every Java file below the given folders, plus single files, into
	 * {@link #compiled}, against this test run's own classpath.
	 *
	 * @param folders     folders whose Java files to compile.
	 * @param singleFiles further files to compile.
	 * @throws IOException if a folder cannot be listed
	 */
	private static void compile(List<Path> folders, Path... singleFiles) throws IOException {
		List<String> arguments = new ArrayList<>(
				List.of("-d", compiled.toString(), "-classpath", System.getProperty("java.class.path"), "-proc:none"));
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
