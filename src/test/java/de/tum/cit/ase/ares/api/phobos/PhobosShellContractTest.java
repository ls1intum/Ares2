package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Test
	void copyToolAssignsTargetDirectoryWithAsciiQuotes() throws Exception {
		Path copyTool = TEMPLATES.resolve("PhobosCopyTool.sh");
		String contents = Files.readString(copyTool, StandardCharsets.UTF_8);

		// The script must not contain Unicode curly quotation marks (U+201C / U+201D).
		assertFalse(contents.indexOf('“') >= 0 || contents.indexOf('”') >= 0,
				"PhobosCopyTool.sh must not contain Unicode curly quotation marks");

		// Parse the assignment purely in Java: the fixture is never executed and its
		// path is never interpolated into a shell command. Exactly one TARGET_DIR
		// assignment must exist so a malformed or duplicate line cannot satisfy the
		// test.
		List<String> assignments = contents.lines().map(String::stripLeading)
				.filter(line -> line.startsWith("TARGET_DIR=")).toList();
		assertEquals(1, assignments.size(), "PhobosCopyTool.sh must contain exactly one TARGET_DIR assignment");

		// The assignment must use ASCII double quotes around exactly /var/tmp/opt/core.
		Matcher matcher = Pattern.compile("^TARGET_DIR=\"(/var/tmp/opt/core)\"$").matcher(assignments.get(0));
		assertTrue(matcher.matches(),
				"TARGET_DIR must be assigned with ASCII double quotes as \"/var/tmp/opt/core\", but was: "
						+ assignments.get(0));
		assertEquals("/var/tmp/opt/core", matcher.group(1));

		// Validate Bash syntax without building a shell command string or passing the
		// fixture path to Bash: feed the already-read script content to `bash -n` via
		// stdin.
		ProcessResult syntax = runBashSyntaxCheck(contents);
		assertEquals(0, syntax.exitCode(), syntax.output());
	}

	private ProcessResult run(String script) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", "-c", script).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private ProcessResult runBashSyntaxCheck(String scriptContent) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", "-n").redirectErrorStream(true).start();
		try (OutputStream stdin = process.getOutputStream()) {
			stdin.write(scriptContent.getBytes(StandardCharsets.UTF_8));
		}
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
