package de.tum.cit.ase.ares.api.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/**
 * Golden-master tests for {@link StringSimilarity}. The expected values were
 * captured from the MIT-licensed info.debatty java-string-similarity library
 * (proven bit-identical by the former parity test) before that dependency was
 * removed. They guard against accidental drift in the vendored algorithms.
 */
class StringSimilarityTest {

	private static final double TOLERANCE = 1e-9;

	@Test
	void damerauLevenshteinDistanceMatchesGoldenValues() {
		assertThat(StringSimilarity.damerauLevenshteinDistance("Main", "Main")).isEqualTo(0.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("MainClass", "MianClass")).isEqualTo(1.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("Student", "Studetn")).isEqualTo(1.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("abcdefgh", "abcdefhg")).isEqualTo(1.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("Pingu", "Penguin")).isEqualTo(3.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("ABCDEF", "abcdef")).isEqualTo(6.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("Main", "")).isEqualTo(4.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("abcd", "xbcy")).isEqualTo(2.0);
	}

	@Test
	void jaroWinklerSimilarityMatchesGoldenValues() {
		assertThat(StringSimilarity.jaroWinklerSimilarity("Main", "Main")).isEqualTo(1.0);
		assertThat(StringSimilarity.jaroWinklerSimilarity("MainClass", "MianClass")).isCloseTo(0.9666666328907013,
				within(TOLERANCE));
		assertThat(StringSimilarity.jaroWinklerSimilarity("Calculator", "Calcualtor")).isCloseTo(0.9833333492279053,
				within(TOLERANCE));
		assertThat(StringSimilarity.jaroWinklerSimilarity("Pingu", "Penguin")).isCloseTo(0.8114285290241241,
				within(TOLERANCE));
		assertThat(StringSimilarity.jaroWinklerSimilarity("café", "cafe")).isCloseTo(0.8833333194255829,
				within(TOLERANCE));
		assertThat(StringSimilarity.jaroWinklerSimilarity("ABCDEF", "abcdef")).isEqualTo(0.0);
		assertThat(StringSimilarity.jaroWinklerSimilarity("HelperUtil", "HelperUtils")).isCloseTo(0.9972451816905629,
				within(TOLERANCE));
	}

	@Test
	void normalizedLevenshteinSimilarityMatchesGoldenValues() {
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("Main", "Main")).isEqualTo(1.0);
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("MainClass", "MianClass"))
				.isCloseTo(0.7777777777777778, within(TOLERANCE));
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("Helper", "Helpr")).isCloseTo(0.8333333333333334,
				within(TOLERANCE));
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("Pingu", "Penguin")).isCloseTo(0.5714285714285714,
				within(TOLERANCE));
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("ABCDEF", "abcdef")).isEqualTo(0.0);
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("HelperUtil", "HelperUtils"))
				.isCloseTo(0.9090909090909091, within(TOLERANCE));
	}

	@Test
	void equalStringsAreFullySimilar() {
		assertThat(StringSimilarity.jaroWinklerSimilarity("x", "x")).isEqualTo(1.0);
		assertThat(StringSimilarity.normalizedLevenshteinSimilarity("x", "x")).isEqualTo(1.0);
		assertThat(StringSimilarity.damerauLevenshteinDistance("x", "x")).isEqualTo(0.0);
	}
}
