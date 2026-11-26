package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Checks the guard Precompile copies into an exercise and registers with JUnit:
 * a JUnit run opened in the built exercise starts for clean student code and is
 * refused before any test when a generated check fails, a student class lies in
 * the copied Ares package, the agent is missing, or the student classes were
 * not woven.
 */
class PrecompileGuardTest {

	/** The exercise package; it must not contain the token the copy rewrites. */
	private static final String EXERCISE = "de.tum.cit.aet.guard";

	/** A plain main class, which every exercise needs. */
	private static final String MAIN = """
			package de.tum.cit.aet.guard;

			public class Main {
			    public int twice(int value) {
			        return 2 * value;
			    }
			}
			""";

	/** A student class loading a class by name, which the policy forbids. */
	private static final String REFLECTIVE = """
			package de.tum.cit.aet.guard;

			public class Reflective {
			    public Object load() throws Exception {
			        return Class.forName("de.tum.cit.aet.guard.Main");
			    }
			}
			""";

	/** A plain student class placed in the package of the copied Ares classes. */
	private static final String SHADOW = """
			package de.tum.cit.aet.guard.ares.api;

			public class Shadow {
			    public int one() {
			        return 1;
			    }
			}
			""";

	/** A package the policy may name although no student class lies in it. */
	private static final String FOREIGN = "de.tum.cit.aet.foreign";

	/** What the launcher prints when every generated test ran and passed. */
	private static final Pattern GENERATED_TESTS_PASSED = Pattern.compile("GENERATED_TESTS [1-9]\\d* passed, 0 failed");

	/**
	 * Opens a JUnit run, which calls every registered session listener, and prints
	 * whether it opened or which security error refused it. Once open, it runs the
	 * generated test class named by the first argument and prints how many of its
	 * tests passed and failed. Last come the two ArchUnit settings the generated
	 * checks rely on, as ArchUnit read them.
	 */
	private static final String LAUNCHER = """
			package launcher;

			public final class OpenSession {
			    public static void main(String[] arguments) {
			        try (org.junit.platform.launcher.LauncherSession session =
			                org.junit.platform.launcher.core.LauncherFactory.openSession()) {
			            System.out.println("GUARD_PASSED");
			            org.junit.platform.launcher.listeners.SummaryGeneratingListener summary =
			                    new org.junit.platform.launcher.listeners.SummaryGeneratingListener();
			            session.getLauncher().execute(org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request()
			                    .selectors(org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(arguments[0]))
			                    .build(), summary);
			            System.out.println("GENERATED_TESTS " + summary.getSummary().getTestsSucceededCount() + " passed, "
			                    + summary.getSummary().getTestsFailedCount() + " failed");
			            summary.getSummary().getFailures().forEach(failure ->
			                    System.out.println("GENERATED_FAILURE " + failure.getException()));
			        } catch (SecurityException refused) {
			            System.out.println("GUARD_REFUSED " + refused.getMessage());
			        }
			        com.tngtech.archunit.ArchConfiguration settings = com.tngtech.archunit.ArchConfiguration.get();
			        System.out.println("ARCHUNIT_SETTINGS " + settings.resolveMissingDependenciesFromClassPath() + " "
			                + settings.getPropertyOrDefault("archRule.failOnEmptyShould", "true"));
			    }
			}
			""";

	/**
	 * Clean student code starts in both engines, while a forbidden class lookup
	 * stops the run before any test, naming the failed generated check.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param tempDir  the folder for the made-up exercises
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void aFailedGeneratedCheckStopsTheRunBeforeAnyTest(String analysis, @TempDir Path tempDir) throws Exception {
		String clean = run(analysis, "INSTRUMENTATION", tempDir.resolve("clean"), Map.of("Main", MAIN), true, false);
		String refused = run(analysis, "INSTRUMENTATION", tempDir.resolve("reflective"),
				Map.of("Main", MAIN, "Reflective", REFLECTIVE), true, false);

		assertTrue(clean.contains("GUARD_PASSED"), clean);
		assertTrue(GENERATED_TESTS_PASSED.matcher(clean).find(), clean);
		String prefix = Messages.localized("security.precompile.guard.check.failed", "classLoadingShouldNotBeAccessed",
				"");
		assertTrue(refused.contains("GUARD_REFUSED " + prefix), refused);
		assertTrue(refused.contains("Reflective"), refused);
	}

	/**
	 * A student class in the package the Ares classes were copied into, which the
	 * runtime would trust by name, stops the run in both engines, while the same
	 * code elsewhere does not.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param tempDir  the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void aStudentClassInTheCopiedAresPackageStopsTheRun(String analysis, @TempDir Path tempDir) throws Exception {
		String refused = run(analysis, "INSTRUMENTATION", tempDir.resolve("shadow"),
				Map.of("Main", MAIN, "ares.api.Shadow", SHADOW), true, false);
		String elsewhere = run(analysis, "INSTRUMENTATION", tempDir.resolve("elsewhere"),
				Map.of("Main", MAIN, "helpers.Shadow", SHADOW.replace(".ares.api;", ".helpers;")), true, false);

		assertTrue(refused.contains("GUARD_REFUSED") && refused.contains(EXERCISE + ".ares.api.Shadow")
				&& refused.contains("reserved package " + EXERCISE + ".ares.api"), refused);
		assertTrue(elsewhere.contains("GUARD_PASSED"), elsewhere);
	}

	/**
	 * An exercise whose supervised package has no compiled class yet starts in both
	 * engines, and its generated tests pass when JUnit runs them: there is nothing
	 * they could judge.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param tempDir  the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void anExerciseWithoutStudentClassesStarts(String analysis, @TempDir Path tempDir) throws Exception {
		String output = run(analysis, "INSTRUMENTATION", tempDir, Map.of(), true, false);

		assertTrue(output.contains("GUARD_PASSED"), output);
		assertTrue(GENERATED_TESTS_PASSED.matcher(output).find(), output);
	}

	/**
	 * An exercise generated for one engine, then generated again for the other and
	 * rebuilt from scratch, starts and passes its generated tests: the test class
	 * of the earlier engine is gone, so the guard finds exactly one.
	 *
	 * @param analysis the engine of the second generation
	 * @param tempDir  the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void anExerciseRegeneratedForTheOtherEngineStarts(String analysis, @TempDir Path tempDir) throws Exception {
		String previous = "WALA".equals(analysis) ? "ARCHUNIT" : "WALA";
		String first = run(previous, "INSTRUMENTATION", tempDir, Map.of("Main", MAIN), true, false);
		deleteRecursively(tempDir.resolve("compiled"));
		String regenerated = run(analysis, "INSTRUMENTATION", tempDir, Map.of("Main", MAIN), true, false);

		assertTrue(first.contains("GUARD_PASSED"), first);
		assertTrue(regenerated.contains("GUARD_PASSED"), regenerated);
		assertTrue(GENERATED_TESTS_PASSED.matcher(regenerated).find(), regenerated);
		assertFalse(Files.exists(tempDir.resolve("project/src/test/java")
				.resolve(generatedChecks(previous).replace('.', '/') + ".java")));
	}

	/**
	 * The generated checks run with the ArchUnit settings Ares relies on: missing
	 * classes are not fetched from the class path, and a rule left with no class to
	 * check passes. ArchUnit reads them from the root of the test class path.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param tempDir  the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void theGeneratedChecksRunWithTheAresArchunitSettings(String analysis, @TempDir Path tempDir) throws Exception {
		String output = run(analysis, "INSTRUMENTATION", tempDir, Map.of("Main", MAIN), true, false);

		assertTrue(output.contains("GUARD_PASSED"), output);
		assertTrue(output.contains("ARCHUNIT_SETTINGS false false"), output);
	}

	/**
	 * A policy naming a package that holds none of the compiled student classes
	 * stops the run in both engines, since neither the generated checks nor the
	 * runtime checks would look at the student code. The same code passes under its
	 * own package in the tests above.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param tempDir  the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ARCHUNIT", "WALA" })
	void aPolicyPackageHoldingNoStudentClassStopsTheRun(String analysis, @TempDir Path tempDir) throws Exception {
		String output = run(analysis, "INSTRUMENTATION", tempDir, Map.of("Main", MAIN), true, false, FOREIGN);

		assertTrue(output.contains("GUARD_REFUSED"), output);
		assertTrue(output.contains("\"" + FOREIGN + "\" matches no compiled production class"), output);
	}

	/**
	 * An instrumentation exercise started without the agent runs no test.
	 *
	 * @param tempDir the folder for the made-up exercise
	 * @throws Exception if a step fails to start
	 */
	@Test
	void anInstrumentationRunWithoutTheAgentStopsTheRun(@TempDir Path tempDir) throws Exception {
		String output = run("ARCHUNIT", "INSTRUMENTATION", tempDir, Map.of("Main", MAIN), false, false);

		assertTrue(
				output.contains("GUARD_REFUSED " + Messages.localized("security.instrumentation.agent.not.attached")),
				output);
	}

	/**
	 * An AspectJ exercise whose student classes were compiled without the generated
	 * aspects runs no test, and one whose classes were woven with them starts.
	 *
	 * @param tempDir the folder for the made-up exercises
	 * @throws Exception if a step fails to start
	 */
	@Test
	void anAspectJRunStartsOnlyWithWovenStudentClasses(@TempDir Path tempDir) throws Exception {
		String unwoven = run("ARCHUNIT", "ASPECTJ", tempDir.resolve("unwoven"), Map.of("Main", MAIN), false, false);
		String woven = run("ARCHUNIT", "ASPECTJ", tempDir.resolve("woven"), Map.of("Main", MAIN), false, true);

		assertTrue(
				unwoven.contains("GUARD_REFUSED "
						+ Messages.localized("security.precompile.guard.aspectj.not.woven", 1, EXERCISE + ".Main")),
				unwoven);
		assertTrue(woven.contains("GUARD_PASSED"), woven);
	}

	/**
	 * Builds a Maven-layout exercise, generates its Precompile files, compiles them
	 * and opens a JUnit run inside it.
	 *
	 * @param analysis  the engine
	 * @param aop       the runtime enforcement
	 * @param root      the folder for this exercise
	 * @param sources   the student sources, by class name below the exercise
	 *                  package
	 * @param withAgent whether to start the run with the generated agent
	 * @param weave     whether to compile the student classes with the generated
	 *                  aspects
	 * @return what the run printed
	 * @throws Exception if a step fails
	 */
	private static String run(String analysis, String aop, Path root, Map<String, String> sources, boolean withAgent,
			boolean weave) throws Exception {
		return run(analysis, aop, root, sources, withAgent, weave, EXERCISE);
	}

	/**
	 * Builds the exercise as
	 * {@link #run(String, String, Path, Map, boolean, boolean)} does, with the
	 * student sources in the exercise package and the policy naming the given
	 * package.
	 *
	 * @param analysis      the engine
	 * @param aop           the runtime enforcement
	 * @param root          the folder for this exercise
	 * @param sources       the student sources, by class name below the exercise
	 *                      package
	 * @param withAgent     whether to start the run with the generated agent
	 * @param weave         whether to compile the student classes with the
	 *                      generated aspects
	 * @param policyPackage the package the policy supervises
	 * @return what the run printed
	 * @throws Exception if a step fails
	 */
	private static String run(String analysis, String aop, Path root, Map<String, String> sources, boolean withAgent,
			boolean weave, String policyPackage) throws Exception {
		Path project = Files.createDirectories(root.resolve("project"));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Path mainSources = project.resolve("src/main/java");
		for (Map.Entry<String, String> source : sources.entrySet()) {
			write(mainSources, EXERCISE + "." + source.getKey(), source.getValue());
		}
		Path studentClasses = Files.createDirectories(project.resolve("target/classes"));
		if (!weave && !sources.isEmpty()) {
			compileWithAjc(studentClasses, libraries(), List.of("-sourceroots", mainSources.toString()), root);
		}
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Path policy = Files.writeString(root.resolve("policy.yaml"), policy(analysis, aop, policyPackage));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		write(testSources, "launcher.OpenSession", LAUNCHER);
		Path compiled = Files.createDirectories(root.resolve("compiled"));
		compileWithAjc(compiled, libraries() + File.pathSeparator + studentClasses,
				List.of("-sourceroots", testSources.toString()), root);
		copyTemplates(testSources, compiled);
		if (weave) {
			compileWithAjc(studentClasses, libraries() + File.pathSeparator + compiled,
					List.of("-aspectpath", compiled.toString(), "-sourceroots", mainSources.toString()), root);
		}
		List<String> command = new ArrayList<>(List.of(java(), "-Duser.language=" + Locale.getDefault().getLanguage(),
				"-Duser.country=" + Locale.getDefault().getCountry()));
		if (withAgent) {
			command.add("-javaagent:" + agentJar(testSources, compiled, root.resolve("agent.jar"), policyPackage));
		}
		command.addAll(List.of("--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens",
				"java.base/java.io=ALL-UNNAMED", "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED", "-cp",
				String.join(File.pathSeparator, studentClasses.toString(), compiled.toString(),
						project.resolve("src/test/resources").toString(), libraries()),
				"launcher.OpenSession", generatedChecks(analysis)));
		return start(command, project, root.resolve("run.log"));
	}

	/**
	 * Returns the name of the test class Precompile generates for an engine.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @return the fully qualified class name
	 */
	private static String generatedChecks(String analysis) {
		String engine = "WALA".equals(analysis) ? "Wala" : "Archunit";
		return EXERCISE + ".ares.api.architecture.java." + engine.toLowerCase(Locale.ROOT) + ".Java" + engine
				+ "TestCase";
	}

	/**
	 * Deletes a folder and everything below it, as a clean build does with its
	 * output.
	 *
	 * @param folder the folder
	 * @throws IOException if a file cannot be deleted
	 */
	private static void deleteRecursively(Path folder) throws IOException {
		List<Path> contents;
		try (Stream<Path> files = Files.walk(folder)) {
			contents = files.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList());
		}
		for (Path file : contents) {
			Files.delete(file);
		}
	}

	/**
	 * Returns the policy of the made-up exercise: one readable file, so the file
	 * domain is left to the runtime checks, and nothing else.
	 *
	 * @param analysis      the engine
	 * @param aop           the runtime enforcement
	 * @param policyPackage the package the policy supervises
	 * @return the policy text
	 */
	private static String policy(String analysis, String aop, String policyPackage) {
		return String.join("\n", "thisPolicyFileCompliesToThePolicyVersion: 1", "regardingTheSupervisedCode:",
				"  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_" + analysis + "_AND_" + aop,
				"  theSupervisedCodeUsesTheFollowingPackage: \"" + policyPackage + "\"",
				"  theMainClassInsideThisPackageIs: \"Main\"", "  theFollowingClassesAreTestClasses: [ ]",
				"  theFollowingResourceAccessesArePermitted:", "    regardingFileSystemInteractions:",
				"      - readAllFiles: true", "        overwriteAllFiles: false", "        createAllFiles: false",
				"        executeAllFiles: false", "        deleteAllFiles: false",
				"        onThisPathAndAllPathsBelow: \"data.txt\"", "    regardingNetworkConnections: [ ]",
				"    regardingCommandExecutions: [ ]", "    regardingThreadCreations: [ ]",
				"    regardingPackageImports: [ ]", "    regardingTimeouts: [ ]", "");
	}

	/**
	 * Compiles with the AspectJ compiler, which also compiles plain Java, and fails
	 * when it reports an error.
	 *
	 * @param output    the folder the classes are written to
	 * @param classPath the class path
	 * @param arguments the further compiler arguments
	 * @param root      the folder the log is written to
	 * @throws Exception if the compiler cannot be started
	 */
	private static void compileWithAjc(Path output, String classPath, List<String> arguments, Path root)
			throws Exception {
		List<String> command = new ArrayList<>(List.of(java(), "-cp", JceGeneratedExerciseTest.compiler().toString(),
				"org.aspectj.tools.ajc.Main", "-17", "-nowarn", "-d", output.toString(), "-classpath", classPath));
		command.addAll(arguments);
		Path log = root.resolve("ajc-" + output.getFileName() + ".log");
		String result = start(command, root, log);
		assertTrue(result.startsWith("0\n"), result);
	}

	/**
	 * Copies the generated method lists next to the compiled classes, where the
	 * generated checks look for them and where an exercise build puts them too.
	 *
	 * @param testSources the generated test sources
	 * @param compiled    the compiled test classes
	 * @throws IOException if a file cannot be copied
	 */
	private static void copyTemplates(Path testSources, Path compiled) throws IOException {
		List<Path> templates;
		try (Stream<Path> files = Files.walk(testSources)) {
			templates = files.filter(file -> file.toString().endsWith(".txt")).collect(Collectors.toList());
		}
		for (Path template : templates) {
			Path copy = compiled.resolve(testSources.relativize(template).toString());
			Files.createDirectories(copy.getParent());
			Files.copy(template, copy);
		}
	}

	/**
	 * Packs the compiled classes with the manifest Precompile wrote into the agent
	 * the exercise starts with.
	 *
	 * @param testSources   the generated test sources
	 * @param compiled      the compiled test classes
	 * @param destination   the agent file
	 * @param policyPackage the package the policy supervises, below which the
	 *                      manifest was written
	 * @return the agent file
	 * @throws IOException if the agent cannot be written
	 */
	private static Path agentJar(Path testSources, Path compiled, Path destination, String policyPackage)
			throws IOException {
		Manifest manifest;
		try (var input = Files
				.newInputStream(testSources.resolve(policyPackage.replace('.', '/') + "/META-INF/MANIFEST.MF"))) {
			manifest = new Manifest(input);
		}
		try (var output = new JarOutputStream(Files.newOutputStream(destination), manifest);
				Stream<Path> files = Files.walk(compiled)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				output.putNextEntry(new JarEntry(compiled.relativize(file).toString().replace('\\', '/')));
				Files.copy(file, output);
				output.closeEntry();
			}
		}
		return destination;
	}

	/**
	 * Writes a Java source file below a source root.
	 *
	 * @param sourceRoot the source root
	 * @param className  the fully qualified class name
	 * @param source     the source text
	 * @throws IOException if the file cannot be written
	 */
	private static void write(Path sourceRoot, String className, String source) throws IOException {
		Path file = sourceRoot.resolve(className.replace('.', '/') + ".java");
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
	}

	/**
	 * Returns the library archives of this run's class path, without the class
	 * folders of Ares itself, so the exercise sees only its copied classes.
	 *
	 * @return the archives, joined as a class path
	 */
	private static String libraries() {
		return Arrays.stream(JceGeneratedExerciseTest.classpath().split(File.pathSeparator))
				.filter(entry -> entry.endsWith(".jar")).collect(Collectors.joining(File.pathSeparator));
	}

	/**
	 * Returns the Java launcher of this run.
	 *
	 * @return the path of the launcher
	 */
	private static String java() {
		return Path.of(System.getProperty("java.home"), "bin", "java").toString();
	}

	/**
	 * Runs a command in a folder and returns its exit status and output.
	 *
	 * @param command   the command
	 * @param directory the working folder
	 * @param log       the file the output is written to
	 * @return the exit status, a line break and the output
	 * @throws Exception if the command cannot be started or times out
	 */
	private static String start(List<String> command, Path directory, Path log) throws Exception {
		Process child = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
				.redirectOutput(log.toFile()).start();
		try {
			assertTrue(child.waitFor(5, TimeUnit.MINUTES), () -> "timed out: " + log);
			return child.exitValue() + "\n" + Files.readString(log);
		} finally {
			child.destroyForcibly();
		}
	}
}
