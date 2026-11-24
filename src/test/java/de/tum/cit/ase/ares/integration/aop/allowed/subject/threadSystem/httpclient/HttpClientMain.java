package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Student code that sends one request with {@code java.net.http.HttpClient}.
 */
public final class HttpClientMain {

	/**
	 * How long one request may take, in seconds.
	 */
	private static final long TIMEOUT_SECONDS = 5;

	/**
	 * A utility class is not built.
	 */
	private HttpClientMain() {
	}

	/**
	 * Sends one request with a client that uses its own threads.
	 *
	 * @param host the host to ask
	 * @param port the port to ask
	 * @return the status code of the answer
	 * @throws IOException          if the request fails
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static int getWithOwnThreads(String host, int port) throws IOException, InterruptedException {
		return get(HttpClient.newHttpClient(), host, port);
	}

	/**
	 * Sends one request without waiting in the call, so the client connects on a
	 * thread of its own pool, and then waits for the answer.
	 *
	 * @param host the host to ask
	 * @param port the port to ask
	 * @return the status code of the answer
	 * @throws ExecutionException   if the request fails
	 * @throws TimeoutException     if the answer takes too long
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static int getAsyncWithOwnThreads(String host, int port)
			throws ExecutionException, TimeoutException, InterruptedException {
		return HttpClient.newHttpClient().sendAsync(request(host, port), HttpResponse.BodyHandlers.discarding())
				.get(TIMEOUT_SECONDS, TimeUnit.SECONDS).statusCode();
	}

	/**
	 * Builds clients without sending anything.
	 *
	 * @param clients how many clients to build
	 * @return how many were built
	 */
	public static int buildClients(int clients) {
		int built = 0;
		for (int i = 0; i < clients; i++) {
			HttpClient.newBuilder().build();
			built++;
		}
		return built;
	}

	/**
	 * Sends one request with a client that runs its tasks on a student executor.
	 *
	 * @param host the host to ask
	 * @param port the port to ask
	 * @return the status code of the answer
	 * @throws IOException          if the request fails
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static int getWithStudentExecutor(String host, int port) throws IOException, InterruptedException {
		return get(HttpClient.newBuilder().executor(new StudentExecutor()).build(), host, port);
	}

	/**
	 * Sends one GET request.
	 *
	 * @param client the client to send it with
	 * @param host   the host to ask
	 * @param port   the port to ask
	 * @return the status code of the answer
	 * @throws IOException          if the request fails
	 * @throws InterruptedException if waiting is interrupted
	 */
	private static int get(HttpClient client, String host, int port) throws IOException, InterruptedException {
		return client.send(request(host, port), HttpResponse.BodyHandlers.discarding()).statusCode();
	}

	/**
	 * Builds one GET request.
	 *
	 * @param host the host to ask
	 * @param port the port to ask
	 * @return the request
	 */
	private static HttpRequest request(String host, int port) {
		return HttpRequest.newBuilder(URI.create("http://" + host + ":" + port + "/"))
				.timeout(Duration.ofSeconds(TIMEOUT_SECONDS)).GET().build();
	}
}
