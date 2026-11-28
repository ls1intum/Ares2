package de.tum.cit.ase.ares.api.securitytest.java.creator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Checks, on a real Maven project, that the creator reports its test cases as
 * created from unchanged input only while the build file, the supervised class
 * files and the exempted test class are unchanged.
 */
class JavaCreatorUnchangedInputTest {

	/** A compiled fixture without nested classes, standing in for a test class. */
	private static final String TEST_CLASS = "example.nest.NestHostFixture$Evil";

	/** The supervised package. */
	private static final String PACKAGE = "example.task";

	/** The project the creator works on. */
	@TempDir
	Path project;

	/** The folder of the supervised class files. */
	private Path supervised;

	/** The creator, with its test cases created. */
	private JavaCreator creator;

	/**
	 * Writes a Maven project with one supervised class file and the test class, and
	 * creates the test cases for it.
	 *
	 * @throws IOException if the project cannot be written
	 */
	@BeforeEach
	void createTestCases() throws IOException {
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		supervised = Files.createDirectories(project.resolve("target/classes/example/task"));
		copyFixture(TEST_CLASS, supervised.resolve("Supervised.class"));
		Path testClass = project.resolve("target/test-classes/" + TEST_CLASS.replace('.', '/') + ".class");
		Files.createDirectories(testClass.getParent());
		copyFixture(TEST_CLASS, testClass);
		creator = new JavaCreator(ProjectSourcesFinder.discover(project, BuildMode.MAVEN));
		creator.createTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of("java.lang"),
				List.of(), List.of(TEST_CLASS), PACKAGE, "Main", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), ResourceAccesses.createRestrictive(), Path.of(""), false);
	}

	/** Nothing changed, so the test cases still describe the project. */
	@Test
	void unchangedInputIsReported() {
		assertTrue(creator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));
	}

	/**
	 * A class file added to the supervised folder ends the reuse.
	 *
	 * @throws IOException if the class file cannot be written
	 */
	@Test
	void anAddedSupervisedClassFileIsAChange() throws IOException {
		copyFixture("example.nest.NestHostFixture", supervised.resolve("Added.class"));
		assertFalse(creator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));
	}

	/**
	 * A build file that moves the source roots ends the reuse.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void aChangedBuildFileIsAChange() throws IOException {
		Files.createDirectories(project.resolve("elsewhere"));
		Files.writeString(project.resolve("pom.xml"),
				"<project><build><sourceDirectory>elsewhere</sourceDirectory></build></project>");
		assertFalse(creator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));
	}

	/**
	 * A test class that can no longer be verified is refused, as a fresh
	 * preparation would refuse it, rather than reused.
	 *
	 * @throws IOException if the test class file cannot be deleted
	 */
	@Test
	void aRemovedTestClassIsRefused() throws IOException {
		Files.delete(project.resolve("target/test-classes/" + TEST_CLASS.replace('.', '/') + ".class"));
		assertThrows(SecurityException.class, () -> creator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));
	}

	/**
	 * A supervised class path that now resolves to another folder, here through a
	 * link that was pointed elsewhere, ends the reuse, since a fresh preparation
	 * would analyse that folder.
	 *
	 * @throws IOException if the folders or the link cannot be written
	 */
	@Test
	void aRetargetedScopeIsAChange() throws IOException {
		Path classes = project.resolve("target/classes");
		Path link = classes.resolve("linked");
		Path other = Files.createDirectories(classes.resolve("example/other"));
		copyFixture(TEST_CLASS, other.resolve("Other.class"));
		try {
			Files.createSymbolicLink(link, supervised);
		} catch (UnsupportedOperationException | IOException noLinks) {
			org.junit.jupiter.api.Assumptions.abort("this file system cannot create a symbolic link");
		}
		JavaCreator linkedCreator = new JavaCreator(ProjectSourcesFinder.discover(project, BuildMode.MAVEN));
		linkedCreator.createTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of("java.lang"),
				List.of(), List.of(TEST_CLASS), PACKAGE, "Main", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), ResourceAccesses.createRestrictive(), Path.of("classes/linked"), false);
		assertTrue(linkedCreator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));

		Files.delete(link);
		Files.createSymbolicLink(link, other);

		assertFalse(linkedCreator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT));
	}

	/**
	 * A later phase checks the supervised classes for reserved packages again: a
	 * class planted there after the test cases were created is refused.
	 *
	 * @throws IOException if the class file cannot be copied
	 */
	@Test
	void aPlantedReservedClassIsRefusedOnReuse() throws IOException {
		creator.requireNoReservedSupervisedClass();
		copyFixture(de.tum.cit.ase.ares.api.securitytest.ReservedPackageGuard.class.getName(),
				supervised.resolve("Planted.class"));
		assertThrows(SecurityException.class, creator::requireNoReservedSupervisedClass);
	}

	/**
	 * Copies a compiled fixture's class file.
	 *
	 * @param className the fixture's binary name
	 * @param target    where to copy it
	 * @throws IOException if it cannot be copied
	 */
	private static void copyFixture(String className, Path target) throws IOException {
		try (InputStream input = JavaCreatorUnchangedInputTest.class.getClassLoader()
				.getResourceAsStream(className.replace('.', '/') + ".class")) {
			Files.write(target, Objects.requireNonNull(input, "the fixture is compiled").readAllBytes());
		}
	}
}
