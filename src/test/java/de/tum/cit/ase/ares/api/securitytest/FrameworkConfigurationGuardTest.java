package de.tum.cit.ase.ares.api.securitytest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks that a compiled submission carrying a settings file a test library
 * reads is found, that ordinary resources are not, and that Ares's own build
 * output is not mistaken for a submission.
 */
class FrameworkConfigurationGuardTest {

	/**
	 * The output folder each case fills.
	 */
	@TempDir
	Path output;

	/**
	 * Each settings file at the top of an output is found.
	 *
	 * @param name the settings file
	 * @throws IOException if the file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "archunit.properties", "junit-platform.properties", "logback.xml", "logback-test.xml",
			"logback.groovy" })
	void findsASettingsFile(String name) throws IOException {
		Files.writeString(output.resolve(name), "x");
		assertThat(FrameworkConfigurationGuard.firstSettingsFileIn(output)).contains(output.resolve(name));
	}

	/**
	 * Any service file is found, whichever service it names.
	 *
	 * @param service the service the file names
	 * @throws IOException if the file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "org.junit.platform.launcher.TestExecutionListener",
			"com.tngtech.archunit.lang.extension.ArchUnitExtension", "java.nio.file.spi.FileSystemProvider" })
	void findsAServiceFile(String service) throws IOException {
		Path services = Files.createDirectories(output.resolve("META-INF/services"));
		Files.writeString(services.resolve(service), "org.example.Evil");
		assertThat(FrameworkConfigurationGuard.firstSettingsFileIn(output)).contains(services.resolve(service));
	}

	/**
	 * A file standing where the service folder belongs is found too.
	 *
	 * @throws IOException if the file cannot be written
	 */
	@Test
	void findsAFileInPlaceOfTheServiceFolder() throws IOException {
		Files.createDirectories(output.resolve("META-INF"));
		Files.writeString(output.resolve("META-INF/services"), "x");
		assertThat(FrameworkConfigurationGuard.firstSettingsFileIn(output))
				.contains(output.resolve("META-INF/services"));
	}

	/**
	 * Ordinary resources, a settings file below the top and other files in META-INF
	 * are left alone.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void leavesOrdinaryResourcesAlone() throws IOException {
		Files.writeString(output.resolve("allowed.txt"), "x");
		Files.createDirectories(output.resolve("data"));
		Files.writeString(output.resolve("data/archunit.properties"), "x");
		Files.createDirectories(output.resolve("META-INF"));
		Files.writeString(output.resolve("META-INF/MANIFEST.MF"), "x");
		Files.writeString(output.resolve("META-INF/other.txt"), "x");
		assertThat(FrameworkConfigurationGuard.firstSettingsFileIn(output)).isEmpty();
	}

	/**
	 * Ares's own output carries archunit.properties and logback.xml, and the check
	 * still passes because that output is where Ares was loaded from.
	 */
	@Test
	void passesForAresItself() {
		assertThat(Path.of("target/classes/archunit.properties")).exists();
		assertThatCode(FrameworkConfigurationGuard::requireNone).doesNotThrowAnyException();
	}
}
