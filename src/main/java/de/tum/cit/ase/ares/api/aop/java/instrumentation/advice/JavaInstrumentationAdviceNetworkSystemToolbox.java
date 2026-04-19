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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation network system security advice.
 * <p>
 * Description: Provides static methods to enforce network system security
 * policies at runtime by checking network system interactions (connect, send,
 * receive) against allowed hosts and ports, call stack criteria, and variable
 * criteria. Uses reflection to interact with test case settings and
 * localization utilities. Designed to prevent unauthorized network system
 * operations during Java application execution, especially in test and
 * instrumentation scenarios.
 * <p>
 * Design Rationale: Centralizes network system security checks for Java
 * instrumentation advice, ensuring consistent enforcement of security policies.
 * Uses static utility methods and a private constructor to prevent
 * instantiation. Reflection is used to decouple the toolbox from direct
 * dependencies on settings and localization classes, supporting flexible and
 * dynamic test setups. Network targets are resolved to a canonical host-and-port
 * pair before comparison, which avoids the multi-level traversal required for
 * file-path or thread-class checks.
 *
 * @since 2.0.0
 * @author Kevin Fischer
 * @version 2.0.0
 */
public final class JavaInstrumentationAdviceNetworkSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

	// <editor-fold desc="Constants">

	/**
	 * Resolve the index of a named field within the given class.
	 * <p>
	 * Description: Returns the positional index of the first field whose name
	 * matches {@code fieldName}. Used to avoid hard-coding field indices that
	 * can shift across JDK versions.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private static int findFieldIndex(Class<?> clazz, String fieldName) {
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i].getName())) {
				return i;
			}
		}
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize("security.instrumentation.field.not.found", fieldName, clazz.getName())); //$NON-NLS-1$
	}

	/**
	 * Map of methods with attribute index exceptions for network system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during network system checks. For example, when
	 * checking a {@link java.net.Socket} instance, only the remote address fields
	 * are relevant for policy enforcement.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	@SuppressWarnings("null")
	private static final Map<String, IgnoreValues> NETWORK_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
			Map.entry("java.net.Socket.connect", IgnoreValues.allExcept(findFieldIndex(java.net.Socket.class, "impl"))), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("java.net.Socket.getInputStream", IgnoreValues.allExcept(findFieldIndex(java.net.Socket.class, "impl")))); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Map of methods with parameter index exceptions for network system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during network system checks. For methods like
	 * {@code Socket.connect(SocketAddress, int)}, only the first parameter (the
	 * remote address) should be checked, not the timeout. For HTTP client methods,
	 * only the {@link java.net.http.HttpRequest} parameter is relevant.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	@SuppressWarnings("null")
	private static final Map<String, IgnoreValues> NETWORK_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries(
			// Socket.connect(SocketAddress endpoint, int timeout) - only check endpoint (index 0)
			Map.entry("java.net.Socket.connect", IgnoreValues.allExcept(0)), //$NON-NLS-1$
			// DatagramSocket.send(DatagramPacket p) - only check the packet (index 0)
			Map.entry("java.net.DatagramSocket.send", IgnoreValues.allExcept(0)), //$NON-NLS-1$
			// DatagramChannel.send(ByteBuffer, SocketAddress) - only check address (index 1)
			Map.entry("java.nio.channels.DatagramChannel.send", IgnoreValues.allExcept(1)), //$NON-NLS-1$
			// HttpClient.send(HttpRequest, BodyHandler) - only check the request (index 0)
			Map.entry("java.net.http.HttpClient.send", IgnoreValues.allExcept(0)), //$NON-NLS-1$
			// HttpClient.sendAsync(HttpRequest, BodyHandler) - only check the request (index 0)
			Map.entry("java.net.http.HttpClient.sendAsync", IgnoreValues.allExcept(0))); //$NON-NLS-1$

	/**
	 * Internal value type representing a resolved network target.
	 * <p>
	 * Description: Pairs a nullable hostname string with an integer port number.
	 * Used throughout the toolbox as the canonical representation of a network
	 * endpoint, regardless of whether it originated from a {@link Socket}, a
	 * {@link URI}, a {@link HttpRequest}, or another network-capable object.
	 *
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private record NetworkTarget(@Nullable String host, int port) {

		/**
		 * Returns a human-readable string representation of this network target.
		 * <p>
		 * Description: Formats the host and port as {@code host:port}. If the host
		 * is {@code null} or blank, substitutes the placeholder {@code <unknown>}.
		 *
		 * @return non-null display string in the form {@code host:port}
		 * @since 2.0.0
		 * @author Kevin Fischer
		 */
		@Nonnull
		String toDisplayString() {
			String normalizedHost = host == null || host.isBlank() ? "<unknown>" : host;
			return normalizedHost + ":" + port;
		}
	}

	// </editor-fold>

	// <editor-fold desc="Constructor">

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * <p>
	 * Description: Throws a SecurityException if instantiation is attempted,
	 * enforcing the utility class pattern.
	 *
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private JavaInstrumentationAdviceNetworkSystemToolbox() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationAdviceNetworkSystemToolbox"));
	}

	// </editor-fold>

	// <editor-fold desc="Network system methods">

	// <editor-fold desc="Variable criteria methods">

	// <editor-fold desc="Forbidden handling">

	/**
	 * Checks if a resolved network target is outside the allowed hosts and ports
	 * whitelist.
	 * <p>
	 * Description: Returns {@code true} if {@code allowedHosts} or
	 * {@code allowedPorts} is null or empty, or if no entry in the parallel
	 * allowlists matches both the host and the port of {@code target}. Host
	 * matching is delegated to {@link #hostMatches} and port matching to
	 * {@link #portMatches}.
	 *
	 * @param target       the resolved network target to evaluate; must not be null
	 * @param allowedHosts parallel array of allowed hostname patterns; the wildcard
	 *                     {@code "*"} matches any host
	 * @param allowedPorts parallel array of allowed port numbers; {@code -1}
	 *                     matches any port
	 * @return {@code true} if the target is forbidden; {@code false} if it is
	 *         explicitly allowed
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static boolean checkIfNetworkIsForbidden(@Nonnull NetworkTarget target, @Nullable String[] allowedHosts,
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

	/**
	 * Checks whether an actual hostname matches an allowed hostname pattern.
	 * <p>
	 * Description: Returns {@code false} if either value is null or blank. Accepts
	 * the wildcard {@code "*"} as an unconditional match for any host. Otherwise
	 * performs a case-insensitive comparison after trimming, and also accepts suffix
	 * matches of the form {@code actualHost.endsWith("." + allowedHost)} to support
	 * subdomain policies.
	 *
	 * @param actualHost  the hostname resolved from the intercepted call; may be
	 *                    null
	 * @param allowedHost the hostname pattern from the security policy; may be null
	 * @return {@code true} if the actual host is covered by the allowed pattern
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Checks whether an actual port number matches an allowed port.
	 * <p>
	 * Description: A value of {@code -1} for {@code allowedPort} acts as a
	 * wildcard that permits any port number. Otherwise performs an exact integer
	 * equality check.
	 *
	 * @param actualPort  the port resolved from the intercepted call
	 * @param allowedPort the port from the security policy; {@code -1} means any
	 *                    port is permitted
	 * @return {@code true} if the actual port is permitted by the policy
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static boolean portMatches(int actualPort, int allowedPort) {
		if (allowedPort == -1) {
			return true;
		}
		return actualPort == allowedPort;
	}

	// </editor-fold>

	// <editor-fold desc="Conversion handling">

	/**
	 * Converts an arbitrary value to a {@link NetworkTarget} if possible.
	 * <p>
	 * Description: Inspects the runtime type of {@code value} and delegates to the
	 * appropriate extraction logic. Handles {@link InetSocketAddress},
	 * {@link SocketAddress}, {@link HttpRequest}, {@link URI}, {@link URL},
	 * {@link URLConnection}, {@link Socket}, {@link DatagramSocket},
	 * {@link SocketChannel}, {@link DatagramChannel}, and {@link String} values.
	 * Returns {@code null} when the value is {@code null} or of an unrecognised
	 * type.
	 *
	 * @param value the object to convert; may be null
	 * @return a {@link NetworkTarget} describing the endpoint, or {@code null} if
	 *         conversion is not possible
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget variableToTarget(@Nullable Object value) {
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
			return variableToTarget(httpRequest.uri());
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
			return variableToTarget(urlConnection.getURL());
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
				return variableToTarget(socketChannel.getRemoteAddress());
			} catch (Exception ignored) {
				return null;
			}
		}
		if (value instanceof DatagramChannel datagramChannel) {
			try {
				return variableToTarget(datagramChannel.getRemoteAddress());
			} catch (Exception ignored) {
				return null;
			}
		}
		if (value instanceof String str) {
			try {
				URI uri = new URI(str);
				if (uri.getHost() != null) {
					return variableToTarget(uri);
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

	/**
	 * Derives the effective port number for a URI.
	 * <p>
	 * Description: Returns {@code explicitPort} when it is non-negative. Otherwise
	 * falls back to the well-known default port for the scheme: {@code 80} for
	 * {@code http} and {@code 443} for {@code https}. Returns {@code -1} when no
	 * default applies.
	 *
	 * @param explicitPort the port parsed directly from the URI; {@code -1} when
	 *                     absent
	 * @param scheme       the URI scheme string; may be null
	 * @return the resolved port, or {@code -1} if unknown
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Dispatches to the most appropriate target extractor for the given method.
	 * <p>
	 * Description: Uses the fully qualified method name as a key to select the
	 * specialised extraction helper that best understands the semantics of each
	 * intercepted API. Falls back to {@link #firstResolvable} for methods that do
	 * not require dedicated handling.
	 *
	 * @param declaringTypeName the fully qualified class name of the intercepted
	 *                          method
	 * @param methodName        the name of the intercepted method
	 * @param parameters        the intercepted method arguments; may be null
	 * @param attributes        the instance fields captured by the advice; may be
	 *                          null
	 * @param instance          the receiver object of the intercepted call; may be
	 *                          null
	 * @return a resolved {@link NetworkTarget}, or {@code null} if extraction
	 *         failed
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Extracts a network target from socket or datagram-socket constructor
	 * parameters.
	 * <p>
	 * Description: Tries to read a host/port pair directly from the first two
	 * parameters (e.g. {@code Socket(String host, int port)}). Falls back to
	 * {@link #fromFirstParameter} when that attempt yields no result.
	 *
	 * @param parameters the constructor arguments; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Builds a {@link NetworkTarget} from an explicit host and port value pair.
	 * <p>
	 * Description: Accepts {@link String} or {@link InetAddress} as the host
	 * candidate and {@link Integer} as the port candidate. Returns {@code null}
	 * when either value cannot be resolved.
	 *
	 * @param hostCandidate the value that should represent the hostname; may be null
	 * @param portCandidate the value that should represent the port; may be null
	 * @return a {@link NetworkTarget}, or {@code null} if the inputs cannot be
	 *         resolved
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Extracts a network target from the first resolvable parameter.
	 * <p>
	 * Description: Attempts {@link #variableToTarget} on {@code parameters[0]}; if
	 * that fails or no parameters exist, delegates to {@link #firstResolvable}.
	 *
	 * @param parameters the intercepted method arguments; may be null
	 * @param instance   the receiver object; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget fromFirstParameter(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null && parameters.length > 0) {
			NetworkTarget target = variableToTarget(parameters[0]);
			if (target != null) {
				return target;
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	/**
	 * Extracts a network target for {@code DatagramSocket.connect} invocations.
	 * <p>
	 * Description: First attempts to build a target from two explicit parameters
	 * (host, port). Falls back to {@link #fromFirstParameter} when that fails.
	 *
	 * @param parameters the intercepted method arguments; may be null
	 * @param instance   the receiver object; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Extracts a network target from a datagram send operation.
	 * <p>
	 * Description: Searches {@code parameters} for a {@link DatagramPacket} whose
	 * destination address and port are set, and converts that to a
	 * {@link NetworkTarget}. Falls back to {@link #firstResolvable} when no usable
	 * packet is found (e.g. the packet has no destination, in which case the
	 * connected socket or channel determines the target).
	 *
	 * @param parameters the intercepted method arguments; may be null
	 * @param instance   the sender socket or channel instance; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget fromDatagramSend(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				if (parameter instanceof DatagramPacket datagramPacket) {
					InetAddress address = datagramPacket.getAddress();
					int port = datagramPacket.getPort();
					if (address != null && port > 0) {
						String host = address.getHostAddress();
						return new NetworkTarget(host, port);
					}
					// If the packet does not specify a usable destination, fall through
					// and let firstResolvable resolve the target from the connected socket/channel.
				}
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	/**
	 * Extracts a network target from an {@link HttpRequest} in the parameters.
	 * <p>
	 * Description: Searches {@code parameters} for an {@link HttpRequest} instance
	 * and extracts its URI via {@link #variableToTarget}. Falls back to
	 * {@link #firstResolvable} when none is found.
	 *
	 * @param parameters the intercepted method arguments; may be null
	 * @param instance   the HttpClient instance; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget fromHttpRequest(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				if (parameter instanceof HttpRequest httpRequest) {
					return variableToTarget(httpRequest.uri());
				}
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	/**
	 * Extracts a network target from a {@link URL} or {@link URLConnection}
	 * instance.
	 * <p>
	 * Description: Checks whether {@code instance} is a {@link URL} or a
	 * {@link URLConnection} and converts it via {@link #variableToTarget}. Falls
	 * back to {@link #firstResolvable} if neither type matches.
	 *
	 * @param instance   the receiver object; may be null
	 * @param parameters the intercepted method arguments; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget fromUrlOrConnection(@Nullable Object instance, @Nullable Object[] parameters,
			@Nullable Object[] attributes) {
		if (instance instanceof URL url) {
			return variableToTarget(url);
		}
		if (instance instanceof URLConnection urlConnection) {
			return variableToTarget(urlConnection.getURL());
		}
		return firstResolvable(parameters, instance, attributes);
	}

	/**
	 * Extracts a network target from a {@link Socket} instance.
	 * <p>
	 * Description: Reads the remote {@link InetAddress} and port directly from the
	 * socket. Falls back to {@link #firstResolvable} when {@code instance} is not a
	 * {@link Socket}.
	 *
	 * @param instance   the receiver object; may be null
	 * @param parameters the intercepted method arguments; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
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

	/**
	 * Extracts a network target from the receive side of a datagram socket or
	 * channel.
	 * <p>
	 * Description: For a connected {@link DatagramSocket}, reads the remote address
	 * directly. For a {@link DatagramChannel}, attempts to read the remote address
	 * via its API. Falls back to {@link #firstResolvable} when neither source
	 * yields a usable target. Unconnected datagram sockets are intentionally
	 * skipped because their {@code getInetAddress()} returns {@code null} and their
	 * port refers to the local endpoint, not the remote sender.
	 *
	 * @param instance   the receiver socket or channel; may be null
	 * @param parameters the intercepted method arguments; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return a resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget fromReceiveSide(@Nullable Object instance, @Nullable Object[] parameters,
			@Nullable Object[] attributes) {
		if (instance instanceof DatagramSocket datagramSocket) {
			// Only derive a remote target for connected DatagramSockets; for unconnected sockets
			// getInetAddress() is null and getPort() is the local port, not the remote sender.
			if (datagramSocket.isConnected()) {
				InetAddress remote = datagramSocket.getInetAddress();
				if (remote != null) {
					return new NetworkTarget(remote.getHostAddress(), datagramSocket.getPort());
				}
			}
		}
		if (instance instanceof DatagramChannel datagramChannel) {
			try {
				SocketAddress remoteAddress = datagramChannel.getRemoteAddress();
				NetworkTarget target = variableToTarget(remoteAddress);
				if (target != null) {
					return target;
				}
			} catch (Exception ignored) {
				// Best-effort extraction only.
			}
		}
		return firstResolvable(parameters, instance, attributes);
	}

	/**
	 * Attempts to resolve a {@link NetworkTarget} from parameters, then from the
	 * instance, then from attributes.
	 * <p>
	 * Description: Iterates each source in priority order, calling
	 * {@link #variableToTarget} on every element. Returns the first non-null result
	 * found; returns {@code null} if no element can be converted.
	 *
	 * @param parameters the intercepted method arguments; may be null
	 * @param instance   the receiver object; may be null
	 * @param attributes the captured instance fields; may be null
	 * @return the first resolved {@link NetworkTarget}, or {@code null}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static NetworkTarget firstResolvable(@Nullable Object[] parameters, @Nullable Object instance,
			@Nullable Object[] attributes) {
		if (parameters != null) {
			for (Object parameter : parameters) {
				NetworkTarget target = variableToTarget(parameter);
				if (target != null) {
					return target;
				}
			}
		}
		NetworkTarget instanceTarget = variableToTarget(instance);
		if (instanceTarget != null) {
			return instanceTarget;
		}
		if (attributes != null) {
			for (Object attribute : attributes) {
				NetworkTarget target = variableToTarget(attribute);
				if (target != null) {
					return target;
				}
			}
		}
		return null;
	}

	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Check methods">

	/**
	 * Validates a network system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (connect, send, receive)
	 * complies with allowed hosts, allowed ports, and call stack criteria. Throws
	 * {@link SecurityException} if a policy violation is detected or if the network
	 * target cannot be resolved from the intercepted call's parameters and
	 * attributes.
	 *
	 * @param action            the network system action being performed (connect,
	 *                          send, receive)
	 * @param declaringTypeName the fully qualified class name of the caller
	 * @param methodName        the name of the method invoked
	 * @param methodSignature   the method signature descriptor
	 * @param attributes        optional method attributes
	 * @param parameters        optional method parameters
	 * @param instance          the receiver object of the intercepted call; may be
	 *                          null
	 * @throws SecurityException if unauthorized access is detected or the target
	 *                           cannot be resolved
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	public static void checkNetworkSystemInteraction(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		// <editor-fold desc="Get information from settings">
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (aopMode == null || aopMode.isEmpty() || !aopMode.equals("INSTRUMENTATION")) {
			return;
		}
		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		if (restrictedPackage == null || restrictedPackage.isEmpty()) {
			return;
		}
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
		// </editor-fold>
		// <editor-fold desc="Get information from attributes">
		@Nonnull
		final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String networkSystemMethodToCheck = checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses,
				declaringTypeName, methodName);
		if (networkSystemMethodToCheck == null) {
			return;
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		// </editor-fold>
		// <editor-fold desc="Check target">
		@Nullable
		NetworkTarget target = extractTarget(declaringTypeName, methodName, parameters, attributes, instance);

		if (target == null) {
			throw new SecurityException(localize("security.advice.illegal.network.execution", networkSystemMethodToCheck,
					action, "<unresolved>",
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		if (checkIfNetworkIsForbidden(target, allowedHosts, allowedPorts)) {
			throw new SecurityException(localize("security.advice.illegal.network.execution", networkSystemMethodToCheck,
					action, target.toDisplayString(),
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
	}

	// </editor-fold>

	// </editor-fold>
}
