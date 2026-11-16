package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
	void receiveOnlyNetworkPermissionIsIncludedInTheGeneratedAllowlist() {
		// TD-013: collectAllowHostsAndPorts previously only unioned "connect" and
		// "send" permissions, silently dropping a host permitted only to receive from.
		JavaPhobosTestCase network = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.NETWORK_CONNECTION).resourceAccessSupplier(
						() -> List.of(new NetworkPermission("receive-only.example", 443, false, false, true)))
				.build();
		assertTrue(network.writePhobosTestCase().contains("allow receive-only.example:443\n"));
	}

	/**
	 * Requires a path the Phobos reader would cut, trim or refuse to be refused
	 * where Ares writes it. The reader pins no locale, so under C.UTF-8 it also
	 * trims Unicode spaces; the guard refuses those and the no-break spaces too.
	 *
	 * @param path the path a policy permits
	 */
	@ParameterizedTest
	@ValueSource(strings = { "safe\n[write]\n/etc", "/tmp#private", "/data/notes#1.txt", "#/tmp", "/trailing   ",
			"   /leading", "/trailing\t", "/trailing\r", "safe\rdraft", "\u2003/tmp", "/tmp\u3000", "/tmp\u1680",
			"/tmp\u2028", "\u00A0/tmp", "/tmp\u202F", "/tmp\u001C" })
	void refusesAFilesystemPathTheReaderWouldRewrite(String path) {
		JavaPhobosTestCase rewritten = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
				.resourceAccessSupplier(() -> List.of(new FilePermission(path, true, false, false, false, false)))
				.build();
		assertThrows(SecurityException.class, rewritten::writePhobosTestCase);
	}

	/**
	 * Requires a path the reader does not rewrite to be written as it is, interior
	 * blanks, Unicode spaces and percent signs included. Phobos itself refuses a
	 * relative path or one holding a bracket when it runs, so those cases only pin
	 * that Ares does not rewrite them.
	 *
	 * @param path the path a policy permits
	 */
	@ParameterizedTest
	@ValueSource(strings = { "safe\tdraft", "/var/lib/ares", "relative/path", "/a b/c", "/a\u2003b", "/tmp/a%n/etc",
			"/tmp/d100%", "/tmp/[cache]/file", "[write]/etc", "/etc[write]", "[]" })
	void keepsWritingAPathTheReaderDoesNotRewrite(String path) {
		JavaPhobosTestCase accepted = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
				.resourceAccessSupplier(() -> List.of(new FilePermission(path, true, false, false, false, false)))
				.build();
		assertTrue(accepted.writePhobosTestCase().contains(path));
	}

	/**
	 * Requires a path the reader would take for a section header to be refused. The
	 * reader opens a section on such a line instead of granting the path, so
	 * writing one changes what the lines around it mean.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "[write]", "[network]", "[hide]", "[anything]", "[read\ronly]" })
	void refusesAFilesystemPathTheReaderWouldTakeForASectionHeader(String path) {
		JavaPhobosTestCase header = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
				.resourceAccessSupplier(() -> List.of(new FilePermission(path, true, false, false, false, false)))
				.build();
		SecurityException refused = assertThrows(SecurityException.class, header::writePhobosTestCase);
		assertTrue(refused.getMessage().contains(path));
	}

	/**
	 * Requires a policy timeout, held in milliseconds, to be written as the seconds
	 * Phobos reads in {@code [limits]}: whole seconds, a dot and exactly three
	 * digits. No millisecond is lost, and a value below one second never reads as
	 * zero, which Phobos takes for no limit at all.
	 */
	@Test
	void writesTheTimeoutAsSecondsWithThreeDecimals() {
		assertEquals("0.001", timeoutValue(1));
		assertEquals("0.500", timeoutValue(500));
		assertEquals("0.999", timeoutValue(999));
		assertEquals("2.000", timeoutValue(2000));
		assertEquals("2.500", timeoutValue(2500));
		assertEquals("120.000", timeoutValue(120000));
	}

	/**
	 * Pins the conversion at the edge of what Phobos reads. Phobos refuses more
	 * than 15 digits of whole seconds, so it reads the first value and refuses the
	 * other two with a policy error that ends the run, rather than running without
	 * a limit. Ares writes all three exactly and leaves that refusal to Phobos.
	 */
	@Test
	void writesTheLargestTimeoutsExactly() {
		assertEquals("999999999999999.999", timeoutValue(999_999_999_999_999_999L));
		assertEquals("1000000000000000.000", timeoutValue(1_000_000_000_000_000_000L));
		assertEquals("9223372036854775.807", timeoutValue(Long.MAX_VALUE));
	}

	/**
	 * Requires a timeout of zero to be refused before anything is written, since
	 * {@code timeout=0.000} would switch the limit off in Phobos.
	 */
	@Test
	void neverWritesATimeoutOfZero() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceLimitsPermission(0));
	}

	/**
	 * Reads the timeout back from the configuration written for one policy timeout.
	 *
	 * @param millis the policy timeout in milliseconds
	 * @return the text after {@code timeout=}
	 */
	private static String timeoutValue(long millis) {
		JavaPhobosTestCase timeout = JavaPhobosTestCase.builder()
				.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.TIMEOUT)
				.resourceAccessSupplier(() -> List.of(new ResourceLimitsPermission(millis))).build();
		String configuration = timeout.writePhobosTestCase();
		Matcher matcher = Pattern.compile("(?m)^timeout=(\\S+)$").matcher(configuration);
		assertTrue(matcher.find(), configuration);
		return matcher.group(1);
	}
}
