package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Architecture test for the subsystem documentation.
 * <p>
 * The pages under {@code documentation/docs/contributor/subsystems} are
 * deliberately not uniform in the way the policy pages are. Some are short
 * overviews of a package, and some are the long reference manuals migrated from
 * the former {@code docs/} folder, which run to over a thousand lines and have
 * their own internal structure.
 * <p>
 * Pinning one heading layout across both would be dishonest, so what is
 * asserted here is the contract that genuinely does apply to all of them: they
 * carry the front matter Docusaurus needs, they take their title from that
 * front matter rather than from an h1, they do not silently claim to be
 * finished when they are stubs, and their categories are ordered.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class SubsystemsDocumentationStructureTest {

	private static final String STUB_MARKER = "This page is a stub";

	private static final String STUB_ADMONITION = ":::note[This page is a stub]";

	/**
	 * The reference manuals that must stay substantial.
	 * <p>
	 * Pinned by name rather than counted, so that truncating one and adding another
	 * cannot cancel out.
	 */
	private static final List<String> EXPECTED_MANUALS = List.of("block-command-system-access.md",
			"block-command-system-access.md", "block-file-system-access.md", "block-file-system-access.md",
			"block-thread-system-access.md", "block-thread-system-access.md", "package-overview.md",
			"reader-and-director.md", "security-policy-manual.md", "test-case-factory-and-builder.md");

	/** The eight subsystems the section is expected to cover. */
	private static final List<String> EXPECTED_SUBSYSTEMS = List.of("aop", "architecture", "ast", "jqwik", "jupiter",
			"phobos", "policy", "securitytest");

	private static List<Path> subsystemPages() {
		return DocumentationPages.pagesBelow("contributor", "subsystems");
	}

	private static List<Path> categoryFiles() {
		try (Stream<Path> entries = Files.walk(DocumentationPages.DOCS.resolve("contributor").resolve("subsystems"))) {
			return entries.filter(path -> path.getFileName().toString().equals("_category_.json")).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk the subsystem documentation", exception);
		}
	}

	@Test
	void theSubsystemSectionHoldsAPageForEveryPackageAndItsManuals() {
		assertEquals(16, subsystemPages().size(),
				"The subsystem section is expected to hold exactly the eight package pages and the "
						+ "reference manuals beneath them.");
	}

	@Test
	void everySubsystemIsRepresented() {
		String tree = subsystemPages().stream().map(Path::toString).reduce("", (left, right) -> left + " " + right)
				+ categoryFiles().stream().map(Path::toString).reduce("", (left, right) -> left + " " + right);

		List<String> missing = EXPECTED_SUBSYSTEMS.stream().filter(subsystem -> !tree.contains(subsystem)).toList();
		assertTrue(missing.isEmpty(), "The subsystem documentation covers no page for: " + missing);
	}

	@ParameterizedTest(name = "{0} carries the front matter Docusaurus needs")
	@MethodSource("subsystemPages")
	void everyPageCarriesFrontMatter(Path page) {
		String content = DocumentationPages.read(page);

		assertTrue(DocumentationPages.hasFrontMatter(content), page + " must start with YAML front matter.");
		DocumentationPages.REQUIRED_FRONT_MATTER
				.forEach(field -> assertTrue(content.contains(field), page + " front matter must declare " + field));
		assertTrue(DocumentationPages.sidebarPosition(content) > 0,
				page + " must declare a positive sidebar_position.");

		assertTrue(DocumentationPages.opensWithSimpleStory(content),
				page + " must open with a Simple Story box written as ':::tip[Simple Story]'. Every page in the "
						+ "documentation opens with one, including the long reference manuals.");
		assertFalse(!DocumentationPages.legacyAdmonitionsIn(content).isEmpty(),
				page + " uses an admonition form that renders as plain text.");

		// The manuals differ too much for one heading layout to be honest, but every
		// page has
		// to be divided into sections rather than being one undifferentiated wall of
		// prose.
		assertFalse(DocumentationPages.sectionHeadings(content).isEmpty(),
				page + " must be divided into at least one '## ' section.");
	}

	@ParameterizedTest(name = "{0} takes its title from the front matter")
	@MethodSource("subsystemPages")
	void noPageCarriesItsOwnTopLevelHeading(Path page) {
		// Docusaurus renders the front-matter title as the h1. A second h1 in the body
		// gives the
		// page two competing titles and breaks the document outline.
		List<String> topLevelHeadings = DocumentationPages.topLevelHeadings(DocumentationPages.read(page));
		assertTrue(topLevelHeadings.isEmpty(), page + " must not carry an h1, found: " + topLevelHeadings);
	}

	@ParameterizedTest(name = "{0} marks an unfinished page honestly")
	@MethodSource("subsystemPages")
	void everyStubSaysSoInABox(Path page) {
		String content = DocumentationPages.read(page);
		if (content.contains(STUB_MARKER)) {
			assertTrue(content.contains(STUB_ADMONITION), page + " is a stub, so it must say so in an admonition "
					+ "written as '" + STUB_ADMONITION + "' rather than in running text.");
		}
	}

	@ParameterizedTest(name = "{0} is an ordered category")
	@MethodSource("categoryFiles")
	void everyCategoryDeclaresALabelAndAPosition(Path category) {
		String content;
		try {
			content = Files.readString(category, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + category, exception);
		}
		assertTrue(content.contains("\"label\""), category + " must declare a label.");
		assertTrue(content.contains("\"position\""), category + " must declare a position, so the sidebar is ordered.");
	}

	@Test
	void theMigratedManualsAreStillPresentAndSubstantial() {
		// The long reference manuals are the reason this section is not uniform. If one
		// of them
		// were ever truncated or replaced by a stub, this section would quietly lose
		// most of its
		// content while every other assertion here still passed.
		List<Path> manuals = subsystemPages().stream()
				.filter(page -> DocumentationPages.read(page).lines().count() > 300).toList();
		assertFalse(manuals.isEmpty(), "Expected the migrated reference manuals under subsystems.");
		assertEquals(EXPECTED_MANUALS, manuals.stream().map(page -> page.getFileName().toString()).sorted().toList(),
				"The set of substantial reference manuals changed. Add the new one deliberately rather than "
						+ "letting a manual be truncated unnoticed.");
	}
}
