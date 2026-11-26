package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.hostName;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import javax.net.ServerSocketFactory;

/** Student code that opens server sockets and a client datagram socket. */
public final class ListenMain {

	/** Prevents creating instances of this utility class. */
	private ListenMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Listens on a port of the loopback address and stops at once.
	 *
	 * @param port the port
	 * @throws IOException if the port is taken
	 */
	public static void listenOnLoopback(int port) throws IOException {
		new ServerSocket(port, 1, InetAddress.getLoopbackAddress()).close();
	}

	/**
	 * Listens on a port of every interface and stops at once.
	 *
	 * @param port the port
	 * @throws IOException if the port is taken
	 */
	public static void listenOnEveryInterface(int port) throws IOException {
		new ServerSocket(port).close();
	}

	/**
	 * Binds a server socket channel to a port of the loopback address.
	 *
	 * @param port the port
	 * @throws IOException if the port is taken
	 */
	public static void bindChannelOnLoopback(int port) throws IOException {
		try (ServerSocketChannel channel = ServerSocketChannel.open()) {
			channel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
		}
	}

	/**
	 * Listens on a port of the loopback address through the default server socket
	 * factory and stops at once.
	 *
	 * @param port the port
	 * @throws IOException if the port is taken
	 */
	public static void listenThroughFactoryOnLoopback(int port) throws IOException {
		ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getLoopbackAddress()).close();
	}

	/**
	 * Listens on a port of every interface through the default server socket
	 * factory and stops at once.
	 *
	 * @param port the port
	 * @throws IOException if the port is taken
	 */
	public static void listenThroughFactoryOnEveryInterface(int port) throws IOException {
		ServerSocketFactory.getDefault().createServerSocket(port).close();
	}

	/**
	 * Opens a datagram socket on a port the system picks, as a client does.
	 *
	 * @throws IOException if no port is free
	 */
	public static void openDatagramSocket() throws IOException {
		new DatagramSocket().close();
	}
}
