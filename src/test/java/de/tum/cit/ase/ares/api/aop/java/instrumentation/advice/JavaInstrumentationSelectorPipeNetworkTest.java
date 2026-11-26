package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.SelectorPipeProbe;

/**
 * The network check lets the JDK connect the socket of its own selector pipe,
 * which it does on Windows for every selector, also an {@code HttpClient}'s,
 * while a connect the supervised code asks for itself is still decided by the
 * policy. The policy allows only one TCP address, as for an echo server.
 */
class JavaInstrumentationSelectorPipeNetworkTest {

	/** The package of the probe, standing in for student code. */
	private static final String STUDENT_PACKAGE = "example.student";

	/** Loads the messages the denials use, before any check can block that. */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.network.execution");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.file.execution");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.target.not.allowed.network");
	}

	/**
	 * Arms an instrumentation policy that allows connecting to one loopback port
	 * and nothing else.
	 *
	 * @throws ReflectiveOperationException if the settings cannot be reset
	 */
	@BeforeEach
	void armPolicyWithOneAllowedAddress() throws ReflectiveOperationException {
		inBothCopies("reset");
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", STUDENT_PACKAGE, "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeConnectedTo",
				new String[] { InetAddress.getLoopbackAddress().getHostAddress() }, "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeConnectedTo", new int[] { 25565 }, "ARCH",
				"INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeSentTo", new String[0], "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeSentTo", new int[0], "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeReceivedFrom", new String[0], "ARCH",
				"INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeReceivedFrom", new int[0], "ARCH",
				"INSTRUMENTATION");
	}

	/**
	 * Leaves nothing armed for the next test class.
	 *
	 * @throws ReflectiveOperationException if the settings cannot be reset
	 */
	@AfterEach
	void resetSettings() throws ReflectiveOperationException {
		inBothCopies("reset");
	}

	/**
	 * Runs a settings method in the copy the test sees and in the boot copy the
	 * instrumentation advice reads, which can be two classes.
	 *
	 * @param methodName the static method of the settings class
	 * @throws ReflectiveOperationException if the method cannot be run
	 */
	private static void inBothCopies(String methodName) throws ReflectiveOperationException {
		JavaAOPTestCaseSettings.class.getMethod(methodName).invoke(null);
		Class<?> bootCopy = Class.forName(JavaAOPTestCaseSettings.class.getName(), false, null);
		bootCopy.getMethod(methodName).invoke(null);
	}

	/**
	 * On Windows, opening a selector connects, and later deletes, the socket file
	 * of the JDK's pipe; under this policy both pass.
	 */
	@Test
	@EnabledOnOs(OS.WINDOWS)
	void selectorPipeOfTheJdkPasses() {
		assertDoesNotThrow(SelectorPipeProbe::openSelector);
	}

	/**
	 * A connect to a socket file the supervised code asks for itself is still
	 * refused, as no JDK pipe is on the way.
	 *
	 * @param tempDir the folder of the socket file
	 */
	@Test
	void connectOfTheStudentToASocketFileIsRefused(@TempDir Path tempDir) {
		UnixDomainSocketAddress socketFile = UnixDomainSocketAddress.of(tempDir.resolve("socket_1"));
		SecurityException refusal = assertThrows(SecurityException.class,
				() -> SelectorPipeProbe.connectTo(socketFile));
		assertTrue(refusal.getMessage().contains("socket_1"), refusal.getMessage());
	}
}
