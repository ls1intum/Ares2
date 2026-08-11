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

	private BuildToolConfiguration configuration(Path production, Path tests) {
		return new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production), List.of(tests),
				root.resolve("target/classes"), root.resolve("target/test-classes"));
	}
}
