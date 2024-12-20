package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.network;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Callable;

public final class NetworkPenguin {

	private NetworkPenguin() {
	}

	public static void tryStartServer(int port, String expectLine) throws Exception {
		try (ServerSocket socket = new ServerSocket(port)) {
			socket.setSoTimeout(2000);
			connectAndCheckSentLine(socket::accept, expectLine);
		}
	}

	public static void tryConnect(String host, int port, String expectLine) throws Exception {
		connectAndCheckSentLine(() -> new Socket(host, port), expectLine);
	}

	private static void connectAndCheckSentLine(Callable<Socket> socketSupplier, String expectLine) throws Exception {
		try (Socket s = socketSupplier.call(); Scanner in = new Scanner(s.getInputStream())) {
			if (expectLine != null) {
				s.setSoTimeout(200);
//				assertEquals(expectLine, in.nextLine());
			}
		}
	}

	public static int googleCode() throws IOException {
		URL url = new URL("http://google.com");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		return con.getResponseCode();
	}
}
