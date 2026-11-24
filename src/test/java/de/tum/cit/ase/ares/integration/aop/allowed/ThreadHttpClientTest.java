package de.tum.cit.ase.ares.integration.aop.allowed;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.httpclient.HttpClientMain;

/**
 * Checks that the threads {@code java.net.http.HttpClient} starts for itself do
 * not count against the thread policy in either runtime mode, while the network
 * check and a student executor's threads still do. The request goes to the
 * external echo server; when it is absent, only Ares' verdict is checked.
 */
class ThreadHttpClientTest {

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The policy allowing the echo server's address, the same name in every mode
	 * folder.
	 */
	private static final String POLICY = "/PolicyHttpClientThreads.yaml";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/threadSystem/httpclient";

	/**
	 * The host the policy allows, the IPv4 loopback address.
	 */
	private static final String ALLOWED_HOST = InetAddress.getLoopbackAddress().getHostAddress();

	/**
	 * The port of the external echo server, which the policy allows.
	 */
	private static final int ALLOWED_PORT = 25565;

	/**
	 * A port the policy does not allow.
	 */
	private static final int FORBIDDEN_PORT = 25566;

	/**
	 * Finds the refusal of Ares on the way to a failure.
	 *
	 * @param thrown the failure
	 * @return the first {@link SecurityException} among its causes, or {@code null}
	 */
	private static SecurityException refusalOf(Throwable thrown) {
		for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
			if (cause instanceof SecurityException refusal) {
				return refusal;
			}
		}
		return null;
	}

	/**
	 * Asserts that Ares refuses the call, directly or as the cause of the failure,
	 * with a message naming what it refused.
	 *
	 * @param executable the call
	 * @param named      a part of the refusal message
	 */
	private static void assertRefused(Executable executable, String named) {
		Throwable thrown = Assertions.assertThrows(Throwable.class, executable);
		SecurityException refusal = refusalOf(thrown);
		Assertions.assertNotNull(refusal, () -> "expected a SecurityException, got " + thrown);
		Assertions.assertTrue(String.valueOf(refusal.getMessage()).contains(named), refusal::getMessage);
	}

	/**
	 * Asserts that Ares does not refuse the call. A failure of the request itself,
	 * such as an absent echo server or its echo not being an HTTP answer, is no
	 * refusal.
	 *
	 * @param executable the call
	 */
	private static void assertNotRefused(Executable executable) {
		try {
			executable.execute();
		} catch (IOException failure) {
			Assertions.assertNull(refusalOf(failure), () -> "Ares refused the request: " + failure);
		} catch (Throwable failure) {
			Assertions.fail("Ares refused the request: " + failure, failure);
		}
	}

	/**
	 * A request to the allowed address is not refused for the threads the client
	 * starts for itself in ArchunitAspectJ.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void clientThreadsAreNotCountedMavenArchunitAspectJ() throws InterruptedException {
		assertNotRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, ALLOWED_PORT));
	}

	/**
	 * A request to an address the policy does not name is still refused in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void forbiddenAddressIsRefusedMavenArchunitAspectJ() {
		assertRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, FORBIDDEN_PORT),
				String.valueOf(FORBIDDEN_PORT));
	}

	/**
	 * A thread a student executor starts for the client is still counted, and
	 * refused, in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void studentExecutorThreadIsRefusedMavenArchunitAspectJ() {
		assertRefused(() -> HttpClientMain.getWithStudentExecutor(ALLOWED_HOST, ALLOWED_PORT), "StudentExecutor");
	}

	/**
	 * A request to the allowed address is not refused for the threads the client
	 * starts for itself in ArchunitInstrumentation.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void clientThreadsAreNotCountedMavenArchunitInstrumentation() throws InterruptedException {
		assertNotRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, ALLOWED_PORT));
	}

	/**
	 * A request to an address the policy does not name is still refused in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void forbiddenAddressIsRefusedMavenArchunitInstrumentation() {
		assertRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, FORBIDDEN_PORT),
				String.valueOf(FORBIDDEN_PORT));
	}

	/**
	 * A thread a student executor starts for the client is still counted, and
	 * refused, in ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void studentExecutorThreadIsRefusedMavenArchunitInstrumentation() {
		assertRefused(() -> HttpClientMain.getWithStudentExecutor(ALLOWED_HOST, ALLOWED_PORT), "StudentExecutor");
	}

	/**
	 * A request to the allowed address is not refused for the threads the client
	 * starts for itself in WalaAspectJ.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void clientThreadsAreNotCountedMavenWalaAspectJ() throws InterruptedException {
		assertNotRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, ALLOWED_PORT));
	}

	/**
	 * A request to an address the policy does not name is still refused in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void forbiddenAddressIsRefusedMavenWalaAspectJ() {
		assertRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, FORBIDDEN_PORT),
				String.valueOf(FORBIDDEN_PORT));
	}

	/**
	 * A thread a student executor starts for the client is still counted, and
	 * refused, in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	void studentExecutorThreadIsRefusedMavenWalaAspectJ() {
		assertRefused(() -> HttpClientMain.getWithStudentExecutor(ALLOWED_HOST, ALLOWED_PORT), "StudentExecutor");
	}

	/**
	 * A request to the allowed address is not refused for the threads the client
	 * starts for itself in WalaInstrumentation.
	 *
	 * @throws InterruptedException if waiting is interrupted
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void clientThreadsAreNotCountedMavenWalaInstrumentation() throws InterruptedException {
		assertNotRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, ALLOWED_PORT));
	}

	/**
	 * A request to an address the policy does not name is still refused in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void forbiddenAddressIsRefusedMavenWalaInstrumentation() {
		assertRefused(() -> HttpClientMain.getWithOwnThreads(ALLOWED_HOST, FORBIDDEN_PORT),
				String.valueOf(FORBIDDEN_PORT));
	}

	/**
	 * A thread a student executor starts for the client is still counted, and
	 * refused, in WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_BASE + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	void studentExecutorThreadIsRefusedMavenWalaInstrumentation() {
		assertRefused(() -> HttpClientMain.getWithStudentExecutor(ALLOWED_HOST, ALLOWED_PORT), "StudentExecutor");
	}
}
