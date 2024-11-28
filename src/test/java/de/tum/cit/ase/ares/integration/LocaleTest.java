package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.LocaleUser;
import de.tum.cit.ase.ares.integration.testuser.LocaleUser.*;
import de.tum.cit.ase.ares.testutilities.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@UserBased({ LocaleUser.class, LocaleEn.class, LocaleUnsupported.class })
class LocaleTest {

	@UserTestResults
	private static Events tests;

	private final String testLocaleEn = "testLocaleEn";
	private final String testLocaleUnsupported = "testLocaleUnsupported";
	private final String testLocaleDe = "testLocaleDe";
	private final String testUnknownFormatted = "testUnknownFormatted";
	private final String testUnknownNormal = "testUnknownNormal";

	private static final String ENGLISH_FILE_PATH = "src/main/resources/de/tum/cit/ase/ares/api/localization/messages.properties";  // Replace with actual path
	private static final String GERMAN_FILE_PATH = "src/main/resources/de/tum/cit/ase/ares/api/localization/messages_de.properties"; // Replace with actual path

	@TestTest
	void test_testLocaleEn() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testLocaleEn));
	}

	@TestTest
	void test_testLocaleUnsupported() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testLocaleUnsupported));
	}

	@TestTest
	void test_testLocaleDe() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testLocaleDe));
	}

	@TestTest
	void test_testUnknownFormatted() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testUnknownFormatted));
	}

	@TestTest
	void test_testUnknownNormal() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(testUnknownNormal));
	}

	@TestTest
	public void testTranslationsCompleteness() throws IOException {
		Set<String> englishKeys = loadPropertiesKeys(ENGLISH_FILE_PATH);
		Set<String> germanKeys = new HashSet<>(loadPropertiesKeys(GERMAN_FILE_PATH));

		assertThat(germanKeys)
				.containsExactlyInAnyOrderElementsOf(englishKeys)
				.withFailMessage("Sets are not equal. Difference: %s", germanKeys.removeAll(englishKeys) ? germanKeys : englishKeys);
	}

	private Set<String> loadPropertiesKeys(String filePath) throws IOException {
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream(filePath)) {
			properties.load(input);
		}
		return properties.stringPropertyNames();
	}

}
