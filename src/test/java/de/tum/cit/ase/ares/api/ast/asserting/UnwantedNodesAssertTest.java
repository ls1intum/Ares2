package de.tum.cit.ase.ares.api.ast.asserting;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;

import de.tum.cit.ase.ares.api.AresConfiguration;
import de.tum.cit.ase.ares.api.ast.AstSourceFiles;
import de.tum.cit.ase.ares.api.ast.model.UnwantedNode;
import de.tum.cit.ase.ares.api.ast.type.ClassType;
import de.tum.cit.ase.ares.api.ast.type.ConditionalType;
import de.tum.cit.ase.ares.api.ast.type.ExceptionHandlingType;
import de.tum.cit.ase.ares.api.ast.type.LoopType;
import de.tum.cit.ase.ares.api.ast.type.Type;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Checks {@link UnwantedNodesAssert} and the kinds of node it can look for.
 */
class UnwantedNodesAssertTest {

	/**
	 * The message shown when no language level was chosen.
	 */
	private static final String LEVEL_MESSAGE = "The 'level' is not set. Please use UnwantedNodesAssert.withLanguageLevel(LanguageLevel).";

	/**
	 * A class with one for loop that starts on line 3 at column 3.
	 */
	private static final String LOOP_SOURCE = "class A {\n  void m() {\n  for (int i = 0; i < 1; i++) {}\n  }\n}\n";

	/**
	 * A class with a method that has no statements.
	 */
	private static final String CLEAN_SOURCE = "class A {\n  void m() {\n  }\n}\n";

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * The parser configuration found before the test, put back afterwards.
	 */
	private ParserConfiguration savedConfiguration;

	/**
	 * The pom path found before the test, put back afterwards.
	 */
	private String savedPomPath;

	/**
	 * The Gradle build path found before the test, put back afterwards.
	 */
	private String savedGradlePath;

	/**
	 * Installs the Java 17 parser and remembers the old settings.
	 */
	@BeforeEach
	void rememberSettings() {
		savedConfiguration = StaticJavaParser.getParserConfiguration();
		StaticJavaParser.setConfiguration(new ParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_17));
		savedPomPath = ProjectSourcesFinder.getPomXmlPath();
		savedGradlePath = ProjectSourcesFinder.getBuildGradlePath();
	}

	/**
	 * Puts the old parser configuration and build file paths back.
	 */
	@AfterEach
	void restoreSettings() {
		StaticJavaParser.setConfiguration(savedConfiguration);
		AresConfiguration.setPomXmlPath(savedPomPath);
		AresConfiguration.setBuildGradlePath(savedGradlePath);
	}

	/**
	 * Wraps statements into a method of a class.
	 */
	private static String withBody(String body) {
		return "class A {\n  void m() {\n  " + body + "\n  }\n}\n";
	}

	/**
	 * Asserts on the folder with the Java 17 language level.
	 */
	private static UnwantedNodesAssert checking(Path folder) {
		return UnwantedNodesAssert.assertThatSourcesIn(folder).withLanguageLevel(LanguageLevel.JAVA_17);
	}

	/**
	 * Without a language level the check refuses to run.
	 */
	@Test
	void failsWithoutALanguageLevel() throws IOException {
		AstSourceFiles.write(directory, "A.java", LOOP_SOURCE);
		assertThatThrownBy(() -> UnwantedNodesAssert.assertThatSourcesIn(directory).hasNo(LoopType.ANY))
				.isInstanceOf(AssertionError.class).hasMessage(LEVEL_MESSAGE);
	}

	/**
	 * A folder that does not exist is refused with its path in the message.
	 */
	@Test
	void refusesAMissingFolder() {
		Path missing = directory.resolve("absent");
		assertThatThrownBy(() -> UnwantedNodesAssert.assertThatSourcesIn(missing)).isInstanceOf(AssertionError.class)
				.hasMessage(String.format("The source directory %s does not exist", missing));
	}

	/**
	 * An unwanted loop is reported with its file and its position.
	 */
	@Test
	void reportsAnUnwantedNodeWithItsPosition() throws IOException {
		Path file = AstSourceFiles.write(directory, "A.java", LOOP_SOURCE);
		String expected = localized("ast.method.has_no") + System.lineSeparator()
				+ UnwantedNode
						.getMessageForUnwantedNodesForAllFilesBelow(directory, LoopType.ANY.getNodeNameNodeMap(), false)
						.orElseThrow();
		assertThatThrownBy(() -> checking(directory).hasNo(LoopType.ANY)).isInstanceOf(AssertionError.class)
				.hasMessage(expected).hasMessageContaining(file.toString())
				.hasMessageContaining(localized("ast.check.has_begin_end", 3, 3));
	}

	/**
	 * A clean folder passes and the same object comes back for chaining.
	 */
	@Test
	void passesAndChainsWhenNothingIsFound() throws IOException {
		AstSourceFiles.write(directory, "A.java", CLEAN_SOURCE);
		UnwantedNodesAssert assertion = checking(directory);
		UnwantedNodesAssert returned = assertion.hasNo(LoopType.ANY);
		assertThat(returned).isSameAs(assertion);
		assertThat(returned.hasNo(ConditionalType.IFSTMT)).isSameAs(assertion);
	}

	/**
	 * A loop in the main method is ignored only when the main method is excluded.
	 */
	@Test
	void excludeMainMethodSkipsOnlyTheMainMethod() throws IOException {
		AstSourceFiles.write(directory, "A.java",
				"class A {\n  public static void main(String[] args) {\n  for (int i = 0; i < 1; i++) {}\n  }\n}\n");
		UnwantedNodesAssert excluded = checking(directory).excludeMainMethod();
		assertThat(excluded).isNotNull();
		assertThat(excluded.hasNo(LoopType.ANY)).isSameAs(excluded);
		assertThatThrownBy(() -> checking(directory).hasNo(LoopType.ANY)).isInstanceOf(AssertionError.class);
	}

	/**
	 * Only the chosen package and its folders are looked at.
	 */
	@Test
	void withinPackageNarrowsToThatFolder() throws IOException {
		AstSourceFiles.write(directory, "a/b/X.java", LOOP_SOURCE.replace("class A", "class X"));
		AstSourceFiles.write(directory, "c/Y.java", CLEAN_SOURCE.replace("class A", "class Y"));
		UnwantedNodesAssert narrowed = checking(directory).withinPackage("c");
		assertThat(narrowed.hasNo(LoopType.ANY)).isSameAs(narrowed);
		assertThatThrownBy(() -> checking(directory).withinPackage("a.b").hasNo(LoopType.ANY))
				.isInstanceOf(AssertionError.class);
	}

	/**
	 * Pairs of a kind of node and a statement that contains it.
	 */
	static Stream<Arguments> kindsOfNodes() {
		return Stream.of(Arguments.of(LoopType.FORSTMT, "for (int i = 0; i < 1; i++) {}"),
				Arguments.of(ConditionalType.IFSTMT, "if (true) {}"), Arguments.of(ClassType.CLASS, "class L {}"),
				Arguments.of(ExceptionHandlingType.THROW, "throw new RuntimeException();"));
	}

	/**
	 * Each kind of node is found where it is and not found where it is absent.
	 */
	@ParameterizedTest
	@MethodSource("kindsOfNodes")
	void findsEachKindOfNode(Type type, String statement) throws IOException {
		AstSourceFiles.write(directory, "with/A.java", withBody(statement));
		AstSourceFiles.write(directory, "without/A.java", withBody(""));
		assertThatThrownBy(() -> checking(directory).withinPackage("with").hasNo(type))
				.isInstanceOf(AssertionError.class);
		UnwantedNodesAssert clean = checking(directory).withinPackage("without");
		assertThat(clean.hasNo(type)).isSameAs(clean);
	}

	/**
	 * The source folder named in the pom is the one that is checked.
	 */
	@Test
	void projectSourcesComeFromTheBuildFile() throws IOException {
		Path sources = directory.resolve("my-sources");
		AstSourceFiles.write(sources, "A.java", LOOP_SOURCE);
		Path pom = AstSourceFiles.write(directory, "pom.xml",
				"<project><build><sourceDirectory>" + sources + "</sourceDirectory></build></project>");
		AresConfiguration.setPomXmlPath(pom.toString());
		AresConfiguration.setBuildGradlePath(directory.resolve("absent.gradle").toString());
		assertThatThrownBy(() -> UnwantedNodesAssert.assertThatProjectSources().withLanguageLevel(LanguageLevel.JAVA_17)
				.hasNo(LoopType.ANY)).isInstanceOf(AssertionError.class).hasMessageContaining("A.java");
		assertThatThrownBy(() -> checking(sources).hasNo(LoopType.ANY)).isInstanceOf(AssertionError.class)
				.hasMessageContaining(sources.toString() + File.separator + "A.java");
	}

	/**
	 * Without any build file the check says so clearly.
	 */
	@Test
	void failsClearlyWithoutABuildFile() {
		AresConfiguration.setPomXmlPath(directory.resolve("absent.xml").toString());
		AresConfiguration.setBuildGradlePath(directory.resolve("absent.gradle").toString());
		assertThatThrownBy(UnwantedNodesAssert::assertThatProjectSources).isInstanceOf(AssertionError.class)
				.hasMessageStartingWith("Could not find project sources folder.");
	}
}
