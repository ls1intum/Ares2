package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The rules that hold for every page of the documentation, in both guides.
 * <p>
 * The three section tests each pin what makes their own section different. This
 * one pins what they all share, applied to every page rather than to the pages
 * of one section, so a new section added later inherits the same floor without
 * anyone having to remember to write a test for it.
 * <p>
 * The admonition rule is here for a specific reason. Docusaurus 3 takes the
 * title as a remark-directive label, {@code :::tip[Title]}. The Docusaurus 2
 * form {@code :::tip Title} is not a directive at all, so the fence, the title
 * and the body render as an ordinary paragraph beginning with three colons.
 * Nothing warns: the build succeeds, the page renders, and the box is simply
 * absent. 53 of them shipped that way, including every ELI5 box, and were only
 * found once the Playwright suite looked at a rendered page.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class SiteWideDocumentationStructureTest {

	/** Every page of both guides. */
	private static List<Path> allPages() {
		return DocumentationPages.pagesBelow();
	}

	@Test
	void theDocumentationHoldsEveryPageOfBothGuides() {
		assertEquals(66, allPages().size(),
				"The page count changed. Add or remove a page deliberately rather than by accident.");
	}

	@ParameterizedTest(name = "{0} declares the front matter Docusaurus needs")
	@MethodSource("allPages")
	void everyPageDeclaresItsFrontMatter(Path page) {
		String content = DocumentationPages.read(page);

		assertTrue(DocumentationPages.hasFrontMatter(content), page + " must start with YAML front matter.");
		DocumentationPages.REQUIRED_FRONT_MATTER
				.forEach(field -> assertTrue(content.contains(field), page + " front matter must declare " + field));
		assertTrue(DocumentationPages.sidebarPosition(content) > 0,
				page + " must declare a positive sidebar_position.");
	}

	@ParameterizedTest(name = "{0} takes its title from the front matter")
	@MethodSource("allPages")
	void noPageCarriesItsOwnTopLevelHeading(Path page) {
		List<String> headings = DocumentationPages.topLevelHeadings(DocumentationPages.read(page));
		assertTrue(headings.isEmpty(),
				page + " must not carry an h1; the title comes from the front matter. Found: " + headings);
	}

	@ParameterizedTest(name = "{0} opens with an ELI5 box")
	@MethodSource("allPages")
	void everyPageOpensWithAnEli5Box(Path page) {
		assertTrue(DocumentationPages.read(page).contains(":::tip[ELI5]"),
				page + " must open with an ELI5 box written as ':::tip[ELI5]'.");
	}

	@ParameterizedTest(name = "{0} is divided into sections")
	@MethodSource("allPages")
	void everyPageIsDividedIntoSections(Path page) {
		assertFalse(DocumentationPages.sectionHeadings(DocumentationPages.read(page)).isEmpty(),
				page + " must be divided into at least one '## ' section.");
	}

	@Test
	void noPageUsesTheAdmonitionSyntaxThatRendersAsPlainText() {
		// Reported in one message rather than per page, so a systematic slip shows its
		// full
		// extent at once instead of one file at a time.
		StringBuilder offenders = new StringBuilder();
		for (Path page : allPages()) {
			Matcher matcher = DocumentationPages.LEGACY_ADMONITION.matcher(DocumentationPages.read(page));
			while (matcher.find()) {
				offenders.append(System.lineSeparator()).append("  ").append(page).append(": ").append(matcher.group());
			}
		}

		assertTrue(offenders.isEmpty(),
				"These admonitions use the Docusaurus 2 form ':::kind Title', which renders as literal text "
						+ "instead of a box. Use ':::kind[Title]' instead." + offenders);
	}
}
