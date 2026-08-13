package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
	void aDedicatedTimeoutSectionWithoutAValueIsRejected() throws Exception {
		// A dedicated section announces a timeout, so declaring one and then supplying
		// nothing is a policy error rather than "no timeout". Blank and comment lines
		// are dropped before the value is classified, so the absence only shows up at
		// the next section header or at the end of the file.
		assertPolicyError("section-empty-at-eof.cfg", "[timeout]\n");
		assertPolicyError("section-comment-only-at-eof.cfg", "[timeout]\n# no effective value\n");
		assertPolicyError("section-empty-before-section.cfg", "[timeout]\n\n[read]\n/etc\n");
		assertPolicyError("section-comment-only-before-section.cfg", "[timeout]\n# configured later\n\n[read]\n/etc\n");
		// Every dedicated section is judged on its own, so a filled one does not excuse
		// an empty one later in the same file.
		assertPolicyError("section-repeated-second-empty.cfg", "[timeout]\n0.500\n[timeout]\n# nothing\n");
	}

	@Test
	void aDedicatedTimeoutSectionWithSeveralValuesIsRejected() throws Exception {
		// Two values in one section have no defined winner, so silently taking the last
		// one would hide a policy the author did not write.
		assertPolicyError("section-two-values.cfg", "[timeout]\n0.500\n1.000\n");
		assertPolicyError("section-two-assignments.cfg", "[timeout]\ntimeout=0.500\ntimeout=1.000\n");
	}

	@Test
	void aDedicatedTimeoutSectionWithExactlyOneValueIsAccepted() throws Exception {
		assertEquals("0.500", parsedTimeoutOf("section-one-bare.cfg", "[timeout]\n0.500\n"));
		assertEquals("0.500", parsedTimeoutOf("section-one-assignment.cfg", "[timeout]\ntimeout=0.500\n"));
		// A numeric zero is a supplied value that means "no timeout", so it satisfies
		// the section even though it resolves to the empty runtime value. The count,
		// not the resolved value, is what decides.
		assertEquals("", parsedTimeoutOf("section-one-zero.cfg", "[timeout]\n0.000\n"));
		// Repeated sections stay valid as long as each supplies its own single value.
		assertEquals("1.500", parsedTimeoutOf("section-repeated-filled.cfg", "[timeout]\n0.500\n[timeout]\n1.500\n"));
		// A policy that never mentions a timeout remains valid, and [limits] keeps
		// carrying unrelated entries.
		assertEquals("", parsedTimeoutOf("section-absent.cfg", "[read]\n/etc\n"));
		assertEquals("", parsedTimeoutOf("limits-without-timeout.cfg", "[limits]\nmemory=512\n"));
	}

	@Test
	void anEmptyTimeoutSectionCannotInheritAPreviouslyParsedValue() throws Exception {
		// The parsed timeout outlives a single file so that layered policies can leave
		// it alone, which is exactly why an empty section must be judged on what the
		// section itself supplied.
		Path first = temporaryDirectory.resolve("layer-first.cfg");
		Files.writeString(first, "[limits]\ntimeout=0.500\n");
		Path second = temporaryDirectory.resolve("layer-second.cfg");
		Files.writeString(second, "[timeout]\n# nothing here\n");

		ProcessResult result = run(sourceCommonAndParse(first, second));

		assertEquals(11, result.exitCode(), result.output());
		assertTrue(result.output().contains("PHB-EPOLICY"), result.output());
	}

	@Test
	void aSuppliedTimeoutStillWinsOverTheLayerBeforeIt() throws Exception {
		// The counter guards how many values a section supplies, not which one the
		// layering keeps, so a policy that does supply a value must still override the
		// layer before it and a policy that stays silent must still leave it standing.
		Path first = temporaryDirectory.resolve("wins-first.cfg");
		Files.writeString(first, "[limits]\ntimeout=0.500\n");
		Path second = temporaryDirectory.resolve("wins-second.cfg");
		Files.writeString(second, "[timeout]\n1.500\n");
		Path silent = temporaryDirectory.resolve("wins-silent.cfg");
		Files.writeString(silent, "[read]\n/etc\n");

		ProcessResult overridden = run(sourceCommonAndParse(first, second) + printParsedTimeout());
		assertEquals(0, overridden.exitCode(), overridden.output());
		assertEquals("1.500", overridden.output());

		ProcessResult retained = run(sourceCommonAndParse(first, silent) + printParsedTimeout());
		assertEquals(0, retained.exitCode(), retained.output());
		assertEquals("0.500", retained.output());
	}

	@Test
	void aPaddedSectionHeaderNamesTheSameSectionAsAnUnpaddedOne() throws Exception {
		// Validation and parsing must agree on what a header names. When only one of
		// them ignored the padding, "[ timeout ]" passed validation and was then parsed
		// as a section nobody handles, so its value was dropped without a word.
		assertEquals("0.500", parsedTimeoutOf("padded-timeout.cfg", "[ timeout ]\n0.500\n"));
		assertEquals("/etc", parsedReadPathsOf("padded-read.cfg", "[ read ]\n/etc\n"));
		// The canonical spelling is unaffected.
		assertEquals("/etc", parsedReadPathsOf("plain-read.cfg", "[read]\n/etc\n"));
		// A padded section is a real section, so it carries the cardinality rule too.
		assertPolicyError("padded-timeout-empty.cfg", "[ timeout ]\n# nothing\n");
		assertPolicyError("padded-timeout-two.cfg", "[ timeout ]\n0.500\n1.000\n");
		// Unknown sections stay rejected in either spelling.
		assertPolicyError("padded-unknown.cfg", "[ unknown ]\nvalue\n");
		assertPolicyError("plain-unknown.cfg", "[unknown]\nvalue\n");
		// Only the padding around the name is ignored, so a name that is genuinely two
		// words is unknown rather than being read as a section it merely resembles.
		assertPolicyError("inner-space.cfg", "[read only]\n/etc\n");
		// Padding is whitespace of any kind, not spaces alone.
		assertEquals("0.500", parsedTimeoutOf("tab-padded-timeout.cfg", "[\ttimeout\t]\n0.500\n"));
		assertEquals("/etc", parsedReadPathsOf("tab-padded-read.cfg", "[\tread\t]\n/etc\n"));
	}

	@Test
	void aSectionNameIsComparedLiterallyRatherThanAsAPattern() throws Exception {
		// The parser compares section names literally, so the allowed-name check must
		// too. A name that merely reads as a pattern for an allowed one would
		// otherwise be accepted and then parsed as a section nobody handles, and the
		// whole section would be dropped without a word.
		assertPolicyError("pattern-any-character.cfg", "[re.d]\n/etc\n");
		assertPolicyError("pattern-repetition.cfg", "[timeoutt*]\n0.500\n");
		assertPolicyError("pattern-anchor.cfg", "[read$]\n/etc\n");
		assertPolicyError("pattern-any-sequence.cfg", "[time.*]\n0.500\n");
		// A leading dash is part of the name, never an option to the comparison.
		assertPolicyError("leading-dash.cfg", "[-read]\n/etc\n");
	}

	@Test
	void aSectionHeaderWithoutANameIsRejected() throws Exception {
		// An empty name selects no section, so its lines would be read and silently
		// discarded. Whitespace of any kind leaves the name empty once trimmed.
		assertPolicyError("blank-name.cfg", "[   ]\nvalue\n");
		assertPolicyError("tab-name.cfg", "[\t]\nvalue\n");
		assertPolicyError("tabs-only-name.cfg", "[\t\t]\n/etc\n");
		assertPolicyError("no-name.cfg", "[]\n/etc\n");
	}

	@Test
	void aMalformedSectionHeaderIsRejectedRatherThanReadAsContent() throws Exception {
		// A line that opens with a bracket is a header, so it is held to the whole
		// grammar: exactly one bracket pair around a name. Anything else used to be
		// read as a section nobody handles, or fall through as a path, and either way
		// the lines that followed were discarded in silence.
		assertPolicyError("nested-brackets.cfg", "[[rt]ead]\n/etc\n");
		assertPolicyError("double-closing.cfg", "[read]]\n/etc\n");
		assertPolicyError("double-opening.cfg", "[[read]\n/etc\n");
		assertPolicyError("unclosed-inner.cfg", "[re[ad]\n/etc\n");
		// Content after the closing bracket is not a comment, so it is not a header.
		assertPolicyError("trailing-content.cfg", "[read] trailing-content\n/etc\n");
	}

	@Test
	void aTrailingCommentAfterASectionHeaderFollowsTheCommentContract() throws Exception {
		// Comments end a line before anything classifies it, for headers exactly as
		// for values, so a documented header stays a header.
		assertEquals("/etc", parsedReadPathsOf("commented-read.cfg", "[read] # what this covers\n/etc\n"));
		assertEquals("0.500", parsedTimeoutOf("commented-timeout.cfg", "[timeout] # canonical seconds\n0.500\n"));
		// The name behind the comment is validated like any other, which it was not
		// while validation and parsing read the line differently.
		assertPolicyError("commented-unknown.cfg", "[unknown] # documented\nvalue\n");
	}

	@Test
	void bracketsInsideSectionContentStayContent() throws Exception {
		// Only a line that opens with a bracket is a header, so a path keeps its own
		// brackets, spaces and pattern characters instead of being reclassified.
		assertEquals("/tmp/a[b]c", parsedReadPathsOf("bracketed-path.cfg", "[read]\n/tmp/a[b]c\n"));
		assertEquals("/tmp/x]", parsedReadPathsOf("trailing-bracket-path.cfg", "[read]\n/tmp/x]\n"));
		assertEquals("/tmp/a b c", parsedReadPathsOf("spaced-path.cfg", "[read]\n/tmp/a b c\n"));
		assertEquals("/tmp/a.*b", parsedReadPathsOf("pattern-path.cfg", "[read]\n/tmp/a.*b\n"));
	}

	@Test
	void aNegativeTimeoutIsRejected() throws Exception {
		assertPolicyError("negative-integer.cfg", "[limits]\ntimeout=-1\n");
		assertPolicyError("negative-decimal.cfg", "[limits]\ntimeout=-0.500\n");
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
	void aTimeoutSectionWithoutAValueStopsTheProtectedCommand() throws Exception {
		// A section that promises a timeout and supplies none must stop the run, not
		// start it unprotected.
		Path shellRoot = wrapperShellRoot();

		for (String withoutValue : new String[] { "[timeout]\n", "[timeout]\n# configured later\n",
				"[timeout]\n\n[read]\n/etc\n", "[timeout]\n0.500\n1.000\n" }) {
			Files.deleteIfExists(markerOf(shellRoot));

			ProcessResult rejected = runWrapper(shellRoot, "no-value.cfg", withoutValue);

			assertEquals(11, rejected.exitCode(), rejected.output());
			assertTrue(rejected.output().contains("PHB-EPOLICY"), rejected.output());
			assertTrue(Files.notExists(markerOf(shellRoot)),
					"the protected command must not run under " + withoutValue);
		}
	}

	@Test
	void aRejectedSectionHeaderStopsTheProtectedCommand() throws Exception {
		// A header the policy layer refuses must stop the run rather than let it start
		// with the section quietly missing from the sandbox.
		Path shellRoot = wrapperShellRoot();

		for (String rejected : new String[] { "[re.d]\n/etc\n", "[timeoutt*]\n0.500\n", "[   ]\nvalue\n",
				"[\t]\nvalue\n", "[[rt]ead]\n/etc\n", "[read]]\n/etc\n", "[[read]\n/etc\n",
				"[read] trailing-content\n/etc\n", "[]\n/etc\n", "[unknown] # documented\nvalue\n" }) {
			Files.deleteIfExists(markerOf(shellRoot));

			ProcessResult result = runWrapper(shellRoot, "rejected-header.cfg", rejected);

			assertEquals(11, result.exitCode(), result.output());
			assertTrue(result.output().contains("PHB-EPOLICY"), result.output());
			assertTrue(Files.notExists(markerOf(shellRoot)), "the protected command must not run under " + rejected);
		}
	}

	@Test
	void anEmptyTimeoutSectionIsRejectedRatherThanInheritingTheBaseTimeout() throws Exception {
		// The wrapper parses the base policy before the exercise policy, so the base
		// timeout is still in hand when the exercise section turns out to be empty.
		// Reusing it would run the exercise under a limit its own policy never stated.
		Path shellRoot = wrapperShellRoot("[read]\n/etc\n[limits]\ntimeout=0.500\n");
		Files.deleteIfExists(markerOf(shellRoot));

		ProcessResult rejected = runWrapper(shellRoot, "inheriting.cfg", "[timeout]\n# nothing here\n");

		assertEquals(11, rejected.exitCode(), rejected.output());
		assertTrue(rejected.output().contains("PHB-EPOLICY"), rejected.output());
		assertTrue(Files.notExists(markerOf(shellRoot)), "the protected command must not run under an empty section");
	}

	@Test
	void anExplicitZeroInALaterPolicyClearsTheBaseTimeout() throws Exception {
		// Zero means "no timeout", so a policy that states it must remove the limit the
		// base established. Reading it as silence instead left the base seconds in
		// force and killed a command the exercise policy had deliberately unbounded.
		Path shellRoot = wrapperShellRoot("[read]\n/etc\n[limits]\ntimeout=0.500\n");
		Files.deleteIfExists(markerOf(shellRoot));

		ProcessResult result = runWrapper(shellRoot, "explicit-zero.cfg", "[timeout]\n0.000\n",
				slowCommandTouchingMarker(shellRoot));

		assertEquals(0, result.exitCode(), result.output());
		assertTrue(Files.exists(markerOf(shellRoot)), "the command must outlive the cleared base timeout");
		assertFalse(result.output().contains("PHB-ETIMEOUT"), result.output());
	}

	@Test
	void aLaterPolicyWithoutATimeoutKeepsTheBaseTimeout() throws Exception {
		// Silence is not zero. A policy that says nothing about the timeout must leave
		// the base limit standing, which is what makes the explicit zero above a real
		// statement rather than the default.
		Path shellRoot = wrapperShellRoot("[read]\n/etc\n[limits]\ntimeout=0.500\n");
		Files.deleteIfExists(markerOf(shellRoot));

		ProcessResult result = runWrapper(shellRoot, "silent.cfg", "[read]\n/etc\n",
				slowCommandTouchingMarker(shellRoot));

		assertEquals(14, result.exitCode(), result.output());
		assertTrue(result.output().contains("PHB-ETIMEOUT"), result.output());
		assertTrue(Files.notExists(markerOf(shellRoot)), "the base timeout must still stop the command");
	}

	@Test
	void aLaterNonZeroTimeoutReplacesTheBaseTimeout() throws Exception {
		// Last-wins for supplied values is unchanged: a later policy can both loosen
		// and tighten what the base established.
		Path shellRoot = wrapperShellRoot("[read]\n/etc\n[limits]\ntimeout=0.500\n");
		Files.deleteIfExists(markerOf(shellRoot));

		ProcessResult longer = runWrapper(shellRoot, "longer.cfg", "[timeout]\n5.000\n",
				slowCommandTouchingMarker(shellRoot));

		assertEquals(0, longer.exitCode(), longer.output());
		assertTrue(Files.exists(markerOf(shellRoot)), "the later, longer timeout must replace the base one");

		ProcessResult shorter = runWrapper(shellRoot, "shorter.cfg", "[timeout]\n0.200\n", "sleep 5");

		assertEquals(14, shorter.exitCode(), shorter.output());
		assertTrue(shorter.output().contains("Timed out after 0.200s"), shorter.output());
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
		return wrapperShellRoot("[read]\n/etc\n");
	}

	/**
	 * The same copy with a caller-chosen base policy, for the cases that observe
	 * how an exercise policy interacts with what the base already established.
	 */
	private Path wrapperShellRoot(String baseConfiguration) throws IOException {
		Path shellRoot = Files.createDirectory(temporaryDirectory.resolve("wrapper"));
		for (String script : new String[] { "phobos.sh", "phobos-common.sh", "phobos-timeout.sh", "phobos-network.sh",
				"phobos-filesystem.sh" }) {
			Files.copy(TEMPLATES.resolve(script), shellRoot.resolve(script));
		}
		Files.writeString(shellRoot.resolve("Base.cfg"), baseConfiguration);
		Files.createFile(shellRoot.resolve("TailPhobos.cfg"));
		return shellRoot;
	}

	private Path markerOf(Path shellRoot) {
		return shellRoot.resolve("protected-command-ran");
	}

	/**
	 * A protected command that outlives a sub-second timeout and leaves the marker
	 * behind, so completing and being killed are told apart by evidence the command
	 * itself wrote. It is a script file rather than an inline command because the
	 * wrapper invocation is already a quoted shell string.
	 */
	private String slowCommandTouchingMarker(Path shellRoot) throws IOException {
		Path script = shellRoot.resolve("slow-command.sh");
		Files.writeString(script, "#!/usr/bin/env bash\nsleep 2\ntouch '" + markerOf(shellRoot) + "'\n");
		return "bash '" + script + "'";
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

		ProcessResult result = run(sourceCommonAndParse(config) + printParsedTimeout());

		assertEquals(0, result.exitCode(), result.output());
		return result.output();
	}

	/**
	 * Runs the shipped parser over a configuration and returns the read-only paths
	 * it collected, so a silently ignored section shows up as an empty result.
	 */
	private String parsedReadPathsOf(String fileName, String configurationContents) throws Exception {
		Path config = temporaryDirectory.resolve(fileName);
		Files.writeString(config, configurationContents);

		ProcessResult result = run(sourceCommonAndParse(config) + "; cat \"${PARSED_RO_FILE}\"");

		assertEquals(0, result.exitCode(), result.output());
		return result.output().strip();
	}

	/**
	 * Sources the shipped parser and runs it over the configurations in order, so a
	 * layered policy is exercised the way the wrapper stacks its files.
	 */
	private String sourceCommonAndParse(Path... configs) {
		StringBuilder script = new StringBuilder(
				"source '" + TEMPLATES.resolve("phobos-common.sh") + "'; INI_TMP_DIRS=''");
		for (Path config : configs) {
			script.append("; parse_cfg_policy '").append(config).append("'");
		}
		return script.toString();
	}

	private String printParsedTimeout() {
		return "; printf '%s' \"${PARSED_TIMEOUT}\"";
	}

	private ProcessResult run(String script) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", "-c", script).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		return new ProcessResult(process.waitFor(), output);
	}

	private record ProcessResult(int exitCode, String output) {
	}
}
