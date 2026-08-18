package de.tum.cit.ase.ares.documentation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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

	/** The admonition that must open every page. */
	public static final String SIMPLE_STORY = ":::tip[Simple Story]";

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
	 * Returns every Markdown page of one guide, as slash-separated paths relative
	 * to that guide's own root.
	 * <p>
	 * The guide-relative form is what the expected-path sets are written in: it is
	 * stable across operating systems, and it reads as the route a page will be
	 * published at rather than as a filesystem location.
	 */
	public static List<String> pagePathsOf(String guide) {
		Path root = DOCS.resolve(guide);
		try (Stream<Path> entries = Files.walk(root)) {
			return entries.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".md"))
					.map(path -> root.relativize(path).toString().replace('\\', '/')).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + root, exception);
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

	/**
	 * Returns the contents of the front matter block, without its delimiters, or
	 * the empty string when the page has none.
	 * <p>
	 * Every check on a metadata field has to run against this rather than against
	 * the whole page. A page whose prose happens to contain the text
	 * {@code description:} otherwise satisfies a front-matter assertion while
	 * declaring no description at all, and the same page can carry a
	 * {@code sidebar_position:} line inside a YAML example that has nothing to do
	 * with its own position in the sidebar.
	 */
	public static String frontMatterOf(String content) {
		Matcher matcher = FRONT_MATTER.matcher(content);
		return matcher.find() ? matcher.group(1) : "";
	}

	/**
	 * Returns true when the page body, after the front matter and any blank lines,
	 * begins with the Simple Story admonition, and that admonition is closed before
	 * anything else opens.
	 * <p>
	 * "Opens with" is the actual rule. Merely containing the box somewhere lets a
	 * page bury it below several paragraphs, which defeats the purpose: it is meant
	 * to be the first thing a reader who is new to the subject sees.
	 * <p>
	 * The closing fence belongs in the same check rather than being left to the
	 * build. An unclosed {@code :::tip[Simple Story]} swallows everything beneath
	 * it until some later directive happens to close it, so a page can read
	 * correctly in source, render as one enormous admonition, and still satisfy an
	 * assertion that only looks for the opening line.
	 */
	public static boolean opensWithSimpleStory(String content) {
		List<String> body = content.substring(frontMatterEnd(content)).stripLeading().lines().toList();
		if (body.isEmpty() || !body.get(0).startsWith(SIMPLE_STORY)) {
			return false;
		}
		for (String line : body.subList(1, body.size())) {
			if (":::".equals(line.stripTrailing())) {
				return true;
			}
			if (line.startsWith(":::")) {
				// A second directive opened while the Simple Story box was still open.
				return false;
			}
		}
		return false;
	}

	private static int frontMatterEnd(String content) {
		Matcher matcher = FRONT_MATTER.matcher(content);
		return matcher.find() ? matcher.end() : 0;
	}

	/**
	 * Returns every legacy admonition on the page, ignoring fenced code blocks.
	 * <p>
	 * The fence tracking is not cosmetic. A page that documents this very mistake,
	 * or that shows a Docusaurus 2 snippet as an example of what not to write,
	 * would otherwise be reported for its own example, and the only way to silence
	 * that is to stop documenting the mistake.
	 */
	public static List<String> legacyAdmonitionsIn(String content) {
		return outsideFencedCode(content, line -> LEGACY_ADMONITION.matcher(line).matches());
	}

	/**
	 * Returns every line satisfying the predicate that is not inside a fenced code
	 * block.
	 * <p>
	 * The fence state is the opening marker and its length, not a boolean. A
	 * boolean toggling on both {@code ```} and {@code ~~~} desynchronises as soon
	 * as one appears inside a block delimited by the other, and a state that
	 * ignores the length closes a four-backtick block on the three-backtick example
	 * it exists to contain. Both leave every following line classified backwards,
	 * which is how a page about writing documentation gets reported for the
	 * examples it is documenting.
	 */
	private static List<String> outsideFencedCode(String content, Predicate<String> predicate) {
		List<String> matches = new ArrayList<>();
		char fence = 0;
		int fenceLength = 0;
		for (String line : content.lines().toList()) {
			String trimmed = line.stripLeading();
			char marker = fenceMarkerOf(trimmed);
			int length = marker == 0 ? 0 : fenceLengthOf(trimmed, marker);
			if (fence == 0) {
				if (marker != 0) {
					fence = marker;
					fenceLength = length;
				} else if (predicate.test(line)) {
					matches.add(line);
				}
				continue;
			}
			if (closesFence(trimmed, marker, length, fence, fenceLength)) {
				fence = 0;
				fenceLength = 0;
			}
		}
		return List.copyOf(matches);
	}

	/**
	 * Returns the fence character a line opens or closes with, or 0 when it is not
	 * a fence at all. Three of the character are the minimum a fence can be.
	 */
	public static char fenceMarkerOf(String trimmedLine) {
		if (trimmedLine.startsWith("```")) {
			return '`';
		}
		return trimmedLine.startsWith("~~~") ? '~' : 0;
	}

	/** Returns how many fence characters the line opens or closes with. */
	public static int fenceLengthOf(String trimmedLine, char marker) {
		int length = 0;
		while (length < trimmedLine.length() && trimmedLine.charAt(length) == marker) {
			length++;
		}
		return length;
	}

	/**
	 * Whether this line closes the open fence: the same character, at least as
	 * long, and carrying nothing else. An info string such as {@code ```yaml}
	 * opens, so a line that has one cannot close.
	 */
	public static boolean closesFence(String trimmedLine, char marker, int length, char fence, int fenceLength) {
		return marker == fence && length >= fenceLength && trimmedLine.substring(length).isBlank();
	}

	/**
	 * Returns the declared sidebar position, or -1 when the page declares none.
	 * <p>
	 * Read from the front matter only. A YAML example inside the prose can carry a
	 * {@code sidebar_position:} line of its own, and matching that would report a
	 * position the sidebar never sees.
	 */
	public static int sidebarPosition(String content) {
		Matcher matcher = SIDEBAR_POSITION.matcher(frontMatterOf(content));
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
		return outsideFencedCode(content, line -> line.startsWith(prefix));
	}
}
