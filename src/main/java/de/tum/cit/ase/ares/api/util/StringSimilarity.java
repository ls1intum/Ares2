/*
 * Portions of this file are derived from the "java-string-similarity" library
 * by Thibault Debatty (https://github.com/tdebatty/java-string-similarity),
 * specifically the Levenshtein, NormalizedLevenshtein, Damerau and JaroWinkler
 * algorithms. That library is distributed under the MIT License: The MIT
 * License Copyright 2015 Thibault Debatty. Permission is hereby granted, free
 * of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions: The above copyright notice and this permission
 * notice shall be included in all copies or substantial portions of the
 * Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package de.tum.cit.ase.ares.api.util;

import java.util.Arrays;
import java.util.HashMap;

/**
 * In-house string-similarity metrics vendored from the MIT-licensed <a href=
 * "https://github.com/tdebatty/java-string-similarity">java-string-similarity</a>
 * library by Thibault Debatty.
 * <p>
 * The algorithm bodies are reproduced verbatim (only adapted to static methods)
 * so that their numeric results are identical to the original library, on which
 * {@link de.tum.cit.ase.ares.api.structural.testutils.ClassNameScanner} relies
 * for its typo-detection thresholds. Replacing this with a "standard"
 * reimplementation would shift results around those thresholds.
 */
public final class StringSimilarity {

	private static final double JARO_WINKLER_THRESHOLD = 0.7;
	private static final int THREE = 3;
	private static final double JW_COEF = 0.1;

	private StringSimilarity() {
		throw new UnsupportedOperationException("StringSimilarity is a utility class and should not be instantiated");
	}

	/**
	 * Jaro-Winkler similarity with the library default threshold (0.7).
	 *
	 * @param s1 the first string to compare.
	 * @param s2 the second string to compare.
	 * @return the Jaro-Winkler similarity in the range [0, 1].
	 */
	public static double jaroWinklerSimilarity(String s1, String s2) {
		if (s1 == null) {
			throw new NullPointerException("s1 must not be null");
		}
		if (s2 == null) {
			throw new NullPointerException("s2 must not be null");
		}
		if (s1.equals(s2)) {
			return 1;
		}
		int[] mtp = jaroWinklerMatches(s1, s2);
		float m = mtp[0];
		if (m == 0) {
			return 0f;
		}
		double j = ((m / s1.length() + m / s2.length() + (m - mtp[1]) / m)) / THREE;
		double jw = j;
		if (j > JARO_WINKLER_THRESHOLD) {
			jw = j + Math.min(JW_COEF, 1.0 / mtp[THREE]) * mtp[2] * (1 - j);
		}
		return jw;
	}

	private static int[] jaroWinklerMatches(String s1, String s2) {
		String max;
		String min;
		if (s1.length() > s2.length()) {
			max = s1;
			min = s2;
		} else {
			max = s2;
			min = s1;
		}
		int range = Math.max(max.length() / 2 - 1, 0);
		int[] matchIndexes = new int[min.length()];
		Arrays.fill(matchIndexes, -1);
		boolean[] matchFlags = new boolean[max.length()];
		int matches = 0;
		for (int mi = 0; mi < min.length(); mi++) {
			char c1 = min.charAt(mi);
			for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
				if (!matchFlags[xi] && c1 == max.charAt(xi)) {
					matchIndexes[mi] = xi;
					matchFlags[xi] = true;
					matches++;
					break;
				}
			}
		}
		char[] ms1 = new char[matches];
		char[] ms2 = new char[matches];
		for (int i = 0, si = 0; i < min.length(); i++) {
			if (matchIndexes[i] != -1) {
				ms1[si] = min.charAt(i);
				si++;
			}
		}
		for (int i = 0, si = 0; i < max.length(); i++) {
			if (matchFlags[i]) {
				ms2[si] = max.charAt(i);
				si++;
			}
		}
		int transpositions = 0;
		for (int mi = 0; mi < ms1.length; mi++) {
			if (ms1[mi] != ms2[mi]) {
				transpositions++;
			}
		}
		int prefix = 0;
		for (int mi = 0; mi < min.length(); mi++) {
			if (s1.charAt(mi) == s2.charAt(mi)) {
				prefix++;
			} else {
				break;
			}
		}
		return new int[] { matches, transpositions / 2, prefix, max.length() };
	}

	/**
	 * Normalized Levenshtein similarity, computed as
	 * {@code 1 - levenshtein(s1, s2) / max(|s1|, |s2|)}.
	 *
	 * @param s1 the first string to compare.
	 * @param s2 the second string to compare.
	 * @return the similarity in the range [0, 1].
	 */
	public static double normalizedLevenshteinSimilarity(String s1, String s2) {
		return 1.0 - normalizedLevenshteinDistance(s1, s2);
	}

	private static double normalizedLevenshteinDistance(String s1, String s2) {
		if (s1 == null) {
			throw new NullPointerException("s1 must not be null");
		}
		if (s2 == null) {
			throw new NullPointerException("s2 must not be null");
		}
		if (s1.equals(s2)) {
			return 0;
		}
		int mLen = Math.max(s1.length(), s2.length());
		if (mLen == 0) {
			return 0;
		}
		return levenshteinDistance(s1, s2) / mLen;
	}

	private static double levenshteinDistance(String s1, String s2) {
		if (s1 == null) {
			throw new NullPointerException("s1 must not be null");
		}
		if (s2 == null) {
			throw new NullPointerException("s2 must not be null");
		}
		if (s1.equals(s2)) {
			return 0;
		}
		if (s1.length() == 0) {
			return s2.length();
		}
		if (s2.length() == 0) {
			return s1.length();
		}
		int[] v0 = new int[s2.length() + 1];
		int[] v1 = new int[s2.length() + 1];
		int[] vtemp;
		for (int i = 0; i < v0.length; i++) {
			v0[i] = i;
		}
		for (int i = 0; i < s1.length(); i++) {
			v1[0] = i + 1;
			for (int j = 0; j < s2.length(); j++) {
				int cost = 1;
				if (s1.charAt(i) == s2.charAt(j)) {
					cost = 0;
				}
				v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
			}
			vtemp = v0;
			v0 = v1;
			v1 = vtemp;
		}
		return v0[s2.length()];
	}

	/**
	 * Unrestricted Damerau-Levenshtein distance (with transposition of two adjacent
	 * characters), not the optimal-string-alignment variant.
	 *
	 * @param s1 the first string to compare.
	 * @param s2 the second string to compare.
	 * @return the computed distance.
	 */
	public static double damerauLevenshteinDistance(String s1, String s2) {
		if (s1 == null) {
			throw new NullPointerException("s1 must not be null");
		}
		if (s2 == null) {
			throw new NullPointerException("s2 must not be null");
		}
		if (s1.equals(s2)) {
			return 0;
		}
		int inf = s1.length() + s2.length();
		HashMap<Character, Integer> da = new HashMap<>();
		for (int d = 0; d < s1.length(); d++) {
			da.put(s1.charAt(d), 0);
		}
		for (int d = 0; d < s2.length(); d++) {
			da.put(s2.charAt(d), 0);
		}
		int[][] h = new int[s1.length() + 2][s2.length() + 2];
		for (int i = 0; i <= s1.length(); i++) {
			h[i + 1][0] = inf;
			h[i + 1][1] = i;
		}
		for (int j = 0; j <= s2.length(); j++) {
			h[0][j + 1] = inf;
			h[1][j + 1] = j;
		}
		for (int i = 1; i <= s1.length(); i++) {
			int db = 0;
			for (int j = 1; j <= s2.length(); j++) {
				int i1 = da.get(s2.charAt(j - 1));
				int j1 = db;
				int cost = 1;
				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					cost = 0;
					db = j;
				}
				h[i + 1][j + 1] = min4(h[i][j] + cost, h[i + 1][j] + 1, h[i][j + 1] + 1,
						h[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
			}
			da.put(s1.charAt(i - 1), i);
		}
		return h[s1.length() + 1][s2.length() + 1];
	}

	private static int min4(int a, int b, int c, int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}
}
