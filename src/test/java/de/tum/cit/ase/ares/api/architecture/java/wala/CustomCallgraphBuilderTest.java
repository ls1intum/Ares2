package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;

public class CustomCallgraphBuilderTest {
	private static final String FIXTURE_CLASSPATH = "target/test-classes/de/tum/cit/ase/ares/api/architecture/java/wala/fixture";
	private static final String PARALLEL_STREAM_FIXTURE_CLASSPATH = "target/test-classes/de/tum/cit/ase/ares/integration/wala/fixture";

	@Test
	void testConvertTypeNameToClassName_Valid() throws Exception {
		String input = "com.example.MyClass";
		String expected = "/com/example/MyClass.class";

		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
		method.setAccessible(true);
		String actual = (String) method.invoke(null, input);

		Assertions.assertEquals(expected, actual);
	}

	@Test
	void testConvertTypeNameToClassName_Invalid_Null() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
		method.setAccessible(true);

		Assertions.assertThrows(SecurityException.class, () -> {
			try {
				method.invoke(null, (String) null);
			} catch (Exception e) {
				if (e.getCause() instanceof SecurityException) {
					throw (SecurityException) e.getCause();
				}
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void testConvertTypeNameToClassName_Invalid_Empty() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
		method.setAccessible(true);

		Assertions.assertThrows(SecurityException.class, () -> {
			try {
				method.invoke(null, "");
			} catch (Exception e) {
				if (e.getCause() instanceof SecurityException) {
					throw (SecurityException) e.getCause();
				}
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void testConvertTypeNameToWalaName_Valid() throws Exception {
		String input = "com.example.MyClass";
		String expected = "Lcom/example/MyClass";

		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
		method.setAccessible(true);
		String actual = (String) method.invoke(null, input);

		Assertions.assertEquals(expected, actual);
	}

	@Test
	void testConvertTypeNameToWalaName_Invalid_Null() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
		method.setAccessible(true);

		Assertions.assertThrows(SecurityException.class, () -> {
			try {
				method.invoke(null, (String) null);
			} catch (Exception e) {
				if (e.getCause() instanceof SecurityException) {
					throw (SecurityException) e.getCause();
				}
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void testConvertTypeNameToWalaName_Invalid_Empty() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
		method.setAccessible(true);

		Assertions.assertThrows(SecurityException.class, () -> {
			try {
				method.invoke(null, "");
			} catch (Exception e) {
				if (e.getCause() instanceof SecurityException) {
					throw (SecurityException) e.getCause();
				}
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void testTryResolve_NonExistent() throws Exception {
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);

		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("tryResolve", String.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		Optional<JavaClass> result = (Optional<JavaClass>) method.invoke(builder, "non.existent.ClassName");

		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void testGetImmediateSubclasses_NonExistent() {
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);
		Set<JavaClass> subclasses = builder.getImmediateSubclasses("non.existent.ClassName");
		Assertions.assertTrue(subclasses.isEmpty());
	}

	@Test
	void testBuildersOwnIndependentClassHierarchies() throws Exception {
		CustomCallgraphBuilder firstBuilder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);
		CustomCallgraphBuilder secondBuilder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);
		Field hierarchyField = CustomCallgraphBuilder.class.getDeclaredField("classHierarchy");
		hierarchyField.setAccessible(true);
		ClassHierarchy firstHierarchy = (ClassHierarchy) hierarchyField.get(firstBuilder);
		ClassHierarchy secondHierarchy = (ClassHierarchy) hierarchyField.get(secondBuilder);

		Assertions.assertNotSame(firstHierarchy, secondHierarchy,
				"Each policy execution must own a fresh mutable WALA hierarchy");
	}

	@Test
	void testDerivePackagePrefixSupportsProductionAndTestOutputs() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("derivePackagePrefix", String.class);
		method.setAccessible(true);

		Assertions.assertEquals("Lexample/student/", method.invoke(null, "target/classes/example/student"));
		Assertions.assertEquals("Lexample/student/", method.invoke(null, "target/test-classes/example/student"));
		Assertions.assertEquals("Lexample/student/", method.invoke(null, "build/classes/java/main/example/student"));
		Assertions.assertEquals("Lexample/student/", method.invoke(null, "build/classes/java/test/example/student"));
	}

	@Test
	void testBuildCallGraph_InvalidPath() {
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);
		Assertions.assertThrows(SecurityException.class, () -> {
			builder.buildCallGraph("invalid/path/to/classes");
		});
	}

	@Test
	void testExpandClassPathWithReachableJarDependency() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("expandClassPathWithReachableDependencies",
				String.class);
		method.setAccessible(true);
		Path fixtureDirectory = Path.of("target", "test-classes", "de", "tum", "cit", "ase", "ares", "api",
				"architecture", "java", "wala", "fixture");
		String expandedClassPath = (String) method.invoke(null, fixtureDirectory.toString());
		URI commonsIoLocation = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		String commonsIoJar = Path.of(commonsIoLocation).toString();

		Assertions.assertTrue(
				Arrays.asList(expandedClassPath.split(Pattern.quote(File.pathSeparator))).contains(commonsIoJar));
	}

	@Test
	void testImportJarLoadsAllSiblingClasses() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("importJar", ClassFileImporter.class,
				String.class);
		method.setAccessible(true);
		URI commonsIoLocation = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		JavaClasses classes = (JavaClasses) method.invoke(null, new ClassFileImporter(),
				Path.of(commonsIoLocation).toString());

		Assertions.assertDoesNotThrow(() -> classes.get("org.apache.commons.io.FileUtils"));
		Assertions.assertDoesNotThrow(() -> classes.get("org.apache.commons.io.IOUtils"));
	}

	@Test
	void testNormaliseDependencyClassNameHandlesReferenceAndPrimitiveArrays() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("normaliseDependencyClassName", String.class);
		method.setAccessible(true);

		Assertions.assertEquals("java.io.File", method.invoke(null, "[[Ljava.io.File;"));
		Assertions.assertNull(method.invoke(null, "[I"));
	}

	@Test
	void testDirectAccessCheckCatchesJdkInterfaceTargets() {
		JavaClasses classes = new ClassFileImporter().importPath(Path.of(PARALLEL_STREAM_FIXTURE_CLASSPATH));

		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Manipulates threads", Set.of("java.util.Collection.parallelStream()"))
						.checkDirectAccesses(classes, Set.of()));
		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Manipulates threads", Set.of("java.util.stream.Stream.parallel()"))
						.checkDirectAccesses(classes, Set.of()));
		Assertions.assertDoesNotThrow(
				() -> new WalaRule("Manipulates threads", Set.of("java.util.Collection.parallelStream()"))
						.checkDirectAccesses(classes, Set.of(new ClassPermission(
								"de.tum.cit.ase.ares.integration.wala.fixture.ParallelStreamUser"))));
	}
}
