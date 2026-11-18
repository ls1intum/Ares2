package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.unixdomainsocket.UnixDomainSocketConnectMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.datagram.DatagramConnectMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.send.httpclient.HttpClientSendMain;

/**
 * Integration tests to verify that composite network operations are checked
 * against every real action they perform, not just one (I-068/TD-020): a policy
 * that grants SEND but denies RECEIVE must still block
 * {@link java.net.http.HttpClient#send}, since it both transmits the request
 * AND returns the response body. Also covers I-110/TD-059: a
 * {@link java.net.UnixDomainSocketAddress} target, whose {@code toString()} has
 * no colon, must still be denied under a non-empty network policy that doesn't
 * cover it - not silently allowed because the target failed to parse.
 */
class NetworkSystemAccessTest extends SystemAccessTest {

	private static final String HTTP_CLIENT_SEND_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/send/httpclient";
	private static final String UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/connect/unixdomainsocket";

	// <editor-fold
	// desc="accessNetworkSystemViaHttpClientSendSendAllowedReceiveDenied">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_HTTP_SEND_ALLOWED_RECEIVE_DENIED, withinPath = HTTP_CLIENT_SEND_WITHIN_PATH)
	void test_httpClientSendReceiveDeniedMavenArchunitAspectJ() {
		SecurityException exception = assertAresSecurityExceptionNetwork(HttpClientSendMain::sendToSendOnlyAllowedHost,
				HttpClientSendMain.class);
		assertReceiveActionDenied(exception.getMessage());
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_HTTP_SEND_ALLOWED_RECEIVE_DENIED, withinPath = HTTP_CLIENT_SEND_WITHIN_PATH)
	void test_httpClientSendReceiveDeniedMavenArchunitInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionNetwork(HttpClientSendMain::sendToSendOnlyAllowedHost,
				HttpClientSendMain.class);
		assertReceiveActionDenied(exception.getMessage());
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_HTTP_SEND_ALLOWED_RECEIVE_DENIED, withinPath = HTTP_CLIENT_SEND_WITHIN_PATH)
	void test_httpClientSendReceiveDeniedMavenWalaAspectJ() {
		SecurityException exception = assertAresSecurityExceptionNetwork(HttpClientSendMain::sendToSendOnlyAllowedHost,
				HttpClientSendMain.class);
		assertReceiveActionDenied(exception.getMessage());
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_HTTP_SEND_ALLOWED_RECEIVE_DENIED, withinPath = HTTP_CLIENT_SEND_WITHIN_PATH)
	void test_httpClientSendReceiveDeniedMavenWalaInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionNetwork(HttpClientSendMain::sendToSendOnlyAllowedHost,
				HttpClientSendMain.class);
		assertReceiveActionDenied(exception.getMessage());
	}
	// </editor-fold>

	// <editor-fold desc="accessNetworkSystemViaUnixDomainSocket">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_unixDomainSocketConnectMavenArchunitAspectJ() {
		assertAresSecurityExceptionNetwork(UnixDomainSocketConnectMain::connectViaUnixDomainSocket,
				UnixDomainSocketConnectMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_unixDomainSocketConnectMavenArchunitInstrumentation() {
		assertAresSecurityExceptionNetwork(UnixDomainSocketConnectMain::connectViaUnixDomainSocket,
				UnixDomainSocketConnectMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_unixDomainSocketConnectMavenWalaAspectJ() {
		assertAresSecurityExceptionNetwork(UnixDomainSocketConnectMain::connectViaUnixDomainSocket,
				UnixDomainSocketConnectMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_unixDomainSocketConnectMavenWalaInstrumentation() {
		assertAresSecurityExceptionNetwork(UnixDomainSocketConnectMain::connectViaUnixDomainSocket,
				UnixDomainSocketConnectMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="Datagram connect is a connect, not a receive">
	/**
	 * Where the datagram subjects live.
	 */
	private static final String DATAGRAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/datagram";

	/**
	 * Expects the call to be refused and the refusal to name the given action.
	 *
	 * @param executable the datagram call
	 * @param action     the action the refusal must name, such as "send"
	 */
	private void assertDatagramRefusedAs(Executable executable, String action) {
		SecurityException exception = assertAresSecurityExceptionNetwork(executable, DatagramConnectMain.class);
		Assertions.assertTrue(exception.getMessage().contains("illegally " + action),
				() -> "Expected the denial to name the '" + action + "' action, but was:\n" + exception.getMessage());
	}

	/**
	 * Under a policy that permits connecting to the host and port but neither
	 * sending nor receiving, a connect works, a connect to another port is refused,
	 * and each send or receive on the connected socket or channel is refused under
	 * its own name.
	 */
	private void assertDatagramActionsCheckedSeparately() {
		Assertions.assertAll(() -> Assertions.assertDoesNotThrow(DatagramConnectMain::connectDatagramSocket),
				() -> Assertions.assertDoesNotThrow(DatagramConnectMain::connectDatagramChannel),
				() -> assertDatagramRefusedAs(DatagramConnectMain::connectDatagramSocketToOtherPort, "connect"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::connectDatagramChannelToOtherPort, "connect"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::sendOnConnectedDatagramSocket, "send"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::receiveOnConnectedDatagramSocket, "receive"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::receiveOnConnectedDatagramChannel, "receive"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::writeOnConnectedDatagramChannel, "send"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::readOnConnectedDatagramChannel, "receive"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::writeThroughWritableChannel, "send"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::writeThroughGatheringChannel, "send"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::readThroughReadableChannel, "receive"),
				() -> assertDatagramRefusedAs(DatagramConnectMain::readThroughScatteringChannel, "receive"),
				() -> Assertions.assertDoesNotThrow(DatagramConnectMain::bindDatagramSocketToLocalPort),
				() -> Assertions.assertDoesNotThrow(DatagramConnectMain::bindDatagramSocketToLocalAddress),
				() -> Assertions.assertDoesNotThrow(DatagramConnectMain::bindDatagramSocketToLocalPortAndAddress));
	}

	/**
	 * Datagram actions are checked separately with ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = DATAGRAM_WITHIN_PATH)
	void test_datagramActionsCheckedSeparatelyMavenArchunitAspectJ() {
		assertDatagramActionsCheckedSeparately();
	}

	/**
	 * Datagram actions are checked separately with ArchUnit and instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = DATAGRAM_WITHIN_PATH)
	void test_datagramActionsCheckedSeparatelyMavenArchunitInstrumentation() {
		assertDatagramActionsCheckedSeparately();
	}

	/**
	 * Datagram actions are checked separately with WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = DATAGRAM_WITHIN_PATH)
	void test_datagramActionsCheckedSeparatelyMavenWalaAspectJ() {
		assertDatagramActionsCheckedSeparately();
	}

	/**
	 * Datagram actions are checked separately with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = DATAGRAM_WITHIN_PATH)
	void test_datagramActionsCheckedSeparatelyMavenWalaInstrumentation() {
		assertDatagramActionsCheckedSeparately();
	}
	// </editor-fold>

	/**
	 * Asserts the exception was raised by the RECEIVE action check specifically
	 * (not, say, a NOT_PERMITTED-on-SEND denial, which the policy already allows) -
	 * the whole point of I-068's fix is that RECEIVE is now checked independently
	 * of SEND.
	 */
	private void assertReceiveActionDenied(String actualMessage) {
		Assertions.assertTrue(actualMessage.contains("receive"),
				() -> "Expected the denial to name the 'receive' action specifically, but was:\n" + actualMessage);
	}
}
