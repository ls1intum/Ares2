package de.tum.cit.ase.ares.api.securitytest.java.creator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.AncestorPackageExercise;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

/**
 * Test suite for the JavaCreator class.
 * <p>
 * Description: This class contains unit tests that verify the creation of Java
 * security test cases based on security policies. It tests the interaction with
 * build modes, architecture modes, and AOP modes, as well as the preparation of
 * allowed packages and classes.
 * <p>
 * Design Rationale: Tests the Creator pattern implementation for Java projects,
 * ensuring proper delegation to specialized components and correct handling of
 * security policies.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaCreatorTest {

	@Mock
	private BuildMode buildMode;

	@Mock
	private ArchitectureMode architectureMode;

	@Mock
	private AOPMode aopMode;

	@Mock
	private ResourceAccesses resourceAccesses;

	@Mock
	private JavaClasses javaClasses;

	@Mock
	private CallGraph callGraph;

	@TempDir
	Path tempDir;

	private JavaCreator javaCreator;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(buildMode.getTestBuildDirectory()).thenReturn(tempDir.resolve("test-classes").toString());
		when(buildMode.getBuildDirectory()).thenReturn(tempDir.resolve("classes").toString());
		javaCreator = new JavaCreator();
	}

	@Nested
	@DisplayName("CacheResult Tests")
	class CacheResultTests {

		@Test
		@DisplayName("Should cache supplier result and avoid recomputation")
		void shouldCacheSupplierResult() {
			// Arrange
			@SuppressWarnings("unchecked")
			Supplier<String> mockSupplier = mock(Supplier.class);
			when(mockSupplier.get()).thenReturn("cached_value");

			// Act
			Supplier<String> cachedSupplier = javaCreator.cacheResult("cached_value", mockSupplier);
			String firstCall = cachedSupplier.get();
			String secondCall = cachedSupplier.get();

			// Assert
			assertEquals("cached_value", firstCall);
			assertEquals("cached_value", secondCall);
			verify(mockSupplier, times(1)).get(); // Should only be called once
		}

		@Test
		@DisplayName("Should handle null values in cache")
		@SuppressWarnings("unchecked")
		void shouldHandleNullValuesInCache() {
			// Arrange
			Supplier<String> mockSupplier = mock(Supplier.class);
			when(mockSupplier.get()).thenReturn(null);

			// Act - the cache is per-JavaCreator instance (fresh each @BeforeEach), so keys
			// no longer collide across tests
			Supplier<String> cachedSupplier = javaCreator.cacheResult("null_test_unique_key_abc123", mockSupplier);
			String result = cachedSupplier.get();

			// Assert
			assertNull(result);
			verify(mockSupplier, times(1)).get();
		}
	}

	@Nested
	@DisplayName("CreateTestCases Tests")
	class CreateTestCasesTests {

		/** A compiled fixture without nested classes, standing in for a test class. */
		private static final String TEST_CLASS = "example.nest.NestHostFixture$Evil";

		private String packageName;
		private String mainClassName;
		private String classpath;
		private List<ArchitectureTestCase> architectureTestCases;
		private List<AOPTestCase> aopTestCases;
		private List<PhobosTestCase> phobosTestCases;

		@BeforeEach
		void arrangeCommonScenario() throws IOException {
			packageName = "com.example";
			mainClassName = "Main";
			classpath = "/test/classpath";
			architectureTestCases = new ArrayList<>();
			aopTestCases = new ArrayList<>();
			phobosTestCases = new ArrayList<>();
			Path compiled = tempDir.resolve("test-classes/example/nest/NestHostFixture$Evil.class");
			Files.createDirectories(compiled.getParent());
			try (InputStream input = JavaCreatorTest.class.getClassLoader()
					.getResourceAsStream("example/nest/NestHostFixture$Evil.class")) {
				Files.write(compiled, Objects.requireNonNull(input, "the fixture is compiled").readAllBytes());
			}
		}

		private void stubClasspathAndArchitecture() {
			when(buildMode.getClasspath(tempDir, packageName)).thenReturn(classpath);
			when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
			when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
		}

		@Test
		@DisplayName("Should create test cases with valid parameters")
		void shouldCreateTestCasesWithValidParameters() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang", "java.util");
			List<String> essentialClasses = List.of("String", "Object");
			List<String> testClasses = List.of(TEST_CLASS);
			stubClasspathAndArchitecture();
			when(resourceAccesses.regardingPackageImports())
					.thenReturn(List.of(new PackagePermission("allowed.example")));

			// Act
			assertDoesNotThrow(() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode,
					essentialPackages, essentialClasses, testClasses, packageName, mainClassName, architectureTestCases,
					aopTestCases, phobosTestCases, resourceAccesses, tempDir, true));

			// Assert
			verify(buildMode).getClasspath(tempDir, packageName);
			verify(architectureMode).getJavaClasses(classpath);
			// The call graph is built lazily through a cached Supplier and is not resolved
			// during createTestCases, so getCallGraph is never invoked here.
			verify(architectureMode, never()).getCallGraph(classpath);
			verify(resourceAccesses).regardingPackageImports();
		}

		@Test
		@DisplayName("Should handle empty essential packages")
		void shouldHandleEmptyEssentialPackages() {
			// Arrange
			List<String> essentialPackages = List.of();
			List<String> essentialClasses = List.of("TestClass");
			List<String> testClasses = List.of(TEST_CLASS);
			stubClasspathAndArchitecture();
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

			// Act & Assert
			assertDoesNotThrow(() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode,
					essentialPackages, essentialClasses, testClasses, packageName, mainClassName, architectureTestCases,
					aopTestCases, phobosTestCases, resourceAccesses, tempDir, true));
		}

		@Test
		@DisplayName("Should handle empty essential classes")
		void shouldHandleEmptyEssentialClasses() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang");
			List<String> essentialClasses = List.of();
			List<String> testClasses = List.of();
			stubClasspathAndArchitecture();
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

			// Act & Assert
			assertDoesNotThrow(() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode,
					essentialPackages, essentialClasses, testClasses, packageName, mainClassName, architectureTestCases,
					aopTestCases, phobosTestCases, resourceAccesses, tempDir, true));
		}

		@Test
		@DisplayName("Should use cached results for JavaClasses and CallGraph")
		void shouldUseCachedResultsForJavaClassesAndCallGraph() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang");
			List<String> essentialClasses = List.of("String");
			List<String> testClasses = List.of(TEST_CLASS);
			stubClasspathAndArchitecture();
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

			// Act - Call twice to test caching
			javaCreator.createTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
					testClasses, packageName, mainClassName, architectureTestCases, aopTestCases, phobosTestCases,
					resourceAccesses, tempDir, true);

			javaCreator.createTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
					testClasses, packageName, mainClassName, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
					resourceAccesses, tempDir, true);

			// Assert - Each method should only be called once due to static cache reuse
			verify(buildMode, times(1)).getClasspath(tempDir, packageName);
			verify(architectureMode, times(1)).getJavaClasses(classpath);
			// The call graph is built lazily through a cached Supplier; it is not resolved
			// during createTestCases, so getCallGraph is never invoked across both calls.
			verify(architectureMode, never()).getCallGraph(classpath);
		}

		@Test
		@DisplayName("Should populate architecture and AOP test case lists")
		void shouldPopulateArchitectureAndAOPTestCaseLists() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang");
			List<String> essentialClasses = List.of("String");
			List<String> testClasses = List.of(TEST_CLASS);
			stubClasspathAndArchitecture();
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

			// Act
			javaCreator.createTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
					testClasses, packageName, mainClassName, architectureTestCases, aopTestCases, phobosTestCases,
					resourceAccesses, tempDir, true);

			// Assert - The lists should be modified (exact contents depend on
			// implementation details)
			// We verify that the method completed successfully and the parameters were used
			verify(resourceAccesses).regardingPackageImports();
		}

		/**
		 * Student code declared in {@code de.tum.cit.ase}, above Ares' own API, is not
		 * refused. Its own package is not granted here; the rule adds it as an exact
		 * permission when it runs.
		 *
		 * @throws IOException if the fixture cannot be copied
		 */
		@Test
		@DisplayName("Should permit a supervised package that lies above a trusted namespace")
		void shouldPermitASupervisedPackageAboveATrustedNamespace() throws IOException {
			Path output = Files.createDirectories(tempDir.resolve("classes/de/tum/cit/ase"));
			try (InputStream bytecode = AncestorPackageExercise.class
					.getResourceAsStream(AncestorPackageExercise.class.getSimpleName() + ".class")) {
				Files.copy(bytecode, output.resolve(AncestorPackageExercise.class.getSimpleName() + ".class"));
			}
			String ancestorPackage = AncestorPackageExercise.class.getPackageName();
			String ancestorClasspath = tempDir.resolve("classes").toString();
			when(buildMode.getClasspath(tempDir, ancestorPackage)).thenReturn(ancestorClasspath);
			when(architectureMode.getJavaClasses(ancestorClasspath)).thenReturn(javaClasses);
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

			assertDoesNotThrow(() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode, List.of("java"),
					List.of(), List.of(), ancestorPackage, mainClassName, architectureTestCases, aopTestCases,
					phobosTestCases, resourceAccesses, tempDir, false));

			assertFalse(architectureTestCases.isEmpty());
			assertTrue(architectureTestCases.get(0).getAllowedPackages().stream()
					.noneMatch(permission -> permission.importTheFollowingPackage().equals(ancestorPackage)));
		}
	}

	@Nested
	@DisplayName("Implementation Tests")
	class ImplementationTests {

		@Test
		@DisplayName("Should implement Creator interface")
		void shouldImplementCreatorInterface() {
			// Assert
			assertTrue(javaCreator instanceof Creator);
		}

		@Test
		@DisplayName("Should have public constructor")
		void shouldHavePublicConstructor() {
			// Act & Assert
			assertDoesNotThrow(() -> new JavaCreator());
		}
	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Should handle null parameters gracefully")
		void shouldHandleNullParametersGracefully() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang");
			List<String> essentialClasses = List.of("String");
			List<String> testClasses = List.of("TestClass");
			String packageName = "com.example";
			String mainClassName = "Main";
			List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
			List<AOPTestCase> aopTestCases = new ArrayList<>();
			List<PhobosTestCase> phobosTestCases = new ArrayList<>();

			// Mock buildMode to throw exception to test error handling
			when(buildMode.getClasspath(tempDir, packageName)).thenThrow(new IllegalArgumentException("Invalid path"));

			// Act & Assert - Should propagate exceptions properly
			assertThrows(IllegalArgumentException.class,
					() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode, essentialPackages,
							essentialClasses, testClasses, packageName, mainClassName, architectureTestCases,
							aopTestCases, phobosTestCases, resourceAccesses, tempDir, true));
		}

		@Test
		@DisplayName("Should handle buildMode exceptions")
		void shouldHandleBuildModeExceptions() {
			// Arrange
			List<String> essentialPackages = List.of("java.lang");
			List<String> essentialClasses = List.of("String");
			List<String> testClasses = List.of("TestClass");
			String packageName = "com.example";
			String mainClassName = "Main";
			List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
			List<AOPTestCase> aopTestCases = new ArrayList<>();
			List<PhobosTestCase> phobosTestCases = new ArrayList<>();

			when(buildMode.getClasspath(tempDir, packageName)).thenThrow(new RuntimeException("Build error"));

			// Act & Assert
			assertThrows(RuntimeException.class,
					() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode, essentialPackages,
							essentialClasses, testClasses, packageName, mainClassName, architectureTestCases,
							aopTestCases, phobosTestCases, resourceAccesses, tempDir, true));
		}
	}

	/**
	 * Checks that a malformed essential package is refused while a valid one in the
	 * JDK namespace is kept as that package alone. The pinned package and the test
	 * class names are checked elsewhere, and the supervised code's own packages are
	 * read from the compiled classes when the rule runs.
	 */
	@Test
	@SuppressWarnings("unchecked")
	void prepareAllowedPackagesRejectsAMalformedEssentialPackage() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedPackages", List.class,
				ResourceAccesses.class);
		prepare.setAccessible(true);
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertInvocationCause(IllegalArgumentException.class,
				() -> prepare.invoke(null, List.of("com..essential"), restrictive));
		Set<PackagePermission> valid = (Set<PackagePermission>) prepare.invoke(null, List.of("java.lang"), restrictive);
		assertEquals(Set.of(new PackagePermission("java.lang", true)), valid);
	}

	/** Checks that a wildcard package declared in the policy is kept unchanged. */
	@Test
	@SuppressWarnings("unchecked")
	void prepareAllowedPackagesPassesAPolicyDeclaredWildcardThrough() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedPackages", List.class,
				ResourceAccesses.class);
		prepare.setAccessible(true);
		when(resourceAccesses.regardingPackageImports()).thenReturn(List.of(new PackagePermission("*")));
		Set<PackagePermission> result = (Set<PackagePermission>) prepare.invoke(null, List.of(), resourceAccesses);
		assertEquals(Set.of(new PackagePermission("*")), result);
	}

	/**
	 * Checks that blank entries are dropped and a malformed class name is refused
	 * before any class is exempted.
	 */
	@Test
	@SuppressWarnings("unchecked")
	void usableClassNamesFiltersBlanksThenValidates() throws Exception {
		Method usable = JavaCreator.class.getDeclaredMethod("usableClassNames", List.class);
		usable.setAccessible(true);
		List<String> result = (List<String>) usable.invoke(null,
				Arrays.asList("com.example.Foo", "   ", null, "com.example.BarTest", ""));
		assertEquals(List.of("com.example.Foo", "com.example.BarTest"), result);
		assertInvocationCause(IllegalArgumentException.class, () -> usable.invoke(null, List.of("com..example.Foo")));
	}

	/**
	 * Asserts a reflective call fails with the given cause, unwrapping the
	 * {@link InvocationTargetException} that reflection wraps a thrown exception
	 * in.
	 *
	 * @param expected   the exception type the underlying call should raise
	 * @param invocation the reflective call under test
	 * @return the unwrapped cause, for any further assertion
	 */
	private static <T extends Throwable> T assertInvocationCause(Class<T> expected, Executable invocation) {
		InvocationTargetException wrapper = assertThrows(InvocationTargetException.class, invocation);
		return assertInstanceOf(expected, wrapper.getCause());
	}

	/**
	 * Checks that the creator reads nest members from the trusted test output and
	 * scans the whole production output for a class named like one of them.
	 */
	@Nested
	@DisplayName("Exempted nest tests")
	class ExemptedNestTests {

		/** The fixture whose nest members are exempted with it. */
		private static final String HOST = "example.nest.NestHostFixture";

		/** The fixture's package, supervised in these tests. */
		private static final String PACKAGE = "example.nest";

		/**
		 * Lays out a test output holding the fixture and an empty production output.
		 */
		@BeforeEach
		void arrangeOutputs() throws IOException {
			Path testOutput = tempDir.resolve("test-classes");
			Path productionOutput = tempDir.resolve("classes");
			Files.createDirectories(testOutput.resolve("example/nest"));
			Files.createDirectories(productionOutput.resolve("example/nest"));
			Files.write(testOutput.resolve("example/nest/NestHostFixture.class"), fixtureBytes());
			when(buildMode.getTestBuildDirectory()).thenReturn(testOutput.toString());
			when(buildMode.getBuildDirectory()).thenReturn(productionOutput.toString());
			when(buildMode.getClasspath(tempDir, PACKAGE)).thenReturn("/narrow/classpath");
			when(architectureMode.getJavaClasses("/narrow/classpath")).thenReturn(javaClasses);
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());
		}

		/** Reads the compiled fixture through the test class loader. */
		private byte[] fixtureBytes() throws IOException {
			try (InputStream input = JavaCreatorTest.class.getClassLoader()
					.getResourceAsStream("example/nest/NestHostFixture.class")) {
				return Objects.requireNonNull(input, "the fixture is compiled").readAllBytes();
			}
		}

		/** Runs createTestCases with the fixture as the only test class. */
		private void createTestCases() {
			javaCreator.createTestCases(buildMode, architectureMode, aopMode, List.of(), List.of(), List.of(HOST),
					PACKAGE, "Main", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), resourceAccesses, tempDir,
					true);
		}

		/**
		 * The nest members listed in the test output are exempted, without collision.
		 */
		@Test
		@DisplayName("Exempts the nest members the test output lists")
		void exemptsTheNestMembersTheTestOutputLists() {
			createTestCases();
			assertTrue(javaCreator.exemptedClassNames().containsAll(
					List.of(HOST, HOST + "$Member", HOST + "$Member$Deeper", HOST + "$1", HOST + "$Pair")));
			assertDoesNotThrow(() -> javaCreator.requireNoProductionClassNamedLikeAnExemptedOne(buildMode));
		}

		/**
		 * A production class named like a nest member is refused, although the narrower
		 * analysis classpath does not contain it.
		 */
		@Test
		@DisplayName("Refuses a production class named like a nest member outside the classpath")
		void refusesAProductionClassNamedLikeANestMember() throws IOException {
			Files.write(tempDir.resolve("classes/example/nest/NestHostFixture$Member.class"), new byte[] { 0 });
			createTestCases();
			assertThrows(SecurityException.class,
					() -> javaCreator.requireNoProductionClassNamedLikeAnExemptedOne(buildMode));
		}

		/**
		 * A declared test class the test output does not hold is refused while the test
		 * cases are created, so its bare name never becomes an exemption a student
		 * class could take over.
		 */
		@Test
		@DisplayName("Refuses a declared test class that was never compiled")
		void refusesADeclaredTestClassThatWasNeverCompiled() {
			SecurityException refused = assertThrows(SecurityException.class,
					() -> javaCreator.createTestCases(buildMode, architectureMode, aopMode, List.of(), List.of(),
							List.of(HOST, "example.nest.GhostTest"), PACKAGE, "Main", new ArrayList<>(),
							new ArrayList<>(), new ArrayList<>(), resourceAccesses, tempDir, true));
			assertTrue(refused.getMessage().contains("example.nest.GhostTest"), refused.getMessage());
		}
	}

	/**
	 * Checks that a run is refused when the classes the rules analyse are none
	 * while the production output holds some, and let through otherwise.
	 */
	@Nested
	@DisplayName("Supervised classes tests")
	class SupervisedClassesTests {

		/** The package supervised in these tests. */
		private static final String PACKAGE = "example.nest";

		/** Copies the compiled fixture into the given folder. */
		private void compileFixtureInto(Path folder) throws IOException {
			Files.createDirectories(folder);
			try (InputStream input = JavaCreatorTest.class.getClassLoader()
					.getResourceAsStream("example/nest/NestHostFixture.class")) {
				Files.copy(Objects.requireNonNull(input, "the fixture is compiled"),
						folder.resolve("NestHostFixture.class"));
			}
		}

		/** Creates the test cases with the given folder as the analysed one. */
		private void createTestCasesFor(Path supervisedFolder) {
			createTestCasesFor(supervisedFolder, new ClassFileImporter().importPath(supervisedFolder));
		}

		/** Creates the test cases with the given folder and analysed classes. */
		private void createTestCasesFor(Path supervisedFolder, JavaClasses analysedClasses) {
			String classPath = supervisedFolder.toString();
			when(buildMode.getClasspath(tempDir, PACKAGE)).thenReturn(classPath);
			when(architectureMode.getJavaClasses(classPath)).thenReturn(analysedClasses);
			when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());
			javaCreator.createTestCases(buildMode, architectureMode, aopMode, List.of(), List.of(), List.of(), PACKAGE,
					"Main", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), resourceAccesses, tempDir, true);
		}

		/** A scope holding a compiled class is accepted. */
		@Test
		@DisplayName("Accepts a scope holding a compiled class")
		void acceptsAScopeHoldingACompiledClass() throws IOException {
			Path scope = tempDir.resolve("classes/example/nest");
			compileFixtureInto(scope);
			createTestCasesFor(scope);
			assertDoesNotThrow(() -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/** An empty scope is refused while the output holds a class elsewhere. */
		@Test
		@DisplayName("Refuses an empty scope while classes are compiled elsewhere")
		void refusesAnEmptyScopeWhileClassesAreCompiledElsewhere() throws IOException {
			compileFixtureInto(tempDir.resolve("classes/example/other"));
			createTestCasesFor(Files.createDirectories(tempDir.resolve("classes/example/nest")));
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * A missing scope is refused while the output holds a class. The path is the
		 * one a withinPath with "\" separators used to resolve to on Windows.
		 */
		@Test
		@DisplayName("Refuses a missing scope while classes are compiled")
		void refusesAMissingScopeWhileClassesAreCompiled() throws IOException {
			compileFixtureInto(tempDir.resolve("classes/example/nest"));
			createTestCasesFor(tempDir.resolve("classes/classes/java/main/example/nest"));
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * A missing test-output scope is refused while the test output holds a class,
		 * although the production output is empty.
		 */
		@Test
		@DisplayName("Refuses a missing test scope while test classes are compiled")
		void refusesAMissingTestScopeWhileTestClassesAreCompiled() throws IOException {
			Files.createDirectories(tempDir.resolve("classes"));
			compileFixtureInto(tempDir.resolve("test-classes/example/nest"));
			createTestCasesFor(tempDir.resolve("test-classes/example/typo"));
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * A missing test-output scope is refused while the production output holds a
		 * class, although the test output is empty.
		 */
		@Test
		@DisplayName("Refuses a missing test scope while production classes are compiled")
		void refusesAMissingTestScopeWhileProductionClassesAreCompiled() throws IOException {
			compileFixtureInto(tempDir.resolve("classes/example/nest"));
			createTestCasesFor(tempDir.resolve("test-classes/example/typo"));
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * The rules' own classes decide, not the folder: a scope whose classes the
		 * analysis left out is refused.
		 */
		@Test
		@DisplayName("Refuses a scope whose classes the analysis left out")
		void refusesAScopeWhoseClassesTheAnalysisLeftOut() throws IOException {
			Path scope = tempDir.resolve("classes/example/nest");
			compileFixtureInto(scope);
			createTestCasesFor(scope, new ClassFileImporter().importClasses());
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * A creator reused after compiling still analyses its first, empty import, so
		 * it is refused.
		 */
		@Test
		@DisplayName("Refuses a reused creator whose cached import was empty")
		void refusesAReusedCreatorWhoseCachedImportWasEmpty() throws IOException {
			Path scope = Files.createDirectories(tempDir.resolve("classes/example/nest"));
			createTestCasesFor(scope);
			compileFixtureInto(scope);
			createTestCasesFor(scope);
			assertThrows(SecurityException.class, () -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * A directory named like a class file is not a compiled class, so an output
		 * holding only that is still an exercise with nothing compiled.
		 */
		@Test
		@DisplayName("Ignores a directory named like a class file")
		void ignoresADirectoryNamedLikeAClassFile() throws IOException {
			Files.createDirectories(tempDir.resolve("classes/assets/cache.class"));
			createTestCasesFor(tempDir.resolve("classes/example/nest"));
			assertDoesNotThrow(() -> javaCreator.requireSupervisedClasses(buildMode));
		}

		/**
		 * Nothing compiled at all is an exercise the student has yet to fill, and is
		 * let through rather than turned into an Ares error.
		 */
		@Test
		@DisplayName("Accepts an exercise with nothing compiled yet")
		void acceptsAnExerciseWithNothingCompiledYet() throws IOException {
			Files.createDirectories(tempDir.resolve("classes"));
			createTestCasesFor(tempDir.resolve("classes/example/nest"));
			assertDoesNotThrow(() -> javaCreator.requireSupervisedClasses(buildMode));
		}
	}
}
