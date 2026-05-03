package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.BitSet;

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

	@Test
	void parametersToTarget_skipsLeadingNonHostPortPairsAndConsumesOnlyResolvedIndices() throws Exception {
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { new FileDescriptor(), InetAddress.getByName("127.0.0.1"), Integer.valueOf(12345) };
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
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object[] params = new Object[] { "127.0.0.1", Integer.valueOf(12345),
				InetAddress.getByName("10.0.0.1"), Integer.valueOf(0) };
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
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class, BitSet.class);
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
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class, BitSet.class);
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
		Method parametersToTarget = JavaInstrumentationAdviceNetworkSystemToolbox.class.getDeclaredMethod(
				"parametersToTarget", Object[].class, BitSet.class);
		parametersToTarget.setAccessible(true);

		BitSet consumed = new BitSet();
		Object target = parametersToTarget.invoke(null, (Object) new Object[0], consumed);
		assertNull(target);
		assertTrue(consumed.isEmpty());
	}
}



