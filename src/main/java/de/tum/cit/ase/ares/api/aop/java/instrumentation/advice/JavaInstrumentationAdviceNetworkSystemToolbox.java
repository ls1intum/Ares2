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
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
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
	 * Map of methods with attribute index exceptions for network system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during network system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> NETWORK_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries();

	/**
	 * Map of methods with parameter index exceptions for network system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during network system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> NETWORK_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();

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
	 * @param target       the resolved network target to evaluate; may be null
	 * @param allowedHosts parallel array of allowed hostname patterns; the wildcard
	 *                     {@code "*"} matches any host
	 * @param allowedPorts parallel array of allowed port numbers; {@code -1}
	 *                     matches any port
	 * @return {@code true} if the target is forbidden; {@code false} if it is
	 *         explicitly allowed
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static boolean checkIfNetworkIsForbidden(@Nullable NetworkTarget target, @Nullable String[] allowedHosts,
			@Nullable int[] allowedPorts) {
		if (target == null) {
			return false;
		}
		if (allowedHosts == null || allowedHosts.length == 0 || allowedPorts == null || allowedPorts.length == 0) {
			return true;
		}
		for (int i = 0; i < allowedHosts.length; i++) {
			String allowedHost = allowedHosts[i];
			int allowedPort = allowedPorts[i];
			if (hostMatches(target.host(), allowedHost) && portMatches(target.port(), allowedPort)) {
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
	 * {@link SocketAddress}, {@code java.net.http.HttpRequest}, {@link URI}, {@link URL},
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
		// HttpRequest is in the java.net.http module which may not be visible from
		// the bootstrap class-loader. Use reflection to avoid a hard dependency.
		try {
			Class<?> httpRequestClass = Class.forName("java.net.http.HttpRequest", false, null);
			if (httpRequestClass.isInstance(value)) {
				Object uri = httpRequestClass.getMethod("uri").invoke(value);
				return variableToTarget(uri);
			}
		} catch (ClassNotFoundException ignored) {
			// java.net.http module not available — skip
		} catch (Exception ignored) {
			// reflection failure — skip
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
		if (value instanceof DatagramPacket datagramPacket) {
			InetAddress address = datagramPacket.getAddress();
			if (address != null && datagramPacket.getPort() > 0) {
				return new NetworkTarget(address.getHostAddress(), datagramPacket.getPort());
			}
			return null;
		}
		if (value instanceof Socket socket) {
			return extractSocketTarget(socket);
		}
		if (value instanceof DatagramSocket datagramSocket) {
			return extractDatagramSocketTarget(datagramSocket);
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
		if (value instanceof InetAddress inetAddress) {
			return new NetworkTarget(inetAddress.getHostAddress(), -1);
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
	 * Backwards-compatible alias retained for existing reflective tests.
	 *
	 * @param value the object to convert
	 * @return the resolved network target, or {@code null}
	 */
	@Nullable
	private static NetworkTarget toTarget(@Nullable Object value) {
		return variableToTarget(value);
	}

	/**
	 * Resolves a network target from a full intercepted parameter list.
	 * <p>
	 * Description: Supports APIs that split host and port across separate
	 * arguments, such as {@code new Socket(String, int)} and
	 * {@code new Socket(InetAddress, int)}. The first directly resolvable target
	 * wins.
	 *
	 * @param parameters the intercepted argument array
	 * @return the resolved target, or {@code null} if no target can be derived
	 */
	@Nullable
	private static NetworkTarget parametersToTarget(@Nonnull Object[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			NetworkTarget directTarget = variableToTarget(parameters[i]);
			if (directTarget != null) {
				return directTarget;
			}
			if (i + 1 < parameters.length) {
				NetworkTarget pairTarget = hostAndPortToTarget(parameters[i], parameters[i + 1]);
				if (pairTarget != null) {
					return pairTarget;
				}
			}
		}
		return null;
	}

	/**
	 * Resolves a host-and-port argument pair to a network target.
	 *
	 * @param hostCandidate candidate host argument
	 * @param portCandidate candidate port argument
	 * @return the resolved target, or {@code null} if the pair is unsupported
	 */
	@Nullable
	private static NetworkTarget hostAndPortToTarget(@Nullable Object hostCandidate,
			@Nullable Object portCandidate) {
		Integer port = extractPort(portCandidate);
		if (port == null) {
			return null;
		}
		if (hostCandidate instanceof String host && !host.isBlank()) {
			return new NetworkTarget(host, port);
		}
		if (hostCandidate instanceof InetAddress inetAddress) {
			return new NetworkTarget(inetAddress.getHostAddress(), port);
		}
		return null;
	}

	/**
	 * Extracts a valid TCP/UDP port from an intercepted argument.
	 *
	 * @param value the candidate port value
	 * @return the port number, or {@code null} if the value is not a valid port
	 */
	@Nullable
	private static Integer extractPort(@Nullable Object value) {
		if (!(value instanceof Number number)) {
			return null;
		}
		int port = number.intValue();
		if (port < 0 || port > 65_535) {
			return null;
		}
		return port;
	}

	/**
	 * Resolves a connected socket to a network target.
	 * <p>
	 * Description: Prefers the remote socket address because it preserves the
	 * target that is about to be contacted. Returns {@code null} for unresolved
	 * sockets so that constructor-time receiver objects do not become
	 * {@code <unknown>:0} false positives.
	 *
	 * @param socket the socket to inspect
	 * @return the resolved target, or {@code null} if the socket is not yet bound
	 *         to a remote endpoint
	 */
	@Nullable
	private static NetworkTarget extractSocketTarget(@Nonnull Socket socket) {
		SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
		if (remoteSocketAddress != null) {
			NetworkTarget remoteTarget = variableToTarget(remoteSocketAddress);
			if (remoteTarget != null) {
				return remoteTarget;
			}
		}
		InetAddress inetAddress = socket.getInetAddress();
		int port = socket.getPort();
		if (inetAddress == null || port <= 0) {
			return null;
		}
		return new NetworkTarget(inetAddress.getHostAddress(), port);
	}

	/**
	 * Resolves a connected datagram socket to a network target.
	 *
	 * @param datagramSocket the datagram socket to inspect
	 * @return the resolved target, or {@code null} if the socket is not connected
	 */
	@Nullable
	private static NetworkTarget extractDatagramSocketTarget(@Nonnull DatagramSocket datagramSocket) {
		SocketAddress remoteSocketAddress = datagramSocket.getRemoteSocketAddress();
		if (remoteSocketAddress != null) {
			NetworkTarget remoteTarget = variableToTarget(remoteSocketAddress);
			if (remoteTarget != null) {
				return remoteTarget;
			}
		}
		InetAddress inetAddress = datagramSocket.getInetAddress();
		int port = datagramSocket.getPort();
		if (inetAddress == null || port <= 0) {
			return null;
		}
		return new NetworkTarget(inetAddress.getHostAddress(), port);
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

	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Violation analysis">

	/**
	 * Analyzes a variable to determine if it violates allowed network targets.
	 * <p>
	 * Description: Attempts to resolve the variable to a {@link NetworkTarget}
	 * via {@link #variableToTarget}. Returns {@code true} if the resolved target
	 * is forbidden according to the allowed hosts and ports whitelist.
	 *
	 * @param observedVariable the variable to analyze
	 * @param allowedHosts     whitelist of allowed hosts
	 * @param allowedPorts     whitelist of allowed ports
	 * @return true if a violation is found, false otherwise
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedHosts,
			@Nullable int[] allowedPorts) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return false;
		}
		NetworkTarget target = variableToTarget(observedVariable);
		return target != null && checkIfNetworkIsForbidden(target, allowedHosts, allowedPorts);
	}

	/**
	 * Extracts the display string of the first violating network target from a
	 * variable.
	 * <p>
	 * Description: Resolves the variable to a {@link NetworkTarget} and returns
	 * its display string if the target is forbidden. Returns {@code null} if no
	 * violation is found.
	 *
	 * @param observedVariable the variable to inspect
	 * @param allowedHosts     the hosts that are allowed
	 * @param allowedPorts     the ports that are allowed
	 * @return the violating target as a display string, or null if none found
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nullable
	private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedHosts,
			@Nullable int[] allowedPorts) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return null;
		}
		NetworkTarget target = variableToTarget(observedVariable);
		if (target != null && checkIfNetworkIsForbidden(target, allowedHosts, allowedPorts)) {
			return target.toDisplayString();
		}
		return null;
	}

	/**
	 * Checks an array of observedVariables against the allowed network targets
	 * whitelist.
	 * <p>
	 * Description: Iterates through the filtered observedVariables (excluding those
	 * matching ignoreVariables). For each non-null variable, attempts to resolve it
	 * to a {@link NetworkTarget} and checks whether it is forbidden. The first
	 * violating target found is returned.
	 *
	 * @param observedVariables array of values to validate
	 * @param allowedHosts      whitelist of allowed hosts
	 * @param allowedPorts      whitelist of allowed ports
	 * @param ignoreVariables   criteria determining which observedVariables to skip
	 * @return the first violating target (as display string) or null if none violate
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static String checkIfVariableCriteriaIsViolated(@Nonnull Object[] observedVariables,
			@Nullable String[] allowedHosts, @Nullable int[] allowedPorts,
			@Nonnull IgnoreValues ignoreVariables) {
		for (@Nullable
		Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
			if (analyseViolation(observedVariable, allowedHosts, allowedPorts)) {
				return extractViolationPath(observedVariable, allowedHosts, allowedPorts);
			}
		}
		return null;
	}

	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Action derivation">

	/**
	 * Derives the list of network actions that need to be validated for a given
	 * invocation.
	 * <p>
	 * Description: Currently returns a singleton list containing the original
	 * action because network operations (connect, send, receive) do not
	 * decompose into sub-actions the way file-system operations can. The method
	 * exists to maintain structural symmetry with the file-system toolbox and to
	 * provide a natural extension point should future network actions require
	 * derivation.
	 *
	 * @param defaultAction the network action associated with the pointcut
	 *                      configuration (e.g., {@code connect})
	 * @return ordered list of action/allow-non-existing pairs to validate
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static List<Map.Entry<String, Boolean>> deriveActionChecks(@Nonnull String defaultAction) {
		return Collections.singletonList(Map.entry(defaultAction, false));
	}

	// </editor-fold>

	// <editor-fold desc="Check methods">

	/**
	 * Performs the security validation for a single network action.
	 * <p>
	 * Description: Resolves the allowed hosts and ports for the given action,
	 * then evaluates method parameters, the receiver instance, and instance
	 * attributes against the whitelist. Throws {@link SecurityException} if a
	 * policy violation is detected.
	 *
	 * @param action                              the concrete network action under
	 *                                            inspection
	 * @param declaringTypeName                   fully qualified declaring type name
	 * @param methodName                          method being intercepted
	 * @param methodSignature                     JVM method signature
	 * @param attributes                          instance attributes (if any)
	 * @param parameters                          intercepted method arguments
	 * @param instance                            instance on which the method is
	 *                                            invoked
	 * @param restrictedPackage                   package prefix under security
	 *                                            scrutiny; reserved for structural
	 *                                            symmetry with the file-system
	 *                                            toolbox; currently unused
	 * @param allowedClasses                      classes allowed within the
	 *                                            restricted package; reserved for
	 *                                            structural symmetry with the
	 *                                            file-system toolbox; currently
	 *                                            unused
	 * @param networkSystemMethodToCheck          offending method discovered in the
	 *                                   restricted call stack
	 * @param studentCalledMethod     external method initiating the restricted
	 *                                call (may be null)
	 * @param fullMethodSignature     human-readable method signature for diagnostics
	 * @throws SecurityException if the interaction violates configured policies
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	private static void checkNetworkSystemInteractionForAction(@Nonnull String action,
			@Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance, @Nullable String restrictedPackage,
			@Nullable String[] allowedClasses, @Nonnull String networkSystemMethodToCheck,
			@Nullable String studentCalledMethod, @Nonnull String fullMethodSignature) {
		// <editor-fold desc="Resolve allowed hosts and ports">
		@Nullable
		final String[] allowedHosts = switch (action) {
		case "connect" -> getValueFromSettings("hostsAllowedToBeConnectedTo");
		case "send" -> getValueFromSettings("hostsAllowedToBeSentTo");
		case "receive" -> getValueFromSettings("hostsAllowedToBeReceivedFrom");
		default -> throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.settings.invalid.network.permission", action));
		};
		@Nullable
		final int[] allowedPorts = switch (action) {
		case "connect" -> getValueFromSettings("portsAllowedToBeConnectedTo");
		case "send" -> getValueFromSettings("portsAllowedToBeSentTo");
		case "receive" -> getValueFromSettings("portsAllowedToBeReceivedFrom");
		default -> throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.settings.invalid.network.permission", action));
		};

		if ((allowedHosts == null ? 0 : allowedHosts.length) != (allowedPorts == null ? 0 : allowedPorts.length)) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.network.allowed.size", action,
					allowedHosts == null ? 0 : allowedHosts.length, allowedPorts == null ? 0 : allowedPorts.length));
		}
		// </editor-fold>
		// <editor-fold desc="Check parameters">
		@Nullable
		NetworkTarget targetFromParameters = (parameters == null || parameters.length == 0) ? null
				: parametersToTarget(parameters);
		if (targetFromParameters != null && checkIfNetworkIsForbidden(targetFromParameters, allowedHosts, allowedPorts)) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.network.execution", networkSystemMethodToCheck, action,
					targetFromParameters.toDisplayString(), fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		@Nullable
		String networkIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(parameters, allowedHosts, allowedPorts,
						NETWORK_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(declaringTypeName + "." + methodName,
								IgnoreValues.NONE));
		if (networkIllegallyInteractedThroughParameter != null) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.network.execution", networkSystemMethodToCheck, action,
					networkIllegallyInteractedThroughParameter, fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
		// <editor-fold desc="Check receiver instance">
		@Nullable
		String networkIllegallyInteractedThroughReceiver = instance == null ? null
				: checkIfVariableCriteriaIsViolated(new Object[] { instance }, allowedHosts, allowedPorts,
						IgnoreValues.NONE);
		if (networkIllegallyInteractedThroughReceiver != null) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.network.execution", networkSystemMethodToCheck, action,
					networkIllegallyInteractedThroughReceiver, fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
		// <editor-fold desc="Check attributes">
		@Nullable
		String networkIllegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(attributes, allowedHosts, allowedPorts,
						NETWORK_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName,
								IgnoreValues.NONE));
		if (networkIllegallyInteractedThroughAttribute != null) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.network.execution", networkSystemMethodToCheck, action,
					networkIllegallyInteractedThroughAttribute, fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
	}

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
		// </editor-fold>
		// <editor-fold desc="Get information from attributes">
		@Nonnull
		final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String networkSystemMethodToCheck = (restrictedPackage == null) ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (networkSystemMethodToCheck == null) {
			return;
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		// </editor-fold>

		List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action);
		for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
			checkNetworkSystemInteractionForAction(actionCheck.getKey(),
					declaringTypeName, methodName, methodSignature, attributes, parameters, instance,
					restrictedPackage, allowedClasses, networkSystemMethodToCheck, studentCalledMethod,
					fullMethodSignature);
		}
	}

	// </editor-fold>

	// </editor-fold>
}
