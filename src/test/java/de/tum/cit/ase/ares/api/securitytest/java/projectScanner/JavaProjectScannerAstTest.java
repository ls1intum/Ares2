package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

	static Stream<Arguments> mainMethodShapes() {
		return Stream.of(Arguments.of("public static void main(String[] arguments) {}", true),
				Arguments.of("public static void main(java.lang.String[] arguments) {}", true),
				Arguments.of("public static void main(String... arguments) {}", true),
				Arguments.of("public static void main(java.lang.String... arguments) {}", true),
				Arguments.of("public static void main(int... arguments) {}", false),
				Arguments.of("public static void main(String argument) {}", false),
				Arguments.of("public static void main(int[] arguments) {}", false),
				Arguments.of("public static void main() {}", false),
				Arguments.of("public static void main(String[] arguments, int extra) {}", false),
				Arguments.of("static void main(String[] arguments) {}", false),
				Arguments.of("public void main(String[] arguments) {}", false),
				Arguments.of("public static int main(String[] arguments) { return 0; }", false),
				Arguments.of("public static void notMain(String[] arguments) {}", false));
	}

	@ParameterizedTest
	@MethodSource("mainMethodShapes")
	void classifiesEachMainMethodShape(String methodSource, boolean isRecognisedMain) throws IOException {
		Path production = Files.createDirectories(root.resolve("shape/main"));
		Path tests = Files.createDirectories(root.resolve("shape/test"));
		Files.writeString(production.resolve("Candidate.java"),
				"package shape; class Candidate { " + methodSource + " }\n");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		// A recognised main method resolves to its declaring class; any other shape
		// leaves
		// the scanner to fall back to the default main-class name.
		assertEquals(isRecognisedMain ? "Candidate" : "Main", scanner.scanForMainClassInPackage());
	}

	@Test
	void handlesDefaultPackageAndReservedPrefixesWhenScanning() throws IOException {
		Path production = Files.createDirectories(root.resolve("edge/main"));
		Path tests = Files.createDirectories(root.resolve("edge/test"));
		Files.writeString(production.resolve("Root.java"), "class Root {}\n");
		Files.writeString(production.resolve("Reserved.java"), "package java.fake; class Reserved {}\n");
		Files.writeString(production.resolve("Real.java"), "package real.app; class Real {}\n");
		Files.writeString(tests.resolve("DefaultPackageCases.java"),
				"import org.junit.jupiter.api.Test; class DefaultPackageCases { @Test void probe() {} }\n");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		// The blank default package and the reserved java.* prefix are both skipped, so
		// the only counted production package wins.
		assertEquals("real.app", scanner.scanForPackageName());
		// A test type in the default package has no package prefix in its qualified
		// name.
		assertArrayEquals(new String[] { "DefaultPackageCases" }, scanner.scanForTestClasses());
	}

	@Test
	void prefersMainThenApplicationAndRecognisesJUnitThreeTestCases() throws IOException {
		Path production = Files.createDirectories(root.resolve("pref/main"));
		Path tests = Files.createDirectories(root.resolve("pref/test"));
		Files.writeString(production.resolve("Mains.java"), """
				package pref;
				class Zebra { public static void main(String[] arguments) {} }
				class Application { public static void main(String[] arguments) {} }
				class Main { public static void main(String[] arguments) {} }
				""");
		Files.writeString(tests.resolve("LegacyTests.java"), """
				package pref;
				import junit.framework.TestCase;
				import org.junit.jupiter.api.Test;
				class LegacyCase extends TestCase { public void testSomething() {} }
				@Test class ClassLevelAnnotated {}
				class PlainHelper extends Object { void doWork() {} }
				""");
		JavaProjectScanner scanner = new JavaProjectScanner(configuration(production, tests));
		// Main is preferred over Application, which is preferred over any other name.
		assertEquals("Main", scanner.scanForMainClassInPackage());
		// A JUnit 3 class extending TestCase and a class with a class-level test
		// annotation are both recognised, while a plain class that does neither is not.
		assertArrayEquals(new String[] { "pref.ClassLevelAnnotated", "pref.LegacyCase" }, scanner.scanForTestClasses());
	}

	private BuildToolConfiguration configuration(Path production, Path tests) {
		return new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production), List.of(tests),
				root.resolve("target/classes"), root.resolve("target/test-classes"));
	}
}
