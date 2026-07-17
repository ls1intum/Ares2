package de.tum.cit.ase.ares.api.io;

import java.net.URL;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import de.tum.cit.ase.ares.api.structural.StructuralTestProvider;

public final class NonUtf8DefaultCharsetVerifier extends StructuralTestProvider {

	private NonUtf8DefaultCharsetVerifier() {
	}

	public static void main(String[] arguments) throws Exception {
		if (!"tr-TR".equals(Locale.getDefault().toLanguageTag())) {
			throw new AssertionError("Verifier did not start under the requested Turkish default locale");
		}
		IOTester tester = IOTester.installNew(false, 1_000);
		try {
			System.out.println("Grüße €");
			if (!tester.out().getLinesAsString().equals(java.util.List.of("Grüße €"))) {
				throw new AssertionError("IOTester did not preserve UTF-8 output");
			}
		} finally {
			IOTester.uninstallCurrent();
		}
		URL oracle = NonUtf8DefaultCharsetVerifier.class.getResource("non-utf8-default-oracle.json");
		JsonNode parsed = retrieveStructureOracleJSON(oracle);
		if (!"Grüße €".equals(parsed.get(0).get("label").asText())) {
			throw new AssertionError("Structural oracle was not decoded as UTF-8");
		}
	}
}
