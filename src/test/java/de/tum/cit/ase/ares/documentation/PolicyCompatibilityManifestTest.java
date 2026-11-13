package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorFieldNames;

/**
 * Keeps the published compatibility table honest: its newest release accepts
 * exactly the keys the validator accepts, it is not older than the version in
 * {@code pom.xml}, and the documentation page shows the same table.
 */
class PolicyCompatibilityManifestTest {

	/** The repository root the test runs from. */
	private static final Path REPOSITORY = Path.of("").toAbsolutePath().normalize();

	/** The published table. */
	private static final Path MANIFEST = REPOSITORY.resolve("documentation/static/policy-compatibility.json");

	/** The page generated from the table. */
	private static final Path PAGE = REPOSITORY
			.resolve("documentation/docs/contributor/subsystems/policy/policy-compatibility.md");

	/** The start of a release row on the page, such as {@code | 2.3.0 |}. */
	private static final Pattern RELEASE_ROW = Pattern.compile("\\| \\d+\\.\\d+\\.\\d+ \\|");

	/** The project version in {@code pom.xml}. */
	private static final Pattern POM_VERSION = Pattern
			.compile("<artifactId>ares</artifactId>\\s*<version>([^<]+)</version>");

	/** The newest release accepts exactly the keys the validator knows. */
	@Test
	void theNewestReleaseListsExactlyTheValidatorsKeys() throws IOException {
		JsonNode newest = newestRelease();
		List<String> listed = new ArrayList<>();
		newest.get("testBehaviorKeys").forEach(key -> listed.add(key.textValue()));
		assertEquals(new TreeSet<>(validatorKeys()), new TreeSet<>(listed));
		assertEquals(SecurityPolicy.CURRENT_POLICY_VERSION, newest.get("maximumPolicyFormat").intValue());
	}

	/**
	 * A release cannot be built without its own row; a row for a later release does
	 * not stand in for it.
	 */
	@Test
	void theProjectVersionHasItsOwnRow() throws IOException {
		Matcher matcher = POM_VERSION.matcher(Files.readString(REPOSITORY.resolve("pom.xml")));
		assertTrue(matcher.find(), "no project version in pom.xml");
		String pomVersion = matcher.group(1).replace("-SNAPSHOT", "");
		List<String> versions = new ArrayList<>();
		manifest().get("releases").forEach(release -> versions.add(release.get("version").textValue()));
		assertTrue(versions.contains(pomVersion),
				() -> "policy-compatibility.json has no row for " + pomVersion + "; it lists " + versions);
	}

	/**
	 * The page shows exactly the table the file holds: every release row of the
	 * file, and no release row the file lacks.
	 */
	@Test
	void thePageShowsTheManifest() throws IOException {
		List<String> manifestRows = new ArrayList<>();
		for (JsonNode release : manifest().get("releases")) {
			List<String> keys = new ArrayList<>();
			release.get("testBehaviorKeys").forEach(key -> keys.add("`" + key.textValue() + "`"));
			manifestRows.add("| " + release.get("version").textValue() + " | "
					+ release.get("minimumPolicyFormat").intValue() + " to "
					+ release.get("maximumPolicyFormat").intValue() + " | " + String.join("<br/>", keys) + " |");
		}
		List<String> pageRows = Files.readString(PAGE).replace("\r\n", "\n").lines()
				.filter(line -> RELEASE_ROW.matcher(line).lookingAt()).toList();
		assertEquals(new TreeSet<>(manifestRows), new TreeSet<>(pageRows));
	}

	/** Every key path the validator accepts below the test-behaviour wrapper. */
	private static List<String> validatorKeys() {
		List<String> keys = new ArrayList<>(List.of(TestBehaviorFieldNames.WRAPPER));
		for (Map.Entry<String, List<String>> category : TestBehaviorFieldNames.FIELDS_BY_CATEGORY.entrySet()) {
			String categoryPath = TestBehaviorFieldNames.WRAPPER + "." + category.getKey();
			keys.add(categoryPath);
			category.getValue().forEach(field -> keys.add(categoryPath + "." + field));
		}
		return keys;
	}

	/** The newest release in the table. */
	private static JsonNode newestRelease() throws IOException {
		JsonNode newest = null;
		for (JsonNode release : manifest().get("releases")) {
			if (newest == null || compare(release.get("version").textValue(), newest.get("version").textValue()) > 0) {
				newest = release;
			}
		}
		assertTrue(newest != null, "policy-compatibility.json lists no release");
		return newest;
	}

	/** Reads the table. */
	private static JsonNode manifest() throws IOException {
		return new ObjectMapper().readTree(MANIFEST.toFile());
	}

	/** Compares two dotted version numbers part by part. */
	private static int compare(String left, String right) {
		String[] a = left.split("\\.");
		String[] b = right.split("\\.");
		for (int i = 0; i < Math.max(a.length, b.length); i++) {
			int difference = Integer.compare(i < a.length ? Integer.parseInt(a[i]) : 0,
					i < b.length ? Integer.parseInt(b[i]) : 0);
			if (difference != 0) {
				return difference;
			}
		}
		return 0;
	}
}
