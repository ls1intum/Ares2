package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Student code that connects a datagram socket or channel to a host and port
 * the policy lets it connect to, but not send to or receive from.
 */
public final class DatagramConnectMain {

	/**
	 * The host the policy permits connecting to.
	 */
	private static final String HOST = "127.0.0.1";

	/**
	 * The port the policy permits connecting to.
	 */
	private static final int PORT = 12345;

	/**
	 * A port on the same host that the policy does not name.
	 */
	private static final int OTHER_PORT = 12346;

	/**
	 * How long a receive waits before giving up, in milliseconds.
	 */
	private static final int RECEIVE_TIMEOUT_MILLIS = 200;

	/**
	 * Prevents instantiation of this utility class.
	 */
	private DatagramConnectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Connects a {@link DatagramSocket}, which is permitted.
	 *
	 * @throws IOException if the socket cannot be connected
	 */
	public static void connectDatagramSocket() throws IOException {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName(HOST), PORT);
		}
	}

	/**
	 * Connects a {@link DatagramChannel}, which is permitted.
	 *
	 * @throws IOException if the channel cannot be connected
	 */
	public static void connectDatagramChannel() throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, PORT));
		}
	}

	/**
	 * Connects a {@link DatagramSocket} to a port the policy does not name, which
	 * is not permitted.
	 *
	 * @throws IOException if the socket cannot be connected
	 */
	public static void connectDatagramSocketToOtherPort() throws IOException {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName(HOST), OTHER_PORT);
		}
	}

	/**
	 * Connects a {@link DatagramChannel} to a port the policy does not name, which
	 * is not permitted.
	 *
	 * @throws IOException if the channel cannot be connected
	 */
	public static void connectDatagramChannelToOtherPort() throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, OTHER_PORT));
		}
	}

	/**
	 * Sends a datagram on a connected {@link DatagramSocket}, which is not
	 * permitted.
	 *
	 * @throws IOException if the datagram cannot be sent
	 */
	public static void sendOnConnectedDatagramSocket() throws IOException {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName(HOST), PORT);
			byte[] data = { 1 };
			socket.send(new DatagramPacket(data, data.length));
		}
	}

	/**
	 * Receives on a connected {@link DatagramSocket}, which is not permitted.
	 *
	 * @throws IOException if nothing can be received
	 */
	public static void receiveOnConnectedDatagramSocket() throws IOException {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName(HOST), PORT);
			socket.setSoTimeout(RECEIVE_TIMEOUT_MILLIS);
			byte[] data = new byte[1];
			socket.receive(new DatagramPacket(data, data.length));
		}
	}

	/**
	 * Writes on a connected {@link DatagramChannel}, which is not permitted.
	 *
	 * @throws IOException if the datagram cannot be written
	 */
	public static void writeOnConnectedDatagramChannel() throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, PORT));
			channel.write(ByteBuffer.wrap(new byte[] { 1 }));
		}
	}

	/**
	 * Reads without waiting on a connected {@link DatagramChannel}, which is not
	 * permitted.
	 *
	 * @throws IOException if the channel cannot be read
	 */
	public static void readOnConnectedDatagramChannel() throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, PORT));
			channel.configureBlocking(false);
			channel.read(ByteBuffer.allocate(1));
		}
	}

	/**
	 * Receives without waiting on a connected {@link DatagramChannel}, which is not
	 * permitted.
	 *
	 * @throws IOException if the channel cannot receive
	 */
	public static void receiveOnConnectedDatagramChannel() throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, PORT));
			channel.configureBlocking(false);
			channel.receive(ByteBuffer.allocate(1));
		}
	}
}
