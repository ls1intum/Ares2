package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import org.junit.jupiter.api.Test;

class JavaInstrumentationAdviceNetworkSystemToolboxTest {

	@Test
	void toTarget_extractsHostAndPortFromInetSocketAddress() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget", Object.class);
		toTarget.setAccessible(true);
		Object target = toTarget.invoke(null, new InetSocketAddress("example.org", 443));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("example.org:443", toDisplayString.invoke(target));
	}

	@Test
	void toTarget_extractsDefaultPortFromUriScheme() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget", Object.class);
		toTarget.setAccessible(true);
		Object target = toTarget.invoke(null, URI.create("https://example.org/path"));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("example.org:443", toDisplayString.invoke(target));
	}

	@Test
	void hostAndPortMatching_acceptsSuffixHostAndWildcardPort() throws Exception {
		Method hostMatches = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("hostMatches",
				String.class, String.class);
		hostMatches.setAccessible(true);
		Method portMatches = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("portMatches", int.class,
				int.class);
		portMatches.setAccessible(true);

		boolean hostAllowed = (boolean) hostMatches.invoke(null, "api.example.org", "example.org");
		boolean portAllowed = (boolean) portMatches.invoke(null, 8443, -1);

		assertTrue(hostAllowed);
		assertTrue(portAllowed);
	}

	@Test
	void toTarget_returnsNullForUnresolvedSocketReceiver() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget", Object.class);
		toTarget.setAccessible(true);

		try (Socket socket = new Socket()) {
			Object target = toTarget.invoke(null, socket);
			assertNull(target);
		}
	}

	@Test
	void toTarget_extractsHostAndPortFromConnectedSocket() throws Exception {
		Method toTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod("toTarget", Object.class);
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
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class);
		parametersToTarget.setAccessible(true);

		Object target = parametersToTarget.invoke(null, (Object) new Object[] { "127.0.0.1", 12345 });
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("127.0.0.1:12345", toDisplayString.invoke(target));
	}
}



