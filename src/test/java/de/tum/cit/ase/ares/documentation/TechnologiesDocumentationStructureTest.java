package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Architecture test for the technology documentation.
 * <p>
 * Every page under {@code documentation/docs/contributor/technologies}
 * introduces one concept or one dependency, and every page has the same shape:
 * an ELI5 box, the explanation, and a closing list of further reading.
 * <p>
 * The citation rules are asserted here as well, because they are a policy
 * rather than a matter of taste. Sources are restricted to an allow list of
 * official documentation, reference sites and the project repositories, and a
 * link to a Medium article has to record that it was checked to be readable
 * without a membership. Nine candidate links were dropped during the original
 * pass for being dead; this keeps the standard from slipping later.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class TechnologiesDocumentationStructureTest {

	private static final String FURTHER_READING = "## Further reading";

	private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[[^\\]]+\\]\\((https?://[^)\\s]+)\\)");

	/**
	 * Hosts a citation may point at.
	 * <p>
	 * Official documentation and the projects' own repositories first, then the
	 * reference sites agreed for background reading. Anything else has to be added
	 * here deliberately rather than slipped into a page.
	 */
	private static final List<String> ALLOWED_HOSTS = List.of("docs.junit.org", "jqwik.net", "www.archunit.org",
			"github.com", "eclipse.dev", "bytebuddy.net", "javaparser.org", "man7.org", "www.baeldung.com",
			"www.w3schools.com", "docs.oracle.com", "www.oracle.com", "medium.com");

	private static List<Path> technologyPages() {
		return DocumentationPages.pagesBelow("contributor", "technologies");
	}

	private static String hostOf(String url) {
		String withoutScheme = url.replaceFirst("^https?://", "");
		String host = withoutScheme.split("/", 2)[0];
		// Medium publishes author pages on subdomains such as abhijit-pal.medium.com.
		return host.endsWith(".medium.com") ? "medium.com" : host;
	}

	@Test
	void theTechnologySectionIsFullyWritten() {
		List<Path> pages = technologyPages();
		assertEquals(26, pages.size(), "Every technology and concept needs exactly one page.");

		List<Path> stubs = pages.stream().filter(page -> DocumentationPages.read(page).contains("This page is a stub"))
				.toList();
		assertTrue(stubs.isEmpty(), "These technology pages are still stubs: " + stubs);
	}

	@ParameterizedTest(name = "{0} has the required structure")
	@MethodSource("technologyPages")
	void everyPageHasTheSameStructure(Path page) {
		String content = DocumentationPages.read(page);

		assertTrue(DocumentationPages.hasFrontMatter(content), page + " must start with YAML front matter.");
		DocumentationPages.REQUIRED_FRONT_MATTER
				.forEach(field -> assertTrue(content.contains(field), page + " front matter must declare " + field));
		assertTrue(DocumentationPages.sidebarPosition(content) > 0,
				page + " must declare a positive sidebar_position.");

		assertTrue(DocumentationPages.opensWithEli5(content),
				page + " must open with an ELI5 box written as ':::tip[ELI5]'.");
		assertFalse(!DocumentationPages.legacyAdmonitionsIn(content).isEmpty(),
				page + " uses an admonition form that renders as plain text.");

		List<String> headings = DocumentationPages.sectionHeadings(content);
		assertFalse(headings.isEmpty(), page + " must have at least one section.");
		assertEquals(FURTHER_READING, headings.get(headings.size() - 1),
				page + " must close with a '" + FURTHER_READING + "' section.");

		assertTrue(DocumentationPages.topLevelHeadings(content).isEmpty(),
				page + " must not carry an h1; the title comes from the front matter.");
	}

	@ParameterizedTest(name = "{0} cites only permitted sources")
	@MethodSource("technologyPages")
	void everyPageCitesOnlyPermittedSources(Path page) {
		String content = DocumentationPages.read(page);
		int start = content.indexOf(FURTHER_READING);
		assertTrue(start >= 0, page + " must have a '" + FURTHER_READING + "' section.");
		String furtherReading = content.substring(start);

		Matcher matcher = MARKDOWN_LINK.matcher(furtherReading);
		int citations = 0;
		while (matcher.find()) {
			String url = matcher.group(1);
			String host = hostOf(url);
			assertTrue(ALLOWED_HOSTS.contains(host),
					page + " cites " + url + ", whose host " + host + " is not on the allow list.");
			citations++;
		}
		assertTrue(citations >= 2, page + " must cite at least two sources, found " + citations + ".");
	}

	@ParameterizedTest(name = "{0} records that Medium citations are free to read")
	@MethodSource("technologyPages")
	void everyMediumCitationIsRecordedAsFreelyReadable(Path page) {
		// The requirement is that only pages without a payment block are linked. That
		// cannot be
		// checked from here, so what is enforced instead is that whoever added the link
		// stated
		// they checked it. A bare Medium link is rejected.
		DocumentationPages.read(page).lines().filter(line -> line.contains("medium.com"))
				.forEach(line -> assertTrue(line.contains("freely readable"),
						page + " cites Medium without recording that the article was checked to be readable "
								+ "without a membership: " + line.trim()));
	}
}
