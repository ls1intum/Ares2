package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.network;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class NetworkPenguin {

	private NetworkPenguin() {
	}

	public static void tryConnect(String host, int port, String expectLine) throws Exception {
		try (Socket s = new Socket(host, port); Scanner in = new Scanner(s.getInputStream(), StandardCharsets.UTF_8)) {
			if (expectLine != null) {
				s.setSoTimeout(200);
				String actualLine = in.nextLine();
				if (!expectLine.equals(actualLine)) {
					throw new AssertionError("Expected '" + expectLine + "' but received '" + actualLine + "'");
				}
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
