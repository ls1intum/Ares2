package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Assertions;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.unixdomainsocket.UnixDomainSocketConnectMain;

/**
 * End-to-end tests that the two AOP backends name the same blocked call in a
 * mutually recognisable way.
 * <p>
 * The backends observe a call from different positions. AspectJ weaves the
 * {@code call(...)} join point in the supervised code, so its signature names
 * the static receiver type ({@code java.nio.channels.SocketChannel}).
 * Instrumentation advises the implementation whose bytecode runs and reports
 * that class through {@code @Advice.Origin("#t")}
 * ({@code sun.nio.ch.SocketChannelImpl}). An expectation authored against one
 * backend could therefore never match the other, even though both raise the
 * same denial for the same target.
 * <p>
 * AspectJ now appends the resolved runtime declaration, so its message contains
 * the Instrumentation form as a substring while keeping the call-site identity
 * that only AspectJ observes. These assertions are deliberately made against a
 * real woven call rather than a synthetic join point, because the whole
 * mechanism depends on {@code thisJoinPoint.getTarget()} being populated for
 * {@code call} join points.
 */
class AspectJDeclarationNormalisationTest extends SystemAccessTest {

	private static final String UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/connect/unixdomainsocket";

	private static final String STATIC_DECLARATION = "java.nio.channels.SocketChannel.connect";
	private static final String RESOLVED_DECLARATION = "sun.nio.ch.SocketChannelImpl.connect";

	/**
	 * The AspectJ message must keep its own call-site identity and additionally
	 * carry the implementation identity that Instrumentation reports.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_aspectJReportsBothTheCallSiteAndTheResolvedDeclaration() {
		SecurityException exception = assertAresSecurityExceptionNetwork(
				UnixDomainSocketConnectMain::connectViaUnixDomainSocket, UnixDomainSocketConnectMain.class);
		String message = exception.getMessage();

		Assertions.assertTrue(message.contains(STATIC_DECLARATION),
				"the call-site declaration only AspectJ observes must be kept: " + message);
		Assertions.assertTrue(message.contains("[resolved runtime declaration: " + RESOLVED_DECLARATION),
				"the resolved implementation must be appended: " + message);
	}

	/**
	 * Pins the property the reporting gap was actually about: an expectation
	 * recorded from the Instrumentation backend must be findable in the AspectJ
	 * message. Consumers match the API name as a substring, so this is what makes a
	 * single expectation file usable for both backends.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = UNIX_DOMAIN_SOCKET_CONNECT_WITHIN_PATH)
	void test_instrumentationReportsTheResolvedDeclaration() {
		SecurityException exception = assertAresSecurityExceptionNetwork(
				UnixDomainSocketConnectMain::connectViaUnixDomainSocket, UnixDomainSocketConnectMain.class);
		String message = exception.getMessage();

		Assertions.assertTrue(message.contains(RESOLVED_DECLARATION),
				"the Instrumentation backend reports the implementation class: " + message);
		Assertions.assertFalse(message.contains("[resolved runtime declaration:"),
				"Instrumentation already names the implementation and must not be annotated: " + message);
	}
}
