package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

class JavaWalaTestCaseCacheTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void testImplementationFingerprintChangesWhenExistingClassIsOverwritten() throws IOException {
		Path apiRoot = temporaryDirectory.resolve(Path.of("de", "tum", "cit", "ase", "ares", "api"));
		Files.createDirectories(apiRoot);
		Path classFile = apiRoot.resolve("Rule.class");
		Files.writeString(classFile, "first implementation");
		String firstFingerprint = JavaWalaTestCase.implementationFingerprint(temporaryDirectory);

		Files.writeString(classFile, "second implementation");
		String secondFingerprint = JavaWalaTestCase.implementationFingerprint(temporaryDirectory);

		Assertions.assertNotEquals(firstFingerprint, secondFingerprint);
	}

	@Test
	void testImplementationFingerprintChangesWhenEitherArchitectureEngineChanges() throws IOException {
		Path aresCodeSource = Files.createDirectory(temporaryDirectory.resolve("ares"));
		Path walaCodeSource = temporaryDirectory.resolve("wala.jar");
		Path archUnitCodeSource = temporaryDirectory.resolve("archunit.jar");
		Files.writeString(walaCodeSource, "first WALA implementation");
		Files.writeString(archUnitCodeSource, "first ArchUnit implementation");
		String original = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);

		Files.writeString(walaCodeSource, "second WALA implementation");
		String changedWala = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);
		Files.writeString(archUnitCodeSource, "second ArchUnit implementation");
		String changedArchUnit = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);

		Assertions.assertNotEquals(original, changedWala);
		Assertions.assertNotEquals(changedWala, changedArchUnit);
	}

	@Test
	void testSignedStoreReplaysPassAndViolationAndRejectsTampering() throws Exception {
		Path cache = temporaryDirectory.resolve("outcomes.v3");
		Path lock = temporaryDirectory.resolve("outcomes.v3.lock");
		byte[] secret = "test-secret-with-sufficient-entropy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		Map<String, Optional<String>> outcomes = Map.of("pass", Optional.empty(), "violation", Optional.of("blocked"));

		WalaOutcomeCacheStore.mergeAndSave(cache, lock, secret, outcomes);
		assertOutcomes(outcomes, WalaOutcomeCacheStore.load(cache, secret));
		Files.writeString(cache, Files.readString(cache) + "tampered");
		Assertions.assertTrue(WalaOutcomeCacheStore.load(cache, secret).isEmpty());
	}

	@Test
	void testConcurrentWritersMergeWithoutLosingVerdicts() throws Exception {
		Path cache = temporaryDirectory.resolve("concurrent.v3");
		Path lock = temporaryDirectory.resolve("concurrent.v3.lock");
		byte[] secret = "shared-concurrent-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		var executor = Executors.newFixedThreadPool(6);
		try {
			for (int index = 0; index < 24; index++) {
				int entry = index;
				executor.submit(() -> {
					try {
						WalaOutcomeCacheStore.mergeAndSave(cache, lock, secret, Map.of("key-" + entry,
								entry % 2 == 0 ? Optional.empty() : Optional.of("blocked-" + entry)));
					} catch (Exception failure) {
						throw new IllegalStateException(failure);
					}
				});
			}
		} finally {
			executor.shutdown();
			Assertions.assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
		}
		Map<String, Optional<String>> persisted = WalaOutcomeCacheStore.load(cache, secret);
		Assertions.assertEquals(24, persisted.size());
		Assertions.assertEquals(Optional.empty(), persisted.get("key-0"));
		Assertions.assertEquals(Optional.of("blocked-23"), persisted.get("key-23"));
	}

	@Test
	void testClasspathFingerprintChangesForJarAndExplodedClassContent() throws IOException {
		Path jar = Files.writeString(temporaryDirectory.resolve("dependency.jar"), "version-one");
		Path classes = Files.createDirectory(temporaryDirectory.resolve("classes"));
		Path classFile = Files.writeString(classes.resolve("Dependency.class"), "bytecode-one");
		String classpath = jar + java.io.File.pathSeparator + classes;
		String original = JavaWalaTestCase.classpathFingerprint(classpath);

		Files.writeString(jar, "version-two");
		String changedJar = JavaWalaTestCase.classpathFingerprint(classpath);
		Files.writeString(classFile, "bytecode-two");
		String changedClass = JavaWalaTestCase.classpathFingerprint(classpath);

		Assertions.assertNotEquals(original, changedJar);
		Assertions.assertNotEquals(changedJar, changedClass);
	}

	@Test
	void testEnvironmentFingerprintIncludesJdkIdentityLocaleAndSchema() {
		String originalVendor = System.getProperty("java.vendor");
		Locale originalLocale = Locale.getDefault();
		try {
			String original = JavaWalaTestCase.environmentFingerprint();
			System.setProperty("java.vendor", originalVendor + "-changed");
			String changedJdk = JavaWalaTestCase.environmentFingerprint();
			Locale.setDefault(Locale.JAPANESE);
			String changedLocale = JavaWalaTestCase.environmentFingerprint();

			Assertions.assertEquals("3", JavaWalaTestCase.CACHE_SCHEMA_VERSION);
			Assertions.assertNotEquals(original, changedJdk);
			Assertions.assertNotEquals(changedJdk, changedLocale);
		} finally {
			System.setProperty("java.vendor", originalVendor);
			Locale.setDefault(originalLocale);
		}
	}

	private static void assertOutcomes(Map<String, Optional<String>> expected, Map<String, Optional<String>> actual) {
		Assertions.assertEquals(new HashMap<>(expected), new HashMap<>(actual));
	}

	/**
	 * Returns the cache key of a WALA test case over the given classes and
	 * permissions.
	 *
	 * @param packages the permitted package names
	 * @param classes  the exempted class names
	 * @return the cache key
	 */
	private static String cacheKeyFor(Set<String> packages, Set<String> classes) throws Exception {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		packages.forEach(name -> allowedPackages.add(new PackagePermission(name)));
		Set<ClassPermission> allowedClasses = new HashSet<>();
		classes.forEach(name -> allowedClasses.add(new ClassPermission(name)));
		JavaWalaTestCase testCase = JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(allowedPackages).allowedClasses(allowedClasses).javaClasses(SHARED_CLASSES)
				.callGraph(Mockito.mock(CallGraph.class)).build();
		Method cacheKey = JavaWalaTestCase.class.getDeclaredMethod("cacheKey");
		cacheKey.setAccessible(true);
		return (String) cacheKey.invoke(testCase);
	}

	/** The analysed classes shared by the cache-key tests. */
	private static final JavaClasses SHARED_CLASSES = new ClassFileImporter()
			.importClasses(JavaWalaTestCaseCacheTest.class);

	/**
	 * A permitted package named "a,b" does not share a key with packages a and b.
	 */
	@Test
	void testCacheKeyTellsACommaJoinedPackageFromTheSplitOnes() throws Exception {
		Assertions.assertNotEquals(cacheKeyFor(Set.of("a", "b"), Set.of()), cacheKeyFor(Set.of("a,b"), Set.of()));
	}

	/** An exempted class named "a,b" does not share a key with classes a and b. */
	@Test
	void testCacheKeyTellsACommaJoinedExemptionFromTheSplitOnes() throws Exception {
		Assertions.assertNotEquals(cacheKeyFor(Set.of(), Set.of("x.A", "x.B")),
				cacheKeyFor(Set.of(), Set.of("x.A,x.B")));
	}

	/** Equal permissions still share a key, so the cache keeps working. */
	@Test
	void testCacheKeyIsStableForEqualPermissions() throws Exception {
		Assertions.assertEquals(cacheKeyFor(Set.of("a", "b"), Set.of("x.A")),
				cacheKeyFor(Set.of("b", "a"), Set.of("x.A")));
	}
}
