package de.tum.cit.ase.ares.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;

/** Discovers Maven, Groovy-Gradle and Kotlin-Gradle project layouts. */
@API(status = Status.INTERNAL)
public final class ProjectSourcesFinder {
	private static final Logger LOG = LoggerFactory.getLogger(ProjectSourcesFinder.class);
	private static final String DEFAULT_PRODUCTION_SOURCE = "src/main/java";
	private static final String DEFAULT_TEST_SOURCE = "src/test/java";
	private static final Pattern PROPERTY_ASSIGNMENT = Pattern
			.compile("(?m)^\\s*(?:def|val|var)?\\s*([A-Za-z_][A-Za-z0-9_.-]*)\\s*=\\s*['\"]([^'\"]+)['\"]");
	private static final Pattern SOURCE_DIRECTORY = Pattern
			.compile("(?:srcDir\\s*(?:\\(\\s*)?|srcDirs\\s*(?:=|\\()\\s*)([^)\\]\\n}]+)");
	private static String pomXmlPath = "pom.xml";
	private static String buildGradlePath = "build.gradle";

	private ProjectSourcesFinder() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "ProjectSourcesFinder"));
	}

	public static BuildToolConfiguration discover(Path projectRoot) {
		return discover(projectRoot, null);
	}

	public static BuildToolConfiguration discover(Path projectRoot, BuildMode explicitlySelectedMode) {
		Path root = BuildToolConfiguration
				.canonicalise(Objects.requireNonNull(projectRoot, "projectRoot must not be null"));
		if (!Files.isDirectory(root)) {
			throw new IllegalArgumentException("Project root is not a directory: " + projectRoot);
		}
		boolean maven = Files.isRegularFile(root.resolve("pom.xml"));
		boolean gradleGroovy = Files.isRegularFile(root.resolve("build.gradle"));
		boolean gradleKotlin = Files.isRegularFile(root.resolve("build.gradle.kts"));
		boolean gradle = gradleGroovy || gradleKotlin;
		BuildMode mode = explicitlySelectedMode;
		if (mode == null && maven && gradle) {
			throw new IllegalStateException(
					"Ambiguous project: both Maven and Gradle descriptors are active in " + root);
		}
		if (mode == null && !maven && !gradle) {
			throw new IllegalStateException(
					"Unsupported project: no pom.xml, build.gradle or build.gradle.kts in " + root);
		}
		if (mode == null) {
			mode = maven ? BuildMode.MAVEN : BuildMode.GRADLE;
		}
		if (mode == BuildMode.MAVEN && !maven) {
			throw new IllegalStateException("Maven was selected but pom.xml is absent in " + root);
		}
		if (mode == BuildMode.GRADLE && !gradle) {
			throw new IllegalStateException("Gradle was selected but no Gradle descriptor is present in " + root);
		}
		List<Path> productionRoots = mode == BuildMode.MAVEN ? discoverMavenRoots(root, false)
				: discoverGradleRoots(root,
						gradleKotlin ? root.resolve("build.gradle.kts") : root.resolve("build.gradle"), false);
		List<Path> testRoots = mode == BuildMode.MAVEN ? discoverMavenRoots(root, true)
				: discoverGradleRoots(root,
						gradleKotlin ? root.resolve("build.gradle.kts") : root.resolve("build.gradle"), true);
		return new BuildToolConfiguration(mode, root, productionRoots, testRoots,
				root.resolve(mode.getBuildDirectory()), root.resolve(mode.getTestBuildDirectory()));
	}

	private static List<Path> discoverMavenRoots(Path root, boolean tests) {
		String elementName = tests ? "testSourceDirectory" : "sourceDirectory";
		String defaultRoot = tests ? DEFAULT_TEST_SOURCE : DEFAULT_PRODUCTION_SOURCE;
		List<Path> roots = new ArrayList<>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			NodeList nodes = factory.newDocumentBuilder().parse(root.resolve("pom.xml").toFile())
					.getElementsByTagName(elementName);
			for (int index = 0; index < nodes.getLength(); index++) {
				String value = nodes.item(index).getTextContent();
				if (value != null && !value.isBlank()) {
					roots.add(validateSourceRoot(root, substituteMavenRoot(value.trim(), root)));
				}
			}
		} catch (Exception exception) {
			throw new IllegalStateException("Cannot parse Maven source roots from " + root.resolve("pom.xml"),
					exception);
		}
		if (roots.isEmpty() && Files.isDirectory(root.resolve(defaultRoot))) {
			roots.add(validateSourceRoot(root, root.resolve(defaultRoot)));
		}
		return List.copyOf(roots);
	}

	private static Path substituteMavenRoot(String value, Path root) {
		return Path.of(value.replace("${project.basedir}", root.toString()).replace("${basedir}", root.toString()));
	}

	private static List<Path> discoverGradleRoots(Path root, Path descriptor, boolean tests) {
		String defaultRoot = tests ? DEFAULT_TEST_SOURCE : DEFAULT_PRODUCTION_SOURCE;
		List<Path> roots = new ArrayList<>();
		Map<String, String> properties = loadGradleProperties(root);
		try {
			String content = Files.readString(descriptor);
			Matcher assignments = PROPERTY_ASSIGNMENT.matcher(content);
			while (assignments.find()) {
				properties.put(assignments.group(1), assignments.group(2));
			}
			String[] lines = content.split("\\R");
			String sourceSet = "";
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.matches("(?:getByName\\(\\\"|named\\(\\\")[^\"]+.*")
						|| trimmed.matches("(?:main|test)\\s*\\{.*")) {
					sourceSet = trimmed.contains("test") ? "test" : "main";
				}
				Matcher matcher = SOURCE_DIRECTORY.matcher(stripLineComment(line));
				while (matcher.find()) {
					if ((tests && "test".equals(sourceSet)) || (!tests && !"test".equals(sourceSet))) {
						for (String token : matcher.group(1).split(",")) {
							resolveGradlePath(token, properties)
									.ifPresent(value -> roots.add(validateSourceRoot(root, root.resolve(value))));
						}
					}
				}
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Cannot read Gradle descriptor " + descriptor, exception);
		}
		if (roots.isEmpty() && Files.isDirectory(root.resolve(defaultRoot))) {
			roots.add(validateSourceRoot(root, root.resolve(defaultRoot)));
		}
		return roots.stream().distinct().toList();
	}

	private static Map<String, String> loadGradleProperties(Path root) {
		Map<String, String> properties = new LinkedHashMap<>();
		Path file = root.resolve("gradle.properties");
		if (!Files.isRegularFile(file)) {
			return properties;
		}
		try {
			for (String line : Files.readAllLines(file)) {
				int separator = line.indexOf('=');
				if (separator > 0 && !line.stripLeading().startsWith("#")) {
					properties.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
				}
			}
			return properties;
		} catch (IOException exception) {
			throw new IllegalStateException("Cannot read " + file, exception);
		}
	}

	private static Optional<String> resolveGradlePath(String token, Map<String, String> properties) {
		String value = token.trim().replace("[", "").replace("]", "").replace("files(", "").trim();
		while (value.endsWith(")")) {
			value = value.substring(0, value.length() - 1).trim();
		}
		if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
			return Optional.of(value.substring(1, value.length() - 1));
		}
		return Optional.ofNullable(properties.get(value));
	}

	private static String stripLineComment(String line) {
		int index = line.indexOf("//");
		return index < 0 ? line : line.substring(0, index);
	}

	private static Path validateSourceRoot(Path root, Path candidate) {
		Path canonical = BuildToolConfiguration
				.canonicalise(candidate.isAbsolute() ? candidate : root.resolve(candidate));
		if (!canonical.startsWith(root)) {
			throw new SecurityException("Configured source root escapes project root: " + candidate);
		}
		if (!Files.isDirectory(canonical)) {
			throw new IllegalStateException("Configured source root is not a directory: " + candidate);
		}
		return canonical;
	}

	public static Optional<Path> findProjectSourcesPath() {
		// Compatibility adapter for ClassNameScanner's configurable descriptor paths.
		// Modern enforcement always uses discover(projectRoot, mode), whose paths are
		// canonical and confined. This legacy API deliberately returns the descriptor's
		// source string relative to the current exercise workspace, as its callers have
		// historically supplied fixture descriptors located outside that workspace.
		if (isMavenProject()) {
			return findLegacyMavenSource();
		}
		if (isGradleProject()) {
			return findLegacyGradleSource();
		}
		LOG.error("Could not find any build file. Contact your instructor.");
		return Optional.empty();
	}

	private static Optional<Path> findLegacyMavenSource() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			NodeList buildNodes = factory.newDocumentBuilder().parse(Path.of(pomXmlPath).toFile())
					.getElementsByTagName("build");
			for (int index = 0; index < buildNodes.getLength(); index++) {
				Node buildNode = buildNodes.item(index);
				if (buildNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				NodeList sources = ((Element) buildNode).getElementsByTagName("sourceDirectory");
				if (sources.getLength() > 0) {
					String source = sources.item(0).getTextContent();
					if (source != null && !source.isBlank()) {
						return Optional.of(Path.of(stripBasedirPrefix(source.trim())));
					}
				}
			}
			return Files.isDirectory(Path.of(DEFAULT_PRODUCTION_SOURCE))
					? Optional.of(Path.of(DEFAULT_PRODUCTION_SOURCE))
					: Optional.empty();
		} catch (Exception exception) {
			LOG.error("Could not retrieve the source directory from the pom.xml file. Contact your instructor.",
					exception);
			return Optional.empty();
		}
	}

	private static Optional<Path> findLegacyGradleSource() {
		try {
			Matcher matcher = PROPERTY_ASSIGNMENT.matcher(Files.readString(Path.of(buildGradlePath)));
			while (matcher.find()) {
				if ("assignmentSrcDir".equals(matcher.group(1))) {
					return Optional.of(Path.of(matcher.group(2)));
				}
			}
			return Optional.empty();
		} catch (IOException exception) {
			LOG.error("Could not retrieve the source directory from the build.gradle file. Contact your instructor.",
					exception);
			return Optional.empty();
		}
	}

	private static String stripBasedirPrefix(String source) {
		for (String prefix : List.of("${project.basedir}/", "${basedir}/")) {
			if (source.startsWith(prefix)) {
				return source.substring(prefix.length());
			}
		}
		return source;
	}

	public static boolean isMavenProject() {
		return pomXmlPath != null && new File(pomXmlPath).isFile();
	}

	public static boolean isGradleProject() {
		if (buildGradlePath == null) {
			return false;
		}
		return new File(buildGradlePath).isFile() || new File(buildGradlePath + ".kts").isFile();
	}

	public static String getPomXmlPath() {
		return pomXmlPath;
	}

	public static void setPomXmlPath(String path) {
		pomXmlPath = path;
	}

	public static String getBuildGradlePath() {
		return buildGradlePath;
	}

	public static void setBuildGradlePath(String path) {
		buildGradlePath = path;
	}
}
