package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketConnectMain {

	private SocketConnectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Connect to a forbidden network target using {@link Socket#connect}.
	 * <p>
	 * In the AspectJ variants under test the advice should intercept
	 * {@code Socket.connect(SocketAddress, int)} before the real connection is
	 * attempted, so the (non-routable TEST-NET-3, RFC 5737) host is never
	 * contacted. The {@code 1} ms timeout is a last-resort guard in case the advice
	 * is not applied.
	 */
	public static void connectViaSocket() throws IOException {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("203.0.113.1", 80), 1);
		}
	}
}
