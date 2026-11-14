package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

/**
 * Checks which group a build descriptor is read to declare. What counts follows
 * what Gradle 9.9.1 and Maven themselves do, as measured: a local variable
 * named {@code group}, a group set only for subprojects and an inherited Maven
 * group are not the project's own.
 */
class ProjectSourcesFinderGroupTest {

	/** The temporary project. */
	@TempDir
	Path projectRoot;

	/** The project's own Maven groupId is read. */
	@Test
	void readsTheOwnMavenGroupId() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"), """
				<project><parent><groupId>org.parent</groupId></parent><groupId>de.tum.cit.aet</groupId>
				<dependencies><dependency><groupId>org.other</groupId></dependency></dependencies></project>
				""");

		assertEquals(Optional.of("de.tum.cit.aet"), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.MAVEN));
	}

	/** A groupId inherited from the parent is not the project's own. */
	@Test
	void ignoresAnInheritedMavenGroupId() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"), """
				<project><parent><groupId>org.parent</groupId></parent><artifactId>x</artifactId></project>
				""");

		assertEquals(Optional.empty(), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.MAVEN));
	}

	/** A property reference would be evaluated by Maven, so it is no literal. */
	@Test
	void ignoresAnInterpolatedMavenGroupId() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"), "<project><groupId>${base}.aet</groupId></project>");

		assertEquals(Optional.empty(), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.MAVEN));
	}

	/** A namespace prefix on the element does not hide it. */
	@Test
	void readsAPrefixedMavenGroupId() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"), """
				<p:project xmlns:p="http://maven.apache.org/POM/4.0.0"><p:groupId>de.tum.cit.aet</p:groupId></p:project>
				""");

		assertEquals(Optional.of("de.tum.cit.aet"), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.MAVEN));
	}

	/**
	 * What the narrow reader takes from a Groovy descriptor: a plain top-level
	 * literal assignment, and nothing where the descriptor uses the name in any
	 * other way, anywhere. A null expectation means no group, which also keeps
	 * gradle.properties from being read.
	 * 
	 * @param descriptor the build.gradle content @param expected the group read, or
	 *                   null
	 */
	@ParameterizedTest
	@MethodSource("groovyDescriptors")
	void readsOnlyAPlainLiteralAssignment(String descriptor, String expected) throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), descriptor + "\n");
		Files.writeString(projectRoot.resolve("gradle.properties"), "group=from.properties\n");

		assertEquals(Optional.ofNullable(expected), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.GRADLE));
	}

	/**
	 * Groovy descriptors and the group the narrow reader takes from each.
	 *
	 * @return the cases
	 */
	static Stream<Arguments> groovyDescriptors() {
		String group = "de.tum.cit.aet";
		return Stream.of(Arguments.of("group = 'de.tum.cit.aet'", group),
				Arguments.of("project.group = \"de.tum.cit.aet\"", group),
				Arguments.of("group = 'first.one'\ngroup = 'de.tum.cit.aet'", group),
				Arguments.of("group = 'de.tum.cit.aet'; version = '1'", group),
				Arguments.of("group = 'de.tum.cit.aet' // the exercise", group),
				Arguments.of("tasks.register('x') { group = 'verification' }", null),
				Arguments.of("subprojects { group = 'de.tum.cit.aet' }", null),
				Arguments.of("group = 'org.example'\nif (true) { project.group = 'org.example.exercise' }", null),
				Arguments.of("if (true) { group = 'org.example.exercise' }", null),
				Arguments.of("// group = 'de.tum.cit.aet'", "from.properties"),
				Arguments.of("println \"group = 'de.tum.cit.aet'\"", "from.properties"),
				Arguments.of("groupId = 'de.tum.cit.aet'", "from.properties"),
				Arguments.of("group 'de.tum.cit.aet'", null), Arguments.of("group(\"de.tum.cit.aet\")", null),
				Arguments.of("allprojects { group = 'de.tum.cit.aet' }", null),
				Arguments.of("def group = 'de.tum.cit.aet'", null),
				Arguments.of("String group = 'de.tum.cit.aet'", null),
				Arguments.of("def group = 'local'\ngroup = 'org.other'", null),
				Arguments.of("def label(String group) { return group }\ngroup = 'org.other'", null),
				Arguments.of("rootProject.group = 'de.tum.cit.aet'", null),
				Arguments.of("myproject.group = 'de.tum.cit.aet'", null), Arguments.of("group = \"${base}.aet\"", null),
				Arguments.of("group = 'de.tum' + '.cit.aet'", null),
				Arguments.of("group = 'de.tum.cit.aet'\ngroup = computeGroup()", null),
				Arguments.of("group = 'de.tum.cit.aet'\nprintln group", null), Arguments.of("group = ''", null));
	}

	/**
	 * A Kotlin local is not the project's group; an assignment is.
	 *
	 * @param descriptor the build.gradle.kts content
	 * @param expected   the group read, or null
	 */
	@ParameterizedTest
	@MethodSource("kotlinDescriptors")
	void readsTheGroupOfAKotlinDescriptor(String descriptor, String expected) throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle.kts"), descriptor + "\n");

		assertEquals(Optional.ofNullable(expected), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.GRADLE));
	}

	/**
	 * Kotlin descriptors and the group each declares.
	 *
	 * @return the cases
	 */
	static Stream<Arguments> kotlinDescriptors() {
		return Stream.of(Arguments.of("group = \"de.tum.cit.aet\"", "de.tum.cit.aet"),
				Arguments.of("val group = \"de.tum.cit.aet\"", null));
	}

	/**
	 * gradle.properties is read as Gradle reads it, any separator included.
	 *
	 * @param line the properties line
	 */
	@ParameterizedTest
	@ValueSource(strings = { "group=de.tum.cit.aet", "group : de.tum.cit.aet", "group de.tum.cit.aet" })
	void readsTheGroupOfGradleProperties(String line) throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }\n");
		Files.writeString(projectRoot.resolve("gradle.properties"), line + "\n");

		assertEquals(Optional.of("de.tum.cit.aet"), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.GRADLE));
	}

	/** The descriptor wins over gradle.properties, as it does in Gradle. */
	@Test
	void prefersTheDescriptorOverGradleProperties() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), "group = 'from.build'\n");
		Files.writeString(projectRoot.resolve("gradle.properties"), "group=from.properties\n");

		assertEquals(Optional.of("from.build"), ProjectSourcesFinder.declaredGroup(projectRoot, BuildMode.GRADLE));
	}
}
