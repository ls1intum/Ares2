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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
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
 * Runs the strict timeout a precompile run generates, end to end: the real
 * generator writes it into a project, the generated sources are compiled with
 * fixture tests, and nested JUnit runs show every timed invocation stopping at
 * the policy's limit.
 */
class GeneratedStrictTimeoutTest {

	/** The generated JUnit interceptor's sentinel test. */
	private static final String JUPITER_SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedStrictTimeoutSentinelTest";

	/** The policy's timeout, as the timeout message formats it. */
	private static final String LIMIT = "100 ms";

	/** The generated project, shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** The compiled generated code and fixtures. */
	private static Path compiled;

	/**
	 * A settings class with an hour's timeout, standing in for one a student
	 * replaced.
	 */
	private static Path shadowSettings;

	/** The generated code nested runs load ahead of {@link #compiled}. */
	private static Path classes;

	/**
	 * Whether nested runs put {@link #shadowSettings} first on their class path.
	 */
	private static boolean shadowed;

	/** The project's test resources, holding the bundles and registrations. */
	private static Path resources;

	/**
	 * The include value the generator wrote into {@code junit-platform.properties}.
	 */
	private static String include;

	/**
	 * Generates the precompile output for a policy with a 100 ms strict timeout,
	 * then compiles it with the fixtures.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		Path policy = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policy, policyText("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ"));
		Path testSources = generate(tempDir, policy, "project", "<project/>");
		writeHiddenVisibilityFixture(testSources);
		resources = testSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		compiled = Files.createDirectories(tempDir.resolve("compiled"));
		compile(compiled, List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
				testSources.resolve("com/example/ares/api/localization"), writeFixtures(tempDir.resolve("fixtures"))),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
		Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
				.resolve("GeneratedTestBehaviorSettings.java");
		Files.writeString(shadowSource,
				Files.readString(
						testSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"))
						.replace("\"MILLISECONDS\"", "\"HOURS\""));
		compile(shadowSettings, List.of(), shadowSource);
		classes = compiled;
	}

	/** Writes a test-only generated visibility hook for the timeout contract. */
	private static void writeHiddenVisibilityFixture(Path testSources) throws IOException {
		Path folder = Files.createDirectories(testSources.resolve("de/tum/cit/ase/ares/generated"));
		Files.writeString(folder.resolve("GeneratedHiddenTests.java"),
				"""
						package de.tum.cit.ase.ares.generated;

						public final class GeneratedHiddenTests implements org.junit.jupiter.api.extension.InvocationInterceptor {
							public static boolean isHiddenContext(org.junit.jupiter.api.extension.ExtensionContext context) {
								for (var current = context; current != null; current = current.getParent().orElse(null)) {
									if (current.getTestClass().isPresent()) {
										return current.getTestClass().orElseThrow().getSimpleName().startsWith("HiddenTimeout");
									}
								}
								return false;
							}
							@Override public void interceptTestMethod(Invocation<Void> invocation,
									org.junit.jupiter.api.extension.ReflectiveInvocationContext<java.lang.reflect.Method> method,
									org.junit.jupiter.api.extension.ExtensionContext context) throws Throwable {
								try { invocation.proceed(); } catch (Throwable failure) { throw new HiddenTestFailure(); }
							}
							public static final class HiddenTestFailure extends AssertionError {
								public HiddenTestFailure() { setStackTrace(new StackTraceElement[0]); }
								@Override public String toString() { return ""; }
							}
						}
						""");
	}

	/**
	 * Runs the real generator on a fresh project.
	 *
	 * @param base       the folder to create the project in.
	 * @param policy     the policy file.
	 * @param folder     the project's folder name.
	 * @param pomContent the project's {@code pom.xml}.
	 * @return the project's test source root
	 * @throws IOException if the project cannot be created
	 */
	static Path generate(Path base, Path policy, String folder, String pomContent) throws IOException {
		Path project = Files.createDirectory(base.resolve(folder));
		Files.writeString(project.resolve("pom.xml"), pomContent);
		Files.createDirectories(project.resolve("src/main/java"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("target/classes"));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		return testSources;
	}

	/**
	 * A policy for the {@code com.example} exercise with a 100 ms timeout and a
	 * one-second grace period.
	 *
	 * @param configuration the programming-language configuration to name.
	 * @return the policy text
	 */
	static String policyText(String configuration) {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: %s
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
				    regardingStrictTimeouts:
				      theTimeoutIs: 100
				      theTimeUnitIs: MILLISECONDS
				      theTerminationGraceIs: 1
				      theTerminationGraceUnitIs: SECONDS
				""".formatted(configuration);
	}

	/**
	 * Every JUnit invocation kind the interceptor times stops at the limit with the
	 * localised timeout message.
	 *
	 * @param fixture the fixture whose one timed invocation loops.
	 * @throws Exception if the run cannot be set up
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ConstructorFixture", "BeforeAllFixture", "BeforeEachFixture", "TestMethodFixture",
			"FactoryFixture", "TemplateFixture", "DynamicFixture", "AfterEachFixture", "AfterAllFixture" })
	void everyInvocationKindTimesOut(String fixture) throws Exception {
		EngineExecutionResults results = runJupiter(fixture, Locale.ENGLISH);

		assertThat(failureMessages(results)).isNotEmpty().allMatch(timeoutMessage(Locale.ENGLISH)::equals);
	}

	/**
	 * A hidden timeout keeps failed status without the duration or worker stack.
	 */
	@Test
	void hiddenTimeoutHasNoDiagnosticText() throws Exception {
		EngineExecutionResults results = runJupiter("HiddenTimeoutFixture", Locale.ENGLISH);
		assertThat(results.testEvents().failed().count()).isEqualTo(1);
		assertThat(failureMessages(results)).containsExactly("");
	}

	/** The timeout and hidden interceptors keep both registration orders opaque. */
	@Test
	void hiddenTimeoutHookOrdersStayOpaque() throws Exception {
		assertThat(failureMessages(run("com.example.fixtures.HiddenTimeoutHiddenFirst", false, Locale.ENGLISH)))
				.containsExactly("");
		assertThat(failureMessages(run("com.example.fixtures.HiddenTimeoutStrictFirst", false, Locale.ENGLISH)))
				.containsExactly("");
	}

	/**
	 * The timeout message is localised: in German it is the German bundle's text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theTimeoutMessageIsLocalised() throws Exception {
		assertThat(failureMessages(runJupiter("TestMethodFixture", Locale.GERMAN)))
				.containsExactly(timeoutMessage(Locale.GERMAN));
	}

	/**
	 * A fast test, even a plain {@code @Test} without any Ares annotation, passes.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aFastTestPasses() throws Exception {
		EngineExecutionResults results = runJupiter("FastFixture", Locale.ENGLISH);

		assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
		assertThat(results.allEvents().failed().count()).isZero();
	}

	/** An interrupted caller still halts when its worker ignores cancellation. */
	@Test
	void interruptedCallerAppliesTerminationGrace() throws Exception {
		String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
		String classPath = compiled + File.pathSeparator + System.getProperty("java.class.path");
		Process process = new ProcessBuilder(java, "-cp", classPath, "com.example.fixtures.InterruptedCallerProbe")
				.start();
		try {
			assertThat(process.waitFor(5, TimeUnit.SECONDS)).isTrue();
			assertThat(process.exitValue()).isEqualTo(124);
		} finally {
			process.destroyForcibly();
		}
	}

	/** An interrupted caller keeps its interrupted flag after a worker stops. */
	@Test
	void interruptedCallerKeepsItsInterruptFlag() throws Exception {
		String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
		String classPath = compiled + File.pathSeparator + System.getProperty("java.class.path");
		Process process = new ProcessBuilder(java, "-cp", classPath, "com.example.fixtures.InterruptStatusProbe")
				.start();
		try {
			assertThat(process.waitFor(5, TimeUnit.SECONDS)).isTrue();
			assertThat(process.exitValue()).isZero();
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * The sentinel passes when the interceptor timed it.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelPassesWithTheInterceptor() throws Exception {
		assertThat(run(JUPITER_SENTINEL, true, Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);
	}

	/**
	 * The JUnit sentinel fails when JUnit does not load the interceptor.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelFailsWithoutTheInterceptor() throws Exception {
		EngineExecutionResults results = run(JUPITER_SENTINEL, false, Locale.ENGLISH);

		assertThat(failureMessages(results)).singleElement().asString().contains("not active");
	}

	/**
	 * Replacing the generated settings class with an hour's timeout changes
	 * nothing: the interceptor carries the settings as compile-time constants.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aReplacedSettingsClassChangesNothing() throws Exception {
		shadowed = true;
		try {
			assertThat(failureMessages(runJupiter("TestMethodFixture", Locale.ENGLISH)))
					.containsExactly(timeoutMessage(Locale.ENGLISH));
		} finally {
			shadowed = false;
		}
	}

	/**
	 * The timeout message the copied bundle holds for a locale.
	 *
	 * @param locale the locale.
	 * @return the message for the 100 ms limit
	 */
	private static String timeoutMessage(Locale locale) {
		return String.format(ResourceBundle.getBundle("de.tum.cit.ase.ares.api.localization.messages", locale)
				.getString("timeout.failure_message"), LIMIT);
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
			assertThat(runIn(loader, JUPITER_SENTINEL, true, Locale.ENGLISH).testEvents().succeeded().count())
					.isEqualTo(1);

			EngineExecutionResults withoutExtension = runIn(loader, JUPITER_SENTINEL, false, Locale.ENGLISH);

			assertThat(failureMessages(withoutExtension)).singleElement().asString().contains("not active");
		}
	}

	/**
	 * Runs a fixture class through a nested JUnit Jupiter session.
	 *
	 * @param fixture the fixture's simple name.
	 * @param locale  the locale messages are shown in.
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults runJupiter(String fixture, Locale locale) throws Exception {
		return run("com.example.fixtures." + fixture, true, locale);
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
	 * The class path of a nested run: the generated code, the fixtures and the
	 * resources.
	 *
	 * @return the class path entries
	 * @throws IOException if a path cannot be turned into a URL
	 */
	private static URL[] nestedRunClassPath() throws IOException {
		List<URL> urls = new ArrayList<>();
		if (shadowed) {
			urls.add(shadowSettings.toUri().toURL());
		}
		for (Path root : List.of(classes, compiled, resources)) {
			urls.add(root.toUri().toURL());
		}
		return urls.toArray(URL[]::new);
	}

	/**
	 * The failure messages of every failed test and container of a run.
	 *
	 * @param results the run's results.
	 * @return the messages
	 */
	private static List<String> failureMessages(EngineExecutionResults results) {
		return results.allEvents().failed().stream().map(GeneratedStrictTimeoutTest::messageOf).toList();
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
	 * Writes the fixture tests: one per invocation kind, each looping in exactly
	 * that invocation until interrupted.
	 *
	 * @param root the folder to write them into.
	 * @return the folder
	 * @throws IOException if a file cannot be written
	 */
	private static Path writeFixtures(Path root) throws IOException {
		Path folder = Files.createDirectories(root.resolve("com/example/fixtures"));
		Files.writeString(folder.resolve("Loop.java"), """
				package com.example.fixtures;

				public final class Loop {
					public static int untilInterrupted() {
						while (!Thread.currentThread().isInterrupted()) {
							Thread.onSpinWait();
						}
						return 0;
					}
				}
				""");
		fixture(folder, "ConstructorFixture", "private final int value = Loop.untilInterrupted();",
				"@Test void test() {}");
		fixture(folder, "BeforeAllFixture", "@BeforeAll static void setUp() { Loop.untilInterrupted(); }",
				"@Test void test() {}");
		fixture(folder, "BeforeEachFixture", "@BeforeEach void setUp() { Loop.untilInterrupted(); }",
				"@Test void test() {}");
		fixture(folder, "TestMethodFixture", "", "@Test void loops() { Loop.untilInterrupted(); }");
		fixture(folder, "HiddenTimeoutFixture", "", "@Test void loops() { Loop.untilInterrupted(); }");
		fixture(folder, "HiddenTimeoutHiddenFirst", "",
				"@org.junit.jupiter.api.extension.ExtendWith({ de.tum.cit.ase.ares.generated.GeneratedHiddenTests.class, de.tum.cit.ase.ares.generated.GeneratedStrictTimeout.class }) @Test void loops() { Loop.untilInterrupted(); }");
		fixture(folder, "HiddenTimeoutStrictFirst", "",
				"@org.junit.jupiter.api.extension.ExtendWith({ de.tum.cit.ase.ares.generated.GeneratedStrictTimeout.class, de.tum.cit.ase.ares.generated.GeneratedHiddenTests.class }) @Test void loops() { Loop.untilInterrupted(); }");
		fixture(folder, "FactoryFixture", "",
				"@TestFactory java.util.List<DynamicTest> cases() { Loop.untilInterrupted(); return java.util.List.of(); }");
		fixture(folder, "TemplateFixture", "",
				"@org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(ints = { 1 }) void loops(int value) { Loop.untilInterrupted(); }");
		fixture(folder, "DynamicFixture", "",
				"@TestFactory java.util.List<DynamicTest> cases() { return java.util.List.of(DynamicTest.dynamicTest(\"case\", () -> Loop.untilInterrupted())); }");
		fixture(folder, "AfterEachFixture", "@AfterEach void tearDown() { Loop.untilInterrupted(); }",
				"@Test void test() {}");
		fixture(folder, "AfterAllFixture", "@AfterAll static void tearDown() { Loop.untilInterrupted(); }",
				"@Test void test() {}");
		fixture(folder, "FastFixture", "", "@Test void fast() throws InterruptedException { Thread.sleep(10); }");
		Files.writeString(folder.resolve("InterruptedCallerProbe.java"), """
				package com.example.fixtures;
				import java.time.Duration;
				import java.util.concurrent.CountDownLatch;
				import de.tum.cit.ase.ares.generated.GeneratedStrictTimeout;
				public final class InterruptedCallerProbe {
					public static void main(String[] args) throws Exception {
						CountDownLatch started = new CountDownLatch(1);
						Thread caller = new Thread(() -> {
							try {
								GeneratedStrictTimeout.executeWithTimeout(() -> {
									started.countDown();
									while (true) Thread.onSpinWait();
								}, Duration.ofSeconds(1));
							} catch (Throwable failure) {
								System.exit(20);
							}
						});
						caller.start();
						started.await();
						caller.interrupt();
						caller.join();
						System.exit(21);
					}
				}
				""");
		Files.writeString(folder.resolve("InterruptStatusProbe.java"), """
				package com.example.fixtures;
				import java.time.Duration;
				import de.tum.cit.ase.ares.generated.GeneratedStrictTimeout;
				public final class InterruptStatusProbe {
					public static void main(String[] args) {
						Thread.currentThread().interrupt();
						try {
							GeneratedStrictTimeout.executeWithTimeout(() -> {
								new java.util.concurrent.CountDownLatch(1).await();
								return 1;
							}, Duration.ofSeconds(1));
							System.exit(21);
						} catch (InterruptedException expected) {
							System.exit(Thread.currentThread().isInterrupted() ? 0 : 22);
						} catch (Throwable failure) {
							System.exit(23);
						}
					}
				}
				""");
		return root;
	}

	/**
	 * Writes one JUnit fixture class.
	 *
	 * @param folder the fixtures' package folder.
	 * @param name   the class's simple name.
	 * @param setup  a member to declare first, or an empty string.
	 * @param test   the test member.
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
	static void compile(Path output, List<Path> folders, Path... singleFiles) throws IOException {
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
			throw new IOException("Compiling the generated strict timeout failed");
		}
	}
}
