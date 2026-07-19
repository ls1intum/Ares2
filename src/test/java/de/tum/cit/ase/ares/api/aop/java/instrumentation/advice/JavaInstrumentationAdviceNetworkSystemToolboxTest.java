package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnixDomainSocketAddress;
import java.net.http.HttpClient;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

class JavaInstrumentationAdviceNetworkSystemToolboxTest {

	/**
	 * Loads the localization bundle while still unrestricted, so that building a
	 * denial message under INSTRUMENTATION mode hits the cached bundle instead of
	 * reading messages.properties, which the file-system advice would otherwise
	 * block. Without this, the message would degrade to the {@code !key!} fallback
	 * and the suffix assertions would fail on a cold bundle.
	 */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.no.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.not.in.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.network.execution");
	}

	private static void resetSettings() throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
	}

	private static void configureInstrumentationMode() {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
	}

	@Test
	void checkNetworkSystemInteraction_appendsNoAllowlistReasonWhenNoRuleConfigured() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeConnectedTo", new String[0], "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeConnectedTo", new int[0], "ARCH",
					"INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkNetworkConnect("evil.example.com", 443));
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "Network exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No allow rule configured")
							|| exception.getMessage().contains("Keine Erlaubnisregel"),
					() -> "Expected the no-allowlist reason, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkNetworkSystemInteraction_appendsNotPermittedReasonWhenConfiguredButNotAllowed() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeConnectedTo",
					new String[] { "good.example.com" }, "ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeConnectedTo", new int[] { 443 }, "ARCH",
					"INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkNetworkConnect("evil.example.com", 443));
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "Network exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No configured allow rule permits this access") || exception
							.getMessage().contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
					() -> "Expected the not-permitted reason, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void toTarget_extractsHostAndPortFromInetSocketAddress() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);
		Object target = toTarget.invoke(null, new InetSocketAddress("example.org", 443));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("example.org:443", toDisplayString.invoke(target));
	}

	@Test
	void toTarget_extractsDefaultPortFromUriScheme() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);
		Object target = toTarget.invoke(null, URI.create("https://example.org/path"));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("example.org:443", toDisplayString.invoke(target));
	}

	@Test
	void toTarget_doesNotTreatBareIpv6LiteralAsHostPort() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		Object target = toTarget.invoke(null, "::1");

		assertNull(target);
	}

	@Test
	void toTarget_extractsBracketedIpv6HostAndPort() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		Object target = toTarget.invoke(null, "[::1]:25565");
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("::1:25565", toDisplayString.invoke(target));
	}

	@Test
	void hostAndPortMatching_acceptsSuffixHostAndWildcardPort() throws Exception {
		Method hostMatches = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("hostMatches",
				String.class, String.class);
		hostMatches.setAccessible(true);
		Method portMatches = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("portMatches",
				int.class, int.class);
		portMatches.setAccessible(true);

		boolean hostAllowed = (boolean) hostMatches.invoke(null, "api.example.org", "example.org");
		boolean portAllowed = (boolean) portMatches.invoke(null, 8443, 0);

		assertTrue(hostAllowed);
		assertTrue(portAllowed);
	}

	@Test
	void toTarget_returnsNullForUnresolvedSocketReceiver() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		try (Socket socket = new Socket()) {
			Object target = toTarget.invoke(null, socket);
			assertNull(target);
		}
	}

	@Test
	void toTarget_extractsHostAndPortFromConnectedSocket() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		try (ServerSocket serverSocket = new ServerSocket(0);
				Socket socket = new Socket("127.0.0.1", serverSocket.getLocalPort());
				Socket acceptedSocket = serverSocket.accept()) {
			Object target = toTarget.invoke(null, socket);
			assertNotNull(target);

			Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
			toDisplayString.setAccessible(true);
			assertEquals("127.0.0.1:" + serverSocket.getLocalPort(), toDisplayString.invoke(target));
		}
	}

	@Test
	void parametersToTarget_extractsHostAndPortFromSeparateArguments() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class);
		parametersToTarget.setAccessible(true);

		Object target = parametersToTarget.invoke(null, (Object) new Object[] { "127.0.0.1", 12345 });
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("127.0.0.1:12345", toDisplayString.invoke(target));
	}

	@Test
	void parametersToTarget_skipsLeadingNonHostPortPairsAndConsumesOnlyResolvedIndices() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { new FileDescriptor(), InetAddress.getByName("127.0.0.1"),
				Integer.valueOf(12345) };
		Object target = parametersToTarget.invoke(null, (Object) params, consumed);
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("127.0.0.1:12345", toDisplayString.invoke(target));
		assertFalse(consumed.get(0));
		assertTrue(consumed.get(1));
		assertTrue(consumed.get(2));
	}

	@Test
	void parametersToTarget_consumesAllAdjacentPairsForMultiPairSignatures() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { "127.0.0.1", Integer.valueOf(12345), InetAddress.getByName("10.0.0.1"),
				Integer.valueOf(0) };
		Object target = parametersToTarget.invoke(null, (Object) params, consumed);
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("127.0.0.1:12345", toDisplayString.invoke(target));
		assertTrue(consumed.get(0));
		assertTrue(consumed.get(1));
		assertTrue(consumed.get(2));
		assertTrue(consumed.get(3));
	}

	@Test
	void parametersToTarget_bareInetAddressFallsBackToSinglePortAndConsumesIndex() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { InetAddress.getByName("127.0.0.2") };
		Object target = parametersToTarget.invoke(null, (Object) params, consumed);
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		// Documents that bare InetAddress preserves legacy blocking semantic by
		// surfacing port=-1; the allowlist downstream still rejects unknown hosts.
		assertEquals("127.0.0.2:-1", toDisplayString.invoke(target));
		assertTrue(consumed.get(0));
	}

	@Test
	void parametersToTarget_bareUriFallsBackToSinglePortAndConsumesIndex() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { URI.create("ssh://evil.example.com/path") };
		Object target = parametersToTarget.invoke(null, (Object) params, consumed);
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("evil.example.com:-1", toDisplayString.invoke(target));
		assertTrue(consumed.get(0));
	}

	@Test
	void parametersToTarget_returnsNullForEmptyParameterArray() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object target = parametersToTarget.invoke(null, (Object) new Object[0], consumed);
		assertNull(target);
		assertTrue(consumed.isEmpty());
	}

	@Test
	void toTarget_resolvesUnixDomainSocketAddressAsPathWithNoPort() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		Object target = toTarget.invoke(null, UnixDomainSocketAddress.of("/tmp/ares-audit.sock"));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("/tmp/ares-audit.sock:-1", toDisplayString.invoke(target));
	}

	@Test
	void toTarget_failsClosedForUnrecognisedSocketAddressInsteadOfReturningNull() throws Exception {
		// I-110: before the fix, an unparseable SocketAddress (no colon in its
		// toString()) returned null here, which analyseViolation's target != null
		// short-circuit then treated as "not forbidden" - a fail-open. Any future
		// address-family shape must still produce a target so the allow-list can deny
		// it.
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget",
				Object.class);
		toTarget.setAccessible(true);

		SocketAddress unparseable = new SocketAddress() {
			@Override
			public String toString() {
				return "no-colon-here";
			}
		};
		Object target = toTarget.invoke(null, unparseable);
		assertNotNull(target);
	}

	@Test
	void checkNetworkSystemInteraction_deniesUnixDomainSocketConnectUnderNonEmptyPolicy() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeConnectedTo", new String[] { "example.com" },
					"ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeConnectedTo", new int[] { 443 }, "ARCH",
					"INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
					.checkNetworkConnectAddress(UnixDomainSocketAddress.of("/tmp/ares-audit.sock")));
			assertTrue(exception.getMessage().contains("ares-audit.sock"),
					() -> "Expected the Unix domain socket path in the message, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void deriveActionChecks_checksBothRealActionsForTheFourCompositeNetworkMethods() throws Exception {
		Method deriveActionChecks = JavaInstrumentationAdviceNetworkSystemToolbox.class
				.getDeclaredMethod("deriveActionChecks", String.class, String.class, Object.class);
		deriveActionChecks.setAccessible(true);

		HttpClient httpClient = HttpClient.newHttpClient();
		assertActionNames(deriveActionChecks, "send", "send", httpClient, "send", "receive");
		assertActionNames(deriveActionChecks, "send", "sendAsync", httpClient, "send", "receive");

		URL url = URI.create("http://example.org").toURL();
		assertActionNames(deriveActionChecks, "connect", "openStream", url, "connect", "receive");

		URLConnection connection = url.openConnection();
		assertActionNames(deriveActionChecks, "send", "getOutputStream", connection, "connect", "send");
		assertActionNames(deriveActionChecks, "receive", "getInputStream", connection, "connect", "receive");

		// A method/instance combination outside the four composite cases still checks
		// only its single default action.
		try (Socket socket = new Socket()) {
			assertActionNames(deriveActionChecks, "connect", "connect", socket, "connect");
		}
	}

	@SuppressWarnings("unchecked")
	private static void assertActionNames(Method deriveActionChecks, String defaultAction, String methodName,
			Object instance, String... expectedActions) throws Exception {
		List<Map.Entry<String, Boolean>> actionsToValidate = (List<Map.Entry<String, Boolean>>) deriveActionChecks
				.invoke(null, defaultAction, methodName, instance);
		List<String> actualActions = actionsToValidate.stream().map(Map.Entry::getKey).toList();
		assertEquals(List.of(expectedActions), actualActions);
	}
}
