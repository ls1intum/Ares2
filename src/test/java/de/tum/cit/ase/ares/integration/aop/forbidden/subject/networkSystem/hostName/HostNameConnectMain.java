package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.hostName;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Student code connecting to the loopback host by name, by address and with a
 * forged name.
 */
public final class HostNameConnectMain {

	/** How long a connection attempt may take, in milliseconds. */
	private static final int CONNECT_TIMEOUT = 1000;

	/** Prevents creating instances of this utility class. */
	private HostNameConnectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Connects to localhost by name.
	 *
	 * @param port the port
	 * @throws IOException if nothing listens there
	 */
	public static void connectByName(int port) throws IOException {
		connect(new InetSocketAddress("localhost", port));
	}

	/**
	 * Connects to the loopback address, which localhost resolves to.
	 *
	 * @param port the port
	 * @throws IOException if nothing listens there
	 */
	public static void connectByLoopbackAddress(int port) throws IOException {
		connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
	}

	/**
	 * Connects to an address that is not localhost's but carries the name
	 * localhost, set without asking the name service.
	 *
	 * @param port the port
	 * @throws IOException if nothing listens there
	 */
	public static void connectWithForgedName(int port) throws IOException {
		connect(new InetSocketAddress(
				InetAddress.getByAddress("localhost", new byte[] { 10, (byte) 255, (byte) 255, 1 }), port));
	}

	/**
	 * Connects a socket to an address.
	 *
	 * @param address the address
	 * @throws IOException if nothing listens there
	 */
	private static void connect(InetSocketAddress address) throws IOException {
		try (Socket socket = new Socket()) {
			socket.connect(address, CONNECT_TIMEOUT);
		}
	}
}
