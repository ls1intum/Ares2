package de.tum.cit.ase.ares.testutilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;

import example.student.JceAdviceProbe;

/** Runs the same path and denial contract against both actual backends. */
public abstract class JceFileSystemContract {

	/** The caller's locale restored after each test. */
	private Locale originalLocale;

	/** Supplies the real backend selected by the concrete test class. */
	protected abstract AOPMode jceMode();

	/** Clears policy state without warming or replacing an enforcement class. */
	@BeforeEach
	void rememberJceLocale() {
		originalLocale = Locale.getDefault();
		AOPMode.ASPECTJ.reset();
	}

	/** Restores settings and locale even when a denial assertion failed. */
	@AfterEach
	void restoreJceState() {
		AOPMode.ASPECTJ.reset();
		Locale.setDefault(originalLocale);
	}

	/** Trusted JDK policy paths retain the existing read allowance. */
	@Test
	void readAllowsTrustedJdkPolicyFile() {
		configure(null);
		Path policy = Path.of(System.getProperty("java.home"), "conf/security/policy/unlimited/default_local.policy");
		assertDoesNotThrow(() -> probe("read", "parameter", policy, null));
	}

	/** Exact JCE names and a lookalike never authorise unlisted parameters. */
	@ParameterizedTest
	@ValueSource(strings = { "default_local.policy", "default_US_export.policy", "exempt_local.policy",
			"default_tokens.policy" })
	void readRejectsUnlistedPolicyNamedFile(String name, @TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve(name), "protected");
		configure(directory.resolve("allowed"));
		requireDenied("read", "parameter", file, null);
	}

	/** The JCE glob is a filter when the directory itself is permitted. */
	@Test
	void directoryGlobDoesNotBecomePath(@TempDir Path directory) {
		configure(directory);
		assertDoesNotThrow(() -> probe("read", "glob", directory, null));
	}

	/** An exact JCE glob cannot grant access to another directory. */
	@Test
	void directoryGlobDoesNotPermitForbiddenDirectory(@TempDir Path directory) {
		configure(directory.resolve("allowed"));
		requireDenied("read", "glob", directory, null);
	}

	/** Parameter conversion remains authoritative for an unlisted policy file. */
	@Test
	void parameterReadRejectsUnlistedPolicyNamedFile(@TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve("default_local.policy"), "protected");
		configure(null);
		requireDenied("read", "parameter", file, null);
	}

	/** A real File receiver cannot bypass its path check. */
	@Test
	void receiverReadRejectsUnlistedPolicyNamedFile(@TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve("default_local.policy"), "protected");
		configure(null);
		requireDenied("read", "receiver", file, file.toFile());
	}

	/** The path field of a real FileInputStream remains subject to the policy. */
	@Test
	void attributeReadRejectsUnlistedPolicyNamedFile(@TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve("default_local.policy"), "protected");
		try (var stream = new FileInputStream(file.toFile())) {
			configure(null);
			requireDenied("read", "attribute", file, stream);
		}
	}

	/** Names cannot grant creation, overwrite or deletion through advice. */
	@ParameterizedTest
	@ValueSource(strings = { "create", "overwrite", "delete" })
	void cryptoPolicyNamesDoNotPermitCreateOverwriteOrDelete(String action, @TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve("default_local.policy"), "protected");
		configure(null);
		requireDenied(action, "parameter", file, null);
	}

	/** Explicit permission still allows a policy-named file. */
	@Test
	void explicitlyPermittedPolicyNamedFileRemainsReadable(@TempDir Path directory) throws Exception {
		Path file = Files.writeString(directory.resolve("default_local.policy"), "permitted");
		configure(file);
		assertDoesNotThrow(() -> probe("read", "parameter", file, null));
	}

	/**
	 * Both denial reasons use the selected language and leave the guard reusable.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "en", "de" })
	void denialMessagesUseSelectedLocale(String language, @TempDir Path directory) throws Exception {
		Locale.setDefault(Locale.forLanguageTag(language));
		Path file = Files.writeString(directory.resolve("default_local.policy"), "protected");
		for (boolean empty : new boolean[] { true, false }) {
			String key = empty ? "security.advice.denial.reason.no.allowlist"
					: "security.advice.denial.reason.not.in.allowlist";
			String reason = Messages.localized(key);
			configure(empty ? null : directory.resolve("unrelated"));
			SecurityException denied = requireDenied("read", "parameter", file, null);
			assertTrue(denied.getMessage().contains(reason), denied.getMessage());
			configure(file);
			assertDoesNotThrow(() -> probe("read", "parameter", file, null));
		}
	}

	/** Publishes the real policy values read by the selected advice. */
	private void configure(Path allowed) {
		AOPMode.ASPECTJ.reset();
		for (String name : new String[] { "aopMode", "restrictedPackage", "allowedListedClasses",
				"pathsAllowedToBeRead" }) {
			Object value = switch (name) {
			case "aopMode" -> jceMode().name();
			case "restrictedPackage" -> "example.student";
			case "allowedListedClasses" -> new String[0];
			default -> allowed == null ? new String[0] : new String[] { allowed.toString() };
			};
			JavaAOPTestCase.setJavaAdviceSettingValue(name, value, "ARCHUNIT", jceMode().name());
		}
	}

	/** Calls the real backend from the supervised probe. */
	private void probe(String action, String representation, Path file, Object instance) {
		JceAdviceProbe.check(jceMode(), action, representation, file, instance);
	}

	/** Requires a file-specific runtime denial instead of an unrelated failure. */
	private SecurityException requireDenied(String action, String representation, Path file, Object instance) {
		SecurityException denied = assertThrows(SecurityException.class,
				() -> probe(action, representation, file, instance));
		assertTrue(denied.getMessage().contains(file.toAbsolutePath().toString()), denied.getMessage());
		return denied;
	}
}
