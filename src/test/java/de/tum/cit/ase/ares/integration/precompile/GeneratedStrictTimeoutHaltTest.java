package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proves that the generated strict timeout halts the test process when a timed
 * worker ignores its interruption, as {@code @StrictTimeout} does, in a JVM of
 * its own so the halt cannot take this test run with it.
 */
class GeneratedStrictTimeoutHaltTest {

	/** The exit code of a test process halted because a worker did not stop. */
	private static final int UNRESPONSIVE_EXIT_CODE = 124;

	/** Printed by the probe if the halt returned, which it must never do. */
	private static final String SURVIVED = "ares.probe.survived";

	/**
	 * Generates the strict timeout, compiles a probe whose timed work ignores
	 * interruption, and runs it in a separate JVM.
	 *
	 * @param tempDir where the project is generated and compiled.
	 * @throws Exception if generating, compiling or starting the JVM fails
	 */
	@Test
	void anUnresponsiveWorkerHaltsTheTestProcess(@TempDir Path tempDir) throws Exception {
		Path policy = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policy,
				GeneratedStrictTimeoutTest.policyText("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ")
						.replace("theTerminationGraceIs: 1", "theTerminationGraceIs: 100")
						.replace("theTerminationGraceUnitIs: SECONDS", "theTerminationGraceUnitIs: MILLISECONDS"));
		Path testSources = GeneratedStrictTimeoutTest.generate(tempDir, policy, "project", "<project/>");
		Path compiled = Files.createDirectories(tempDir.resolve("compiled"));
		GeneratedStrictTimeoutTest.compile(compiled,
				List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
						testSources.resolve("com/example/ares/api/localization"), writeProbe(tempDir.resolve("probe"))),
				testSources.resolve("com/example/ares/api/util/LruCache.java"));
		String classPath = String.join(File.pathSeparator, compiled.toString(),
				testSources.resolveSibling("resources").toString(),
				System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
		Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
				classPath, "com.example.probe.HaltProbe").redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(30, TimeUnit.SECONDS)).isTrue();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

			assertThat(process.exitValue()).as(output).isEqualTo(UNRESPONSIVE_EXIT_CODE);
			assertThat(output).doesNotContain(SURVIVED);
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * Writes the probe: it times a loop that ignores interruption.
	 *
	 * @param root the folder to write it into.
	 * @return the folder
	 * @throws IOException if the file cannot be written
	 */
	private static Path writeProbe(Path root) throws IOException {
		Path folder = Files.createDirectories(root.resolve("com/example/probe"));
		Files.writeString(folder.resolve("HaltProbe.java"), """
				package com.example.probe;

				import java.time.Duration;

				import de.tum.cit.ase.ares.generated.GeneratedStrictTimeout;

				public final class HaltProbe {
					public static void main(String[] arguments) throws Throwable {
						try {
							GeneratedStrictTimeout.executeWithTimeout(() -> {
								while (true) {
									Thread.onSpinWait();
								}
							}, Duration.ofMillis(50));
						} finally {
							System.out.println("%s");
						}
					}
				}
				""".formatted(SURVIVED));
		return root;
	}
}
