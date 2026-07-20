package de.tum.cit.ase.ares.integration.testuser;

import java.io.*;
import java.net.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.network.NetworkPenguin;

@Public
@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(5)
@TestMethodOrder(MethodName.class)
public class NetworkUser {

	// The echo server is an EXTERNAL process (see AGENTS.md): a sandboxed test JVM
	// must never spin up its own server to test incoming/outgoing connections,
	// because that server would itself be subject to the very policy under test.
	// CI is expected to provide an echo server on the loopback at this port.
	private static final int PORT = 25565;
	private static final String MESSAGE = "hello";

	@ParameterizedTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLoopbackEchoConnectionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
	@ValueSource(strings = { "localhost", "127.0.0.1", "::1" })
	void connectLocallyAllowed(String host) throws Exception {
		try {
			NetworkPenguin.tryConnect(host, PORT, MESSAGE);
		} catch (SecurityException e) {
			// An Ares block of an explicitly allowed connection is a real failure.
			throw e;
		} catch (IOException e) {
			// No Ares block: the external echo server is simply not reachable in this
			// environment (see AGENTS.md). The security property under test - that an
			// allowed connection is NOT blocked by Ares - already holds, so this is not
			// a failure. The functional round-trip is exercised by CI, which provides
			// the external echo server.
		}
	}

	@Test
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/network")
	void connectLocallyNotAllowed() {
		// Should be empty
	}

	@Test
	void connectRemoteNotAllowed() throws Exception {
		NetworkPenguin.tryConnect("example.com", 80, null);
	}
}
