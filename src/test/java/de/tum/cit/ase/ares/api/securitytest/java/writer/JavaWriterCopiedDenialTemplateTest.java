package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

/**
 * Checks that precompile carries the one denial sentence into the written
 * exercise, for ArchUnit and WALA output with either runtime backend: both
 * copied catalogues hold the file denial template exactly as Ares's own, with
 * the reason as its last value, and the copied file advice uses that key. That
 * the copied advice compiles and prints the sentence is shown by
 * {@code JceGeneratedExerciseTest}, which builds and runs such an exercise.
 */
class JavaWriterCopiedDenialTemplateTest {

	/** The key of the denial sentence for a file. */
	private static final String FILE_DENIAL_KEY = "security.advice.illegal.file.execution";

	/** Where Ares keeps its own catalogues on the class path. */
	private static final String CATALOGUE_FOLDER = "/de/tum/cit/ase/ares/api/localization/";

	/** The package of the written exercise. */
	private static final String EXERCISE_PACKAGE = "exercise.security";

	/** The project the exercise is written into, fresh for each case. */
	@TempDir
	Path project;

	/**
	 * Lists every mode combination precompile can write.
	 *
	 * @return the architecture and runtime modes
	 */
	static Stream<Arguments> modes() {
		return Stream.of(Arguments.of(ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ),
				Arguments.of(ArchitectureMode.ARCHUNIT, AOPMode.INSTRUMENTATION),
				Arguments.of(ArchitectureMode.WALA, AOPMode.ASPECTJ),
				Arguments.of(ArchitectureMode.WALA, AOPMode.INSTRUMENTATION));
	}

	/**
	 * Writes an exercise and compares its catalogues and its file advice with
	 * Ares's own.
	 *
	 * @param architectureMode the static analysis written
	 * @param aopMode          the runtime backend written
	 * @throws IOException if a file cannot be written or read
	 */
	@ParameterizedTest
	@MethodSource("modes")
	void copiesTheDenialTemplateAndTheAdviceUsingIt(ArchitectureMode architectureMode, AOPMode aopMode)
			throws IOException {
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Path sourceRoot = Files.createDirectories(project.resolve("src/test/java"));
		new JavaWriter(project).writeTestCases(BuildMode.MAVEN, architectureMode, aopMode, List.of(), List.of(),
				List.of(), EXERCISE_PACKAGE, "Main", List.of(), List.of(), List.of(), sourceRoot);
		Path bundles = project.resolve("src/test/resources/exercise/security/ares/api/localization");
		for (String name : List.of("messages.properties", "messages_de.properties")) {
			String template = load(
					JavaWriterCopiedDenialTemplateTest.class.getResourceAsStream(CATALOGUE_FOLDER + name), name)
							.getProperty(FILE_DENIAL_KEY);
			assertNotNull(template, name);
			assertTrue(template.matches("(?s).*%(5\\$)?s\\.$"), name + ": " + template);
			assertEquals(template, load(Files.newInputStream(bundles.resolve(name)), name).getProperty(FILE_DENIAL_KEY),
					name);
		}
		assertTrue(load(
				JavaWriterCopiedDenialTemplateTest.class.getResourceAsStream(CATALOGUE_FOLDER + "messages.properties"),
				"messages.properties").getProperty(FILE_DENIAL_KEY).contains("because %s"));
		String advice = aopMode == AOPMode.ASPECTJ ? "JavaAspectJFileSystemAdviceDefinitions.aj"
				: "JavaInstrumentationAdviceFileSystemToolbox.java";
		Path copiedAdvice = writtenFile(project, advice);
		assertTrue(Files.readString(copiedAdvice).contains("\"" + FILE_DENIAL_KEY + "\""), copiedAdvice.toString());
	}

	/**
	 * Finds a file the writer wrote below a folder.
	 *
	 * @param folder the folder
	 * @param name   the file's name
	 * @return the file
	 * @throws IOException if the folder cannot be walked
	 */
	private static Path writtenFile(Path folder, String name) throws IOException {
		try (Stream<Path> files = Files.walk(folder)) {
			return files.filter(path -> path.getFileName().toString().equals(name)).findFirst()
					.orElseThrow(() -> new AssertionError(name + " was not written below " + folder));
		}
	}

	/**
	 * Reads a catalogue.
	 *
	 * @param input the catalogue's bytes, closed afterwards
	 * @param name  the catalogue's name, for the failure message
	 * @return the catalogue
	 * @throws IOException if it cannot be read
	 */
	private static Properties load(InputStream input, String name) throws IOException {
		assertNotNull(input, name);
		Properties catalogue = new Properties();
		try (input) {
			catalogue.load(input);
		}
		return catalogue;
	}
}
