package de.tum.cit.ase.ares.api.ast.asserting;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import de.tum.cit.ase.ares.api.AresConfiguration;
import de.tum.cit.ase.ares.api.ast.AstSourceFiles;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Checks {@link UnwantedRecursionAssert}: the refusals of its settings, the
 * filters for the starting method, the excluded methods and the package, and
 * the two messages of the recursion check.
 */
class UnwantedRecursionAssertTest {

	/**
	 * The message shown when no language level was chosen.
	 */
	private static final String LEVEL_MESSAGE = "The 'level' is not set. Please use UnwantedNodesAssert.withLanguageLevel(LanguageLevel).";

	/**
	 * Class {@code p.A} whose methods {@code a()} and {@code b()} call each other.
	 */
	private static final String CYCLE_SOURCE = "package p;\nclass A {\nvoid a() { b(); }\nvoid b() { a(); }\n}\n";

	/**
	 * Class {@code p.A} whose method {@code a()} calls {@code b()}, which ends.
	 */
	private static final String CHAIN_SOURCE = "package p;\nclass A {\nvoid a() { b(); }\nvoid b() { }\n}\n";

	/**
	 * Class {@code p.A} whose method {@code a()} calls itself.
	 */
	private static final String SELF_SOURCE = "package p;\nclass A {\nvoid a() { a(); }\n}\n";

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * The pom path found before the test, put back afterwards.
	 */
	private String savedPomPath;

	/**
	 * The Gradle build path found before the test, put back afterwards.
	 */
	private String savedGradlePath;

	/**
	 * Remembers the build file paths.
	 */
	@BeforeEach
	void rememberSettings() {
		savedPomPath = ProjectSourcesFinder.getPomXmlPath();
		savedGradlePath = ProjectSourcesFinder.getBuildGradlePath();
	}

	/**
	 * Puts the old build file paths back.
	 */
	@AfterEach
	void restoreSettings() {
		AresConfiguration.setPomXmlPath(savedPomPath);
		AresConfiguration.setBuildGradlePath(savedGradlePath);
	}

	/**
	 * Asserts on the folder with the Java 17 language level.
	 */
	private static UnwantedRecursionAssert checking(Path folder) {
		return UnwantedRecursionAssert.assertThatSourcesIn(folder).withLanguageLevel(LanguageLevel.JAVA_17);
	}

	/**
	 * Reads the pattern that a method name has to match.
	 */
	private static String startingNodeRegex() throws ReflectiveOperationException {
		Field field = UnwantedRecursionAssert.class.getDeclaredField("STARTING_NODE_REGEX");
		field.setAccessible(true);
		return (String) field.get(null);
	}

	/**
	 * A starting method that is not written as a full name is refused.
	 */
	@Test
	void refusesAStartingMethodOfTheWrongShape() throws IOException, ReflectiveOperationException {
		AstSourceFiles.write(directory, "p/A.java", CHAIN_SOURCE);
		UnwantedRecursionAssert assertion = checking(directory);
		assertThatThrownBy(() -> assertion.startingWithMethod("a")).isInstanceOf(AssertionError.class).hasMessage(String
				.format("The starting method %s does not match the regular expression %s", "a", startingNodeRegex()));
		assertThatCode(() -> assertion.startingWithMethod("p.A.a()")).doesNotThrowAnyException();
	}

	/**
	 * A starting method may not be one of the excluded methods.
	 */
	@Test
	void refusesAStartingMethodThatIsAlsoExcluded() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", CHAIN_SOURCE);
		UnwantedRecursionAssert excluding = checking(directory).excludeMethods("p.A.a()");
		assertThatThrownBy(() -> excluding.startingWithMethod("p.A.a()")).isInstanceOf(AssertionError.class)
				.hasMessage("The starting method p.A.a() is also excluded from the recursion check");
		assertThatCode(() -> excluding.startingWithMethod("p.A.b()")).doesNotThrowAnyException();
		assertThatCode(() -> checking(directory).startingWithMethod("p.A.a()")).doesNotThrowAnyException();
	}

	/**
	 * A folder that does not exist is refused with its path in the message.
	 */
	@Test
	void refusesAMissingFolder() {
		Path missing = directory.resolve("absent");
		assertThatThrownBy(() -> UnwantedRecursionAssert.assertThatSourcesIn(missing))
				.isInstanceOf(AssertionError.class)
				.hasMessage(String.format("The source directory %s does not exist", missing));
	}

	/**
	 * An excluded method that is not written as a full name is refused.
	 */
	@Test
	void refusesAnExcludedMethodOfTheWrongShape() throws IOException, ReflectiveOperationException {
		AstSourceFiles.write(directory, "p/A.java", CHAIN_SOURCE);
		UnwantedRecursionAssert assertion = checking(directory);
		assertThatThrownBy(() -> assertion.excludeMethods("p.A.a()", "b")).isInstanceOf(AssertionError.class)
				.hasMessage(String.format("The method %s does not match the regular expression %s", "b",
						startingNodeRegex()));
		assertThatCode(() -> assertion.excludeMethods("p.A.a()")).doesNotThrowAnyException();
	}

	/**
	 * Without a language level the check refuses to run.
	 */
	@Test
	void failsWithoutALanguageLevel() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", CYCLE_SOURCE);
		assertThatThrownBy(() -> UnwantedRecursionAssert.assertThatSourcesIn(directory).hasNoRecursion())
				.isInstanceOf(AssertionError.class).hasMessage(LEVEL_MESSAGE);
	}

	/**
	 * A cycle fails the check with the heading and both methods, a straight chain
	 * passes.
	 */
	@Test
	void hasNoRecursionFailsOnACycle() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", CYCLE_SOURCE);
		assertThatThrownBy(() -> checking(directory).hasNoRecursion()).isInstanceOf(AssertionError.class)
				.hasMessageStartingWith(localized("ast.method.has_no") + System.lineSeparator())
				.hasMessageContaining("p.A.a()").hasMessageContaining("p.A.b()");
		AstSourceFiles.write(directory, "p/A.java", CHAIN_SOURCE);
		assertThatCode(() -> checking(directory).hasNoRecursion()).doesNotThrowAnyException();
	}

	/**
	 * Asking for a recursion that is missing fails with the two messages, a cycle
	 * passes.
	 */
	@Test
	void hasRecursionFailsWithoutACycle() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", CHAIN_SOURCE);
		assertThatThrownBy(() -> checking(directory).hasRecursion()).isInstanceOf(AssertionError.class)
				.hasMessage(localized("ast.recursion.has.recursion") + System.lineSeparator()
						+ localized("ast.recursion.no.recursion.found"));
		AstSourceFiles.write(directory, "p/A.java", CYCLE_SOURCE);
		assertThatCode(() -> checking(directory).hasRecursion()).doesNotThrowAnyException();
	}

	/**
	 * Excluding one method of a cycle removes the cycle from the check.
	 */
	@Test
	void excludingAMemberBreaksTheCycle() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", CYCLE_SOURCE);
		assertThatThrownBy(() -> checking(directory).hasNoRecursion()).isInstanceOf(AssertionError.class);
		assertThatCode(() -> checking(directory).excludeMethods("p.A.b()").hasNoRecursion()).doesNotThrowAnyException();
	}

	/**
	 * Only the methods reachable from the starting method are checked.
	 */
	@Test
	void startingMethodLimitsTheCheck() throws IOException {
		AstSourceFiles.write(directory, "p/A.java",
				"package p;\nclass A {\nvoid a() { b(); }\nvoid b() { a(); }\nvoid c() { d(); }\nvoid d() { }\n}\n");
		assertThatCode(() -> checking(directory).startingWithMethod("p.A.c()").hasNoRecursion())
				.doesNotThrowAnyException();
		assertThatThrownBy(() -> checking(directory).startingWithMethod("p.A.a()").hasNoRecursion())
				.isInstanceOf(AssertionError.class);
	}

	/**
	 * A package narrows the check to its folder.
	 */
	@Test
	void withinPackageNarrowsToThatFolder() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", SELF_SOURCE);
		AstSourceFiles.write(directory, "q/B.java", "package q;\nclass B {\nvoid b() { }\n}\n");
		assertThatThrownBy(() -> checking(directory).withinPackage("p").hasNoRecursion())
				.isInstanceOf(AssertionError.class).hasMessageContaining("p.A.a()");
		assertThatCode(() -> checking(directory).withinPackage("q").hasNoRecursion()).doesNotThrowAnyException();
	}

	/**
	 * The source folder named in the pom is the one that is checked.
	 */
	@Test
	void projectSourcesComeFromTheBuildFile() throws IOException {
		Path sources = directory.resolve("my-sources");
		AstSourceFiles.write(sources, "p/A.java", SELF_SOURCE);
		Path pom = AstSourceFiles.writePomWithSourceDirectory(directory, sources);
		AresConfiguration.setPomXmlPath(pom.toString());
		AresConfiguration.setBuildGradlePath(directory.resolve("absent.gradle").toString());
		assertThatThrownBy(() -> UnwantedRecursionAssert.assertThatProjectSources()
				.withLanguageLevel(LanguageLevel.JAVA_17).hasNoRecursion()).isInstanceOf(AssertionError.class)
						.hasMessageContaining("p.A.a()");
	}

	/**
	 * Without any build file the check says so clearly.
	 */
	@Test
	void failsClearlyWithoutABuildFile() {
		AresConfiguration.setPomXmlPath(directory.resolve("absent.xml").toString());
		AresConfiguration.setBuildGradlePath(directory.resolve("absent.gradle").toString());
		assertThatThrownBy(UnwantedRecursionAssert::assertThatProjectSources).isInstanceOf(AssertionError.class)
				.hasMessageStartingWith("Could not find project sources folder.");
	}
}
