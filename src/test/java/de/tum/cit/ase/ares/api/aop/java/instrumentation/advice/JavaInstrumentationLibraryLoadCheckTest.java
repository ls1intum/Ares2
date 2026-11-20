package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;

import example.student.InstrumentationSecurityProbe;

/**
 * Checks the library-load check of instrumentation mode: the JDK loading its
 * own library at {@code Runtime.loadLibrary0} is allowed, every other load is
 * checked against the policy, and the public {@code loadLibrary} checks leave
 * the decision to it only with a sealed weave record on an audited JDK.
 */
class JavaInstrumentationLibraryLoadCheckTest {

	/** The JVM descriptor of {@code Runtime.loadLibrary0}. */
	private static final String LOAD_LIBRARY0 = "(Ljava/lang/Class;Ljava/lang/String;)V";

	/** How the check's messages name {@code Runtime.loadLibrary0}. */
	private static final String LOAD_LIBRARY0_SIGNATURE = "java.lang.Runtime.loadLibrary0(java.lang.Class,java.lang.String)";

	/** The property the JVM's library folder is read from. */
	private static final String BOOT_LIBRARY_PATH = "sun.boot.library.path";

	/**
	 * Loads the denial messages while nothing is restricted, so building one later
	 * does not have to read the message file under the policy.
	 */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.no.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.not.in.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.file.execution");
	}

	/**
	 * Starts every test with a policy that allows no file to be executed.
	 */
	@BeforeEach
	void configureEmptyPolicy() {
		resetSettings();
		configure(new String[0]);
	}

	/**
	 * Clears the policy again, even after a failure.
	 */
	@AfterEach
	void clearPolicy() {
		resetSettings();
	}

	/**
	 * The JDK loading its own library through a JDK class is allowed under a policy
	 * that allows nothing.
	 *
	 * @param jdkClass a class of the JVM's own loader
	 */
	@ParameterizedTest
	@ValueSource(classes = { java.awt.Toolkit.class, Object.class })
	void theJdkLoadingItsOwnLibraryIsAllowed(Class<?> jdkClass) {
		assertThatCode(() -> InstrumentationSecurityProbe.checkLibraryLoad0(jdkClass, "net"))
				.doesNotThrowAnyException();
	}

	/**
	 * A load attributed to student code is checked as a file and refused, with the
	 * file check's message naming {@code loadLibrary0} and the library as a path in
	 * the working directory.
	 */
	@Test
	void aLoadByStudentCodeIsRefused() {
		String path = Path.of("net").toAbsolutePath().normalize().toString();
		assertThatThrownBy(
				() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net"))
						.isInstanceOf(SecurityException.class).hasMessageContaining(path)
						.hasMessageContaining("java.lang.Runtime.loadLibrary0(java.lang.Class,java.lang.String)");
	}

	/**
	 * An allow rule for the library's path in the working directory still allows a
	 * load by student code, as it did at the public methods.
	 */
	@Test
	void anAllowRuleForTheNameStillAllowsAStudentLoad() {
		configure(new String[] { Path.of("net").toAbsolutePath().normalize().toString() });
		assertThatCode(() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net"))
				.doesNotThrowAnyException();
	}

	/**
	 * A class from a loader that picks its own library file is not the JDK, so its
	 * load is refused.
	 *
	 * @throws IOException if the fixture's class file cannot be read
	 */
	@Test
	void aLoadByAClassOfALibraryFindingLoaderIsRefused() throws IOException {
		Class<?> foreign = new LibraryFindingLoader().defineCopyOf(LibraryCallerFixture.class);
		assertThat(foreign.getClassLoader()).isInstanceOf(LibraryFindingLoader.class);
		assertRefusedAsIllegalExecute(() -> InstrumentationSecurityProbe.checkLibraryLoad0(foreign, "net"),
				LOAD_LIBRARY0_SIGNATURE);
	}

	/**
	 * A JDK caller with a name that is not a plain name of a JDK library gets no
	 * exemption and is refused as an illegal execute.
	 *
	 * @param libraryName the library name
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ares\\net", "../net", "a/net", "net.x", "net\0", "aresNoSuchLib" })
	void aJdkCallerWithAnUnusualNameIsRefused(String libraryName) {
		assertRefusedAsIllegalExecute(() -> InstrumentationSecurityProbe.checkLibraryLoad0(Object.class, libraryName),
				LOAD_LIBRARY0_SIGNATURE);
	}

	/**
	 * A JDK caller with an empty name gets no exemption and is refused, by the file
	 * check's refusal of an empty path.
	 */
	@Test
	void aJdkCallerWithAnEmptyNameIsRefused() {
		assertThatThrownBy(() -> InstrumentationSecurityProbe.checkLibraryLoad0(Object.class, ""))
				.isInstanceOf(SecurityException.class).hasMessage(
						JavaInstrumentationAdviceAbstractToolbox.localize("security.instrumentation.invalid.path", ""));
	}

	/**
	 * Only a plain name passes: no {@code /}, {@code \}, {@code .} or NUL and not
	 * empty.
	 *
	 * @param libraryName a name that is not plain
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ares\\net", "../net", "a/net", "net.x", "", "net\0" })
	void onlyAPlainNameIsAPlainLibraryName(String libraryName) {
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPlainLibraryName(libraryName)).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPlainLibraryName(null)).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPlainLibraryName("net")).isTrue();
	}

	/**
	 * A name that is not plain gets no exemption even when the file it maps to
	 * exists, in the Linux and macOS layout {@code lib} and the Windows layout
	 * {@code bin}.
	 *
	 * @param home a fresh Java home
	 * @throws IOException if a library file cannot be written
	 */
	@Test
	void aNameThatIsNotPlainIsNotExemptEvenIfItsFileExists(@TempDir Path home) throws IOException {
		for (String folder : new String[] { "lib", "bin" }) {
			Path directory = Files.createDirectories(home.resolve(folder));
			for (String libraryName : new String[] { "ares\\net", "../x", "a/net", "net.x", "" }) {
				Path mapped = directory.resolve(System.mapLibraryName(libraryName));
				Files.createDirectories(mapped.getParent());
				Files.writeString(mapped, "library");
				assertThat(mapped).as("%s in %s", libraryName, folder).isRegularFile();
				assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime",
						"loadLibrary0", LOAD_LIBRARY0, Object.class, libraryName, directory))
								.as("%s in %s", libraryName, folder).isFalse();
			}
		}
	}

	/**
	 * A name whose file lies in another folder never counts as the JDK's file, even
	 * if that file exists: neither in a sub folder nor above the library folder.
	 * This is the check that also stops a Windows drive-relative name. Every file
	 * stays inside the fresh Java home, on every platform.
	 *
	 * @param home a fresh Java home
	 * @throws IOException if a library file cannot be written
	 */
	@Test
	void aFileOutsideTheLibraryFolderNeverCounts(@TempDir Path home) throws IOException {
		Path bin = Files.createDirectories(home.resolve("bin"));
		for (String libraryName : new String[] { "a/net", "a/../../x" }) {
			Path raw = bin.resolve(System.mapLibraryName(libraryName));
			Path outside = raw.normalize();
			assertThat(outside).as(libraryName).startsWithRaw(home);
			createFoldersAlong(bin, Path.of(System.mapLibraryName(libraryName)));
			Files.createDirectories(outside.getParent());
			Files.writeString(outside, "library");
			assertThat(outside.getParent()).as(libraryName).isNotEqualTo(bin);
			assertThat(raw).as(libraryName).isRegularFile();
			assertThat(JavaInstrumentationAdviceFileSystemToolbox.isBootLibraryFile(bin, libraryName)).as(libraryName)
					.isFalse();
		}
		Files.writeString(bin.resolve(System.mapLibraryName("net")), "library");
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isBootLibraryFile(bin, "net")).isTrue();
	}

	/**
	 * A name that cannot be read as a path is refused with the file check's own
	 * message, and allowed when the execute allow-list allows every path.
	 */
	@Test
	void aNameThatIsNoPathIsRefusedUnlessEveryPathIsAllowed() {
		assertThatThrownBy(
				() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net\0"))
						.isInstanceOf(SecurityException.class)
						.hasMessageContaining(JavaInstrumentationAdviceAbstractToolbox
								.localize("security.advice.denial.reason.no.allowlist"))
						.hasMessageContaining("loadLibrary0");
		configure(new String[] { "*" });
		assertThatCode(
				() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net\0"))
						.doesNotThrowAnyException();
	}

	/**
	 * Without a policy in force nothing is refused, not even a name that is no
	 * path.
	 */
	@Test
	void nothingIsRefusedWithoutAPolicy() {
		resetSettings();
		assertThatCode(
				() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net\0"))
						.doesNotThrowAnyException();
		assertThatCode(() -> InstrumentationSecurityProbe.checkLibraryLoad0(InstrumentationSecurityProbe.class, "net"))
				.doesNotThrowAnyException();
	}

	/**
	 * Only {@code Runtime.loadLibrary0} with its exact descriptor can be exempt;
	 * the {@code #s} rendering or another method never is.
	 */
	@Test
	void onlyTheExactMethodCanBeExempt() {
		Path directory = capturedBootLibraryDirectory();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime", "loadLibrary0",
				LOAD_LIBRARY0, Object.class, "net", directory)).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime", "loadLibrary0",
				"(java.lang.Class,java.lang.String)", Object.class, "net", directory)).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.System", "loadLibrary",
				"(Ljava/lang/String;)V", Object.class, "net", directory)).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime", "load0",
				LOAD_LIBRARY0, Object.class, "net", directory)).isFalse();
	}

	/**
	 * The platform loader counts as the JDK; the application loader and no class do
	 * not.
	 */
	@Test
	void onlyBootAndPlatformClassesAreJdkCallers() throws ClassNotFoundException {
		Class<?> platformClass = Class.forName("java.sql.Connection", false, ClassLoader.getPlatformClassLoader());
		assertThat(platformClass.getClassLoader()).isSameAs(ClassLoader.getPlatformClassLoader());
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkCaller(platformClass)).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkCaller(Object.class)).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkCaller(InstrumentationSecurityProbe.class))
				.isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkCaller(null)).isFalse();
	}

	/**
	 * A public {@code loadLibrary} check defers only on 17, 21 and 25 and only with
	 * the record; any other release, or no record, keeps the check.
	 */
	@Test
	void deferringNeedsAnAuditedReleaseAndTheRecord() {
		for (int release : new int[] { 17, 21, 25 }) {
			assertThat(JavaInstrumentationAdviceFileSystemToolbox.shouldDeferToLoadLibrary0(release, true))
					.as("release %d", release).isTrue();
			assertThat(JavaInstrumentationAdviceFileSystemToolbox.shouldDeferToLoadLibrary0(release, false))
					.as("release %d without the record", release).isFalse();
		}
		for (int release : new int[] { 16, 18, 19, 20, 22, 23, 24, 26 }) {
			assertThat(JavaInstrumentationAdviceFileSystemToolbox.shouldDeferToLoadLibrary0(release, true))
					.as("release %d", release).isFalse();
		}
	}

	/**
	 * Only the two public {@code loadLibrary} methods, recognised by descriptor,
	 * may defer; {@code load} never does.
	 */
	@Test
	void onlyThePublicLoadLibraryMethodsMayDefer() {
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPublicLoadLibrary("java.lang.System", "loadLibrary",
				"(Ljava/lang/String;)V")).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPublicLoadLibrary("java.lang.Runtime", "loadLibrary",
				"(Ljava/lang/String;)V")).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPublicLoadLibrary("java.lang.System", "load",
				"(Ljava/lang/String;)V")).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPublicLoadLibrary("java.lang.Runtime", "loadLibrary",
				"(java.lang.String)")).isFalse();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isPublicLoadLibrary("java.awt.Desktop", "loadLibrary",
				"(Ljava/lang/String;)V")).isFalse();
	}

	/**
	 * Without the agent's sealed record a public {@code loadLibrary} call is still
	 * checked and refused under a policy that allows nothing.
	 */
	@Test
	void aPublicLoadLibraryCallWithoutTheRecordIsChecked() {
		assertThat(JavaInstrumentationLibraryLoadWeaveRecord.INSTALLATION.isWoven()).isFalse();
		assertRefusedAsIllegalExecute(
				() -> InstrumentationSecurityProbe.checkPublicLoadLibrary("java.lang.System", "net"),
				"java.lang.System.loadLibrary(java.lang.String)");
		assertRefusedAsIllegalExecute(
				() -> InstrumentationSecurityProbe.checkPublicLoadLibrary("java.lang.Runtime", "net"),
				"java.lang.Runtime.loadLibrary(java.lang.String)");
	}

	/**
	 * A value that is missing, empty, several folders, relative or outside the Java
	 * home gives no library folder.
	 *
	 * @param home a fresh Java home
	 */
	@Test
	void anUnacceptedBootLibraryPathGivesNoFolder(@TempDir Path home) {
		String lib = home.resolve("lib").toString();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(), null,
				File.pathSeparator)).isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(), "",
				File.pathSeparator)).isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(),
				lib + File.pathSeparator + lib, File.pathSeparator)).isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(), "lib",
				File.pathSeparator)).isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(),
				home.getParent().resolve("elsewhere").toString(), File.pathSeparator)).isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(),
				home.resolve("lib").resolve("..").resolve("..").resolve("elsewhere").toString(), File.pathSeparator))
						.isNull();
		assertThat(
				JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(null, lib, File.pathSeparator))
						.isNull();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(), lib,
				File.pathSeparator)).isEqualTo(home.resolve("lib"));
	}

	/**
	 * The Windows layout, with the JDK's libraries in {@code bin}, gives that
	 * folder and exempts a JDK load of a library held there, but not of one missing
	 * there.
	 *
	 * @param home a fresh Java home
	 * @throws IOException if the library file cannot be written
	 */
	@Test
	void theWindowsLayoutUsesTheBinFolder(@TempDir Path home) throws IOException {
		Path bin = Files.createDirectories(home.resolve("bin"));
		Files.writeString(bin.resolve(System.mapLibraryName("net")), "library");
		Path directory = JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(home.toString(),
				bin.toString(), ";");
		assertThat(directory).isEqualTo(bin);
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime", "loadLibrary0",
				LOAD_LIBRARY0, Object.class, "net", directory)).isTrue();
		assertThat(JavaInstrumentationAdviceFileSystemToolbox.isJdkOwnLibraryLoad("java.lang.Runtime", "loadLibrary0",
				LOAD_LIBRARY0, Object.class, "zip", directory)).isFalse();
	}

	/**
	 * The running JDK's own library folder is accepted: the folder the toolbox
	 * captured is not missing and is the one the JDK's property names.
	 */
	@Test
	void theRunningJdkLibraryFolderIsAccepted() {
		assertThat(capturedBootLibraryDirectory()).isNotNull().isEqualTo(
				JavaInstrumentationAdviceFileSystemToolbox.trustedBootLibraryDirectory(System.getProperty("java.home"),
						System.getProperty(BOOT_LIBRARY_PATH), File.pathSeparator));
	}

	/**
	 * The running JDK still declares the method the check is woven into, so a JDK
	 * that drops it breaks this test instead of silently ending the exemption.
	 *
	 * @throws NoSuchMethodException if the JDK no longer declares it
	 */
	@Test
	void theRunningJdkDeclaresLoadLibrary0() throws NoSuchMethodException {
		Method loadLibrary0 = Runtime.class.getDeclaredMethod("loadLibrary0", Class.class, String.class);
		assertThat(loadLibrary0.getReturnType()).isEqualTo(void.class);
	}

	/**
	 * A mark made while the window is open counts once the record is sealed, and
	 * only then.
	 */
	@Test
	void aMarkDuringInstallationCountsOnceSealed() {
		JavaInstrumentationLibraryLoadWeaveRecord record = new JavaInstrumentationLibraryLoadWeaveRecord();
		record.open();
		markLoadLibrary0(record);
		assertThat(record.isWoven()).isFalse();
		record.seal();
		assertThat(record.isWoven()).isTrue();
	}

	/**
	 * A mark made before the window opens, or wiped by opening it again, counts for
	 * nothing.
	 */
	@Test
	void openingWipesAnEarlierMark() {
		JavaInstrumentationLibraryLoadWeaveRecord early = new JavaInstrumentationLibraryLoadWeaveRecord();
		markLoadLibrary0(early);
		early.open();
		early.seal();
		assertThat(early.isWoven()).isFalse();
		JavaInstrumentationLibraryLoadWeaveRecord reopened = new JavaInstrumentationLibraryLoadWeaveRecord();
		reopened.open();
		markLoadLibrary0(reopened);
		reopened.open();
		reopened.seal();
		assertThat(reopened.isWoven()).isFalse();
	}

	/**
	 * A mark made without opening the window counts for nothing.
	 */
	@Test
	void aMarkWithoutOpeningCountsForNothing() {
		JavaInstrumentationLibraryLoadWeaveRecord record = new JavaInstrumentationLibraryLoadWeaveRecord();
		markLoadLibrary0(record);
		record.seal();
		assertThat(record.isWoven()).isFalse();
	}

	/**
	 * After sealing, neither opening nor marking changes the record, in either
	 * direction.
	 */
	@Test
	void aSealedRecordNeverChanges() {
		JavaInstrumentationLibraryLoadWeaveRecord empty = new JavaInstrumentationLibraryLoadWeaveRecord();
		empty.open();
		empty.seal();
		empty.open();
		markLoadLibrary0(empty);
		empty.seal();
		assertThat(empty.isWoven()).isFalse();
		JavaInstrumentationLibraryLoadWeaveRecord woven = new JavaInstrumentationLibraryLoadWeaveRecord();
		woven.open();
		markLoadLibrary0(woven);
		woven.seal();
		woven.open();
		woven.seal();
		assertThat(woven.isWoven()).isTrue();
	}

	/**
	 * A mark counts only for exactly {@code Runtime.loadLibrary0} of the JVM's own
	 * loader.
	 */
	@Test
	void onlyTheExactBootMethodIsMarked() {
		assertThat(sealedAfterMark("java.lang.Runtime", "loadLibrary0", LOAD_LIBRARY0, false)).isFalse();
		assertThat(sealedAfterMark("java.lang.System", "loadLibrary0", LOAD_LIBRARY0, true)).isFalse();
		assertThat(sealedAfterMark("java.lang.Runtime", "loadLibrary", LOAD_LIBRARY0, true)).isFalse();
		assertThat(sealedAfterMark("java.lang.Runtime", "loadLibrary0", "(java.lang.Class,java.lang.String)", true))
				.isFalse();
		assertThat(sealedAfterMark("java.lang.Runtime", "loadLibrary0", LOAD_LIBRARY0, true)).isTrue();
	}

	/**
	 * Creates every folder a relative file path passes through from a base folder,
	 * skipping the {@code ..} steps, so the path can be followed as written.
	 *
	 * @param base     the folder the path starts in
	 * @param relative the file path, possibly with {@code ..} steps
	 * @throws IOException if a folder cannot be created
	 */
	private static void createFoldersAlong(Path base, Path relative) throws IOException {
		Path current = base;
		for (int index = 0; index < relative.getNameCount() - 1; index++) {
			current = current.resolve(relative.getName(index));
			if (!"..".equals(relative.getName(index).toString())) {
				Files.createDirectories(current.normalize());
			}
		}
	}

	/**
	 * Requires a call to be refused with the file check's message for illegally
	 * executing a file, naming the given method.
	 *
	 * @param call   the call that must be refused
	 * @param method the method as the message names it
	 */
	private static void assertRefusedAsIllegalExecute(ThrowingCallable call, String method) {
		assertThatThrownBy(call).isInstanceOf(SecurityException.class).hasMessageContaining(illegalExecuteWording())
				.hasMessageContaining(method);
	}

	/**
	 * Returns the words of the illegal-file message between the student method and
	 * the file, for the action {@code execute}, in the current language.
	 *
	 * @return those words, such as {@code tried to illegally execute File}
	 */
	private static String illegalExecuteWording() {
		String message = JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.file.execution",
				"\u0001", "execute", "\u0002", "\u0003");
		return message.substring(message.indexOf('\u0001') + 1, message.indexOf('\u0002'));
	}

	/**
	 * Marks a record for exactly {@code Runtime.loadLibrary0} of the JVM's own
	 * loader.
	 *
	 * @param record the record
	 */
	private static void markLoadLibrary0(JavaInstrumentationLibraryLoadWeaveRecord record) {
		record.mark("java.lang.Runtime", "loadLibrary0", LOAD_LIBRARY0, true);
	}

	/**
	 * Opens a fresh record, marks it once with the given method, seals it and tells
	 * whether it counts.
	 *
	 * @return whether the sealed record holds the mark
	 */
	private static boolean sealedAfterMark(String type, String method, String descriptor, boolean loaderIsBoot) {
		JavaInstrumentationLibraryLoadWeaveRecord record = new JavaInstrumentationLibraryLoadWeaveRecord();
		record.open();
		record.mark(type, method, descriptor, loaderIsBoot);
		record.seal();
		return record.isWoven();
	}

	/**
	 * Reads the library folder the toolbox captured at start-up.
	 *
	 * @return the captured folder, or {@code null}
	 */
	private static Path capturedBootLibraryDirectory() {
		try {
			Field field = JavaInstrumentationAdviceFileSystemToolbox.class
					.getDeclaredField("TRUSTED_BOOT_LIBRARY_DIRECTORY");
			field.setAccessible(true);
			return (Path) field.get(null);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("the toolbox has no readable library folder", exception);
		}
	}

	/**
	 * Publishes an instrumentation policy for {@code example.student} with the
	 * given execute allow-list.
	 *
	 * @param executable the execute allow-list
	 */
	private static void configure(String[] executable) {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeExecuted", executable, "ARCH", "INSTRUMENTATION");
	}

	/**
	 * Clears every policy setting in both copies of the settings.
	 */
	private static void resetSettings() {
		AOPMode.ASPECTJ.reset();
	}

	/** A class copied into {@link LibraryFindingLoader}; it has no behaviour. */
	static final class LibraryCallerFixture {
	}

	/**
	 * A class loader that names its own file for every library, as a student loader
	 * could, so a class it defines must never count as the JDK.
	 */
	private static final class LibraryFindingLoader extends ClassLoader {

		/** Creates the loader with the JVM's own loader as parent. */
		private LibraryFindingLoader() {
			super(null);
		}

		/**
		 * Defines a copy of a class from its class file.
		 *
		 * @param type the class to copy
		 * @return the copy, defined by this loader
		 * @throws IOException if the class file cannot be read
		 */
		private Class<?> defineCopyOf(Class<?> type) throws IOException {
			String resource = type.getName().replace('.', '/') + ".class";
			try (InputStream in = type.getClassLoader().getResourceAsStream(resource)) {
				if (in == null) {
					throw new IOException("no class file for " + type.getName());
				}
				byte[] bytes = in.readAllBytes();
				return defineClass(type.getName(), bytes, 0, bytes.length);
			}
		}

		/**
		 * Names a file outside the JDK for every library.
		 *
		 * @param libname the library name
		 * @return a path outside the JDK
		 */
		@Override
		protected String findLibrary(String libname) {
			return Path.of(System.getProperty("java.io.tmpdir"), System.mapLibraryName(libname)).toString();
		}
	}
}
