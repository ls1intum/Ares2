package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;
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
	void theShellParserAcceptsTheDecimalSecondsTheGeneratorWrites() throws Exception {
		// The generator and the parser are two halves of one contract, and nothing
		// exercised them together: the generator emits canonical decimal seconds while
		// the parser matched integers only, so every generated timeout fell through
		// unmatched, PARSED_TIMEOUT stayed empty and the timeout layer ran with no
		// limit at all. Feeding the real generated configuration into the real parser
		// is what closes that gap.
		String generated = JavaPhobosTestCase.builder().javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.TIMEOUT)
				.resourceAccessSupplier(() -> List.of(new ResourceLimitsPermission(1234))).build()
				.writePhobosTestCase();

		assertEquals("1.234", parsedTimeoutOf("generated.cfg", generated));
	}

	@Test
	void subSecondAndWholeSecondTimeoutsSurviveTheParser() throws Exception {
		assertEquals("0.500", parsedTimeoutOf("sub-second.cfg", "[limits]\ntimeout=0.500\n"));
		assertEquals("10.000", parsedTimeoutOf("whole-second.cfg", "[limits]\ntimeout=10.000\n"));
		// The legacy integer form stays readable, so a configuration written before the
		// unit fix keeps its meaning instead of becoming a policy error.
		assertEquals("10", parsedTimeoutOf("legacy-integer.cfg", "[limits]\ntimeout=10\n"));
	}

	@Test
	void aNumericZeroDisablesTheTimeoutInEitherNotation() throws Exception {
		assertEquals("", parsedTimeoutOf("zero-decimal.cfg", "[limits]\ntimeout=0.000\n"));
		assertEquals("", parsedTimeoutOf("zero-integer.cfg", "[limits]\ntimeout=0\n"));
	}

	@Test
	void theDedicatedTimeoutSectionIsReadInBothNotations() throws Exception {
		assertEquals("2.500", parsedTimeoutOf("section-decimal.cfg", "[timeout]\n2.500\n"));
		assertEquals("7", parsedTimeoutOf("section-integer.cfg", "[timeout]\ntimeout=7\n"));
	}

	@Test
	void anUnreadableTimeoutFailsClosedInsteadOfDroppingTheLimit() throws Exception {
		// Both notations must fail loudly. Silently ignoring an unreadable value is
		// what let the unit mismatch run without any limit in the first place, so a
		// guard that covered only the assignment form would leave the same hole open
		// one line further down.
		assertPolicyError("unreadable-assignment.cfg", "[limits]\ntimeout=abc\n");
		assertPolicyError("unreadable-suffix.cfg", "[limits]\ntimeout=5s\n");
		assertPolicyError("unreadable-bare.cfg", "[timeout]\n5s\n");
	}

	@Test
	void anUnknownLimitsKeyStaysTolerated() throws Exception {
		// [limits] may grow further keys, so only the timeout itself is policed there.
		assertEquals("4.000", parsedTimeoutOf("other-key.cfg", "[limits]\nmemory=512\ntimeout=4.000\n"));
	}

	private void assertPolicyError(String fileName, String configurationContents) throws Exception {
		Path config = temporaryDirectory.resolve(fileName);
		Files.writeString(config, configurationContents);

		ProcessResult result = run(sourceCommonAndParse(config));

		assertEquals(11, result.exitCode(), result.output());
		assertTrue(result.output().contains("PHB-EPOLICY"), result.output());
	}

	/**
	 * Runs the shipped parser over a configuration and returns what it resolved the
	 * timeout to, the empty string meaning "no timeout".
	 */
	private String parsedTimeoutOf(String fileName, String configurationContents) throws Exception {
		Path config = temporaryDirectory.resolve(fileName);
		Files.writeString(config, configurationContents);

		ProcessResult result = run(sourceCommonAndParse(config) + "; printf '%s' \"${PARSED_TIMEOUT}\"");

		assertEquals(0, result.exitCode(), result.output());
		return result.output();
	}

	private String sourceCommonAndParse(Path config) {
		return "source '" + TEMPLATES.resolve("phobos-common.sh") + "'; INI_TMP_DIRS=''; parse_cfg_policy '" + config
				+ "'";
	}

	private ProcessResult run(String script) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", "-c", script).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
