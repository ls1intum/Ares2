package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
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
}
