package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Architecture test for the enforcement-facing half of the policy
 * documentation.
 * <p>
 * The instructor guide answers "what may I write", pinned by
 * {@link PolicyDocumentationStructureTest}. This section answers "how is it
 * enforced", and the two must stay one-to-one: a domain documented for authors
 * with no enforcement page beside it is a domain whose guarantees nobody has
 * written down, and an enforcement page with no author page is a field nobody
 * can reach.
 * <p>
 * The shared headings are pinned for the same reason as on the instructor side.
 * The value of this section is that every domain can be compared against every
 * other along the same axes, and that only holds while the axes are identical.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class ContributorPolicyDocumentationStructureTest {

	private static final Path CONTRIBUTOR_POLICY = Path.of("documentation", "docs", "contributor", "policy");

	private static final Path INSTRUCTOR_POLICY = Path.of("documentation", "docs", "instructor", "policy-reference");

	/**
	 * The headings every enforcement page must carry, in this order and with
	 * nothing else at this level.
	 */
	private static final List<String> REQUIRED_HEADINGS = List.of("## Model", "## Validation and normalisation",
			"## What it generates", "## Static enforcement", "## Runtime enforcement", "## Where the code lives",
			"## Known gaps");

	/**
	 * The domain pages, excluding a section index.
	 * <p>
	 * Excluded for the same reason as on the instructor side, and with the same
	 * filter as {@link #fileNamesIn}: an index is a page of the directory but not a
	 * domain page, so neither the shared headings nor the counterpart link apply to
	 * it. The instructor section already has one, so this directory acquiring one
	 * is a question of when.
	 */
	private static List<Path> enforcementPages() {
		try (Stream<Path> entries = Files.list(CONTRIBUTOR_POLICY)) {
			return entries.filter(path -> path.getFileName().toString().endsWith(".md"))
					.filter(path -> !"index.md".equals(path.getFileName().toString())).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not list " + CONTRIBUTOR_POLICY, exception);
		}
	}

	private static List<String> fileNamesIn(Path directory) {
		try (Stream<Path> entries = Files.list(directory)) {
			return entries.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".md"))
					.filter(name -> !"index.md".equals(name)).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not list " + directory, exception);
		}
	}

	@Test
	void everyDomainDocumentedForAuthorsHasAnEnforcementPage() {
		assertEquals(fileNamesIn(INSTRUCTOR_POLICY), fileNamesIn(CONTRIBUTOR_POLICY),
				"The two policy sections must stay one-to-one, by filename, so that a reader can move "
						+ "between a field and its enforcement without guessing.");
	}

	@ParameterizedTest(name = "{0} carries the shared enforcement headings")
	@MethodSource("enforcementPages")
	void everyPageHasTheSameStructure(Path page) {
		String content = DocumentationPages.read(page);

		assertEquals(REQUIRED_HEADINGS, DocumentationPages.sectionHeadings(content),
				page + " must carry exactly the shared enforcement headings, in order, and no others.");
	}

	@ParameterizedTest(name = "{0} points at its instructor counterpart")
	@MethodSource("enforcementPages")
	void everyPageLinksToTheFieldReference(Path page) {
		String slug = page.getFileName().toString().replace(".md", "");

		assertTrue(DocumentationPages.read(page).contains("/instructor/policy-reference/" + slug),
				page + " must link to /instructor/policy-reference/" + slug + ", so that a reader who "
						+ "landed on the enforcement page can reach the fields it enforces.");
	}
}
