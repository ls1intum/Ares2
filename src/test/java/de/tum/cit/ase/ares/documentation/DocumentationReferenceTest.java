package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * The Docusaurus build resolves the links inside the site and fails on a broken
 * one, but it sees only the site. A build file, a script or a review
 * configuration that names a manual is outside that reach, and a path that
 * stops matching never fails; it quietly stops applying.
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
	 * they diverge.
	 */
	private static final Path REPOSITORY = Path.of("").toAbsolutePath().normalize();

	/** Where the documentation lives now. */
	private static final String SITE = "documentation/docs/";

	/**
	 * Where it lived before the migration, and where nothing lives today.
	 * <p>
	 * The first lookbehind keeps this off the tail of a correct
	 * {@code documentation/docs/...}, which ends in the very characters being
	 * searched for. The second keeps it off a longer word ending in {@code docs}.
	 * The third keeps it off a nested directory of that name, such as
	 * {@code examples/docs/}, which is a different directory and still exists.
	 */
	private static final Pattern REMOVED_TREE = Pattern
			.compile("(?<!documentation/)(?<![\\w-])(?<!\\w/)docs/[A-Za-z0-9_./*-]+");

	/** A path into the documentation as it is written outside the site. */
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

	/**
	 * The files to scan, collected once for the whole class.
	 * <p>
	 * Once rather than per test, and pruned rather than filtered afterwards. The
	 * skipped directories are not merely uninteresting: the site's
	 * {@code node_modules} holds tens of thousands of entries, and descending into
	 * it to discard everything found there is most of what this test would spend
	 * its time on.
	 */
	private static List<Path> scannedFiles;

	/**
	 * The entries the walk could not read, and which are therefore in none of the
	 * checks below. Kept rather than discarded, because a file that is silently
	 * never scanned is a reference that is silently never resolved.
	 */
	private static List<String> walkFailures;

	/**
	 * Collects the files to scan, and the entries that could not be read, before
	 * the first test.
	 */
	@BeforeAll
	static void collectTheFilesToScan() {
		List<Path> found = new ArrayList<>();
		List<String> failures = new ArrayList<>();
		try {
			Files.walkFileTree(REPOSITORY, new SimpleFileVisitor<>() {
				/** Prunes a skipped directory rather than descending into it. */
				@Override
				public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
					return SKIPPED_DIRECTORIES.contains(directory.getFileName().toString())
							? FileVisitResult.SKIP_SUBTREE
							: FileVisitResult.CONTINUE;
				}

				/** Keeps a file whose suffix says it can hold a reference. */
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					Path relative = REPOSITORY.relativize(file);
					if (hasScannedSuffix(relative)) {
						found.add(relative);
					}
					return FileVisitResult.CONTINUE;
				}

				/** Records an entry that could not be read, and walks on. */
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException failure) {
					if (hasScannedSuffix(REPOSITORY.relativize(file))) {
						failures.add(REPOSITORY.relativize(file) + ": " + failure);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + REPOSITORY, exception);
		}
		found.sort(Path::compareTo);
		scannedFiles = List.copyOf(found);
		walkFailures = List.copyOf(failures);
	}

	/** Asserts that every path into the site names something that is there. */
	@Test
	@DisplayName("Every path into the documentation resolves")
	void everyReferenceIntoTheDocumentationResolves() {
		List<String> broken = new ArrayList<>();
		List<String> undetermined = new ArrayList<>();
		for (Path file : scannedFiles()) {
			Matcher references = REFERENCE.matcher(read(file));
			while (references.find()) {
				String reference = trimProse(references.group());
				if (isUndetermined(targetOf(reference))) {
					undetermined.add(file + " -> " + reference);
				} else if (!resolves(reference)) {
					broken.add(file + " -> " + reference);
				}
			}
		}

		assertEquals(List.of(), undetermined,
				"The filesystem could not say whether these targets are there, so whether the reference arrives "
						+ "is unknown rather than answered. That is a permission or a mount problem where the test "
						+ "runs, and not a stale reference.");
		assertEquals(List.of(), broken,
				"These references name something under documentation/docs that is not there. Nothing else "
						+ "reports them: the Docusaurus build resolves the links inside the site, and a path in a "
						+ "build file or a script is never resolved at all until somebody follows it.");
	}

	/**
	 * Asserts that nothing names the top-level tree the migration removed. A line
	 * carrying a URL is passed over: a URL holds paths of its own, and a
	 * {@code docs/} in one of them says nothing about this repository.
	 */
	@Test
	@DisplayName("Nothing still names the documentation tree the migration removed")
	void nothingNamesTheRemovedDocumentationTree() {
		List<String> stale = new ArrayList<>();
		for (Path file : scannedFiles()) {
			for (String line : read(file).lines().toList()) {
				if (line.contains("://")) {
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

	/**
	 * Asserts that the removed-tree pattern reads a whole path segment. A
	 * {@code docs/} inside a longer word, or under a directory of its own, is a
	 * different thing and still exists; reporting it would send a reader after a
	 * file that was never moved.
	 */
	@Test
	@DisplayName("The removed-tree pattern reads a path segment, not a substring")
	void theRemovedTreePatternReadsAPathSegment() {
		assertTrue(REMOVED_TREE.matcher(removedPath("aop/BlockFileSystemAccessAOP.md")).find(),
				"A bare top-level path is what the migration removed and what this test hunts.");
		assertTrue(REMOVED_TREE.matcher("read `" + removedPath("policy/SecurityPolicyManual.md") + "` first").find(),
				"A path in prose or in a code span is still a path into the removed tree.");
		assertFalse(REMOVED_TREE.matcher("documentation/docs/instructor/troubleshooting.md").find(),
				"The current tree ends in the very characters being searched for.");
		assertFalse(REMOVED_TREE.matcher("examples/docs/topic.md").find(),
				"A nested docs/ is a different directory, and nothing about it moved.");
		assertFalse(REMOVED_TREE.matcher("javadocs/index.html").find(),
				"A longer word ending in docs is not a path segment of that name.");
	}

	/**
	 * Asserts that the walk read every entry it reached. An entry it could not read
	 * holds no checked reference, so a hole in the scan has to be reported rather
	 * than mistaken for a clean run.
	 */
	@Test
	@DisplayName("The scan read every file it reached")
	void theScanReadEveryFileItReached() {
		assertEquals(List.of(), walkFailures,
				"These files were reached and could not be read, so any reference in them went unchecked and "
						+ "this run proves less than it appears to. Fix the permissions where the test runs.");
	}

	/**
	 * Asserts that the scan reaches the files that carried the references this test
	 * exists for. Existence is checked first, because the two causes call for
	 * opposite responses: a renamed file means this list is stale, while a file
	 * that is there and unscanned means the scan has a hole.
	 */
	@Test
	@DisplayName("The scan reaches the files that carried the broken references")
	void theScanReachesTheFilesThatCarriedTheBrokenReferences() {
		List<String> scanned = scannedFiles().stream().map(path -> path.toString().replace('\\', '/')).toList();

		for (String file : List.of("examples/README.md", "examples/ares-exercise-maven/pom.xml",
				"examples/ares-exercise-gradle/build.gradle", "tools/pointcut_comparison.R", ".coderabbit.yaml")) {
			Path absolute = REPOSITORY.resolve(file);
			assertFalse(isUndetermined(absolute),
					file + " could not be inspected, so neither this list nor the scan is what is in question. "
							+ "Fix the permissions where the test runs.");
			assertTrue(Files.exists(absolute),
					file + " no longer exists, so this list is out of date rather than the scan. Name the file "
							+ "that replaced it, or drop the entry if nothing points into the documentation from "
							+ "there any more.");
			assertTrue(scanned.contains(file),
					file + " exists but is not scanned, so a reference in it would go unnoticed. It held one of "
							+ "the references this test exists for.");
		}
	}

	/**
	 * A path into the removed tree, assembled rather than written out. The check
	 * above scans this file too, and a fixture written in one piece would be
	 * reported by it as exactly the stale reference it exists to describe.
	 */
	private static String removedPath(String rest) {
		return "docs" + "/" + rest;
	}

	/**
	 * Whether the filesystem could not say if the path is there. Absence and an
	 * unreadable parent both leave {@link Files#exists} false, and only one of them
	 * is a broken reference.
	 */
	private static boolean isUndetermined(Path absolute) {
		return !Files.exists(absolute) && !Files.notExists(absolute);
	}

	/**
	 * The path a reference names, as far as it can be checked. A reference carrying
	 * a glob is checked as far as the directory its fixed prefix starts from, which
	 * is what a path filter needs: that directory has to exist for the filter to
	 * match anything.
	 */
	private static Path targetOf(String reference) {
		int glob = reference.indexOf('*');
		if (glob < 0) {
			return REPOSITORY.resolve(reference);
		}
		String prefix = reference.substring(0, glob);
		int lastSeparator = prefix.lastIndexOf('/');
		return REPOSITORY.resolve(lastSeparator > 0 ? prefix.substring(0, lastSeparator) : prefix);
	}

	/** Whether the reference names something that is there. */
	private static boolean resolves(String reference) {
		Path target = targetOf(reference);
		return reference.indexOf('*') < 0 ? Files.exists(target) : Files.isDirectory(target);
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

	/** The files collected for the whole class. */
	private static List<Path> scannedFiles() {
		return scannedFiles;
	}

	/** Whether the file is in one of the text formats a reference is read from. */
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
