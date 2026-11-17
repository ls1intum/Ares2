package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

/**
 * Checks that precompile carries the type-annotation part of the import rule
 * into an exercise: the generated import test, run inside the exercise project,
 * rejects a type annotation from a forbidden package and accepts it once the
 * policy allows the package, for both engines.
 */
class PrecompileTypeAnnotationImportTest {

	/**
	 * Package of the made-up exercise. It must not contain {@code de.tum.cit.ase},
	 * which the copy step rewrites.
	 */
	private static final String EXERCISE_PACKAGE = "de.tum.cit.aet.typeuse";

	/**
	 * The package of the forbidden annotation.
	 */
	private static final String FOREIGN = "org.example.subject.typeuse.foreign";

	/**
	 * The student class, which names the forbidden package only through a type
	 * annotation on a field.
	 */
	private static final String STUDENT_SOURCE = """
			package de.tum.cit.aet.typeuse;

			public class Main {
			    private @org.example.subject.typeuse.foreign.ForeignTypeUse String name;
			}
			""";

	/**
	 * A launcher that runs one generated test method by reflection on a new
	 * instance, and exits with status 3 when the method throws.
	 */
	private static final String LAUNCHER_SOURCE = """
			package launcher;

			public final class RunGenerated {
			    public static void main(String[] arguments) throws Exception {
			        Class<?> test = Class.forName(arguments[0]);
			        try {
			            test.getMethod(arguments[1]).invoke(test.getDeclaredConstructor().newInstance());
			        } catch (java.lang.reflect.InvocationTargetException failure) {
			            failure.getCause().printStackTrace(System.out);
			            System.exit(3);
			        }
			        System.out.println("PASSED");
			    }
			}
			""";

	/**
	 * Generates the exercise for one engine, once with the forbidden package left
	 * out of the policy and once with it allowed, and runs the generated
	 * {@code packageImportShouldNotBeAccessed} test inside the exercise project.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param testCase the generated test class
	 * @param tempDir  the temporary directory
	 * @throws Exception if a run fails to start
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,architecture.java.archunit.JavaArchunitTestCase",
			"WALA,architecture.java.wala.JavaWalaTestCase" })
	void generatedImportTestSeesTypeAnnotations(String analysis, String testCase, @TempDir Path tempDir)
			throws Exception {
		String forbidden = runGeneratedImportTest(analysis, testCase, tempDir.resolve("forbidden"), "[ ]");
		String allowed = runGeneratedImportTest(analysis, testCase, tempDir.resolve("allowed"),
				"\n      - importTheFollowingPackage: \"" + FOREIGN + "\"");

		assertTrue(forbidden.startsWith("3\n") && forbidden.contains(FOREIGN), forbidden);
		assertTrue(allowed.startsWith("0\n") && allowed.contains("PASSED"), allowed);
	}

	/**
	 * Builds and generates the exercise, then runs its import test in a JVM started
	 * inside the project, where it finds the student classes.
	 *
	 * @param analysis       the engine
	 * @param testCase       the generated test class
	 * @param root           the folder for this run
	 * @param packageImports the allowed imports in YAML
	 * @return the exit status and the output
	 * @throws Exception if the run fails to start
	 */
	private static String runGeneratedImportTest(String analysis, String testCase, Path root, String packageImports)
			throws Exception {
		Path project = Files.createDirectories(root.resolve("project"));
		Path studentSource = writeSource(project.resolve("src/main/java"), EXERCISE_PACKAGE + ".Main", STUDENT_SOURCE);
		JavacFixtures.compileSources(JavacFixtures.testClasses().toString(),
				Files.createDirectories(project.resolve("target/classes")), List.of(studentSource));
		Path generated = generate(analysis, root, project, packageImports);
		Path compiled = Files.createDirectories(root.resolve("compiled"));
		List<Path> sources = new ArrayList<>(javaSources(generated));
		sources.add(writeSource(root.resolve("launcher"), "launcher.RunGenerated", LAUNCHER_SOURCE));
		String classPath = System.getProperty("java.class.path");
		JavacFixtures.compileSources(classPath, compiled, sources);
		copyResources(generated, compiled.resolve(EXERCISE_PACKAGE.replace('.', '/')).resolve("ares/api"));
		return run(project, compiled + File.pathSeparator + classPath, EXERCISE_PACKAGE + ".ares.api." + testCase,
				root.resolve("run.log"));
	}

	/**
	 * Writes a Java source file below the given source root.
	 *
	 * @param sourceRoot the source root
	 * @param className  the fully qualified class name
	 * @param source     the source text
	 * @return the written file
	 * @throws IOException if the file cannot be written
	 */
	private static Path writeSource(Path sourceRoot, String className, String source) throws IOException {
		Path file = sourceRoot.resolve(className.replace('.', '/') + ".java");
		Files.createDirectories(file.getParent());
		return Files.writeString(file, source);
	}

	/**
	 * Runs the precompile flow for the given engine and policy and returns the
	 * folder the copied Ares sources were written to.
	 *
	 * @param analysis       the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param root           the folder for this run
	 * @param project        the exercise project
	 * @param packageImports the YAML value of {@code regardingPackageImports}
	 * @return the folder of the copied sources
	 * @throws IOException if the project cannot be prepared
	 */
	private static Path generate(String analysis, Path root, Path project, String packageImports) throws IOException {
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Path writeTarget = Files.createDirectories(project.resolve("src/test/java"));
		Path policy = Files.writeString(root.resolve("policy.yaml"),
				String.join("\n", "thisPolicyFileCompliesToThePolicyVersion: 1", "regardingTheSupervisedCode:",
						"  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_" + analysis
								+ "_AND_INSTRUMENTATION",
						"  theSupervisedCodeUsesTheFollowingPackage: \"" + EXERCISE_PACKAGE + "\"",
						"  theMainClassInsideThisPackageIs: \"Main\"", "  theFollowingClassesAreTestClasses: [ ]",
						"  theFollowingResourceAccessesArePermitted:", "    regardingFileSystemInteractions: [ ]",
						"    regardingNetworkConnections: [ ]", "    regardingCommandExecutions: [ ]",
						"    regardingThreadCreations: [ ]", "    regardingPackageImports: " + packageImports,
						"    regardingTimeouts:", "      - timeout: 3000", ""));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(writeTarget);
		return writeTarget.resolve(EXERCISE_PACKAGE.replace('.', '/')).resolve("ares/api");
	}

	/**
	 * Runs the generated import test in a separate JVM inside the project.
	 *
	 * @param project   the exercise project, used as the working directory
	 * @param classPath the class path of the run
	 * @param testClass the generated test class
	 * @param log       the file the output is written to
	 * @return the exit status, a line break, and the output of the run
	 * @throws Exception if the run cannot be started or times out
	 */
	private static String run(Path project, String classPath, String testClass, Path log) throws Exception {
		Path java = Path.of(System.getProperty("java.home"), "bin", "java");
		Process child = new ProcessBuilder(java.toString(), "-cp", classPath, "launcher.RunGenerated", testClass,
				"packageImportShouldNotBeAccessed").directory(project.toFile()).redirectErrorStream(true)
						.redirectOutput(log.toFile()).start();
		try {
			assertTrue(child.waitFor(3, TimeUnit.MINUTES), () -> "the generated import test timed out: " + log);
			return child.exitValue() + "\n" + Files.readString(log);
		} finally {
			child.destroyForcibly();
		}
	}

	/**
	 * Lists the copied Java sources the architecture rules need: the architecture
	 * package and the helpers it was copied with.
	 *
	 * @param generated the folder of the copied sources
	 * @return the source files
	 * @throws IOException if the folder cannot be read
	 */
	private static List<Path> javaSources(Path generated) throws IOException {
		try (Stream<Path> files = Stream.of("architecture", "localization", "policy", "util").map(generated::resolve)
				.filter(Files::isDirectory).flatMap(PrecompileTypeAnnotationImportTest::walk)) {
			return files.filter(file -> file.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}

	/**
	 * Copies the generated files that are not Java sources, such as the method
	 * lists the rules load, next to the compiled classes, where an exercise build
	 * puts them too.
	 *
	 * @param generated the folder of the copied sources
	 * @param target    the matching folder below the compiled classes
	 * @throws IOException if a file cannot be copied
	 */
	private static void copyResources(Path generated, Path target) throws IOException {
		List<Path> resources;
		try (Stream<Path> files = Files.walk(generated)) {
			resources = files.filter(Files::isRegularFile).filter(file -> !file.toString().endsWith(".java"))
					.collect(Collectors.toList());
		}
		for (Path resource : resources) {
			Path copy = target.resolve(generated.relativize(resource).toString());
			Files.createDirectories(copy.getParent());
			Files.copy(resource, copy);
		}
	}

	/**
	 * Walks a folder, turning the checked exception into an unchecked one so the
	 * walk fits into a stream.
	 *
	 * @param folder the folder
	 * @return the files below it
	 */
	private static Stream<Path> walk(Path folder) {
		try {
			return Files.walk(folder);
		} catch (IOException unreadable) {
			throw new IllegalStateException(unreadable);
		}
	}
}
