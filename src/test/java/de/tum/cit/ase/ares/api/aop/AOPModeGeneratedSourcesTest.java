package de.tum.cit.ase.ares.api.aop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Generates the enforcement sources each AOP mode copies into a supervised
 * project, through the real copy and format steps, and checks that the copied
 * base class still carries the helpers that word violation messages. Nothing
 * here is mocked, so a copy step that mangled those sources would fail.
 */
class AOPModeGeneratedSourcesTest {

	/** The package the generated sources are moved into. */
	private static final String TARGET_PACKAGE = "de.example";

	/**
	 * Copies and formats every source of a mode and checks the copied base class.
	 *
	 * @param mode     the AOP mode to generate
	 * @param baseFile the file name of the copied base class holding the helpers
	 * @param tempDir  a fresh directory standing in for the supervised project
	 * @throws IOException if a generated file cannot be read
	 */
	@ParameterizedTest
	@CsvSource({ "ASPECTJ, JavaAspectJAbstractAdviceDefinitions.aj",
			"INSTRUMENTATION, JavaInstrumentationAdviceAbstractToolbox.java" })
	void copiedBaseClassKeepsTheMessageHelpers(AOPMode mode, String baseFile, @TempDir Path tempDir)
			throws IOException {
		AOPMode.setFileLoader(new JavaCSVFileLoader());
		Path root = tempDir.resolve("de").resolve("example");
		List<Path> generated = new ArrayList<>(FileTools.copyAndFormatFSFiles(mode.fsFilesToCopy(),
				mode.fsTargetsToCopyTo(root), mode.fsFormatValues(TARGET_PACKAGE, "Main")));
		generated.addAll(FileTools.copyAndFormatNonFSFiles(mode.nonFSFilesToCopy(), mode.nonFSTargetsToCopyTo(root),
				mode.placeholderValues(), mode.nonFSFormatValues(TARGET_PACKAGE, "Main")));
		Path base = generated.stream().filter(path -> path.getFileName().toString().equals(baseFile)).findFirst()
				.orElseThrow(() -> new AssertionError(baseFile + " was not generated: " + generated));
		String content = Files.readString(base, StandardCharsets.UTF_8);
		assertTrue(
				content.contains("static String localizeAction(") && content.contains("static String describeCaller("),
				"the copied base class lost the message helpers");
		assertTrue(content.contains("package " + TARGET_PACKAGE + "."),
				"the copied base class was not moved into " + TARGET_PACKAGE);
	}
}
