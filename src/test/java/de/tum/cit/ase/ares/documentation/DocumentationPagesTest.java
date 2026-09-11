package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Covers the fenced-code tracking the documentation structure tests rely on.
 * <p>
 * Everything those tests assert about a page is asserted about the lines
 * outside its code blocks, so a fence read wrongly does not merely miss one
 * line: every line after it is classified backwards, and the page is reported
 * for the examples it is documenting.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class DocumentationPagesTest {

	@Test
	void reportsALegacyAdmonitionInProse() {
		List<String> offenders = DocumentationPages.legacyAdmonitionsIn("""
				:::tip Simple Story
				Written the Docusaurus 2 way, so it renders as text.
				:::
				""");

		assertEquals(List.of(":::tip Simple Story"), offenders);
	}

	@Test
	void ignoresALegacyAdmonitionInsideAFencedBlock() {
		List<String> offenders = DocumentationPages.legacyAdmonitionsIn("""
				```markdown
				:::tip Simple Story
				```
				""");

		assertTrue(offenders.isEmpty(), "a page showing the mistake must not be reported for showing it");
	}

	@Test
	void doesNotCloseALongFenceOnAShorterOneInsideIt() {
		List<String> offenders = DocumentationPages.legacyAdmonitionsIn("""
				````markdown
				```markdown
				:::tip Simple Story
				```
				````
				""");

		assertTrue(offenders.isEmpty(),
				"the three-backtick fence is content of the four-backtick block, so the admonition between "
						+ "them is still inside a code block");
	}

	@Test
	void doesNotCloseABacktickFenceOnATildeOne() {
		List<String> offenders = DocumentationPages.legacyAdmonitionsIn("""
				```markdown
				~~~
				:::tip Simple Story
				~~~
				```
				""");

		assertTrue(offenders.isEmpty(), "a block ends on the marker that opened it, not on the other one");
	}

	@Test
	void readsPastAFenceThatCarriesAnInfoString() {
		List<String> offenders = DocumentationPages.legacyAdmonitionsIn("""
				```markdown
				content
				```

				:::warning Outside
				""");

		assertEquals(List.of(":::warning Outside"), offenders,
				"the closing fence carries nothing, so what follows it is prose again");
	}
}
