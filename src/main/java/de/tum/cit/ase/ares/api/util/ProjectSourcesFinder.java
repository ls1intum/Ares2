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
	/**
	 * The filler {@link #maskInactiveRegions} writes over a comment, its delimiters
	 * included.
	 * <p>
	 * Kept distinct from {@link #STRING_MASK} because the two are treated
	 * differently when an operand token is rebuilt: a comment is dropped, while a
	 * string literal still supplies its value. Masking the delimiters too is what
	 * lets "copy every position that is not commented out" produce the text Gradle
	 * would have seen.
	 */
	private static final char COMMENT_MASK = '\u0001';
	/**
	 * The filler {@link #maskInactiveRegions} writes over the body of a string
	 * literal, excluding its quotes.
	 * <p>
	 * The body is masked so its contents cannot be mistaken for code; the quotes
	 * are kept so a rebuilt token still looks like the literal it is.
	 */
	private static final char STRING_MASK = '\u0002';
	/**
	 * Where a source-directory operand ends: the statement or the collection does.
	 */
	private static final String OPERAND_TERMINATORS = ")]\n}";
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
					root.resolve(mode.getTestBuildDirectory()), true);
		}
		GradleSourceRoots gradleRoots = discoverGradleRoots(root,
				gradleKotlin ? root.resolve("build.gradle.kts") : root.resolve("build.gradle"));
		return new BuildToolConfiguration(mode, root, gradleRoots.production(), gradleRoots.test(),
				root.resolve(mode.getBuildDirectory()), root.resolve(mode.getTestBuildDirectory()),
				gradleRoots.productionComplete());
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
	private record GradleSourceRoots(List<Path> production, List<Path> test, boolean productionComplete) {
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
	 * generated one alone. <b>Known gaps, deliberately not covered:</b> Kotlin's
	 * {@code setSrcDirs(…)}, receiver chains such as {@code sourceSets.main.java {
	 * … }}, and any computed value. None is silent: a declaration this reader
	 * cannot resolve marks the source set incomplete, and that travels to the
	 * consumer through {@link BuildToolConfiguration#productionRootsComplete()}, so
	 * the roots are still offered but no longer as the whole project. That matters
	 * because {@code srcDirs =} clears the conventional root before its operand is
	 * read, so an unresolvable value left the source set empty and
	 * indistinguishable from a project declaring no sources. {@code srcDirs = []}
	 * stays what it says: known, complete and empty. Properties are read from the
	 * mask so an assignment inside a comment or a string cannot define one, and
	 * taken from the original at the same offsets, which works because masking
	 * preserves every length.
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
		Matcher assignments = PROPERTY_ASSIGNMENT.matcher(code);
		while (assignments.find()) {
			properties.put(assignments.group(1), content.substring(assignments.start(2), assignments.end(2)));
		}
		Map<String, List<Path>> declared = new LinkedHashMap<>();
		Map<String, Boolean> complete = new LinkedHashMap<>();
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
					index = readSourceDirectories(root, content, code, end, identifier, blocks, properties, declared,
							complete);
				} else {
					pending = chain;
					index = end;
				}
			} else {
				index++;
			}
		}
		boolean productionComplete = complete.getOrDefault("main", Boolean.TRUE);
		if (!productionComplete) {
			LOG.warn("The main source set in {} declares a source root this reader cannot resolve, so the discovered "
					+ "production roots are not known to be the whole of it. The supervised package will not be "
					+ "derived from them.", descriptor);
		}
		return new GradleSourceRoots(rootsOf(root, declared, "main"), rootsOf(root, declared, "test"),
				productionComplete);
	}

	/**
	 * Applies one {@code srcDir} or {@code srcDirs} declaration and returns the
	 * offset to continue from.
	 * <p>
	 * The operator decides the operation: {@code =} replaces, while {@code +=}, a
	 * parenthesised call and Groovy's bare {@code srcDir 'path'} all add. An
	 * occurrence outside a Java source set is skipped rather than attributed to
	 * one.
	 * <p>
	 * A parenthesised call is pointed AT rather than past, so the opening delimiter
	 * stays visible and {@code srcDirs(…)} is recognised as a bracketed operand at
	 * all. An unbalanced collection cannot be apportioned and is unresolved; a
	 * token that is only whitespace or a comment is skipped, an empty list being
	 * empty rather than unreadable. A replacement supersedes whatever uncertainty
	 * preceded it, so it can restore completeness as well as remove it, whereas an
	 * addition can only ever make the picture less complete.
	 */
	private static int readSourceDirectories(Path root, String content, String code, int end, String identifier,
			Deque<String> blocks, Map<String, String> properties, Map<String, List<Path>> declared,
			Map<String, Boolean> complete) {
		int operator = skipInactive(code, end);
		char next = operator < code.length() ? code.charAt(operator) : '\0';
		boolean replacing = next == '=';
		int operandStart;
		if (replacing) {
			operandStart = skipInactive(code, operator + 1);
		} else if (next == '+' && operator + 1 < code.length() && code.charAt(operator + 1) == '=') {
			operandStart = skipInactive(code, operator + 2);
		} else if (next == '(') {
			operandStart = operator;
		} else if ("srcDir".equals(identifier) && (next == '\'' || next == '"' || isIdentifierStart(next))) {
			operandStart = operator;
		} else {
			return end;
		}
		char opener = operandStart < code.length() ? code.charAt(operandStart) : '\0';
		boolean bracketed = opener == '[' || opener == '(';
		boolean resolvedEverything = true;
		int tokensStart;
		int tokensEnd;
		int resumeAt;
		if (bracketed) {
			int close = matchingClose(code, operandStart);
			if (close < 0) {
				tokensStart = operandStart + 1;
				tokensEnd = code.length();
				resumeAt = code.length();
				resolvedEverything = false;
			} else {
				tokensStart = operandStart + 1;
				tokensEnd = close;
				resumeAt = close + 1;
				resolvedEverything = endsStatement(code, close + 1);
			}
		} else {
			tokensStart = operandStart;
			tokensEnd = operandStart;
			while (tokensEnd < code.length() && OPERAND_TERMINATORS.indexOf(code.charAt(tokensEnd)) < 0) {
				tokensEnd++;
			}
			resumeAt = tokensEnd;
		}
		List<String> values = new ArrayList<>();
		for (int[] span : splitTopLevel(code, tokensStart, tokensEnd)) {
			String token = activeText(content, code, span[0], span[1]).trim();
			if (token.isEmpty()) {
				continue;
			}
			Optional<String> value = resolveGradlePath(token, properties);
			if (value.isPresent()) {
				values.add(value.get());
			} else {
				resolvedEverything = false;
			}
		}
		String sourceSet = sourceSetOf(blocks);
		if (sourceSet != null) {
			List<Path> roots = declared.computeIfAbsent(sourceSet, name -> conventionalRootOf(root, name));
			complete.putIfAbsent(sourceSet, Boolean.TRUE);
			if (replacing) {
				roots.clear();
				complete.put(sourceSet, resolvedEverything);
			} else if (!resolvedEverything) {
				complete.put(sourceSet, Boolean.FALSE);
			}
			for (String value : values) {
				roots.add(validateSourceRoot(root, root.resolve(value)));
			}
		}
		return resumeAt;
	}

	/**
	 * The offset of the delimiter closing the collection or call opening at
	 * {@code open}, or -1 when it is never closed.
	 * <p>
	 * Depth is counted on the mask, so a bracket inside a comment or a string does
	 * not count, and the search deliberately crosses line breaks: a list written
	 * over several lines is ordinary Gradle, and stopping at the first newline is
	 * what used to leave a replaced source set empty.
	 */
	private static int matchingClose(String code, int open) {
		char opener = code.charAt(open);
		char closer = opener == '[' ? ']' : ')';
		int depth = 0;
		for (int index = open; index < code.length(); index++) {
			char current = code.charAt(index);
			if (current == opener) {
				depth++;
			} else if (current == closer) {
				depth--;
				if (depth == 0) {
					return index;
				}
			}
		}
		return -1;
	}

	/**
	 * The comma-separated spans of an operand, split only where a comma sits at the
	 * operand's own nesting depth.
	 * <p>
	 * {@code [someFunction('a', 'b'), 'third']} is two entries rather than three:
	 * the inner comma separates arguments, not source roots.
	 */
	private static List<int[]> splitTopLevel(String code, int start, int end) {
		List<int[]> spans = new ArrayList<>();
		int depth = 0;
		int spanStart = start;
		for (int index = start; index < end; index++) {
			char current = code.charAt(index);
			if (current == '[' || current == '(') {
				depth++;
			} else if (current == ']' || current == ')') {
				depth--;
			} else if (current == ',' && depth == 0) {
				spans.add(new int[] { spanStart, index });
				spanStart = index + 1;
			}
		}
		spans.add(new int[] { spanStart, end });
		return spans;
	}

	/**
	 * The text Gradle would have seen in this span: the original characters, minus
	 * everything commented out.
	 * <p>
	 * The mask decides what is inactive and the original supplies the values, which
	 * works only because masking preserves every offset. Dropping comments here is
	 * what lets one sit inside a live declaration without invalidating the path
	 * beside it.
	 */
	private static String activeText(String content, String code, int start, int end) {
		StringBuilder active = new StringBuilder(end - start);
		for (int index = start; index < end; index++) {
			if (code.charAt(index) != COMMENT_MASK) {
				active.append(content.charAt(index));
			}
		}
		return active.toString();
	}

	/**
	 * Whether the declaration is over at this offset rather than continuing into
	 * more of the same expression.
	 * <p>
	 * Only a definite boundary counts, because guessing the other way is what makes
	 * a partly read declaration look complete. A statement separator, the close of
	 * an enclosing block or call, a following statement and the end of the
	 * descriptor all end it; anything else continues it, which covers
	 * {@code + generatedRoots}, {@code .collect { file(it) }}, indexing and spread
	 * without having to predict them.
	 * <p>
	 * A line break settles nothing on its own, since an expression may continue on
	 * the next line in both Groovy and Kotlin, so the first active character beyond
	 * it is what decides.
	 */
	private static boolean endsStatement(String code, int from) {
		int index = skipInactive(code, from);
		if (index >= code.length()) {
			return true;
		}
		char current = code.charAt(index);
		return current == ';' || current == '}' || current == ')' || current == ']' || isIdentifierStart(current);
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
	 * Skips what Gradle would have skipped: whitespace, and anything commented out.
	 * <p>
	 * Used where an operator or an operand is expected, so that a comment written
	 * between a declaration and its value, as in
	 * {@code srcDirs / * why * / = [...]}, does not hide the operator behind it.
	 */
	private static int skipInactive(String code, int from) {
		int index = from;
		while (index < code.length()
				&& (Character.isWhitespace(code.charAt(index)) || code.charAt(index) == COMMENT_MASK)) {
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
	 * @param content              the descriptor as written A comment's opening
	 *                             delimiter is masked along with its body, or a
	 *                             rebuilt token would still carry the {@code //}.
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
				maskCharacter(masked, index, COMMENT_MASK);
				maskCharacter(masked, index + 1, COMMENT_MASK);
				index = maskUntilLineEnd(masked, index + 2);
			} else if (current == '/' && next == '*') {
				maskCharacter(masked, index, COMMENT_MASK);
				maskCharacter(masked, index + 1, COMMENT_MASK);
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
			masked[index] = COMMENT_MASK;
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
				maskCharacter(masked, index, COMMENT_MASK);
				maskCharacter(masked, index + 1, COMMENT_MASK);
				index += 2;
				continue;
			}
			if (nesting && masked[index] == '/' && index + 1 < masked.length && masked[index + 1] == '*') {
				depth++;
				maskCharacter(masked, index, COMMENT_MASK);
				maskCharacter(masked, index + 1, COMMENT_MASK);
				index += 2;
				continue;
			}
			maskCharacter(masked, index, COMMENT_MASK);
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
			maskCharacter(masked, index, STRING_MASK);
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
				maskCharacter(masked, index, STRING_MASK);
				maskCharacter(masked, Math.min(index + 1, masked.length - 1), STRING_MASK);
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
			maskCharacter(masked, index, STRING_MASK);
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
					maskCharacter(masked, position, STRING_MASK);
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

	private static void maskCharacter(char[] masked, int index, char filler) {
		if (masked[index] != '\n' && masked[index] != '\r') {
			masked[index] = filler;
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

	/**
	 * The directory a source-root token names, or empty when this reader cannot say
	 * so statically.
	 * <p>
	 * Empty is not a neutral answer. The caller records it as an incomplete source
	 * set, because a value that cannot be read is a root that may exist and has not
	 * been found, which is a different thing from a source set that is genuinely
	 * empty. Three bodies are unreadable: one carrying its own delimiter, which is
	 * the wreckage of several literals rather than one, as {@code files('a', 'b')}
	 * collapses to once its wrapper is stripped; a double-quoted string, which
	 * interpolates in both Groovy and Kotlin, so taking it literally would name a
	 * directory such as {@code $generatedRoot} that normally does not exist and is
	 * rejected, yet would silently pass for a project that happens to contain one;
	 * and an escape this reader does not decode.
	 */
	private static Optional<String> resolveGradlePath(String token, Map<String, String> properties) {
		String value = token.trim().replace("[", "").replace("]", "").replace("files(", "").trim();
		while (value.endsWith(")")) {
			value = value.substring(0, value.length() - 1).trim();
		}
		if (value.length() > 1 && value.charAt(0) == '"' && value.endsWith("\"")) {
			String literal = value.substring(1, value.length() - 1);
			if (literal.indexOf('"') >= 0) {
				return Optional.empty();
			}
			return isStaticLiteral(literal) ? Optional.of(literal) : Optional.empty();
		}
		if (value.length() > 1 && value.charAt(0) == '\'' && value.endsWith("'")) {
			String literal = value.substring(1, value.length() - 1);
			return literal.indexOf('\'') < 0 && literal.indexOf('\\') < 0 ? Optional.of(literal) : Optional.empty();
		}
		String resolved = properties.get(value);
		return resolved != null && isStaticLiteral(resolved) ? Optional.of(resolved) : Optional.empty();
	}

	/**
	 * Whether a string literal's body is a fixed path rather than something this
	 * reader would have to evaluate. Interpolation and undecoded escapes both mean
	 * it is not.
	 */
	private static boolean isStaticLiteral(String literal) {
		return literal.indexOf('$') < 0 && literal.indexOf('\\') < 0;
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
