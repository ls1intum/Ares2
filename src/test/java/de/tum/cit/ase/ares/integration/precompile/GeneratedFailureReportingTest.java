package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.internal.SecurityViolationFeedbackTexts;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

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
							public static final class HiddenTestFailure extends AssertionError implements java.util.function.Supplier<Throwable> {
								public HiddenTestFailure() { setStackTrace(new StackTraceElement[0]); }
								@Override public Throwable get() { return new HiddenTestFailure(); }
								@Override public String toString() { return ""; }
							}
							public static final class HiddenTestAbort extends org.opentest4j.TestAbortedException implements java.util.function.Supplier<Throwable> {
								public HiddenTestAbort() { setStackTrace(new StackTraceElement[0]); }
								@Override public Throwable get() { return new HiddenTestAbort(); }
								@Override public String toString() { return ""; }
							}
							public static final class HiddenTestScheduled extends org.opentest4j.AssertionFailedError implements java.util.function.Supplier<Throwable> {
								public HiddenTestScheduled() { super("hidden tests will be executed after the deadline."); }
								@Override public Throwable get() { return new HiddenTestScheduled(); }
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
				    regardingTheFailureFeedbackForTheTestCases:
				      onlyPrivilegedExceptionsAreReported: true
				      theFailureMessageForOtherExceptionsIs: "Ask your tutor."
				""";
	}

	/**
	 * The DEBUG entry for a blocked file action is one complete, quoted list item:
	 * pasted into a policy, Ares's own reader accepts it as written and a Windows
	 * path reads back unchanged.
	 *
	 * @param policyFolder where the policy with the pasted entry is written.
	 * @throws Exception if the generated hook cannot be loaded
	 */
	@Test
	void theDebugFileEntryIsACompletePolicyEntry(@TempDir Path policyFolder) throws Exception {
		String target = "C:\\temp\\new \"draft\".txt";
		try (URLClassLoader loader = new URLClassLoader(new URL[] { compiled.toUri().toURL() },
				GeneratedFailureReportingTest.class.getClassLoader())) {
			java.lang.reflect.Method entryOf = loader
					.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
					.getDeclaredMethod("entryOf", String.class, String.class, String.class);
			entryOf.setAccessible(true);
			String entry = (String) entryOf.invoke(null, "file", "create", target);
			java.util.List<java.util.Map<String, Object>> entries = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper()
					.readValue(entry,
							new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {
							});
			assertThat(entries).hasSize(1);
			java.util.Map<String, Object> read = entries.get(0);
			assertThat(read)
					.containsOnlyKeys("onThisPathAndAllPathsBelow", "readAllFiles", "overwriteAllFiles",
							"createAllFiles", "executeAllFiles", "deleteAllFiles")
					.containsEntry("onThisPathAndAllPathsBelow", target).containsEntry("createAllFiles", true)
					.containsEntry("readAllFiles", false).containsEntry("overwriteAllFiles", false)
					.containsEntry("executeAllFiles", false).containsEntry("deleteAllFiles", false);
			Path policy = policyFolder.resolve("Pasted.yaml");
			Files.writeString(policy, """
					thisPolicyFileCompliesToThePolicyVersion: 1
					regardingTheSupervisedCode:
					  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
					  theFollowingClassesAreTestClasses: []
					  theFollowingResourceAccessesArePermitted:
					    regardingFileSystemInteractions:
					"""
					+ entry.lines().map(line -> "      " + line + "\n").collect(java.util.stream.Collectors.joining())
					+ """
							    regardingNetworkConnections: []
							    regardingCommandExecutions: []
							    regardingThreadCreations: []
							    regardingPackageImports: []
							    regardingTimeouts: []
							""");
			assertThat(de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader.selectSecurityPolicyReader(policy)
					.readSecurityPolicyFrom(policy).regardingTheSupervisedCode()
					.theFollowingResourceAccessesArePermitted().regardingFileSystemInteractions()).containsExactly(
							new de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission(target, false, false,
									true, false, false));
		}
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
				.isEqualTo("Der Test hat das Zeitlimit \u00fcberschritten.");
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
	 * A factory whose tests fail while JUnit walks them, directly, inside a
	 * container or through a result that only has a public or package-private
	 * {@code iterator()} method, shows the policy's message; a stream whose
	 * iterator cannot be made is closed; and a passing factory still runs.
	 */
	@Test
	void aFactoryFailingWhileWalkedShowsThePolicyMessage() throws Exception {
		System.clearProperty("ares.test.factoryStreamClosed");
		EngineExecutionResults results = runJupiter("LazyFactoryFixture", true, Locale.ENGLISH);

		assertThat(results.allEvents().failed().stream().map(GeneratedFailureReportingTest::messageOf)).hasSize(5)
				.allMatch(POLICY_MESSAGE::equals);
		assertThat(System.getProperty("ares.test.factoryStreamClosed")).isEqualTo("true");
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
		return GeneratedCodeRuns.runFresh(nestedRunClassPath(), qualified, autodetection, include, locale);
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
		return GeneratedCodeRuns.runIn(loader, className, autodetection, include, locale);
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
		return GeneratedCodeRuns.messageOf(event);
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
				import java.util.Spliterator;
				import java.util.stream.Stream;
				import java.util.stream.StreamSupport;

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
						return Stream.of(DynamicContainer.dynamicContainer("box",
								StreamSupport.<DynamicNode>stream(() -> {
									throw new AssertionError("expected=42");
								}, Spliterator.ORDERED, false)));
					}

					@TestFactory
					IteratorOnly failsWhileWalkedThroughItsIterator() {
						return new IteratorOnly();
					}

					@TestFactory
					PackageIteratorOnly failsWhileWalkedThroughItsPackageIterator() {
						return new PackageIteratorOnly();
					}

					@TestFactory
					Stream<DynamicTest> failsWhileItsIteratorIsMade() {
						Stream<DynamicTest> used = Stream.of(DynamicTest.dynamicTest("never", () -> {
						})).onClose(() -> System.setProperty("ares.test.factoryStreamClosed", "true"));
						used.count();
						return used;
					}

					@TestFactory
					List<DynamicTest> passes() {
						return List.of(DynamicTest.dynamicTest("ok", () -> {
						}));
					}

					public static final class IteratorOnly {
						public java.util.Iterator<DynamicTest> iterator() {
							return Stream.<DynamicTest>generate(() -> {
								throw new AssertionError("expected=42");
							}).limit(1).iterator();
						}
					}

					public static final class PackageIteratorOnly {
						java.util.Iterator<DynamicTest> iterator() {
							return Stream.<DynamicTest>generate(() -> {
								throw new AssertionError("expected=42");
							}).limit(1).iterator();
						}
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
		GeneratedCodeRuns.compile(output, folders, singleFiles);
	}

	/**
	 * Runs the failure reporting a precompile run generates against the security
	 * tests Ares generates into the exercise, and checks that their failures follow
	 * the policy's feedback level: unchanged at HIGH, the fixed text at LOW, the
	 * report plus the policy category at DEBUG. The architecture tests are
	 * stand-ins under their exact generated names, because the generated ones need
	 * the copied analysis libraries to compile.
	 */
	@Nested
	class ArchitectureFeedbackLevel {

		/** The generated ArchUnit security test class of the exercise. */
		private static final String ARCHUNIT_TEST = "com.example.ares.api.architecture.java.archunit.JavaArchunitTestCase";

		/** The generated WALA security test class of the exercise. */
		private static final String WALA_TEST = "com.example.ares.api.architecture.java.wala.JavaWalaTestCase";

		/** A class with the same method, outside the generated security tests. */
		private static final String LOOKALIKE = "com.example.fixtures.JavaArchunitTestCase";

		/** What the architecture check reports, including a path a student chose. */
		private static final String REPORT = "Architecture Violation: Student.read reads /secret/path";

		/**
		 * Ares's own configuration error, as a generated architecture test reports it.
		 */
		private static final String CONFIGURATION_ERROR = "Ares Security Error (Reason: Ares-Code; Stage: Creation): no class matches the scope";

		/** A failure that wraps a blocked action, with a text of its own. */
		private static final String WRAPPED_TEST = "com.example.fixtures.WrappedViolationTest";

		/** The message of the wrapped blocked action. */
		private static final String VIOLATION = "Ares Security Error (Reason: Student-Code; Stage: Execution): blocked read";

		/** The message the policy sets for other failures. */
		private static final String POLICY_MESSAGE = "Ask your tutor.";

		/** The projects shared by every test of this class. */
		@TempDir
		static Path tempDir;

		/** Compiled output for a policy at HIGH with privileged-only reporting on. */
		private static Path full;

		/** Compiled output for a policy at LOW. */
		private static Path low;

		/** Compiled output for a policy at DEBUG. */
		private static Path debug;

		/**
		 * Generates and compiles the precompile output for the three policies.
		 *
		 * @throws IOException if writing or compiling fails
		 */
		@BeforeAll
		static void generateAndCompile() throws IOException {
			full = project("full", "HIGH", true);
			low = project("low", "LOW", false);
			debug = project("debug", "DEBUG", false);
		}

		/**
		 * At HIGH, the generated security tests keep their report even with
		 * privileged-only reporting on, while another failing test gets the policy's
		 * message.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void fullKeepsTheArchitectureReport() throws Exception {
			assertThat(messages(full, ARCHUNIT_TEST, Locale.ENGLISH)).containsExactlyInAnyOrder(REPORT,
					CONFIGURATION_ERROR);
			assertThat(messages(full, WALA_TEST, Locale.ENGLISH)).containsExactly(REPORT);
			assertThat(messages(full, LOOKALIKE, Locale.ENGLISH)).containsExactly(POLICY_MESSAGE);
		}

		/**
		 * At LOW, the generated security tests show only that something was blocked, in
		 * both locales, and nothing of the report.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void lowShowsOnlyTheFixedText() throws Exception {
			for (String test : List.of(ARCHUNIT_TEST, WALA_TEST)) {
				assertThat(messages(low, test, Locale.ENGLISH)).contains(
						"Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.")
						.noneMatch(message -> message.contains("/secret/path"));
				assertThat(messages(low, test, Locale.GERMAN))
						.filteredOn(message -> !message.equals(CONFIGURATION_ERROR)).singleElement().asString()
						.startsWith("Ares Sicherheitsfehler").doesNotContain("/secret/path");
			}
		}

		/**
		 * At DEBUG, the generated security tests show the report and the category that
		 * would allow the access.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void debugAddsTheCategory() throws Exception {
			for (String test : List.of(ARCHUNIT_TEST, WALA_TEST)) {
				assertThat(messages(debug, test, Locale.ENGLISH)).filteredOn(message -> message.startsWith(REPORT))
						.singleElement().asString().endsWith(
								"Allow it by adding an entry under regardingFileSystemInteractions in the security policy SecurityPolicy.yaml.");
			}
		}

		/**
		 * At DEBUG, a blocked argument Ares could not inspect gets no allow hint, since
		 * no policy entry can allow it, while a blocked file action still gets one.
		 *
		 * @throws Exception if the generated hook cannot be loaded
		 */
		@Test
		void debugGivesNoHintForAnUninspectableArgument() throws Exception {
			assertThat(renderedAtDebug(new Blocked("uninspectable", "inspect", "a student Path")))
					.isEqualTo("Main.run tried to illegally inspect a student Path but was blocked by Ares.");
			assertThat(renderedAtDebug(new Blocked("file", "read", "/secret/path")))
					.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debug(
							"Main.run tried to illegally read /secret/path but was blocked by Ares.", "read",
							"/secret/path", java.util.Optional.of("SecurityPolicy.yaml"),
							SecurityViolationFeedbackTexts.lines("- onThisPathAndAllPathsBelow: \"/secret/path\"",
									"  readAllFiles: true", "  overwriteAllFiles: false", "  createAllFiles: false",
									"  executeAllFiles: false", "  deleteAllFiles: false"))));
		}

		/**
		 * At DEBUG, a target no single entry can name exactly gets only the category: a
		 * command with arguments, whose boundaries the target text lost (one argument
		 * holding a space reads as two), a target that is the wildcard {@code *}, port
		 * zero, a thread, whose denial may be an exhausted quota, a path holding a
		 * {@code ${...}} placeholder, which the policy expands, and a target cut at the
		 * 200-character field limit. A command without arguments gets its entry with an
		 * empty argument list, and a path with a {@code *} that is not the whole path
		 * gets its entry too, since only the whole value is the wildcard.
		 *
		 * @throws Exception if the generated hook cannot be loaded
		 */
		@Test
		void debugNamesOnlyTheCategoryWhenNoEntryIsExact() throws Exception {
			String echo = "Main.run tried to illegally execute /bin/echo hello world but was blocked by Ares.";
			assertThat(renderedAtDebug(new Blocked("command", "execute", "/bin/echo hello world")))
					.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(echo,
							"regardingCommandExecutions", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("file", "read", "*")))
					.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(
							"Main.run tried to illegally read * but was blocked by Ares.",
							"regardingFileSystemInteractions", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("network", "connect", "*:443")))
					.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
							"regardingNetworkConnections", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("network", "connect", "127.0.0.1:0")))
					.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
							"regardingNetworkConnections", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("thread", "create", "java.lang.Thread")))
					.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
							"regardingThreadCreations", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("file", "read", "/home/${user}/notes")))
					.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
							"regardingFileSystemInteractions", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("file", "read", "/data/a *")))
					.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debug(
							"Main.run tried to illegally read /data/a * but was blocked by Ares.", "read", "/data/a *",
							java.util.Optional.of("SecurityPolicy.yaml"),
							SecurityViolationFeedbackTexts.lines("- onThisPathAndAllPathsBelow: \"/data/a *\"",
									"  readAllFiles: true", "  overwriteAllFiles: false", "  createAllFiles: false",
									"  executeAllFiles: false", "  deleteAllFiles: false"))));
			String longPath = "/" + "a".repeat(250);
			assertThat(renderedAtDebug(new Blocked("file", "read", longPath)))
					.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
							"regardingFileSystemInteractions", java.util.Optional.of("SecurityPolicy.yaml"))));
			assertThat(renderedAtDebug(new Blocked("command", "execute", "/bin/date")))
					.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debug(
							"Main.run tried to illegally execute /bin/date but was blocked by Ares.", "execute",
							"/bin/date", java.util.Optional.of("SecurityPolicy.yaml"), SecurityViolationFeedbackTexts
									.lines("- executeTheCommand: \"/bin/date\"", "  withTheseArguments: []"))));
		}

		/**
		 * At DEBUG, the report keeps the blocked action's own stack, so an instructor
		 * sees where the action happened.
		 *
		 * @throws Exception if the generated hook cannot be loaded
		 */
		@Test
		void debugKeepsTheBlockedActionsStack() throws Exception {
			Blocked blocked = new Blocked("file", "read", "/secret/path");
			StackTraceElement[] stack = { new StackTraceElement("org.example.Main", "run", "Main.java", 7) };
			blocked.setStackTrace(stack);
			assertThat(invokeTheDebugHookUnchecked("renderedViolation", new Class<?>[] { Throwable.class }, blocked)
					.getStackTrace()).containsExactly(stack);
		}

		/**
		 * At DEBUG, a generated architecture test for a fixed rule, such as reflection,
		 * which no policy entry relaxes, shows its report without a hint, while a
		 * file-system test names its category.
		 *
		 * @throws Exception if the generated hook cannot be loaded
		 */
		@Test
		void debugGivesAFixedArchitectureRuleNoHint() throws Exception {
			SecurityException reflection = new SecurityException("Main.run tried to illegally use reflection.");
			assertThat(inEnglish(() -> invokeTheDebugHookUnchecked("architectureReplacementFor",
					new Class<?>[] { Throwable.class, String.class }, reflection,
					"theFollowingResourceAccessesArePermitted").getMessage()))
							.isEqualTo("Main.run tried to illegally use reflection.");
			SecurityException file = new SecurityException("Main.run tried to illegally access the file system.");
			assertThat(inEnglish(() -> invokeTheDebugHookUnchecked("architectureReplacementFor",
					new Class<?>[] { Throwable.class, String.class }, file, "regardingFileSystemInteractions")
							.getMessage())).isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(
									"Main.run tried to illegally access the file system.",
									"regardingFileSystemInteractions", java.util.Optional.of("SecurityPolicy.yaml"))));
		}

		/**
		 * Calls one static method of the DEBUG project's failure reporting.
		 *
		 * @param method    the method's name.
		 * @param types     its parameter types.
		 * @param arguments its arguments.
		 * @return the failure it returns
		 */
		private static Throwable invokeTheDebugHookUnchecked(String method, Class<?>[] types, Object... arguments) {
			try (java.net.URLClassLoader loader = new java.net.URLClassLoader(
					new java.net.URL[] { debug.toUri().toURL(),
							debug.resolveSibling("debug-compiled-resources").toUri().toURL() },
					ArchitectureFeedbackLevel.class.getClassLoader())) {
				java.lang.reflect.Method called = loader
						.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
						.getDeclaredMethod(method, types);
				called.setAccessible(true);
				return (Throwable) called.invoke(null, arguments);
			} catch (Exception unexpected) {
				throw new IllegalStateException(unexpected);
			}
		}

		/**
		 * Renders a blocked action as the DEBUG project's failure reporting does, with
		 * English as the display language, so the expected English text does not depend
		 * on the machine's language.
		 *
		 * @param violation the blocked action.
		 * @return the rendered text
		 * @throws Exception if the generated hook cannot be loaded
		 */
		private static String renderedAtDebug(Throwable violation) throws Exception {
			return inEnglish(() -> renderedByTheDebugHookUnchecked(violation));
		}

		/**
		 * Computes a text with English as the display language.
		 *
		 * @param text the text to compute.
		 * @return the text
		 */
		private static String inEnglish(java.util.function.Supplier<String> text) {
			Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
			try {
				Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
				return text.get();
			} finally {
				Locale.setDefault(Locale.Category.DISPLAY, originalLocale);
			}
		}

		/**
		 * {@link #renderedByTheDebugHook} with its checked failure wrapped.
		 *
		 * @param violation the blocked action.
		 * @return the rendered text
		 */
		private static String renderedByTheDebugHookUnchecked(Throwable violation) {
			try {
				return renderedByTheDebugHook(violation);
			} catch (Exception unexpected) {
				throw new IllegalStateException(unexpected);
			}
		}

		/**
		 * Loads the DEBUG project's failure reporting and renders a blocked action with
		 * it.
		 *
		 * @param violation the blocked action.
		 * @return the rendered text
		 * @throws Exception if the generated hook cannot be loaded
		 */
		private static String renderedByTheDebugHook(Throwable violation) throws Exception {
			try (java.net.URLClassLoader loader = new java.net.URLClassLoader(
					new java.net.URL[] { debug.toUri().toURL(),
							debug.resolveSibling("debug-compiled-resources").toUri().toURL() },
					ArchitectureFeedbackLevel.class.getClassLoader())) {
				java.lang.reflect.Method rendered = loader
						.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
						.getDeclaredMethod("renderedViolation", Throwable.class);
				rendered.setAccessible(true);
				return ((Throwable) rendered.invoke(null, violation)).getMessage();
			}
		}

		/** A blocked action with the fields the failure reporting reads. */
		public static final class Blocked extends RuntimeException {
			/** Serialisation identifier. */
			private static final long serialVersionUID = 1L;

			/** The kind of action. */
			private final String kind;

			/** The action. */
			private final String action;

			/** What it was aimed at. */
			private final String target;

			/**
			 * A blocked action.
			 *
			 * @param kind   the kind of action.
			 * @param action the action.
			 * @param target what it was aimed at.
			 */
			Blocked(String kind, String action, String target) {
				super("Main.run tried to illegally " + action + " " + target + " but was blocked by Ares.");
				this.kind = kind;
				this.action = action;
				this.target = target;
			}

			/**
			 * The kind of action.
			 *
			 * @return the kind
			 */
			public String kind() {
				return kind;
			}

			/**
			 * The action.
			 *
			 * @return the action
			 */
			public String action() {
				return action;
			}

			/**
			 * What it was aimed at.
			 *
			 * @return the target
			 */
			public String target() {
				return target;
			}
		}

		/**
		 * Ares's own configuration error in a generated architecture test keeps its
		 * text at every level, so it is neither shortened nor given an allow hint.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aConfigurationErrorKeepsItsText() throws Exception {
			for (Path project : List.of(low, debug)) {
				assertThat(messages(project, ARCHUNIT_TEST, Locale.ENGLISH)).contains(CONFIGURATION_ERROR);
			}
		}

		/**
		 * With privileged-only reporting at HIGH, a failure that wraps a blocked action
		 * reports the blocked action, not the wrapper's own text.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aWrappedViolationIsReportedWithoutItsWrapper() throws Exception {
			assertThat(messages(full, WRAPPED_TEST, Locale.ENGLISH)).containsExactly(VIOLATION);
		}

		/**
		 * With privileged-only reporting at HIGH, a blocked action carrying a student
		 * failure whose {@code toString} never returns is reported as a copy with its
		 * own text, so printing the report runs no student code, in a JVM of its own.
		 *
		 * @throws Exception if compiling or starting the JVM fails
		 */
		@Test
		void aFullViolationIsReportedAsACopy() throws Exception {
			String output = runProbe("loop");
			assertThat(output).as(output).contains("ares.probe.message=" + VIOLATION).contains("ares.probe.done");
		}

		/**
		 * With privileged-only reporting at HIGH, an ordinary assertion attached to a
		 * blocked action as its cause or suppressed failure shows only the policy's
		 * message, while the blocked action keeps its own text.
		 *
		 * @throws Exception if compiling or starting the JVM fails
		 */
		@Test
		void aFullViolationRedactsWhatIsAttachedToIt() throws Exception {
			String output = runProbe("assertion");
			assertThat(output).as(output).contains("ares.probe.message=" + VIOLATION).contains(POLICY_MESSAGE)
					.doesNotContain("expected=42").doesNotContain("cause=7").contains("ares.probe.done");
		}

		/**
		 * With privileged-only reporting at HIGH, a class named like the copied blocked
		 * action but loaded by another class loader is not treated as one, so its own
		 * text is never shown.
		 *
		 * @throws Exception if compiling or starting the JVM fails
		 */
		@Test
		void aLookalikeViolationFromAnotherLoaderIsNotKept() throws Exception {
			String output = runProbe("lookalike");
			assertThat(output).as(output).contains("ares.probe.message=" + POLICY_MESSAGE)
					.doesNotContain("lookalike-secret").contains("ares.probe.done");
		}

		/**
		 * Runs the compiled probe in a JVM of its own and returns what it printed.
		 *
		 * @param mode what the probe attaches to the blocked action.
		 * @return the probe's output
		 * @throws Exception if compiling or starting the JVM fails
		 */
		private String runProbe(String mode) throws Exception {
			Path probe = compileProbe(Files.createDirectories(tempDir.resolve("probe-" + mode)));
			String classPath = String.join(File.pathSeparator, probe.toString(), full.toString(),
					full.resolveSibling(full.getFileName() + "-resources").toString(),
					System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
			Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
					"-cp", classPath, "com.example.probe.FullViolationProbe", VIOLATION, mode, full.toUri().toString())
							.redirectErrorStream(true).start();
			try {
				java.util.concurrent.CompletableFuture<String> drained = GeneratedCodeRuns.drain(process);
				assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
				return drained.get(10, TimeUnit.SECONDS);
			} finally {
				process.destroyForcibly();
			}
		}

		/**
		 * Compiles a probe that hands the HIGH project's failure reporting a blocked
		 * action with a looping student failure, or ordinary assertions, attached and
		 * prints the report.
		 *
		 * @param root the probe's folder.
		 * @return the compiled probe's folder
		 * @throws IOException if writing or compiling fails
		 */
		private static Path compileProbe(Path root) throws IOException {
			Path sources = Files.createDirectories(root.resolve("sources/com/example/probe"));
			Files.writeString(sources.resolve("FullViolationProbe.java"),
					"""
							package com.example.probe;

							public final class FullViolationProbe {
								public static void main(String[] arguments) throws Exception {
									ClassLoader loader = FullViolationProbe.class.getClassLoader();
									ClassLoader origin = "lookalike".equals(arguments[1])
											? new java.net.URLClassLoader(new java.net.URL[] { java.net.URI.create(arguments[2]).toURL() }, null)
											: loader;
									Throwable violation = (Throwable) origin
											.loadClass("com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation")
											.getConstructor(String.class)
											.newInstance("lookalike".equals(arguments[1]) ? "lookalike-secret" : arguments[0]);
									if ("assertion".equals(arguments[1])) {
										violation.initCause(new AssertionError("cause=7"));
										violation.addSuppressed(new AssertionError("expected=42"));
									} else {
										violation.addSuppressed(new RuntimeException("attached") {
											@Override
											public String toString() {
												while (true) {
													Thread.onSpinWait();
												}
											}
										});
									}
									java.lang.reflect.Method replacement = loader
											.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
											.getDeclaredMethod("replacementFor", Throwable.class);
									replacement.setAccessible(true);
									Throwable reported = (Throwable) replacement.invoke(null, violation);
									java.io.StringWriter trace = new java.io.StringWriter();
									reported.printStackTrace(new java.io.PrintWriter(trace));
									System.out.println("ares.probe.message=" + reported.getMessage());
									System.out.println("ares.probe.trace=" + trace);
									System.out.println("ares.probe.done");
									System.exit(0);
								}
							}
							""");
			Path classes = Files.createDirectories(root.resolve("classes"));
			GeneratedCodeRuns.compile(classes, List.of(root.resolve("sources")));
			return classes;
		}

		/**
		 * A class that only looks like a generated security test is not shortened, so
		 * the level applies to Ares's own tests by their exact names alone.
		 *
		 * @throws Exception if the run cannot be set up
		 */
		@Test
		void aLookalikeIsNotTreatedAsASecurityTest() throws Exception {
			assertThat(messages(low, LOOKALIKE, Locale.ENGLISH)).containsExactly(REPORT);
		}

		/**
		 * Runs one class through a nested JUnit session and returns its failure
		 * messages.
		 *
		 * @param project the compiled project.
		 * @param test    the class to run.
		 * @param locale  the locale messages are shown in.
		 * @return the failure messages
		 * @throws Exception if the run cannot be set up
		 */
		private static List<String> messages(Path project, String test, Locale locale) throws Exception {
			String include = Files
					.readAllLines(project.resolveSibling(project.getFileName() + "-resources")
							.resolve("junit-platform.properties"))
					.stream().filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
					.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
			return GeneratedCodeRuns.failureMessages(GeneratedCodeRuns.runFresh(
					GeneratedCodeRuns
							.classPath(List.of(project, project.resolveSibling(project.getFileName() + "-resources"))),
					test, true, include, locale));
		}

		/**
		 * Generates the precompile output for one policy and compiles it with the
		 * stand-ins.
		 *
		 * @param name           the project's folder name.
		 * @param level          the feedback level.
		 * @param privilegedOnly whether only privileged exceptions are reported.
		 * @return the compiled project
		 * @throws IOException if writing or compiling fails
		 */
		private static Path project(String name, String level, boolean privilegedOnly) throws IOException {
			Path project = Files.createDirectory(tempDir.resolve(name));
			Files.writeString(project.resolve("pom.xml"), "<project/>");
			Files.createDirectories(project.resolve("src/main/java"));
			Path testSources = Files.createDirectories(project.resolve("src/test/java"));
			Files.createDirectories(project.resolve("target/classes"));
			Path policy = project.resolve("SecurityPolicy.yaml");
			Files.writeString(policy, policyText(level, privilegedOnly));
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
					.createTestCases().writeTestCases(testSources);
			Path compiled = Files.createDirectories(tempDir.resolve(name + "-compiled"));
			List<Path> standIns = writeStandIns(Files.createDirectories(tempDir.resolve(name + "-stand-ins")));
			List<Path> singles = new ArrayList<>(standIns);
			singles.add(testSources.resolve("com/example/ares/api/util/LruCache.java"));
			GeneratedCodeRuns.compile(compiled, List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
					testSources.resolve("com/example/ares/api/localization")), singles.toArray(Path[]::new));
			copyResources(project.resolve("src/test/resources"), tempDir.resolve(name + "-compiled-resources"));
			return compiled;
		}

		/**
		 * Copies the generated test resources next to the compiled project.
		 *
		 * @param from the generated resources.
		 * @param to   where the nested run reads them.
		 * @throws IOException if copying fails
		 */
		private static void copyResources(Path from, Path to) throws IOException {
			try (var files = Files.walk(from)) {
				for (Path file : files.filter(Files::isRegularFile).toList()) {
					Path target = to.resolve(from.relativize(file).toString());
					Files.createDirectories(target.getParent());
					Files.copy(file, target);
				}
			}
		}

		/**
		 * Writes the stand-ins: the two generated security tests and a lookalike, each
		 * failing its file-system check with the same report, the ArchUnit one also
		 * with a configuration error, and a test wrapping a blocked action under the
		 * generated violation's exact name.
		 *
		 * @param sources the folder to write them into.
		 * @return the written files
		 * @throws IOException if writing fails
		 */
		private static List<Path> writeStandIns(Path sources) throws IOException {
			List<Path> files = new ArrayList<>();
			for (String className : List.of(ARCHUNIT_TEST, WALA_TEST, LOOKALIKE)) {
				String configurationTest = className.equals(ARCHUNIT_TEST) ? """
						@Test
						public void networkSystemShouldNotBeAccessed() {
							throw new SecurityException("%s");
						}
						""".formatted(CONFIGURATION_ERROR) : "";
				files.add(writeClass(sources, className, """
						import org.junit.jupiter.api.Test;

						public class %s {
							@Test
							public void filesystemsShouldNotBeAccessed() {
								throw new AssertionError("%s");
							}
							%s
						}
						""".formatted(simpleName(className), REPORT, configurationTest)));
			}
			files.add(writeClass(sources,
					"com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation", """
							public final class AresSecurityViolation extends SecurityException {
								private static final long serialVersionUID = 1L;
								public AresSecurityViolation(String message) { super(message); }
								public String kind() { return "file"; }
								public String action() { return "read"; }
								public String target() { return "secret.txt"; }
							}
							"""));
			files.add(writeClass(sources, WRAPPED_TEST,
					"""
							import org.junit.jupiter.api.Test;

							public class WrappedViolationTest {
								@Test
								public void wraps() {
									throw new AssertionError("secret expected value",
											new com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation("%s"));
								}
							}
							"""
							.formatted(VIOLATION)));
			return files;
		}

		/**
		 * Writes one source file under its package's folder.
		 *
		 * @param sources   the source root.
		 * @param className the class's full name.
		 * @param body      the source after the package declaration.
		 * @return the written file
		 * @throws IOException if writing fails
		 */
		private static Path writeClass(Path sources, String className, String body) throws IOException {
			String packageName = className.substring(0, className.lastIndexOf('.'));
			Path file = Files.createDirectories(sources.resolve(packageName.replace('.', '/')))
					.resolve(simpleName(className) + ".java");
			Files.writeString(file, "package " + packageName + ";\n\n" + body);
			return file;
		}

		/**
		 * The simple name of a class.
		 *
		 * @param className the class's full name.
		 * @return the part after the last dot
		 */
		private static String simpleName(String className) {
			return className.substring(className.lastIndexOf('.') + 1);
		}

		/**
		 * A policy for the {@code com.example} exercise with a failure-feedback
		 * category.
		 *
		 * @param level          the feedback level.
		 * @param privilegedOnly whether only privileged exceptions are reported.
		 * @return the policy text
		 */
		private static String policyText(String level, boolean privilegedOnly) {
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
					    regardingTheFailureFeedbackForTheTestCases:
					      onlyPrivilegedExceptionsAreReported: %s
					      theFailureMessageForOtherExceptionsIs: "%s"
					      theSecurityViolationFeedbackLevelIs: %s
					""".formatted(privilegedOnly, POLICY_MESSAGE, level);
		}
	}
}
