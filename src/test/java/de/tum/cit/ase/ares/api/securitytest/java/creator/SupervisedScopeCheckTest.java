package de.tum.cit.ase.ares.api.securitytest.java.creator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The withinPath of a policy and its supervised package must name the same
 * student classes: a misspelt or foreign package and a withinPath that leaves
 * part of the package out are refused, a matching pair passes.
 */
class SupervisedScopeCheckTest {

	/** The compiled production output of the fake project. */
	private Path classes;

	/** The compiled test output of the fake project. */
	private Path testClasses;

	/**
	 * Writes a project whose student code is {@code org.example.Penguin} and
	 * {@code org.example.sub.Sub}, with the test class
	 * {@code org.example.PenguinTest}.
	 *
	 * @param project the project folder
	 * @throws IOException if a file cannot be written
	 */
	@BeforeEach
	void writeProject(@TempDir Path project) throws IOException {
		classes = project.resolve("target/classes");
		testClasses = project.resolve("target/test-classes");
		touch(classes.resolve("org/example/Penguin.class"));
		touch(classes.resolve("org/example/sub/Sub.class"));
		touch(testClasses.resolve("org/example/PenguinTest.class"));
	}

	/**
	 * Creates an empty file and its folders.
	 *
	 * @param file the file
	 * @throws IOException if it cannot be written
	 */
	private static void touch(Path file) throws IOException {
		Files.createDirectories(file.getParent());
		Files.createFile(file);
	}

	/**
	 * Runs the check with the test class exempted.
	 *
	 * @param scopeClassNames the classes found in the scope
	 * @param scope           the scope folder
	 * @param packageName     the supervised package
	 */
	private void check(List<String> scopeClassNames, Path scope, String packageName) {
		SupervisedScopeCheck.requireScopeToMatchPackage(scopeClassNames, scope, classes, packageName,
				List.of("org.example.PenguinTest"));
	}

	/**
	 * Asserts the check refuses and names the given class.
	 *
	 * @param className       the class the refusal must name
	 * @param scopeClassNames the classes found in the scope
	 * @param scope           the scope folder
	 * @param packageName     the supervised package
	 */
	private void assertRefusedNaming(String className, List<String> scopeClassNames, Path scope, String packageName) {
		SecurityException refusal = assertThrows(SecurityException.class,
				() -> check(scopeClassNames, scope, packageName));
		assertTrue(refusal.getMessage().contains(className), refusal.getMessage());
	}

	/** A withinPath and package that name the same classes pass. */
	@Test
	void matchingScopeAndPackagePass() {
		assertDoesNotThrow(() -> check(List.of("org.example.Penguin", "org.example.sub.Sub"),
				classes.resolve("org/example"), "org.example"));
	}

	/** A parent of the student package still covers every student class. */
	@Test
	void parentPackagePasses() {
		assertDoesNotThrow(() -> check(List.of("org.example.Penguin", "org.example.sub.Sub"),
				classes.resolve("org/example"), "org"));
	}

	/** A misspelt, a foreign or a too narrow package is refused. */
	@Test
	void packageThatMissesTheStudentClassesIsRefused() {
		List<String> studentClasses = List.of("org.example.Penguin", "org.example.sub.Sub");
		Path scope = classes.resolve("org/example");
		assertRefusedNaming("org.example.Penguin", studentClasses, scope, "org.exmaple");
		assertRefusedNaming("org.example.Penguin", studentClasses, scope, "com.other");
		assertRefusedNaming("org.example.Penguin", studentClasses, scope, "org.example.sub");
		assertRefusedNaming("org.example.Penguin", studentClasses, scope, "org.ex");
	}

	/**
	 * A withinPath naming a sub-folder leaves the rest of the package unchecked.
	 */
	@Test
	void scopeNamingASubFolderIsRefused() {
		assertRefusedNaming("org.example.Penguin", List.of("org.example.sub.Sub"), classes.resolve("org/example/sub"),
				"org.example");
	}

	/** A withinPath naming the test output leaves the student code unchecked. */
	@Test
	void scopeNamingTheTestOutputIsRefused() {
		assertRefusedNaming("org.example.Penguin", List.of("org.example.PenguinTest"),
				testClasses.resolve("org/example"), "org.example");
	}

	/** Ares' own packages in the output take no part, as the checks skip them. */
	@Test
	void reservedPackagesAreLeftOut() throws IOException {
		touch(classes.resolve("de/tum/cit/ase/ares/api/Policy.class"));
		assertDoesNotThrow(() -> SupervisedScopeCheck.requireScopeToMatchPackage(List.of("de.tum.cit.ase.ares.Main"),
				testClasses, classes, "de.tum.cit.ase.ares", List.of()));
	}
}
