package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.hostName.HostNameConnectMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.hostName.ListenMain;

/**
 * Checks that a policy naming a host lets through the addresses that name
 * resolves to, refuses an address that only carries the name, and governs the
 * ports supervised code listens on, in every mode. Nothing needs to listen on
 * the port: a refused connection that is not an Ares refusal shows the check
 * let it through.
 */
class NetworkEndpointMatchingTest {

	/** The port the policies allow on localhost. */
	private static final int PORT = 47123;

	/** Folder of the student code the static analysis inspects. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/hostName";

	/** A connection to localhost by name passes the name allowance. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsTheNamedHostMavenArchunitAspectJ() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByName(PORT));
	}

	/** The loopback address passes, as localhost resolves to it. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsItsAddressMavenArchunitAspectJ() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByLoopbackAddress(PORT));
	}

	/**
	 * An address carrying the name localhost without resolving to it is refused.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceRefusesAForgedNameMavenArchunitAspectJ() {
		assertThrows(SecurityException.class, () -> HostNameConnectMain.connectWithForgedName(PORT));
	}

	/** A connection to localhost by name passes the name allowance. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsTheNamedHostMavenArchunitInstrumentation() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByName(PORT));
	}

	/** The loopback address passes, as localhost resolves to it. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsItsAddressMavenArchunitInstrumentation() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByLoopbackAddress(PORT));
	}

	/**
	 * An address carrying the name localhost without resolving to it is refused.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceRefusesAForgedNameMavenArchunitInstrumentation() {
		assertThrows(SecurityException.class, () -> HostNameConnectMain.connectWithForgedName(PORT));
	}

	/** A connection to localhost by name passes the name allowance. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsTheNamedHostMavenWalaAspectJ() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByName(PORT));
	}

	/** The loopback address passes, as localhost resolves to it. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsItsAddressMavenWalaAspectJ() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByLoopbackAddress(PORT));
	}

	/**
	 * An address carrying the name localhost without resolving to it is refused.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceRefusesAForgedNameMavenWalaAspectJ() {
		assertThrows(SecurityException.class, () -> HostNameConnectMain.connectWithForgedName(PORT));
	}

	/** A connection to localhost by name passes the name allowance. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsTheNamedHostMavenWalaInstrumentation() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByName(PORT));
	}

	/** The loopback address passes, as localhost resolves to it. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceAdmitsItsAddressMavenWalaInstrumentation() {
		assertNotRefusedByAres(() -> HostNameConnectMain.connectByLoopbackAddress(PORT));
	}

	/**
	 * An address carrying the name localhost without resolving to it is refused.
	 */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_hostNameAllowanceRefusesAForgedNameMavenWalaInstrumentation() {
		assertThrows(SecurityException.class, () -> HostNameConnectMain.connectWithForgedName(PORT));
	}

	/** Listening on the allowed port of the loopback address is not refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnTheAllowedEndpointMavenArchunitAspectJ() {
		assertNotRefusedByAres(() -> ListenMain.listenOnLoopback(PORT));
	}

	/** Listening on every interface needs the host "*", so it is refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnEveryInterfaceIsRefusedMavenArchunitAspectJ() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnEveryInterface(PORT));
	}

	/** Listening on another port is refused, through a socket and a channel. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnAnotherPortIsRefusedMavenArchunitAspectJ() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnLoopback(PORT + 1));
		assertThrows(SecurityException.class, () -> ListenMain.bindChannelOnLoopback(PORT + 1));
	}

	/** A datagram socket on a port the system picks contacts no one and passes. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_datagramSocketOnAPickedPortPassesMavenArchunitAspectJ() {
		assertNotRefusedByAres(ListenMain::openDatagramSocket);
	}

	/** Listening on the allowed port of the loopback address is not refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnTheAllowedEndpointMavenArchunitInstrumentation() {
		assertNotRefusedByAres(() -> ListenMain.listenOnLoopback(PORT));
	}

	/** Listening on every interface needs the host "*", so it is refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnEveryInterfaceIsRefusedMavenArchunitInstrumentation() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnEveryInterface(PORT));
	}

	/** Listening on another port is refused, through a socket and a channel. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnAnotherPortIsRefusedMavenArchunitInstrumentation() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnLoopback(PORT + 1));
		assertThrows(SecurityException.class, () -> ListenMain.bindChannelOnLoopback(PORT + 1));
	}

	/** A datagram socket on a port the system picks contacts no one and passes. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_datagramSocketOnAPickedPortPassesMavenArchunitInstrumentation() {
		assertNotRefusedByAres(ListenMain::openDatagramSocket);
	}

	/** Listening on the allowed port of the loopback address is not refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnTheAllowedEndpointMavenWalaAspectJ() {
		assertNotRefusedByAres(() -> ListenMain.listenOnLoopback(PORT));
	}

	/** Listening on every interface needs the host "*", so it is refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnEveryInterfaceIsRefusedMavenWalaAspectJ() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnEveryInterface(PORT));
	}

	/** Listening on another port is refused, through a socket and a channel. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnAnotherPortIsRefusedMavenWalaAspectJ() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnLoopback(PORT + 1));
		assertThrows(SecurityException.class, () -> ListenMain.bindChannelOnLoopback(PORT + 1));
	}

	/** A datagram socket on a port the system picks contacts no one and passes. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_datagramSocketOnAPickedPortPassesMavenWalaAspectJ() {
		assertNotRefusedByAres(ListenMain::openDatagramSocket);
	}

	/** Listening on the allowed port of the loopback address is not refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnTheAllowedEndpointMavenWalaInstrumentation() {
		assertNotRefusedByAres(() -> ListenMain.listenOnLoopback(PORT));
	}

	/** Listening on every interface needs the host "*", so it is refused. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnEveryInterfaceIsRefusedMavenWalaInstrumentation() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnEveryInterface(PORT));
	}

	/** Listening on another port is refused, through a socket and a channel. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_listenOnAnotherPortIsRefusedMavenWalaInstrumentation() {
		assertThrows(SecurityException.class, () -> ListenMain.listenOnLoopback(PORT + 1));
		assertThrows(SecurityException.class, () -> ListenMain.bindChannelOnLoopback(PORT + 1));
	}

	/** A datagram socket on a port the system picks contacts no one and passes. */
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyLocalhostByName.yaml", withinPath = WITHIN_PATH)
	void test_datagramSocketOnAPickedPortPassesMavenWalaInstrumentation() {
		assertNotRefusedByAres(ListenMain::openDatagramSocket);
	}

	/**
	 * Runs a connection attempt and fails if Ares refused it. Any other outcome, a
	 * refused connection included, means Ares let it through.
	 *
	 * @param attempt the connection attempt
	 */
	private static void assertNotRefusedByAres(Executable attempt) {
		try {
			attempt.execute();
		} catch (Throwable outcome) {
			for (Throwable cause = outcome; cause != null; cause = cause.getCause()) {
				assertFalse(cause instanceof SecurityException, () -> "Ares refused an allowed connection: " + outcome);
			}
		}
	}
}
