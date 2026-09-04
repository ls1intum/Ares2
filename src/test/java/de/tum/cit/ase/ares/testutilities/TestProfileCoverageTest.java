package de.tum.cit.ase.ares.testutilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Asserts that every test class is selected by something the workflows actually
 * run.
 * <p>
 * Surefire is never invoked here without a selection: each step names a profile
 * whose {@code <includes>} list the packages it covers, or names classes with
 * {@code -Dtest}. A test class outside every one of those selections is
 * compiled, is green on the machine of whoever wrote it, and is never executed
 * by the build. Nothing reports that, because a test that does not run cannot
 * fail.
 * <p>
 * This has now happened twice. The {@code testutilities/**} include exists
 * because that package was in exactly this position, and the whole
 * {@code documentation/**} suite followed it, including the test asserting that
 * a documented policy is one Ares accepts, which was added precisely so that a
 * documentation defect could not merge.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
@DisplayName("Test-profile coverage")
class TestProfileCoverageTest {

	private static final Path POM = Path.of("pom.xml");

	private static final Path WORKFLOWS = Path.of(".github", "workflows");

	private static final Path TEST_SOURCES = Path.of("src", "test", "java");

	/** Reads the profile identifiers of a {@code -Punit-core-tests,coverage}. */
	private static final Pattern PROFILE_FLAG = Pattern.compile("-P([A-Za-z0-9,_-]+)");

	/** Reads the class selection of a {@code -Dtest=...}, quoted or bare. */
	private static final Pattern TEST_FLAG = Pattern.compile("-Dtest=(?:'([^']*)'|\"([^\"]*)\"|(\\S+))");

	/**
	 * Every annotation that marks a method or class as a test here.
	 * <p>
	 * The same set {@code JavaProjectScanner.TEST_ANNOTATIONS} recognises, and for
	 * the same reason: whichever of them appears, the file runs tests. Searching
	 * for {@code @Test} alone would not even find {@code @ParameterizedTest}, whose
	 * name contains {@code Test} but never the {@code @Test} the search is looking
	 * for. Both the simple and the fully qualified form are accepted, since either
	 * compiles.
	 */
	private static final Pattern TEST_ANNOTATION = Pattern
			.compile("@(?:" + "(?:org\\.junit\\.jupiter\\.api\\.)?(?:Test|RepeatedTest|TestFactory|TestTemplate)"
					+ "|(?:org\\.junit\\.)?Test" + "|(?:org\\.junit\\.jupiter\\.params\\.)?ParameterizedTest"
					+ "|(?:net\\.jqwik\\.api\\.)?(?:Property|Example)"
					+ "|(?:de\\.tum\\.cit\\.ase\\.ares\\.api\\.jupiter\\.)?(?:PublicTest|HiddenTest)" + ")\\b");

	/**
	 * Files that end in {@code Test.java} without being tests.
	 * <p>
	 * Named individually and with the reason, because the alternative is a pattern
	 * that quietly grows until it covers a real test again.
	 */
	private static final Map<String, String> NOT_A_TEST_CLASS = Map.of("p/TrustedTest.java",
			"A fixture for the allow-list prefix-collision case, whose fully qualified name has to be exactly "
					+ "p.TrustedTest for the scenario to mean anything. It declares no test method.");

	@Test
	@DisplayName("Every test class is selected by a profile or a -Dtest the workflows use")
	void everyTestClassIsSelectedBySomethingTheWorkflowsRun() {
		Selections selections = selectionsOfTheWorkflows();
		Map<String, Map<String, List<String>>> profiles = surefireSelectionsOfThePom();

		assertFalse(selections.profiles().isEmpty(), "No -P flag was found in " + WORKFLOWS
				+ ". Either the workflows stopped naming profiles, or this test is reading the wrong place.");

		List<String> unselected = new ArrayList<>();
		for (String testClass : testClasses()) {
			if (NOT_A_TEST_CLASS.containsKey(testClass)) {
				continue;
			}
			if (!isSelected(testClass, selections, profiles)) {
				unselected.add(testClass);
			}
		}

		assertEquals(List.of(), unselected,
				"These test classes are compiled but never executed: no profile the workflows activate includes "
						+ "them, and no -Dtest names them. Add them to the includes of the profile they belong to, "
						+ "or record them in NOT_A_TEST_CLASS when they are fixtures rather than tests.");
	}

	@Test
	@DisplayName("Every exempted file really is a fixture rather than a test")
	void everyExemptionStillExistsAndDeclaresNoTest() {
		for (Map.Entry<String, String> exemption : NOT_A_TEST_CLASS.entrySet()) {
			Path file = TEST_SOURCES.resolve(exemption.getKey());

			assertTrue(Files.isRegularFile(file),
					exemption.getKey() + " is exempted from test-profile coverage but no longer exists.");
			assertFalse(TEST_ANNOTATION.matcher(read(file)).find(),
					exemption.getKey() + " is exempted from test-profile coverage, but it declares a test. "
							+ "The exemption reads: " + exemption.getValue());
		}
	}

	@Test
	@DisplayName("A -Dtest is recognised in every form Surefire accepts")
	void recognisesEveryFormOfASelection() {
		String testClass = "de/tum/cit/ase/ares/testutilities/TestProfileCoverageTest.java";

		for (String selection : List.of("de.tum.cit.ase.ares.testutilities.TestProfileCoverageTest",
				"de.tum.cit.ase.ares.testutilities.*Test", "TestProfileCoverageTest", "*ProfileCoverageTest",
				"**/TestProfileCoverageTest.java", "de.tum.cit.ase.ares.testutilities.TestProfileCoverageTest#someTest",
				"de.tum.cit.ase.ares.testutilities.TestProfileCoverageTest$*")) {
			assertTrue(matchesClass(testClass, selection),
					"Surefire runs the class for -Dtest=" + selection + ", so this must count it as selected. "
							+ "Reporting a correctly configured workflow as a gap is the worse of the two mistakes "
							+ "a check like this can make.");
		}
		for (String selection : List.of("SomeOtherTest", "de.tum.cit.ase.ares.api.*Test", "**/SomeOtherTest.java")) {
			assertFalse(matchesClass(testClass, selection), selection + " does not name this class.");
		}
	}

	@Test
	@DisplayName("Every annotation that marks a test is detected in an exemption")
	void detectsEveryAnnotationThatMarksATest() {
		for (String annotation : List.of("@Test", "@ParameterizedTest", "@RepeatedTest", "@TestFactory",
				"@TestTemplate", "@Property", "@Example", "@PublicTest", "@HiddenTest", "@org.junit.jupiter.api.Test",
				"@org.junit.Test", "@net.jqwik.api.Property", "@de.tum.cit.ase.ares.api.jupiter.PublicTest")) {
			assertTrue(TEST_ANNOTATION.matcher("\t" + annotation + "\n\tvoid something() {}").find(),
					annotation + " marks a test, so a file carrying it is not a fixture.");
		}
		for (String notATest : List.of("@TestOnly", "@Testcontainers", "@DisplayName(\"Test\")")) {
			assertFalse(TEST_ANNOTATION.matcher(notATest).find(), notATest + " does not mark a test.");
		}
	}

	private static boolean isSelected(String testClass, Selections selections,
			Map<String, Map<String, List<String>>> profiles) {
		for (String profile : selections.profiles()) {
			Map<String, List<String>> configuration = profiles.get(profile);
			if (configuration == null || configuration.get("include").isEmpty()) {
				// A profile that configures no includes, such as coverage, narrows nothing
				// and therefore selects nothing on its own either.
				continue;
			}
			if (matchesAny(testClass, configuration.get("include"))
					&& !matchesAny(testClass, configuration.get("exclude"))) {
				return true;
			}
		}
		return selections.classes().stream().anyMatch(selection -> matchesClass(testClass, selection));
	}

	/** Matches an Ant path pattern against a path relative to the test sources. */
	private static boolean matchesAny(String testClass, List<String> patterns) {
		return patterns.stream().anyMatch(pattern -> testClass.matches(antPatternAsRegex(pattern)));
	}

	private static String antPatternAsRegex(String pattern) {
		StringBuilder regex = new StringBuilder();
		int index = 0;
		while (index < pattern.length()) {
			if (pattern.startsWith("**/", index)) {
				regex.append("(?:[^/]+/)*");
				index += 3;
			} else if (pattern.charAt(index) == '*') {
				regex.append("[^/]*");
				index++;
			} else {
				regex.append(Pattern.quote(String.valueOf(pattern.charAt(index))));
				index++;
			}
		}
		return regex.toString();
	}

	/**
	 * Matches one comma-separated entry of a {@code -Dtest} against a class.
	 * <p>
	 * Surefire accepts three forms of the same selection, and all three run the
	 * class: a fully qualified name, the simple name on its own, and a
	 * source-relative path ending in {@code .java}. Recognising only the first
	 * would make this test fail a workflow that is perfectly correct, which for a
	 * check whose whole purpose is to be trusted is the worse mistake of the two.
	 * <p>
	 * The method filter after {@code #} selects which tests of the class run rather
	 * than whether it runs, and {@code $*} selects its nested classes, so both are
	 * dropped before the comparison.
	 */
	private static boolean matchesClass(String testClass, String selection) {
		String wanted = selection.split("#", 2)[0].replace("$*", "").trim();
		if (wanted.isEmpty()) {
			return false;
		}
		if (wanted.endsWith(".java")) {
			return testClass.matches(antPatternAsRegex(wanted));
		}
		String qualified = testClass.substring(0, testClass.length() - ".java".length()).replace('/', '.');
		String candidate = wanted.contains(".") || wanted.contains("/") ? qualified
				: qualified.substring(qualified.lastIndexOf('.') + 1);
		return candidate.matches(dottedPatternAsRegex(wanted.replace('/', '.')));
	}

	private static String dottedPatternAsRegex(String pattern) {
		StringBuilder regex = new StringBuilder();
		for (char character : pattern.toCharArray()) {
			regex.append(character == '*' ? "[^.]*" : Pattern.quote(String.valueOf(character)));
		}
		return regex.toString();
	}

	/** The profiles and class selections every workflow step asks Surefire for. */
	private record Selections(Set<String> profiles, Set<String> classes) {
	}

	private static Selections selectionsOfTheWorkflows() {
		Set<String> profiles = new LinkedHashSet<>();
		Set<String> classes = new LinkedHashSet<>();
		for (Path workflow : workflowFiles()) {
			String content = read(workflow);
			Matcher profileFlags = PROFILE_FLAG.matcher(content);
			while (profileFlags.find()) {
				profiles.addAll(List.of(profileFlags.group(1).split(",")));
			}
			Matcher testFlags = TEST_FLAG.matcher(content);
			while (testFlags.find()) {
				String selection = testFlags.group(1) != null ? testFlags.group(1)
						: testFlags.group(2) != null ? testFlags.group(2) : testFlags.group(3);
				classes.addAll(List.of(selection.split(",")));
			}
		}
		return new Selections(profiles, classes);
	}

	private static List<Path> workflowFiles() {
		try (Stream<Path> entries = Files.list(WORKFLOWS)) {
			// Both suffixes, because GitHub Actions reads both. Missing one would hide
			// the selections that workflow makes and report its tests as unexecuted.
			return entries.filter(path -> path.getFileName().toString().endsWith(".yml")
					|| path.getFileName().toString().endsWith(".yaml")).sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not list " + WORKFLOWS, exception);
		}
	}

	/** The Surefire includes and excludes each profile of the POM declares. */
	private static Map<String, Map<String, List<String>>> surefireSelectionsOfThePom() {
		Map<String, Map<String, List<String>>> profiles = new LinkedHashMap<>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			NodeList profileNodes = factory.newDocumentBuilder().parse(POM.toFile()).getElementsByTagName("profile");
			for (int index = 0; index < profileNodes.getLength(); index++) {
				Element profile = (Element) profileNodes.item(index);
				String identifier = childText(profile, "id");
				if (identifier == null) {
					continue;
				}
				profiles.put(identifier, Map.of("include", surefirePatterns(profile, "include"), "exclude",
						surefirePatterns(profile, "exclude")));
			}
		} catch (Exception exception) {
			throw new IllegalStateException("Could not read the Surefire configuration from " + POM, exception);
		}
		return profiles;
	}

	/**
	 * The patterns Surefire is configured with in this profile. Read from the
	 * Surefire plugin only: JaCoCo declares includes of its own, over class names
	 * rather than paths, and reading those as test selections would report every
	 * profile as covering everything.
	 */
	private static List<String> surefirePatterns(Element profile, String tag) {
		List<String> patterns = new ArrayList<>();
		NodeList plugins = profile.getElementsByTagName("plugin");
		for (int index = 0; index < plugins.getLength(); index++) {
			Element plugin = (Element) plugins.item(index);
			if (!"maven-surefire-plugin".equals(childText(plugin, "artifactId"))) {
				continue;
			}
			NodeList entries = plugin.getElementsByTagName(tag);
			for (int entry = 0; entry < entries.getLength(); entry++) {
				patterns.add(entries.item(entry).getTextContent().trim());
			}
		}
		return List.copyOf(patterns);
	}

	private static String childText(Element parent, String tag) {
		NodeList children = parent.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (child.getNodeType() == Node.ELEMENT_NODE && tag.equals(child.getNodeName())) {
				return child.getTextContent().trim();
			}
		}
		return null;
	}

	/**
	 * Every file whose name marks it as a test class by Surefire's suffix
	 * conventions: {@code *Test.java}, {@code *Tests.java} and
	 * {@code *TestCase.java}. Matching only {@code *Test.java} would let a class in
	 * one of the other two forms stay outside every profile's selection without
	 * this guard reporting it. The {@code Test*.java} prefix is left out, because
	 * helper files such as {@code TestUserExtension.java} carry it without being
	 * tests.
	 */
	private static List<String> testClasses() {
		try (Stream<Path> entries = Files.walk(TEST_SOURCES)) {
			Set<String> classes = new HashSet<>();
			entries.filter(Files::isRegularFile).filter(TestProfileCoverageTest::isTestClassName)
					.forEach(path -> classes.add(TEST_SOURCES.relativize(path).toString().replace('\\', '/')));
			return classes.stream().sorted().toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not walk " + TEST_SOURCES, exception);
		}
	}

	/**
	 * Whether a file name is one Surefire selects as a test class by its suffix.
	 */
	private static boolean isTestClassName(Path path) {
		String name = path.getFileName().toString();
		return name.endsWith("Test.java") || name.endsWith("Tests.java") || name.endsWith("TestCase.java");
	}

	private static String read(Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + file, exception);
		}
	}
}
