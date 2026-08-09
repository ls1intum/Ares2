package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

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
 * <p>
 * It also owns the inventory of the site: one expected set of page paths per
 * guide. Everything else here is a rule about a page's contents, but a rule
 * about contents cannot notice a page that is missing, duplicated, or filed
 * under the wrong audience. The two sets are the only assertion that can.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class SiteWideDocumentationStructureTest {

	/** Every page of both guides. */
	private static List<Path> allPages() {
		return DocumentationPages.pagesBelow();
	}

	/**
	 * Every page of the instructor guide, as a guide-relative path.
	 * <p>
	 * This replaced a single assertion on the total page count. A count detects a
	 * net change and nothing else: deleting one required page while adding an
	 * accidental one, keeping a page that should have moved, or filing a page under
	 * the wrong audience all leave the total untouched. Restructuring is exactly
	 * when those mistakes happen, so the invariant has to name the pages.
	 */
	private static final List<String> EXPECTED_INSTRUCTOR_PAGES = List.of(
			"ares-2/what-does-ares-2-not-protect-against.md", "ares-2/what-does-ares-2-protect-against.md",
			"ares-2/what-is-ares-2.md", "policy-cookbook/allowing-exactly-one-host.md", "policy-cookbook/index.md",
			"policy-cookbook/reading-a-file-from-resources.md", "policy-cookbook/setting-time-and-memory-budgets.md",
			"policy-cookbook/using-a-library-that-reflects.md", "policy-cookbook/using-threads.md",
			"policy-cookbook/writing-an-output-file.md", "policy-reference/class-permission.md",
			"policy-reference/command-system-access.md", "policy-reference/file-system-access.md",
			"policy-reference/index.md", "policy-reference/network-system-access.md",
			"policy-reference/package-permission.md", "policy-reference/programming-language-configuration.md",
			"policy-reference/resource-limits.md", "policy-reference/thread-system-access.md",
			"protect-a-java-project/further-options.md", "protect-a-java-project/github-packages.md",
			"protect-a-java-project/installation.md", "protect-a-java-project/policy-configuration.md",
			"protect-a-java-project/postcompile/gradle.md", "protect-a-java-project/postcompile/maven.md",
			"protect-a-java-project/precompile/gradle.md", "protect-a-java-project/precompile/maven.md",
			"protect-a-java-project/precompile-or-postcompile.md", "protect-a-java-project/setup.md",
			"protect-a-java-project/test-annotations.md", "transform-ares-1-into-ares-2/index.md",
			"transform-ares-1-into-ares-2/postcompile/gradle.md", "transform-ares-1-into-ares-2/postcompile/maven.md",
			"transform-ares-1-into-ares-2/postcompile-or-precompile.md",
			"transform-ares-1-into-ares-2/precompile/gradle.md", "transform-ares-1-into-ares-2/precompile/maven.md",
			"troubleshooting.md");

	/** Every page of the contributor guide, as a guide-relative path. */
	private static final List<String> EXPECTED_CONTRIBUTOR_PAGES = List.of("extending-ares-2/index.md",
			"extending-ares-2/new-analysis-technology.md", "extending-ares-2/new-build-tools.md",
			"extending-ares-2/new-enforcement-mechanism.md", "extending-ares-2/new-policy-domains.md",
			"extending-ares-2/new-programming-languages.md", "how-can-you-contribute.md", "life-of-a-test-execution.md",
			"policy/class-permission.md", "policy/command-system-access.md", "policy/file-system-access.md",
			"policy/network-system-access.md", "policy/package-permission.md",
			"policy/programming-language-configuration.md", "policy/resource-limits.md",
			"policy/thread-system-access.md", "subsystems/aop/aspectj-vs-instrumentation-weaknesses.md",
			"subsystems/aop/block-command-system-access.md", "subsystems/aop/block-file-system-access.md",
			"subsystems/aop/block-thread-system-access.md", "subsystems/architecture/block-command-system-access.md",
			"subsystems/architecture/block-file-system-access.md",
			"subsystems/architecture/block-thread-system-access.md", "subsystems/ast.md", "subsystems/jqwik.md",
			"subsystems/jupiter.md", "subsystems/package-overview.md", "subsystems/phobos.md",
			"subsystems/policy/enforcement-model.md", "subsystems/policy/reader-and-director.md",
			"subsystems/policy/security-policy-manual.md", "subsystems/securitytest/test-case-factory-and-builder.md",
			"technologies/aop-tests/aspectj.md", "technologies/aop-tests/base-idea/advice.md",
			"technologies/aop-tests/base-idea/aspect.md", "technologies/aop-tests/base-idea/binding.md",
			"technologies/aop-tests/base-idea/join-point.md", "technologies/aop-tests/base-idea/pointcut.md",
			"technologies/aop-tests/base-idea/weaving.md", "technologies/aop-tests/instrumentation-with-bytebuddy.md",
			"technologies/architecture-tests/archunit.md", "technologies/architecture-tests/base-idea/call-graph.md",
			"technologies/architecture-tests/base-idea/dfs-path.md",
			"technologies/architecture-tests/base-idea/node.md", "technologies/architecture-tests/base-idea/rule.md",
			"technologies/architecture-tests/wala.md", "technologies/ast-tests/base-idea/call-graph.md",
			"technologies/ast-tests/base-idea/node.md", "technologies/ast-tests/base-idea/visitor.md",
			"technologies/ast-tests/javaparser.md", "technologies/jqwik.md", "technologies/junit-jupiter.md",
			"technologies/linux-based-security/base-idea/allow-list.md",
			"technologies/linux-based-security/base-idea/interception.md",
			"technologies/linux-based-security/base-idea/wrapper.md", "technologies/linux-based-security/bubblewrap.md",
			"technologies/linux-based-security/ld-preload-firewall.md", "technologies/linux-based-security/timeout.md",
			"testing-conventions.md");

	@Test
	void theInstructorGuideHoldsExactlyTheExpectedPages() {
		assertPagesOf("instructor", EXPECTED_INSTRUCTOR_PAGES);
	}

	@Test
	void theContributorGuideHoldsExactlyTheExpectedPages() {
		assertPagesOf("contributor", EXPECTED_CONTRIBUTOR_PAGES);
	}

	/**
	 * Compares one guide against its expected set, reporting what is missing and
	 * what is unexpected separately.
	 * <p>
	 * Reported separately on purpose: a rename shows up as one of each, and reading
	 * the two lists side by side tells you immediately whether a page moved or a
	 * page was lost.
	 */
	private static void assertPagesOf(String guide, List<String> expected) {
		List<String> actual = DocumentationPages.pagePathsOf(guide);

		List<String> missing = expected.stream().filter(page -> !actual.contains(page)).toList();
		List<String> unexpected = actual.stream().filter(page -> !expected.contains(page)).toList();

		assertTrue(missing.isEmpty() && unexpected.isEmpty(),
				() -> "The " + guide + " guide no longer matches its expected pages. Update the expected set in "
						+ "this test deliberately, in the same commit as the change." + System.lineSeparator()
						+ "  missing:    " + missing + System.lineSeparator() + "  unexpected: " + unexpected);
	}

	@ParameterizedTest(name = "{0} declares the front matter Docusaurus needs")
	@MethodSource("allPages")
	void everyPageDeclaresItsFrontMatter(Path page) {
		String content = DocumentationPages.read(page);

		assertTrue(DocumentationPages.hasFrontMatter(content), page + " must start with YAML front matter.");
		String frontMatter = DocumentationPages.frontMatterOf(content);
		DocumentationPages.REQUIRED_FRONT_MATTER.forEach(
				field -> assertTrue(frontMatter.contains(field), page + " front matter must declare " + field));
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
		assertTrue(DocumentationPages.opensWithEli5(DocumentationPages.read(page)),
				page + " must open with an ELI5 box written as ':::tip[ELI5]'. Carrying one further down "
						+ "the page does not count: it is meant to be the first thing a new reader sees.");
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
			for (String offender : DocumentationPages.legacyAdmonitionsIn(DocumentationPages.read(page))) {
				offenders.append(System.lineSeparator()).append("  ").append(page).append(": ").append(offender);
			}
		}

		assertTrue(offenders.isEmpty(),
				"These admonitions use the Docusaurus 2 form ':::kind Title', which renders as literal text "
						+ "instead of a box. Use ':::kind[Title]' instead." + offenders);
	}
}
