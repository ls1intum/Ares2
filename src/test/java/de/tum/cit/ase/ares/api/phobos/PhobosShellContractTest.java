package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.util.FileTools;

class PhobosShellContractTest {
	private static final Path TEMPLATES = FileTools.resolveFileOnSourceDirectory("templates", "phobos");

	@TempDir
	Path temporaryDirectory;

	@Test
	void rejectsUnknownConfigurationSectionsWithPolicyError() throws Exception {
		Path config = temporaryDirectory.resolve("invalid.cfg");
		Files.writeString(config, "[unknown]\nvalue\n");
		ProcessResult result = run("source '" + TEMPLATES.resolve("phobos-common.sh")
				+ "'; INI_TMP_DIRS=''; parse_cfg_policy '" + config + "'");
		assertEquals(11, result.exitCode());
		assertTrue(result.output().contains("PHB-EPOLICY"));
	}

	@Test
	void missingBaseAndRuntimeDependenciesUseDocumentedFailClosedErrors() throws Exception {
		Path shellRoot = Files.createDirectory(temporaryDirectory.resolve("shell"));
		Files.copy(TEMPLATES.resolve("phobos.sh"), shellRoot.resolve("phobos.sh"));
		Files.copy(TEMPLATES.resolve("phobos-common.sh"), shellRoot.resolve("phobos-common.sh"));
		ProcessResult missingBase = run("bash '" + shellRoot.resolve("phobos.sh") + "' -- true");
		assertEquals(13, missingBase.exitCode());
		assertTrue(missingBase.output().contains("PHB-EBASE"));

		Path specification = Files.createDirectory(temporaryDirectory.resolve("spec"));
		for (String file : new String[] { "ro.paths", "rw.paths", "hide.paths", "tail.flags" }) {
			Files.createFile(specification.resolve(file));
		}
		ProcessResult missingRuntime = run("BWRAP_BIN='ares-definitely-missing-bwrap' bash '"
				+ TEMPLATES.resolve("phobos-filesystem.sh") + "' '" + specification + "' -- true");
		assertEquals(15, missingRuntime.exitCode());
		assertTrue(missingRuntime.output().contains("PHB-ERUNTIME"));
	}

	private ProcessResult run(String script) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", "-c", script).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
