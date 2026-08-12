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

	@Test
	void aTimeoutDeclarationRequiresAnAssignmentAndAValue() throws Exception {
		// Malformed timeout declarations must fail closed instead of resolving to
		// the empty value that represents "no timeout".
		assertPolicyError("bare-key.cfg", "[limits]\ntimeout\n");
		assertPolicyError("colon-without-value.cfg", "[limits]\ntimeout:\n");
		assertPolicyError("colon-delimiter.cfg", "[limits]\ntimeout: 5\n");
		assertPolicyError("spaced-colon-delimiter.cfg", "[limits]\ntimeout : 5\n");
		assertPolicyError("missing-delimiter.cfg", "[limits]\ntimeout 5\n");
		assertPolicyError("missing-delimiter-with-unit.cfg", "[limits]\ntimeout 5s\n");
		assertPolicyError("empty-value.cfg", "[limits]\ntimeout =\n");
		assertPolicyError("whitespace-only-value.cfg", "[limits]\ntimeout =    \n");
		assertPolicyError("non-numeric-value.cfg", "[limits]\ntimeout = invalid\n");
	}

	@Test
	void aKeyThatMerelyStartsWithTimeoutIsNotATimeoutDeclaration() throws Exception {
		// The guard reads "timeout" as a whole key, not as a prefix, so [limits] keeps
		// tolerating neighbouring keys instead of rejecting the policy that carries
		// them.
		assertEquals("4.000", parsedTimeoutOf("neighbour-underscore.cfg", "[limits]\ntimeout_ms=5\ntimeout=4.000\n"));
		assertEquals("4.000", parsedTimeoutOf("neighbour-plural.cfg", "[limits]\ntimeouts=3\ntimeout=4.000\n"));
		assertEquals("4.000", parsedTimeoutOf("neighbour-hyphen.cfg", "[limits]\ntimeout-max=5\ntimeout=4.000\n"));
		// Unrelated dotted entries in [limits] remain tolerated.
		assertEquals("4.000", parsedTimeoutOf("neighbour-dotted.cfg", "[limits]\ntimeout.foo=5\ntimeout=4.000\n"));
	}

	@Test
	void commentsAndBlankLinesAreNotMistakenForTimeoutDeclarations() throws Exception {
		// Comments are stripped before the line is classified, so the word alone is
		// never a declaration and a policy that documents its timeout still parses.
		assertEquals("4.000",
				parsedTimeoutOf("commented.cfg", "[limits]\n# timeout: 5 would be wrong here\n\ntimeout=4.000\n"));
		assertEquals("4.000", parsedTimeoutOf("trailing-comment.cfg", "[limits]\ntimeout=4.000 # canonical seconds\n"));
	}

	@Test
	void aMalformedTimeoutStopsTheProtectedCommand() throws Exception {
		// The value only matters if the malformed policy also prevents the run. The
		// wrapper must refuse before it reaches the command, rather than starting it
		// with no limit applied.
		Path shellRoot = wrapperShellRoot();

		ProcessResult accepted = runWrapper(shellRoot, "valid.cfg", "[limits]\ntimeout=5.000\n");
		assertEquals(0, accepted.exitCode(), accepted.output());
		assertTrue(Files.exists(markerOf(shellRoot)), "a valid policy must still run the protected command");

		for (String malformed : new String[] { "[limits]\ntimeout\n", "[limits]\ntimeout:\n", "[limits]\ntimeout: 5\n",
				"[limits]\ntimeout 5\n", "[limits]\ntimeout =\n", "[limits]\ntimeout = invalid\n" }) {
			Files.deleteIfExists(markerOf(shellRoot));

			ProcessResult rejected = runWrapper(shellRoot, "malformed.cfg", malformed);

			assertEquals(11, rejected.exitCode(), rejected.output());
			assertTrue(rejected.output().contains("PHB-EPOLICY"), rejected.output());
			assertTrue(Files.notExists(markerOf(shellRoot)), "the protected command must not run under " + malformed);
		}
	}

	@Test
	void aConfiguredTimeoutIsEnforcedByTheWrapper() throws Exception {
		// A parsed value is worth nothing unless it reaches GNU timeout and fires. The
		// wrapper's own timeout exit code and message are what prove that the whole
		// chain carried the configured seconds through, so they are asserted rather
		// than the elapsed time.
		Path shellRoot = wrapperShellRoot();

		ProcessResult result = runWrapper(shellRoot, "enforced.cfg", "[limits]\ntimeout=0.500\n", "sleep 5");

		assertEquals(14, result.exitCode(), result.output());
		assertTrue(result.output().contains("PHB-ETIMEOUT"), result.output());
		assertTrue(result.output().contains("Timed out after 0.500s"), result.output());
	}

	/**
	 * A self-contained copy of the wrapper with a minimal base policy, so the run
	 * exercises the shipped entry point rather than a reimplementation of it.
	 */
	private Path wrapperShellRoot() throws IOException {
		Path shellRoot = Files.createDirectory(temporaryDirectory.resolve("wrapper"));
		for (String script : new String[] { "phobos.sh", "phobos-common.sh", "phobos-timeout.sh", "phobos-network.sh",
				"phobos-filesystem.sh" }) {
			Files.copy(TEMPLATES.resolve(script), shellRoot.resolve(script));
		}
		Files.writeString(shellRoot.resolve("Base.cfg"), "[read]\n/etc\n");
		Files.createFile(shellRoot.resolve("TailPhobos.cfg"));
		return shellRoot;
	}

	private Path markerOf(Path shellRoot) {
		return shellRoot.resolve("protected-command-ran");
	}

	/**
	 * Runs the wrapper over a policy with the network and filesystem layers off, so
	 * the timeout layer under test is reached without a sandbox runtime present.
	 */
	private ProcessResult runWrapper(Path shellRoot, String fileName, String configurationContents) throws Exception {
		return runWrapper(shellRoot, fileName, configurationContents, "touch '" + markerOf(shellRoot) + "'");
	}

	/**
	 * The same run with an explicit protected command, for the cases that observe
	 * how the command ends rather than whether it started. The command is a fixed
	 * literal supplied by the test.
	 */
	private ProcessResult runWrapper(Path shellRoot, String fileName, String configurationContents,
			String protectedCommand) throws Exception {
		Path config = temporaryDirectory.resolve(fileName);
		Files.writeString(config, configurationContents);

		return run("bash '" + shellRoot.resolve("phobos.sh") + "' --no-network --no-filesystem --config '" + config
				+ "' -- " + protectedCommand);
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
