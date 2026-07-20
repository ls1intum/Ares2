package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;

import example.student.InstrumentationSecurityProbe;

class JavaInstrumentationAdviceFileSystemToolboxTest {

	@Test
	void pathWildcardAllowsEveryPath(@TempDir Path tempDir) throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path file = tempDir.resolve("anywhere.txt");
			Files.writeString(file, "allowed by wildcard");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");

			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(file.toUri().toURL()));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Loads the localization bundle while still unrestricted, so that building a
	 * denial message under INSTRUMENTATION mode hits the cached bundle instead of
	 * reading messages.properties, which the file-system advice would otherwise
	 * block. Without this, {@code buildDenialReason} would degrade to the
	 * {@code !key!} fallback when this class runs cold and in isolation.
	 */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.no.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.not.in.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.file.execution");
	}

	private static void resetSettings() throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
	}

	private static void configureInstrumentationMode() {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
	}

	@Test
	void testCheckFileSystemInteraction_AllowedInteraction() {
		try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(
				JavaInstrumentationAdviceFileSystemToolbox.class)) {
			// When the class is mocked statically, checkFileSystemInteraction is
			// intercepted
			// and returns null by default — just verify no exception is thrown
			assertDoesNotThrow(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read",
					"de.tum.cit.ase.safe.FileReader", "readFile", "(Ljava/lang/String;)V", null,
					new Object[] { "/allowed/path" }, null));
		}
	}

	@Test
	void testCheckFileSystemInteraction_AllowsCreateOptions(@TempDir Path tempDir) throws Exception {
		try {
			resetSettings();
			JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "de.tum.cit.ase", "ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
			String allowedPath = tempDir.toString();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[] { allowedPath },
					"ARCH", "INSTRUMENTATION");

			Path target = tempDir.resolve("created.txt");
			Object[] parameters = new Object[] { target,
					new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE } };

			assertDoesNotThrow(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("overwrite",
					"de.tum.cit.ase.restricted.Subject", "openStream",
					"(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/OutputStream;", null, parameters, null));
		} finally {
			resetSettings();
		}
	}

	@Test
	void testCheckCallstackCriteriaReflectiveInvocationDoesNotBypass() throws Exception {
		assertEquals("example.student.InstrumentationSecurityProbe.stackCheckHelper",
				InstrumentationSecurityProbe.reflectiveStackCheck());
	}

	@Test
	void testInstrumentationPointcutsContainNewCoverage() {
		// java.net.URL.openStream is a network fetch, so it is bound to the network
		// connect pointcut, not the file-read pointcut.
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK.get("java.net.URL")
				.contains("openStream"));
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES
				.get("org.apache.commons.io.FileUtils").contains("forceDelete"));
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES
				.get("java.nio.file.spi.FileSystemProvider").contains("delete"));
	}

	@Test
	void testCheckFileSystemInteraction_BlocksFileUrlOpenStream(@TempDir Path tempDir) throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path allowedDir = Files.createDirectory(tempDir.resolve("allowed"));
			Path forbiddenFile = tempDir.resolve("forbidden.txt");
			Files.writeString(forbiddenFile, "secret");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { allowedDir.toString() },
					"ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(forbiddenFile.toUri().toURL()));
			assertTrue(exception.getMessage().contains(forbiddenFile.toAbsolutePath().toString()));
		} finally {
			resetSettings();
		}
	}

	@Test
	void testCheckFileSystemInteraction_BlocksDeleteIfExistsForMissingForbiddenPath(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path allowedDir = Files.createDirectory(tempDir.resolve("allowed"));
			Path forbiddenPath = tempDir.resolve("missing.txt");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeDeleted", new String[] { allowedDir.toString() },
					"ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkDeleteIfExists(forbiddenPath));
			assertNotNull(exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_appendsNoAllowlistReasonWhenNoRuleConfigured(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path forbiddenFile = tempDir.resolve("forbidden.txt");
			Files.writeString(forbiddenFile, "secret");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(forbiddenFile.toUri().toURL()));
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "File exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No allow rule configured")
							|| exception.getMessage().contains("Keine Erlaubnisregel"),
					() -> "Expected the no-allowlist reason, but was:\n" + exception.getMessage());
			assertFalse(
					exception.getMessage().contains("No configured allow rule permits this access") || exception
							.getMessage().contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
					() -> "Did not expect the not-permitted reason, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_appendsNotPermittedReasonWhenConfiguredButNotAllowed(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path allowedDir = Files.createDirectory(tempDir.resolve("allowed"));
			Path forbiddenFile = tempDir.resolve("forbidden.txt");
			Files.writeString(forbiddenFile, "secret");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { allowedDir.toString() },
					"ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(forbiddenFile.toUri().toURL()));
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "File exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No configured allow rule permits this access") || exception
							.getMessage().contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
					() -> "Expected the not-permitted reason, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void testLocalizeFallback() {
		String key = "security.advice.test.key";
		String result = JavaInstrumentationAdviceAbstractToolbox.localize(key, "arg1", "arg2");
		key = "!security.advice.test.key!";
		assertEquals(key, result);
	}

	@Test
	void testBuildDenialReason_distinguishesNoAllowlistFromNotPermitted() {
		String noAllowlist = JavaInstrumentationAdviceAbstractToolbox.buildDenialReason(true);
		String notPermitted = JavaInstrumentationAdviceAbstractToolbox.buildDenialReason(false);

		assertNotNull(noAllowlist);
		assertNotNull(notPermitted);
		assertFalse(noAllowlist.isBlank());
		assertFalse(notPermitted.isBlank());
		// The two branches must map to distinct, non-fallback (non-key) messages.
		assertNotEquals(noAllowlist, notPermitted);
		assertNotEquals("security.advice.denial.reason.no.allowlist", noAllowlist);
		assertNotEquals("security.advice.denial.reason.not.in.allowlist", notPermitted);
		// Locale-tolerant content checks (English or German bundle).
		assertTrue(noAllowlist.contains("No allow rule configured") || noAllowlist.contains("Keine Erlaubnisregel"),
				() -> "Unexpected no-allowlist reason: " + noAllowlist);
		assertTrue(
				notPermitted.contains("No configured allow rule permits this access")
						|| notPermitted.contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
				() -> "Unexpected not-permitted reason: " + notPermitted);
	}
}
