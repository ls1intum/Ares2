package de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.send.httpclient;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox;

/**
 * Deterministic test client whose overridden methods remain instrumentable
 * application code. This avoids starting the JDK HTTP client's internal
 * selector thread before the network policy can reject the operation.
 */
final class TestHttpClient extends HttpClient {

	@Override
	public Optional<CookieHandler> cookieHandler() {
		return Optional.empty();
	}

	@Override
	public Optional<Duration> connectTimeout() {
		return Optional.empty();
	}

	@Override
	public Redirect followRedirects() {
		return Redirect.NEVER;
	}

	@Override
	public Optional<ProxySelector> proxy() {
		return Optional.empty();
	}

	@Override
	public SSLContext sslContext() {
		return null;
	}

	@Override
	public SSLParameters sslParameters() {
		return new SSLParameters();
	}

	@Override
	public Optional<Authenticator> authenticator() {
		return Optional.empty();
	}

	@Override
	public Version version() {
		return Version.HTTP_1_1;
	}

	@Override
	public Optional<Executor> executor() {
		return Optional.empty();
	}

	@Override
	public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
			throws IOException, InterruptedException {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("send",
				TestHttpClient.class.getName(), "send", "TestHttpClient.send", new Object[0],
				new Object[] { request, responseBodyHandler }, this);
		throw new AssertionError("HttpClient.send was not intercepted");
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
			HttpResponse.BodyHandler<T> responseBodyHandler) {
		throw new AssertionError("HttpClient.sendAsync was not intercepted");
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
			HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
		throw new AssertionError("HttpClient.sendAsync was not intercepted");
	}
}
