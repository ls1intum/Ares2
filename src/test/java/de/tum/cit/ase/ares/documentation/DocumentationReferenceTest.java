package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Asserts that what points at the documentation from outside it still arrives.
 * <p>
 * The Docusaurus build resolves the links between pages and fails on a broken
 * one, but it sees only the site. A build file, a script or a review
 * configuration that names a manual is outside that reach, and nothing else
 * looks: a comment naming a moved file compiles, a path filter naming a moved
 * directory simply stops matching, and a script naming a moved manual fails
 * only when somebody runs it.
 * <p>
 * All three happened here. The migration moved the manuals under
 * {@code documentation/docs} and repointed the outside references at an interim
 * layout, and when that layout was renamed the outside references were not
 * carried over. {@code examples/README.md} and two build files pointed into
 * directories that never existed, {@code tools/pointcut_comparison.R} read two
 * manuals that had moved, and {@code .coderabbit.yaml} filtered on a directory
 * that was gone.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
@DisplayName("References into the documentation")
class DocumentationReferenceTest {

	private static final Path REPOSITORY = Path.of("");

	/** Where the documentation lives now. */
	private static final String SITE = "documentation/docs/";

	/**
	 * Where it lived before the migration, and where nothing lives today.
	 * <p>
	 * The first lookbehind is what keeps this off the tail of a correct
	 * {@code documentation/docs/...}, which ends in the very characters being
	 * searched for. The second keeps it off a longer word ending in {@code docs}.
	 */
	private static final Pattern REMOVED_TREE = Pattern
			.compile("(?<!documentation/)(?<![\\w.-])docs/[A-Za-z0-9_./*-]+");

	private static final Pattern REFERENCE = Pattern.compile("documentation/docs/[A-Za-z0-9_./*-]+");

	/**
	 * Directories holding no reference worth resolving: the site itself, whose
	 * links the Docusaurus build already checks, and everything generated.
	 */
	private static final Set<String> SKIPPED_DIRECTORIES = Set.of("documentation", "target", "node_modules", ".git",
			".idea", ".settings", "site");

	/** The text formats a reference has been found in. */
	private static final Set<String> SCANNED_SUFFIXES = Set.of(".md", ".markdown", ".adoc", ".txt", ".xml", ".yml",
			".yaml", ".json", ".gradle", ".kts", ".java", ".r", ".py", ".sh", ".properties");

	@Test
	@DisplayName("Every path into the documentation resolves")
	void everyReferenceIntoTheDocumentationResolves() {
		List<String> broken = new ArrayList<>();
		for (Path file : scannedFiles()) {
			Matcher references = REFERENCE.matcher(read(file));
			while (references.find()) {
				String reference = trimProse(references.group());
				if (!resolves(reference)) {
					broken.add(file + " -> " + reference);
				}
			}
		}

		assertEquals(List.of(), broken,
				"These references name something under documentation/docs that is not there. Nothing else "
						+ "reports them: the Docusaurus build resolves the links inside the site, and a path in a "
						+ "build file or a script is never resolved at all until somebody follows it.");
	}

	@Test
	@DisplayName("Nothing still names the documentation tree the migration removed")
	void nothingNamesTheRemovedDocumentationTree() {
		List<String> stale = new ArrayList<>();
		for (Path file : scannedFiles()) {
			for (String line : read(file).lines().toList()) {
				if (line.contains("://")) {
					// A URL carries paths of its own, and docs/ in one of them says nothing
					// about this repository.
					continue;
				}
				Matcher removed = REMOVED_TREE.matcher(line);
				while (removed.find()) {
					stale.add(file + " -> " + removed.group());
				}
			}
		}

		assertEquals(List.of(), stale,
				"The top-level docs/ directory was replaced by documentation/docs/ and no longer exists, so "
						+ "these names match nothing. A path filter that stops matching does not fail; it just "
						+ "quietly stops applying.");
	}

	@Test
	@DisplayName("The scan reaches the files that carried the broken references")
	void theScanReachesTheFilesThatCarriedTheBrokenReferences() {
		List<String> scanned = scannedFiles().stream().map(path -> path.toString().replace('\\', '/')).toList();

		for (String file : List.of("examples/README.md", "examples/ares-exercise-maven/pom.xml",
				"examples/ares-exercise-gradle/build.gradle", "tools/pointcut_comparison.R", ".coderabbit.yaml")) {
			assertTrue(scanned.contains(file),
					file + " is not scanned, so a reference in it would go unnoticed. It held one of the "
							+ "references this test exists for.");
		}
	}

	/**
	 * Whether the reference names something that is there. A reference carrying a
	 * glob is checked as far as its fixed prefix, which is what a path filter
	 * needs: the directory it starts from has to exist for it to match anything.
	 */
	private static boolean resolves(String reference) {
		int glob = reference.indexOf('*');
		if (glob < 0) {
			return Files.exists(Path.of(reference));
		}
		String prefix = reference.substring(0, glob);
		int lastSeparator = prefix.lastIndexOf('/');
		return lastSeparator > 0 && Files.isDirectory(Path.of(prefix.substring(0, lastSeparator)));
	}

	/**
	 * Removes what belongs to the sentence rather than to the path: the anchor of a
	 * Markdown link, and a full stop that ends the line it sits in.
	 */
	private static String trimProse(String reference) {
		String trimmed = reference.split("#", 2)[0];
		while (trimmed.endsWith(".") || trimmed.endsWith(",")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	private static List<Path> scannedFiles() {
		try (Stream<Path> entries = Files.walk(REPOSITORY.toAbsolutePath().normalize())) {
			Path root = REPOSITORY.toAbsolutePath().normalize();
			return entries.filter(Files::isRegularFile).map(root::relativize)
					.filter(DocumentationReferenceTest::isScanned).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk the repository", exception);
		}
	}

	private static boolean isScanned(Path relative) {
		for (Path segment : relative) {
			if (SKIPPED_DIRECTORIES.contains(segment.toString())) {
				return false;
			}
		}
		String name = relative.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
		int dot = name.lastIndexOf('.');
		return dot >= 0 && SCANNED_SUFFIXES.contains(name.substring(dot));
	}

	/**
	 * Reads a file as text, answering the empty string for one that is not. The
	 * scan is by suffix, and a suffix is a guess: a .txt fixture may hold bytes
	 * that are not UTF-8, and that is not a broken reference.
	 */
	private static String read(Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (MalformedInputException notText) {
			return "";
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + file, exception);
		}
	}
}
