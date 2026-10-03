package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Runs the strict timeout a precompile run generates, end to end: the real
 * generator writes it into a project, the generated sources are compiled with
 * fixture tests, and nested JUnit and jqwik runs show every timed invocation
 * stopping at the policy's limit.
 */
class GeneratedStrictTimeoutTest {

	/** The jqwik engine, loaded afresh for each nested jqwik run. */
	private static final String JQWIK_ENGINE = "net.jqwik.engine.JqwikTestEngine";

	/** The generated JUnit interceptor's sentinel test. */
	private static final String JUPITER_SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedStrictTimeoutSentinelTest";

	/** The generated jqwik hook's sentinel test. */
	private static final String JQWIK_SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedJqwikStrictTimeoutSentinelTest";

	/** The policy's timeout, as the timeout message formats it. */
	private static final String LIMIT = "100 ms";

	/** The generated project, shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** The compiled generated code and fixtures. */
	private static Path compiled;

	/** Generated code compiled from a build that does not name jqwik. */
	private static Path withoutJqwikHook;

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
		Path testSources = generate(tempDir, policy, "project", "<project><!-- net.jqwik:jqwik --></project>");
		resources = testSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		compiled = Files.createDirectories(tempDir.resolve("compiled"));
		compile(compiled, List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
				testSources.resolve("com/example/ares/api/localization"), writeFixtures(tempDir.resolve("fixtures"))),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		Path withoutJqwikSources = generate(tempDir, policy, "project-without-jqwik", "<project/>");
		withoutJqwikHook = Files.createDirectories(tempDir.resolve("compiled-without-jqwik"));
		compile(withoutJqwikHook,
				List.of(withoutJqwikSources.resolve("de/tum/cit/ase/ares/generated"),
						withoutJqwikSources.resolve("com/example/ares/api/localization")),
				withoutJqwikSources.resolve("com/example/ares/api/util/LruCache.java"));
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

	/**
	 * A jqwik try that loops stops at the limit.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aJqwikTryTimesOut() throws Exception {
		EngineExecutionResults results = run("jqwik", "com.example.fixtures.JqwikFixture", true, Locale.ENGLISH);

		assertThat(failureMessages(results)).containsExactly(timeoutMessage(Locale.ENGLISH));
	}

	/**
	 * Both sentinels pass when their hooks timed them.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelsPassWithTheirHooks() throws Exception {
		assertThat(run("junit-jupiter", JUPITER_SENTINEL, true, Locale.ENGLISH).testEvents().succeeded().count())
				.isEqualTo(2);
		assertThat(run("jqwik", JQWIK_SENTINEL, true, Locale.ENGLISH).testEvents().succeeded().count()).isEqualTo(1);
	}

	/**
	 * The JUnit sentinel fails when JUnit does not load the interceptor.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelFailsWithoutTheInterceptor() throws Exception {
		EngineExecutionResults results = run("junit-jupiter", JUPITER_SENTINEL, false, Locale.ENGLISH);

		assertThat(failureMessages(results)).singleElement().asString().contains("not active");
	}

	/**
	 * With jqwik on the class path but no jqwik hook generated, the JUnit sentinel
	 * fails, so properties cannot quietly run unbounded.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void theSentinelFailsWhenJqwikIsPresentWithoutItsHook() throws Exception {
		classes = withoutJqwikHook;
		try {
			EngineExecutionResults results = run("junit-jupiter", JUPITER_SENTINEL, true, Locale.ENGLISH);

			assertThat(failureMessages(results)).singleElement().asString().contains("without its jqwik hook");
		} finally {
			classes = compiled;
		}
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
	 * Without jqwik's engine on the class path, the jqwik hook refuses to time a
	 * try, naming the engine, instead of running it without its context.
	 *
	 * @throws Exception if the stand-in loader cannot be set up
	 */
	@Test
	void theJqwikHookNamesAMissingEngine() throws Exception {
		try (URLClassLoader withoutEngine = new JqwikIsolatingClassLoader(nestedRunClassPath(),
				Thread.currentThread().getContextClassLoader(), false)) {
			Method capture = withoutEngine
					.loadClass("de.tum.cit.ase.ares.generated.GeneratedJqwikStrictTimeout$EngineState")
					.getDeclaredMethod("capture");
			capture.setAccessible(true);

			Throwable failure = captureFailure(capture);

			assertThat(failure).isInstanceOf(AssertionError.class).hasMessageContaining("jqwik-engine");
		}
	}

	/**
	 * What calling a static method threw.
	 *
	 * @param method the method.
	 * @return what it threw
	 * @throws IllegalAccessException if it cannot be called
	 */
	private static Throwable captureFailure(Method method) throws IllegalAccessException {
		try {
			method.invoke(null);
			throw new AssertionError("capture succeeded without jqwik's engine");
		} catch (InvocationTargetException thrown) {
			return thrown.getCause();
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
	 * Runs a fixture class through a nested JUnit Jupiter session.
	 *
	 * @param fixture the fixture's simple name.
	 * @param locale  the locale messages are shown in.
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private static EngineExecutionResults runJupiter(String fixture, Locale locale) throws Exception {
		return run("junit-jupiter", "com.example.fixtures." + fixture, true, locale);
	}

	/**
	 * Runs a class through a nested session of one engine, with a fresh class
	 * loader, so no earlier run leaves a hook marked active. jqwik is loaded afresh
	 * too, because it caches the hooks it finds once per loaded engine.
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
		try (URLClassLoader loader = new JqwikIsolatingClassLoader(nestedRunClassPath(), originalLoader, true)) {
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
	 * The class path of a nested run: the generated code, the fixtures, the
	 * resources and this run's jqwik JARs.
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
		jqwikJars().map(GeneratedStrictTimeoutTest::url).forEach(urls::add);
		return urls.toArray(URL[]::new);
	}

	/**
	 * The jqwik JARs on this test run's class path.
	 *
	 * @return their paths
	 */
	private static Stream<Path> jqwikJars() {
		return Stream.of(System.getProperty("java.class.path").split(File.pathSeparator)).map(Path::of)
				.filter(entry -> entry.getFileName().toString().startsWith("jqwik"));
	}

	/**
	 * A path as a URL.
	 *
	 * @param path the path.
	 * @return its URL
	 */
	private static URL url(Path path) {
		try {
			return path.toUri().toURL();
		} catch (IOException malformed) {
			throw new IllegalStateException(malformed);
		}
	}

	/**
	 * Loads jqwik's classes from its own class path first, so each nested run gets
	 * a jqwik that has not yet looked for hooks. Everything else comes from the
	 * outer test run as usual. It can also pretend jqwik's engine is absent.
	 */
	private static final class JqwikIsolatingClassLoader extends URLClassLoader {

		/** Whether jqwik's engine classes may load. */
		private final boolean withEngine;

		/**
		 * Creates the loader.
		 *
		 * @param urls       the nested run's class path.
		 * @param parent     the outer test run's class loader.
		 * @param withEngine whether jqwik's engine classes may load.
		 */
		JqwikIsolatingClassLoader(URL[] urls, ClassLoader parent, boolean withEngine) {
			super(urls, parent);
			this.withEngine = withEngine;
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
			if (!withEngine && name.startsWith("net.jqwik.engine.")) {
				throw new ClassNotFoundException(name);
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
		Files.writeString(folder.resolve("JqwikFixture.java"), """
				package com.example.fixtures;

				import net.jqwik.api.Example;

				class JqwikFixture {
					@Example
					void loops() {
						Loop.untilInterrupted();
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
