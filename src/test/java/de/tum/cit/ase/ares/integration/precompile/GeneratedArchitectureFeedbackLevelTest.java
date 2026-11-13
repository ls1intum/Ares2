package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

/**
 * Runs the failure reporting a precompile run generates against the security
 * tests Ares generates into the exercise, and checks that their failures follow
 * the policy's feedback level: unchanged at FULL, the fixed text at MINIMIZED,
 * the report plus the policy category at DEBUG. The architecture tests are
 * stand-ins under their exact generated names, because the generated ones need
 * the copied analysis libraries to compile.
 */
class GeneratedArchitectureFeedbackLevelTest {

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

	/** Compiled output for a policy at FULL with privileged-only reporting on. */
	private static Path full;

	/** Compiled output for a policy at MINIMIZED. */
	private static Path minimized;

	/** Compiled output for a policy at DEBUG. */
	private static Path debug;

	/**
	 * Generates and compiles the precompile output for the three policies.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		full = project("full", "FULL", true);
		minimized = project("minimized", "MINIMIZED", false);
		debug = project("debug", "DEBUG", false);
	}

	/**
	 * At FULL, the generated security tests keep their report even with
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
	 * At MINIMIZED, the generated security tests show only that something was
	 * blocked, in both locales, and nothing of the report.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void minimizedShowsOnlyTheFixedText() throws Exception {
		for (String test : List.of(ARCHUNIT_TEST, WALA_TEST)) {
			assertThat(messages(minimized, test, Locale.ENGLISH)).contains(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.")
					.noneMatch(message -> message.contains("/secret/path"));
			assertThat(messages(minimized, test, Locale.GERMAN))
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
					.singleElement().asString().contains("To allow this, add to regardingFileSystemInteractions");
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
		for (Path project : List.of(minimized, debug)) {
			assertThat(messages(project, ARCHUNIT_TEST, Locale.ENGLISH)).contains(CONFIGURATION_ERROR);
		}
	}

	/**
	 * With privileged-only reporting at FULL, a failure that wraps a blocked action
	 * reports the blocked action, not the wrapper's own text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aWrappedViolationIsReportedWithoutItsWrapper() throws Exception {
		assertThat(messages(full, WRAPPED_TEST, Locale.ENGLISH)).containsExactly(VIOLATION);
	}

	/**
	 * A class that only looks like a generated security test is not shortened, so
	 * the level applies to Ares's own tests by their exact names alone.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aLookalikeIsNotTreatedAsASecurityTest() throws Exception {
		assertThat(messages(minimized, LOOKALIKE, Locale.ENGLISH)).containsExactly(REPORT);
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
		files.add(
				writeClass(sources, "com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation", """
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
				    regardingTheFailureFeedbackForTheTestCases:
				      onlyPrivilegedExceptionsAreReported: %s
				      theFailureMessageForOtherExceptionsIs: "%s"
				      theSecurityViolationFeedbackLevelIs: %s
				""".formatted(privilegedOnly, POLICY_MESSAGE, level);
	}
}
