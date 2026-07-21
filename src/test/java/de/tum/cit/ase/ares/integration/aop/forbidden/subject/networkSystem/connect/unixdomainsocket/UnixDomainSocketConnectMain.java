package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.unixdomainsocket;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;

public class UnixDomainSocketConnectMain {

	private UnixDomainSocketConnectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Connect to a forbidden network target using a
	 * {@link UnixDomainSocketAddress}.
	 * <p>
	 * Reproduces I-110: {@code UnixDomainSocketAddress.toString()} has no colon, so
	 * the pre-fix target-resolution fallback returned {@code null} and the
	 * violation check silently treated that as "not forbidden" instead of denying.
	 * The path deliberately does not exist, so even if the advice failed to
	 * intercept this (a regression), the real {@code connect} would fail fast with
	 * an I/O error rather than hang - the same "last-resort guard" pattern as
	 * {@code SocketConnectMain}.
	 */
	public static void connectViaUnixDomainSocket() throws IOException {
		try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
			channel.connect(UnixDomainSocketAddress.of("/tmp/ares-integration-test-nonexistent.sock"));
		}
	}
}
