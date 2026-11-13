package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
		Files.writeString(policy, """
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
				      theTimeoutIs: "5s"
				    regardingTheOutputOfTheTestCases:
				      theMaximumNumberOfOutputCharactersIs: 100000
				    regardingTheFailureFeedbackForTheTestCases:
				      theSecurityViolationFeedbackLevelIs: MINIMIZED
				    regardingTheVisibilityAndScheduleOfTheTestCases:
				      theDeadlineIs: "2000-01-01 00:00 UTC"
				      testsNotListedHereAreHidden: false
				      theFollowingTestsAreHidden: ["com.example.fixtures.HiddenFixture"]
				""");
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Files.createDirectories(project.resolve("target/classes"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Path fixtures = Files.createDirectories(testSources.resolve("com/example/fixtures"));
		fixture(fixtures, "PublicFixture", "@Test void fails() { throw new IllegalStateException(\"visible\"); }");
		fixture(fixtures, "HiddenFixture", "@Test void fails() { System.out.print(\"SECRET_ALL_HOOKS\");"
				+ " throw new IllegalStateException(\"SECRET_ALL_HOOKS\"); }");
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
		try (URLClassLoader loader = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiled, resources)),
				Thread.currentThread().getContextClassLoader())) {
			return GeneratedCodeRuns.runIn(loader, className, Locale.ENGLISH,
					Map.of(GeneratedCodeRuns.AUTODETECTION, "true", GeneratedCodeRuns.AUTODETECTION_INCLUDE, include));
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
