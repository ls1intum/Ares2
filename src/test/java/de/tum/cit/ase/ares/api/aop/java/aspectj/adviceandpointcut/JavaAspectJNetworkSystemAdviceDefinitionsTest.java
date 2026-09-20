package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnixDomainSocketAddress;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for I-110's fix on the AspectJ backend:
 * {@code variableToTarget} (reflected here as it is a private, JoinPoint-free
 * pure conversion method, unlike the JoinPoint-taking advice entry points) must
 * no longer return {@code null} for a recognised but unparseable
 * {@link SocketAddress}, which previously caused {@code analyseViolation} to
 * fail open.
 */
class JavaAspectJNetworkSystemAdviceDefinitionsTest {

	@Test
	void variableToTarget_resolvesUnixDomainSocketAddressAsPathWithNoPort() throws Exception {
		Method variableToTarget = JavaAspectJNetworkSystemAdviceDefinitions.class.getDeclaredMethod("variableToTarget",
				Object.class);
		variableToTarget.setAccessible(true);

		Object target = variableToTarget.invoke(null, UnixDomainSocketAddress.of("/tmp/ares-audit.sock"));
		assertNotNull(target);

		Method toDisplayString = target.getClass().getDeclaredMethod("toDisplayString");
		toDisplayString.setAccessible(true);
		assertEquals("/tmp/ares-audit.sock:-1", toDisplayString.invoke(target));
	}

	@Test
	void variableToTarget_failsClosedForUntrustedSocketAddress() throws Exception {
		Method variableToTarget = JavaAspectJNetworkSystemAdviceDefinitions.class.getDeclaredMethod("variableToTarget",
				Object.class);
		variableToTarget.setAccessible(true);

		SocketAddress unparseable = new SocketAddress() {
			@Override
			public String toString() {
				return "no-colon-here";
			}
		};
		InvocationTargetException exception = assertThrows(InvocationTargetException.class,
				() -> variableToTarget.invoke(null, unparseable));
		assertInstanceOf(SecurityException.class, exception.getCause());
	}

	/**
	 * A denial raised while the advice resolves the connection must reach the code
	 * under test, instead of leaving no target and skipping the receiver check.
	 */
	@Test
	void urlOfConnection_letsADenialThrough() throws Exception {
		URLConnection denying = new URLConnection(URI.create("https://example.org/path").toURL()) {
			@Override
			public void connect() {
			}

			@Override
			public URL getURL() {
				throw new SecurityException("Ares denied this connection");
			}
		};

		Method urlOfConnection = JavaAspectJNetworkSystemAdviceDefinitions.class.getDeclaredMethod("urlOfConnection",
				URLConnection.class);
		urlOfConnection.setAccessible(true);

		InvocationTargetException exception = assertThrows(InvocationTargetException.class,
				() -> urlOfConnection.invoke(null, denying));
		assertInstanceOf(SecurityException.class, exception.getCause(),
				"A denial raised while resolving the connection must propagate, because swallowing it"
						+ " would leave no target and skip the receiver check");
	}

	/**
	 * A connection that cannot report its URL yet must yield no URL, rather than
	 * letting its own failure surface as a fault of the code under test.
	 */
	@Test
	void urlOfConnection_yieldsNoUrlWhenTheConnectionCannotReportOneYet() throws Exception {
		URLConnection notReadyYet = new URLConnection(URI.create("https://example.org/path").toURL()) {
			@Override
			public void connect() {
			}

			@Override
			public URL getURL() {
				throw new NullPointerException("delegate is null");
			}
		};

		Method urlOfConnection = JavaAspectJNetworkSystemAdviceDefinitions.class.getDeclaredMethod("urlOfConnection",
				URLConnection.class);
		urlOfConnection.setAccessible(true);

		assertNull(urlOfConnection.invoke(null, notReadyYet),
				"A connection that cannot report its URL yet must not let its own failure escape into"
						+ " the code under test");
	}
}
