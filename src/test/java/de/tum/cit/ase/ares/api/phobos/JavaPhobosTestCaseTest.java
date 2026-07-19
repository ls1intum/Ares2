package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
		assertTrue(timeout.writePhobosTestCase().contains("timeout=1234"));
	}
}
