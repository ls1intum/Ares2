package de.tum.cit.ase.ares.api.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NonUtf8DefaultCharsetTest {

	@Test
	void ioAndStructuralOracleRemainUtf8UnderNonUtf8JvmDefault(@TempDir Path temporaryDirectory) throws Exception {
		Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
		Path outputFile = temporaryDirectory.resolve("verifier-output.txt"); //$NON-NLS-1$
		Process process = new ProcessBuilder(javaExecutable.toString(), "-Dfile.encoding=ISO-8859-1",
				"-Duser.language=tr", "-Duser.country=TR", "-cp", System.getProperty("java.class.path"),
				NonUtf8DefaultCharsetVerifier.class.getName()).redirectErrorStream(true)
						.redirectOutput(outputFile.toFile()).start();
		try {
			if (!process.waitFor(30L, TimeUnit.SECONDS)) {
				terminate(process);
				fail("The non-UTF-8 verifier process did not finish within 30 seconds");
			}
			String output = Files.readString(outputFile, StandardCharsets.UTF_8);
			assertEquals(0, process.exitValue(), output);
		} finally {
			if (process.isAlive()) {
				terminate(process);
			}
		}
	}

	private static void terminate(Process process) throws InterruptedException {
		process.destroy();
		if (!process.waitFor(5L, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			if (!process.waitFor(5L, TimeUnit.SECONDS)) {
				fail("The non-UTF-8 verifier process could not be terminated");
			}
		}
	}
}
