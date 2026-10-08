package de.tum.cit.ase.ares.integration.jce;

import java.nio.file.Path;
import java.security.SecureRandom;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

import example.jce.JceCryptoSubject;

/**
 * Trusted child launcher that prepares a real policy before entering student
 * code.
 */
public final class JceForkProbe {

	/** Prevents instances of the launcher. */
	private JceForkProbe() {
	}

	/**
	 * Checks fresh default discovery and its static refusal before student entry.
	 */
	private static void verifyRestrictiveDefault() {
		var events = org.junit.platform.testkit.engine.EngineTestKit.engine("junit-jupiter")
				.selectors(org.junit.platform.engine.discovery.DiscoverySelectors
						.selectClass("de.tum.cit.ase.ares.integration.jce.JceRestrictiveDefaultUser"))
				.execute().testEvents();
		events.assertStatistics(statistics -> statistics.started(1).failed(1));
		Throwable failure = events.failed().list().get(0)
				.getPayload(org.junit.platform.engine.TestExecutionResult.class).orElseThrow().getThrowable()
				.orElseThrow();
		if (!(failure instanceof SecurityException) || !failure.getMessage().contains("access the file system")) {
			throw new AssertionError("An unrelated failure masked restrictive project discovery", failure);
		}
		System.out.println("JCE_RESTRICTIVE_DEFAULT_DENIED_BEFORE_CRYPTO");
	}

	/**
	 * Runs one isolated capture or crypto case and leaves ordering markers in its
	 * log.
	 */
	public static void main(String[] arguments) throws Exception {
		System.out.println("JCE_PROBE_ENTRY");
		if ("lifecycle".equals(arguments[0])) {
			verifyLifecycle();
			System.out.println("JCE_PROBE_PASSED");
			return;
		}
		if ("default".equals(arguments[0])) {
			verifyRestrictiveDefault();
			System.out.println("JCE_PROBE_PASSED");
			return;
		}
		if ("environment".equals(arguments[0])) {
			verifyEnvironmentBan(arguments);
			System.out.println("JCE_PROBE_PASSED");
			return;
		}
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(Path.of(arguments[3]))
				.securityPolicyFilePath(Path.of(arguments[1])).withinPath(Path.of(arguments[4])).build()
				.createTestCases().executeTestCases();
		System.out.println("JCE_PROBE_ARMED");
		if ("snapshot".equals(arguments[0])) {
			checkSnapshot(Path.of(arguments[2]));
		} else if ("boundary".equals(arguments[0])) {
			JceRuntimeContract.verify(Path.of(arguments[2]).getParent(), "de.tum.cit.ase.ares");
		} else {
			checkCrypto(Path.of(arguments[2]));
		}
		JavaInstrumentationAgent.throwIfTransformationFailed();
		System.out.println("JCE_PROBE_PASSED");
	}

	/**
	 * Requires the real static environment verdict without perturbing the capture
	 * fixture.
	 */
	private static void verifyEnvironmentBan(String[] arguments) throws Exception {
		String originalHome = System.getProperty("java.home");
		try {
			SecurityPolicyReaderAndDirector.builder().projectFolderPath(Path.of(arguments[3]))
					.securityPolicyFilePath(Path.of(arguments[1])).withinPath(Path.of(arguments[4])).build()
					.createTestCases().executeTestCases();
			throw new AssertionError("The static environment rule permitted System.setProperty");
		} catch (SecurityException denied) {
			if (!denied.getMessage().contains("environment") || !denied.getMessage().contains("setProperty")) {
				throw new AssertionError("A different static rule masked the environment attempt", denied);
			}
			if (!originalHome.equals(System.getProperty("java.home"))) {
				throw new AssertionError("Static analysis changed the trusted home");
			}
			System.out.println("JCE_ENVIRONMENT_ACCESS_DENIED");
		} finally {
			System.setProperty("java.home", originalHome);
		}
	}

	/**
	 * Supplies local test entropy without exposing an entropy device to student
	 * code.
	 */
	public static SecureRandom testRandom() {
		return new FixedRandom();
	}

	/**
	 * Verifies each Jupiter result and both settings copies after the whole
	 * lifecycle.
	 */
	private static void verifyLifecycle() throws Exception {
		var results = org.junit.platform.testkit.engine.EngineTestKit.engine("junit-jupiter").selectors(
				org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(JcePolicyLifecycleUser.class))
				.execute();
		var events = results.testEvents();
		events.debug();
		events.assertStatistics(statistics -> statistics.started(8).succeeded(5).failed(3));
		for (String name : new String[] { "unsupervisedReadSucceeds", "activePolicyAllowsCryptoAndRejectsRead",
				"deactivatedPolicyReadsAfterFailure", "reactivatedPolicyRejectsRead",
				"deactivatedPolicyReadsAfterPreparationFailure" }) {
			if (events.succeeded().list().stream()
					.noneMatch(event -> event.getTestDescriptor().getDisplayName().equals(name + "()"))) {
				throw new AssertionError("The lifecycle control did not succeed: " + name);
			}
		}
		for (var event : events.failed().list()) {
			Throwable failure = event.getPayload(org.junit.platform.engine.TestExecutionResult.class).orElseThrow()
					.getThrowable().orElseThrow();
			String name = event.getTestDescriptor().getDisplayName();
			boolean expected = switch (name) {
			case "failingActiveTestCleansUp()" -> failure instanceof AssertionError
					&& "Expected lifecycle failure".equals(failure.getMessage());
			case "absentPolicyUsesRestrictiveDiscovery()" -> failure instanceof SecurityException
					&& failure.getMessage().contains("access the file system");
			case "failedPreparationCleansUp()" -> failure instanceof SecurityException
					&& failure.getMessage().contains("missing-policy.yaml");
			default -> false;
			};
			if (!expected) {
				throw new AssertionError("An unrelated failure masked the lifecycle case: " + name, failure);
			}
		}
		for (ClassLoader loader : new ClassLoader[] { JceForkProbe.class.getClassLoader(), null }) {
			var settings = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false, loader);
			var mode = settings.getDeclaredField("aopMode");
			mode.setAccessible(true);
			if (mode.get(null) != null) {
				throw new AssertionError("Jupiter left an active policy after the lifecycle");
			}
		}
		System.out.println("JCE_LIFECYCLE_PASSED");
	}

	/**
	 * Changes the home only after normal policy preparation, before the student
	 * read.
	 */
	private static void checkSnapshot(Path file) throws Exception {
		String originalHome = System.getProperty("java.home");
		System.out.println("JCE_PROBE_PROPERTY_CHANGE");
		try {
			System.setProperty("java.home", file.getParent().toString());
			requireDeniedRead(file);
		} finally {
			System.setProperty("java.home", originalHome);
		}
	}

	/**
	 * Requires first and repeated crypto use, followed by a genuine forbidden read.
	 */
	private static void checkCrypto(Path file) throws Exception {
		SecureRandom random = new FixedRandom();
		System.out.println("JCE_PROBE_FIRST_CRYPTO");
		if (JceCryptoSubject.initialiseCrypto(random) < 128 || JceCryptoSubject.initialiseCrypto(random) < 128) {
			throw new AssertionError("AES jurisdiction policy was not available");
		}
		requireDeniedRead(file);
	}

	/**
	 * Requires a filesystem denial, rather than a missing fixture or another
	 * failure.
	 */
	private static void requireDeniedRead(Path file) throws Exception {
		try {
			String contents = JceCryptoSubject.read(file);
			throw new AssertionError("Unlisted policy-named file was returned: " + contents);
		} catch (SecurityException denied) {
			if (!denied.getMessage().contains(file.toAbsolutePath().toString())) {
				throw new AssertionError("The denial did not identify the attempted file", denied);
			}
			System.out.println("JCE_PROBE_READ_DENIED");
		}
	}

	/**
	 * Supplies trusted deterministic entropy to keep this fixture local to JCE
	 * policy reads.
	 */
	private static final class FixedRandom extends SecureRandom {

		/** Serialization identifier required by SecureRandom. */
		private static final long serialVersionUID = 1L;

		/** Fills requests without opening a student-accessible entropy source. */
		@Override
		public void nextBytes(byte[] bytes) {
			java.util.Arrays.fill(bytes, (byte) 1);
		}
	}
}
