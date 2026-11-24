package de.tum.cit.ase.ares.integration.precompile;

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

	/**
	 * Opens a JUnit run, which calls every registered session listener, and prints
	 * whether it opened or which security error refused it.
	 */
	private static final String LAUNCHER = """
			package launcher;

			public final class OpenSession {
			    public static void main(String[] arguments) {
			        try (org.junit.platform.launcher.LauncherSession session =
			                org.junit.platform.launcher.core.LauncherFactory.openSession()) {
			            System.out.println("GUARD_PASSED");
			        } catch (SecurityException refused) {
			            System.out.println("GUARD_REFUSED " + refused.getMessage());
			        }
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
		Path project = Files.createDirectories(root.resolve("project"));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Path mainSources = project.resolve("src/main/java");
		for (Map.Entry<String, String> source : sources.entrySet()) {
			write(mainSources, EXERCISE + "." + source.getKey(), source.getValue());
		}
		Path studentClasses = Files.createDirectories(project.resolve("target/classes"));
		if (!weave) {
			compileWithAjc(studentClasses, libraries(), List.of("-sourceroots", mainSources.toString()), root);
		}
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Path policy = Files.writeString(root.resolve("policy.yaml"), policy(analysis, aop));
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
			command.add("-javaagent:" + agentJar(testSources, compiled, root.resolve("agent.jar")));
		}
		command.addAll(List
				.of("--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED",
						"--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED", "-cp",
						String.join(File.pathSeparator, studentClasses.toString(), compiled.toString(),
								project.resolve("src/test/resources").toString(), libraries()),
						"launcher.OpenSession"));
		return start(command, project, root.resolve("run.log"));
	}

	/**
	 * Returns the policy of the made-up exercise: one readable file, so the file
	 * domain is left to the runtime checks, and nothing else.
	 *
	 * @param analysis the engine
	 * @param aop      the runtime enforcement
	 * @return the policy text
	 */
	private static String policy(String analysis, String aop) {
		return String.join("\n", "thisPolicyFileCompliesToThePolicyVersion: 1", "regardingTheSupervisedCode:",
				"  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_" + analysis + "_AND_" + aop,
				"  theSupervisedCodeUsesTheFollowingPackage: \"" + EXERCISE + "\"",
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
	 * @param testSources the generated test sources
	 * @param compiled    the compiled test classes
	 * @param destination the agent file
	 * @return the agent file
	 * @throws IOException if the agent cannot be written
	 */
	private static Path agentJar(Path testSources, Path compiled, Path destination) throws IOException {
		Manifest manifest;
		try (var input = Files
				.newInputStream(testSources.resolve(EXERCISE.replace('.', '/') + "/META-INF/MANIFEST.MF"))) {
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
