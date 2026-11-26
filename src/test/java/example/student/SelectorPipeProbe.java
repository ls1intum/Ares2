package example.student;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Selector;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox;

/**
 * Supervised code for the selector pipe tests: it opens a selector, which on
 * Windows makes the JDK build a pipe over a socket file, and it asks the
 * network check about a connect of its own.
 */
public final class SelectorPipeProbe {

	/** A utility class is not built. */
	private SelectorPipeProbe() {
	}

	/**
	 * Opens a selector and closes it again.
	 *
	 * @throws IOException if the selector cannot be opened
	 */
	public static void openSelector() throws IOException {
		Selector.open().close();
	}

	/**
	 * Asks the network check whether this code may connect a socket channel to the
	 * given address, as {@code SocketChannel.open(SocketAddress)} does.
	 *
	 * @param address the address to connect to
	 */
	public static void connectTo(SocketAddress address) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("connect",
				"java.nio.channels.SocketChannel", "open",
				"(Ljava/net/SocketAddress;)Ljava/nio/channels/SocketChannel;", null, new Object[] { address }, null);
	}
}
