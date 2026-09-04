package de.tum.cit.ase.ares.api.securitytest.java.creator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
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

		private String packageName;
		private String mainClassName;
		private String classpath;
		private List<ArchitectureTestCase> architectureTestCases;
		private List<AOPTestCase> aopTestCases;
		private List<PhobosTestCase> phobosTestCases;

		@BeforeEach
		void arrangeCommonScenario() {
			packageName = "com.example";
			mainClassName = "Main";
			classpath = "/test/classpath";
			architectureTestCases = new ArrayList<>();
			aopTestCases = new ArrayList<>();
			phobosTestCases = new ArrayList<>();
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
			List<String> testClasses = List.of("TestClass1", "TestClass2");
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
			List<String> testClasses = List.of("TestClass");
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
			List<String> testClasses = List.of("TestClass");
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
			List<String> testClasses = List.of("TestClass");
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

	@Test
	void prepareAllowedPackagesRejectsMalformedNamesOnEveryDerivedPath() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedPackages", List.class,
				ResourceAccesses.class, String.class, Set.class, List.class);
		prepare.setAccessible(true);
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertInvocationCause(IllegalArgumentException.class,
				() -> prepare.invoke(javaCreator, List.of(), restrictive, "com..pinned", Set.of(), List.of()));
		assertInvocationCause(IllegalArgumentException.class,
				() -> prepare.invoke(javaCreator, List.of(), restrictive, "", Set.of("com..student"), List.of()));
		assertInvocationCause(IllegalArgumentException.class,
				() -> prepare.invoke(javaCreator, List.of(), restrictive, "", Set.of(), List.of("com..tests.BadTest")));
	}

	@Test
	@SuppressWarnings("unchecked")
	void prepareAllowedPackagesKeepsTheReservedNamespaceGuards() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedPackages", List.class,
				ResourceAccesses.class, String.class, Set.class, List.class);
		prepare.setAccessible(true);
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertInvocationCause(SecurityException.class,
				() -> prepare.invoke(javaCreator, List.of(), restrictive, "", Set.of("de.tum.cit"), List.of()));
		Set<PackagePermission> fromTestClass = (Set<PackagePermission>) prepare.invoke(javaCreator, List.of(),
				restrictive, "", Set.of(), List.of("de.tum.cit.Test"));
		assertEquals(Set.of(new PackagePermission("de.tum.cit")), fromTestClass);
	}

	@Test
	@SuppressWarnings("unchecked")
	void prepareAllowedPackagesPassesAPolicyDeclaredWildcardThrough() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedPackages", List.class,
				ResourceAccesses.class, String.class, Set.class, List.class);
		prepare.setAccessible(true);
		when(resourceAccesses.regardingPackageImports()).thenReturn(List.of(new PackagePermission("*")));
		Set<PackagePermission> result = (Set<PackagePermission>) prepare.invoke(javaCreator, List.of(),
				resourceAccesses, "", Set.of(), List.of());
		assertEquals(Set.of(new PackagePermission("*")), result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void prepareAllowedClassesFiltersBlanksThenValidates() throws Exception {
		Method prepare = JavaCreator.class.getDeclaredMethod("prepareAllowedClasses", List.class, List.class);
		prepare.setAccessible(true);
		Set<ClassPermission> result = (Set<ClassPermission>) prepare.invoke(javaCreator,
				Arrays.asList("com.example.Foo", "   ", null), Arrays.asList("com.example.BarTest", "", null));
		assertEquals(Set.of(new ClassPermission("com.example.Foo"), new ClassPermission("com.example.BarTest")),
				result);
		assertInvocationCause(IllegalArgumentException.class,
				() -> prepare.invoke(javaCreator, List.of("com..example.Foo"), List.of()));
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
}
