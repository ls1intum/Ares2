package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.testutilities.FakeSecureRandomSeedingFixture;

import example.student.InstrumentationSecurityProbe;

/**
 * Carries a loopback literal for the network-adjacent cases in this class.
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
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

	// The MockedStatic resource is never read, and that is the point: the static
	// mock is
	// active for the scope of the try-with-resources, not through the variable. PMD
	// counts
	// it as an unused local, but a resource cannot be declared without a name.
	@SuppressWarnings("PMD.UnusedLocalVariable")
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
			SecurityException exception = triggerBlockedFileUrlOpenStream(tempDir);
			assertTrue(exception.getMessage().contains(tempDir.resolve("forbidden.txt").toAbsolutePath().toString()));
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
			SecurityException exception = triggerBlockedFileUrlOpenStream(tempDir);
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

	/**
	 * Placeholder used to cut a localised message template into its fixed parts.
	 */
	private static final String MESSAGE_ARGUMENT_MARKER = "\u0000";

	/**
	 * Name of the settings field that holds the default temp directory Ares fixed
	 * at start-up.
	 */
	private static final String FROZEN_TEMP_DIRECTORY_FIELD = "frozenDefaultTempDirectory";

	/**
	 * A student opening a seed device directly has no JDK seeding code on the call
	 * stack, so the read stays denied.
	 */
	@Test
	void entropySourceReadDirectlyByStudentCodeIsStillDenied() throws Exception {
		Assumptions.assumeTrue(Files.exists(Path.of("/dev/urandom")), "requires /dev/urandom (Linux/BSD)");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkEntropyDeviceReadDirectly("/dev/urandom"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * A student-written random generator runs beneath the public
	 * {@code SecureRandom} class, which is not trusted, so a seed-device read from
	 * inside it stays denied.
	 */
	@Test
	void customSecureRandomSpiCannotForgeTheEntropyDeviceReadExemption() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			assertThrows(SecurityException.class, () -> FakeSecureRandomSeedingFixture.triggerFakeSecureRandomSeeding(
					() -> InstrumentationSecurityProbe.checkEntropyDeviceReadDirectly("/dev/urandom")));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Real JDK seeding still works with a policy that allows no reads. The blocking
	 * generator is requested by name because the JDK caches its default device
	 * stream, which could let the call pass without reaching the check at all.
	 */
	@Test
	void genuineSecureRandomEntropySeedingIsPermittedByAnActivePolicy() throws Exception {
		SecureRandom nativeBlockingSecureRandom;
		try {
			nativeBlockingSecureRandom = SecureRandom.getInstance("NativePRNGBlocking");
		} catch (NoSuchAlgorithmException e) {
			Assumptions.abort("NativePRNGBlocking unavailable on this platform (" + e.getMessage() + ")");
			return;
		}
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			SecureRandom finalNativeBlockingSecureRandom = nativeBlockingSecureRandom;
			assertDoesNotThrow(() -> finalNativeBlockingSecureRandom.generateSeed(8));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Reading the system timezone file directly is denied, since no exemption
	 * exists for it: the JDK reads it natively.
	 */
	@Test
	void systemTimezoneReadDirectlyByStudentCodeIsDenied() throws Exception {
		Assumptions.assumeTrue(Files.exists(Path.of("/etc/localtime")), "requires /etc/localtime (Linux/BSD)");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkSystemFileReadDirectly("/etc/localtime"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * The trusted certificates file lies under the Java home, which is already
	 * readable without an entry; this keeps that from narrowing unnoticed.
	 */
	@Test
	void cacertsReadUnderJavaHomeIsAlreadyExemptWithoutAllowlistEntry() throws Exception {
		String cacertsPath = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts").toString();
		Assumptions.assumeTrue(Files.exists(Path.of(cacertsPath)), "requires a JDK-bundled cacerts file");
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[0], "ARCH", "INSTRUMENTATION");

			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkSystemFileReadDirectly(cacertsPath));
		} finally {
			resetSettings();
		}
	}

	/**
	 * {@code Files.createTempFile} without a directory is allowed without an entry
	 * once the default temp directory is fixed.
	 */
	@Test
	void filesCreateTempFileWithoutDirectoryIsAllowedOnceTheTempDirectoryIsFrozen() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> assertDoesNotThrow(
				() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(null, "ares-baseline-", ".tmp")));
	}

	/**
	 * {@code File.createTempFile} without a directory, or with {@code null} for it,
	 * is allowed without an entry once the default temp directory is fixed.
	 */
	@Test
	void fileCreateTempFileWithoutDirectoryIsAllowedOnceTheTempDirectoryIsFrozen() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> {
			assertDoesNotThrow(() -> InstrumentationSecurityProbe
					.checkFileCreateTempFileMalformed(new Object[] { "ares-baseline-", ".tmp", null }));
			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", null));
		});
	}

	/**
	 * Naming the fixed default temp directory itself is allowed without an entry.
	 */
	@Test
	void fileCreateTempFileInTheFrozenTempDirectoryIsAllowed() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> assertDoesNotThrow(() -> InstrumentationSecurityProbe
				.checkFileCreateTempFile("ares-baseline-", ".tmp", new File(realDefaultTempDirectory()))));
	}

	/**
	 * Naming a directory outside the temp directory needs an entry; this was
	 * silently allowed before, when every argument of the method was ignored.
	 */
	@Test
	void fileCreateTempFileInAnotherDirectoryIsDenied() throws Exception {
		File explicitDir = createNonTempDirOutsideDefaultTempDir("fileCreateTempFileInAnotherDirectory");
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> assertThrows(SecurityException.class,
				() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", explicitDir)));
	}

	/**
	 * A directory the policy lets students create files in is allowed.
	 */
	@Test
	void filesCreateTempFileInAnAllowedDirectoryIsAllowed() throws Exception {
		Path explicitDir = createNonTempDirOutsideDefaultTempDir("filesCreateTempFileInAnAllowedDirectory").toPath();
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> {
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated",
					new String[] { explicitDir.toString() }, "ARCH", "INSTRUMENTATION");
			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(explicitDir, "ares-baseline-", ".tmp"));
		});
	}

	/**
	 * A directory the policy does not list is denied.
	 */
	@Test
	void filesCreateTempFileInADirectoryNotAllowedIsDenied() throws Exception {
		Path explicitDir = createNonTempDirOutsideDefaultTempDir("filesCreateTempFileInADirectoryNotAllowed").toPath();
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> assertThrows(SecurityException.class,
				() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(explicitDir, "ares-baseline-", ".tmp")));
	}

	/**
	 * A subdirectory of the temp directory is not the temp directory itself, so it
	 * needs an entry.
	 */
	@Test
	void fileCreateTempFileInASubdirectoryOfTheFrozenTempDirectoryIsDenied(@TempDir Path subdirectory)
			throws Exception {
		Assumptions.assumeTrue(subdirectory.toRealPath().startsWith(realDefaultTempDirectory()),
				"JUnit's temporary directory is not below java.io.tmpdir here");
		withFrozenTempDirectory(realDefaultTempDirectory(),
				() -> assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileCreateTempFile("ares-baseline-", ".tmp", subdirectory.toFile())));
	}

	/**
	 * A link inside the temp directory that leads somewhere else is judged by where
	 * it leads, so it needs an entry.
	 */
	@Test
	void fileCreateTempFileThroughALinkInsideTheFrozenTempDirectoryIsDenied(@TempDir Path linkParent) throws Exception {
		Path target = createNonTempDirOutsideDefaultTempDir("linkTargetOutsideTempDirectory").toPath().toAbsolutePath();
		Path link;
		try {
			link = Files.createSymbolicLink(linkParent.resolve("link-to-elsewhere"), target);
		} catch (UnsupportedOperationException | IOException e) {
			Assumptions.abort("symbolic links are not available here (" + e.getMessage() + ")");
			return;
		}
		Path finalLink = link;
		withFrozenTempDirectory(realDefaultTempDirectory(),
				() -> assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileCreateTempFile("ares-baseline-", ".tmp", finalLink.toFile())));
	}

	/**
	 * A directory named after one of Ares's own internal files grants nothing,
	 * since a student can create a directory with any name.
	 */
	@Test
	void explicitTempDirectoryEndingInInternalPathSuffixIsNotExempt() throws Exception {
		File explicitDir = Path.of("target", "baseline-low-risk-test-dirs", "student-crafted", "ares", "api",
				"localization", "Messages.class").toFile();
		withFrozenTempDirectory(realDefaultTempDirectory(), () -> assertThrows(SecurityException.class,
				() -> InstrumentationSecurityProbe.checkFileCreateTempFile("ares-baseline-", ".tmp", explicitDir)));
	}

	/**
	 * Without a fixed default temp directory, a temp file without a directory is
	 * denied even when the policy covers the temp directory, because Ares cannot
	 * tell where the JDK will write. It says trusted start-up is missing, as every
	 * access without it does; checked in both languages.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "en", "de" })
	void tempFileWithoutDirectoryIsDeniedWhenNothingFroze(String language) throws Exception {
		withDisplayLocale(Locale.forLanguageTag(language), () -> withFrozenTempDirectory(null, () -> {
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated",
					new String[] { realDefaultTempDirectory() }, "ARCH", "INSTRUMENTATION");
			SecurityException denial = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkFilesCreateTempFile(null, "ares-baseline-", ".tmp"));
			assertLocalisedMessage("security.advice.trusted.startup.missing", denial);
		}));
	}

	/**
	 * Arguments that fit no {@code Files.createTempFile} overload are denied, in
	 * both languages, rather than treated as "no directory".
	 */
	@ParameterizedTest
	@ValueSource(strings = { "en", "de" })
	void filesCreateTempFileWithWrongParameterCountFailsClosed(String language) throws Exception {
		withDisplayLocale(Locale.forLanguageTag(language),
				() -> withFrozenTempDirectory(realDefaultTempDirectory(), () -> {
					SecurityException denial = assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
							.checkFilesCreateTempFileMalformed(new Object[] { "ares-baseline-" }));
					assertLocalisedMessage("security.advice.file.system.malformed.temp.file.creation", denial);
				}));
	}

	/**
	 * Four arguments with something other than a {@code Path} first are denied.
	 */
	@Test
	void filesCreateTempFileWithNonPathDirectoryPositionFailsClosed() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(),
				() -> assertThrows(SecurityException.class,
						() -> InstrumentationSecurityProbe.checkFilesCreateTempFileMalformed(
								new Object[] { "not-a-path", "ares-baseline-", ".tmp", new FileAttribute<?>[0] })));
	}

	/**
	 * Arguments that fit no {@code File.createTempFile} overload are denied.
	 */
	@Test
	void fileCreateTempFileWithWrongParameterCountFailsClosed() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(),
				() -> assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileCreateTempFileMalformed(new Object[] { "ares-baseline-" })));
	}

	/**
	 * Three arguments with something other than a {@code File} or {@code null} last
	 * are denied.
	 */
	@Test
	void fileCreateTempFileWithNonFileDirectoryPositionFailsClosed() throws Exception {
		withFrozenTempDirectory(realDefaultTempDirectory(),
				() -> assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe
						.checkFileCreateTempFileMalformed(new Object[] { "ares-baseline-", ".tmp", "not-a-file" })));
	}

	/**
	 * Something a temp-file test runs while the settings are prepared.
	 */
	@FunctionalInterface
	private interface SettingsBody {

		/**
		 * Runs the test body.
		 *
		 * @throws Exception whatever the body throws
		 */
		void run() throws Exception;
	}

	/**
	 * Runs a body with instrumentation on, no create entries, and the given fixed
	 * temp directory in both settings copies, then puts the previous fixed values
	 * back, since they outlive {@code reset()}.
	 *
	 * @param frozenDirectory the fixed directory to use, or {@code null} for none
	 * @param body            the test body
	 * @throws Exception whatever the body throws
	 */
	private static void withFrozenTempDirectory(String frozenDirectory, SettingsBody body) throws Exception {
		Field bootstrapField = frozenTempDirectoryField(null);
		Field applicationField = frozenTempDirectoryField(JavaAOPTestCaseSettings.class.getClassLoader());
		Object bootstrapBefore = bootstrapField == null ? null : bootstrapField.get(null);
		Object applicationBefore = applicationField.get(null);
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[0], "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue(FROZEN_TEMP_DIRECTORY_FIELD, frozenDirectory, "ARCH",
					"INSTRUMENTATION");
			body.run();
		} finally {
			if (bootstrapField != null) {
				bootstrapField.set(null, bootstrapBefore);
			}
			applicationField.set(null, applicationBefore);
			resetSettings();
		}
	}

	/**
	 * Runs a body with the given display language, loading its message bundle first
	 * so the check does not block reading it.
	 *
	 * @param locale the language to use
	 * @param body   the test body
	 * @throws Exception whatever the body throws
	 */
	private static void withDisplayLocale(Locale locale, SettingsBody body) throws Exception {
		Locale before = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Messages.init();
			body.run();
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, before);
		}
	}

	/**
	 * Checks that a denial carries the localised text of a key, with every fixed
	 * part of the template in order, whatever the arguments were.
	 *
	 * @param key    the message key
	 * @param denial the denial to check
	 */
	private static void assertLocalisedMessage(String key, SecurityException denial) {
		String[] fixedParts = Messages.localized(key, MESSAGE_ARGUMENT_MARKER, MESSAGE_ARGUMENT_MARKER)
				.split(MESSAGE_ARGUMENT_MARKER, -1);
		int searchFrom = 0;
		for (String fixedPart : fixedParts) {
			int found = denial.getMessage().indexOf(fixedPart, searchFrom);
			assertTrue(found >= 0, () -> "Expected \"" + fixedPart + "\" in: " + denial.getMessage());
			searchFrom = found + fixedPart.length();
		}
	}

	/**
	 * Returns the settings field holding the fixed temp directory in one copy of
	 * the settings.
	 *
	 * @param loader the loader of the copy, {@code null} for the bootstrap copy
	 * @return the accessible field, or {@code null} if that copy does not exist
	 * @throws NoSuchFieldException if the copy lacks the field
	 */
	private static Field frozenTempDirectoryField(ClassLoader loader) throws NoSuchFieldException {
		Class<?> settings;
		try {
			settings = Class.forName(JavaAOPTestCaseSettings.class.getName(), false, loader);
		} catch (ClassNotFoundException absent) {
			return null;
		}
		Field field = settings.getDeclaredField(FROZEN_TEMP_DIRECTORY_FIELD);
		field.setAccessible(true);
		return field;
	}

	/**
	 * Returns the JVM's default temp directory resolved to its real location, the
	 * way Ares fixes it.
	 *
	 * @return the real default temp directory
	 * @throws IOException if it cannot be resolved
	 */
	private static String realDefaultTempDirectory() throws IOException {
		return Path.of(System.getProperty("java.io.tmpdir")).toRealPath().toString();
	}

	/**
	 * Creates a directory under {@code target/}, which unlike JUnit's
	 * {@code @TempDir} is not below the default temp directory, and skips the test
	 * if this environment places {@code target/} there anyway.
	 *
	 * @param name the directory name
	 * @return the directory
	 * @throws IOException if it cannot be created
	 */
	private static File createNonTempDirOutsideDefaultTempDir(String name) throws IOException {
		Path dir = Path.of("target", "baseline-low-risk-test-dirs", name);
		Files.createDirectories(dir);
		dir.toFile().deleteOnExit();

		Path realDir = dir.toRealPath();
		Path realDefaultTempDir = Path.of(realDefaultTempDirectory());
		if (realDir.startsWith(realDefaultTempDir)) {
			Assumptions.abort("fixture directory " + realDir + " is inside java.io.tmpdir (" + realDefaultTempDir
					+ ") in this environment; cannot exercise a genuine non-default-temp-directory denial here");
		}
		return dir.toFile();
	}

	// </editor-fold>

	private SecurityException triggerBlockedFileUrlOpenStream(Path tempDir) throws Exception {
		resetSettings();
		configureInstrumentationMode();
		Path allowedDir = Files.createDirectory(tempDir.resolve("allowed"));
		Path forbiddenFile = tempDir.resolve("forbidden.txt");
		Files.writeString(forbiddenFile, "secret");
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { allowedDir.toString() },
				"ARCH", "INSTRUMENTATION");
		return assertThrows(SecurityException.class,
				() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(forbiddenFile.toUri().toURL()));
	}
}
