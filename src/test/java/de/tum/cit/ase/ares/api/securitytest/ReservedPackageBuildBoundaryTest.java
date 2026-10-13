package de.tum.cit.ase.ares.api.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;
import de.tum.cit.ase.ares.api.util.FileTools;

class ReservedPackageBuildBoundaryTest {
	private static final Path ROOT = FileTools.resolveFileOnSourceDirectory("configuration", "reservedPackages");

	/**
	 * The fileset of the Maven snippet, whose includes are the rejected patterns.
	 */
	private static final Pattern MAVEN_FILESET = Pattern.compile("<fileset[^>]*>(.*?)</fileset>", Pattern.DOTALL);

	/** One include of that fileset, capturing its pattern. */
	private static final Pattern MAVEN_INCLUDE = Pattern.compile("<include\\s+name\\s*=\\s*\"([^\"]*)\"\\s*/>");

	/**
	 * An XML comment, which Maven ignores, so a pattern inside one rejects nothing.
	 */
	private static final Pattern XML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

	/** What may separate the entries of a list: whitespace and commas. */
	private static final Pattern SEPARATORS = Pattern.compile("[\\s,]+");

	/** The pattern list of the Gradle snippet. */
	private static final Pattern GRADLE_LIST = Pattern.compile("def aresReservedPackagePatterns = \\[(.*?)\\]",
			Pattern.DOTALL);

	/** One quoted entry of that list, capturing its pattern. */
	private static final Pattern GRADLE_PATTERN = Pattern.compile("'([^']*)'");

	/**
	 * The prefix list, its versions and both build snippets agree with the code.
	 * The snippets must list exactly the reserved patterns, in order, since an
	 * extra pattern refuses student packages Ares does not reserve.
	 */
	@Test
	void versionedMavenAndGradleFixturesRejectEveryReservedPrefix() throws Exception {
		Path patternsFile = ROOT.resolve("ReservedPackagePrefixes.txt");
		List<String> lines = Files.readAllLines(patternsFile);
		assertTrue(lines.contains("# version=" + WalaPathClassification.RESERVED_PACKAGE_PREFIX_VERSION));
		assertTrue(
				lines.contains("# boundary-version=" + WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION));
		List<String> patterns = lines.stream().filter(line -> !line.isBlank() && !line.startsWith("#")).toList();
		List<String> expected = WalaPathClassification.RESERVED_PACKAGE_PREFIXES.stream()
				.map(prefix -> prefix.substring(0, prefix.length() - 1).replace('.', '/') + "/**").toList();
		assertEquals(expected, patterns);

		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		assertEquals(patterns,
				valuesIn(XML_COMMENT.matcher(onlyMatch(MAVEN_FILESET, maven)).replaceAll(""), MAVEN_INCLUDE),
				"Maven fixture must reject exactly the reserved prefixes");
		assertEquals(patterns, valuesIn(onlyMatch(GRADLE_LIST, gradle), GRADLE_PATTERN),
				"Gradle fixture must reject exactly the reserved prefixes");
		for (String pattern : patterns) {
			String syntheticClass = pattern.replace("/**", "/Student.class");
			assertTrue(matches(pattern, syntheticClass), () -> "Fixture does not reject " + syntheticClass);
		}
		assertTrue(maven.contains("No bypass flag is supported"));
		assertTrue(gradle.contains("No bypass flag is supported"));
	}

	@Test
	void gradleFixtureGatesEveryTestTaskAndNotOnlyCheck() throws Exception {
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		String task = "verifyAresReservedPackagesV" + WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION;
		assertTrue(gradle.contains("tasks.register('" + task + "')"), () -> "Gradle fixture does not declare " + task);
		// The defect boundary version 2 exists to fix: check.dependsOn test, not the
		// reverse, so hanging the validation off `check` alone left `gradlew test`
		// - which is what a grading run invokes - completely ungated.
		assertTrue(gradle.contains("tasks.withType(Test).configureEach { dependsOn tasks.named('" + task + "') }"),
				"Gradle fixture must gate every Test task, or `gradlew test` skips the boundary");
		assertTrue(gradle.contains("tasks.named('check') { dependsOn tasks.named('" + task + "') }"),
				"Gradle fixture must still gate `check`, which covers `gradlew build`");
		assertTrue(gradle.contains("import org.gradle.api.tasks.testing.Test"),
				"the snippet is copied into foreign builds, so the Test type must be imported explicitly");
	}

	@Test
	void bothFixturesNameTheSameBoundaryVersion() throws Exception {
		String version = WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION;
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		assertTrue(maven.contains("verify-ares-reserved-packages-v" + version));
		assertTrue(maven.contains("Ares reserved-package validation " + version + " rejected"));
		assertTrue(gradle.contains("def aresReservedPackageBoundaryVersion = '" + version + "'"));
	}

	/**
	 * Returns what the container pattern captures, failing when it matches nowhere
	 * or more than once, so a reshaped snippet cannot pass with an empty list.
	 */
	private static String onlyMatch(Pattern container, String text) {
		Matcher matcher = container.matcher(text);
		assertTrue(matcher.find(), () -> "No match for " + container);
		String region = matcher.group(1);
		assertFalse(matcher.find(), () -> "More than one match for " + container);
		return region;
	}

	/**
	 * Returns every value the pattern captures in the region, in order, failing
	 * when anything but those values and separators is left, so an entry written
	 * another way cannot hide from the comparison.
	 */
	private static List<String> valuesIn(String region, Pattern value) {
		String leftover = SEPARATORS.matcher(value.matcher(region).replaceAll("")).replaceAll("");
		assertTrue(leftover.isEmpty(), () -> "Unrecognised content in a pattern list: " + leftover);
		return value.matcher(region).results().map(result -> result.group(1)).toList();
	}

	private boolean matches(String pattern, String classFile) {
		return classFile.startsWith(pattern.substring(0, pattern.length() - 2));
	}
}
