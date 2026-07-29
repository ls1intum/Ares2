package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

class JavaProjectScannerAstTest {
	@TempDir
	Path root;

	@Test
	void parsesDeclarationsTestsMainAndDeterministicPackageTies() throws IOException {
		Path production = Files.createDirectories(root.resolve("custom/main"));
		Path tests = Files.createDirectories(root.resolve("custom/test"));
		Files.writeString(production.resolve("Application.java"), """
				package beta;
				class Application { public static void main(java.lang.String... arguments) {} }
				// public class Fake { public static void main(String[] args) {} }
				""");
		Files.writeString(production.resolve("Alpha.java"), "package alpha; record Alpha(int value) {}\n");
		Files.writeString(tests.resolve("Cases.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				@Test @interface FastTest {}
				class Cases {
				  @FastTest void packagePrivateTest() {}
				  static class Nested { @org.junit.jupiter.params.ParameterizedTest void parameterised() {} }
				  String fake = "@Test class NotAType {}";
				}
				record RecordCases(int value) { @org.junit.jupiter.api.TestFactory Object factory() { return null; } }
				interface InterfaceCases { @org.junit.jupiter.api.RepeatedTest(2) default void repeated() {} }
				enum EnumCases { VALUE; @net.jqwik.api.Property boolean property() { return true; } }
				class JupiterTemplate { @org.junit.jupiter.api.TestTemplate void template() {} }
				class JunitFour { @org.junit.Test void oldTest() {} }
				class JqwikExample { @net.jqwik.api.Example void example() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertEquals("alpha", scanner.scanForPackageName());
		assertEquals("Application", scanner.scanForMainClassInPackage());
		assertArrayEquals(
				new String[] { "checks.Cases", "checks.Cases.Nested", "checks.EnumCases", "checks.InterfaceCases",
						"checks.JqwikExample", "checks.JunitFour", "checks.JupiterTemplate", "checks.RecordCases" },
				scanner.scanForTestClasses());
	}

	@Test
	void recognisesAresPublicAndHiddenTestAnnotations() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		Files.writeString(tests.resolve("AresCases.java"), """
				package checks;
				import de.tum.cit.ase.ares.api.jupiter.PublicTest;
				import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
				class PublicCases { @PublicTest void visible() {} }
				class HiddenCases { @HiddenTest void secret() {} }
				class FullyQualifiedCases { @de.tum.cit.ase.ares.api.jupiter.PublicTest void qualified() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "checks.FullyQualifiedCases", "checks.HiddenCases", "checks.PublicCases" },
				scanner.scanForTestClasses());
	}

	@Test
	void reportsMalformedFileWithItsPath() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Path malformed = production.resolve("Broken.java");
		Files.writeString(malformed, "class Broken {");
		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> new JavaProjectScanner(configuration(production, tests)).scanForPackageName());
		assertEquals("Cannot parse Java source file: " + malformed.toRealPath(), failure.getMessage());
	}

	@Test
	void reportsAConfiguredNonDirectorySourceRoot() throws IOException {
		Path production = root.resolve("not-a-directory");
		Files.writeString(production, "content");
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		BuildToolConfiguration invalid = new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production),
				List.of(tests), root.resolve("target/classes"), root.resolve("target/test-classes"));
		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> new JavaProjectScanner(invalid).scanForPackageName());
		assertEquals("Unreadable Java source root: " + production.toRealPath(), failure.getMessage());
	}

	@Test
	void ranksValidMainClassesByPreference() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Main.java"), """
				package app;
				class Main { public static void main(String[] arguments) {} }
				""");
		Files.writeString(production.resolve("Beta.java"), """
				package app;
				class Beta { public static void main(java.lang.String[] arguments) {} }
				""");
		Files.writeString(production.resolve("Application.java"), """
				package app;
				class Application { public static void main(java.lang.String... arguments) {} }
				""");
		Files.writeString(production.resolve("Zeta.java"), """
				package app;
				class Zeta { public static void main(String... arguments) {} }
				""");
		// Main outranks Application, which outranks the remaining classes
		// alphabetically.
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertEquals("Main", scanner.scanForMainClassInPackage());
	}

	@Test
	void rejectsNonMainSignaturesEvenWhenTheClassWouldRankFirst() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		// Every signature here is not a valid entry point (wrong name, non-public,
		// non-static, non-void, wrong arity, non-array and non-String array/varargs
		// parameters). The class is named "Main" so that it would win the ranking if
		// any
		// signature were wrongly accepted; asserting the valid "Fallback" wins
		// therefore
		// proves each invalid signature is rejected, not merely executed.
		Files.writeString(production.resolve("Main.java"), """
				package app;
				class Main {
				  public static void notMain(String[] arguments) {}
				  static void main(String[] arguments) {}
				  public void main(String[] arguments) {}
				  public static int main(String[] arguments) { return 0; }
				  public static void main() {}
				  public static void main(String single) {}
				  public static void main(int[] numbers) {}
				  public static void main(int... numbers) {}
				}
				""");
		Files.writeString(production.resolve("Fallback.java"), """
				package app;
				class Fallback { public static void main(String[] arguments) {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertEquals("Fallback", scanner.scanForMainClassInPackage());
	}

	@Test
	void recognisesJunitThreeTypeLevelAndDefaultPackageTestClasses() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		// A default-package production class must be skipped when picking the package
		// name.
		Files.writeString(production.resolve("Rooted.java"), "class Rooted {}\n");
		// A reserved-prefix production class must also be skipped when picking the
		// package name.
		Files.writeString(production.resolve("Reserved.java"), "package metatest; class Reserved {}\n");
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		Files.writeString(tests.resolve("LegacyCase.java"), """
				package checks;
				class LegacyCase extends TestCase { public void testLegacy() {} }
				""");
		// A plain non-test class in the test root must not be reported as a test class.
		Files.writeString(tests.resolve("PlainHelper.java"), """
				package checks;
				class PlainHelper {}
				""");
		Files.writeString(tests.resolve("TypeAnnotated.java"), """
				package checks;
				@org.junit.jupiter.api.Test class TypeAnnotated {}
				""");
		// A default-package test class exercises the empty-package qualified-name join.
		Files.writeString(tests.resolve("DefaultPackageCase.java"), """
				@org.junit.jupiter.api.Test class DefaultPackageCase {}
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertEquals("sol", scanner.scanForPackageName());
		assertArrayEquals(new String[] { "DefaultPackageCase", "checks.LegacyCase", "checks.TypeAnnotated" },
				scanner.scanForTestClasses());
	}

	@Test
	void ignoresLocalLookalikeAnnotationsButHonoursImportedAresAnnotations() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// Locally declared look-alikes with no Ares import must not mark classes as
		// tests.
		Files.writeString(tests.resolve("Spoof.java"), """
				package checks;
				@interface PublicTest {}
				@interface HiddenTest {}
				class SpoofPublic { @PublicTest void looksLikeATest() {} }
				class SpoofHidden { @HiddenTest void looksLikeATest() {} }
				class Unrelated { @java.lang.Deprecated void notATest() {} }
				""");
		// A look-alike meta-annotated with a recognised annotation must not slip into
		// the
		// shared simple-name set and thereby bypass the import check for @PublicTest.
		Files.writeString(tests.resolve("MetaSpoof.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				@Test @interface PublicTest {}
				class MetaSpoofed { @PublicTest void looksLikeATest() {} }
				""");
		// In a package that declares no look-alike, a wildcard import of the Ares
		// package
		// makes the bare simple name trustworthy; an unrelated wildcard is ignored.
		Files.writeString(tests.resolve("WildcardCase.java"), """
				package genuine;
				import java.util.*;
				import de.tum.cit.ase.ares.api.jupiter.*;
				class WildcardCase { @PublicTest void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "genuine.WildcardCase" }, scanner.scanForTestClasses());
	}

	@Test
	void rejectsWildcardImportedNamesShadowedByALocalAnnotation() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// A wildcard import does not establish identity: the annotation declared in the
		// same package shadows it, so Java binds the bare name to the local type.
		Files.writeString(tests.resolve("ShadowSpoof.java"), """
				package checks;
				import de.tum.cit.ase.ares.api.jupiter.*;
				@interface PublicTest {}
				class ShadowSpoofed { @PublicTest void looksLikeATest() {} }
				""");
		// The same trick against a JUnit name, with the look-alike in a sibling file of
		// the same package rather than the using file itself.
		Files.writeString(tests.resolve("ShadowedName.java"), """
				package siblings;
				@interface Test {}
				""");
		Files.writeString(tests.resolve("ShadowSibling.java"), """
				package siblings;
				import org.junit.jupiter.api.*;
				class ShadowSibling { @Test void looksLikeATest() {} }
				""");
		// A fully-qualified use names the type outright and stays trustworthy, even in
		// a
		// package that declares a look-alike.
		Files.writeString(tests.resolve("GenuineCase.java"), """
				package checks;
				class GenuineCase { @de.tum.cit.ase.ares.api.jupiter.PublicTest void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "checks.GenuineCase" }, scanner.scanForTestClasses());
	}

	private BuildToolConfiguration configuration(Path production, Path tests) {
		return new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production), List.of(tests),
				root.resolve("target/classes"), root.resolve("target/test-classes"));
	}
}
