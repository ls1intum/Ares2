package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpRequest;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation network system security advice.
 */
public final class JavaInstrumentationAdviceNetworkSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

	private JavaInstrumentationAdviceNetworkSystemToolbox() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationAdviceNetworkSystemToolbox"));
	}

	private record NetworkTarget(@Nullable String host, int port) {
		@Nonnull
		String toDisplayString() {
			String normalizedHost = host == null || host.isBlank() ? "<unknown>" : host;
			return normalizedHost + ":" + port;
		}
	}

	public static void checkNetworkSystemInteraction(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (!"INSTRUMENTATION".equals(aopMode)) {
			return;
		}

		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		@Nullable
		final String[] allowedClasses = getValueFromSettings("allowedListedClasses");

		@Nullable
		final String[] allowedHosts = switch (action) {
		case "connect" -> getValueFromSettings("hostsAllowedToBeConnectedTo");
		case "send" -> getValueFromSettings("hostsAllowedToBeSentTo");
		case "receive" -> getValueFromSettings("hostsAllowedToBeReceivedFrom");
		default -> throw new SecurityException(localize("security.advice.settings.invalid.network.permission", action));
		};
		@Nullable
		final int[] allowedPorts = switch (action) {
		case "connect" -> getValueFromSettings("portsAllowedToBeConnectedTo");
		case "send" -> getValueFromSettings("portsAllowedToBeSentTo");
		case "receive" -> getValueFromSettings("portsAllowedToBeReceivedFrom");
		default -> throw new SecurityException(localize("security.advice.settings.invalid.network.permission", action));
		};

		if ((allowedHosts == null ? 0 : allowedHosts.length) != (allowedPorts == null ? 0 : allowedPorts.length)) {
			throw new SecurityException(localize("security.advice.network.allowed.size", action,
					allowedHosts == null ? 0 : allowedHosts.length, allowedPorts == null ? 0 : allowedPorts.length));
		}

		@Nullable
		String networkSystemMethodToCheck = restrictedPackage == null ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (networkSystemMethodToCheck == null) {
			return;
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);

		@Nonnull
		String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
		@Nullable
		NetworkTarget target = extractTarget(declaringTypeName, methodName, parameters, attributes, instance);

		if (target == null) {
			throw new SecurityException(localize("security.advice.illegal.network.execution", networkSystemMethodToCheck,
					action, "<unresolved>",
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		if (isForbidden(target, allowedHosts, allowedPorts)) {
			throw new SecurityException(localize("security.advice.illegal.network.execution", networkSystemMethodToCheck,
					action, target.toDisplayString(),
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
	}

	private static boolean isForbidden(@Nonnull NetworkTarget target, @Nullable String[] allowedHosts,
			@Nullable int[] allowedPorts) {
		if (allowedHosts == null || allowedHosts.length == 0 || allowedPorts == null || allowedPorts.length == 0) {
			return true;
		}
		for (int i = 0; i < allowedHosts.length; i++) {
			String allowedHost = allowedHosts[i];
			int allowedPort = allowedPorts[i];
			if (hostMatches(target.host, allowedHost) && portMatches(target.port, allowedPort)) {
				return false;
			}
		}
		return true;
	}

	private static boolean hostMatches(@Nullable String actualHost, @Nullable String allowedHost) {
		if (allowedHost == null || allowedHost.isBlank()) {
			return false;
		}
		if ("*".equals(allowedHost)) {
			return true;
		}
		if (actualHost == null || actualHost.isBlank()) {
			return false;
		}
		String normalizedActual = actualHost.trim().toLowerCase(Locale.ROOT);
		String normalizedAllowed = allowedHost.trim().toLowerCase(Locale.ROOT);
		return normalizedActual.equals(normalizedAllowed) || normalizedActual.endsWith("." + normalizedAllowed);
	}

	private static boolean portMatches(int actualPort, int allowedPort) {
		if (allowedPort == -1) {
			return true;
		}
		return actualPort == allowedPort;
	}

	@Nullable
	private static NetworkTarget extractTarget(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nullable Object[] parameters, @Nullable Object[] attributes, @Nullable Object instance) {
		String key = declaringTypeName + "." + methodName;
		return switch (key) {
		case "java.net.Socket.connect", "java.nio.channels.SocketChannel.connect",
				"java.nio.channels.AsynchronousSocketChannel.connect", "java.nio.channels.DatagramChannel.connect" ->
				fromFirstParameter(parameters, instance, attributes);
		case "java.net.DatagramSocket.connect" -> fromDatagramSocketConnect(parameters, instance, attributes);
		case "java.net.DatagramSocket.send", "java.nio.channels.DatagramChannel.send" ->
				fromDatagramSend(parameters, instance, attributes);
		case "java.net.DatagramSocket.receive", "java.nio.channels.DatagramChannel.receive" ->
				fromReceiveSide(instance, parameters, attributes);
		case "java.net.http.HttpClient.send", "java.net.http.HttpClient.sendAsync" ->
				fromHttpRequest(parameters, instance, attributes);
		case "java.net.URL.openConnection", "java.net.URLConnection.connect", "java.net.URLConnection.getInputStream",
				"java.net.URLConnection.getOutputStream", "java.net.HttpURLConnection.connect",
				"java.net.HttpURLConnection.getInputStream", "java.net.HttpURLConnection.getOutputStream" ->
				fromUrlOrConnection(instance, parameters, attributes);
		case "java.net.Socket.getInputStream" -> fromSocketInstance(instance, parameters, attributes);
		case "java.net.Socket.<init>", "java.net.DatagramSocket.<init>" -> fromConstructorParameters(parameters);
		default -> firstResolvable(parameters, instance, attributes);
		};
	}

	@Nullable
	private static NetworkTarget fromConstructorParameters(@Nullable Object[] parameters) {
		if (parameters == null || parameters.length == 0) {
			return null;
		}
		if (parameters.length >= 2) {
			NetworkTarget fromHostPort = fromHostAndPort(parameters[0], parameters[1]);
			if (fromHostPort != null) {
				return fromHostPort;
			}
		}
		return fromFirstParameter(parameters, null, null);
	}

	@Nullable
	private static NetworkTarget fromHostAndPort(@Nullable Object hostCandidate, @Nullable Object portCandidate) {
		String host = null;
		if (hostCandidate instanceof String str) {
			host = str;
		} else if (hostCandidate instanceof InetAddress inetAddress) {
			host = inetAddress.getHostAddress();
		}
		if (host == null || !(portCandidate instanceof Integer port)) {
			return null;
		}
		return new NetworkTarget(host, port);
	}

	@Nullable
	private static NetworkTarget fromFirstParameter(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null && parameters.length > 0) {
			NetworkTarget target = toTarget(parameters[0]);
			if (target != null) {
				return target;
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromDatagramSocketConnect(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null && parameters.length >= 2) {
			NetworkTarget explicit = fromHostAndPort(parameters[0], parameters[1]);
			if (explicit != null) {
				return explicit;
			}
		}
		return fromFirstParameter(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromDatagramSend(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				if (parameter instanceof DatagramPacket datagramPacket) {
					String host = datagramPacket.getAddress() == null ? null : datagramPacket.getAddress().getHostAddress();
					return new NetworkTarget(host, datagramPacket.getPort());
				}
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromHttpRequest(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				if (parameter instanceof HttpRequest httpRequest) {
					return toTarget(httpRequest.uri());
				}
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromUrlOrConnection(@Nullable Object instance, @Nullable Object[] parameters,
			@Nullable Object[] attributes) {
		if (instance instanceof URL url) {
			return toTarget(url);
		}
		if (instance instanceof URLConnection urlConnection) {
			return toTarget(urlConnection.getURL());
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromSocketInstance(@Nullable Object instance, @Nullable Object[] parameters,
			@Nullable Object[] attributes) {
		if (instance instanceof Socket socket) {
			InetAddress remote = socket.getInetAddress();
			String host = remote == null ? null : remote.getHostAddress();
			return new NetworkTarget(host, socket.getPort());
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget fromReceiveSide(@Nullable Object instance, @Nullable Object[] parameters,
			@Nullable Object[] attributes) {
		if (instance instanceof DatagramSocket datagramSocket) {
			InetAddress remote = datagramSocket.getInetAddress();
			return new NetworkTarget(remote == null ? null : remote.getHostAddress(), datagramSocket.getPort());
		}
		if (instance instanceof DatagramChannel datagramChannel) {
			try {
				SocketAddress remoteAddress = datagramChannel.getRemoteAddress();
				NetworkTarget target = toTarget(remoteAddress);
				if (target != null) {
					return target;
				}
			} catch (Exception ignored) {
				// Best-effort extraction only.
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	@Nullable
	private static NetworkTarget firstResolvable(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				NetworkTarget target = toTarget(parameter);
				if (target != null) {
					return target;
				}
			}
		}
		NetworkTarget instanceTarget = toTarget(instance);
		if (instanceTarget != null) {
			return instanceTarget;
		}
		if (attributes != null) {
			for (Object attribute : attributes) {
				NetworkTarget target = toTarget(attribute);
				if (target != null) {
					return target;
				}
			}
		}
		return null;
	}

	@Nullable
	private static NetworkTarget toTarget(@Nullable Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof NetworkTarget target) {
			return target;
		}
		if (value instanceof InetSocketAddress inetSocketAddress) {
			String host = inetSocketAddress.getHostString();
			if (host == null || host.isBlank()) {
				InetAddress address = inetSocketAddress.getAddress();
				host = address == null ? null : address.getHostAddress();
			}
			return new NetworkTarget(host, inetSocketAddress.getPort());
		}
		if (value instanceof SocketAddress socketAddress) {
			String socketAddressAsString = socketAddress.toString();
			int delimiter = socketAddressAsString.lastIndexOf(':');
			if (delimiter > 0 && delimiter + 1 < socketAddressAsString.length()) {
				String host = socketAddressAsString.substring(0, delimiter).replace("/", "");
				try {
					int port = Integer.parseInt(socketAddressAsString.substring(delimiter + 1));
					return new NetworkTarget(host, port);
				} catch (NumberFormatException ignored) {
					return null;
				}
			}
			return null;
		}
		if (value instanceof HttpRequest httpRequest) {
			return toTarget(httpRequest.uri());
		}
		if (value instanceof URI uri) {
			return new NetworkTarget(uri.getHost(), effectivePort(uri.getPort(), uri.getScheme()));
		}
		if (value instanceof URL url) {
			int defaultPort = url.getDefaultPort();
			int port = url.getPort() >= 0 ? url.getPort() : defaultPort;
			return new NetworkTarget(url.getHost(), port >= 0 ? port : -1);
		}
		if (value instanceof URLConnection urlConnection) {
			return toTarget(urlConnection.getURL());
		}
		if (value instanceof Socket socket) {
			InetAddress inetAddress = socket.getInetAddress();
			String host = inetAddress == null ? null : inetAddress.getHostAddress();
			return new NetworkTarget(host, socket.getPort());
		}
		if (value instanceof DatagramSocket datagramSocket) {
			InetAddress inetAddress = datagramSocket.getInetAddress();
			String host = inetAddress == null ? null : inetAddress.getHostAddress();
			return new NetworkTarget(host, datagramSocket.getPort());
		}
		if (value instanceof SocketChannel socketChannel) {
			try {
				return toTarget(socketChannel.getRemoteAddress());
			} catch (Exception ignored) {
				return null;
			}
		}
		if (value instanceof DatagramChannel datagramChannel) {
			try {
				return toTarget(datagramChannel.getRemoteAddress());
			} catch (Exception ignored) {
				return null;
			}
		}
		if (value instanceof String str) {
			try {
				URI uri = new URI(str);
				if (uri.getHost() != null) {
					return toTarget(uri);
				}
			} catch (URISyntaxException ignored) {
				// no-op
			}
			int delimiter = str.lastIndexOf(':');
			if (delimiter > 0 && delimiter + 1 < str.length()) {
				String host = str.substring(0, delimiter);
				try {
					int port = Integer.parseInt(str.substring(delimiter + 1));
					return new NetworkTarget(host, port);
				} catch (NumberFormatException ignored) {
					return null;
				}
			}
		}
		return null;
	}

	private static int effectivePort(int explicitPort, @Nullable String scheme) {
		if (explicitPort >= 0) {
			return explicitPort;
		}
		if (scheme == null) {
			return -1;
		}
		return switch (scheme.toLowerCase(Locale.ROOT)) {
		case "http" -> 80;
		case "https" -> 443;
		default -> -1;
		};
	}
}


