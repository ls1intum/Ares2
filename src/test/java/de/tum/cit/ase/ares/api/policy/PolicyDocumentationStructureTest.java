package de.tum.cit.ase.ares.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Architecture test for the policy documentation.
 * <p>
 * Every page under {@code documentation/docs/contributor/policy} documents one
 * section of the same example security policy file, and the pages are meant to
 * be read in order: each shows the whole example and marks its own section in
 * red, so that the section as a whole walks the example from top to bottom.
 * <p>
 * That only works while every page keeps the identical shape. This test pins
 * that shape, so a page that grows an extra heading, loses its ELI5 box,
 * forgets a table column or marks the wrong part of the example fails the build
 * rather than quietly breaking the reading order.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class PolicyDocumentationStructureTest {

	/** The documentation directory, resolved from the module root. */
	private static final Path POLICY_DOCUMENTATION = Path.of("documentation", "docs", "contributor", "policy");

	/**
	 * The headings every page must carry, in this order and with nothing else at
	 * this level.
	 */
	private static final List<String> REQUIRED_HEADINGS = List.of("## Position in the example policy file", "## Fields",
			"## Notes");

	/** The table header every field table must use. */
	private static final String TABLE_HEADER = "| Field | Datatype | Explanation | Example | Regex or Range |";

	private static final Pattern FRONT_MATTER = Pattern.compile("\\A---\\R(.*?)\\R---\\R", Pattern.DOTALL);
	private static final Pattern SIDEBAR_POSITION = Pattern.compile("^sidebar_position: (\\d+)$", Pattern.MULTILINE);
	private static final Pattern YAML_BLOCK = Pattern.compile("```yaml title=\"security-policy\\.yaml\"\\R(.*?)\\R```",
			Pattern.DOTALL);

	private static List<Path> policyPages() throws IOException {
		try (Stream<Path> entries = Files.list(POLICY_DOCUMENTATION)) {
			return entries.filter(path -> path.getFileName().toString().endsWith(".md")).sorted().toList();
		}
	}

	private static String read(Path page) throws IOException {
		return Files.readString(page, StandardCharsets.UTF_8).replace("\r\n", "\n");
	}

	@Test
	void policyDocumentationDirectoryHoldsOnePagePerPolicyDomain() throws IOException {
		assertTrue(Files.isDirectory(POLICY_DOCUMENTATION),
				"Expected the policy documentation at " + POLICY_DOCUMENTATION.toAbsolutePath());
		assertEquals(8, policyPages().size(), "Every policy domain needs exactly one page, and no page needs two.");
	}

	@ParameterizedTest(name = "{0} has the required structure")
	@MethodSource("policyPages")
	void everyPageHasTheSameStructure(Path page) throws IOException {
		String content = read(page);

		assertTrue(FRONT_MATTER.matcher(content).find(), page + " must start with YAML front matter.");
		for (String field : List.of("title:", "sidebar_position:", "description:")) {
			assertTrue(content.contains(field), page + " front matter must declare " + field);
		}

		assertTrue(content.contains(":::tip ELI5"), page + " must open with an ELI5 box (:::tip ELI5).");
		assertTrue(content.contains(":::"), page + " ELI5 box must be closed.");

		List<String> headings = content.lines().filter(line -> line.startsWith("## ")).toList();
		assertEquals(REQUIRED_HEADINGS, headings,
				page + " must carry exactly the shared headings, in order, and no others.");

		assertTrue(content.contains(TABLE_HEADER), page + " must use the shared field table header.");
		assertTrue(YAML_BLOCK.matcher(content).find(),
				page + " must show the example policy file in a yaml block titled security-policy.yaml.");
	}

	@ParameterizedTest(name = "{0} marks exactly one contiguous section")
	@MethodSource("policyPages")
	void everyPageMarksExactlyOneContiguousSection(Path page) throws IOException {
		String block = yamlBlockOf(page);
		long starts = block.lines().filter(line -> line.equals("# policy-focus-start")).count();
		long ends = block.lines().filter(line -> line.equals("# policy-focus-end")).count();

		assertEquals(1, starts, page + " must mark exactly one section of the example.");
		assertEquals(1, ends, page + " must close its marked section exactly once.");
		assertTrue(block.indexOf("# policy-focus-start") < block.indexOf("# policy-focus-end"),
				page + " must open its marked section before closing it.");

		List<String> marked = markedLinesOf(block);
		assertFalse(marked.isEmpty(), page + " must mark at least one line.");
		marked.forEach(line -> assertFalse(line.isBlank(), page + " must not mark a blank line."));
	}

	@Test
	void everyPageShowsTheIdenticalExampleAndTogetherTheyCoverItTopToBottom() throws IOException {
		List<Path> pages = policyPages();

		// One example, shown identically everywhere. Compared with the markers removed,
		// since
		// each page marks a different part of it.
		List<String> reference = null;
		for (Path page : pages) {
			List<String> withoutMarkers = yamlBlockOf(page).lines().filter(line -> !line.startsWith("# policy-focus-"))
					.toList();
			if (reference == null) {
				reference = withoutMarkers;
			} else {
				assertEquals(reference, withoutMarkers, page + " must show the same example policy file as every "
						+ "other page, so that only the marked section differs.");
			}
		}

		// Reading the pages in sidebar order must walk the example downwards, and the
		// marked
		// sections must not overlap.
		List<Path> inSidebarOrder = new ArrayList<>(pages);
		inSidebarOrder.sort((left, right) -> Integer.compare(sidebarPositionOf(left), sidebarPositionOf(right)));

		int previousLast = -1;
		for (Path page : inSidebarOrder) {
			int[] range = markedRangeOf(page);
			assertTrue(range[0] > previousLast, page + " marks a section at or above the previous page's section. The "
					+ "pages must walk the example from top to bottom in sidebar order.");
			previousLast = range[1];
		}
	}

	/**
	 * Returns the first and last index of the marked section, counted within the
	 * example with the marker lines removed.
	 * <p>
	 * Counting has to ignore the markers: they are themselves lines, they sit at a
	 * different place on every page, and including them would shift the indices of
	 * two pages relative to one another so that the comparison across pages became
	 * meaningless.
	 */
	private static int[] markedRangeOf(Path page) throws IOException {
		int index = -1;
		int first = -1;
		int last = -1;
		boolean inside = false;
		for (String line : yamlBlockOf(page).lines().toList()) {
			if (line.equals("# policy-focus-start")) {
				inside = true;
				continue;
			}
			if (line.equals("# policy-focus-end")) {
				inside = false;
				continue;
			}
			index++;
			if (inside) {
				if (first < 0) {
					first = index;
				}
				last = index;
			}
		}
		return new int[] { first, last };
	}

	private static int sidebarPositionOf(Path page) {
		try {
			var matcher = SIDEBAR_POSITION.matcher(read(page));
			assertTrue(matcher.find(), page + " must declare a sidebar_position.");
			return Integer.parseInt(matcher.group(1));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read " + page, exception);
		}
	}

	private static String yamlBlockOf(Path page) throws IOException {
		var matcher = YAML_BLOCK.matcher(read(page));
		assertTrue(matcher.find(), page + " must contain the example policy file.");
		return matcher.group(1);
	}

	private static List<String> markedLinesOf(String block) {
		List<String> marked = new ArrayList<>();
		boolean inside = false;
		for (String line : block.lines().toList()) {
			if (line.equals("# policy-focus-start")) {
				inside = true;
			} else if (line.equals("# policy-focus-end")) {
				inside = false;
			} else if (inside) {
				marked.add(line);
			}
		}
		return marked;
	}
}
