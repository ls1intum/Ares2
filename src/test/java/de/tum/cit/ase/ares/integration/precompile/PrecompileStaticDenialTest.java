package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.ForeignCallBytecode;

/**
 * Checks that the native-code and environment tests Precompile generates refuse
 * the calls below {@code java.lang} that the package-import rule now lets a
 * student import, for both engines. Each generated test checks one rule and,
 * run inside the exercise project, fails beside the student class making its
 * call and passes for plain code, which ties the failure to that class.
 */
class PrecompileStaticDenialTest {

	/** The plain main class every exercise needs. */
	private static final String MAIN_SOURCE = """
			package de.tum.cit.aet.typeuse;

			public class Main {
			    public int twice(int value) {
			        return 2 * value;
			    }
			}
			""";

	/** A student class reading the class path through the management API. */
	private static final String ENVIRONMENT_SOURCE = """
			package de.tum.cit.aet.typeuse;

			public class EnvironmentUser {
			    public String classPath() {
			        return java.lang.management.ManagementFactory.getRuntimeMXBean().getClassPath();
			    }
			}
			""";

	/** The generated native-code test of both engines. */
	private static final String NATIVE_METHOD = "nativeCodeShouldNotBeAccessed";

	/**
	 * Generates the exercise for one engine twice, once with a class reading the
	 * class path through the management API and once with plain code only, and runs
	 * the generated native-code and environment tests in each.
	 *
	 * @param analysis          the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param testCase          the generated test class
	 * @param environmentMethod the engine's name for the generated environment test
	 * @param tempDir           the temporary directory
	 * @throws Exception if a run fails to start
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,architecture.java.archunit.JavaArchunitTestCase,environmentAccessShouldNotBeAllowed",
			"WALA,architecture.java.wala.JavaWalaTestCase,environmentShouldNotBeAccessed" })
	void generatedEnvironmentTestRefusesTheManagementApi(String analysis, String testCase, String environmentMethod,
			@TempDir Path tempDir) throws Exception {
		String exercise = PrecompileTypeAnnotationImportTest.EXERCISE_PACKAGE;
		String forbidden = runExercise(analysis, testCase, tempDir.resolve("forbidden"),
				Map.of(exercise + ".Main", MAIN_SOURCE, exercise + ".EnvironmentUser", ENVIRONMENT_SOURCE), false,
				environmentMethod);
		String plain = runExercise(analysis, testCase, tempDir.resolve("plain"),
				Map.of(exercise + ".Main", MAIN_SOURCE), false, NATIVE_METHOD, environmentMethod);

		assertTrue(forbidden.startsWith("3\n"), forbidden);
		assertRefused(forbidden, environmentMethod, "EnvironmentUser", "ManagementFactory.getRuntimeMXBean");
		assertTrue(plain.startsWith("0\n"), plain);
		assertTrue(lineOf(plain, NATIVE_METHOD).equals(NATIVE_METHOD + " PASSED"), plain);
		assertTrue(lineOf(plain, environmentMethod).equals(environmentMethod + " PASSED"), plain);
	}

	/**
	 * Generates an exercise whose student class widens a memory segment made for an
	 * address, as a chained call, and runs the generated native-code test. The
	 * foreign-memory API exists from Java 22 on; on older JDKs the class it names
	 * is missing and WALA refuses to build the exercise at all.
	 *
	 * @param analysis the engine, {@code ARCHUNIT} or {@code WALA}
	 * @param testCase the generated test class
	 * @param tempDir  the temporary directory
	 * @throws Exception if a run fails to start
	 */
	@ParameterizedTest
	@EnabledForJreRange(min = JRE.JAVA_22)
	@CsvSource({ "ARCHUNIT,architecture.java.archunit.JavaArchunitTestCase",
			"WALA,architecture.java.wala.JavaWalaTestCase" })
	void generatedNativeCodeTestRefusesForeignMemory(String analysis, String testCase, @TempDir Path tempDir)
			throws Exception {
		String output = runExercise(analysis, testCase, tempDir,
				Map.of(PrecompileTypeAnnotationImportTest.EXERCISE_PACKAGE + ".Main", MAIN_SOURCE), true,
				NATIVE_METHOD);

		assertTrue(output.startsWith("3\n"), output);
		assertRefused(output, NATIVE_METHOD, "NativeUser", "MemorySegment.reinterpret");
	}

	/**
	 * Builds an exercise and runs the given generated tests in it.
	 *
	 * @param analysis        the engine
	 * @param testCase        the generated test class
	 * @param root            the folder for this run
	 * @param sources         the student sources, by fully qualified class name
	 * @param withForeignCall whether to add the class making the foreign call
	 * @param methods         the generated test methods to run
	 * @return the exit status and the output
	 * @throws Exception if the run fails to start
	 */
	private static String runExercise(String analysis, String testCase, Path root, Map<String, String> sources,
			boolean withForeignCall, String... methods) throws Exception {
		Path project = Files.createDirectories(root.resolve("project"));
		PrecompileTypeAnnotationImportTest.compileStudentSources(project, sources);
		if (withForeignCall) {
			ForeignCallBytecode.writeChainedReinterpret(project.resolve("target/classes"),
					PrecompileTypeAnnotationImportTest.EXERCISE_PACKAGE + ".NativeUser");
		}
		return PrecompileTypeAnnotationImportTest.runGenerated(analysis, testCase, root, project, "[ ]", methods);
	}

	/**
	 * Asserts that a generated test method, which checks one rule, failed with an
	 * architecture violation naming the student class and the refused call, not
	 * with an error of Ares itself, which is a security exception.
	 *
	 * @param output   the output of the run
	 * @param method   the generated test method
	 * @param offender the simple name of the student class
	 * @param call     the refused call, as the violation names it
	 */
	private static void assertRefused(String output, String method, String offender, String call) {
		String line = lineOf(output, method);
		assertTrue(line.startsWith(method + " FAILED java.lang.AssertionError") && line.contains(offender)
				&& line.contains(call), output);
	}

	/**
	 * Returns the line the launcher printed for a generated test method.
	 *
	 * @param output the output of the run
	 * @param method the generated test method
	 * @return the line, or an empty string if there is none
	 */
	private static String lineOf(String output, String method) {
		return Arrays.stream(output.split("\n")).filter(line -> line.startsWith(method + " ")).findFirst().orElse("");
	}
}
