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

import de.tum.cit.ase.ares.api.util.FileTools;

class PhobosShellContractTest {
	private static final Path TEMPLATES = FileTools.resolveFileOnSourceDirectory("templates", "phobos");

	/**
	 * Where bash is looked for, in order: /bin/bash on Linux and macOS, and the
	 * bash of Git for Windows in its default folder. Windows has no /bin/bash, and
	 * a search of the path could find the Windows Subsystem for Linux launcher,
	 * also named bash, before it.
	 */
	private static final List<Path> BASH_LOCATIONS = List.of(Path.of("/bin/bash"),
			Path.of(System.getenv().getOrDefault("ProgramFiles", "C:\\Program Files"), "Git", "bin", "bash.exe"));

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

	/**
	 * Checks that a script parses, by handing its text to bash on standard input
	 * rather than naming the file, so its path never reaches a shell and nothing in
	 * it runs. Bash is named by absolute path rather than looked up on the search
	 * path; see {@link #BASH_LOCATIONS}.
	 *
	 * @param scriptContent the script text to parse
	 * @return what bash printed, and whether it accepted the script
	 */
	private ProcessResult runBashSyntaxCheck(String scriptContent) throws IOException, InterruptedException {
		Process process = new ProcessBuilder(bash().toString(), "-n").redirectErrorStream(true).start();
		try (OutputStream stdin = process.getOutputStream()) {
			stdin.write(scriptContent.getBytes(StandardCharsets.UTF_8));
		}
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	/**
	 * Finds bash at the first of its fixed locations that holds a program.
	 *
	 * @return the absolute path of bash
	 * @throws AssertionError if none of the locations holds one
	 */
	private static Path bash() {
		return BASH_LOCATIONS.stream().filter(Files::isExecutable).findFirst()
				.orElseThrow(() -> new AssertionError("bash is at none of " + BASH_LOCATIONS));
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
