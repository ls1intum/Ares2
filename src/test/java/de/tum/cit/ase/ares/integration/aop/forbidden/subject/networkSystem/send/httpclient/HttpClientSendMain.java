package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.send.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClientSendMain {

	private HttpClientSendMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Send a request via {@link HttpClient#send} to a host that the policy permits
	 * to SEND to but not to RECEIVE from.
	 * <p>
	 * In the variants under test the advice should intercept {@code send} before
	 * the real connection is attempted (the request targets a non-routable
	 * TEST-NET-3, RFC 5737 host), reproducing I-068: {@code send} both transmits
	 * the request AND returns the response body, so a policy granting only SEND
	 * must still block the call because RECEIVE was never independently checked
	 * before this fix. The short connect timeout is a last-resort guard in case the
	 * advice is not applied.
	 */
	public static HttpResponse<Void> sendToSendOnlyAllowedHost() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(50)).build();
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://203.0.113.1:80/")).GET().build();
		return client.send(request, HttpResponse.BodyHandlers.discarding());
	}
}
