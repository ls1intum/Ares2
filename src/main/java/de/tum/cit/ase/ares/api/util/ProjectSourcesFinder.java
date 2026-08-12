package de.tum.cit.ase.ares.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
	/** The filler that {@link #maskInactiveRegions} writes over inactive text. */
	private static final char MASKED = '\u0001';
	/**
	 * Where a source-directory operand ends: the statement or the collection does.
	 */
	private static final String OPERAND_TERMINATORS = ")]\n}";
	// Known gaps in the Gradle reading below, deliberately not covered: Kotlin's
	// setSrcDirs(...) and receiver chains such as sourceSets.main.java { ... },
	// lists spread over several lines, and any value that is computed rather than
	// written down. These are non-matches rather than misreadings, and an
	// undiscovered root costs the supervised scope nothing worse than a
	// fall-through to JavaProjectScanner's compiled-output step. That step is not a
	// guarantee either, because it reads the build tool's conventional output
	// directory rather than one taken from the descriptor, so a build that also
	// moves its output destination is not followed there.
	//
	// One case is answered rather than skipped: an assignment whose value cannot be
	// resolved leaves the source set empty, exactly as srcDirs = [] does. The two
	// are indistinguishable here, and answering the conventional root instead would
	// name a directory the descriptor has explicitly replaced.
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
		if (mode == BuildMode.MAVEN) {
			return new BuildToolConfiguration(mode, root, discoverMavenRoots(root, false),
					discoverMavenRoots(root, true), root.resolve(mode.getBuildDirectory()),
					root.resolve(mode.getTestBuildDirectory()));
		}
		GradleSourceRoots gradleRoots = discoverGradleRoots(root,
				gradleKotlin ? root.resolve("build.gradle.kts") : root.resolve("build.gradle"));
		return new BuildToolConfiguration(mode, root, gradleRoots.production(), gradleRoots.test(),
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

	/**
	 * The roots of both Gradle source sets, read in one pass over the descriptor.
	 *
	 * @param production the roots of the main source set
	 * @param test       the roots of the test source set
	 */
	private record GradleSourceRoots(List<Path> production, List<Path> test) {
	}

	/**
	 * Reads the source roots a Gradle descriptor declares.
	 * <p>
	 * The descriptor is neither evaluated nor fully parsed. It is masked so that
	 * comments and the contents of string literals cannot be mistaken for
	 * declarations, then walked once while a stack records the enclosing blocks. A
	 * declaration counts only inside {@code sourceSets}, then {@code main} or
	 * {@code test}, then {@code java}, which is what keeps a {@code resources}
	 * block, and a {@code main} block belonging to something other than a source
	 * set, out of the Java source roots. Reading both source sets together is what
	 * lets one occurrence belong to exactly one of them.
	 * <p>
	 * Gradle's own semantics decide what the declarations add up to. A source set
	 * starts at its conventional root; {@code srcDir}, {@code srcDirs(…)} and
	 * {@code srcDirs +=} add to it, and {@code srcDirs =} replaces whatever came
	 * before. Applying them in order is what makes {@code srcDirs += ['generated']}
	 * mean the conventional root <em>and</em> the generated one, rather than the
	 * generated one alone.
	 *
	 * @param root       the project root
	 * @param descriptor the build.gradle or build.gradle.kts to read
	 * @return the roots of both source sets, each possibly empty
	 */
	private static GradleSourceRoots discoverGradleRoots(Path root, Path descriptor) {
		String content;
		try {
			content = Files.readString(descriptor);
		} catch (IOException exception) {
			throw new IllegalStateException("Cannot read Gradle descriptor " + descriptor, exception);
		}
		String code = maskInactiveRegions(content, Objects.toString(descriptor.getFileName(), "").endsWith(".kts"));
		Map<String, String> properties = loadGradleProperties(root);
		// Read from the mask so that an assignment inside a comment or a string
		// cannot define a property, and taken from the original at the very same
		// offsets, because masking preserves every length.
		Matcher assignments = PROPERTY_ASSIGNMENT.matcher(code);
		while (assignments.find()) {
			properties.put(assignments.group(1), content.substring(assignments.start(2), assignments.end(2)));
		}
		Map<String, List<Path>> declared = new LinkedHashMap<>();
		Deque<String> blocks = new ArrayDeque<>();
		String pending = "";
		int index = 0;
		while (index < code.length()) {
			char current = code.charAt(index);
			if (current == '{') {
				blocks.push(pending);
				pending = "";
				index++;
			} else if (current == '}') {
				blocks.poll();
				pending = "";
				index++;
			} else if (isIdentifierStart(current)) {
				int end = index;
				while (end < code.length() && isIdentifierPart(code.charAt(end))) {
					end++;
				}
				String chain = code.substring(index, end);
				String identifier = lastSegmentOf(chain);
				if ("named".equals(identifier) || "getByName".equals(identifier)) {
					int close = code.indexOf(')', end);
					pending = close < 0 ? "" : unquote(content.substring(skipWhitespace(code, end) + 1, close).trim());
					index = close < 0 ? end : close + 1;
				} else if ("srcDir".equals(identifier) || "srcDirs".equals(identifier)) {
					index = readSourceDirectories(root, content, code, end, identifier, blocks, properties, declared);
				} else {
					pending = chain;
					index = end;
				}
			} else {
				index++;
			}
		}
		return new GradleSourceRoots(rootsOf(root, declared, "main"), rootsOf(root, declared, "test"));
	}

	/**
	 * Applies one {@code srcDir} or {@code srcDirs} declaration and returns the
	 * offset to continue from.
	 * <p>
	 * The operator decides the operation: {@code =} replaces, while {@code +=}, a
	 * parenthesised call and Groovy's bare {@code srcDir 'path'} all add. An
	 * occurrence outside a Java source set is skipped rather than attributed to
	 * one.
	 */
	private static int readSourceDirectories(Path root, String content, String code, int end, String identifier,
			Deque<String> blocks, Map<String, String> properties, Map<String, List<Path>> declared) {
		int operator = skipWhitespace(code, end);
		char next = operator < code.length() ? code.charAt(operator) : '\0';
		boolean replacing = next == '=';
		int operandStart;
		if (replacing) {
			operandStart = operator + 1;
		} else if (next == '+' && operator + 1 < code.length() && code.charAt(operator + 1) == '=') {
			operandStart = operator + 2;
		} else if (next == '(') {
			operandStart = operator + 1;
		} else if ("srcDir".equals(identifier) && (next == '\'' || next == '"' || isIdentifierStart(next))) {
			operandStart = operator;
		} else {
			return end;
		}
		int operandEnd = operandStart;
		while (operandEnd < code.length() && OPERAND_TERMINATORS.indexOf(code.charAt(operandEnd)) < 0) {
			operandEnd++;
		}
		String sourceSet = sourceSetOf(blocks);
		if (sourceSet != null) {
			List<Path> roots = declared.computeIfAbsent(sourceSet, name -> conventionalRootOf(root, name));
			if (replacing) {
				roots.clear();
			}
			for (String token : content.substring(operandStart, operandEnd).split(",")) {
				resolveGradlePath(token, properties)
						.ifPresent(value -> roots.add(validateSourceRoot(root, root.resolve(value))));
			}
		}
		return operandEnd;
	}

	/**
	 * Returns the source set a declaration at this point belongs to, or null when
	 * it belongs to none.
	 * <p>
	 * The blocks must nest as {@code sourceSets}, then {@code main} or
	 * {@code test}, then {@code java}, though not necessarily one immediately
	 * inside the next. Requiring {@code sourceSets} is what stops an unrelated
	 * block that happens to be called {@code main} from declaring a source root,
	 * and requiring {@code java} is what stops a {@code resources} block from doing
	 * so.
	 */
	private static String sourceSetOf(Deque<String> blocks) {
		List<String> path = new ArrayList<>();
		blocks.descendingIterator().forEachRemaining(block -> {
			for (String segment : block.split("\\.")) {
				path.add(segment);
			}
		});
		int sourceSets = path.indexOf("sourceSets");
		if (sourceSets < 0) {
			return null;
		}
		for (int outer = sourceSets + 1; outer < path.size(); outer++) {
			String name = path.get(outer);
			if ("main".equals(name) || "test".equals(name)) {
				return path.subList(outer + 1, path.size()).contains("java") ? name : null;
			}
		}
		return null;
	}

	/**
	 * The set a source set starts from: its conventional root, where the project
	 * has one. Gradle keeps it unless a declaration replaces it, so it is the base
	 * an additive declaration adds to rather than something to fall back on.
	 */
	private static List<Path> conventionalRootOf(Path root, String sourceSet) {
		List<Path> roots = new ArrayList<>();
		Path conventional = root.resolve("test".equals(sourceSet) ? DEFAULT_TEST_SOURCE : DEFAULT_PRODUCTION_SOURCE);
		if (Files.isDirectory(conventional)) {
			roots.add(validateSourceRoot(root, conventional));
		}
		return roots;
	}

	private static List<Path> rootsOf(Path root, Map<String, List<Path>> declared, String sourceSet) {
		return declared.computeIfAbsent(sourceSet, name -> conventionalRootOf(root, name)).stream().distinct().toList();
	}

	private static boolean isIdentifierStart(char character) {
		return Character.isLetter(character) || character == '_' || character == '$';
	}

	private static boolean isIdentifierPart(char character) {
		return isIdentifierStart(character) || Character.isDigit(character) || character == '.';
	}

	private static String lastSegmentOf(String chain) {
		int separator = chain.lastIndexOf('.');
		return separator < 0 ? chain : chain.substring(separator + 1);
	}

	private static String unquote(String value) {
		return value.length() > 1 && (value.charAt(0) == '\'' || value.charAt(0) == '"')
				? value.substring(1, value.length() - 1)
				: value;
	}

	private static int skipWhitespace(String code, int from) {
		int index = from;
		while (index < code.length() && Character.isWhitespace(code.charAt(index))) {
			index++;
		}
		return index;
	}

	/**
	 * Returns the descriptor with comments and the contents of string literals
	 * replaced by a filler character.
	 * <p>
	 * Lengths and line breaks are preserved and the quotes themselves are kept, so
	 * an offset in the mask is the same offset in the original: the mask decides
	 * what is code, and the original supplies the values.
	 * <p>
	 * This is what stops inactive text becoming a source root. A commented-out
	 * {@code srcDirs = ['old/path']}, or one inside a string as in
	 * {@code println "srcDirs = ..."}, used to be read as a declaration, and since
	 * a declared root that is not a directory is rejected, a descriptor Gradle
	 * accepts aborted discovery instead.
	 *
	 * @param content              the descriptor as written
	 * @param nestingBlockComments whether block comments nest, which they do in
	 *                             Kotlin and do not in Groovy
	 * @return the masked descriptor, of the same length as the original
	 */
	private static String maskInactiveRegions(String content, boolean nestingBlockComments) {
		char[] masked = content.toCharArray();
		int index = 0;
		while (index < masked.length) {
			char current = masked[index];
			char next = index + 1 < masked.length ? masked[index + 1] : '\0';
			if (current == '/' && next == '/') {
				index = maskUntilLineEnd(masked, index + 2);
			} else if (current == '/' && next == '*') {
				index = maskBlockComment(masked, index + 2, nestingBlockComments);
			} else if (current == '$' && next == '/') {
				index = maskUntil(masked, index + 2, "/$");
			} else if (current == '\'' || current == '"') {
				index = maskQuotedString(masked, index);
			} else if (current == '/' && startsAnExpression(masked, index)) {
				index = maskSlashyString(masked, index + 1);
			} else {
				index++;
			}
		}
		return new String(masked);
	}

	private static int maskUntilLineEnd(char[] masked, int from) {
		int index = from;
		while (index < masked.length && masked[index] != '\n') {
			masked[index] = MASKED;
			index++;
		}
		return index;
	}

	private static int maskBlockComment(char[] masked, int from, boolean nesting) {
		int index = from;
		int depth = 1;
		while (index < masked.length && depth > 0) {
			if (masked[index] == '*' && index + 1 < masked.length && masked[index + 1] == '/') {
				depth--;
				maskCharacter(masked, index);
				maskCharacter(masked, index + 1);
				index += 2;
				continue;
			}
			if (nesting && masked[index] == '/' && index + 1 < masked.length && masked[index + 1] == '*') {
				depth++;
				maskCharacter(masked, index);
				maskCharacter(masked, index + 1);
				index += 2;
				continue;
			}
			maskCharacter(masked, index);
			index++;
		}
		return index;
	}

	private static int maskUntil(char[] masked, int from, String terminator) {
		int index = from;
		while (index < masked.length) {
			if (masked[index] == terminator.charAt(0) && index + 1 < masked.length
					&& masked[index + 1] == terminator.charAt(1)) {
				return index + 2;
			}
			maskCharacter(masked, index);
			index++;
		}
		return index;
	}

	/**
	 * Masks the contents of a quoted string, single, double or triple. An
	 * unterminated single-line string ends at the line break rather than swallowing
	 * the rest of the descriptor, which would hide every declaration below it.
	 */
	private static int maskQuotedString(char[] masked, int start) {
		char quote = masked[start];
		boolean triple = start + 2 < masked.length && masked[start + 1] == quote && masked[start + 2] == quote;
		int index = start + (triple ? 3 : 1);
		while (index < masked.length) {
			if (masked[index] == '\\') {
				maskCharacter(masked, index);
				maskCharacter(masked, Math.min(index + 1, masked.length - 1));
				index += 2;
				continue;
			}
			if (masked[index] == quote && (!triple
					|| (index + 2 < masked.length && masked[index + 1] == quote && masked[index + 2] == quote))) {
				return index + (triple ? 3 : 1);
			}
			if (!triple && masked[index] == '\n') {
				return index;
			}
			maskCharacter(masked, index);
			index++;
		}
		return index;
	}

	/**
	 * Masks a Groovy slashy string, but only when it closes on the same line.
	 * Otherwise the slash is a division or a path separator that no quote covers,
	 * and treating it as a string opener would mask the rest of the descriptor.
	 */
	private static int maskSlashyString(char[] masked, int from) {
		int index = from;
		while (index < masked.length && masked[index] != '\n') {
			if (masked[index] == '\\') {
				index += 2;
				continue;
			}
			if (masked[index] == '/') {
				for (int position = from; position < index; position++) {
					maskCharacter(masked, position);
				}
				return index + 1;
			}
			index++;
		}
		return from;
	}

	/**
	 * Whether a slash at this offset can open a string rather than divide. Only a
	 * position where an expression may begin qualifies, which is what keeps an
	 * ordinary division from masking the code after it.
	 */
	private static boolean startsAnExpression(char[] masked, int slash) {
		for (int index = slash - 1; index >= 0; index--) {
			if (!Character.isWhitespace(masked[index])) {
				return "=([{,:;+-*%&|!<>?".indexOf(masked[index]) >= 0;
			}
		}
		return true;
	}

	private static void maskCharacter(char[] masked, int index) {
		if (masked[index] != '\n' && masked[index] != '\r') {
			masked[index] = MASKED;
		}
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
