package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

/**
 * Architecture test asserting that every complete policy file shown in the
 * documentation is one Ares actually accepts.
 * <p>
 * A reference page is copied, not read. An example that omits a required field
 * costs the reader a rejected policy at best, and at worst a run they believe
 * is supervised: the policy fails to load and the reason is a schema diagnostic
 * naming a field the page never mentioned. The eight policy reference pages all
 * shipped without the root {@code thisPolicyFileCompliesToThePolicyVersion}
 * field, which {@code SecurityPolicySchemaValidator} requires, so none of them
 * could be copied into a working exercise.
 * <p>
 * Prose cannot be checked, so this test checks the one thing that can be: the
 * examples are fed to the production reader, and a page that documents a policy
 * Ares would reject fails the build.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
class PolicyExampleValidityTest {

	/**
	 * The lowest number of complete examples the documentation is expected to
	 * carry.
	 * <p>
	 * Without it the test passes vacuously the moment the extractor stops matching,
	 * which is exactly what a change to the fence syntax or the directory layout
	 * would cause.
	 */
	private static final int EXPECTED_AT_LEAST = 15;

	@TempDir
	Path temporaryDirectory;

	/**
	 * Every fenced YAML block in the documentation that is a whole policy file
	 * rather than an excerpt of one.
	 * <p>
	 * A whole file is recognised by its root key sitting at column zero. An
	 * excerpt, such as the permission shapes shown in the migration guide, starts
	 * at a nested key and would be rejected for being an excerpt rather than for
	 * being wrong.
	 */
	static Stream<Arguments> documentedPolicies() {
		List<Arguments> examples = new ArrayList<>();
		for (Path page : DocumentationPages.pagesBelow()) {
			String content = DocumentationPages.read(page);
			int index = 0;
			for (String block : yamlBlocksIn(content)) {
				index++;
				if (isWholePolicyFile(block)) {
					examples.add(Arguments.of(page + " (block " + index + ")", withoutFocusMarkers(block)));
				}
			}
		}
		return examples.stream();
	}

	@Test
	void theDocumentationCarriesTheExpectedNumberOfCompleteExamples() {
		long found = documentedPolicies().count();

		assertTrue(found >= EXPECTED_AT_LEAST, "Only " + found + " complete policy examples were found, and at least "
				+ EXPECTED_AT_LEAST + " are expected. The extractor has most likely stopped matching.");
	}

	@ParameterizedTest(name = "{0} is a policy Ares accepts")
	@MethodSource("documentedPolicies")
	void everyDocumentedPolicyLoads(String origin, String yaml) throws IOException {
		Path file = temporaryDirectory.resolve("policy-" + Integer.toUnsignedString(yaml.hashCode()) + ".yaml");
		Files.writeString(file, yaml, StandardCharsets.UTF_8);

		try {
			SecurityPolicy policy = new SecurityPolicyYAMLReader(new YAMLMapper(), temporaryDirectory)
					.readSecurityPolicyFrom(file);

			assertEquals(SecurityPolicy.CURRENT_POLICY_VERSION, policy.thisPolicyFileCompliesToThePolicyVersion(),
					origin + " declares a policy version Ares does not accept.");
		} catch (SecurityException rejection) {
			fail(origin + " shows a policy Ares rejects: " + rootCauseOf(rejection), rejection);
		}
	}

	/**
	 * Returns the deepest message of the rejection, which is the schema diagnostic
	 * naming the offending field. The wrapping {@code SecurityException} only
	 * reports that some file could not be read.
	 */
	private static String rootCauseOf(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null) {
			current = current.getCause();
		}
		return current.getMessage();
	}

	/**
	 * Returns the content of every fenced YAML block, in document order.
	 * <p>
	 * Every fence is tracked, not only the YAML ones, and a block ends on its own
	 * marker at its own length or longer. A page that shows a fenced example inside
	 * a longer fence would otherwise have the inner fence close the outer block,
	 * after which the rest of the page is read as if it were content.
	 */
	static List<String> yamlBlocksIn(String content) {
		List<String> blocks = new ArrayList<>();
		StringBuilder current = null;
		char fence = 0;
		int fenceLength = 0;
		for (String line : content.lines().toList()) {
			String trimmed = line.stripLeading();
			char marker = DocumentationPages.fenceMarkerOf(trimmed);
			int length = marker == 0 ? 0 : DocumentationPages.fenceLengthOf(trimmed, marker);
			if (fence == 0) {
				if (marker != 0) {
					fence = marker;
					fenceLength = length;
					current = trimmed.substring(length).stripLeading().startsWith("yaml") ? new StringBuilder() : null;
				}
				continue;
			}
			if (DocumentationPages.closesFence(trimmed, marker, length, fence, fenceLength)) {
				if (current != null) {
					blocks.add(current.toString());
					current = null;
				}
				fence = 0;
				fenceLength = 0;
				continue;
			}
			if (current != null) {
				current.append(line).append('\n');
			}
		}
		return List.copyOf(blocks);
	}

	@Test
	void readsAPolicyFromATildeFencedBlock() {
		List<String> blocks = yamlBlocksIn("""
				~~~yaml
				regardingTheSupervisedCode:
				~~~
				""");

		assertEquals(List.of("regardingTheSupervisedCode:\n"), blocks);
	}

	@Test
	void doesNotCloseALongFenceOnAShorterOneInsideIt() {
		List<String> blocks = yamlBlocksIn("""
				````markdown
				```yaml
				not: a policy, an example of one
				```
				````

				```yaml
				regardingTheSupervisedCode:
				```
				""");

		assertEquals(List.of("regardingTheSupervisedCode:\n"), blocks,
				"the three-backtick example belongs to the four-backtick block that contains it, and the "
						+ "policy after it is the only block this reads");
	}

	/** Returns true when the block declares a root key of the policy file. */
	private static boolean isWholePolicyFile(String block) {
		return block.lines().anyMatch(line -> "regardingTheSupervisedCode:".equals(line.stripTrailing()));
	}

	/**
	 * Removes the {@code policy-focus} markers of the reference pages. They are
	 * comments, so YAML ignores them, but stripping them keeps the file the reader
	 * sees identical to the one the page claims to show.
	 */
	private static String withoutFocusMarkers(String block) {
		return block.lines().filter(line -> !line.stripLeading().startsWith("# policy-focus-"))
				.collect(Collectors.joining("\n", "", "\n"));
	}
}
