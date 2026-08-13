package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
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

	/**
	 * The repository root, resolved once.
	 * <p>
	 * Every path here is resolved against it rather than against the working
	 * directory of the JVM. The two are the same under Surefire today, and a check
	 * that quietly depends on that would report every reference as broken the day
	 * they diverge, pointing at the documentation for what would be a configuration
	 * change.
	 */
	private static final Path REPOSITORY = Path.of("").toAbsolutePath().normalize();

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

	private static final Pattern REFERENCE = Pattern.compile(Pattern.quote(SITE) + "[A-Za-z0-9_./*-]+");

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
			// Existence first, because the two causes call for opposite responses: a
			// renamed file means this list is stale, while a file that is there and not
			// scanned means the scan has a hole. One message for both would send a
			// reader after the wrong one.
			assertTrue(Files.exists(REPOSITORY.resolve(file)),
					file + " no longer exists, so this list is out of date rather than the scan. Name the file "
							+ "that replaced it, or drop the entry if nothing points into the documentation from "
							+ "there any more.");
			assertTrue(scanned.contains(file),
					file + " exists but is not scanned, so a reference in it would go unnoticed. It held one of "
							+ "the references this test exists for.");
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
			return Files.exists(REPOSITORY.resolve(reference));
		}
		String prefix = reference.substring(0, glob);
		int lastSeparator = prefix.lastIndexOf('/');
		return lastSeparator > 0 && Files.isDirectory(REPOSITORY.resolve(prefix.substring(0, lastSeparator)));
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

	/**
	 * The files to scan, collected once for the whole class.
	 * <p>
	 * Once rather than per test, and pruned rather than filtered afterwards. The
	 * skipped directories are not merely uninteresting: the site's
	 * {@code node_modules} holds tens of thousands of entries, and descending into
	 * it three times to discard everything found there is most of what this test
	 * would spend its time on.
	 */
	private static List<Path> scannedFiles;

	@BeforeAll
	static void collectTheFilesToScan() {
		List<Path> found = new ArrayList<>();
		try {
			Files.walkFileTree(REPOSITORY, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
					return SKIPPED_DIRECTORIES.contains(directory.getFileName().toString())
							? FileVisitResult.SKIP_SUBTREE
							: FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					Path relative = REPOSITORY.relativize(file);
					if (hasScannedSuffix(relative)) {
						found.add(relative);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException failure) {
					// An entry that cannot be read carries no reference this test can check,
					// and it is not what the test is about. Walking on beats failing here.
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + REPOSITORY, exception);
		}
		found.sort(Path::compareTo);
		scannedFiles = List.copyOf(found);
	}

	private static List<Path> scannedFiles() {
		return scannedFiles;
	}

	private static boolean hasScannedSuffix(Path relative) {
		String name = relative.getFileName().toString().toLowerCase(Locale.ROOT);
		int dot = name.lastIndexOf('.');
		return dot >= 0 && SCANNED_SUFFIXES.contains(name.substring(dot));
	}

	/**
	 * Reads a file as text, answering the empty string for one that is not. The
	 * scan is by suffix, and a suffix is a guess: a .txt fixture may hold bytes
	 * that are not UTF-8, and that is not a broken reference.
	 */
	private static String read(Path relative) {
		try {
			return Files.readString(REPOSITORY.resolve(relative), StandardCharsets.UTF_8);
		} catch (MalformedInputException notText) {
			return "";
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + relative, exception);
		}
	}
}
