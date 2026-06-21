package de.tum.cit.ase.ares.api.structural.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests the typo-detection threshold decisions of
 * {@link ClassNameScanner#isMisspelledWithHighProbability(String, String)}.
 * These guard the branch boundaries (length difference, Damerau distance, and
 * the Jaro-Winkler / Normalized-Levenshtein 0.9 cut-off) so that vendoring the
 * string-similarity algorithms in
 * {@link de.tum.cit.ase.ares.api.util.StringSimilarity} cannot silently shift
 * the decisions.
 */
class ClassNameScannerTest {

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
