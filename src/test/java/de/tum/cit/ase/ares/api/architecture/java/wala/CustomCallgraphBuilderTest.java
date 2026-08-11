package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;

public class CustomCallgraphBuilderTest {
	private static final String FIXTURE_CLASSPATH = "target/test-classes/de/tum/cit/ase/ares/api/architecture/java/wala/fixture";
	private static final String PARALLEL_STREAM_FIXTURE_CLASSPATH = "target/test-classes/de/tum/cit/ase/ares/integration/wala/fixture";

	@TempDir
	Path temporaryDirectory;

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
	void testExpandClassPathIncludesThirdPartyJavaxJarByOrigin() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("expandClassPathWithReachableDependencies",
				String.class);
		method.setAccessible(true);
		String expandedClassPath = (String) method.invoke(null, FIXTURE_CLASSPATH);
		String activationJar = Path.of(FileDataSource.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.toString();

		Assertions.assertTrue(
				Arrays.asList(expandedClassPath.split(Pattern.quote(File.pathSeparator))).contains(activationJar));
	}

	@Test
	void testExplodedSiblingDependencyUsesTheCompleteClassOutputRoot() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("classpathEntryFor", java.net.URL.class,
				String.class);
		method.setAccessible(true);
		String siblingName = "anonymous.sibling.SiblingFileHelper";
		java.net.URL siblingClass = CustomCallgraphBuilder.class
				.getResource("/" + siblingName.replace('.', '/') + ".class");
		@SuppressWarnings("unchecked")
		Optional<String> root = (Optional<String>) method.invoke(null, siblingClass, siblingName);

		Assertions.assertTrue(root.isPresent());
		Assertions.assertEquals(Path.of("target", "test-classes").toRealPath(),
				Path.of(root.orElseThrow()).toRealPath());
	}

	@Test
	void testSiblingPackageCannotHideLibraryMediatedForbiddenCall() {
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);

		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Accesses file system", Set.of("java.nio.file.Files.readString"))
						.check(builder.buildCallGraph(FIXTURE_CLASSPATH)));
	}

	@Test
	void testSeparateMavenModuleOutputCannotHideForbiddenCall() throws IOException {
		Path reactor = Files.createDirectory(temporaryDirectory.resolve("reactor"));
		Files.writeString(reactor.resolve("pom.xml"), "<project/>");
		Path helperModule = Files.createDirectories(reactor.resolve("helper"));
		Path appModule = Files.createDirectories(reactor.resolve("app"));
		Files.writeString(helperModule.resolve("pom.xml"), "<project/>");
		Files.writeString(appModule.resolve("pom.xml"), "<project/>");
		Path helperOutput = Files.createDirectories(helperModule.resolve(Path.of("target", "classes")));
		Path appOutput = Files.createDirectories(appModule.resolve(Path.of("target", "classes")));
		Path helperSource = Files.writeString(helperModule.resolve("Helper.java"), """
				package external.module;
				public final class Helper {
				  public static String read(java.nio.file.Path path) throws java.io.IOException {
				    return java.nio.file.Files.readString(path);
				  }
				}
				""");
		Path appSource = Files.writeString(appModule.resolve("Entry.java"), """
				package student.entry;
				public final class Entry {
				  public static String read(java.nio.file.Path path) throws java.io.IOException {
				    return external.module.Helper.read(path);
				  }
				}
				""");
		javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
		Assertions.assertNotNull(compiler, "The module-scope conformance test requires a JDK");
		Assertions.assertEquals(0,
				compiler.run(null, null, null, "-d", helperOutput.toString(), helperSource.toString()));
		Assertions.assertEquals(0, compiler.run(null, null, null, "-classpath", helperOutput.toString(), "-d",
				appOutput.toString(), appSource.toString()));
		String narrowAppPackage = appOutput.resolve(Path.of("student", "entry")).toString();

		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(narrowAppPackage);
		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Accesses file system", Set.of("java.nio.file.Files.readString"))
						.check(builder.buildCallGraph(narrowAppPackage)));
	}

	@Test
	void testFilterClassPathTrustsExactOriginsRatherThanFilenameFragments() throws Exception {
		Path fakeJunit = Files.writeString(temporaryDirectory.resolve("junit-backdoor.jar"), "student bytecode");
		Path fakeAres = Files.writeString(temporaryDirectory.resolve("ares-escape.jar"), "student bytecode");
		Path actualAres = Path
				.of(CustomCallgraphBuilder.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.toRealPath();
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("filterClassPath", String.class);
		method.setAccessible(true);

		String filtered = (String) method.invoke(null,
				String.join(File.pathSeparator, fakeJunit.toString(), fakeAres.toString(), actualAres.toString()));
		Set<String> entries = Set.of(filtered.split(Pattern.quote(File.pathSeparator)));

		Assertions.assertTrue(entries.contains(fakeJunit.toString()));
		Assertions.assertTrue(entries.contains(fakeAres.toString()));
		Assertions.assertFalse(entries.contains(actualAres.toString()));
	}

	@Test
	void testExplodedFrameworkClassIsTrustedOnlyWithinItsCanonicalCodeSource() throws Exception {
		URI classUri = CustomCallgraphBuilder.class
				.getResource("/de/tum/cit/ase/ares/api/architecture/java/wala/CustomCallgraphBuilder.class").toURI();
		Path packageDirectory = Path.of(classUri).getParent();
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("isTrustedFrameworkLocation", java.net.URL.class,
				String.class);
		method.setAccessible(true);

		boolean trusted = (boolean) method.invoke(null, classUri.toURL(), packageDirectory.toString());

		Assertions.assertTrue(trusted);
	}

	@Test
	void testAnalysisEntryFingerprintChangesWhenJarOrDirectoryContentChanges() throws Exception {
		Path dependencyJar = Files.writeString(temporaryDirectory.resolve("dependency.jar"), "first jar content");
		Path dependencyDirectory = Files.createDirectory(temporaryDirectory.resolve("classes"));
		Path dependencyClass = Files.writeString(dependencyDirectory.resolve("Dependency.class"),
				"first class content");
		Set<String> entries = Set.of(dependencyJar.toString(), dependencyDirectory.toString());
		String original = CustomCallgraphBuilder.fingerprintAnalysisEntries(entries);

		Files.writeString(dependencyJar, "second jar content");
		String changedJar = CustomCallgraphBuilder.fingerprintAnalysisEntries(entries);
		Files.writeString(dependencyClass, "second class content");
		String changedDirectory = CustomCallgraphBuilder.fingerprintAnalysisEntries(entries);

		Assertions.assertNotEquals(original, changedJar);
		Assertions.assertNotEquals(changedJar, changedDirectory);
	}

	@Test
	void testDependencyFingerprintIncludesReachableThirdPartyEntries() {
		JavaClasses fixtureClasses = new ClassFileImporter().importPath(Path.of(FIXTURE_CLASSPATH));
		String dependencyFingerprint = CustomCallgraphBuilder.dependencyFingerprint(fixtureClasses);
		String noDependenciesFingerprint = CustomCallgraphBuilder.fingerprintAnalysisEntries(Set.of());

		Assertions.assertNotEquals(noDependenciesFingerprint, dependencyFingerprint);
	}

	@Test
	void testThirdPartyJavaxJarCannotHideLibraryMediatedForbiddenCall() {
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(FIXTURE_CLASSPATH);

		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Accesses file system", Set.of("java.io.FileInputStream.<init>(Ljava/io/File;)"))
						.check(builder.buildCallGraph(FIXTURE_CLASSPATH)));
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

		// ArchUnit can resolve List -> Collection on some JDK/toolchain combinations,
		// producing the normal AssertionError violation. When that hierarchy is absent,
		// the matcher must instead fail closed with SecurityException. Both outcomes
		// deny the access; silently returning is never acceptable.
		Throwable interfaceTargetViolation = Assertions.assertThrows(Throwable.class,
				() -> new WalaRule("Manipulates threads", Set.of("java.util.Collection.parallelStream()"))
						.checkDirectAccesses(classes, Set.of()));
		Assertions.assertTrue(interfaceTargetViolation instanceof AssertionError
				|| interfaceTargetViolation instanceof SecurityException);
		Assertions.assertThrows(AssertionError.class,
				() -> new WalaRule("Manipulates threads", Set.of("java.util.stream.Stream.parallel()"))
						.checkDirectAccesses(classes, Set.of()));
		Assertions.assertDoesNotThrow(
				() -> new WalaRule("Manipulates threads", Set.of("java.util.Collection.parallelStream()"))
						.checkDirectAccesses(classes, Set.of(new ClassPermission(
								"de.tum.cit.ase.ares.integration.wala.fixture.ParallelStreamUser"))));
	}
}
