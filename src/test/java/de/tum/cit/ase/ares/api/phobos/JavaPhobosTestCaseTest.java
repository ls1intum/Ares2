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
