package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Starts real JVMs with and without a source-root report, so that what is
 * tested is the JVM's own start-up values rather than a map handed in. The
 * project's build file claims the main folder as a test folder; only the report
 * may decide.
 */
class GradleReportedSourceRootsForkedTest {

	/** The export Ares needs to read the start-up values. */
	private static final String EXPORT = "java.base/jdk.internal.misc=ALL-UNNAMED";

	/** The project the probe reads. */
	@TempDir
	Path temporaryDirectory;

	/** The canonical project root. */
	private Path root;

	/**
	 * Writes a project whose build file names {@code src/main/java} a test folder,
	 * whose real test class lies in {@code checks}, and whose production class
	 * carries a test annotation it should not be exempted for, compiled into
	 * {@code out}, where only the report says the classes are.
	 *
	 * @throws IOException if the project cannot be written
	 */
	@BeforeEach
	void writeTheProject() throws IOException {
		root = BuildToolConfiguration.canonicalise(temporaryDirectory);
		Files.writeString(root.resolve("build.gradle"),
				"plugins { id 'java' }\nsourceSets.test.java.srcDir 'src/main/java'\n");
		Files.createDirectories(root.resolve("checks/p"));
		Files.writeString(root.resolve("checks/p/CheckTest.java"),
				"package p;\nimport org.junit.jupiter.api.Test;\nclass CheckTest { @Test void works() {} }\n");
		Files.createDirectories(root.resolve("src/main/java/p"));
		Files.writeString(root.resolve("src/main/java/p/Decoy.java"),
				"package p;\nimport org.junit.jupiter.api.Test;\npublic class Decoy { @Test public void x() {} }\n");
		compile(root.resolve("src/main/java/p/Decoy.java"), root.resolve("out/main"));
		compile(root.resolve("checks/p/CheckTest.java"), root.resolve("out/test"));
	}

	/**
	 * Compiles one source file into a folder with the test class path, as Gradle
	 * would before the tests run.
	 *
	 * @param source the source file
	 * @param output the output folder
	 * @throws IOException if the folder cannot be created
	 */
	private static void compile(Path source, Path output) throws IOException {
		Files.createDirectories(output);
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", output.toString(), "-cp",
				System.getProperty("java.class.path"), source.toString());
		assertEquals(0, status, "the fixture does not compile: " + source);
	}

	/**
	 * A value as the snippet writes it.
	 *
	 * @param value the plain value
	 * @return the percent-encoded value
	 */
	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	/**
	 * The JVM arguments of a valid report for this project.
	 *
	 * @return the arguments
	 */
	private List<String> report() {
		return List.of("-Dares.gradle.sourceRoots.version=1", "-Dares.gradle.projectDir=" + encode(root.toString()),
				"-Dares.gradle.testSet=test",
				"-Dares.gradle.testSourceRoots=" + encode(root.resolve("checks").toString()),
				"-Dares.gradle.testOutputRoot=" + encode(root.resolve("out/test").toString()),
				"-Dares.gradle.mainSourceRoots=" + encode(root.resolve("src/main/java").toString()),
				"-Dares.gradle.mainOutputRoot=" + encode(root.resolve("out/main").toString()));
	}

	/**
	 * Runs the probe in a new JVM.
	 *
	 * @param export     whether the JVM exports what Ares needs to read its values
	 * @param properties the report, or none
	 * @param mode       {@code tamper} or {@code plain}
	 * @return the probe's output
	 * @throws Exception if the JVM cannot be run or times out
	 */
	private String probe(boolean export, List<String> properties, String mode) throws Exception {
		List<String> command = new ArrayList<>();
		command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
		if (export) {
			command.add("--add-exports");
			command.add(EXPORT);
		}
		command.addAll(properties);
		command.addAll(List.of("-cp", System.getProperty("java.class.path"),
				GradleReportedSourceRootsProbe.class.getName(), root.toString(), mode));
		Path log = Files.createTempFile(temporaryDirectory, "probe", ".log");
		Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
		try {
			assertTrue(child.waitFor(2, TimeUnit.MINUTES), "the probe timed out");
			return Files.readString(log);
		} finally {
			child.destroyForcibly();
		}
	}

	/**
	 * With a valid report the reported folders win over the build file: the
	 * director creates the security test cases of a run without a policy, and only
	 * the class in {@code checks} is exempt, not the decoy in the main folder.
	 *
	 * @throws Exception if the JVM cannot be run
	 */
	@Test
	void exemptsOnlyTheTestClassesOfTheReportedFolders() throws Exception {
		String output = probe(true, report(), "plain");

		assertTrue(output.contains("OUTCOME=VALID"), output);
		assertTrue(output.contains("TEST=[checks]"), output);
		assertTrue(output.contains("OUTPUT=" + Path.of("out", "main") + "," + Path.of("out", "test")), output);
		assertTrue(output.contains("EXEMPT=[p.CheckTest]"), output);
		assertTrue(output.contains("DIRECTOR=exempt [p.CheckTest]"), output);
	}

	/**
	 * A problem Gradle reports makes the director refuse a run without a policy, so
	 * no test class is exempt, and the refusal names the problem.
	 *
	 * @throws Exception if the JVM cannot be run
	 */
	@Test
	void refusesARunWhoseReportNamesAProblem() throws Exception {
		List<String> problem = List.of("-Dares.gradle.sourceRoots.version=1",
				"-Dares.gradle.projectDir=" + encode(root.toString()),
				"-Dares.gradle.sourceRoots.problem=" + encode("main keeps classes outside its Java output"));

		String output = probe(true, problem, "plain");

		assertTrue(output.contains("OUTCOME=PROBLEM"), output);
		assertTrue(output.contains("DIRECTOR=refused"), output);
		assertTrue(output.contains("main keeps classes outside its Java output"), output);
	}

	/**
	 * Setting the property at run time, as test code could, changes nothing: Ares
	 * reads the values the JVM started with.
	 *
	 * @throws Exception if the JVM cannot be run
	 */
	@Test
	void ignoresAPropertySetAfterStartUp() throws Exception {
		String output = probe(true, report(), "tamper");

		assertTrue(output.contains("TEST=[checks]"), output);
		assertTrue(output.contains("EXEMPT=[p.CheckTest]"), output);
	}

	/**
	 * Without the export Ares cannot read the start-up values, says so, and does
	 * not take the report.
	 *
	 * @throws Exception if the JVM cannot be run
	 */
	@Test
	void namesUnreadableStartUpValues() throws Exception {
		String output = probe(false, report(), "plain");

		assertTrue(output.contains("OUTCOME=UNREADABLE"), output);
	}

	/**
	 * Without a report the outcome is absent, which a run without a policy refuses.
	 *
	 * @throws Exception if the JVM cannot be run
	 */
	@Test
	void namesAMissingReport() throws Exception {
		String output = probe(true, List.of(), "plain");

		assertTrue(output.contains("OUTCOME=ABSENT"), output);
		assertTrue(output.contains("DIRECTOR=refused"), output);
	}
}
