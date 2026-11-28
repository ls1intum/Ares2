package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.ForeignCallBytecode;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesManagement;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

/**
 * Runs the WALA architecture test case against small fixtures and checks which
 * rule it runs, when it builds the call graph, how it caches a verdict, how it
 * reports a failure, and what it writes into a generated test. Every run of a
 * cached category carries a package permission no other test used, so no
 * verdict saved on disk by an earlier test run applies.
 */
class JavaWalaTestCaseExecutionTest {

	/** The scope every test case here is pinned to. */
	private static final String SCOPE = "student.pkg";

	/** The categories that delegate to ArchUnit and skip the verdict cache. */
	private static final Set<JavaArchitectureTestCaseSupported> DELEGATED = Set.of(
			JavaArchitectureTestCaseSupported.NATIVE_CODE, JavaArchitectureTestCaseSupported.AGENT_ATTACH,
			JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS, JavaArchitectureTestCaseSupported.MODULE_SYSTEM,
			JavaArchitectureTestCaseSupported.JNDI_INJECTION);

	/** A folder for the files a test writes. */
	@TempDir
	Path temporaryDirectory;

	/** A class that touches nothing any rule forbids. */
	static class CleanFixture {

		/** Does nothing. */
		void idle() {
		}
	}

	/** A class that deletes a file, which the file-system rule forbids. */
	static class DeletingFixture {

		/**
		 * Deletes the given path.
		 *
		 * @param path the path to delete
		 * @throws IOException if the deletion fails
		 */
		void remove(Path path) throws IOException {
			Files.delete(path);
		}
	}

	/** A class that uses {@code java.util}, which is outside java.lang. */
	static class ListFixture {

		/**
		 * Returns an empty list.
		 *
		 * @return an empty list
		 */
		Object list() {
			return List.of();
		}
	}

	/** A class that loads a native library, which the native-code rule forbids. */
	static class LoadingFixture {

		/** Loads the library named x. */
		void load() {
			System.loadLibrary("x");
		}
	}

	/**
	 * Returns a package permission no other test case has used, so no cached
	 * verdict applies.
	 *
	 * @return a fresh permission
	 */
	private static PackagePermission uniquePermission() {
		return new PackagePermission("unique" + UUID.randomUUID().toString().replace("-", ""));
	}

	/**
	 * Builds a WALA test case pinned to {@link #SCOPE} that reads its call graph
	 * from the supplier.
	 *
	 * @param supported the category
	 * @param classes   the classes to check
	 * @param supplier  the call graph supplier
	 * @param extra     the permitted packages
	 * @return the test case
	 */
	private static JavaWalaTestCase testCase(JavaArchitectureTestCaseSupported supported, JavaClasses classes,
			Supplier<CallGraph> supplier, PackagePermission... extra) {
		return new JavaWalaTestCase(supported, Set.of(extra), classes, supplier, SCOPE, false);
	}

	/**
	 * Returns a supplier that counts its calls and hands out a mock call graph
	 * without entry points.
	 *
	 * @param calls the counter
	 * @return the supplier
	 */
	private static Supplier<CallGraph> countingSupplier(AtomicInteger calls) {
		CallGraph graph = Mockito.mock(CallGraph.class);
		return () -> {
			calls.incrementAndGet();
			return graph;
		};
	}

	/**
	 * Opens a private member of the WALA test case.
	 *
	 * @param name  the method name
	 * @param types the parameter types
	 * @return the accessible method
	 * @throws ReflectiveOperationException if the method is missing
	 */
	private static Method privateMethod(String name, Class<?>... types) throws ReflectiveOperationException {
		Method method = JavaWalaTestCase.class.getDeclaredMethod(name, types);
		method.setAccessible(true);
		return method;
	}

	/**
	 * Reads a private static field of the WALA test case.
	 *
	 * @param name the field name
	 * @return the field's value
	 * @throws ReflectiveOperationException if the field is missing
	 */
	private static Object privateField(String name) throws ReflectiveOperationException {
		Field field = JavaWalaTestCase.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}

	/**
	 * Returns the key a test case files its verdict under.
	 *
	 * @param testCase the test case
	 * @return its cache key
	 * @throws ReflectiveOperationException if the method is missing
	 */
	private static String cacheKeyOf(JavaWalaTestCase testCase) throws ReflectiveOperationException {
		return (String) privateMethod("cacheKey").invoke(testCase);
	}

	/**
	 * Returns the in-memory map of cached verdicts.
	 *
	 * @return the map, keyed by cache key
	 * @throws ReflectiveOperationException if the field is missing
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, ?> outcomeCache() throws ReflectiveOperationException {
		return (Map<String, ?>) privateField("RULE_OUTCOME_CACHE");
	}

	/**
	 * Returns the map of evaluations still running, keyed by cache key.
	 *
	 * @return the map
	 * @throws ReflectiveOperationException if the field is missing
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> ruleEvaluations() throws ReflectiveOperationException {
		return (Map<String, Object>) privateField("RULE_EVALUATIONS");
	}

	/**
	 * Throws any throwable, a checked one too, without declaring it.
	 *
	 * @param <T>     the type the compiler takes the throwable for
	 * @param failure the throwable to throw
	 * @throws T always
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow(Throwable failure) throws T {
		throw (T) failure;
	}

	/**
	 * Returns the opening words of an Ares architecture violation, in the current
	 * language.
	 *
	 * @return the text before the first filled-in value
	 */
	private static String violationPrefix() {
		String marker = "@@marker@@";
		String filled = Messages.localized("security.archunit.violation.error", marker, marker, marker, marker);
		return filled.substring(0, filled.indexOf(marker));
	}

	/**
	 * Every call-graph category with the WALA rule it runs.
	 *
	 * @return rows of category and rule
	 */
	static Stream<Arguments> callGraphRules() {
		return Stream.of(
				Arguments.of(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM),
				Arguments.of(JavaArchitectureTestCaseSupported.NETWORK_CONNECTION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK),
				Arguments.of(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS),
				Arguments.of(JavaArchitectureTestCaseSupported.THREAD_CREATION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS),
				Arguments.of(JavaArchitectureTestCaseSupported.REFLECTION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION),
				Arguments.of(JavaArchitectureTestCaseSupported.TERMINATE_JVM,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM),
				Arguments.of(JavaArchitectureTestCaseSupported.SERIALIZATION,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_SERIALIZE),
				Arguments.of(JavaArchitectureTestCaseSupported.CLASS_LOADING,
						JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS));
	}

	/**
	 * The six categories that check classes or delegate to ArchUnit never build the
	 * call graph and pass a clean class; the other eight build it once and refuse
	 * the mock graph, which has no entry points.
	 *
	 * @param supported the category
	 */
	@ParameterizedTest
	@EnumSource(JavaArchitectureTestCaseSupported.class)
	void asksForTheCallGraphOnlyWhereARuleNeedsIt(JavaArchitectureTestCaseSupported supported) {
		boolean needsCallGraph = !(supported == JavaArchitectureTestCaseSupported.PACKAGE_IMPORT
				|| DELEGATED.contains(supported));
		AtomicInteger calls = new AtomicInteger();
		JavaWalaTestCase testCase = testCase(supported, new ClassFileImporter().importClasses(CleanFixture.class),
				countingSupplier(calls), new PackagePermission("java.lang"), uniquePermission());
		Executable run = () -> testCase.executeArchitectureTestCase("WALA", "");

		if (needsCallGraph) {
			assertThrows(SecurityException.class, run);
		} else {
			assertDoesNotThrow(run);
		}

		assertEquals(needsCallGraph ? 1 : 0, calls.get());
	}

	/**
	 * Each call-graph category runs its own WALA rule, which names itself when it
	 * refuses a graph without entry points.
	 *
	 * @param supported the category
	 * @param rule      the rule the category runs
	 */
	@ParameterizedTest
	@MethodSource("callGraphRules")
	void runsTheCallGraphRuleOfEachCategory(JavaArchitectureTestCaseSupported supported, WalaRule rule) {
		JavaWalaTestCase testCase = testCase(supported, new ClassFileImporter().importClasses(CleanFixture.class),
				countingSupplier(new AtomicInteger()), uniquePermission());

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));

		assertEquals(Messages.localized("security.architecture.wala.entrypoints.empty", rule.ruleName),
				thrown.getMessage());
	}

	/**
	 * A forbidden call written directly in the class is reported before the call
	 * graph is walked, worded as an Ares violation that names the calling method
	 * and the action. The whole message is compared only in English, since the
	 * parser recognises only the English wording of a WALA violation.
	 */
	@Test
	void reportsADirectForbiddenCallBeforeTheGraph() {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				new ClassFileImporter().importClasses(DeletingFixture.class), countingSupplier(new AtomicInteger()),
				uniquePermission());
		String caller = DeletingFixture.class.getName() + ".remove(java.nio.file.Path)";
		String action = Messages.localized("security.archunit.action.file.system.access");

		String reported = assertThrows(SecurityException.class, () -> testCase.executeArchitectureTestCase("WALA", ""))
				.getMessage();

		assertNotEquals(Messages.localized("security.architecture.wala.entrypoints.empty",
				JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM.ruleName), reported);
		assertTrue(reported.startsWith(violationPrefix()), reported);
		assertTrue(reported.contains(caller), reported);
		assertTrue(reported.contains(action), reported);
		if (Locale.ENGLISH.getLanguage().equals(Locale.getDefault(Locale.Category.DISPLAY).getLanguage())) {
			assertEquals(
					Messages.localized("security.archunit.violation.error", caller, action,
							"java.nio.file.Files.delete(java.nio.file.Path)", DeletingFixture.class.getName()),
					reported);
		}
	}

	/**
	 * After one run, only the package-import category has filed a verdict; the five
	 * categories that delegate to ArchUnit have not.
	 *
	 * @param supported the category
	 * @throws ReflectiveOperationException if a private member is missing
	 */
	@ParameterizedTest
	@EnumSource(value = JavaArchitectureTestCaseSupported.class, names = { "PACKAGE_IMPORT", "NATIVE_CODE",
			"AGENT_ATTACH", "ENVIRONMENT_ACCESS", "MODULE_SYSTEM", "JNDI_INJECTION" })
	void bypassesTheCacheOnlyForTheFiveDelegatedCategories(JavaArchitectureTestCaseSupported supported)
			throws ReflectiveOperationException {
		JavaWalaTestCase testCase = testCase(supported, new ClassFileImporter().importClasses(CleanFixture.class),
				countingSupplier(new AtomicInteger()), new PackagePermission("java.lang"), uniquePermission());

		testCase.executeArchitectureTestCase("WALA", "");

		assertEquals(!DELEGATED.contains(supported), outcomeCache().containsKey(cacheKeyOf(testCase)));
	}

	/** A violation found by a category that delegates to ArchUnit is thrown. */
	@Test
	void throwsADelegatedViolation() {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.NATIVE_CODE,
				new ClassFileImporter().importClasses(LoadingFixture.class), countingSupplier(new AtomicInteger()),
				uniquePermission());

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));

		assertTrue(thrown.getMessage().contains(LoadingFixture.class.getName()), thrown.getMessage());
	}

	/**
	 * In WALA mode the native-code check refuses a call that reinterprets a memory
	 * segment, a raw memory access reachable below the java.lang baseline.
	 *
	 * @param folder where the calling class is written
	 * @throws IOException if the class cannot be written
	 */
	@Test
	void refusesAForeignMemoryReinterpretation(@TempDir Path folder) throws IOException {
		JavaClasses caller = ForeignCallBytecode.importCallTo(folder, "java/lang/foreign/MemorySegment", "reinterpret",
				"(J)Ljava/lang/foreign/MemorySegment;");
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.NATIVE_CODE, caller,
				countingSupplier(new AtomicInteger()), uniquePermission());

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));

		assertTrue(thrown.getMessage().contains(ForeignCallBytecode.CLASS_NAME), thrown.getMessage());
	}

	/**
	 * In WALA mode the environment check refuses reading the class path through
	 * java.lang.management, which lies below the java.lang baseline.
	 */
	@Test
	void refusesTheManagementRouteToTheClassPath() {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS,
				new ClassFileImporter().importClasses(UsesManagement.class), countingSupplier(new AtomicInteger()),
				uniquePermission());

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));

		assertTrue(thrown.getMessage().contains(UsesManagement.class.getName()), thrown.getMessage());
	}

	/**
	 * A second run with the same key is served the cached violation: the same
	 * message, and the call graph is built only once.
	 */
	@Test
	void servesTheSecondRunFromTheCache() {
		AtomicInteger calls = new AtomicInteger();
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				new ClassFileImporter().importClasses(CleanFixture.class), countingSupplier(calls), uniquePermission());

		String first = assertThrows(SecurityException.class, () -> testCase.executeArchitectureTestCase("WALA", ""))
				.getMessage();
		String second = assertThrows(SecurityException.class, () -> testCase.executeArchitectureTestCase("WALA", ""))
				.getMessage();

		assertFalse(first.isEmpty());
		assertEquals(first, second);
		assertEquals(1, calls.get());
	}

	/**
	 * A cached violation is thrown again and a cached pass passes again.
	 */
	@Test
	void throwsTheCachedViolationAndPassesTheCachedPass() {
		PackagePermission unique = uniquePermission();
		PackagePermission javaLang = new PackagePermission("java.lang");
		JavaWalaTestCase violating = testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				new ClassFileImporter().importClasses(ListFixture.class), countingSupplier(new AtomicInteger()),
				javaLang, unique);
		JavaWalaTestCase clean = testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				new ClassFileImporter().importClasses(CleanFixture.class), countingSupplier(new AtomicInteger()),
				javaLang, unique);

		assertThrows(SecurityException.class, () -> violating.executeArchitectureTestCase("WALA", ""));
		assertThrows(SecurityException.class, () -> violating.executeArchitectureTestCase("WALA", ""));
		assertDoesNotThrow(() -> clean.executeArchitectureTestCase("WALA", ""));
		assertDoesNotThrow(() -> clean.executeArchitectureTestCase("WALA", ""));
	}

	/**
	 * An unchecked exception or an error from building the call graph comes out as
	 * the same object; a checked exception comes out wrapped in an Ares refusal
	 * that keeps it as the cause.
	 */
	@Test
	void rethrowsAFailureOfTheGraphAsItIs() {
		PackagePermission unique = uniquePermission();
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		IllegalStateException unchecked = new IllegalStateException("graph");
		LinkageError error = new LinkageError("graph");
		IOException checked = new IOException("graph");
		JavaWalaTestCase uncheckedCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, classes,
				() -> {
					throw unchecked;
				}, unique);
		JavaWalaTestCase errorCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, classes, () -> {
			throw error;
		}, unique);
		JavaWalaTestCase checkedCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, classes,
				() -> {
					sneakyThrow(checked);
					return null;
				}, unique);

		assertSame(unchecked,
				assertThrows(IllegalStateException.class, () -> uncheckedCase.executeArchitectureTestCase("WALA", "")));
		assertSame(error, assertThrows(LinkageError.class, () -> errorCase.executeArchitectureTestCase("WALA", "")));
		SecurityException wrapped = assertThrows(SecurityException.class,
				() -> checkedCase.executeArchitectureTestCase("WALA", ""));
		assertEquals(Messages.localized("security.architecture.rule.evaluation.failed"), wrapped.getMessage());
		assertSame(checked, wrapped.getCause());
	}

	/** An evaluation that failed is not kept, so the next run tries again. */
	@Test
	void retriesAnEvaluationThatFailed() {
		AtomicInteger calls = new AtomicInteger();
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				new ClassFileImporter().importClasses(CleanFixture.class), () -> {
					calls.incrementAndGet();
					throw new IllegalStateException("graph");
				}, uniquePermission());

		assertThrows(IllegalStateException.class, () -> testCase.executeArchitectureTestCase("WALA", ""));
		assertThrows(IllegalStateException.class, () -> testCase.executeArchitectureTestCase("WALA", ""));

		assertEquals(2, calls.get());
	}

	/**
	 * A run interrupted while it waits for another evaluation of the same key fails
	 * closed and keeps the thread's interrupt flag set. The key is read after the
	 * interrupt, as the run itself reads it.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 */
	@Test
	void stopsWaitingWhenInterrupted() throws ReflectiveOperationException {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				new ClassFileImporter().importClasses(CleanFixture.class), countingSupplier(new AtomicInteger()),
				uniquePermission());
		FutureTask<Object> neverRun = new FutureTask<>(() -> null);
		String key = null;
		Thread.currentThread().interrupt();
		try {
			key = cacheKeyOf(testCase);
			ruleEvaluations().put(key, neverRun);

			SecurityException refused = assertThrows(SecurityException.class,
					() -> testCase.executeArchitectureTestCase("WALA", ""));

			assertEquals(Messages.localized("security.architecture.rule.evaluation.interrupted"), refused.getMessage());
			assertInstanceOf(InterruptedException.class, refused.getCause());
			assertTrue(Thread.interrupted());
		} finally {
			Thread.interrupted();
			if (key != null) {
				ruleEvaluations().remove(key, neverRun);
			}
		}
	}

	/**
	 * A violation without a message is thrown with an empty message rather than
	 * read as a pass, on the first run and again when the second run is served from
	 * the cache.
	 */
	@Test
	void keepsAnEmptyViolationMessage() {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				new ClassFileImporter().importClasses(CleanFixture.class), () -> {
					throw new SecurityException((String) null);
				}, uniquePermission());

		SecurityException first = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));
		SecurityException second = assertThrows(SecurityException.class,
				() -> testCase.executeArchitectureTestCase("WALA", ""));

		assertEquals("", first.getMessage());
		assertEquals("", second.getMessage());
	}

	/**
	 * The two scope-aware constructors that take a ready call graph hand that graph
	 * to the rule, which refuses it for having no entry points.
	 */
	@Test
	void usesTheGraphOfTheEagerConstructors() {
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		CallGraph graph = Mockito.mock(CallGraph.class);
		String expected = Messages.localized("security.architecture.wala.entrypoints.empty",
				JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM.ruleName);
		JavaWalaTestCase plain = new JavaWalaTestCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				Set.of(uniquePermission()), classes, graph, SCOPE, false);
		JavaWalaTestCase withTestClasses = new JavaWalaTestCase(
				JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(uniquePermission()), classes, graph,
				Set.of(), Set.of(), SCOPE, false);

		assertEquals(expected,
				assertThrows(SecurityException.class, () -> plain.executeArchitectureTestCase("WALA", ""))
						.getMessage());
		assertEquals(expected,
				assertThrows(SecurityException.class, () -> withTestClasses.executeArchitectureTestCase("WALA", ""))
						.getMessage());
	}

	/**
	 * Two test cases that differ only in the name of a permitted package have
	 * different keys.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 */
	@Test
	void keysByTheNamesOfThePermittedPackages() throws ReflectiveOperationException {
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		Supplier<CallGraph> supplier = countingSupplier(new AtomicInteger());

		assertNotEquals(
				cacheKeyOf(testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, classes, supplier,
						new PackagePermission("a"))),
				cacheKeyOf(testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, classes, supplier,
						new PackagePermission("b"))));
	}

	/**
	 * The file-system category, which has a list of forbidden methods, and the
	 * package-import category, which has none, have different blocklist
	 * fingerprints.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 */
	@Test
	void fingerprintsTheBlocklistOfTheCategory() throws ReflectiveOperationException {
		Method fingerprint = privateMethod("blocklistFingerprint", JavaArchitectureTestCaseSupported.class);

		assertNotEquals(fingerprint.invoke(null, JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION),
				fingerprint.invoke(null, JavaArchitectureTestCaseSupported.PACKAGE_IMPORT));
	}

	/**
	 * A readable file is read as its content, a missing one as empty text.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 * @throws IOException                  if the file cannot be written
	 */
	@Test
	void readsAFileOrNothing() throws ReflectiveOperationException, IOException {
		Method read = privateMethod("readFileQuietly", Path.class);
		Path file = Files.writeString(temporaryDirectory.resolve("list.txt"), "content");

		assertEquals("content", read.invoke(null, file));
		assertEquals("", read.invoke(null, temporaryDirectory.resolve("missing.txt")));
	}

	/**
	 * Only a real folder that only its owner can use is trusted; a link to one, a
	 * file asked for as a folder, and a folder the group can read are not.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 * @throws IOException                  if a file cannot be created
	 */
	@Test
	void trustsOnlyARealOwnerOnlyDirectory() throws ReflectiveOperationException, IOException {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
		Method trusted = privateMethod("isTrustworthyOwnerOnly", Path.class, boolean.class);
		Path ownerOnly = Files.createDirectory(temporaryDirectory.resolve("owner"));
		Files.setPosixFilePermissions(ownerOnly, PosixFilePermissions.fromString("rwx------"));
		Path link = Files.createSymbolicLink(temporaryDirectory.resolve("link"), ownerOnly);
		Path file = Files.createFile(temporaryDirectory.resolve("file"));
		Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
		Path groupReadable = Files.createDirectory(temporaryDirectory.resolve("group"));
		Files.setPosixFilePermissions(groupReadable, PosixFilePermissions.fromString("rwxr-x---"));

		assertEquals(true, trusted.invoke(null, ownerOnly, true));
		assertEquals(false, trusted.invoke(null, link, true));
		assertEquals(false, trusted.invoke(null, file, true));
		assertEquals(false, trusted.invoke(null, groupReadable, true));
	}

	/**
	 * Compiles one class {@code p.A} whose method returns the given expression into
	 * a folder of its own.
	 *
	 * @param name       the name of the folders
	 * @param expression the expression the method returns
	 * @return the folder that holds {@code p/A.class}
	 * @throws IOException if a file cannot be written
	 */
	private Path classFolderWithClassA(String name, String expression) throws IOException {
		Path sources = Files.createDirectories(temporaryDirectory.resolve(name + "-src").resolve("p"));
		Path source = Files.writeString(sources.resolve("A.java"),
				"package p;\npublic class A {\n\tpublic Object m() {\n\t\treturn " + expression + ";\n\t}\n}\n");
		return JavacFixtures.compileSources(JavacFixtures.testClasses().toString(),
				Files.createDirectory(temporaryDirectory.resolve(name + "-classes")), List.of(source));
	}

	/**
	 * Two classes with the same name but different calls have different keys. Their
	 * class files are deleted after the import and before the keys are read, so
	 * each key holds an empty bytecode hash and the keys differ only by the calls.
	 *
	 * @throws ReflectiveOperationException if a private member is missing
	 * @throws IOException                  if a file cannot be written
	 */
	@Test
	void separatesClassesThatOnlyDifferInTheirAccesses() throws ReflectiveOperationException, IOException {
		PackagePermission unique = uniquePermission();
		Supplier<CallGraph> supplier = countingSupplier(new AtomicInteger());
		Path listingFolder = classFolderWithClassA("list", "java.util.List.of()");
		Path settingFolder = classFolderWithClassA("set", "java.util.Set.of()");
		JavaClasses listing = new ClassFileImporter().importPath(listingFolder);
		JavaClasses setting = new ClassFileImporter().importPath(settingFolder);
		Files.delete(listingFolder.resolve("p").resolve("A.class"));
		Files.delete(settingFolder.resolve("p").resolve("A.class"));

		assertTrue(listing.contain("p.A"));
		assertTrue(setting.contain("p.A"));
		assertNotEquals(
				cacheKeyOf(testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, listing, supplier, unique)),
				cacheKeyOf(testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, setting, supplier, unique)));
	}

	/**
	 * The fingerprint of a code folder counts only regular files named
	 * {@code .class}: a folder named like a class and a text file change nothing.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void skipsAFolderNamedLikeAClassAndOtherFiles() throws IOException {
		Path codeSource = Files.createDirectory(temporaryDirectory.resolve("code"));
		Files.writeString(codeSource.resolve("A.class"), "bytecode");
		String before = JavaWalaTestCase.implementationFingerprint(codeSource);
		Files.createDirectory(codeSource.resolve("ignored.class"));
		Files.writeString(codeSource.resolve("readme.txt"), "notes");

		assertEquals(before, JavaWalaTestCase.implementationFingerprint(codeSource));
	}

	/**
	 * A class path entry that does not exist is left out of the class path
	 * fingerprint.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void leavesOutAClassPathEntryThatDoesNotExist() throws IOException {
		String jar = Files.writeString(temporaryDirectory.resolve("dependency.jar"), "content").toString();
		String missing = temporaryDirectory.resolve("missing.jar").toString();

		assertEquals(JavaWalaTestCase.classpathFingerprint(jar),
				JavaWalaTestCase.classpathFingerprint(missing + File.pathSeparator + jar));
	}

	/**
	 * A generated file-system test builds its call graph from the class path; a
	 * test case without a scope refuses to write one.
	 */
	@Test
	void writesTheCallGraphBuilderIntoTheTest() {
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		Supplier<CallGraph> supplier = countingSupplier(new AtomicInteger());
		JavaWalaTestCase unscoped = new JavaWalaTestCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				Set.of(), classes, supplier, null, false);

		String written = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, classes, supplier)
				.writeArchitectureTestCase("WALA", "");
		SecurityException refused = assertThrows(SecurityException.class,
				() -> unscoped.writeArchitectureTestCase("WALA", ""));

		assertTrue(written.contains(
				"new CustomCallgraphBuilder(System.getProperty(\"java.class.path\")).buildCallGraph(System.getProperty(\"java.class.path\"))"),
				written);
		assertEquals(Messages.localized("security.architecture.scope.missing"), refused.getMessage());
	}

	/**
	 * A generated test checks what Postcompile checks: a delegated category runs
	 * the ArchUnit rule over the classes, and a call-graph category checks the
	 * direct calls before the graph, so a call the graph leaves out is still seen.
	 *
	 * @param supported the category
	 */
	@ParameterizedTest
	@EnumSource(value = JavaArchitectureTestCaseSupported.class, names = "PACKAGE_IMPORT", mode = EnumSource.Mode.EXCLUDE)
	void writesTheChecksPostcompileRuns(JavaArchitectureTestCaseSupported supported) {
		String written = testCase(supported, new ClassFileImporter().importClasses(CleanFixture.class),
				countingSupplier(new AtomicInteger())).writeArchitectureTestCase("WALA", "");

		if (DELEGATED.contains(supported)) {
			assertTrue(written.contains("JavaArchunitTestCaseCollection.") && written.contains(".check(javaClasses);")
					&& !written.contains("callGraph"), written);
		} else {
			assertTrue(
					written.indexOf(".checkDirectAccesses(javaClasses, Set.of(), callGraph.getClassHierarchy());") >= 0
							&& written.indexOf(".checkDirectAccesses(") < written.indexOf(".check(callGraph);"),
					written);
		}
	}

	/**
	 * A cached category's verdict has inputs to compare only once it was looked up,
	 * and they then stay the same until the dependencies or the runtime change. The
	 * mocked call graph has no entry point, so the verdict is a refusal; it is
	 * looked up all the same.
	 */
	@Test
	void aLookedUpVerdictKeepsItsInputsUntilOneChanges() {
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, classes,
				countingSupplier(new AtomicInteger()), new PackagePermission("java.lang"), uniquePermission());
		String dependencies = JavaWalaTestCase.dependencyFingerprintOf(classes);
		assertFalse(testCase.verdictInputsUnchanged(dependencies));

		assertThrows(SecurityException.class, () -> testCase.executeArchitectureTestCase("WALA", ""));

		assertTrue(testCase.verdictInputsUnchanged(JavaWalaTestCase.dependencyFingerprintOf(classes)));
		assertFalse(testCase.verdictInputsUnchanged(dependencies + "changed"));
		Locale original = Locale.getDefault();
		try {
			Locale.setDefault(original.equals(Locale.JAPANESE) ? Locale.GERMAN : Locale.JAPANESE);
			assertFalse(testCase.verdictInputsUnchanged(dependencies));
		} finally {
			Locale.setDefault(original);
		}
	}

	/**
	 * A category that delegates to ArchUnit looks no verdict up and depends on none
	 * of these inputs, so it never ends a reuse.
	 */
	@Test
	void aDelegatedCategoryDependsOnNoVerdictInputs() {
		JavaWalaTestCase testCase = testCase(JavaArchitectureTestCaseSupported.NATIVE_CODE,
				new ClassFileImporter().importClasses(CleanFixture.class), countingSupplier(new AtomicInteger()),
				new PackagePermission("java.lang"));
		assertTrue(testCase.verdictInputsUnchanged("any dependencies"));
	}

	/**
	 * The case the creator builds hands each WALA run to a fresh WALA test case. It
	 * compares the inputs of the verdict its last such run looked up, and answers
	 * {@code false} before any WALA run.
	 */
	@Test
	void theCreatedCaseComparesTheInputsOfItsLastWalaRun() {
		JavaClasses classes = new ClassFileImporter().importClasses(CleanFixture.class);
		de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase createdCase = de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase
				.builder().javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.javaClasses(classes).callGraphSupplier(countingSupplier(new AtomicInteger()))
				.allowedPackages(Set.of(new PackagePermission("java.lang"), uniquePermission()))
				.supervisedPackage(SCOPE).build();
		String dependencies = JavaWalaTestCase.dependencyFingerprintOf(classes);
		assertFalse(createdCase.verdictInputsUnchanged(dependencies));

		assertThrows(SecurityException.class, () -> createdCase.executeArchitectureTestCase("WALA", ""));

		assertTrue(createdCase.verdictInputsUnchanged(dependencies));
		assertFalse(createdCase.verdictInputsUnchanged(dependencies + "changed"));
	}
}
