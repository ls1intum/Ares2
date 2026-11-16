package de.tum.cit.ase.ares.api.aop.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.architecture.java.ForbiddenMethodMatcher;
import de.tum.cit.ase.ares.api.architecture.java.archunit.TransitivelyAccessesMethodsCondition;
import de.tum.cit.ase.ares.api.localization.Messages;

import example.student.BaselineAdviceProbe;
import example.student.BaselineAdviceProbe.Call;

/**
 * Checks that both runtime backends reach the same verdict on every operation
 * the secure baseline allows without a policy entry, or keeps denied, and that
 * both static backends allow the same calls. Each intended difference is named
 * in {@link #INTENDED_ASYMMETRIES}.
 */
class BaselineExemptionParityTest {

	/**
	 * The rows whose two runtime verdicts are meant to differ, each with its
	 * reason. A row that differs without an entry fails, and so does an entry whose
	 * row no longer differs.
	 */
	private static final Map<String, String> INTENDED_ASYMMETRIES = Map.of();

	/** The two runtime backends. */
	private static final AOPMode[] BACKENDS = { AOPMode.INSTRUMENTATION, AOPMode.ASPECTJ };

	/** The settings that hold one allow-list per file-system action. */
	private static final String[] ALLOW_LIST_SETTINGS = { "pathsAllowedToBeRead", "pathsAllowedToBeOverwritten",
			"pathsAllowedToBeCreated", "pathsAllowedToBeExecuted", "pathsAllowedToBeDeleted" };

	/** No file attributes, as a temp-file call without any passes them. */
	private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute<?>[0];

	/** The caller's locale, restored after each test. */
	private Locale originalLocale;

	/** What a backend decides about one call. */
	enum Verdict {
		/** The call may proceed. */
		ALLOW,
		/** The call is refused with a {@code SecurityException}. */
		DENY
	}

	/**
	 * One call both backends judge, with the verdict expected from each.
	 *
	 * @param name            what the row checks, unique among the rows
	 * @param action          the file-system action the call performs
	 * @param allowList       the allow-list for every action, from the test folder
	 * @param call            the call, from the test folder
	 * @param instrumentation the verdict expected from instrumentation
	 * @param aspectJ         the verdict expected from AspectJ
	 */
	record Row(String name, String action, Function<Path, String[]> allowList, Function<Path, Call> call,
			Verdict instrumentation, Verdict aspectJ) {

		/**
		 * Names the row in the test report.
		 *
		 * @return the row's name
		 */
		@Override
		public String toString() {
			return name;
		}
	}

	/** An archive subclass whose overridden method must never run inside advice. */
	private static final class UntrustedZipFile extends ZipFile {
		/** Whether advice called the student-controlled override. */
		private boolean sizeCalled;

		/**
		 * Opens the test archive.
		 *
		 * @param path the archive path
		 * @throws IOException if the archive cannot be opened
		 */
		private UntrustedZipFile(Path path) throws IOException {
			super(path.toFile());
		}

		/**
		 * Records a call that would run under the advice guard.
		 *
		 * @return the archive's entry count
		 */
		@Override
		public int size() {
			sizeCalled = true;
			return super.size();
		}
	}

	/** A jar subclass whose overridden method must never run inside advice. */
	private static final class UntrustedJarFile extends JarFile {
		/** Whether advice called the student-controlled override. */
		private boolean sizeCalled;

		/**
		 * Opens the test jar.
		 *
		 * @param path the jar path
		 * @throws IOException if the jar cannot be opened
		 */
		private UntrustedJarFile(Path path) throws IOException {
			super(path.toFile());
		}

		/**
		 * Records a call that would run under the advice guard.
		 *
		 * @return the jar's entry count
		 */
		@Override
		public int size() {
			sizeCalled = true;
			return super.size();
		}
	}

	/** Clears policy state before a row runs. */
	@BeforeEach
	void rememberLocale() {
		originalLocale = Locale.getDefault();
		AOPMode.ASPECTJ.reset();
	}

	/** Clears policy state and restores the locale, even after a failure. */
	@AfterEach
	void restoreState() {
		AOPMode.ASPECTJ.reset();
		Locale.setDefault(originalLocale);
	}

	/**
	 * Every row gets the expected verdict from each backend.
	 *
	 * @param row       the row
	 * @param directory a fresh folder outside every allow-list
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("rows")
	void eachBackendReachesTheExpectedVerdict(Row row, @TempDir Path directory) {
		Map<AOPMode, Verdict> actual = verdicts(row, directory);
		assertThat(actual).as(row.name()).containsEntry(AOPMode.INSTRUMENTATION, row.instrumentation())
				.containsEntry(AOPMode.ASPECTJ, row.aspectJ());
	}

	/**
	 * An archive a row opens is closed once both backends have judged it, so the
	 * test folder can be deleted on every platform.
	 *
	 * @param directory a fresh folder outside every allow-list
	 */
	@Test
	void anOpenArchiveIsClosedAfterItsRow(@TempDir Path directory) {
		Call call = jarEntryRead(archive(directory));

		verdicts(row("closes its archive", "read", none(), dir -> call, Verdict.ALLOW, Verdict.ALLOW), directory);

		JarFile jar = (JarFile) call.instance();
		assertThatThrownBy(jar::entries).isInstanceOf(IllegalStateException.class);
	}

	/**
	 * The registry names exactly the rows that are meant to differ.
	 */
	@Test
	void theAsymmetryRegistryMatchesTheRows() {
		Set<String> differing = new HashSet<>();
		Set<String> names = new HashSet<>();
		rows().forEach(row -> {
			assertTrue(names.add(row.name()), () -> "two rows are named " + row.name());
			if (row.instrumentation() != row.aspectJ()) {
				differing.add(row.name());
			}
		});
		assertThat(differing).as("rows whose backends differ")
				.containsExactlyInAnyOrderElementsOf(INTENDED_ASYMMETRIES.keySet());
	}

	/**
	 * A denial names the reason in the selected language, while the message bundle
	 * for that language is loaded for the first time inside the check. This is the
	 * positive control for removing the name exemption for Ares's own files: Ares
	 * still reads its messages without one.
	 *
	 * @param language  a locale no other test loads
	 * @param directory a fresh folder outside every allow-list
	 */
	@ParameterizedTest
	@ValueSource(strings = { "de-LI", "en-NZ" })
	void denialMessagesLoadTheirLanguageWithoutAnExemption(String language, @TempDir Path directory)
			throws IOException {
		Locale.setDefault(Locale.forLanguageTag(language));
		Path file = Files.writeString(directory.resolve("plain.txt"), "protected");
		for (AOPMode mode : BACKENDS) {
			configure(mode, new String[0]);
			SecurityException denied = denial(mode, "read", read(file));
			assertThat(denied.getMessage()).as(mode + " " + language).contains(file.toString())
					.contains(Messages.localized("security.advice.denial.reason.no.allowlist"));
			configure(mode, new String[] { directory.resolve("unrelated").toString() });
			denied = denial(mode, "read", read(file));
			assertThat(denied.getMessage()).as(mode + " " + language)
					.contains(Messages.localized("security.advice.denial.reason.not.in.allowlist"));
		}
	}

	/**
	 * The native-library suffixes trusted under the Java installation are the same
	 * on both backends.
	 */
	@Test
	void bothBackendsTrustTheSameNativeLibrarySuffixes() {
		assertThat(new HashSet<>(constant(JavaInstrumentationAdviceFileSystemToolbox.class, "NATIVE_LIBRARY_SUFFIXES")))
				.isNotEmpty().isEqualTo(new HashSet<>(
						constant(JavaAspectJFileSystemAdviceDefinitions.class, "NATIVE_LIBRARY_SUFFIXES")));
	}

	/**
	 * The random-seed devices are exempt on instrumentation only: AspectJ never
	 * sees the JDK reading them, because it only checks calls in exercise code.
	 */
	@Test
	void onlyInstrumentationNamesTheRandomSeedDevices() {
		assertThat(constant(JavaInstrumentationAdviceFileSystemToolbox.class, "ENTROPY_SOURCE_PATHS"))
				.containsExactlyInAnyOrder("/dev/urandom", "/dev/random");
		assertThat(hasField(JavaAspectJFileSystemAdviceDefinitions.class, "ENTROPY_SOURCE_PATHS")).isFalse();
	}

	/**
	 * Both backends trust the same Maven repository for jar reads, so one fixture
	 * jar stands for both.
	 */
	@Test
	void bothBackendsTrustTheSameMavenRepository() {
		assertThat(trustedMavenRepository()).isNotNull()
				.isEqualTo(staticValue(JavaAspectJFileSystemAdviceDefinitions.class, "TRUSTED_MAVEN_REPOSITORY"));
	}

	/**
	 * A refusal counts as a denial only when the file policy made it. A missing
	 * start-up setting is a refusal too, and must fail the row instead.
	 *
	 * @param directory a fresh folder outside every allow-list
	 * @throws IOException If the file cannot be written.
	 */
	@Test
	void onlyAFilePolicyRefusalCountsAsADenial(@TempDir Path directory) throws IOException {
		Path file = Files.writeString(directory.resolve("plain.txt"), "protected");
		for (AOPMode mode : BACKENDS) {
			configure(mode, new String[0]);
			assertThat(isFilePolicyDenial(denial(mode, "read", read(file)))).as(mode.name()).isTrue();
		}
		assertThat(isFilePolicyDenial(
				new SecurityException(Messages.localized("security.advice.startup.properties.unavailable")))).isFalse();
		assertThat(isFilePolicyDenial(
				new SecurityException(Messages.localized("security.advice.trusted.startup.missing")))).isFalse();
	}

	/**
	 * A missing archive method name fails closed on both backends, even though the
	 * synthetic AspectJ signature cannot format that name.
	 *
	 * @param directory a fresh test folder
	 */
	@Test
	void nullArchiveMethodMetadataIsRefused(@TempDir Path directory) {
		Call call = malformedArchiveEntryRead(directory, null);
		try {
			for (AOPMode mode : BACKENDS) {
				configure(mode, new String[0]);
				assertThatThrownBy(() -> BaselineAdviceProbe.check(mode, "read", call)).as(mode.name())
						.isInstanceOf(SecurityException.class);
			}
		} finally {
			close(call.instance());
		}
	}

	/**
	 * A blank or missing method name is refused on both backends, even when the
	 * call names no path the policy forbids.
	 *
	 * @param method    the method name, blank or missing
	 * @param directory a fresh test folder
	 */
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "  " })
	void blankMethodNameIsRefused(String method, @TempDir Path directory) {
		Call call = archiveCallWithoutPath(directory, method);
		try {
			for (AOPMode mode : BACKENDS) {
				configure(mode, new String[] { directory.resolve("archive.jar").toString() });
				assertThatThrownBy(() -> BaselineAdviceProbe.check(mode, "read", call)).as(mode.name())
						.isInstanceOf(SecurityException.class).hasMessageContaining("malformed method");
			}
		} finally {
			close(call.instance());
		}
	}

	/**
	 * A blank or missing class name is refused by instrumentation. AspectJ takes
	 * the class from the call itself, so this case exists on instrumentation only.
	 *
	 * @param owner     the class name, blank or missing
	 * @param directory a fresh test folder
	 */
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "  " })
	void blankClassNameIsRefusedByInstrumentation(String owner, @TempDir Path directory) {
		Call call = archiveCallWithoutPath(directory, "entries");
		try {
			configure(AOPMode.INSTRUMENTATION, new String[] { directory.resolve("archive.jar").toString() });
			assertThatThrownBy(() -> BaselineAdviceProbe.checkInstrumentationAs(owner, "read", call))
					.isInstanceOf(SecurityException.class).hasMessageContaining("malformed method");
		} finally {
			close(call.instance());
		}
	}

	/**
	 * An archive entry method cannot be called without an open archive receiver.
	 *
	 * @param directory a fresh test folder
	 */
	@Test
	void archiveEntryWithoutReceiverIsRefused(@TempDir Path directory) {
		Call call = new Call(JarFile.class, "getInputStream", "(java.util.zip.ZipEntry)",
				new Class<?>[] { ZipEntry.class },
				new Object[] { new ZipEntry(file(directory, "forbidden.txt").toString()) }, null);
		for (AOPMode mode : BACKENDS) {
			configure(mode, new String[0]);
			assertThatThrownBy(() -> BaselineAdviceProbe.check(mode, "read", call)).as(mode.name())
					.isInstanceOf(SecurityException.class);
		}
	}

	/**
	 * A student archive subclass cannot run its override inside the advice guard.
	 *
	 * @param directory a fresh test folder
	 * @throws IOException if the test archive cannot be opened
	 */
	@Test
	void untrustedArchiveOverrideIsNeverInvoked(@TempDir Path directory) throws IOException {
		UntrustedZipFile archive = new UntrustedZipFile(archive(directory));
		try {
			Call call = new Call(ZipFile.class, "entries", "()", new Class<?>[0], new Object[0], archive);
			for (AOPMode mode : BACKENDS) {
				configure(mode, new String[0]);
				assertThatThrownBy(() -> BaselineAdviceProbe.check(mode, "read", call)).as(mode.name())
						.isInstanceOf(SecurityException.class);
				assertThat(archive.sizeCalled).as(mode.name()).isFalse();
			}
		} finally {
			archive.close();
		}
	}

	/**
	 * A student jar subclass cannot run its override inside the advice guard.
	 *
	 * @param directory a fresh test folder
	 * @throws IOException if the test jar cannot be opened
	 */
	@Test
	void untrustedJarOverrideIsNeverInvoked(@TempDir Path directory) throws IOException {
		UntrustedJarFile jar = new UntrustedJarFile(archive(directory));
		try {
			Call call = new Call(JarFile.class, "entries", "()", new Class<?>[0], new Object[0], jar);
			for (AOPMode mode : BACKENDS) {
				configure(mode, new String[0]);
				assertThatThrownBy(() -> BaselineAdviceProbe.check(mode, "read", call)).as(mode.name())
						.isInstanceOf(SecurityException.class);
				assertThat(jar.sizeCalled).as(mode.name()).isFalse();
			}
		} finally {
			jar.close();
		}
	}

	/**
	 * A JDK jar subclass obtained through a local URL can read entries on both
	 * backends. Its loader is trusted even though its class differs from JarFile.
	 *
	 * @param directory a fresh test folder
	 * @throws IOException if the jar URL cannot be opened
	 */
	@Test
	void jdkJarUrlSubclassCanReadEntries(@TempDir Path directory) throws IOException {
		JarURLConnection connection = (JarURLConnection) URI.create("jar:" + archive(directory).toUri() + "!/").toURL()
				.openConnection();
		connection.setUseCaches(false);
		try (JarFile jar = connection.getJarFile()) {
			assertThat(jar.getClass()).isNotEqualTo(JarFile.class);
			Call entry = entryRead(JarFile.class, jar, jar.getEntry("entry.txt"));
			Call entries = archiveEntries(entry);
			for (AOPMode mode : BACKENDS) {
				configure(mode, new String[0]);
				assertThat(verdictOf(mode, "read", entry)).as(mode.name()).isEqualTo(Verdict.ALLOW);
				assertThat(verdictOf(mode, "read", entries)).as(mode.name()).isEqualTo(Verdict.ALLOW);
			}
		}
	}

	/**
	 * Neither backend keeps a list of file names that are allowed wherever they
	 * lie.
	 */
	@Test
	void neitherBackendExemptsFilesByName() {
		assertThat(hasField(JavaInstrumentationAdviceFileSystemToolbox.class, "INTERNAL_PATH_SUFFIXES")).isFalse();
		assertThat(hasField(JavaAspectJFileSystemAdviceDefinitions.class, "INTERNAL_PATH_SUFFIXES")).isFalse();
	}

	/**
	 * ArchUnit and WALA allow the same JDK calls without a policy entry, although
	 * each spells them in its own notation.
	 */
	@Test
	void bothStaticBackendsAllowTheSameCalls() {
		Collection<String> archUnit = constant(TransitivelyAccessesMethodsCondition.class, "STANDARD_ALLOWED_CALLS");
		Collection<String> wala = constant(ForbiddenMethodMatcher.class, "STANDARD_ALLOWED_CALLS");
		assertThat(archUnit).isNotEmpty().hasSameSizeAs(wala).allMatch(ForbiddenMethodMatcher::isStandardAllowed,
				"allowed by the WALA matcher too");
	}

	/**
	 * The rows, built from the test folder at run time.
	 *
	 * @return every row
	 */
	static Stream<Row> rows() {
		Path javaHome = Path.of(System.getProperty("java.home"));
		Path javaHomeFile = javaHome.resolve("release");
		Path nativeLibrary = javaHome.resolve("lib").resolve(System.mapLibraryName("net"));
		Path workingDirectory = Path.of(System.getProperty("user.dir"));
		Path defaultTempDirectory = Path.of(System.getProperty("java.io.tmpdir"));
		return Stream.of(row("java.home read", "read", none(), dir -> read(javaHomeFile), Verdict.ALLOW, Verdict.ALLOW),
				row("java.home create", "create", none(), dir -> read(javaHomeFile), Verdict.DENY, Verdict.DENY),
				row("java.home overwrite", "overwrite", none(), dir -> read(javaHomeFile), Verdict.DENY, Verdict.DENY),
				row("java.home delete", "delete", none(), dir -> read(javaHomeFile), Verdict.DENY, Verdict.DENY),
				row("java.home native library execute", "execute", none(), dir -> read(nativeLibrary), Verdict.ALLOW,
						Verdict.ALLOW),
				row("java.home other file execute", "execute", none(), dir -> read(javaHomeFile), Verdict.DENY,
						Verdict.DENY),
				row("native library by bare name", "execute", none(), dir -> read(workingDirectory.resolve("net")),
						Verdict.ALLOW, Verdict.ALLOW),
				row("native library by file name", "execute", none(),
						dir -> read(workingDirectory.resolve(System.mapLibraryName("net"))), Verdict.DENY,
						Verdict.DENY),
				row("repository jar read", "read", none(), dir -> read(trustedRepositoryJar()), Verdict.ALLOW,
						Verdict.ALLOW),
				row("repository jar overwrite", "overwrite", none(), dir -> read(trustedRepositoryJar()), Verdict.DENY,
						Verdict.DENY),
				row("repository non-jar read", "read", none(),
						dir -> read(siblingWithExtension(trustedRepositoryJar(), ".pom")), Verdict.DENY, Verdict.DENY),
				row("jar outside the repository read", "read", none(), dir -> read(file(dir, "library.jar")),
						Verdict.DENY, Verdict.DENY),
				row("Ares message file name read", "read", none(), dir -> read(lookalikeMessages(dir)), Verdict.DENY,
						Verdict.DENY),
				row("Ares message file name overwrite", "overwrite", none(), dir -> read(lookalikeMessages(dir)),
						Verdict.DENY, Verdict.DENY),
				row("Ares message file name delete", "delete", none(), dir -> read(lookalikeMessages(dir)),
						Verdict.DENY, Verdict.DENY),
				row("Ares essential classes file name overwrite", "overwrite", none(),
						dir -> read(lookalikeEssentialClasses(dir)), Verdict.DENY, Verdict.DENY),
				row("JDK cryptography policy file name read", "read", none(),
						dir -> read(file(dir, "default_local.policy")), Verdict.DENY, Verdict.DENY),
				row("random seed device read by student code", "read", none(), dir -> read(Path.of("/dev/urandom")),
						Verdict.DENY, Verdict.DENY),
				row("Files temp file without a directory", "create", none(), dir -> filesTempFile(null), Verdict.ALLOW,
						Verdict.ALLOW),
				row("File temp file without a directory", "create", none(), dir -> fileTempFile(null), Verdict.ALLOW,
						Verdict.ALLOW),
				row("Files temp file in the default directory", "create", none(),
						dir -> filesTempFile(defaultTempDirectory), Verdict.ALLOW, Verdict.ALLOW),
				row("Files temp file in another directory", "create", none(),
						BaselineExemptionParityTest::filesTempFile, Verdict.DENY, Verdict.DENY),
				row("File temp file in another directory", "create", none(), dir -> fileTempFile(dir.toFile()),
						Verdict.DENY, Verdict.DENY),
				row("temp directory", "create", none(), dir -> tempDirectory(), Verdict.DENY, Verdict.DENY),
				row("opening an unlisted archive", "read", none(), dir -> openArchive(archive(dir)), Verdict.DENY,
						Verdict.DENY),
				row("entry of an open unlisted jar", "read", none(), dir -> jarEntryRead(archive(dir)), Verdict.ALLOW,
						Verdict.ALLOW),
				row("entry of an open unlisted zip", "read", none(), dir -> zipEntryRead(archive(dir)), Verdict.ALLOW,
						Verdict.ALLOW),
				row("entries of an open unlisted jar", "read", none(),
						dir -> archiveEntries(jarEntryRead(archive(dir))), Verdict.ALLOW, Verdict.ALLOW),
				row("entries of an open unlisted zip", "read", none(),
						dir -> archiveEntries(zipEntryRead(archive(dir))), Verdict.ALLOW, Verdict.ALLOW),
				row("entry of an open listed jar", "read",
						dir -> new String[] { dir.resolve("archive.jar").toString() },
						dir -> jarEntryRead(archive(dir)), Verdict.ALLOW, Verdict.ALLOW),
				row("unknown archive method with a forbidden path", "read", none(),
						dir -> malformedArchiveEntryRead(dir, "unknown"), Verdict.DENY, Verdict.DENY),
				row("malformed archive entries call with a forbidden path", "read", none(),
						dir -> malformedArchiveEntryRead(dir, "entries"), Verdict.DENY, Verdict.DENY),
				row("malformed archive getInputStream call with a forbidden path", "read", none(),
						dir -> malformedArchiveEntryRead(dir, "getInputStream"), Verdict.DENY, Verdict.DENY),
				row("opening a listed archive", "read", dir -> new String[] { dir.resolve("archive.jar").toString() },
						dir -> openArchive(archive(dir)), Verdict.ALLOW, Verdict.ALLOW),
				row("missing allow-list", "read", dir -> null, dir -> read(file(dir, "plain.txt")), Verdict.DENY,
						Verdict.DENY),
				row("empty allow-list", "read", none(), dir -> read(file(dir, "plain.txt")), Verdict.DENY,
						Verdict.DENY),
				row("wildcard allow-list", "read", dir -> new String[] { "*" }, dir -> read(file(dir, "plain.txt")),
						Verdict.ALLOW, Verdict.ALLOW),
				row("listed folder", "read", dir -> new String[] { dir.toString() },
						dir -> read(file(dir, "plain.txt")), Verdict.ALLOW, Verdict.ALLOW));
	}

	/**
	 * Builds a row.
	 *
	 * @param name            what the row checks
	 * @param action          the file-system action
	 * @param allowList       the allow-list, from the test folder
	 * @param call            the call, from the test folder
	 * @param instrumentation the verdict expected from instrumentation
	 * @param aspectJ         the verdict expected from AspectJ
	 * @return the row
	 */
	private static Row row(String name, String action, Function<Path, String[]> allowList, Function<Path, Call> call,
			Verdict instrumentation, Verdict aspectJ) {
		return new Row(name, action, allowList, call, instrumentation, aspectJ);
	}

	/**
	 * An empty allow-list, the state of a policy that grants no file access.
	 *
	 * @return the allow-list provider
	 */
	private static Function<Path, String[]> none() {
		return dir -> new String[0];
	}

	/**
	 * Runs a row through both backends, then closes the call's receiver if it holds
	 * a file open, such as an archive.
	 *
	 * @param row       the row
	 * @param directory the test folder
	 * @return each backend's verdict
	 */
	private static Map<AOPMode, Verdict> verdicts(Row row, Path directory) {
		Call call = row.call().apply(directory);
		try {
			String[] allowList = row.allowList().apply(directory);
			Map<AOPMode, Verdict> verdicts = new EnumMap<>(AOPMode.class);
			for (AOPMode mode : BACKENDS) {
				configure(mode, allowList);
				verdicts.put(mode, verdictOf(mode, row.action(), call));
			}
			return verdicts;
		} finally {
			close(call.instance());
		}
	}

	/**
	 * Closes a call's receiver if it holds a file open, so the test folder can be
	 * deleted on every platform.
	 *
	 * @param instance the receiver, or {@code null}
	 */
	private static void close(Object instance) {
		if (instance instanceof Closeable closeable) {
			try {
				closeable.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}
	}

	/**
	 * Runs one call through one backend.
	 *
	 * @param mode   the backend
	 * @param action the file-system action
	 * @param call   the call
	 * @return the backend's verdict
	 */
	private static Verdict verdictOf(AOPMode mode, String action, Call call) {
		try {
			BaselineAdviceProbe.check(mode, action, call);
			return Verdict.ALLOW;
		} catch (SecurityException denied) {
			if (!isFilePolicyDenial(denied)) {
				throw new AssertionError(mode + " refused the call for a reason other than the file policy", denied);
			}
			return Verdict.DENY;
		}
	}

	/**
	 * Tells whether a refusal is the file policy's own, rather than a failure such
	 * as missing start-up settings, which also arrives as a
	 * {@code SecurityException} but proves nothing about the policy.
	 *
	 * @param refusal the refusal
	 * @return {@code true} if it has the shape of a file-policy denial
	 */
	private static boolean isFilePolicyDenial(SecurityException refusal) {
		return refusal.getMessage() != null && filePolicyDenialShape().matcher(refusal.getMessage()).matches();
	}

	/**
	 * The file-policy denial message in the current language, with each of its
	 * values allowed to be any text.
	 *
	 * @return the pattern
	 */
	private static Pattern filePolicyDenialShape() {
		String marker = "\0";
		String template = Messages.localized("security.advice.illegal.file.execution", marker, marker, marker, marker);
		return Pattern.compile(
				Stream.of(template.split(marker, -1)).map(Pattern::quote).collect(Collectors.joining(".*")),
				Pattern.DOTALL);
	}

	/**
	 * Runs a call that must be denied and returns the denial.
	 *
	 * @param mode   the backend
	 * @param action the file-system action
	 * @param call   the call
	 * @return the denial
	 */
	private static SecurityException denial(AOPMode mode, String action, Call call) {
		try {
			BaselineAdviceProbe.check(mode, action, call);
		} catch (SecurityException denied) {
			return denied;
		}
		throw new AssertionError(mode + " allowed a call it should deny");
	}

	/**
	 * Publishes the policy values the selected backend reads.
	 *
	 * @param mode      the backend
	 * @param allowList the allow-list for every action, possibly null
	 */
	private static void configure(AOPMode mode, String[] allowList) {
		AOPMode.ASPECTJ.reset();
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", mode.name(), "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCHUNIT", mode.name());
		for (String setting : ALLOW_LIST_SETTINGS) {
			JavaAOPTestCase.setJavaAdviceSettingValue(setting, allowList, "ARCHUNIT", mode.name());
		}
	}

	/**
	 * A call to {@code Files.readAllBytes(path)}, standing for any call naming one
	 * path; the action decides what the backends check.
	 *
	 * @param path the path
	 * @return the call
	 */
	private static Call read(Path path) {
		return new Call(Files.class, "readAllBytes", "(Ljava/nio/file/Path;)[B", new Class<?>[] { Path.class },
				new Object[] { path }, null);
	}

	/**
	 * A call to {@code Files.createTempFile}, with or without a directory.
	 *
	 * @param directory the directory, or {@code null} for the overload without
	 * @return the call
	 */
	private static Call filesTempFile(Path directory) {
		if (directory == null) {
			return new Call(Files.class, "createTempFile",
					"(Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;",
					new Class<?>[] { String.class, String.class, FileAttribute[].class },
					new Object[] { "ares", ".tmp", NO_ATTRIBUTES }, null);
		}
		return new Call(Files.class, "createTempFile",
				"(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;",
				new Class<?>[] { Path.class, String.class, String.class, FileAttribute[].class },
				new Object[] { directory, "ares", ".tmp", NO_ATTRIBUTES }, null);
	}

	/**
	 * A call to {@code File.createTempFile}, with or without a directory.
	 *
	 * @param directory the directory, or {@code null} for the overload without
	 * @return the call
	 */
	private static Call fileTempFile(File directory) {
		if (directory == null) {
			return new Call(File.class, "createTempFile", "(Ljava/lang/String;Ljava/lang/String;)Ljava/io/File;",
					new Class<?>[] { String.class, String.class }, new Object[] { "ares", ".tmp" }, null);
		}
		return new Call(File.class, "createTempFile",
				"(Ljava/lang/String;Ljava/lang/String;Ljava/io/File;)Ljava/io/File;",
				new Class<?>[] { String.class, String.class, File.class }, new Object[] { "ares", ".tmp", directory },
				null);
	}

	/**
	 * A call to {@code Files.createTempDirectory(prefix)}.
	 *
	 * @return the call
	 */
	private static Call tempDirectory() {
		return new Call(Files.class, "createTempDirectory",
				"(Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;",
				new Class<?>[] { String.class, FileAttribute[].class }, new Object[] { "ares", NO_ATTRIBUTES }, null);
	}

	/**
	 * A call to {@code new JarFile(path)}.
	 *
	 * @param archive the archive
	 * @return the call
	 */
	private static Call openArchive(Path archive) {
		return new Call(JarFile.class, "<init>", "(Ljava/lang/String;)V", new Class<?>[] { String.class },
				new Object[] { archive.toString() }, null);
	}

	/**
	 * A call to {@code getInputStream} on a jar the test opened itself, outside
	 * student code.
	 *
	 * @param archive the archive
	 * @return the call
	 */
	private static Call jarEntryRead(Path archive) {
		try {
			JarFile jar = new JarFile(archive.toFile());
			return entryRead(JarFile.class, jar, jar.getEntry("entry.txt"));
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * A call to {@code getInputStream} on a zip the test opened itself, outside
	 * student code.
	 *
	 * @param archive the archive
	 * @return the call
	 */
	private static Call zipEntryRead(Path archive) {
		try {
			ZipFile zip = new ZipFile(archive.toFile());
			return entryRead(ZipFile.class, zip, zip.getEntry("entry.txt"));
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * A call to {@code getInputStream(entry)} on an open archive.
	 *
	 * @param owner   the archive class
	 * @param archive the open archive
	 * @param entry   the entry
	 * @return the call
	 */
	private static Call entryRead(Class<?> owner, ZipFile archive, ZipEntry entry) {
		return new Call(owner, "getInputStream", "(java.util.zip.ZipEntry)", new Class<?>[] { ZipEntry.class },
				new Object[] { entry }, archive);
	}

	/**
	 * A call with the given method name on a zip the test opened itself. It names
	 * no path the policy forbids, so only the name can make a backend refuse it.
	 *
	 * @param directory the test folder
	 * @param method    the method name
	 * @return the call
	 */
	private static Call archiveCallWithoutPath(Path directory, String method) {
		return new Call(ZipFile.class, method, "()", new Class<?>[0], new Object[0],
				zipEntryRead(archive(directory)).instance());
	}

	/**
	 * Describes the argument-free entry enumeration of an already-open archive.
	 *
	 * @param entryCall a call holding the open archive
	 * @return an enumeration call on the same archive
	 */
	private static Call archiveEntries(Call entryCall) {
		return new Call(entryCall.owner(), "entries", "()", new Class<?>[0], new Object[0], entryCall.instance());
	}

	/**
	 * Describes an impossible archive-entry call that smuggles a file path through
	 * its arguments. Both checkers must validate it as an ordinary file read.
	 *
	 * @param directory the test folder
	 * @param method    an absent, unknown, or wrongly shaped method name
	 * @return the malformed call
	 */
	private static Call malformedArchiveEntryRead(Path directory, String method) {
		Call openCall = jarEntryRead(archive(directory));
		Path forbidden = file(directory, "forbidden.txt");
		return new Call(JarFile.class, method, "(Ljava/nio/file/Path;)V", new Class<?>[] { Path.class },
				new Object[] { forbidden }, openCall.instance());
	}

	/**
	 * Writes a jar with one entry into the test folder.
	 *
	 * @param directory the test folder
	 * @return the jar
	 */
	private static Path archive(Path directory) {
		Path archive = directory.resolve("archive.jar");
		try (OutputStream out = Files.newOutputStream(archive); JarOutputStream jar = new JarOutputStream(out)) {
			jar.putNextEntry(new ZipEntry("entry.txt"));
			jar.write("entry".getBytes(StandardCharsets.UTF_8));
			jar.closeEntry();
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
		return archive;
	}

	/**
	 * Writes a file into the test folder.
	 *
	 * @param directory the test folder
	 * @param name      the file name
	 * @return the file
	 */
	private static Path file(Path directory, String name) {
		try {
			return Files.writeString(directory.resolve(name), "content");
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * Writes a file whose path ends like Ares's English message file.
	 *
	 * @param directory the test folder
	 * @return the file
	 */
	private static Path lookalikeMessages(Path directory) {
		return nested(directory, "ares/api/localization/messages.properties");
	}

	/**
	 * Writes a file whose path ends like Ares's list of essential classes.
	 *
	 * @param directory the test folder
	 * @return the file
	 */
	private static Path lookalikeEssentialClasses(Path directory) {
		return nested(directory, "ares/api/configuration/essentialFiles/java/EssentialClasses.yaml");
	}

	/**
	 * Writes a file below the test folder, creating its folders.
	 *
	 * @param directory the test folder
	 * @param relative  the path below it
	 * @return the file
	 */
	private static Path nested(Path directory, String relative) {
		try {
			Path file = directory.resolve(relative);
			Files.createDirectories(file.getParent());
			return Files.writeString(file, "content");
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * JUnit's jar, provided it lies in the Maven repository both backends trust.
	 * Aborts the row otherwise, for example when the build used another
	 * {@code maven.repo.local} than the test run can see.
	 *
	 * @return the jar
	 */
	private static Path trustedRepositoryJar() {
		Path repository = Path.of(trustedMavenRepository()).toAbsolutePath().normalize();
		Path jar = codeSourceOf(Test.class).toAbsolutePath().normalize();
		Assumptions.assumeTrue(jar.startsWith(repository),
				() -> "JUnit's jar " + jar + " lies outside the trusted Maven repository " + repository);
		return jar;
	}

	/**
	 * The Maven repository root instrumentation trusts for jar reads.
	 *
	 * @return the root
	 */
	private static String trustedMavenRepository() {
		return (String) staticValue(JavaInstrumentationAdviceFileSystemToolbox.class, "TRUSTED_MAVEN_REPOSITORY");
	}

	/**
	 * The jar a class was loaded from.
	 *
	 * @param type a class from a dependency
	 * @return its jar
	 */
	private static Path codeSourceOf(Class<?> type) {
		try {
			return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (java.net.URISyntaxException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * The file next to a jar with the same name and another extension.
	 *
	 * @param jar       the jar
	 * @param extension the other extension
	 * @return that file
	 */
	private static Path siblingWithExtension(Path jar, String extension) {
		String name = jar.getFileName().toString();
		return jar.resolveSibling(name.substring(0, name.length() - ".jar".length()) + extension);
	}

	/**
	 * Reads a private static constant of a backend.
	 *
	 * @param owner the backend class
	 * @param name  the constant
	 * @return its value
	 */
	@SuppressWarnings("unchecked")
	private static Collection<String> constant(Class<?> owner, String name) {
		return (Collection<String>) staticValue(owner, name);
	}

	/**
	 * Reads a private static field of a backend.
	 *
	 * @param owner the backend class
	 * @param name  the field
	 * @return its value
	 */
	private static Object staticValue(Class<?> owner, String name) {
		try {
			Field field = owner.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(null);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(owner.getName() + " has no readable " + name, exception);
		}
	}

	/**
	 * Tells whether a backend declares a field.
	 *
	 * @param owner the backend class
	 * @param name  the field
	 * @return {@code true} if it does
	 */
	private static boolean hasField(Class<?> owner, String name) {
		return Stream.of(owner.getDeclaredFields()).anyMatch(field -> field.getName().equals(name));
	}
}
