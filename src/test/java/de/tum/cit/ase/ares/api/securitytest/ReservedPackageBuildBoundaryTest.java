package de.tum.cit.ase.ares.api.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;
import de.tum.cit.ase.ares.api.util.FileTools;

class ReservedPackageBuildBoundaryTest {
	private static final Path ROOT = FileTools.resolveFileOnSourceDirectory("configuration", "reservedPackages");

	/**
	 * Files a student must not put into the output, since a test framework reads
	 * them from the test class path before Ares is active. A pattern ending in a
	 * slash and two stars covers every file below that folder.
	 */
	private static final List<String> RESERVED_RESOURCE_PATTERNS = List.of("META-INF/services/**",
			"archunit.properties", "archunit_ignore_patterns.txt", "junit-platform.properties", "logback.xml",
			"logback-test.xml", "logback.groovy", "logback.scmo", "logback-test.scmo", "mockito-extensions/**",
			"jacoco-agent.properties");

	/**
	 * The package fileset of the Maven snippet, whose includes are the rejected
	 * package patterns. Like the resource fileset, it ignores case, as a
	 * case-insensitive file system would, and switches off Ant's default excludes,
	 * which would skip package folders such as {@code CVS}.
	 */
	private static final Pattern MAVEN_FILESET = Pattern.compile(
			"<resourcecount property=\"ares\\.reserved\\.package\\.count\">\\s*"
					+ "<fileset dir=\"\\$\\{project\\.build\\.outputDirectory\\}\" defaultexcludes=\"no\" casesensitive=\"false\">(.*?)</fileset>",
			Pattern.DOTALL);

	/** The resource fileset of the Maven snippet. */
	private static final Pattern MAVEN_RESOURCE_FILESET = Pattern.compile(
			"<resourcecount property=\"ares\\.reserved\\.resource\\.count\">\\s*"
					+ "<fileset dir=\"\\$\\{project\\.build\\.outputDirectory\\}\" defaultexcludes=\"no\" casesensitive=\"false\">(.*?)</fileset>",
			Pattern.DOTALL);

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

	/** The reserved-resource list of the Gradle snippet. */
	private static final Pattern GRADLE_RESOURCE_LIST = Pattern
			.compile("def aresReservedResourcePatterns = \\[(.*?)\\]", Pattern.DOTALL);

	/** One quoted entry of that list, capturing its pattern. */
	private static final Pattern GRADLE_PATTERN = Pattern.compile("'([^']*)'");

	/** One output file of each reserved kind, with case variants. */
	private static final List<String> REJECTED_RESOURCES = List.of(
			"META-INF/services/org.junit.platform.launcher.TestExecutionListener",
			"META-INF/services/com.tngtech.archunit.lang.extension.ArchUnitExtension", "meta-inf/Services/x.Y",
			"archunit.properties", "archunit_ignore_patterns.txt", "junit-platform.properties",
			"JUnit-Platform.Properties", "logback.xml", "logback-test.xml", "logback.groovy", "logback.scmo",
			"logback-test.scmo", "LOGBACK.XML", "mockito-extensions/org.mockito.plugins.MockMaker",
			"Mockito-Extensions/x.Y", "jacoco-agent.properties", "JaCoCo-Agent.Properties");

	/**
	 * The failure condition of the Maven snippet, which fails on a reserved package
	 * or on a reserved resource.
	 */
	private static final Pattern MAVEN_CONDITION = Pattern.compile("<condition>\\s*<or>\\s*"
			+ "<not><equals arg1=\"\\$\\{ares\\.reserved\\.package\\.count\\}\" arg2=\"0\"/></not>\\s*"
			+ "<not><equals arg1=\"\\$\\{ares\\.reserved\\.resource\\.count\\}\" arg2=\"0\"/></not>\\s*"
			+ "</or>\\s*</condition>");

	/**
	 * The decision of the Gradle snippet, which compares the case-folded path with
	 * the reserved packages and the reserved resources.
	 */
	private static final Pattern GRADLE_DECISION = Pattern
			.compile("if \\(reservedPrefixes\\.any \\{ lowered\\.startsWith\\(it\\) \\}\\s*"
					+ "\\|\\| reservedResourcePrefixes\\.any \\{ lowered\\.startsWith\\(it\\) \\}\\s*"
					+ "\\|\\| reservedResourceFiles\\.contains\\(lowered\\)\\) \\{");

	/** How the Gradle snippet case-folds the reserved package prefixes. */
	private static final Pattern GRADLE_FOLDED_PREFIXES = Pattern.compile(
			"def reservedPrefixes = aresReservedPackagePatterns\\s*\\.collect \\{ it\\.substring\\(0, it\\.length\\(\\) - 2\\)"
					+ "\\.toUpperCase\\(Locale\\.ROOT\\)\\.toLowerCase\\(Locale\\.ROOT\\) \\}");

	/**
	 * Class files under a reserved package, in any case, since a case-insensitive
	 * file system lets the class loader find {@code ORG/junit/Evil.class} as
	 * {@code org/junit/Evil.class}.
	 */
	private static final List<String> REJECTED_PACKAGE_PATHS = List.of("org/junit/Evil.class", "ORG/junit/Evil.class",
			"Org/JUnit/jupiter/api/Evil.class", "Java/lang/Evil.class", "De/Tum/Cit/Ase/Ares/Api/Evil.class",
			"org/junit/CVS/Evil.class");

	/** Class files of ordinary student packages, which must still pass. */
	private static final List<String> ACCEPTED_PACKAGE_PATHS = List.of("org/example/Penguin.class",
			"Org/Example/Penguin.class", "de/tum/student/Penguin.class", "org/junitx/Penguin.class",
			"sunflower/Penguin.class", "javafx/Penguin.class", "de/tum/cit/ase/ares/integration/Penguin.class");

	/** Ordinary output files that must still pass the check. */
	private static final List<String> ACCEPTED_RESOURCES = List.of("allowed.txt", "data/x.txt", "META-INF/other.txt",
			"META-INF/MANIFEST.MF", "META-INF/servicesList.txt", "data/META-INF/services/x.Y",
			"data/archunit.properties", "config/junit-platform.properties", "logback.xml.txt", "my-logback.xml",
			"org/example/Penguin.class", "data/jacoco-agent.properties", "mockito-extensionsList.txt",
			"data/mockito-extensions/x.Y");

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

	/**
	 * Both snippets reject exactly the reserved resources, Maven in a fileset that
	 * ignores case and counted by the failure condition, Gradle over the whole main
	 * output, the processed resources included.
	 */
	@Test
	void bothFixturesRejectExactlyTheReservedResources() throws Exception {
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		assertEquals(RESERVED_RESOURCE_PATTERNS,
				valuesIn(XML_COMMENT.matcher(onlyMatch(MAVEN_RESOURCE_FILESET, maven)).replaceAll(""), MAVEN_INCLUDE),
				"Maven fixture must reject exactly the reserved resources");
		assertTrue(MAVEN_CONDITION.matcher(maven).find(),
				"the Maven failure condition must fail on a reserved package or a reserved resource");
		assertEquals(RESERVED_RESOURCE_PATTERNS, valuesIn(onlyMatch(GRADLE_RESOURCE_LIST, gradle), GRADLE_PATTERN),
				"Gradle fixture must reject exactly the reserved resources");
		assertTrue(gradle.contains("def studentOutput = sourceSets.main.output") && !gradle.contains("classesDirs"),
				"Gradle fixture must scan the whole main output, where the resources land");
		assertTrue(gradle.contains("def lowered = relative.toUpperCase(Locale.ROOT).toLowerCase(Locale.ROOT)"),
				"Gradle fixture must compare resource names ignoring case");
		assertTrue(GRADLE_DECISION.matcher(gradle).find(),
				"Gradle fixture must reject a reserved package or a reserved resource");
		assertTrue(GRADLE_FOLDED_PREFIXES.matcher(gradle).find(),
				"Gradle fixture must compare package prefixes ignoring case");
	}

	/**
	 * Every reserved resource is rejected, in any case, and ordinary resources,
	 * including reserved names below another folder, still pass.
	 */
	@Test
	void reservedResourcesAreRejectedAndOrdinaryResourcesPass() {
		for (String rejected : REJECTED_RESOURCES) {
			assertTrue(rejectsResource(rejected), () -> "Fixtures do not reject " + rejected);
		}
		for (String accepted : ACCEPTED_RESOURCES) {
			assertFalse(rejectsResource(accepted), () -> "Fixtures reject the ordinary file " + accepted);
		}
	}

	/**
	 * A class file under a reserved package is rejected whatever the case of its
	 * folders, and ordinary student packages still pass.
	 */
	@Test
	void reservedPackagesAreRejectedInAnyCaseAndOrdinaryPackagesPass() {
		for (String rejected : REJECTED_PACKAGE_PATHS) {
			assertTrue(rejectsPackagePath(rejected), () -> "Fixtures do not reject " + rejected);
		}
		for (String accepted : ACCEPTED_PACKAGE_PATHS) {
			assertFalse(rejectsPackagePath(accepted), () -> "Fixtures reject the ordinary class " + accepted);
		}
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

	/**
	 * Whether a reserved package covers the output path, as both snippets decide
	 * it: the case-folded path starts with a case-folded reserved folder.
	 */
	private static boolean rejectsPackagePath(String path) {
		String folded = path.toUpperCase(Locale.ROOT).toLowerCase(Locale.ROOT);
		return WalaPathClassification.RESERVED_PACKAGE_PREFIXES.stream()
				.map(prefix -> prefix.replace('.', '/').toUpperCase(Locale.ROOT).toLowerCase(Locale.ROOT))
				.anyMatch(folded::startsWith);
	}

	/**
	 * Whether a reserved resource pattern covers the output path, as both snippets
	 * decide it: ignoring case, a folder pattern covers every file below the folder
	 * and a file pattern only that file at the root of the output.
	 */
	private static boolean rejectsResource(String path) {
		String lowered = path.toLowerCase(Locale.ROOT);
		return RESERVED_RESOURCE_PATTERNS.stream().map(pattern -> pattern.toLowerCase(Locale.ROOT))
				.anyMatch(pattern -> pattern.endsWith("/**")
						? lowered.startsWith(pattern.substring(0, pattern.length() - 2))
						: lowered.equals(pattern));
	}
}
