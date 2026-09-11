package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
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
		ProcessResult missingBase = run("/bin/bash '" + shellRoot.resolve("phobos.sh") + "' -- true");
		assertEquals(13, missingBase.exitCode());
		assertTrue(missingBase.output().contains("PHB-EBASE"));

		Path specification = Files.createDirectory(temporaryDirectory.resolve("spec"));
		for (String file : new String[] { "ro.paths", "rw.paths", "hide.paths", "tail.flags" }) {
			Files.createFile(specification.resolve(file));
		}
		ProcessResult missingRuntime = run("BWRAP_BIN='ares-definitely-missing-bwrap' /bin/bash '"
				+ TEMPLATES.resolve("phobos-filesystem.sh") + "' '" + specification + "' -- true");
		assertEquals(15, missingRuntime.exitCode());
		assertTrue(missingRuntime.output().contains("PHB-ERUNTIME"));
	}

	/**
	 * Establishes what {@code JavaPhobosTestCase} refuses a bracketed path for: a
	 * line in square brackets opens a section, so a read permission written as
	 * {@code [write]} makes the {@code /tmp/injected} below it writable.
	 */
	@Test
	void readsALineInSquareBracketsAsASectionHeaderRatherThanAsAPath() throws Exception {
		Path config = temporaryDirectory.resolve("header.cfg");
		Files.writeString(config, "[readonly]\n[write]\n/tmp/injected\n");
		ProcessResult result = run(parses(config));
		assertTrue(result.output().contains("--rw\n/tmp/injected"));
	}

	/**
	 * Holds the reader to the blank characters {@code JavaPhobosTestCase} models,
	 * which are the six of the C locale. Some other locales count the em space as
	 * blank too, and under one of those the unpinned trim would cut it off and the
	 * line would open the write section. The prefix proves the host has such a
	 * locale, so the test aborts on a host that cannot show it.
	 */
	@Test
	void trimsTheBlanksOfTheCLocaleWhateverLocaleTheHostAsks() throws Exception {
		Path config = temporaryDirectory.resolve("blank.cfg");
		Files.writeString(config, "[readonly]\n[write]\u2003\n/tmp/injected\n");
		ProcessResult result = run(picksALocaleThatTrimsAnEmSpace() + parses(config));
		Assumptions.assumeTrue(result.output().contains("--probe\nx\n"), "no locale here trims an em space");
		assertFalse(result.output().contains("--rw\n/tmp/injected"));
		assertTrue(result.output().contains("/tmp/injected"));
	}

	/**
	 * Builds a snippet that puts the shell into a locale whose blank class holds
	 * the em space, then prints what that locale's trim makes of one. Locales
	 * disagree about which characters are blank, so the caller has to watch the
	 * trim happen rather than take a locale's name for it.
	 *
	 * @return the snippet, printing {@code x} under the heading {@code --probe}
	 *         when it found such a locale
	 */
	private String picksALocaleThatTrimsAnEmSpace() {
		return "for candidate in $(locale -a | grep -i 'utf-*8'); do "
				+ "if [ \"$(printf 'x\\342\\200\\203' | LC_ALL=\"$candidate\" sed -E 's/[[:space:]]+$//')\" = x ]; "
				+ "then export LC_ALL=\"$candidate\"; break; fi; done; "
				+ "printf '%s\\n' --probe; printf 'x\\342\\200\\203\\n' | sed -E 's/[[:space:]]+$//'; ";
	}

	/**
	 * Builds a snippet that parses one configuration and prints the read-only and
	 * the write paths it produced, each under a heading the assertions look for.
	 *
	 * @param config the configuration file to parse
	 * @return the snippet to run
	 */
	private String parses(Path config) {
		return "source '" + TEMPLATES.resolve("phobos-common.sh") + "'; INI_TMP_DIRS=''; parse_cfg_policy '" + config
				+ "'; printf '%s\\n' --ro; cat \"$PARSED_RO_FILE\"; printf '%s\\n' --rw; cat \"$PARSED_RW_FILE\"";
	}

	/**
	 * Runs a shell snippet and collects what it printed. Every bash this test
	 * names, here and in the snippets it is given, is named by absolute path, so
	 * the environment cannot choose the interpreter. That ties the test to a host
	 * with bash at /bin/bash, which the scripts themselves do not ask of their own
	 * hosts. The utilities they call still come from PATH.
	 */
	private ProcessResult run(String script) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("/bin/bash", "-c", script).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
