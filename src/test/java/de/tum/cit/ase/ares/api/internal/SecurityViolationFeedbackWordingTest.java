package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Pins the feedback texts the advisor agreed, read straight from the message
 * files, so they hold whatever the machine's language is.
 */
class SecurityViolationFeedbackWordingTest {

	/** The English prefix of a blocked action. */
	private static final String EN = "Ares Security Error (Reason: Student-Code; Stage: Execution): ";

	/** The German prefix of a blocked action. */
	private static final String DE = "Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausführung): ";

	/** The English texts for a blocked read of /etc/passwd. */
	@Test
	void theEnglishTextsAreTheAgreedOnes() throws IOException {
		Properties en = load("messages.properties");
		assertThat(en.getProperty("reporting.security.violation.medium")
				.formatted(en.getProperty("reporting.security.violation.phrase.file.read")))
						.isEqualTo(EN + "Supervised code tried to illegally read a File, but was blocked by Ares.");
		assertThat(en.getProperty("reporting.security.violation.low"))
				.isEqualTo(EN + "Supervised code tried to illegally violate a policy, but was blocked by Ares.");
		assertThat(en.getProperty("reporting.security.violation.hint").formatted(
				en.getProperty("reporting.security.violation.permission.read"), "/etc/passwd",
				en.getProperty("reporting.security.violation.policy.named").formatted("SecurityPolicy1.yml")))
						.isEqualTo("Allow it by granting the read permission on /etc/passwd in the security policy "
								+ "SecurityPolicy1.yml as:");
	}

	/** The German texts for the same read. */
	@Test
	void theGermanTextsMatchTheEnglishOnes() throws IOException {
		Properties de = load("messages_de.properties");
		assertThat(de.getProperty("reporting.security.violation.medium")
				.formatted(de.getProperty("reporting.security.violation.phrase.file.read")))
						.isEqualTo(DE + "Überwachter Code hat unerlaubt versucht, eine Datei zu lesen, wurde aber von "
								+ "Ares blockiert.");
		assertThat(de.getProperty("reporting.security.violation.low"))
				.isEqualTo(DE + "Überwachter Code hat unerlaubt versucht, eine Sicherheitsrichtlinie zu "
						+ "verletzen, wurde aber von Ares blockiert.");
		assertThat(de.getProperty("reporting.security.violation.hint").formatted(
				de.getProperty("reporting.security.violation.permission.read"), "/etc/passwd",
				de.getProperty("reporting.security.violation.policy.named").formatted("SecurityPolicy1.yml")))
						.isEqualTo("Erlauben Sie dies, indem Sie die Berechtigung Lesen auf /etc/passwd in der "
								+ "Sicherheitsrichtlinie SecurityPolicy1.yml wie folgt erteilen:");
	}

	/**
	 * Reads one message file.
	 *
	 * @param name the file's name.
	 * @return its entries.
	 * @throws IOException if the file cannot be read
	 */
	private static Properties load(String name) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = SecurityViolationFeedbackWordingTest.class
				.getResourceAsStream("/de/tum/cit/ase/ares/api/localization/" + name)) {
			properties.load(in);
		}
		return properties;
	}
}
