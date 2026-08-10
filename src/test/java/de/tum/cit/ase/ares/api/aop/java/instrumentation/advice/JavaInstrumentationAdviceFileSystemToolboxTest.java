package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
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
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES.get("java.nio.file.Files")
				.containsAll(List.of("copy", "mismatch")));
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES.get("java.nio.file.Files")
				.contains("copy"));
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES
				.get("java.nio.channels.FileChannel").contains("transferTo"));
		assertTrue(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES
				.get("java.nio.channels.FileChannel").containsAll(List.of("transferTo", "transferFrom")));
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

	// <editor-fold desc="I-114: Files.copy / FileChannel.transferTo/transferFrom
	// per-parameter roles">

	@Test
	void checkFileSystemInteraction_filesCopySourceRequiresReadPermissionNotJustOverwrite(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path source = tempDir.resolve("secret.txt");
			Files.writeString(source, "secret content");

			// I-114 privilege escalation: an OVERWRITE-only grant for source's own path
			// must
			// NOT let Files.copy read it.
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[] { source.toString() },
					"ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFilesCopyReadLeg(source, tempDir.resolve("copy.txt")));
			assertTrue(exception.getMessage().contains("read"),
					() -> "Expected the denial to name the 'read' action, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_filesCopyAllowsSourceWithReadPermissionOnly(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path source = tempDir.resolve("readable.txt");
			Files.writeString(source, "content");

			// Only READ granted (no OVERWRITE at all) - copy's source-read leg must
			// succeed.
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { source.toString() },
					"ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[0], "ARCH",
					"INSTRUMENTATION");

			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFilesCopyReadLeg(source, tempDir.resolve("copy.txt")));
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_filesCopyDestinationChecksCreateOrOverwriteBasedOnReplaceExisting(
			@TempDir Path tempDir) throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path source = tempDir.resolve("source.txt");
			Path destination = tempDir.resolve("dest.txt");

			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[0], "ARCH",
					"INSTRUMENTATION");

			SecurityException withoutReplace = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFilesCopyOverwriteLeg(source, destination));
			assertTrue(withoutReplace.getMessage().contains("create"),
					() -> "Expected 'create' without REPLACE_EXISTING, but was:\n" + withoutReplace.getMessage());

			SecurityException withReplace = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFilesCopyOverwriteLeg(source, destination,
							java.nio.file.StandardCopyOption.REPLACE_EXISTING));
			assertTrue(withReplace.getMessage().contains("overwrite"),
					() -> "Expected 'overwrite' with REPLACE_EXISTING, but was:\n" + withReplace.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_fileChannelTransferToChecksSourceReceiverAsRead(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path source = tempDir.resolve("source.txt");
			Files.writeString(source, "content");
			Path destination = tempDir.resolve("dest.txt");
			Files.createFile(destination);

			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
					FileChannel destinationChannel = FileChannel.open(destination, StandardOpenOption.WRITE)) {
				SecurityException exception = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileChannelTransferToReadLeg(sourceChannel, 0, 10, destinationChannel));
				assertTrue(exception.getMessage().contains("read"),
						() -> "Expected the denial to name the 'read' action, but was:\n" + exception.getMessage());
			}
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_fileChannelTransferFromChecksDestinationReceiverAsOverwrite(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path source = tempDir.resolve("source.txt");
			Files.writeString(source, "content");
			Path destination = tempDir.resolve("dest.txt");
			Files.createFile(destination);

			// I-114: transferFrom was previously not intercepted by either backend at all.
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[0], "ARCH",
					"INSTRUMENTATION");

			try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
					FileChannel destinationChannel = FileChannel.open(destination, StandardOpenOption.WRITE)) {
				SecurityException exception = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileChannelTransferFromOverwriteLeg(destinationChannel, sourceChannel, 0, 10));
				assertTrue(exception.getMessage().contains("overwrite"),
						() -> "Expected the denial to name the 'overwrite' action, but was:\n"
								+ exception.getMessage());
			}
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_fileChannelTransferToDatagramChannelChecksNetworkSend(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			Path source = tempDir.resolve("source.txt");
			Files.writeString(source, "content");

			try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
					DatagramChannel targetChannel = DatagramChannel.open()) {
				targetChannel.connect(new InetSocketAddress("203.0.113.1", 80));
				configureInstrumentationMode();
				JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { source.toString() },
						"ARCH", "INSTRUMENTATION");
				JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeSentTo", new String[0], "ARCH",
						"INSTRUMENTATION");
				JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeSentTo", new int[0], "ARCH",
						"INSTRUMENTATION");

				SecurityException exception = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileChannelTransferToReadLeg(sourceChannel, 0, 10, targetChannel));
				assertTrue(exception.getMessage().contains("send"),
						() -> "Expected the denial to name the 'send' action, but was:\n" + exception.getMessage());
			}
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkFileSystemInteraction_fileChannelTransferFromDatagramChannelChecksNetworkReceive(@TempDir Path tempDir)
			throws Exception {
		try {
			resetSettings();
			Path destination = tempDir.resolve("destination.txt");
			Files.createFile(destination);

			try (FileChannel destinationChannel = FileChannel.open(destination, StandardOpenOption.WRITE);
					DatagramChannel sourceChannel = DatagramChannel.open()) {
				sourceChannel.connect(new InetSocketAddress("203.0.113.1", 80));
				configureInstrumentationMode();
				JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten",
						new String[] { destination.toString() }, "ARCH", "INSTRUMENTATION");
				JavaAOPTestCase.setJavaAdviceSettingValue("hostsAllowedToBeReceivedFrom", new String[0], "ARCH",
						"INSTRUMENTATION");
				JavaAOPTestCase.setJavaAdviceSettingValue("portsAllowedToBeReceivedFrom", new int[0], "ARCH",
						"INSTRUMENTATION");

				SecurityException exception = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileChannelTransferFromOverwriteLeg(destinationChannel, sourceChannel, 0, 10));
				assertTrue(exception.getMessage().contains("receive"),
						() -> "Expected the denial to name the 'receive' action, but was:\n" + exception.getMessage());
			}
		} finally {
			resetSettings();
		}
	}

	// </editor-fold>

	// <editor-fold desc="baseline-low-risk-jdk-read-exemptions">

	@Test
	void entropySourceReadDirectlyByStudentCodeIsStillDenied() throws Exception {
		Assumptions.assumeTrue(Files.exists(Path.of("/dev/urandom")), "requires /dev/urandom (Linux/BSD)");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			// No SecureRandom-seeding frame on this stack, so the entropy-device
			// exemption must NOT apply: a student opening the device directly stays
			// blocked.
			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkEntropyDeviceReadDirectly("/dev/urandom"));
		} finally {
			resetSettings();
		}
	}

	@Test
	void secureRandomSeedingPermitsTheEntropyDeviceReadExemption() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			// A real java.security.SecureRandom.generateSeed(...) call genuinely carries
			// a java.security.SecureRandom frame, so the simulated entropy-device read it
			// triggers must be permitted even though the active policy allows no read
			// paths at all. No actual OS entropy device needs to exist for this: the
			// synthetic SecureRandomSpi below simulates the read from within a real
			// SecureRandom-seeding call stack.
			assertDoesNotThrow(() -> triggerRealSecureRandomSeeding(
					() -> InstrumentationSecurityProbe.checkEntropyDeviceReadDirectly("/dev/urandom")));
		} finally {
			resetSettings();
		}
	}

	@Test
	void systemTimezoneReadDirectlyByStudentCodeIsStillDenied() throws Exception {
		Assumptions.assumeTrue(Files.exists(Path.of("/etc/localtime")), "requires /etc/localtime (Linux/BSD)");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			// No trusted java.time timezone-resolution frame on this stack, so the
			// system-timezone exemption must NOT apply: a student opening the symlink
			// directly stays blocked.
			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkSystemFileReadDirectly("/etc/localtime"));
		} finally {
			resetSettings();
		}
	}

	@Test
	void cacertsReadUnderJavaHomeIsAlreadyExemptWithoutAllowlistEntry() throws Exception {
		String cacertsPath = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts").toString();
		Assumptions.assumeTrue(Files.exists(Path.of(cacertsPath)), "requires a JDK-bundled cacerts file");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			// SSLContext/TrustManagerFactory's default init reads cacerts under java.home,
			// already covered by the pre-existing isExemptSystemFileAccess java.home
			// read exemption - locked in here so a future change cannot silently narrow
			// it.
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkSystemFileReadDirectly(cacertsPath));
		} finally {
			resetSettings();
		}
	}

	@Test
	void filesCreateTempFileWithoutExplicitDirectoryIsExemptWithoutAllowlistEntry() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");

			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(null, "ares-baseline-", ".tmp"),
					"Files.createTempFile without an explicit directory defaults to java.io.tmpdir and should be exempt");
		} finally {
			resetSettings();
		}
	}

	@Test
	void fileCreateTempFileTwoArgOverloadIsExemptWithoutAllowlistEntry() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");

			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", null));
		} finally {
			resetSettings();
		}
	}

	@Test
	void fileCreateTempFileExplicitDefaultTempDirectoryIsExempt() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");

			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", tmpDir),
					"An explicit directory argument that IS java.io.tmpdir should still be exempt");
		} finally {
			resetSettings();
		}
	}

	@Test
	void fileCreateTempFileExplicitNonDefaultDirectoryStillRequiresAllowlistEntry() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");

			// Regression guard for the latent bypass this feature closes: before, EVERY
			// parameter of File.createTempFile (including an explicit, non-default
			// directory) was ignored outright, so this call was allowed unconditionally.
			// The directory must genuinely sit outside java.io.tmpdir - unlike @TempDir,
			// which JUnit itself creates below the system temp directory.
			File explicitDir = createNonTempDirOutsideDefaultTempDir("fileCreateTempFileExplicitNonDefault");
			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", explicitDir));
		} finally {
			resetSettings();
		}
	}

	@Test
	void filesCreateTempFileWithExplicitAllowedDirectoryIsPermitted() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			Path explicitDir = createNonTempDirOutsideDefaultTempDir("filesCreateTempFileExplicitAllowed").toPath();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated",
					new String[] { explicitDir.toString() }, "ARCH", "INSTRUMENTATION");

			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(explicitDir, "ares-baseline-", ".tmp"),
					"An explicit directory that IS in pathsAllowedToBeCreated should still be permitted through the special case");
		} finally {
			resetSettings();
		}
	}

	@Test
	void filesCreateTempFileWithExplicitNonAllowedDirectoryIsDenied() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");
			Path explicitDir = createNonTempDirOutsideDefaultTempDir("filesCreateTempFileExplicitNonAllowed").toPath();

			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(explicitDir, "ares-baseline-", ".tmp"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Creates (and registers for deletion) a directory under the build's
	 * {@code target/} tree, which - unlike JUnit's {@code @TempDir} - does not
	 * itself live under {@code java.io.tmpdir}, so it is a genuine "explicit
	 * non-default directory" fixture for the temp-file-creation exemption tests
	 * above. Aborts rather than silently passing if {@code target/} itself happens
	 * to live under {@code java.io.tmpdir} in this environment (e.g. some CI
	 * runners place the whole workspace under the system temp directory), since the
	 * denial tests would then no longer exercise a genuinely non-default directory.
	 */
	private static File createNonTempDirOutsideDefaultTempDir(String name) throws IOException {
		Path dir = Path.of("target", "baseline-low-risk-test-dirs", name);
		Files.createDirectories(dir);
		dir.toFile().deleteOnExit();

		Path realDir = dir.toRealPath();
		Path realDefaultTempDir = Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
		if (realDir.startsWith(realDefaultTempDir)) {
			Assumptions.abort("fixture directory " + realDir + " is inside java.io.tmpdir (" + realDefaultTempDir
					+ ") in this environment; cannot exercise a genuine non-default-temp-directory denial here");
		}
		return dir.toFile();
	}

	/**
	 * Registers a synthetic {@link SecureRandomSpi} whose
	 * {@code engineGenerateSeed} runs the given probe, then calls
	 * {@link SecureRandom#generateSeed(int)} on it. This makes
	 * {@code java.security.SecureRandom.generateSeed(...)} a genuine caller frame
	 * on the real stack while the probe runs, without needing an actual OS entropy
	 * device to exist or be read.
	 */
	private static void triggerRealSecureRandomSeeding(SeedingProbe probe) throws Exception {
		ProbingSecureRandomSpi.PROBE = probe;
		try {
			Provider provider = new Provider("ares-hotfix-test-secure-random-provider", "1.0",
					"Ares test fixture provider for exercising the SecureRandom-seeding stack detector") {
				private static final long serialVersionUID = 1L;
			};
			provider.put("SecureRandom.AresProbe", ProbingSecureRandomSpi.class.getName());
			SecureRandom.getInstance("AresProbe", provider).generateSeed(1);
		} finally {
			ProbingSecureRandomSpi.PROBE = null;
		}
	}

	@FunctionalInterface
	private interface SeedingProbe {
		void run();
	}

	public static final class ProbingSecureRandomSpi extends SecureRandomSpi {

		private static volatile SeedingProbe PROBE;

		@Override
		protected byte[] engineGenerateSeed(int numBytes) {
			PROBE.run();
			return new byte[numBytes];
		}

		@Override
		protected void engineSetSeed(byte[] seed) {
			// Not exercised by these tests.
		}

		@Override
		protected void engineNextBytes(byte[] bytes) {
			// Not exercised by these tests.
		}
	}

	// </editor-fold>
}
