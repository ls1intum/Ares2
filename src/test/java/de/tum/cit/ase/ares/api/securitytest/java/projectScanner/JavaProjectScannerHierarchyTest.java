package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Checks the order in which Ares settles the supervised package when no policy
 * pins it: the build descriptor's group where it fits the code, then the
 * topmost declared package, then the fixed default, and that the check before
 * enforcement still refuses whatever the code contradicts.
 */
@DisplayName("JavaProjectScanner derivation hierarchy")
class JavaProjectScannerHierarchyTest {

	/** The temporary project. */
	@TempDir
	Path projectRoot;

	/** A parent package wins over a child with more files. */
	@Test
	void prefersTheParentOverABusierChild() throws IOException {
		gradle("");
		source("de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");
		source("de/tum/cit/aet/util/A.java", "package de.tum.cit.aet.util; public class A { }");
		source("de/tum/cit/aet/util/B.java", "package de.tum.cit.aet.util; public class B { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("de.tum.cit.aet", scanner.scanForPackageName());
		assertDoesNotThrow(() -> scanner.requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	/** Deeper nesting still yields the topmost package. */
	@Test
	void yieldsTheTopmostOfADeepNest() throws IOException {
		gradle("");
		source("a/A.java", "package a; public class A { }");
		source("a/b/B.java", "package a.b; public class B { }");
		source("a/b/c/C.java", "package a.b.c; public class C { }");
		source("a/b/c/D.java", "package a.b.c; public class D { }");

		assertEquals("a", scanner().scanForPackageName());
	}

	/**
	 * Between disjoint packages each weighs with everything below it, so the
	 * assignment with a helper package outweighs a decoy that has more files at its
	 * own level.
	 */
	@Test
	void weighsDisjointPackagesWithTheirSubtrees() throws IOException {
		gradle("");
		source("real/Main.java", "package real; public class Main { }");
		source("real/util/A.java", "package real.util; public class A { }");
		source("real/util/B.java", "package real.util; public class B { }");
		source("decoy/First.java", "package decoy; public class First { }");
		source("decoy/Second.java", "package decoy; public class Second { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("real", scanner.scanForPackageName());
		assertThrows(SecurityException.class, () -> scanner.requireDerivedScopeToCoverTheProject("real"));
	}

	/** Equally heavy disjoint packages are decided by name. */
	@Test
	void breaksATieByName() throws IOException {
		gradle("");
		source("beta/B.java", "package beta; public class B { }");
		source("alpha/A.java", "package alpha; public class A { }");

		assertEquals("alpha", scanner().scanForPackageName());
	}

	/**
	 * The compiled output, read when there are no source roots, follows the same
	 * rule.
	 */
	@Test
	void appliesTheRuleToTheCompiledOutput() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
		compile(outputRoot, "de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");
		compile(outputRoot, "de/tum/cit/aet/util/A.java", "package de.tum.cit.aet.util; public class A { }");
		compile(outputRoot, "de/tum/cit/aet/util/B.java", "package de.tum.cit.aet.util; public class B { }");

		JavaProjectScanner scanner = new JavaProjectScanner(
				ScannerFixtures.gradleConfigurationWithoutSourceRoots(projectRoot, outputRoot));

		assertEquals("de.tum.cit.aet", scanner.scanForPackageName());
	}

	/**
	 * A group covering two disjoint children is used, which the topmost-package
	 * rule alone could not settle without refusing one of them.
	 */
	@Test
	void usesAGroupThatCoversDisjointChildren() throws IOException {
		maven("de.tum.cit.aet");
		source("de/tum/cit/aet/model/Model.java", "package de.tum.cit.aet.model; public class Model { }");
		source("de/tum/cit/aet/view/View.java", "package de.tum.cit.aet.view; public class View { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("de.tum.cit.aet", scanner.scanForPackageName());
		assertDoesNotThrow(() -> scanner.requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	/** The same in Gradle, through the group in gradle.properties. */
	@Test
	void usesTheGroupOfGradleProperties() throws IOException {
		gradle("");
		Files.writeString(projectRoot.resolve("gradle.properties"), "group=de.tum.cit.aet\n");
		source("de/tum/cit/aet/model/Model.java", "package de.tum.cit.aet.model; public class Model { }");
		source("de/tum/cit/aet/view/View.java", "package de.tum.cit.aet.view; public class View { }");

		assertEquals("de.tum.cit.aet", scanner().scanForPackageName());
	}

	/**
	 * A computed group in the descriptor overrides the one in gradle.properties, as
	 * in Gradle, so the wider overridden group is not revived and the package comes
	 * from the sources.
	 */
	@Test
	void doesNotReviveAGroupTheDescriptorOverrides() throws IOException {
		gradle("group = 'org.example.' + 'exercise'\n");
		Files.writeString(projectRoot.resolve("gradle.properties"), "group=org.example\n");
		source("org/example/exercise/Main.java", "package org.example.exercise; public class Main { }");

		assertEquals("org.example.exercise", scanner().scanForPackageName());
	}

	/**
	 * A descriptor that uses {@code group} in a way the reader does not take
	 * literally yields no group, not the one of gradle.properties, so the topmost
	 * package decides.
	 */
	@Test
	void fallsBackWhereTheDescriptorUsesTheGroupOtherwise() throws IOException {
		gradle("def group = 'local'\ngroup = 'org.other'\n");
		Files.writeString(projectRoot.resolve("gradle.properties"), "group=org.example\n");
		source("org/example/Main.java", "package org.example; public class Main { }");
		source("org/example/model/Model.java", "package org.example.model; public class Model { }");

		assertEquals("org.example", scanner().scanForPackageName());
	}

	/**
	 * A group that does not fit the code is passed over, and the decoy that then
	 * wins is still refused before anything is armed.
	 */
	@Test
	void passesOverAGroupThatDoesNotCoverTheCode() throws IOException {
		maven("de.tum.cit.aet");
		source("de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");
		source("decoy/First.java", "package decoy; public class First { }");
		source("decoy/Second.java", "package decoy; public class Second { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("decoy", scanner.scanForPackageName());
		assertThrows(SecurityException.class, () -> scanner.requireDerivedScopeToCoverTheProject("decoy"));
	}

	/**
	 * Groups that are no package, or lie inside or above a trusted namespace, are
	 * passed over.
	 */
	@Test
	void passesOverGroupsThatCannotBeAScope() throws IOException {
		source("de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");
		for (String group : new String[] { "my-group", "org.junit.fake", "de.tum" }) {
			maven(group);

			assertEquals("de.tum.cit.aet", scanner().scanForPackageName(), group);
		}
	}

	/** Without any declared package the group still beats the fixed default. */
	@Test
	void prefersTheGroupOverTheDefaultInAnEmptyProject() throws IOException {
		maven("org.example.exercise");
		Files.createDirectories(projectRoot.resolve("src/main/java"));

		assertEquals("org.example.exercise",
				new JavaProgrammingExerciseProjectScanner(ProjectSourcesFinder.discover(projectRoot))
						.scanForPackageName());
	}

	/**
	 * The group is chosen from what the sources declare, but the check reads the
	 * compiled output, where a class the sources do not show is still refused.
	 */
	@Test
	void stillRefusesACompiledClassOutsideTheGroup() throws IOException {
		maven("de.tum.cit.aet");
		source("de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");
		compile(projectRoot.resolve("target/classes"), "elsewhere/Hidden.java",
				"package elsewhere; public class Hidden { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("de.tum.cit.aet", scanner.scanForPackageName());
		assertThrows(SecurityException.class, () -> scanner.requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	/**
	 * A package declared only by a descriptor above the assignment would make a
	 * scope containing Ares itself. The check refuses it before anything else.
	 */
	@Test
	void refusesADerivedScopeAboveATrustedNamespace() throws IOException {
		gradle("");
		source("de/tum/cit/package-info.java", "@Deprecated package de.tum.cit;");
		source("de/tum/cit/aet/Main.java", "package de.tum.cit.aet; public class Main { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("de.tum.cit", scanner.scanForPackageName());
		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner.requireDerivedScopeToCoverTheProject("de.tum.cit"));
		assertTrue(refusal.getMessage().contains("de.tum.cit.ase.ares.api."), refusal.getMessage());
	}

	/** The same for a compiled class in a package above the test frameworks. */
	@Test
	void refusesACompiledScopeAboveTheTestFrameworks() throws IOException {
		gradle("");
		source("org/Helper.java", "package org; public class Helper { }");
		source("org/example/Main.java", "package org.example; public class Main { }");

		JavaProjectScanner scanner = scanner();

		assertEquals("org", scanner.scanForPackageName());
		assertThrows(SecurityException.class, () -> scanner.requireDerivedScopeToCoverTheProject("org"));
	}

	/**
	 * Writes a Gradle descriptor.
	 *
	 * @param extra further descriptor lines
	 * @throws IOException if it cannot be written
	 */
	private void gradle(String extra) throws IOException {
		Files.deleteIfExists(projectRoot.resolve("pom.xml"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }\n" + extra);
	}

	/**
	 * Writes a Maven descriptor with the given groupId.
	 *
	 * @param groupId the groupId
	 * @throws IOException if it cannot be written
	 */
	private void maven(String groupId) throws IOException {
		Files.deleteIfExists(projectRoot.resolve("build.gradle"));
		Files.writeString(projectRoot.resolve("pom.xml"),
				"<project><groupId>" + groupId + "</groupId><artifactId>x</artifactId></project>");
	}

	/**
	 * Writes a production source and compiles it into the conventional output of
	 * the descriptor present.
	 *
	 * @param relativePath the path below {@code src/main/java}
	 * @param source       the Java source
	 * @throws IOException if the fixture cannot be written
	 */
	private void source(String relativePath, String source) throws IOException {
		Path file = projectRoot.resolve("src/main/java").resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
		Path output = Files.isRegularFile(projectRoot.resolve("pom.xml")) ? projectRoot.resolve("target/classes")
				: projectRoot.resolve("build/classes/java/main");
		Files.createDirectories(output);
		assertEquals(0,
				ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", output.toString(), file.toString()),
				"the fixture did not compile: " + relativePath);
	}

	/**
	 * Compiles a source kept outside the source roots into an output root.
	 *
	 * @param outputRoot   the output root
	 * @param relativePath the path of the source below a scratch folder
	 * @param source       the Java source
	 * @throws IOException if the fixture cannot be written
	 */
	private void compile(Path outputRoot, String relativePath, String source) throws IOException {
		Path file = projectRoot.resolve("scratch").resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.createDirectories(outputRoot);
		Files.writeString(file, source);
		assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", outputRoot.toString(),
				file.toString()), "the fixture did not compile: " + relativePath);
	}

	/**
	 * A scanner over the project as discovered from its descriptor.
	 *
	 * @return the scanner
	 */
	private JavaProjectScanner scanner() {
		return new JavaProjectScanner(ProjectSourcesFinder.discover(projectRoot));
	}
}
