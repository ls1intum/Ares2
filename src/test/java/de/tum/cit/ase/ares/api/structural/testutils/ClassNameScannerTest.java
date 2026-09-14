package de.tum.cit.ase.ares.api.structural.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * Tests the typo-detection threshold decisions of
 * {@link ClassNameScanner#isMisspelledWithHighProbability(String, String)}.
 * These guard the branch boundaries (length difference, Damerau distance, and
 * the Jaro-Winkler / Normalized-Levenshtein 0.9 cut-off) so that vendoring the
 * string-similarity algorithms in
 * {@link de.tum.cit.ase.ares.api.util.StringSimilarity} cannot silently shift
 * the decisions.
 * <p>
 * Also tests {@link ClassNameScanner#qualifiedNameWithinFile(TypeDeclaration)},
 * guarding against I-099: {@code walkProjectFileStructure} previously derived a
 * file's single type name from its filename alone, so a member/nested type (or
 * an additional top-level type declared alongside the filename-matching one)
 * could never be discovered.
 */
class ClassNameScannerTest {

	private static TypeDeclaration<?> typeNamed(CompilationUnit compilationUnit, String simpleName) {
		return compilationUnit.findAll(TypeDeclaration.class).stream()
				.filter(type -> type.getNameAsString().equals(simpleName)).findFirst()
				.orElseThrow(() -> new AssertionError("no type named " + simpleName + " in fixture source"));
	}

	@Test
	void topLevelTypeQualifiedNameIsItsOwnSimpleName() {
		var compilationUnit = StaticJavaParser.parse("class Outer {}");
		assertThat(ClassNameScanner.qualifiedNameWithinFile(typeNamed(compilationUnit, "Outer"))).contains("Outer");
	}

	@Test
	void additionalTopLevelTypeInTheSameFileIsItsOwnSimpleName() {
		var compilationUnit = StaticJavaParser.parse("class Outer {} class SecondTopLevelType {}");
		assertThat(ClassNameScanner.qualifiedNameWithinFile(typeNamed(compilationUnit, "SecondTopLevelType")))
				.contains("SecondTopLevelType");
	}

	@Test
	void memberTypeQualifiedNameIsDotSeparated() {
		var compilationUnit = StaticJavaParser.parse("class Outer { static class Inner {} }");
		assertThat(ClassNameScanner.qualifiedNameWithinFile(typeNamed(compilationUnit, "Inner")))
				.contains("Outer.Inner");
	}

	@Test
	void doublyNestedMemberTypeQualifiedNameJoinsEveryEnclosingName() {
		var compilationUnit = StaticJavaParser.parse("class Outer { static class Middle { static class Inner {} } }");
		assertThat(ClassNameScanner.qualifiedNameWithinFile(typeNamed(compilationUnit, "Inner")))
				.contains("Outer.Middle.Inner");
	}

	@Test
	void localClassHasNoQualifiedNameWithinFile() {
		var compilationUnit = StaticJavaParser.parse("class Outer { void method() { class Local {} } }");
		assertThat(ClassNameScanner.qualifiedNameWithinFile(typeNamed(compilationUnit, "Local"))).isEmpty();
	}

	@Test
	void singleEditTyposAreMisspellings() {
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Helper", "Helpr")).isTrue();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Calculator", "Calculatro")).isTrue();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Account", "Acount")).isTrue();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Student", "Studetn")).isTrue();
	}

	@Test
	void distanceTwoButHighSimilarityIsMisspelling() {
		// distance == 2, Jaro-Winkler similarity > 0.9
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("DataBase", "Databse")).isTrue();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("HashMap", "HsahMpa")).isTrue();
	}

	@Test
	void largeLengthDifferenceIsNotAMisspelling() {
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Main", "MainHelperClass")).isFalse();
	}

	@Test
	void distanceAboveTwoIsNotAMisspelling() {
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Pingu", "Penguin")).isFalse();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Apple", "Orange")).isFalse();
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("Cat", "Dog")).isFalse();
	}

	@Test
	void distanceTwoWithLowSimilarityIsNotAMisspelling() {
		// distance == 2, but both Jaro-Winkler and Normalized-Levenshtein <= 0.9
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("abcd", "xbcy")).isFalse();
	}

	@Test
	void shortStringsWithoutSharedCharactersAreNotMisspellings() {
		// distance == 1 but max length <= 2 falls through to the similarity check
		assertThat(ClassNameScanner.isMisspelledWithHighProbability("ab", "ba")).isFalse();
	}
}
