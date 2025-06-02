package de.tum.cit.ase.ares.api.aop.networkSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaNetworkSystemExtractor.
 *
 * <p>Description: Verifies extraction of network hosts and ports for connect, send, receive operations.
 *
 * <p>Design Rationale: Confirms correct filtering and error handling across all network permissions.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaNetworkSystemExtractorTest {

    /**
     * Tests static extractHosts and extractPorts for all network predicates.
     *
     * <p>Description: Supplies diverse NetworkPermission instances and asserts correct host and port lists.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testExtractHosts_andPorts() {
        List<NetworkPermission> configs = List.of(
                NetworkPermission.builder().onTheHost("0.0.0.0").onThePort(0).openConnections(true).sendData(true).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("10.10.10.10").onThePort(10).openConnections(true).sendData(false).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("100.100.100.100").onThePort(100).openConnections(false).sendData(true).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("200.200.200.200").onThePort(1000).openConnections(false).sendData(false).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("255.255.255.255").onThePort(10000).openConnections(false).sendData(false).receiveData(false).build()
        );
        // hosts
        Assertions.assertEquals(List.of("0.0.0.0", "10.10.10.10"), JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::openConnections));
        Assertions.assertEquals(List.of("0.0.0.0", "100.100.100.100"), JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::sendData));
        Assertions.assertEquals(List.of("0.0.0.0", "200.200.200.200"), JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::receiveData));

        // ports
        Assertions.assertEquals(List.of("0", "10"), JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::openConnections));
        Assertions.assertEquals(List.of("0", "100"), JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::sendData));
        Assertions.assertEquals(List.of("0", "1000"), JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::receiveData));
    }

    /**
     * Tests instance methods getPermittedNetworkHosts and getPermittedNetworkPorts for valid and invalid permissions.
     *
     * <p>Description: Uses stubbed supplier and asserts host and port lists for each permission type, plus exception on invalid.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testGetPermittedNetworkHostsAndPortsAndInvalid() {
        Supplier<List<?>> supplier = () -> List.of(
                NetworkPermission.builder().onTheHost("0.0.0.0").onThePort(0).openConnections(true).sendData(true).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("10.10.10.10").onThePort(10).openConnections(true).sendData(false).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("100.100.100.100").onThePort(100).openConnections(false).sendData(true).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("200.200.200.200").onThePort(1000).openConnections(false).sendData(false).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("255.255.255.255").onThePort(10000).openConnections(false).sendData(false).receiveData(false).build()
        );
        JavaNetworkSystemExtractor extractor = new JavaNetworkSystemExtractor(supplier);

        // valid
        Assertions.assertEquals(List.of("0.0.0.0", "10.10.10.10"), extractor.getPermittedNetworkHosts("connect"));
        Assertions.assertEquals(List.of("0.0.0.0", "100.100.100.100"), extractor.getPermittedNetworkHosts("send"));
        Assertions.assertEquals(List.of("0.0.0.0", "200.200.200.200"), extractor.getPermittedNetworkHosts("receive"));
        Assertions.assertEquals(List.of(0, 10), extractor.getPermittedNetworkPorts("connect"));
        Assertions.assertEquals(List.of(0, 100), extractor.getPermittedNetworkPorts("send"));
        Assertions.assertEquals(List.of(0, 1000), extractor.getPermittedNetworkPorts("receive"));

        // invalid
        Assertions.assertThrows(IllegalArgumentException.class, () -> extractor.getPermittedNetworkHosts("kill"));
    }
}
