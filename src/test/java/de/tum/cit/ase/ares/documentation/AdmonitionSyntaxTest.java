package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

/**
 * Guards the whole documentation against the admonition syntax that silently
 * does nothing.
 * <p>
 * Docusaurus 3 takes the admonition title as a remark-directive label,
 * {@code :::tip[Title]}. The Docusaurus 2 form {@code :::tip Title} is not a
 * directive, so the fence, the title and the body are rendered as an ordinary
 * paragraph beginning with three colons. Nothing warns: the build succeeds, the
 * page renders, and the box is simply absent.
 * <p>
 * That is exactly what happened here. 53 admonitions across 47 pages, including
 * every ELI5 box, shipped as plain text and were only found once the Playwright
 * suite looked at the rendered page. This test makes the same mistake fail in
 * the ordinary Maven build.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class AdmonitionSyntaxTest {

	@Test
	void noPageUsesTheAdmonitionSyntaxThatRendersAsPlainText() {
		List<Path> pages = DocumentationPages.pagesBelow();
		assertTrue(pages.size() > 50, "Expected the documentation to be present, found " + pages.size() + " pages.");

		StringBuilder offenders = new StringBuilder();
		for (Path page : pages) {
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
