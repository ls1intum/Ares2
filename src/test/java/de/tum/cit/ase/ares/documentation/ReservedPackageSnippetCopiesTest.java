package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;

/**
 * Checks that every copy of the reserved-package snippets in the documentation
 * and the examples still equals the snippet Ares ships, and that the versions
 * the pages state equal the ones in the code.
 * <p>
 * The reserved-package snippets are the build check that stops student classes
 * from landing in a package Ares trusts by name. Instructors copy them from the
 * pages, so a copy that drifted from the shipped file is what they deploy.
 *
 * @since 2.2.2
 * @author Markus Paulsen
 */
class ReservedPackageSnippetCopiesTest {

	/**
	 * The repository root. Like the other documentation tests, this relies on the
	 * test running from the repository root, which Maven does.
	 */
	private static final Path REPOSITORY = Path.of("").toAbsolutePath().normalize();

	/** Where the shipped snippets live in the source tree. */
	private static final Path SHIPPED = REPOSITORY.resolve(Path.of("src", "main", "resources", "de", "tum", "cit",
			"ase", "ares", "api", "configuration", "reservedPackages"));

	/** The examples, which carry the snippets as part of real builds. */
	private static final Path EXAMPLES = REPOSITORY.resolve("examples");

	/** The line that only a copy of the Gradle snippet contains. */
	private static final String GRADLE_MARKER = "def aresReservedPackagePatterns";

	/**
	 * The text that only a copy of the Maven snippet contains. The bare execution
	 * id is not enough, since a README quotes it from Maven's console output.
	 */
	private static final String MAVEN_MARKER = "<id>verify-ares-reserved-packages-v";

	/**
	 * How many Gradle copies each file must hold. Stating the number keeps the test
	 * from passing because a marker stopped matching and nothing was compared.
	 */
	private static final Map<String, Integer> EXPECTED_GRADLE_COPIES = Map.of(
			"documentation/docs/instructor/transform-ares-1-into-ares-2/postcompile/gradle.md", 1,
			"examples/ares-exercise-gradle/gradle/AresReservedPackages.gradle", 1);

	/** How many Maven copies each file must hold, for the same reason. */
	private static final Map<String, Integer> EXPECTED_MAVEN_COPIES = Map.of(
			"documentation/docs/instructor/transform-ares-1-into-ares-2/postcompile/maven.md", 2,
			"examples/ares-exercise-maven/pom.xml", 1);

	/**
	 * A sentence assigning one of the two versions a number, as the pages write it.
	 */
	private static final Pattern VERSION_STATEMENT = Pattern
			.compile("`(RESERVED_PACKAGE_PREFIX_VERSION|RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION) = (\\d+)`");

	/** The pages that state both versions, each exactly once. */
	private static final Set<String> PAGES_STATING_VERSIONS = Set.of(
			"documentation/docs/contributor/subsystems/policy/enforcement-model.md",
			"documentation/docs/instructor/protect-a-java-project/postcompile/gradle.md",
			"documentation/docs/instructor/protect-a-java-project/postcompile/maven.md",
			"documentation/docs/instructor/protect-a-java-project/precompile/gradle.md",
			"documentation/docs/instructor/transform-ares-1-into-ares-2/postcompile/gradle.md",
			"documentation/docs/instructor/transform-ares-1-into-ares-2/postcompile/maven.md");

	/**
	 * An XML comment, which a copy may add or drop without changing the snippet.
	 */
	private static final Pattern XML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

	/** Directories in the examples that hold build output rather than sources. */
	private static final Set<String> SKIPPED_DIRECTORIES = Set.of("build", "target", ".gradle", "node_modules");

	/**
	 * Fails at once when the test does not run from the repository root, rather
	 * than reporting every expected copy as missing.
	 */
	@BeforeAll
	static void runsFromTheRepositoryRoot() {
		for (Path required : List.of(DocumentationPages.DOCS, EXAMPLES, SHIPPED)) {
			assertTrue(Files.isDirectory(REPOSITORY.resolve(required)),
					() -> "Run from the repository root; " + required + " was not found under " + REPOSITORY);
		}
	}

	/**
	 * Every copy of the Gradle snippet equals the shipped one, comments included,
	 * because the pages and the example promise a verbatim copy.
	 */
	@Test
	void everyGradleCopyEqualsTheShippedSnippet() {
		String shipped = withoutTrailingNewlines(
				DocumentationPages.read(SHIPPED.resolve("GradleReservedPackages.gradle")));
		List<String> problems = new ArrayList<>();
		Map<String, Integer> found = new TreeMap<>();
		for (Path file : filesThatMayHoldCopies()) {
			List<String> copies = gradleCopiesIn(file, problems);
			if (!copies.isEmpty()) {
				found.put(relative(file), copies.size());
			}
			for (String copy : copies) {
				if (!shipped.equals(withoutTrailingNewlines(copy))) {
					problems.add(relative(file) + ": a Gradle copy differs from GradleReservedPackages.gradle");
				}
			}
		}
		problems.addAll(countProblems("Gradle", EXPECTED_GRADLE_COPIES, found));
		assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
	}

	/**
	 * Every copy of the Maven plugin equals the shipped one, ignoring indentation,
	 * blank lines and XML comments, since a pom re-indents what it embeds.
	 */
	@Test
	void everyMavenCopyEqualsTheShippedSnippet() {
		List<String> problems = new ArrayList<>();
		List<String> shippedPlugins = mavenCopiesIn(SHIPPED.resolve("MavenReservedPackages.xml"), problems);
		assertEquals(1, shippedPlugins.size(), "MavenReservedPackages.xml must hold exactly one plugin");
		String shipped = normalisedXml(shippedPlugins.get(0));
		Map<String, Integer> found = new TreeMap<>();
		for (Path file : filesThatMayHoldCopies()) {
			List<String> copies = mavenCopiesIn(file, problems);
			if (!copies.isEmpty()) {
				found.put(relative(file), copies.size());
			}
			for (String copy : copies) {
				if (!shipped.equals(normalisedXml(copy))) {
					problems.add(relative(file) + ": a Maven copy differs from MavenReservedPackages.xml");
				}
			}
		}
		problems.addAll(countProblems("Maven", EXPECTED_MAVEN_COPIES, found));
		assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
	}

	/**
	 * Every version a page states equals the constant in the code, and each of the
	 * expected pages states both versions exactly once.
	 */
	@Test
	void everyStatedVersionEqualsTheCode() {
		Map<String, String> actual = Map.of("RESERVED_PACKAGE_PREFIX_VERSION",
				WalaPathClassification.RESERVED_PACKAGE_PREFIX_VERSION, "RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION",
				WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION);
		List<String> problems = new ArrayList<>();
		Set<String> pagesFound = new TreeSet<>();
		for (Path page : DocumentationPages.pagesBelow()) {
			Map<String, Integer> statements = new TreeMap<>();
			Matcher matcher = VERSION_STATEMENT.matcher(DocumentationPages.read(page));
			while (matcher.find()) {
				statements.merge(matcher.group(1), 1, Integer::sum);
				if (!actual.get(matcher.group(1)).equals(matcher.group(2))) {
					problems.add(relative(page) + ": states " + matcher.group(1) + " = " + matcher.group(2)
							+ ", the code says " + actual.get(matcher.group(1)));
				}
			}
			if (!statements.isEmpty()) {
				pagesFound.add(relative(page));
			}
			if (!statements.isEmpty() && !statements.equals(
					Map.of("RESERVED_PACKAGE_PREFIX_VERSION", 1, "RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION", 1))) {
				problems.add(relative(page) + ": must state each version exactly once, but states " + statements);
			}
		}
		if (!pagesFound.equals(new TreeSet<>(PAGES_STATING_VERSIONS))) {
			problems.add("Pages stating the versions: expected " + new TreeSet<>(PAGES_STATING_VERSIONS) + ", found "
					+ pagesFound);
		}
		assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
	}

	/**
	 * Returns the documentation pages and the example build files, the places a
	 * copy is expected in.
	 */
	private static List<Path> filesThatMayHoldCopies() {
		List<Path> files = new ArrayList<>(DocumentationPages.pagesBelow());
		try (Stream<Path> entries = Files.walk(EXAMPLES)) {
			entries.filter(Files::isRegularFile).filter(ReservedPackageSnippetCopiesTest::isExampleBuildFile)
					.filter(ReservedPackageSnippetCopiesTest::isOutsideBuildOutput).sorted().forEach(files::add);
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + EXAMPLES, exception);
		}
		return files;
	}

	/** Whether the file is a Gradle script or an XML file such as a pom. */
	private static boolean isExampleBuildFile(Path file) {
		String name = file.getFileName().toString();
		return name.endsWith(".gradle") || name.endsWith(".xml");
	}

	/** Whether no directory on the path is one that holds build output. */
	private static boolean isOutsideBuildOutput(Path file) {
		for (Path segment : EXAMPLES.relativize(file)) {
			if (SKIPPED_DIRECTORIES.contains(segment.toString())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the Gradle copies in a file: a whole Gradle script, or each fenced
	 * block of a page. A marker that does not lead to exactly one copy is reported
	 * as a problem rather than skipped.
	 */
	private static List<String> gradleCopiesIn(Path file, List<String> problems) {
		String content = DocumentationPages.read(file);
		int markers = occurrences(content, GRADLE_MARKER);
		if (markers == 0) {
			return List.of();
		}
		List<String> copies = file.getFileName().toString().endsWith(".md")
				? fencedBlocksIn(content).stream().filter(block -> block.contains(GRADLE_MARKER)).toList()
				: List.of(content);
		int markersInCopies = copies.stream().mapToInt(copy -> occurrences(copy, GRADLE_MARKER)).sum();
		if (markersInCopies != markers || markers != copies.size()) {
			problems.add(relative(file) + ": " + markers + " Gradle markers, but " + copies.size() + " copies holding "
					+ markersInCopies + " of them");
		}
		return copies;
	}

	/**
	 * Returns the Maven copies in a file: for each marker, the text from the
	 * nearest plugin start tag before it to the first plugin end tag after it. A
	 * marker without such a pair of tags is reported rather than skipped.
	 */
	private static List<String> mavenCopiesIn(Path file, List<String> problems) {
		String content = DocumentationPages.read(file);
		List<String> copies = new ArrayList<>();
		int marker = content.indexOf(MAVEN_MARKER);
		while (marker >= 0) {
			int start = content.lastIndexOf("<plugin>", marker);
			int end = content.indexOf("</plugin>", marker);
			if (start < 0 || end < 0 || content.substring(start, marker).contains("</plugin>")) {
				problems.add(relative(file) + ": a Maven marker sits outside a plugin element");
			} else {
				copies.add(content.substring(start, end + "</plugin>".length()));
			}
			marker = content.indexOf(MAVEN_MARKER, marker + MAVEN_MARKER.length());
		}
		return copies;
	}

	/**
	 * Returns the contents of every fenced code block on a page, with the fence
	 * rules the other documentation tests use.
	 */
	private static List<String> fencedBlocksIn(String content) {
		List<String> blocks = new ArrayList<>();
		StringBuilder block = null;
		char fence = 0;
		int fenceLength = 0;
		for (String line : content.lines().toList()) {
			String trimmed = line.stripLeading();
			char marker = DocumentationPages.fenceMarkerOf(trimmed);
			int length = marker == 0 ? 0 : DocumentationPages.fenceLengthOf(trimmed, marker);
			if (block == null && marker != 0) {
				block = new StringBuilder();
				fence = marker;
				fenceLength = length;
			} else if (block != null && DocumentationPages.closesFence(trimmed, marker, length, fence, fenceLength)) {
				blocks.add(block.toString());
				block = null;
			} else if (block != null) {
				block.append(line).append('\n');
			}
		}
		return blocks;
	}

	/**
	 * Reports each file whose number of copies differs from the expected one,
	 * including a file expected to hold copies that holds none.
	 */
	private static List<String> countProblems(String tool, Map<String, Integer> expected, Map<String, Integer> found) {
		List<String> problems = new ArrayList<>();
		Set<String> files = new TreeSet<>(expected.keySet());
		files.addAll(found.keySet());
		for (String file : files) {
			int want = expected.getOrDefault(file, 0);
			int got = found.getOrDefault(file, 0);
			if (want != got) {
				problems.add(file + ": expected " + want + " " + tool + " copies, found " + got
						+ ". If that is intended, update the expected copies in this test.");
			}
		}
		return problems;
	}

	/**
	 * Reduces XML to what matters for the comparison: comments removed, every line
	 * trimmed, blank lines dropped.
	 */
	private static String normalisedXml(String xml) {
		return XML_COMMENT.matcher(xml).replaceAll("").lines().map(String::strip).filter(line -> !line.isEmpty())
				.reduce((first, second) -> first + "\n" + second).orElse("");
	}

	/** Removes the line breaks at the end of a text, and nothing else. */
	private static String withoutTrailingNewlines(String text) {
		int end = text.length();
		while (end > 0 && text.charAt(end - 1) == '\n') {
			end--;
		}
		return text.substring(0, end);
	}

	/** Counts how often a text occurs, without overlaps. */
	private static int occurrences(String content, String text) {
		int count = 0;
		int index = content.indexOf(text);
		while (index >= 0) {
			count++;
			index = content.indexOf(text, index + text.length());
		}
		return count;
	}

	/** Returns the path relative to the repository root, with forward slashes. */
	private static String relative(Path file) {
		return REPOSITORY.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
	}
}
