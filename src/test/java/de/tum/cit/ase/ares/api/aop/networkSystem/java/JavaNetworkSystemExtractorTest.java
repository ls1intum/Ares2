package de.tum.cit.ase.ares.api.aop.networkSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaNetworkSystemExtractor.
 *
 * <p>Description: Verifies extraction and filtering of network system permissions for connect, send, receive operations.
 *
 * <p>Design Rationale: Ensures full coverage across all permission types and invalid inputs.
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
    public void testExtractHostsAndPortsAllPermissionTypesAndValid() {
        List<NetworkPermission> configs = List.of(
                NetworkPermission.builder().onTheHost("0.0.0.0").onThePort(0).openConnections(true).sendData(true).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("10.10.10.10").onThePort(10).openConnections(true).sendData(false).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("100.100.100.100").onThePort(100).openConnections(false).sendData(true).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("200.200.200.200").onThePort(1000).openConnections(false).sendData(false).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("255.255.255.255").onThePort(10000).openConnections(false).sendData(false).receiveData(false).build()
        );
        // connect
        List<String> connectHosts = List.of("0.0.0.0", "10.10.10.10");
        List<String> connectPorts = List.of("0", "10");
        Assertions.assertEquals(connectHosts, JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::openConnections));
        Assertions.assertEquals(connectPorts, JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::openConnections));

        // send
        List<String> sendHosts = List.of("0.0.0.0", "100.100.100.100");
        List<String> sendPorts = List.of("0", "100");
        Assertions.assertEquals(sendHosts, JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::sendData));
        Assertions.assertEquals(sendPorts, JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::sendData));

        // receive
        List<String> receiveHosts = List.of("0.0.0.0", "200.200.200.200");
        List<String> receivePorts = List.of("0", "1000");
        Assertions.assertEquals(receiveHosts, JavaNetworkSystemExtractor.extractHosts(configs, NetworkPermission::receiveData));
        Assertions.assertEquals(receivePorts, JavaNetworkSystemExtractor.extractPorts(configs, NetworkPermission::receiveData));
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
    public void testExtractHostsAndPortsAllPermissionTypesAndInvalid() {
        Supplier<List<?>> supplier = () -> List.of(
                NetworkPermission.builder().onTheHost("0.0.0.0").onThePort(0).openConnections(true).sendData(true).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("10.10.10.10").onThePort(10).openConnections(true).sendData(false).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("100.100.100.100").onThePort(100).openConnections(false).sendData(true).receiveData(false).build(),
                NetworkPermission.builder().onTheHost("200.200.200.200").onThePort(1000).openConnections(false).sendData(false).receiveData(true).build(),
                NetworkPermission.builder().onTheHost("255.255.255.255").onThePort(10000).openConnections(false).sendData(false).receiveData(false).build()
        );
        JavaNetworkSystemExtractor extractor = new JavaNetworkSystemExtractor(supplier);

        // connect hosts
        List<String> expectedConnectHosts = List.of("0.0.0.0", "10.10.10.10");
        List<Integer> expectedConnectPorts = List.of(0, 10);
        List<String> actualConnectHosts = extractor.getPermittedNetworkHosts("connect");
        List<Integer> actualConnectPorts = extractor.getPermittedNetworkPorts("connect");
        Assertions.assertEquals(expectedConnectHosts, actualConnectHosts);
        Assertions.assertEquals(expectedConnectPorts, actualConnectPorts);

        // send hosts
        List<String> expectedSendHosts = List.of("0.0.0.0", "100.100.100.100");
        List<Integer> expectedSendPorts = List.of(0, 100);
        List<String> actualSendHosts = extractor.getPermittedNetworkHosts("send");
        List<Integer> actualSendPorts = extractor.getPermittedNetworkPorts("send");
        Assertions.assertEquals(expectedSendHosts, actualSendHosts);
        Assertions.assertEquals(expectedSendPorts, actualSendPorts);

        // receive hosts
        List<String> expectedReceiveHosts = List.of("0.0.0.0", "200.200.200.200");
        List<Integer> expectedReceivePorts = List.of(0, 1000);
        List<String> actualReceiveHosts = extractor.getPermittedNetworkHosts("receive");
        List<Integer> actualReceivePorts = extractor.getPermittedNetworkPorts("receive");
        Assertions.assertEquals(expectedReceiveHosts, actualReceiveHosts);
        Assertions.assertEquals(expectedReceivePorts, actualReceivePorts);

        // invalid
        Assertions.assertThrows(IllegalArgumentException.class, () -> extractor.getPermittedNetworkHosts("kill"));
    }
}
