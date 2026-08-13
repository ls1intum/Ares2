package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;

class JavaPhobosTestCaseTest {
	@Test
	void serialisesFilesystemNetworkWildcardBoundaryAndTimeoutPermissions() {
		JavaPhobosTestCase filesystem = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
				.resourceAccessSupplier(() -> List.of(new FilePermission("/tmp/data", true, true, true, false, false)))
				.build();
		assertTrue(filesystem.writePhobosTestCase().contains("[write]\n/tmp/data"));

		JavaPhobosTestCase network = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.NETWORK_CONNECTION)
				.resourceAccessSupplier(() -> List.of(new NetworkPermission("localhost", 0, true, true, false),
						new NetworkPermission("example.org", 65535, true, false, false)))
				.build();
		String networkConfiguration = network.writePhobosTestCase();
		assertTrue(networkConfiguration.contains("allow localhost\n"));
		assertFalse(networkConfiguration.contains("localhost:0"));
		assertTrue(networkConfiguration.contains("allow example.org:65535\n"));

		JavaPhobosTestCase timeout = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.TIMEOUT)
				.resourceAccessSupplier(() -> List.of(new ResourceLimitsPermission(1234))).build();
		assertTrue(timeout.writePhobosTestCase().contains("timeout=1.234"));
	}

	@Test
	void timeoutIsSerialisedAsCanonicalDecimalSeconds() {
		// ResourceLimitsPermission carries milliseconds, but the Phobos [limits]
		// timeout
		// field is seconds. The generator converts once at this serialisation boundary,
		// emitting canonical S.mmm decimal seconds (locale-independent, full
		// millisecond
		// precision, digits and one dot only).
		assertEquals("2.000", timeoutValue(2000));
		assertEquals("10.000", timeoutValue(10000));
		assertEquals("2.500", timeoutValue(2500));
		// Sub-second must not collapse to zero (which Phobos would treat as disabled).
		assertEquals("0.500", timeoutValue(500));
		assertEquals("0.001", timeoutValue(1));

		// An absent timeout yields no [limits] section; its default behaviour is
		// decided
		// elsewhere, not at this serialisation boundary.
		JavaPhobosTestCase none = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.TIMEOUT)
				.resourceAccessSupplier(() -> List.of()).build();
		assertFalse(none.writePhobosTestCase().contains("[limits]"));

		// Non-positive milliseconds are rejected at the API boundary (unchanged).
		assertThrows(IllegalArgumentException.class, () -> new ResourceLimitsPermission(0));
		assertThrows(IllegalArgumentException.class, () -> new ResourceLimitsPermission(-1));
	}

	private static String timeoutValue(long millis) {
		JavaPhobosTestCase timeout = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.TIMEOUT)
				.resourceAccessSupplier(() -> List.of(new ResourceLimitsPermission(millis))).build();
		String cfg = timeout.writePhobosTestCase();
		Matcher matcher = Pattern.compile("(?m)^timeout=(\\S+)$").matcher(cfg);
		assertTrue(matcher.find(), "generated cfg must contain a timeout line, was:\n" + cfg);
		return matcher.group(1);
	}

	@Test
	void aBracketLeadingRelativePathIsSerialisedWithADotSlashPrefix() {
		// A line beginning with '[' is a Phobos section header, so a path spelled that
		// way would be read as a header rather than sandboxed. The equivalent './'
		// form names the same relative path and keeps the line unambiguous.
		assertEquals("./[draft", readOnlyLineFor("[draft"));
		assertEquals("./[draft/file", readOnlyLineFor("[draft/file"));
		assertEquals("./[abc]", readOnlyLineFor("[abc]"));
		// A name that collides with a real section is no different: it is still a path.
		assertEquals("./[read]", readOnlyLineFor("[read]"));
	}

	@Test
	void aWhitespaceLeadingRelativePathIsSerialisedWithADotSlashPrefix() {
		// The parser trims a leading space or tab off a line before reading it, which
		// would name a different file. After the prefix the space or tab is interior
		// and survives, so the path still reaches the sandbox as it was written.
		assertEquals("./ [draft", readOnlyLineFor(" [draft"));
		assertEquals("./\t[draft", readOnlyLineFor("\t[draft"));
		// The bracket is not what matters here; any leading space or tab is trimmed.
		assertEquals("./ file", readOnlyLineFor(" file"));
		assertEquals("./\tfile", readOnlyLineFor("\tfile"));
	}

	@Test
	void onlyLeadingCharactersTheParserWouldChangeArePrefixed() {
		// The prefix is added only for the characters that would otherwise be lost or
		// misread, so every other path keeps the exact spelling the policy author
		// wrote, including a space or bracket that appears later in the name.
		assertEquals("./[draft", readOnlyLineFor("./[draft"));
		assertEquals("./ [draft", readOnlyLineFor("./ [draft"));
		assertEquals("a b", readOnlyLineFor("a b"));
		assertEquals("relative/[draft", readOnlyLineFor("relative/[draft"));
		assertEquals("a[b]c", readOnlyLineFor("a[b]c"));
		assertEquals("/tmp/[draft", readOnlyLineFor("/tmp/[draft"));
		assertEquals("/tmp/a[b]c", readOnlyLineFor("/tmp/a[b]c"));
		assertEquals("/etc", readOnlyLineFor("/etc"));
		assertEquals("relative/dir", readOnlyLineFor("relative/dir"));
	}

	@Test
	void theWriteSectionUsesTheSamePathSerialisation() {
		// Both filesystem sections are emitted through one helper, so neither can
		// drift into writing an ambiguous line.
		assertEquals("./[draft", writeLineFor("[draft"));
		assertEquals("./ [draft", writeLineFor(" [draft"));
		assertEquals("./[abc]", writeLineFor("[abc]"));
		assertEquals("./[draft", writeLineFor("./[draft"));
		assertEquals("a[b]c", writeLineFor("a[b]c"));
		assertEquals("/tmp/[draft", writeLineFor("/tmp/[draft"));
	}

	/** The single line the generator writes under {@code [readonly]}. */
	private static String readOnlyLineFor(String path) {
		return singleFilesystemLine(new FilePermission(path, true, false, false, false, false), "readonly");
	}

	/** The single line the generator writes under {@code [write]}. */
	private static String writeLineFor(String path) {
		return singleFilesystemLine(new FilePermission(path, true, true, false, false, false), "write");
	}

	private static String singleFilesystemLine(FilePermission permission, String section) {
		JavaPhobosTestCase testCase = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
				.resourceAccessSupplier(() -> List.of(permission)).build();
		String configuration = testCase.writePhobosTestCase();
		Matcher matcher = Pattern.compile("(?m)^\\[" + section + "]\\n(.+)$").matcher(configuration);
		assertTrue(matcher.find(), "generated cfg must contain a [" + section + "] entry, was:\n" + configuration);
		return matcher.group(1);
	}

	@Test
	void receiveOnlyNetworkPermissionIsIncludedInTheGeneratedAllowlist() {
		// TD-013: collectAllowHostsAndPorts previously only unioned "connect" and
		// "send" permissions, silently dropping a host permitted only to receive from.
		JavaPhobosTestCase network = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.NETWORK_CONNECTION).resourceAccessSupplier(
						() -> List.of(new NetworkPermission("receive-only.example", 443, false, false, true)))
				.build();
		assertTrue(network.writePhobosTestCase().contains("allow receive-only.example:443\n"));
	}
}
