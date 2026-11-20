package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * How Ares reads the source-root report that {@code GradleSourceRoots.gradle}
 * hands a test run: which reports it takes, and which it refuses.
 */
class GradleReportedSourceRootsTest {

	/** The project the reports are read for. */
	@TempDir
	Path temporaryDirectory;

	/** The canonical project root. */
	private Path root;

	/**
	 * Canonicalises the project root, as {@code ProjectSourcesFinder} does.
	 */
	@BeforeEach
	void canonicaliseTheRoot() {
		root = BuildToolConfiguration.canonicalise(temporaryDirectory);
	}

	/**
	 * A value as the snippet writes it.
	 *
	 * @param value the plain value
	 * @return the percent-encoded value
	 */
	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	/**
	 * A complete valid report for this project, with test folder {@code checks} and
	 * main folder {@code assignment}.
	 *
	 * @return the report as start-up values
	 */
	private Map<String, String> validReport() {
		Map<String, String> values = new HashMap<>();
		values.put("ares.gradle.sourceRoots.version", "1");
		values.put("ares.gradle.projectDir", encode(root.toString()));
		values.put("ares.gradle.testSet", "test");
		values.put("ares.gradle.testSourceRoots", encode(root.resolve("checks").toString()));
		values.put("ares.gradle.testOutputRoot", encode(root.resolve("build/classes/java/test").toString()));
		values.put("ares.gradle.mainSourceRoots", encode(root.resolve("assignment").toString()));
		values.put("ares.gradle.mainOutputRoot", encode(root.resolve("build/classes/java/main").toString()));
		return values;
	}

	/**
	 * Without any report the outcome is absent, and a run without a policy is
	 * refused with the snippet to apply.
	 */
	@Test
	void refusesARunWithoutAReport() {
		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(Map.of("java.version", "17"), root);

		assertEquals(GradleReportedSourceRoots.Outcome.ABSENT, report.outcome());
		SecurityException refusal = assertThrows(SecurityException.class, () -> report.requireValid(root));
		assertTrue(refusal.getMessage().contains("GradleSourceRoots.gradle"), refusal.getMessage());
	}

	/**
	 * A valid report gives exactly the folders Gradle reported that exist, the
	 * outputs as reported, and main as complete.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@Test
	void takesTheReportedFolders() throws IOException {
		Files.createDirectories(root.resolve("checks"));
		Files.createDirectories(root.resolve("assignment"));
		Map<String, String> values = validReport();
		values.put("ares.gradle.testSourceRoots",
				encode(root.resolve("checks").toString()) + "," + encode(root.resolve("src/test/java").toString()));

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);
		BuildToolConfiguration configuration = report.toConfiguration(root);

		assertTrue(report.isValid());
		assertEquals(List.of(root.resolve("checks")), configuration.testSourceRoots());
		assertEquals(List.of(root.resolve("assignment")), configuration.productionSourceRoots());
		assertEquals(root.resolve("build/classes/java/test"), configuration.testOutputRoot());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * A folder whose name holds a comma, a plus, a percent sign, a space and a
	 * non-ASCII letter stays one folder, since the list is split before it is
	 * decoded.
	 *
	 * @throws IOException if the folder cannot be created
	 */
	@Test
	void keepsSpecialCharactersInsideOneFolder() throws IOException {
		Path odd = Files.createDirectories(root.resolve("odd dir+%,\u00e4"));
		Map<String, String> values = validReport();
		values.put("ares.gradle.testSourceRoots", encode(odd.toString()) + "," + encode(odd.toString()));

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

		assertEquals(List.of(odd, odd), report.testSources());
	}

	/**
	 * A colon, which Unix allows in a folder name, stays inside it too.
	 *
	 * @throws IOException if the folder cannot be created
	 */
	@Test
	@DisabledOnOs(OS.WINDOWS)
	void keepsAColonInsideOneFolder() throws IOException {
		Path odd = Files.createDirectories(root.resolve("x:y"));
		Map<String, String> values = validReport();
		values.put("ares.gradle.testSourceRoots", encode(odd.toString()));

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

		assertEquals(List.of(odd), report.testSources());
	}

	/**
	 * An empty list is a list of no folders, not a malformed report.
	 */
	@Test
	void readsAnEmptyListAsNoFolders() {
		Map<String, String> values = validReport();
		values.put("ares.gradle.testSourceRoots", "");

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

		assertTrue(report.isValid());
		assertEquals(List.of(), report.testSources());
	}

	/**
	 * A problem Gradle reports is refused with its reason, decoded.
	 */
	@Test
	void refusesTheProblemGradleReports() {
		Map<String, String> values = new HashMap<>(
				Map.of("ares.gradle.sourceRoots.version", "1", "ares.gradle.projectDir", encode(root.toString()),
						"ares.gradle.sourceRoots.problem", encode("main keeps classes outside its Java output")));

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

		assertEquals(GradleReportedSourceRoots.Outcome.PROBLEM, report.outcome());
		SecurityException refusal = assertThrows(SecurityException.class, () -> report.requireValid(root));
		assertTrue(refusal.getMessage().contains("main keeps classes outside its Java output"), refusal.getMessage());
	}

	/**
	 * A report for another project never stands for this one.
	 *
	 * @throws IOException if the other project cannot be created
	 */
	@Test
	void refusesTheReportOfAnotherProject() throws IOException {
		Path other = Files.createDirectories(root.resolve("other"));
		Map<String, String> values = validReport();
		values.put("ares.gradle.projectDir", encode(other.toString()));

		GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

		assertEquals(GradleReportedSourceRoots.Outcome.FOREIGN, report.outcome());
		assertFalse(report.isValid());
	}

	/**
	 * An unknown version, a missing value, a relative path, a broken escape, a
	 * report without its version, bytes that are not UTF-8, a value that was never
	 * encoded and an escape in non-ASCII digits are each malformed and refused.
	 */
	@Test
	void refusesMalformedReports() {
		Map<String, String> unknownVersion = validReport();
		unknownVersion.put("ares.gradle.sourceRoots.version", "2");
		Map<String, String> missingValue = validReport();
		missingValue.remove("ares.gradle.testSet");
		Map<String, String> relativePath = validReport();
		relativePath.put("ares.gradle.testSourceRoots", "checks");
		Map<String, String> brokenEscape = validReport();
		brokenEscape.put("ares.gradle.mainSourceRoots", "%zz");
		Map<String, String> withoutVersion = validReport();
		withoutVersion.remove("ares.gradle.sourceRoots.version");
		Map<String, String> brokenUtf8InAList = validReport();
		brokenUtf8InAList.put("ares.gradle.mainSourceRoots", encode(root.toString()) + "%2Fa%FF");
		Map<String, String> brokenUtf8InAValue = validReport();
		brokenUtf8InAValue.put("ares.gradle.testOutputRoot", encode(root.toString()) + "%2F%C3");
		Map<String, String> notEncoded = validReport();
		notEncoded.put("ares.gradle.testSet", "t\u00e4st");
		Map<String, String> wideDigits = validReport();
		wideDigits.put("ares.gradle.testSet", "%\uff14\uff11");

		for (Map<String, String> values : List.of(unknownVersion, missingValue, relativePath, brokenEscape,
				withoutVersion, brokenUtf8InAList, brokenUtf8InAValue, notEncoded, wideDigits)) {
			GradleReportedSourceRoots.Report report = GradleReportedSourceRoots.parse(values, root);

			assertEquals(GradleReportedSourceRoots.Outcome.MALFORMED, report.outcome(), values.toString());
			assertThrows(SecurityException.class, () -> report.requireValid(root));
		}
	}
}
