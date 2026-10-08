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
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
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
 * Runs the hidden-test hook a precompile run generates, end to end: the real
 * generator writes it into projects whose policies put the deadline in the
 * past, in the future, or ahead of an always-run-before date, some hiding
 * unlisted tests, the output is compiled with fixture tests, and nested JUnit
 * runs show hidden tests held back and their failures hidden on every path
 * {@code @HiddenTest} covers, and public tests left alone.
 */
class GeneratedHiddenTestsTest {

	/** The generated extension's sentinel test. */
	private static final String SENTINEL = "de.tum.cit.ase.ares.generated.GeneratedHiddenTestsSentinelTest";

	/** The entries every policy of this class lists. */
	private static final List<String> HIDDEN = List.of("com.example.fixtures.MethodFixture",
			"com.example.fixtures.TemplateFixture", "com.example.fixtures.FactoryFixture",
			"com.example.fixtures.LifecycleFixture", "com.example.fixtures.ConstructorFixture",
			"com.example.fixtures.ForgedFixture", "com.example.fixtures.Outer", "com.example.fixtures.Nesting.Inner",
			"com.example.fixtures.Methods#listed", "com.example.fixtures.Shown",
			"com.example.fixtures.Inherits#glides");

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
		active = compileProject("active",
				generate("active", policyText("2200-01-01 00:00 UTC", "2199-01-01 00:00 UTC", false, HIDDEN, PUBLIC)));
		Path visibilitySources = generate("visibility",
				policyText("2200-01-01 00:00 UTC", null, true, VISIBILITY_HIDDEN, VISIBILITY_PUBLIC));
		visibility = compileProject("visibility", visibilitySources);
		visibilityPast = compileProject("visibility-past", generate("visibility-past",
				policyText("2000-01-01 00:00 UTC", null, true, VISIBILITY_HIDDEN, VISIBILITY_PUBLIC)));
		compileArchitectureStandIns(visibility);
		resources = futureSources.resolveSibling("resources");
		include = Files.readAllLines(resources.resolve("junit-platform.properties")).stream()
				.filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		shadowSettings = Files.createDirectories(tempDir.resolve("shadow"));
		Path shadowSource = Files.createDirectories(tempDir.resolve("shadow-source/de/tum/cit/ase/ares/generated"))
				.resolve("GeneratedTestBehaviorSettings.java");
		String settings = Files
				.readString(futureSources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"));
		Files.writeString(shadowSource, settings.replaceAll("THE_DEADLINE_IS = -?\\d+L;", "THE_DEADLINE_IS = 0L;")
				.replaceAll("THE_FOLLOWING_TESTS_ARE_HIDDEN = \"[^\"]*\";", "THE_FOLLOWING_TESTS_ARE_HIDDEN = \"\";"));
		compile(shadowSettings, List.of(), shadowSource);
		shadowVisibility = Files.createDirectories(tempDir.resolve("shadow-visibility"));
		Path shadowVisibilitySource = Files
				.createDirectories(tempDir.resolve("shadow-visibility-source/de/tum/cit/ase/ares/generated"))
				.resolve("GeneratedTestBehaviorSettings.java");
		String visibilitySettings = Files.readString(
				visibilitySources.resolve("de/tum/cit/ase/ares/generated/GeneratedTestBehaviorSettings.java"));
		Files.writeString(shadowVisibilitySource,
				visibilitySettings.replace("UNLISTED_TESTS_ARE_HIDDEN = true;", "UNLISTED_TESTS_ARE_HIDDEN = false;"));
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
	 * Every invocation of a listed test template is held back.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aListedTemplateIsHeldBack() throws Exception {
		assertThat(failureMessages(run(future, "TemplateFixture", Locale.ENGLISH))).hasSize(2)
				.allMatch(beforeDeadline(Locale.ENGLISH)::equals);
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
	 * After the deadline a listed test runs, and its failures, an aborted test
	 * included, are hidden behind a fixed text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void afterTheDeadlineFailuresAreHidden() throws Exception {
		EngineExecutionResults results = run(past, "MethodFixture", Locale.ENGLISH);

		assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
		assertThat(results.testEvents().aborted().count()).isZero();
		assertThat(failureMessages(results)).hasSize(2).allMatch(hiddenFailure(Locale.ENGLISH)::equals);
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
				.allMatch(hiddenFailure(Locale.ENGLISH)::equals);
		assertThat(failureMessages(run(past, "FactoryFixture", Locale.ENGLISH)))
				.containsExactly(hiddenFailure(Locale.ENGLISH));
	}

	/**
	 * A student error that imitates an Ares message is hidden all the same.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aForgedAresMessageIsHidden() throws Exception {
		assertThat(failureMessages(run(past, "ForgedFixture", Locale.ENGLISH)))
				.containsExactly(hiddenFailure(Locale.ENGLISH));
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
	 * The constructor and lifecycle methods of a listed class are neither held back
	 * nor hidden, as in postcompile.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void lifecycleMethodsAndConstructorsAreLeftAlone() throws Exception {
		assertThat(failureMessages(run(future, "LifecycleFixture", Locale.ENGLISH))).containsExactly("secret setup");
		assertThat(failureMessages(run(past, "LifecycleFixture", Locale.ENGLISH))).containsExactly("secret setup");
		assertThat(failureMessages(run(past, "ConstructorFixture", Locale.ENGLISH)))
				.containsExactly("secret constructor");
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
			assertThat(runIn(loader, SENTINEL, Locale.ENGLISH, Map.of()).testEvents().succeeded().count()).isEqualTo(1);

			EngineExecutionResults withoutExtension = runIn(loader, SENTINEL, Locale.ENGLISH,
					Map.of("junit.jupiter.extensions.autodetection.enabled", "false"));

			assertThat(failureMessages(withoutExtension)).singleElement().asString().contains("not active");
		}
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
		assertThat(failureMessages(run(visibility, "TemplateFixture", Locale.ENGLISH))).hasSize(2)
				.allMatch(beforeDeadline(Locale.ENGLISH)::equals);
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
		assertThat(failureMessages(run(visibilityPast, "MethodFixture", Locale.ENGLISH))).hasSize(2)
				.allMatch(hiddenFailure(Locale.ENGLISH)::equals);
		assertThat(failureMessages(run(visibilityPast, "FactoryFixture", Locale.ENGLISH)))
				.containsExactly(hiddenFailure(Locale.ENGLISH));
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
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			EngineTestKit.Builder builder = EngineTestKit.engine("junit-jupiter")
					.configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
					.configurationParameter("junit.jupiter.extensions.autodetection.include", include);
			parameters.forEach(builder::configurationParameter);
			return builder.selectors(DiscoverySelectors.selectClass(loader.loadClass(className))).execute();
		} finally {
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
	 * The before-deadline message for a locale.
	 *
	 * @param locale the locale.
	 * @return the message
	 */
	private static String beforeDeadline(Locale locale) {
		return bundle(locale).getString("test_guard.hidden_test_before_deadline_message");
	}

	/**
	 * The hidden-failure message for a locale.
	 *
	 * @param locale the locale.
	 * @return the message
	 */
	private static String hiddenFailure(Locale locale) {
		return bundle(locale).getString("test_guard.hidden_test_failed");
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
		return results.allEvents().failed().stream().map(GeneratedHiddenTestsTest::messageOf).toList();
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
	 * @param deadline               the deadline.
	 * @param alwaysRunBefore        the always-run-before date, or null.
	 * @param unlistedTestsAreHidden whether unlisted tests are hidden.
	 * @param hidden                 the hidden entries.
	 * @param shown                  the public entries.
	 * @return the policy text
	 */
	private static String policyText(String deadline, String alwaysRunBefore, boolean unlistedTestsAreHidden,
			List<String> hidden, List<String> shown) {
		StringBuilder policy = new StringBuilder("""
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
				    regardingHiddenTests:
				""");
		policy.append("      theDeadlineIs: \"").append(deadline).append("\"\n");
		if (alwaysRunBefore != null) {
			policy.append("      hiddenTestsAlwaysRunBefore: \"").append(alwaysRunBefore).append("\"\n");
		}
		policy.append("      unlistedTestsAreHidden: ").append(unlistedTestsAreHidden).append('\n');
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
		compile(output, List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
				testSources.resolve("com/example/ares/api/localization"), testSources.resolve("com/example/fixtures")),
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
		String secret = "throw new IllegalStateException(\"secret\");";
		fixture(folder, "MethodFixture", "", "@Test void passes() {} @Test void fails() { " + secret
				+ " } @Test void aborts() { Assumptions.abort(\"secret\"); }");
		fixture(folder, "TemplateFixture", "",
				"@org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(ints = { 1, 2 }) void each(int value) { "
						+ secret + " }");
		fixture(folder, "FactoryFixture", "",
				"@TestFactory java.util.List<DynamicTest> cases() { return java.util.List.of(DynamicTest.dynamicTest(\"case\", () -> { "
						+ secret + " })); }");
		fixture(folder, "LifecycleFixture",
				"@BeforeEach void setUp() { throw new IllegalStateException(\"secret setup\"); }",
				"@Test void test() {}");
		fixture(folder, "ConstructorFixture",
				"ConstructorFixture() { throw new IllegalStateException(\"secret constructor\"); }",
				"@Test void test() {}");
		fixture(folder, "ForgedFixture", "",
				"@Test void forges() { throw new SecurityException(\"Ares Security Error (Reason: Student-Code; Stage: Execution): secret\"); }");
		fixture(folder, "UnlistedFixture", "", "@Test void fails() { throw new IllegalStateException(\"visible\"); }");
		fixture(folder, "Outer", "", "@Nested class Inner { @Test void deep() { " + secret + " } }");
		fixture(folder, "Nesting", "@Test void top() {}",
				"@Nested class Inner { @Test void deep() { " + secret + " } }");
		fixture(folder, "Methods", "", "@Test void listed() { " + secret
				+ " } @Test void unlisted() { throw new IllegalStateException(\"visible\"); }");
		fixture(folder, "Shown", "", "@Test void shown() { throw new IllegalStateException(\"visible\"); }"
				+ " @Test void secret() { " + secret + " }");
		fixture(folder, "Layered", "@Test void top() { " + secret + " }",
				"@Nested class Inner { @Test void deep() { throw new IllegalStateException(\"visible\"); } }");
		fixture(folder, "GeneratedHiddenTestsSentinelTest", "",
				"@Test void hiddenTestsAreActive() { throw new IllegalStateException(\"visible\"); }");
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
			throw new IOException("Compiling the generated hidden-test hook failed");
		}
	}
}
