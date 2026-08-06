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

	@Test
	void ignoresLookalikesOfAComposedAnnotationDeclaredInAnotherPackage() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// A genuinely composed annotation: it carries an imported @Test, so it marks a
		// test wherever it is genuinely referred to. It is public and lives in its own
		// file, so that the cross-package references below are ones javac would accept.
		Files.writeString(tests.resolve("FastTest.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				@Test public @interface FastTest {}
				""");
		Files.writeString(tests.resolve("Composed.java"), """
				package checks;
				class ComposedCase { @FastTest void real() {} }
				""");
		// An unrelated annotation of the same simple name in another package must not
		// inherit that trust: it is a different type, and nothing here refers to
		// checks.FastTest.
		Files.writeString(tests.resolve("Lookalike.java"), """
				package attack;
				@interface FastTest {}
				class Spoofed { @FastTest void looksLikeATest() {} }
				""");
		// Referring to the composed annotation genuinely stays recognised, whether it
		// is
		// imported directly, by package wildcard or written out in full.
		Files.writeString(tests.resolve("Importer.java"), """
				package client;
				import checks.FastTest;
				class ImportedCase { @FastTest void real() {} }
				""");
		Files.writeString(tests.resolve("Wildcarder.java"), """
				package wildcard;
				import checks.*;
				class WildcardedCase { @FastTest void real() {} }
				""");
		Files.writeString(tests.resolve("Qualifier.java"), """
				package qualified;
				class QualifiedCase { @checks.FastTest void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "checks.ComposedCase", "client.ImportedCase", "qualified.QualifiedCase",
				"wildcard.WildcardedCase" }, scanner.scanForTestClasses());
	}

	@Test
	void readsAQualifiedNameThroughAnyTypeThatObscuresItsLeftmostSegment() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// Declaring a type named "org" obscures the package of the same name, so Java
		// binds the annotation below to the nest declared here rather than to JUnit.
		// Trusting it on its spelling alone would exempt the class without a single
		// genuine test annotation anywhere in the file.
		Files.writeString(tests.resolve("Obscured.java"), """
				package attack;
				class org { static class junit { static class jupiter { static class api { @interface Test {} } } } }
				class ObscuredSpoof { @org.junit.jupiter.api.Test void looksLikeATest() {} }
				""");
		// The same trick against a composed annotation: an unrelated nest whose
		// spelling
		// happens to match a trusted fully-qualified name.
		Files.writeString(tests.resolve("Genuine.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				public class Outer { @Test public @interface FastTest {} }
				""");
		Files.writeString(tests.resolve("ObscuredComposed.java"), """
				package spoof;
				class checks { static class Outer { @interface FastTest {} } }
				class ComposedSpoof { @checks.Outer.FastTest void looksLikeATest() {} }
				""");
		// Nothing obscures the leftmost segment here, so both names denote what they
		// spell: a nested annotation reached through its enclosing type, and a plain
		// fully-qualified JUnit annotation.
		Files.writeString(tests.resolve("Client.java"), """
				package client;
				import checks.Outer;
				class NestedCase { @Outer.FastTest void real() {} }
				class QualifiedCase { @org.junit.jupiter.api.Test void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "client.NestedCase", "client.QualifiedCase" }, scanner.scanForTestClasses());
	}

	@Test
	void refusesASimpleNameTwoWildcardsBothOffer() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		Files.writeString(tests.resolve("Composed.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				@Test public @interface FastTest {}
				""");
		Files.writeString(tests.resolve("Rival.java"), """
				package rival;
				public @interface FastTest {}
				""");
		// Both wildcards offer a FastTest the scan knows of, so which one the bare name
		// denotes cannot be told apart from the imports alone. Picking the trusted one
		// would exempt a class that may well be carrying the other.
		Files.writeString(tests.resolve("Ambiguous.java"), """
				package attack;
				import checks.*;
				import rival.*;
				class AmbiguousCase { @FastTest void looksLikeATest() {} }
				""");
		// One wildcard offering it stays unambiguous, even beside a wildcard whose
		// package holds nothing of that name.
		Files.writeString(tests.resolve("Single.java"), """
				package client;
				import java.util.*;
				import checks.*;
				class SingleCase { @FastTest void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "client.SingleCase" }, scanner.scanForTestClasses());
	}

	@Test
	void readsAQualifiedNameThroughATypeAWildcardBringsIntoScope() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// The wildcard brings a type named "de" into scope, which obscures the package
		// of that name, so the annotation below denotes the nest declared here and not
		// the Ares annotation its spelling copies.
		Files.writeString(tests.resolve("Shadow.java"), """
				package shadow;
				public class de {
				  public static class tum {
				    public static class cit {
				      public static class ase {
				        public static class ares {
				          public static class api {
				            public static class jupiter {
				              public @interface PublicTest {}
				            }
				          }
				        }
				      }
				    }
				  }
				}
				""");
		Files.writeString(tests.resolve("Obscured.java"), """
				package attack;
				import shadow.*;
				class ObscuredSpoof { @de.tum.cit.ase.ares.api.jupiter.PublicTest void looksLikeATest() {} }
				""");
		// Without that wildcard the same spelling denotes what it says.
		Files.writeString(tests.resolve("Plain.java"), """
				package client;
				class PlainCase { @de.tum.cit.ase.ares.api.jupiter.PublicTest void real() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "client.PlainCase" }, scanner.scanForTestClasses());
	}

	@Test
	void ignoresALocallyDeclaredJunitThreeSuperclass() throws IOException {
		Path production = Files.createDirectories(root.resolve("src/main/java"));
		Path tests = Files.createDirectories(root.resolve("src/test/java"));
		Files.writeString(production.resolve("Solution.java"), "package sol; class Solution {}\n");
		// Imported and written out in full, the superclass denotes JUnit 3's own.
		Files.writeString(tests.resolve("Imported.java"), """
				package legacy;
				import junit.framework.TestCase;
				class ImportedLegacy extends TestCase { public void testReal() {} }
				""");
		Files.writeString(tests.resolve("Qualified.java"), """
				package legacy;
				class QualifiedLegacy extends junit.framework.TestCase { public void testReal() {} }
				""");
		// Declaring a TestCase of one's own and extending it must not confer test-class
		// status, the same way a self-declared annotation does not.
		Files.writeString(tests.resolve("Spoof.java"), """
				package attack;
				class TestCase {}
				class SpoofedLegacy extends TestCase { public void testLooksLegacy() {} }
				""");
		// The same spoof with the declaration in a second file of that package: the
		// name then resolves through the package rather than through the compilation
		// unit, which is a separate resolution step and must reject it just as firmly.
		Files.writeString(tests.resolve("SplitTestCase.java"), """
				package split;
				class TestCase {}
				""");
		Files.writeString(tests.resolve("SplitSpoof.java"), """
				package split;
				class SplitSpoofedLegacy extends TestCase { public void testLooksLegacy() {} }
				""");
		// A wildcard import is the remaining way a bare TestCase can name the JUnit 3
		// class: nothing is imported by name and the package declares no type of that
		// name, so resolution falls through to the wildcarded candidates.
		Files.writeString(tests.resolve("WildcardLegacy.java"), """
				package wildcard;
				import junit.framework.*;
				class WildcardLegacy extends TestCase { public void testWildcarded() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		assertArrayEquals(new String[] { "legacy.ImportedLegacy", "legacy.QualifiedLegacy", "wildcard.WildcardLegacy" },
				scanner.scanForTestClasses());
	}

	private BuildToolConfiguration configuration(Path production, Path tests) {
		return new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production), List.of(tests),
				root.resolve("target/classes"), root.resolve("target/test-classes"));
	}
}
