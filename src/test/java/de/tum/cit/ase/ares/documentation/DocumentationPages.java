package de.tum.cit.ase.ares.documentation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Shared helpers for the documentation structure tests.
 * <p>
 * The tests read the Markdown under {@code documentation/docs} directly rather
 * than the built site, so they run in the ordinary Maven build without Node.
 * What they cannot see is whether a page renders correctly; the Playwright
 * tests under {@code documentation/tests} cover that.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
public final class DocumentationPages {

	/** The root of the documentation content. */
	public static final Path DOCS = Path.of("documentation", "docs");

	/** Front matter fields every page must declare. */
	public static final List<String> REQUIRED_FRONT_MATTER = List.of("title:", "sidebar_position:", "description:");

	private static final Pattern FRONT_MATTER = Pattern.compile("\\A---\\R(.*?)\\R---\\R", Pattern.DOTALL);

	private static final Pattern SIDEBAR_POSITION = Pattern.compile("^sidebar_position: (\\d+)$", Pattern.MULTILINE);

	/**
	 * Matches the Docusaurus 2 admonition form {@code :::kind Title}.
	 * <p>
	 * Docusaurus 3 expects the remark-directive label syntax
	 * {@code :::kind[Title]}. The old form is not a directive at all, so it is
	 * rendered as literal paragraph text instead of a box, silently and without any
	 * build warning. 53 of these shipped before the Playwright suite caught them,
	 * which is why it is asserted here as well.
	 */
	public static final Pattern LEGACY_ADMONITION = Pattern.compile(
			"^:::(?:secondary|info|success|danger|note|tip|warning|important|caution)[ \\t]+(?!\\[).+$",
			Pattern.MULTILINE);

	private DocumentationPages() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Returns every Markdown page directly inside the given documentation
	 * directory.
	 */
	public static List<Path> pagesIn(String... directory) {
		Path folder = DOCS;
		for (String segment : directory) {
			folder = folder.resolve(segment);
		}
		try (Stream<Path> entries = Files.list(folder)) {
			return entries.filter(path -> path.getFileName().toString().endsWith(".md")).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not list " + folder, exception);
		}
	}

	/**
	 * Returns every Markdown page below the given documentation directory,
	 * recursively.
	 */
	public static List<Path> pagesBelow(String... directory) {
		Path folder = DOCS;
		for (String segment : directory) {
			folder = folder.resolve(segment);
		}
		try (Stream<Path> entries = Files.walk(folder)) {
			return entries.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".md"))
					.sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + folder, exception);
		}
	}

	/**
	 * Reads a page with line endings normalised, so the assertions are platform
	 * independent.
	 */
	public static String read(Path page) {
		try {
			return Files.readString(page, StandardCharsets.UTF_8).replace("\r\n", "\n");
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + page, exception);
		}
	}

	/** Returns true when the page starts with a YAML front matter block. */
	public static boolean hasFrontMatter(String content) {
		return FRONT_MATTER.matcher(content).find();
	}

	/** Returns the declared sidebar position, or -1 when the page declares none. */
	public static int sidebarPosition(String content) {
		Matcher matcher = SIDEBAR_POSITION.matcher(content);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
	}

	/** Returns the level-two headings of a page, in document order. */
	public static List<String> sectionHeadings(String content) {
		return headingsAtDepth(content, 2);
	}

	/** Returns the level-one headings of a page, in document order. */
	public static List<String> topLevelHeadings(String content) {
		return headingsAtDepth(content, 1);
	}

	/**
	 * Returns the headings of the given depth, ignoring anything inside a fenced
	 * code block.
	 * <p>
	 * Tracking the fences is not optional here. The migrated manuals are full of
	 * YAML and shell snippets whose comments begin with a hash, and a naive scan
	 * reports lines such as {@code # REQUIRED: The policy format version} as a
	 * level-one heading.
	 */
	private static List<String> headingsAtDepth(String content, int depth) {
		String prefix = "#".repeat(depth) + " ";
		List<String> headings = new java.util.ArrayList<>();
		String fence = null;
		for (String line : content.lines().toList()) {
			String trimmed = line.stripLeading();
			if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
				String token = trimmed.substring(0, 1);
				if (fence == null) {
					fence = token;
				} else if (fence.equals(token)) {
					fence = null;
				}
				continue;
			}
			if (fence == null && line.startsWith(prefix)) {
				headings.add(line);
			}
		}
		return List.copyOf(headings);
	}
}
