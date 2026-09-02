package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
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
				String.class, Set.class);
		method.setAccessible(true);
		Path fixtureDirectory = Path.of("target", "test-classes", "de", "tum", "cit", "ase", "ares", "api",
				"architecture", "java", "wala", "fixture");
		String expandedClassPath = (String) method.invoke(null, fixtureDirectory.toString(),
				requiredTrustedFrameworkCodeSources());
		URI commonsIoLocation = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		String commonsIoJar = Path.of(commonsIoLocation).toString();

		Assertions.assertTrue(
				Arrays.asList(expandedClassPath.split(Pattern.quote(File.pathSeparator))).contains(commonsIoJar));
	}

	@Test
	void testExpandClassPathIncludesThirdPartyJavaxJarByOrigin() throws Exception {
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("expandClassPathWithReachableDependencies",
				String.class, Set.class);
		method.setAccessible(true);
		String expandedClassPath = (String) method.invoke(null, FIXTURE_CLASSPATH,
				requiredTrustedFrameworkCodeSources());
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
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("filterClassPath", String.class, Set.class);
		method.setAccessible(true);

		String filtered = (String) method.invoke(null,
				String.join(File.pathSeparator, fakeJunit.toString(), fakeAres.toString(), actualAres.toString()),
				requiredTrustedFrameworkCodeSources());
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
				String.class, Set.class);
		method.setAccessible(true);

		boolean trusted = (boolean) method.invoke(null, classUri.toURL(), packageDirectory.toString(),
				requiredTrustedFrameworkCodeSources());

		Assertions.assertTrue(trusted);
	}

	@SuppressWarnings("unchecked")
	private static Set<Path> requiredTrustedFrameworkCodeSources() throws Exception {
		Field field = CustomCallgraphBuilder.class.getDeclaredField("REQUIRED_TRUSTED_FRAMEWORK_CODE_SOURCES");
		field.setAccessible(true);
		return (Set<Path>) field.get(null);
	}

	@SuppressWarnings("unchecked")
	private static Set<Path> optionalFrameworkCandidateCodeSources() throws Exception {
		Field field = CustomCallgraphBuilder.class.getDeclaredField("OPTIONAL_FRAMEWORK_CANDIDATE_CODE_SOURCES");
		field.setAccessible(true);
		return (Set<Path>) field.get(null);
	}

	/**
	 * Regression test for the defect that made the WALA backend unusable in every
	 * consumer without a direct jqwik dependency: jqwik is {@code provided}-scope,
	 * and referencing it through a class literal in a static initialiser turned its
	 * absence into a permanent {@link NoClassDefFoundError} for the whole class.
	 * <p>
	 * Loading the class in an isolated loader whose classpath omits jqwik is the
	 * only way to reproduce the consumer's situation from inside a build where
	 * jqwik is always present.
	 */
	@Test
	void testCallgraphBuilderInitialisesWithoutOptionalJqwikOnTheClasspath() throws Exception {
		Set<Path> jqwikOrigins = optionalFrameworkCandidateCodeSources();
		Assumptions.assumeFalse(jqwikOrigins.isEmpty(), "jqwik must be present to build a classpath without it");
		List<Path> testClassPath = currentTestClassPath();
		// Failing to derive the runner's classpath is a missing fixture, not a defect
		// in the code under test, so skip rather than report a false failure.
		Assumptions.assumeTrue(testClassPath.stream().anyMatch(jqwikOrigins::contains),
				"the derived test classpath must still contain the jqwik origins to remove them");
		List<URL> withoutJqwik = new ArrayList<>();
		for (Path entry : testClassPath) {
			if (!jqwikOrigins.contains(entry)) {
				withoutJqwik.add(entry.toUri().toURL());
			}
		}
		Assertions.assertNotEquals(withoutJqwik.size(), testClassPath.size(),
				"the isolated classpath must actually be missing the jqwik entries");

		try (URLClassLoader isolated = new URLClassLoader(withoutJqwik.toArray(URL[]::new),
				ClassLoader.getPlatformClassLoader())) {
			Class<?> isolatedBuilder = Class.forName(CustomCallgraphBuilder.class.getName(), true, isolated);

			Assertions.assertNotSame(CustomCallgraphBuilder.class, isolatedBuilder);
			Field required = isolatedBuilder.getDeclaredField("REQUIRED_TRUSTED_FRAMEWORK_CODE_SOURCES");
			required.setAccessible(true);
			Assertions.assertFalse(((Set<?>) required.get(null)).isEmpty(),
					"required framework origins must still resolve without jqwik");
			Field optional = isolatedBuilder.getDeclaredField("OPTIONAL_FRAMEWORK_CANDIDATE_CODE_SOURCES");
			optional.setAccessible(true);
			Assertions.assertTrue(((Set<?>) optional.get(null)).isEmpty(),
					"absent optional frameworks must contribute no trusted origin");
		}
	}

	/**
	 * The API and the engine may ship in separate JARs, so a partially present
	 * framework must contribute nothing rather than a lone origin. Admitting one
	 * half would let a forged counterpart pass unnoticed.
	 */
	@Test
	void testOptionalFrameworkOriginsAreDiscardedWhenAnyClassIsAbsent() throws Exception {
		// jqwik is provided-scope, so it is genuinely absent in the very consumers this
		// change is for. Treat that as a missing fixture, not as a defect.
		Assumptions.assumeFalse(optionalFrameworkCandidateCodeSources().isEmpty(),
				"jqwik must be present for the all-or-nothing distinction to be observable");
		Method method = CustomCallgraphBuilder.class.getDeclaredMethod("optionalFrameworkCodeSource", String.class,
				ClassLoader.class);
		method.setAccessible(true);
		ClassLoader loader = CustomCallgraphBuilder.class.getClassLoader();

		Optional<?> present = (Optional<?>) method.invoke(null, "net.jqwik.api.Property", loader);
		Optional<?> absent = (Optional<?>) method.invoke(null, "net.jqwik.api.NoSuchPropertyClass", loader);

		Assertions.assertTrue(present.isPresent());
		Assertions.assertTrue(absent.isEmpty(), "an unresolvable optional framework class must not throw");

		// The composition itself must be all-or-nothing: one absent name has to
		// discard the present one too, otherwise a partially present framework
		// contributes a lone origin that a forged counterpart could sit beside.
		Set<Path> bothPresent = CustomCallgraphBuilder.optionalFrameworkCandidateCodeSources(
				List.of("net.jqwik.api.Property", "net.jqwik.engine.JqwikTestEngine"));
		Set<Path> oneAbsent = CustomCallgraphBuilder.optionalFrameworkCandidateCodeSources(
				List.of("net.jqwik.api.Property", "net.jqwik.api.NoSuchPropertyClass"));

		Assertions.assertFalse(bothPresent.isEmpty(), "both jqwik artefacts are present in this build");
		Assertions.assertTrue(oneAbsent.isEmpty(),
				"a single absent name must discard every optional origin, not just its own");
	}

	/**
	 * A student who drops a forged {@code net.jqwik.api.Property} into the
	 * project's own class output must not thereby have that output directory
	 * removed from WALA's scope.
	 */
	@Test
	void testOptionalFrameworkOriginInsideSupervisedProjectIsRejected() throws Exception {
		Path projectRoot = Files.createDirectory(temporaryDirectory.resolve("project"));
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		Path outputRoot = Files.createDirectories(projectRoot.resolve("target/classes"));
		Path forgedOrigin = outputRoot.toRealPath();

		Set<Path> effective = CustomCallgraphBuilder.effectiveTrustedFrameworkCodeSources(outputRoot.toString(),
				Set.of(forgedOrigin));

		Assertions.assertFalse(effective.contains(forgedOrigin),
				"an optional origin inside the supervised project must never be trusted");
		Assertions.assertEquals(requiredTrustedFrameworkCodeSources(), effective);
	}

	/**
	 * An optional origin outside the supervised project is admitted, so a genuine
	 * jqwik dependency keeps being filtered out of WALA's scope exactly as before.
	 */
	@Test
	void testOptionalFrameworkOriginOutsideSupervisedProjectIsAdmitted() throws Exception {
		Path outsideOrigin = Files.createDirectory(temporaryDirectory.resolve("elsewhere")).toRealPath();
		Path projectRoot = Files.createDirectory(temporaryDirectory.resolve("consumer"));
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		Path outputRoot = Files.createDirectories(projectRoot.resolve("target/classes"));

		Set<Path> effective = CustomCallgraphBuilder.effectiveTrustedFrameworkCodeSources(outputRoot.toString(),
				Set.of(outsideOrigin));

		Assertions.assertTrue(effective.contains(outsideOrigin));
		Assertions.assertTrue(effective.containsAll(requiredTrustedFrameworkCodeSources()));
	}

	/**
	 * {@code dependencyFingerprint} has no analysis classpath and therefore no
	 * supervised-project boundary to validate optional origins against, so it must
	 * exclude only required framework origins. Excluding an unvalidated optional
	 * origin could drop student bytecode from the cache key.
	 */
	@Test
	void testRequiredTrustedOriginsExcludeOptionalFrameworkOrigins() throws Exception {
		Set<Path> required = requiredTrustedFrameworkCodeSources();
		Set<Path> optional = optionalFrameworkCandidateCodeSources();
		Assumptions.assumeFalse(optional.isEmpty(), "jqwik must be present for this distinction to be observable");

		Assertions.assertTrue(optional.stream().noneMatch(required::contains),
				"optional origins must not leak into the required set used for cache fingerprinting");
	}

	private static List<Path> currentTestClassPath() throws Exception {
		List<Path> entries = new ArrayList<>();
		for (String entry : System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator))) {
			if (entry.isBlank()) {
				continue;
			}
			Path path = Path.of(entry);
			if (Files.exists(path)) {
				entries.add(path.toRealPath());
			}
		}
		// Surefire may hand over a manifest-only booter JAR; expand its Class-Path so
		// the isolated loader sees the real dependencies.
		if (entries.size() == 1 && entries.get(0).toString().endsWith(".jar")) {
			try (JarFile booter = new JarFile(entries.get(0).toFile())) {
				// A single .jar entry does not prove this is Surefire's booter JAR, and a
				// JAR need not carry a manifest at all, so do not dereference blindly.
				Manifest manifest = booter.getManifest();
				String classPathAttribute = manifest == null ? null
						: manifest.getMainAttributes().getValue("Class-Path");
				if (classPathAttribute != null) {
					entries.clear();
					for (String url : classPathAttribute.split(" ")) {
						if (url.isBlank()) {
							continue;
						}
						// Manifest Class-Path entries are relative URLs by specification;
						// only Surefire's absolute file: URLs are usable here.
						URI entryUri = URI.create(url);
						if (!entryUri.isAbsolute() || !"file".equals(entryUri.getScheme())) {
							continue;
						}
						Path path = Path.of(entryUri);
						if (Files.exists(path)) {
							entries.add(path.toRealPath());
						}
					}
				}
			}
		}
		return entries;
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
