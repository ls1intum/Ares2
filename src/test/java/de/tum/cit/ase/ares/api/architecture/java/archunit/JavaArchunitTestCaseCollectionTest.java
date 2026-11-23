package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.core.importer.Locations;
import com.tngtech.archunit.lang.ArchRule;

import de.tum.cit.ase.AncestorPackageAresImport;
import de.tum.cit.ase.AncestorPackageAspectJImport;
import de.tum.cit.ase.AncestorPackageByteBuddyImport;
import de.tum.cit.ase.AncestorPackageExercise;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

public class JavaArchunitTestCaseCollectionTest {

	private static Path tempMethodsFile;

	@BeforeAll
	static void setUpResources() throws IOException {
		// Create a temporary file under src/test/resources for readMethodsFromGivenPath
		Path resourcesDir = Paths.get("src/test/resources");
		if (!Files.exists(resourcesDir)) {
			Files.createDirectories(resourcesDir);
		}
		tempMethodsFile = resourcesDir.resolve("tempMethods.txt");
		String content = "#commentLine\nmethodA\n\nmethodB\n#anotherComment";
		Files.writeString(tempMethodsFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	@Test
	void noClassMustImportForbiddenPackages_returnsRuleWithExpectedDescription() {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		allowedPackages.add(new PackagePermission("com.allowed"));
		ArchRule rule = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages);
		String expectedDescription = Messages.localized("security.architecture.package.import");
		assertEquals(expectedDescription, rule.getDescription());
	}

	@Test
	void packageWildcardAllowsEveryImportedPackage() {
		ArchRule rule = JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(Set.of(new PackagePermission("*")));
		assertDoesNotThrow(
				() -> rule.check(new ClassFileImporter().importClasses(JavaArchunitTestCaseCollectionTest.class)));
	}

	/**
	 * A permission for a package above Ares' API covers the ordinary packages below
	 * it, and the fixture really depends on one of them.
	 */
	@Test
	void ancestorPermissionCoversTheOrdinaryPackagesBelowIt() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageExercise.class, "java", "de.tum.cit.ase"));
		assertThrows(AssertionError.class,
				() -> checkImports(AncestorPackageExercise.class, "java", "de.tum.cit.ase.ares.api"));
	}

	/**
	 * A permission for a package above Ares' API does not reach into that API.
	 */
	@Test
	void ancestorPermissionDoesNotReachAresApi() {
		assertThrows(AssertionError.class,
				() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase"));
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageAresImport.class, "java", "de"));
	}

	/**
	 * A permission naming Ares' API, or a package inside it, reaches it.
	 */
	@Test
	void explicitPermissionReachesAresApi() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase",
				"de.tum.cit.ase.ares.api.policy"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase.ares.api"));
	}

	/**
	 * A permission above AspectJ or Byte Buddy reaches neither, and one naming them
	 * reaches them.
	 */
	@Test
	void ancestorPermissionDoesNotReachOtherTrustedNamespaces() {
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageAspectJImport.class, "java", "org"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageAspectJImport.class, "java", "org.aspectj"));
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageByteBuddyImport.class, "java", "net"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageByteBuddyImport.class, "java", "net.bytebuddy"));
	}

	/**
	 * The wildcard still covers every package, trusted namespaces included.
	 */
	@Test
	void wildcardStillReachesTrustedNamespaces() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "*"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageByteBuddyImport.class, "*"));
	}

	/**
	 * The package of the marker annotation the AspectJ build weaves into every
	 * fixture. Ares permits it as an essential package in every mode, so a fixture
	 * carrying the marker does not import a forbidden package.
	 */
	private static final String WOVEN_MARKER_PACKAGE = "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut";

	/**
	 * Checks the import rule over one fixture class, with the woven marker's
	 * package permitted as {@link #WOVEN_MARKER_PACKAGE} explains.
	 *
	 * @param fixture           the class whose imports are checked
	 * @param permittedPackages the permitted packages
	 */
	static void checkImports(Class<?> fixture, String... permittedPackages) {
		Set<PackagePermission> permissions = Stream
				.concat(Arrays.stream(permittedPackages), Stream.of(WOVEN_MARKER_PACKAGE)).map(PackagePermission::new)
				.collect(Collectors.toSet());
		JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(permissions)
				.check(new ClassFileImporter().importClasses(fixture));
	}

	@Test
	void staticRules_areNotNull() {
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_SERIALIZE);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS);
	}

	/**
	 * A class that writes bytes with {@code Files.write}, whose forbidden signature
	 * holds an array.
	 */
	private static final String WRITES_BYTES = """
			package p;
			public class WritesBytes {
				public void run(java.nio.file.Path path) throws java.io.IOException {
					java.nio.file.Files.write(path, new byte[0]);
				}
			}
			""";

	/**
	 * A class that flushes the preferences, which the forbidden list names only by
	 * a prefix.
	 */
	private static final String FLUSHES_PREFERENCES = """
			package p;
			public class FlushesPreferences {
				public void run() throws Exception {
					java.util.prefs.Preferences.userRoot().flush();
				}
			}
			""";

	/**
	 * A class that calls nothing forbidden.
	 */
	private static final String CLEAN = """
			package p;
			public class Clean {
				public int run() {
					return 1;
				}
			}
			""";

	/**
	 * A class whose only dependencies are {@code java.lang} and
	 * {@code java.util.List}.
	 */
	private static final String USES_LIST = """
			package p;
			public class UsesList {
				public java.util.List<String> items() {
					return null;
				}
			}
			""";

	/**
	 * Compiles the sources with {@code javac} below the directory and returns the
	 * folder holding the class files.
	 *
	 * @param directory the directory to work in
	 * @param sources   the source files, by their path below the source folder
	 * @return the folder with the class files
	 * @throws IOException if a source file cannot be written
	 */
	private static Path compile(Path directory, Map<String, String> sources) throws IOException {
		Path sourceRoot = directory.resolve("src");
		Path classRoot = Files.createDirectories(directory.resolve("classes"));
		List<Path> files = new ArrayList<>();
		for (Map.Entry<String, String> source : sources.entrySet()) {
			Path file = sourceRoot.resolve(source.getKey());
			Files.createDirectories(file.getParent());
			Files.writeString(file, source.getValue());
			files.add(file);
		}
		return JavacFixtures.compileSources(JavacFixtures.testClasses().toString(), classRoot, files);
	}

	/**
	 * Compiles one source file with {@code javac} and imports the result.
	 *
	 * @param directory the directory to work in
	 * @param path      the path of the source file below the source folder
	 * @param source    the source text
	 * @return the imported classes
	 * @throws IOException if the source file cannot be written
	 */
	private static JavaClasses compileAndImport(Path directory, String path, String source) throws IOException {
		return new ClassFileImporter().importPath(compile(directory, Map.of(path, source)));
	}

	/**
	 * A call to {@code Files.write} with a byte array is refused, although the
	 * forbidden list spells the array as {@code byte[]}.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void refusesAnExactForbiddenSignatureWithArrays(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, "p/WritesBytes.java", WRITES_BYTES);
		ArchRule rule = JavaArchunitTestCaseCollection
				.allowAwareRuleFor(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(), Set.of());

		AssertionError refusal = assertThrows(AssertionError.class, () -> rule.check(classes));

		assertTrue(refusal.getMessage().contains("java.nio.file.Files.write"), refusal.getMessage());
	}

	/**
	 * A call to a method below a forbidden prefix is refused.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void refusesAMethodBelowAForbiddenPrefix(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, "p/FlushesPreferences.java", FLUSHES_PREFERENCES);
		ArchRule rule = JavaArchunitTestCaseCollection
				.allowAwareRuleFor(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(), Set.of());

		AssertionError refusal = assertThrows(AssertionError.class, () -> rule.check(classes));

		assertTrue(refusal.getMessage().contains("java.util.prefs.Preferences.flush"), refusal.getMessage());
	}

	/**
	 * A class that calls nothing forbidden passes the file system rule.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void passesACleanClass(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, "p/Clean.java", CLEAN);
		ArchRule rule = JavaArchunitTestCaseCollection
				.allowAwareRuleFor(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(), Set.of());

		assertDoesNotThrow(() -> rule.check(classes));
	}

	/**
	 * A permission written with a trailing dot covers its own package, and a
	 * misspelled one does not.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void acceptsAPermissionWrittenWithATrailingDot(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, "p/UsesList.java", USES_LIST);
		Set<PackagePermission> spelledRight = Set.of(new PackagePermission("java.util."),
				new PackagePermission("java.lang"));
		Set<PackagePermission> spelledWrong = Set.of(new PackagePermission("java.utilx."),
				new PackagePermission("java.lang"));

		assertDoesNotThrow(() -> JavaArchunitTestCaseCollection
				.allowAwareRuleFor(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, Set.of(),
						JavaArchunitTestCaseCollection.withOwnPackages(spelledRight, classes, Set.of()), Set.of())
				.check(classes));
		assertThrows(AssertionError.class, () -> JavaArchunitTestCaseCollection
				.allowAwareRuleFor(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, Set.of(),
						JavaArchunitTestCaseCollection.withOwnPackages(spelledWrong, classes, Set.of()), Set.of())
				.check(classes));
	}

	/**
	 * A class in the default package and a class of the JDK add no permission for
	 * their own package.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void leavesTheDefaultPackageAndTheJdkOutOfTheOwnPackages(@TempDir Path directory) throws IOException {
		Path classRoot = compile(directory, Map.of("Plain.java", "public class Plain {\n}\n"));
		Set<Location> locations = new HashSet<>(Locations.ofClass(String.class));
		locations.add(Location.of(classRoot));
		JavaClasses classes = new ClassFileImporter().importLocations(locations);

		assertTrue(classes.contain("Plain"));
		assertTrue(classes.contain(String.class));
		assertEquals(Set.of(), JavaArchunitTestCaseCollection.withOwnPackages(Set.of(), classes, Set.of()));
	}
}
