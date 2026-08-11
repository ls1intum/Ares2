package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Assertions;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.unixdomainsocket.UnixDomainSocketConnectMain;
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
