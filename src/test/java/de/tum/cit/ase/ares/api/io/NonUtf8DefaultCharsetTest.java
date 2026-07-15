package de.tum.cit.ase.ares.api.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class NonUtf8DefaultCharsetTest {

	@Test
	void ioAndStructuralOracleRemainUtf8UnderNonUtf8JvmDefault() throws Exception {
		Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
		Process process = new ProcessBuilder(javaExecutable.toString(), "-Dfile.encoding=ISO-8859-1",
				"-Duser.language=tr", "-Duser.country=TR", "-cp", System.getProperty("java.class.path"),
				NonUtf8DefaultCharsetVerifier.class.getName()).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(0, process.waitFor(), output);
	}
}
