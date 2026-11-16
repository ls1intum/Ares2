package de.tum.cit.ase.ares.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
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
	/**
	 * A {@code def}, {@code val} or {@code var} given a string literal, as the
	 * legacy {@link #findProjectSourcesPath()} reads it from the unmasked file.
	 */
	private static final Pattern LEGACY_PROPERTY_ASSIGNMENT = Pattern
			.compile("(?m)^\\s*(?:def|val|var)?\\s*([A-Za-z_][A-Za-z0-9_.-]*)\\s*=\\s*['\"]([^'\"]+)['\"]");
	/**
	 * A name given one string literal as its whole value on its own line, such as
	 * {@code def dir = 'a'}. Matched on masked code.
	 */
	private static final Pattern PROPERTY_ASSIGNMENT = Pattern.compile(
			"(?m)^[ \\t]*(?:def|val|var)?[ \\t]*([A-Za-z_][A-Za-z0-9_.-]*)[ \\t]*=[ \\t]*['\"]([^'\"\\n]+)['\"][ \\t\\r\\u0001;]*$");
	/**
	 * Any assignment to a name, plain or compound such as {@code +=}, wherever it
	 * stands, so that a name given more than one value is not read as a fixed one.
	 */
	private static final Pattern ASSIGNED_NAME = Pattern.compile(
			"(?<![A-Za-z0-9_$.])([A-Za-z_][A-Za-z0-9_.]*)[ \\t]*(?::[ \\t]*[A-Za-z0-9_.<>?, ]+)?[-+*/%]?=(?!=)");
	/**
	 * Any mention of {@code group} as a name in Gradle code, qualified or not.
	 * Matched on masked code, so a mention inside a comment or a string does not
	 * count, and {@code groupId} is a different name.
	 */
	private static final Pattern GROUP_MENTION = Pattern.compile("(?<![A-Za-z0-9_$])group(?![A-Za-z0-9_$])");
	/**
	 * The other ways Gradle code reaches the group: its accessors, and the name as
	 * a string, as in {@code setProperty('group', ...)} or {@code project."group"}.
	 * Matched on the original text; the mask decides whether a match is code.
	 */
	private static final Pattern GROUP_BY_OTHER_MEANS = Pattern
			.compile("(?<![A-Za-z0-9_$])[sg]etGroup(?![A-Za-z0-9_$])|(['\"])group\\1");
	/**
	 * A {@code return}, which at the top of a script skips every statement after
	 * it. Matched on masked code, so one in a comment or a string does not count.
	 */
	private static final Pattern RETURN = Pattern.compile("(?<![A-Za-z0-9_$])return(?![A-Za-z0-9_$])");
	/**
	 * The name {@code project} where it is not a member of something else, as
	 * {@code task.project} is. Matched on masked code.
	 */
	private static final Pattern PROJECT_NAME = Pattern.compile("(?<![A-Za-z0-9_$.])project(?![A-Za-z0-9_$])");
	/**
	 * Words that may open a line yet continue the expression of the line before, as
	 * {@code as} does in Groovy and Kotlin.
	 */
	private static final Set<String> CONTINUING_WORDS = Set.of("as", "in", "is", "instanceof");
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
	 * The name the Gradle reader puts in place of a call that reaches another
	 * project, such as {@code subprojects}, or {@code project(':x')} in the root
	 * project, so that a source set changed there is never taken for this
	 * project's.
	 */
	private static final String OTHER_PROJECT = "?project";
	/**
	 * The name the Gradle reader puts in place of {@code parent} used as a
	 * receiver: another project, whose {@code allprojects} and {@code subprojects}
	 * still include this one.
	 */
	private static final String ANCESTOR = "?ancestor";
	/**
	 * The name the Gradle reader puts in place of {@code rootProject} used as a
	 * receiver where it may or may not be this project, and after a call given a
	 * project as an argument, as in {@code with(rootProject)}, whose block may then
	 * configure that project.
	 */
	private static final String MAYBE_THIS_PROJECT = "?maybe-this-project";
	/**
	 * The name the Gradle reader puts in place of {@code project(...)} or
	 * {@code findProject(...)} where the project found may be this one, such as
	 * {@code project(':')}, and of a {@code rootProject} reached through such
	 * receivers. Unlike {@link #MAYBE_THIS_PROJECT}, it is never dropped before
	 * {@code allprojects}, whose block then may not run here.
	 */
	private static final String LOOKED_UP_PROJECT = "?looked-up-project";
	/**
	 * The name the Gradle reader adds where a statement assigns, so that a block
	 * opened after it, a closure or lambda stored for later, counts as deferred.
	 */
	private static final String ASSIGNED = "?assigned";
	/**
	 * The name the Gradle reader adds where a block is the body of a method, such
	 * as {@code int unused() {}}, which runs only when something calls it.
	 */
	private static final String METHOD_BODY = "?method";
	/**
	 * The name the Gradle reader puts at {@code &&}, {@code ||}, {@code ?:}, a
	 * ternary {@code ?} and its {@code :}, after which the rest of the expression
	 * may never run. The names before it belong to another operand, so it replaces
	 * them. It is also added at a safe call {@code ?.} on {@code parent} or on a
	 * {@code rootProject} that may be this project.
	 */
	private static final String SHORT_CIRCUIT = "?short-circuit";
	/**
	 * The name the Gradle reader adds where code that follows may run later or
	 * never: after a lookup whose configuration action Gradle runs only once the
	 * element is created, which is any {@code register}, and {@code named} on
	 * anything but {@code sourceSets}, whose main and test exist from the start;
	 * and after a declaration, where a block that follows is a path Gradle
	 * evaluates when it needs it.
	 */
	private static final String DEFERRED = "?deferred";
	/** A {@code throws} clause between a parameter list and a method body. */
	private static final Pattern THROWS_CLAUSE = Pattern
			.compile("throws\\s+[A-Za-z_$][A-Za-z0-9_$.]*(?:\\s*,\\s*[A-Za-z_$][A-Za-z0-9_$.]*)*");
	/**
	 * Words before a call and its block that do not make the call a method
	 * declaration.
	 */
	private static final Set<String> NOT_TYPES = Set.of("task", "else", "return", "new", "throw", "in", "is", "as",
			"case", "assert", "println", "print");
	/**
	 * Names that make the code after them conditional or deferred: a branch, a
	 * loop, a handler, a {@code try} an exception may leave, a short-circuiting
	 * operator, the body of a definition, a callback Gradle runs later, or a
	 * project that may not be this one. A declaration under one may never run here,
	 * or run after code written below it.
	 */
	private static final Set<String> CONDITIONS = Set.of("if", "else", "for", "while", "switch", "when", "catch", "do",
			"try", "def", "fun", "void", ASSIGNED, METHOD_BODY, SHORT_CIRCUIT, DEFERRED, MAYBE_THIS_PROJECT,
			LOOKED_UP_PROJECT, "afterEvaluate", "beforeEvaluate", "projectsEvaluated", "whenReady", "doFirst", "doLast",
			"whenTaskAdded", "whenObjectAdded", "provider");
	/** Words that, before a name, declare it as a local variable or function. */
	private static final Set<String> DECLARING_WORDS = Set.of("def", "val", "var", "fun", "final", "static", "private",
			"protected", "public");
	/** How deeply nested an operand this reader works through before giving up. */
	private static final int MAX_NESTING = 64;
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
			MavenSourceRoots production = discoverMavenRoots(root, false);
			warnIfProductionRootsArePartial(production.complete(), root.resolve("pom.xml"), List.of());
			return new BuildToolConfiguration(mode, root, production.roots(), discoverMavenRoots(root, true).roots(),
					root.resolve(mode.getBuildDirectory()), root.resolve(mode.getTestBuildDirectory()),
					production.complete());
		}
		GradleSourceRoots gradleRoots = discoverGradleRoots(root, gradleDescriptorOf(root));
		return new BuildToolConfiguration(mode, root, gradleRoots.production(), gradleRoots.test(),
				root.resolve(mode.getBuildDirectory()), root.resolve(mode.getTestBuildDirectory()),
				gradleRoots.productionComplete());
	}

	/**
	 * The source roots a Maven descriptor answers with for one source set, and
	 * whether they are the whole of it.
	 *
	 * @param roots    the roots, declared or conventional
	 * @param complete whether they are known to be all of them
	 */
	private record MavenSourceRoots(List<Path> roots, boolean complete) {
	}

	/**
	 * Reads the source roots a Maven descriptor declares for one source set.
	 * <p>
	 * A declaration replaces the convention, so the conventional root answers only
	 * where nothing was declared. That is what stops a root passed over for being
	 * absent from reactivating the directory the descriptor replaced.
	 *
	 * @param root  the project root
	 * @param tests whether to read the test source set
	 * @return the roots and whether they are the whole source set
	 */
	private static MavenSourceRoots discoverMavenRoots(Path root, boolean tests) {
		Path descriptor = root.resolve("pom.xml");
		NodeList nodes = readMavenElements(descriptor, tests ? "testSourceDirectory" : "sourceDirectory");
		return declaredMavenRoots(root, descriptor, nodes).orElseGet(() -> conventionalMavenRoots(root, tests));
	}

	/**
	 * The elements of one name in a Maven descriptor.
	 * <p>
	 * Reading the file is kept apart from turning its elements into source roots,
	 * so the broad catch a document builder needs covers the reading alone. A
	 * refusal raised while resolving a root then reaches the caller as itself
	 * rather than as a descriptor that could not be parsed.
	 *
	 * @param descriptor  the pom.xml to read
	 * @param elementName the element to collect
	 * @return the matching elements
	 */
	private static NodeList readMavenElements(Path descriptor, String elementName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			return factory.newDocumentBuilder().parse(descriptor.toFile()).getElementsByTagName(elementName);
		} catch (Exception exception) {
			throw new IllegalStateException("Cannot parse Maven source roots from " + descriptor, exception);
		}
	}

	/**
	 * The roots the given elements declare, or empty where they declare none.
	 * <p>
	 * Empty says nothing was declared, which the caller must tell apart from a
	 * declaration whose every root was passed over. Both leave no roots and mean
	 * opposite things for the convention.
	 *
	 * @param root       the project root
	 * @param descriptor the descriptor read
	 * @param nodes      the source-directory elements
	 * @return the declared roots, or empty where none was declared
	 */
	private static Optional<MavenSourceRoots> declaredMavenRoots(Path root, Path descriptor, NodeList nodes) {
		List<Path> roots = new ArrayList<>();
		boolean declaredAnything = false;
		boolean allPresent = true;
		for (int index = 0; index < nodes.getLength(); index++) {
			String value = nodes.item(index).getTextContent();
			if (value == null || value.isBlank()) {
				continue;
			}
			declaredAnything = true;
			Optional<Path> resolved = resolveDeclaredRoot(root, substituteMavenRoot(value.trim(), root), descriptor);
			if (resolved.isPresent()) {
				roots.add(resolved.get());
			} else {
				allPresent = false;
			}
		}
		return declaredAnything ? Optional.of(new MavenSourceRoots(List.copyOf(roots), allPresent)) : Optional.empty();
	}

	/**
	 * The root Maven uses for a source set nothing was declared for.
	 * <p>
	 * A project need not contain it, and one that does not simply has no roots
	 * here. Nothing is uncertain either way, so these roots are complete.
	 *
	 * @param root  the project root
	 * @param tests whether to answer for the test source set
	 * @return the conventional root where the project has it, empty otherwise
	 */
	private static MavenSourceRoots conventionalMavenRoots(Path root, boolean tests) {
		Path conventional = root.resolve(tests ? DEFAULT_TEST_SOURCE : DEFAULT_PRODUCTION_SOURCE);
		return Files.isDirectory(conventional)
				? new MavenSourceRoots(List.of(canonicaliseSourceRoot(root, conventional)), true)
				: new MavenSourceRoots(List.of(), true);
	}

	private static Path substituteMavenRoot(String value, Path root) {
		return Path.of(value.replace("${project.basedir}", root.toString()).replace("${basedir}", root.toString()));
	}

	/**
	 * The roots of both Gradle source sets, read in one pass over the descriptor.
	 *
	 * @param production         the roots of the main source set
	 * @param test               the roots of the test source set
	 * @param productionComplete whether the main roots are known to be all of them
	 */
	private record GradleSourceRoots(List<Path> production, List<Path> test, boolean productionComplete) {
	}

	/**
	 * What a source-directory declaration changes, judged by where it is written.
	 */
	private enum Slot {
		/** The Java directories of the main source set. */
		MAIN_JAVA,
		/** The Java directories of the test source set. */
		TEST_JAVA,
		/** Something that holds no production Java source, such as main's resources. */
		IGNORED,
		/** No single source set, so main is no longer known to be complete. */
		UNATTRIBUTED
	}

	/** How a name is used where it is written. */
	private enum Use {
		/** Assigned with {@code =}, which replaces. */
		REPLACE,
		/** Extended with {@code +=} in Groovy, which adds. */
		APPEND,
		/** Extended with {@code +=} in Kotlin, which compiles and changes nothing. */
		INERT,
		/** Called with parentheses. */
		CALL,
		/** Called without parentheses, as Groovy allows. */
		COMMAND,
		/** Given a closure, whose result this reader cannot know. */
		BLOCK,
		/** Only read. */
		READ
	}

	/**
	 * The directories an operand names, whether it was read in full, and whether a
	 * value came from a name rather than from a literal.
	 *
	 * @param values   the paths read, relative to the project or absolute
	 * @param complete whether nothing in the operand was left unread
	 * @param named    whether a value came from a property, a variable, an
	 *                 interpolation or a call such as {@code file(...)}, whose
	 *                 value Gradle or the script may set elsewhere, for example on
	 *                 the command line
	 */
	private record Resolution(List<String> values, boolean complete, boolean named) {
		/** An operand this reader cannot work out. */
		private static final Resolution UNREAD = new Resolution(List.of(), false, false);
	}

	/**
	 * An operand read from a script.
	 *
	 * @param resolution what it names
	 * @param end        the offset to go on from
	 */
	private record Operand(Resolution resolution, int end) {
	}

	/**
	 * A block the walker is inside, with what it needs to resume the statement that
	 * opened it once the block closes.
	 *
	 * @param path     the names leading into this block, outermost first
	 * @param head     the names of the statement that opened it
	 * @param brackets that statement's brackets still open, as the walker keeps
	 *                 them
	 */
	private record Frame(List<String> path, List<String> head, List<List<String>> brackets) {
	}

	/**
	 * Reads the source roots a Gradle descriptor declares, without running it. Only
	 * {@code sourceSets.main.java} and {@code sourceSets.test.java} give roots.
	 * What cannot be placed or worked out marks main incomplete; a form this reader
	 * does not know, such as one a plugin adds, stays unseen.
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
		GradleReading reading = new GradleReading(root, loadGradleProperties(root), rootProjectIsThisProject(root));
		if (Files.isDirectory(root.resolve("buildSrc"))) {
			reading.blocked.add("buildSrc: its build logic may change the source sets, and it is not read");
		}
		reading.applying.push(BuildToolConfiguration.canonicalise(descriptor));
		new GradleScriptWalker(reading, descriptor, content).walk();
		List<String> reasons = reading.productionGaps();
		warnIfProductionRootsArePartial(reasons.isEmpty(), descriptor, reasons);
		return new GradleSourceRoots(rootsOf(root, reading.declared, "main"), rootsOf(root, reading.declared, "test"),
				reasons.isEmpty());
	}

	/**
	 * Whether {@code rootProject} is known to be the project being read: a settings
	 * file beside the descriptor, or none at or above it, makes it the root
	 * project. A settings file only above it may make it a subproject, or move the
	 * root project here, which this reader does not work out.
	 *
	 * @param root the project root
	 * @return whether the project is known to be the root project
	 */
	private static boolean rootProjectIsThisProject(Path root) {
		return hasSettingsFile(root) || Stream.iterate(root.getParent(), Objects::nonNull, Path::getParent)
				.noneMatch(ProjectSourcesFinder::hasSettingsFile);
	}

	/**
	 * Whether this directory holds a Gradle settings file.
	 *
	 * @param directory the directory to look in
	 * @return whether settings.gradle or settings.gradle.kts is there
	 */
	private static boolean hasSettingsFile(Path directory) {
		return Files.isRegularFile(directory.resolve("settings.gradle"))
				|| Files.isRegularFile(directory.resolve("settings.gradle.kts"));
	}

	/**
	 * The source set a declaration changes, from the names leading to it: the name
	 * after the last {@code sourceSets}, then {@code java} or {@code resources}
	 * further on. Any other source set, {@code all} included, another project's,
	 * and one under a condition count as unattributed.
	 *
	 * @param path the names leading to the declaration, outermost first
	 * @return what the declaration changes
	 */
	private static Slot slotOf(List<String> path) {
		int set = setIndexOf(path);
		if (set < 0 || set >= path.size() || path.contains(OTHER_PROJECT) || path.contains(ANCESTOR)
				|| path.stream().anyMatch(CONDITIONS::contains)) {
			return Slot.UNATTRIBUTED;
		}
		String kind = path.subList(set + 1, path.size()).stream()
				.filter(name -> "java".equals(name) || "resources".equals(name)).findFirst().orElse("");
		if ("main".equals(path.get(set))) {
			return "java".equals(kind) ? Slot.MAIN_JAVA : "resources".equals(kind) ? Slot.IGNORED : Slot.UNATTRIBUTED;
		}
		if ("test".equals(path.get(set))) {
			return "java".equals(kind) ? Slot.TEST_JAVA : Slot.IGNORED;
		}
		return Slot.UNATTRIBUTED;
	}

	/**
	 * Whether these names lead into a project other than this one, such as
	 * {@code subprojects} or {@code parent}. The last project named decides, so a
	 * {@code rootProject} inside {@code subprojects} may lead back here, and so may
	 * a {@code parent} after any project but another {@code parent}.
	 *
	 * @param path the names leading to a declaration
	 * @return whether another project's configuration holds it
	 */
	private static boolean reachesOnlyAnotherProject(List<String> path) {
		int last = lastProjectIn(path);
		if (last < 0) {
			return false;
		}
		String project = path.get(last);
		return OTHER_PROJECT.equals(project) || ANCESTOR.equals(project)
				&& path.subList(0, last).stream().allMatch(name -> !switchesProject(name) || ANCESTOR.equals(name));
	}

	/**
	 * Where the last name that switches to a project, maybe this one, is in these
	 * names: a project marker or {@code rootProject}.
	 *
	 * @param path the names leading to a declaration
	 * @return its index, or -1 where there is none
	 */
	private static int lastProjectIn(List<String> path) {
		for (int index = path.size() - 1; index >= 0; index--) {
			if (switchesProject(path.get(index))) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Whether this recorded name switches to a project, maybe this one: a project
	 * marker or {@code rootProject}.
	 *
	 * @param name a recorded name
	 * @return whether it names a project
	 */
	private static boolean switchesProject(String name) {
		return OTHER_PROJECT.equals(name) || ANCESTOR.equals(name) || MAYBE_THIS_PROJECT.equals(name)
				|| LOOKED_UP_PROJECT.equals(name) || "rootProject".equals(name);
	}

	/**
	 * Where the source set name stands in these names: right after the last
	 * {@code sourceSets}, past any {@code val} or {@code var}.
	 *
	 * @param path the names leading to a declaration
	 * @return the index, or -1 without {@code sourceSets}
	 */
	private static int setIndexOf(List<String> path) {
		int set = path.lastIndexOf("sourceSets") + 1;
		if (set == 0) {
			return -1;
		}
		while (set < path.size() && ("val".equals(path.get(set)) || "var".equals(path.get(set)))) {
			set++;
		}
		return set;
	}

	/**
	 * What reading one Gradle build has found, shared by its descriptor and every
	 * script the descriptor applies.
	 */
	private static final class GradleReading {
		/** The canonical project root every path is resolved against. */
		private final Path root;
		/** The entries of gradle.properties, which every script can read. */
		private final Map<String, String> properties;
		/** The roots of main and test, each starting from its convention. */
		private final Map<String, List<Path>> declared = new LinkedHashMap<>();
		/** Why a source set's roots may be partial; a replacement starts afresh. */
		private final Map<String, List<String>> gaps = new LinkedHashMap<>();
		/** Why main is partial whatever comes later in the script. */
		private final List<String> blocked = new ArrayList<>();
		/**
		 * The scripts being read right now, to notice one applied again inside itself.
		 */
		private final Deque<Path> applying = new ArrayDeque<>();
		/** Whether code this reader cannot follow may have replaced the test roots. */
		private boolean testUntrusted;
		/** Whether {@code rootProject} is known to be this project. */
		private final boolean rootProjectIsThis;

		/**
		 * Starts reading the build at this root.
		 *
		 * @param root              the canonical project root
		 * @param properties        the entries of gradle.properties
		 * @param rootProjectIsThis whether {@code rootProject} is known to be this
		 *                          project
		 */
		private GradleReading(Path root, Map<String, String> properties, boolean rootProjectIsThis) {
			this.root = root;
			this.properties = properties;
			this.rootProjectIsThis = rootProjectIsThis;
		}

		/**
		 * Every reason main is not known to be complete, in the order found.
		 *
		 * @return the reasons, empty when main is complete
		 */
		private List<String> productionGaps() {
			List<String> reasons = new ArrayList<>(blocked);
			reasons.addAll(gaps.getOrDefault("main", List.of()));
			return reasons;
		}

		/**
		 * Applies one declaration to main or test: a replacement clears the roots and
		 * gaps of that set, an addition keeps both. An absent value or a partly read
		 * operand is a gap, and test takes no value from the latter, nor one a property
		 * or a variable names: Gradle may set those elsewhere, and a test root it does
		 * not use would exempt the classes in it.
		 *
		 * @param sourceSet  main or test
		 * @param replacing  whether the declaration replaces
		 * @param resolution what its operand names
		 * @param where      the script and line, for the warning
		 * @param script     the script it is written in
		 */
		private void declare(String sourceSet, boolean replacing, Resolution resolution, String where, Path script) {
			if ("test".equals(sourceSet) && testUntrusted) {
				return;
			}
			List<Path> roots = declared.computeIfAbsent(sourceSet, name -> conventionalRootOf(root, name));
			List<String> found = new ArrayList<>();
			if (replacing) {
				roots.clear();
			}
			if (!resolution.complete()) {
				found.add(where + ": a source directory whose value cannot be worked out without running Gradle");
			}
			boolean test = "test".equals(sourceSet);
			if (test && resolution.named()) {
				found.add(where + ": a test source directory named by a property or a variable, which is not taken");
			}
			for (String value : test && (!resolution.complete() || resolution.named()) ? List.<String>of()
					: resolution.values()) {
				Optional<Path> resolved = resolveDeclaredRoot(root, root.resolve(value), script);
				if (resolved.isPresent()) {
					roots.add(resolved.get());
				} else {
					found.add(where + ": the source directory " + value + " is not there");
				}
			}
			if (replacing) {
				gaps.put(sourceSet, found);
			} else {
				gaps.computeIfAbsent(sourceSet, name -> new ArrayList<>()).addAll(found);
			}
		}

		/**
		 * Records code this reader cannot follow: main is no longer complete, and the
		 * test roots are dropped for good, since that code may have replaced them and
		 * their classes would otherwise stay exempt.
		 *
		 * @param reason what could not be followed, with script and line
		 */
		private void opaque(String reason) {
			blocked.add(reason);
			declared.put("test", new ArrayList<>());
			testUntrusted = true;
		}

		/**
		 * Reads a script named by {@code apply from} as part of this build. One that
		 * cannot be found or read, lies outside the project, or is already being read
		 * is recorded as code this reader cannot follow instead.
		 *
		 * @param target the script named
		 * @param where  the script and line applying it
		 */
		private void applyScript(Path target, String where) {
			Path script;
			try {
				script = target.toRealPath();
			} catch (IOException exception) {
				opaque(where + ": the applied script " + target + " cannot be found");
				return;
			}
			if (!script.startsWith(root)) {
				opaque(where + ": the applied script " + target + " lies outside the project");
				return;
			}
			if (applying.contains(script)) {
				opaque(where + ": the script " + target + " is applied again while it is being read");
				return;
			}
			String content;
			try {
				content = Files.readString(script);
			} catch (IOException exception) {
				opaque(where + ": the applied script " + target + " cannot be read");
				return;
			}
			applying.push(script);
			try {
				new GradleScriptWalker(this, script, content).walk();
			} finally {
				applying.pop();
			}
		}
	}

	/**
	 * Walks one Gradle script statement by statement, following the blocks and the
	 * names that lead to each source-directory declaration. Comments and the bodies
	 * of strings are masked first, so neither is mistaken for code.
	 */
	private static final class GradleScriptWalker {
		/** Calls whose first argument names the source set or task they return. */
		private static final Set<String> LOOKUPS = Set.of("named", "getByName", "findByName", "maybeCreate", "create",
				"register", "getAt");
		/** The names that declare Java source directories. */
		private static final Set<String> DECLARATIONS = Set.of("srcDir", "srcDirs", "setSrcDirs");
		/** The names that add sources to a compile task or a source directory set. */
		private static final Set<String> SOURCES = Set.of("source", "setSource");
		/** The markers a receiver leaves, including a safe call after one. */
		private static final Set<String> RECEIVER_MARKERS = Set.of(ANCESTOR, MAYBE_THIS_PROJECT, SHORT_CIRCUIT);
		/** The markers that stand for a project other than, or maybe not, this one. */
		private static final Set<String> PROJECT_MARKERS = Set.of(OTHER_PROJECT, ANCESTOR, MAYBE_THIS_PROJECT,
				LOOKED_UP_PROJECT);
		/**
		 * The words before a body that may not run: a branch, a loop, a handler or a
		 * {@code try}, with or without a condition in parentheses.
		 */
		private static final Set<String> BODY_HEADERS = Set.of("if", "for", "while", "switch", "when", "catch", "else",
				"do", "try");
		/**
		 * The project names a script may hide behind a variable of its own, which then
		 * may hold any project.
		 */
		private static final Set<String> SHADOWABLE_PROJECTS = Set.of("project", "rootProject", "parent");
		/**
		 * One of those names where it is bound rather than used: after a capitalised
		 * type on its line, or, across line breaks and comments, before a comma,
		 * {@code ->}, {@code in} or a single colon, or between a comma and a closing
		 * parenthesis, as in a tuple, a closure or loop parameter, or a Kotlin
		 * parameter. A map key or a call argument matches too, which only makes the
		 * reader more careful.
		 */
		private static final Pattern PROJECT_BINDING = Pattern
				.compile("(?:[A-Z][A-Za-z0-9_$<>?]*[ \\t\\u0001]+)(project|rootProject|parent)(?![A-Za-z0-9_$])"
						+ "|(?<![A-Za-z0-9_$.])(project|rootProject|parent)[\\s\\u0001]*(?:,|->|in(?![A-Za-z0-9_$])|:(?!:))"
						+ "|,[\\s\\u0001]*(project|rootProject|parent)[\\s\\u0001]*\\)");
		/**
		 * A whole name as written, also right after the {@code $} of a string
		 * interpolation, not part of a longer one.
		 */
		private static final Pattern NAME = Pattern.compile("(?<![A-Za-z0-9_])[A-Za-z_][A-Za-z0-9_]*");
		/**
		 * A Kotlin variable that reads the project property of its name, as in
		 * {@code val dir: String by project}, which binds nothing of its own. The
		 * statement must end after {@code project}: {@code by project.extra("x")} gives
		 * the variable a value of its own.
		 */
		private static final Pattern PROPERTY_DELEGATE = Pattern.compile(
				"(?<![A-Za-z0-9_$])val[ \\t]+([A-Za-z_][A-Za-z0-9_]*)[ \\t]*(?::[ \\t]*[A-Za-z0-9_.<>?]+[ \\t]*)?by[ \\t]+project[ \\t\\u0001]*(?=[\\r\\n;]|$)");
		/**
		 * The names a closure gives its code without their being written as parameters,
		 * which never stand for a property or a variable of the script.
		 */
		private static final Set<String> IMPLICIT_NAMES = Set.of("it", "owner", "delegate", "this");
		/** The names that, before {@code apply}, end a project receiver. */
		private static final Set<String> PROJECT_NAMES = Set.of("project", "rootProject");
		/**
		 * The receivers before {@code apply} known to be this project: none,
		 * {@code project}, and {@code rootProject} where it is recorded as itself.
		 */
		private static final Set<List<String>> CERTAIN_RECEIVERS = Set.of(List.of(), List.of("project"),
				List.of("rootProject"));
		/** The project sets that, read from an ancestor, include this project. */
		private static final Set<String> INCLUDING_DESCENDANTS = Set.of("allprojects", "subprojects");
		/** The calls that turn their one argument into a single file. */
		private static final Set<String> FILE_CALLS = Set.of("file", "project.file");
		/** The calls that turn their arguments into a collection of files. */
		private static final Set<String> COLLECTION_CALLS = Set.of("files", "project.files", "listOf", "setOf");
		/** The build being read. */
		private final GradleReading reading;
		/** The script this walker reads. */
		private final Path script;
		/** The script as written. */
		private final String content;
		/** Whether the script is Kotlin, where {@code srcDirs +=} changes nothing. */
		private final boolean kotlin;
		/** The script with comments and the bodies of strings masked. */
		private final String code;
		/** The blocks open at the current offset, innermost first. */
		private final Deque<Frame> blocks = new ArrayDeque<>();
		/**
		 * The names of the current statement so far, inside the innermost bracket still
		 * open.
		 */
		private List<String> head = new ArrayList<>();
		/**
		 * For each bracket of the current statement still open, outermost first, the
		 * names written before it.
		 */
		private List<List<String>> brackets = new ArrayList<>();
		/** How many operand entries are being resolved inside one another. */
		private int nesting;
		/** Every name this script assigns, which hides a property of the same name. */
		private final Set<String> localNames = new HashSet<>();
		/**
		 * The names this script declares once, at the top level, with a literal, as in
		 * {@code def dir = 'a'}. An assignment without a declaration writes a project
		 * property instead, which another script may change.
		 */
		private final Map<String, String> locals = new HashMap<>();
		/**
		 * The project names this script assigns or binds anywhere, which as a receiver
		 * may then hold any project.
		 */
		private final Set<String> shadowedProjects = new HashSet<>();
		/**
		 * How often each name is written in the script as written, strings and comments
		 * included, so that every place a name may be bound counts. A Kotlin
		 * {@code val dir by project}, which only reads the property, does not count.
		 */
		private final Map<String, Integer> mentions = new HashMap<>();

		/**
		 * Prepares a walk over one script, collecting the names it assigns and which of
		 * them hold a fixed literal.
		 *
		 * @param reading the build being read
		 * @param script  the script
		 * @param content the script as written
		 */
		private GradleScriptWalker(GradleReading reading, Path script, String content) {
			this.reading = reading;
			this.script = script;
			this.content = content;
			this.kotlin = Objects.toString(script.getFileName(), "").endsWith(".kts");
			this.code = maskInactiveRegions(content, kotlin);
			Map<String, String> literal = new HashMap<>();
			Matcher assignments = PROPERTY_ASSIGNMENT.matcher(code);
			while (assignments.find()) {
				if (atTopLevel(code, assignments.start())) {
					literal.put(assignments.group(1), content.substring(assignments.start(2), assignments.end(2)));
				}
			}
			Map<String, Integer> counts = new HashMap<>();
			Set<String> declared = new HashSet<>();
			Matcher names = ASSIGNED_NAME.matcher(code);
			while (names.find()) {
				counts.merge(names.group(1), 1, Integer::sum);
				if (declaresALocal(names.start())) {
					declared.add(names.group(1));
				} else {
					forgetProjectProperty(names.group(1));
				}
			}
			localNames.addAll(counts.keySet());
			Matcher written = NAME.matcher(content);
			while (written.find()) {
				mentions.merge(written.group(), 1, Integer::sum);
			}
			Matcher delegates = PROPERTY_DELEGATE.matcher(code);
			while (delegates.find()) {
				mentions.merge(delegates.group(1), -1, Integer::sum);
			}
			SHADOWABLE_PROJECTS.stream().filter(localNames::contains).forEach(shadowedProjects::add);
			Matcher bindings = PROJECT_BINDING.matcher(code);
			while (bindings.find()) {
				shadowedProjects.add(Stream.of(bindings.group(1), bindings.group(2), bindings.group(3))
						.filter(Objects::nonNull).findFirst().orElseThrow());
			}
			counts.forEach((name, count) -> {
				if (count == 1 && declared.contains(name) && literal.containsKey(name)) {
					locals.put(name, literal.get(name));
				}
			});
		}

		/**
		 * Forgets the gradle.properties entry an assignment without {@code def},
		 * {@code val} or {@code var} may overwrite, plainly or through {@code ext},
		 * since every script after it would see the new value.
		 */
		private void forgetProjectProperty(String name) {
			reading.properties.remove(name);
			reading.properties.remove(lastSegmentOf(name));
		}

		/**
		 * The fixed value of a name as this script sees it: its own once-assigned
		 * literal, where the script writes the name only there and in this use, else,
		 * where the script assigns nothing to it and writes it only here, the
		 * gradle.properties entry. Any other mention may bind the name, as a closure,
		 * lambda or loop parameter does, so it then has no fixed value; neither has a
		 * closure's implicit {@code it}. A parent script's variables are not visible
		 * here, as in Gradle.
		 */
		private Optional<String> propertyValue(String name) {
			String simple = lastSegmentOf(name).replaceFirst("^\\$+", "");
			int written = mentions.getOrDefault(simple, 0);
			if (IMPLICIT_NAMES.contains(simple)) {
				return Optional.empty();
			}
			String value = localNames.contains(name) ? written == 2 ? locals.get(name) : null
					: written <= 1 ? reading.properties.get(name) : null;
			return value != null && isStaticLiteral(value) ? Optional.of(value) : Optional.empty();
		}

		/** Walks the whole script once. */
		private void walk() {
			int index = 0;
			while (index < code.length()) {
				index = step(index);
			}
		}

		/**
		 * Handles the character at this offset.
		 *
		 * @param index the offset
		 * @return the offset to go on from
		 */
		private int step(int index) {
			char current = code.charAt(index);
			if (current == '{') {
				openBlock(index);
			} else if (current == '}') {
				closeBlock();
			} else if (current == '[') {
				return openBracket(index);
			} else if (current == '(') {
				openArguments();
			} else if (current == ')' || current == ']') {
				closeArguments();
			} else if (current == ',' && !brackets.isEmpty()) {
				leaveArgument();
				head = new ArrayList<>();
			} else if (current == ';'
					|| current == '\n' && brackets.isEmpty() && !continuesOnTheNextLine(index) && !awaitsItsBody()) {
				head = new ArrayList<>();
			} else if ((current == '\'' || current == '"') && followsADot(index)) {
				return quotedName(index);
			} else if (shortCircuitLength(index) > 0) {
				head = new ArrayList<>(List.of(SHORT_CIRCUIT));
				return index + shortCircuitLength(index);
			} else if (code.startsWith("?.", index) && !head.isEmpty()
					&& PROJECT_MARKERS.contains(head.get(head.size() - 1))) {
				head.add(SHORT_CIRCUIT);
			} else if (current == ':' && head.contains(SHORT_CIRCUIT)) {
				head = new ArrayList<>(List.of(SHORT_CIRCUIT));
			} else if (current == '=' && brackets.isEmpty() && "=!<>".indexOf(code.charAt(Math.max(0, index - 1))) < 0
					&& (index + 1 >= code.length() || code.charAt(index + 1) != '=')) {
				head.add(ASSIGNED);
			} else if (isIdentifierStart(current)) {
				return identifier(index);
			}
			return index + 1;
		}

		/**
		 * Enters a bracket, whose names are kept apart from the ones written before it.
		 */
		private void openArguments() {
			brackets.add(head);
			head = new ArrayList<>();
		}

		/**
		 * Leaves the innermost bracket, if one is open, and resumes the names written
		 * before it.
		 */
		private void closeArguments() {
			if (!brackets.isEmpty()) {
				leaveArgument();
				head = brackets.remove(brackets.size() - 1);
			}
		}

		/**
		 * Ends one argument of the innermost bracket. Where it names a project, the
		 * call's block may configure that project, so the call is marked as maybe this
		 * project. A call to {@code configure} is marked for any argument, an empty one
		 * included: as another project where the argument is only another project, as
		 * maybe this one otherwise.
		 */
		private void leaveArgument() {
			List<String> call = brackets.get(brackets.size() - 1);
			if (configures(call)) {
				call.add(List.of(OTHER_PROJECT).equals(head) ? OTHER_PROJECT : MAYBE_THIS_PROJECT);
			} else if (head.stream().anyMatch(this::namesAProject)) {
				call.add(MAYBE_THIS_PROJECT);
			}
		}

		/**
		 * Whether these names, past the markers an argument left, are a call to
		 * {@code configure} or {@code project.configure}.
		 */
		private static boolean configures(List<String> call) {
			List<String> names = call.stream().filter(name -> !PROJECT_MARKERS.contains(name)).toList();
			return List.of("configure").equals(names) || List.of("project", "configure").equals(names);
		}

		/**
		 * Whether this recorded name stands for a project that may not be this one: a
		 * marker, {@code parent}, or a {@code rootProject} not known to be this
		 * project.
		 */
		private boolean namesAProject(String name) {
			return PROJECT_MARKERS.contains(name) || "parent".equals(name)
					|| "rootProject".equals(name) && !reading.rootProjectIsThis;
		}

		/**
		 * Whether the statement so far is a branch, loop or handler still waiting for
		 * its body at a line break, as {@code if (x)} or {@code else}, so that a body
		 * on the next line, braced or not, stays under it. A project marker its
		 * condition left, as in {@code if (findProject(':') == null)}, is passed over.
		 */
		private boolean awaitsItsBody() {
			List<String> names = head.stream().filter(name -> !PROJECT_MARKERS.contains(name)).toList();
			return !names.isEmpty() && BODY_HEADERS.contains(names.get(names.size() - 1));
		}

		/**
		 * Whether the statement goes on after this line break: the next line starts
		 * with {@code .}, {@code ?}, {@code :}, {@code &&} or {@code ||}, or with a
		 * brace, which Groovy takes as a closure passed to the call before it, or this
		 * line ends in the middle of an expression.
		 */
		private boolean continuesOnTheNextLine(int lineBreak) {
			int next = skipInactive(code, lineBreak);
			return next < code.length() && (".?:{".indexOf(code.charAt(next)) >= 0 || code.startsWith("&&", next)
					|| code.startsWith("||", next)) || endsMidExpression(lineBreak);
		}

		/**
		 * Whether the last code before this line break is {@code &&}, {@code ||},
		 * {@code :}, a dot, a backslash continuing the line or, in Groovy, a ternary
		 * {@code ?}.
		 */
		private boolean endsMidExpression(int lineBreak) {
			int last = skipInactiveBackwards(lineBreak - 1);
			if (last < 0) {
				return false;
			}
			char character = code.charAt(last);
			return ":.\\".indexOf(character) >= 0 || character == '?' && !kotlin
					|| (character == '&' || character == '|') && last > 0 && code.charAt(last - 1) == character;
		}

		/**
		 * How many characters of a short-circuiting operator start here: two for
		 * {@code &&}, {@code ||} and {@code ?:}, one for a Groovy ternary {@code ?},
		 * none otherwise. A safe call {@code ?.}, a safe index {@code ?[} and a Kotlin
		 * nullable type are not counted.
		 */
		private int shortCircuitLength(int at) {
			char current = code.charAt(at);
			char next = at + 1 < code.length() ? code.charAt(at + 1) : '\0';
			if ((current == '&' || current == '|') && next == current || current == '?' && next == ':') {
				return 2;
			}
			return current == '?' && !kotlin && next != '.' && next != '[' ? 1 : 0;
		}

		/**
		 * Handles a quoted name after a dot, as in {@code java.'srcDir'('a')}, which
		 * Groovy calls like any other. One of the declaring names is taken as a
		 * replacement Ares cannot read, since the mask hides it from the rest of the
		 * walk.
		 */
		private int quotedName(int quote) {
			int close = code.indexOf(code.charAt(quote), quote + 1);
			if (close < 0) {
				return quote + 1;
			}
			String name = content.substring(quote + 1, close);
			List<String> path = pathWith(List.of());
			if ((DECLARATIONS.contains(name) || SOURCES.contains(name)) && path.contains("sourceSets")) {
				declare(quote, path, true, Resolution.UNREAD);
			} else if (DECLARATIONS.contains(name) || SOURCES.contains(name)) {
				reading.opaque(locationOf(quote) + ": the quoted name " + name + ", which is not read");
			}
			return close + 1;
		}

		/**
		 * Whether the last code before this offset, past blanks and comments, is a dot.
		 */
		private boolean followsADot(int at) {
			int index = skipInactiveBackwards(at - 1);
			return index >= 0 && code.charAt(index) == '.';
		}

		/** Enters the block opening at this brace, saving the statement around it. */
		private void openBlock(int brace) {
			if (startsAMethodBody(brace)) {
				head.add(METHOD_BODY);
			}
			blocks.push(new Frame(pathWith(List.of()), head, brackets));
			head = new ArrayList<>();
			brackets = new ArrayList<>();
		}

		/**
		 * Whether the brace here opens a method body, as in {@code int unused() {}} or
		 * {@code List<String> names() {}}: a parameter list closes right before it, a
		 * name opens that list, and a type stands before the name.
		 */
		private boolean startsAMethodBody(int brace) {
			int close = parameterListCloseBefore(brace);
			int open = close >= 0 ? openingParenthesis(close) : -1;
			if (open <= 0) {
				return false;
			}
			int nameEnd = skipBlanksBackwards(code, open - 1) + 1;
			int nameStart = wordStartBefore(nameEnd);
			if (nameStart == nameEnd || nameStart > 0 && code.charAt(nameStart - 1) == '.') {
				return false;
			}
			int typeEnd = skipBlanksBackwards(code, nameStart - 1) + 1;
			if (typeEnd == nameStart || typeEnd == 0) {
				return false;
			}
			char before = code.charAt(typeEnd - 1);
			String type = code.substring(wordStartBefore(typeEnd), typeEnd);
			return before == '>' || before == ']'
					|| !type.isEmpty() && isIdentifierStart(type.charAt(0)) && !NOT_TYPES.contains(type);
		}

		/**
		 * The parenthesis closing a parameter list right before this brace, past a
		 * {@code throws} clause, or -1 where none closes there.
		 */
		private int parameterListCloseBefore(int brace) {
			int close = skipInactiveBackwards(brace - 1);
			if (close >= 0 && code.charAt(close) == ')') {
				return close;
			}
			int paren = code.lastIndexOf(')', brace);
			if (paren < 0) {
				return -1;
			}
			String between = code.substring(paren + 1, brace).replace(String.valueOf(COMMENT_MASK), " ").strip();
			return THROWS_CLAUSE.matcher(between).matches() ? paren : -1;
		}

		/** The offset of the parenthesis this one closes, or -1. */
		private int openingParenthesis(int close) {
			int level = 0;
			for (int index = close; index >= 0; index--) {
				if (code.charAt(index) == ')') {
					level++;
				} else if (code.charAt(index) == '(' && --level == 0) {
					return index;
				}
			}
			return -1;
		}

		/**
		 * The last offset at or before this one that is neither whitespace nor a
		 * comment.
		 */
		private int skipInactiveBackwards(int from) {
			int index = from;
			while (index >= 0 && (Character.isWhitespace(code.charAt(index)) || code.charAt(index) == COMMENT_MASK)) {
				index--;
			}
			return index;
		}

		/** Where the undotted name ending at this offset begins. */
		private int wordStartBefore(int end) {
			int start = end;
			while (start > 0 && isIdentifierPart(code.charAt(start - 1)) && code.charAt(start - 1) != '.') {
				start--;
			}
			return start;
		}

		/** Leaves the innermost block and resumes the statement that opened it. */
		private void closeBlock() {
			Frame frame = blocks.poll();
			if (frame != null) {
				head = frame.head();
				brackets = frame.brackets();
			}
		}

		/**
		 * Handles a square bracket: an index right after a name, as in
		 * {@code sourceSets["main"]}, adds its literal to the names, or {@code ?} where
		 * it is not one; any other bracket only opens a nesting level.
		 */
		private int openBracket(int bracket) {
			char previous = bracket > 0 ? code.charAt(bracket - 1) : ' ';
			boolean indexing = brackets.isEmpty() && (previous != '.' && isIdentifierPart(previous) || previous == ')');
			if (!indexing) {
				openArguments();
				return bracket + 1;
			}
			int close = matchingClose(code, bracket);
			if (close < 0) {
				head.add("?");
				return code.length();
			}
			head.add(unnamedLiteral(bracket + 1, close).orElse("?"));
			return close + 1;
		}

		/**
		 * Handles the name starting here, a dotted chain such as
		 * {@code sourceSets.main.java.srcDir}, by its last segment.
		 *
		 * @param start where the name begins
		 * @return the offset to go on from
		 */
		private int identifier(int start) {
			int end = start;
			while (end < code.length() && isIdentifierPart(code.charAt(end))) {
				end++;
			}
			List<String> chain = new ArrayList<>(List.of(code.substring(start, end).split("\\.")));
			chain.removeIf(String::isEmpty);
			List<String> prefix = recordChain(chain, start, end);
			String recorded = prefix.remove(prefix.size() - 1);
			String name = chain.get(chain.size() - 1);
			boolean deferred = defersItsAction(pathWith(prefix), name);
			if (LOOKUPS.contains(name) && brackets.isEmpty()) {
				return lookup(end, prefix, name, deferred);
			}
			if (DECLARATIONS.contains(name)) {
				return recordHandled(declaration(start, end, prefix, name), prefix, recorded);
			}
			if (SOURCES.contains(name)) {
				return recordHandled(source(start, end, prefix, name), prefix, recorded);
			}
			if ("apply".equals(name) && namesAProjectReceiver(prefix)) {
				return apply(start, end, prefix);
			}
			head.addAll(prefix);
			head.add(recorded);
			if (deferred) {
				head.add(DEFERRED);
			}
			return end;
		}

		/**
		 * Adds a handled declaration or {@code source} to the names of the statement,
		 * so that a branch whose one-line body it was stops waiting for its body at the
		 * next line break: its names, as any other name is added, so that a call
		 * chained to it keeps its receiver, or {@link #DEFERRED} where a brace follows,
		 * whose closure Gradle evaluates later as a path rather than as configuration
		 * of the source set the declaration names.
		 *
		 * @param next     the offset the handler goes on from
		 * @param prefix   the names before it in its dotted name
		 * @param recorded the name recorded for it
		 * @return the offset to go on from
		 */
		private int recordHandled(int next, List<String> prefix, String recorded) {
			if (takesAClosureAfter(next)) {
				head.add(DEFERRED);
			} else {
				head.addAll(prefix);
				head.add(recorded);
			}
			return next;
		}

		/**
		 * Whether the names before {@code apply} are a project: none, or names ending
		 * in {@code project}, {@code rootProject} or one of the project markers.
		 */
		private static boolean namesAProjectReceiver(List<String> prefix) {
			return prefix.isEmpty() || PROJECT_NAMES.contains(prefix.get(prefix.size() - 1))
					|| PROJECT_MARKERS.contains(prefix.get(prefix.size() - 1));
		}

		/**
		 * The names the walker records for a dotted name, going on from the statement
		 * so far where it follows a dot. Before {@code allprojects} or
		 * {@code subprojects} the run of receivers is replaced by
		 * {@link #includeThisProject}, before a receiver {@code rootProject} by
		 * {@link #rootProjectAfter}.
		 *
		 * @param chain the segments of the dotted name
		 * @param start where the dotted name begins
		 * @param end   where the dotted name ends
		 * @return the name to record for each segment
		 */
		private List<String> recordChain(List<String> chain, int start, int end) {
			boolean continuing = followsADot(start);
			List<String> working = new ArrayList<>(continuing ? head : List.of());
			int kept = working.size();
			for (int index = 0; index < chain.size(); index++) {
				String segment = chain.get(index);
				int run = receiversStart(working);
				boolean last = index == chain.size() - 1;
				if (INCLUDING_DESCENDANTS.contains(segment) && run < working.size()) {
					kept = Math.min(kept, run);
					includeThisProject(working.subList(run, working.size()), "subprojects".equals(segment));
					working.add(segment);
				} else if ("rootProject".equals(segment) && (!last || leadsOn(end))) {
					int projects = projectReceiversStart(working);
					kept = Math.min(kept, projects);
					List<String> receivers = working.subList(projects, working.size());
					String root = rootProjectAfter(receivers, recorded(segment, last, end));
					receivers.clear();
					working.add(root);
				} else {
					working.add(recorded(segment, index == chain.size() - 1, end));
				}
			}
			if (continuing) {
				head = new ArrayList<>(working.subList(0, kept));
			}
			return new ArrayList<>(working.subList(kept, working.size()));
		}

		/**
		 * Replaces a run of receivers before {@code allprojects} or
		 * {@code subprojects}: the ancestors go, a safe call stays as a condition, and
		 * a {@code rootProject} that may be this project stays once before
		 * {@code subprojects}, which may then leave this project out.
		 *
		 * @param receivers   the run, changed in place
		 * @param subprojects whether {@code subprojects} follows
		 */
		private static void includeThisProject(List<String> receivers, boolean subprojects) {
			boolean safe = receivers.contains(SHORT_CIRCUIT);
			boolean uncertain = subprojects && receivers.contains(MAYBE_THIS_PROJECT);
			receivers.clear();
			if (safe) {
				receivers.add(SHORT_CIRCUIT);
			}
			if (uncertain) {
				receivers.add(MAYBE_THIS_PROJECT);
			}
		}

		/**
		 * What a receiver {@code rootProject} stands for after the project receivers
		 * before it: itself after none or only {@code project}, else LOOKED_UP_PROJECT,
		 * since those receivers may be a collection, as in
		 * {@code subprojects.rootProject}, a call that may return null, or behind a
		 * condition.
		 *
		 * @param receivers the project receivers before it
		 * @param itself    what {@code rootProject} alone is recorded as
		 * @return the name to record in place of the receivers and itself
		 */
		private static String rootProjectAfter(List<String> receivers, String itself) {
			return receivers.stream().allMatch("project"::equals) ? itself : LOOKED_UP_PROJECT;
		}

		/**
		 * Where the run of project receivers ending these names begins: project
		 * markers, safe calls and {@code project}, as in {@code project(':x')?.}.
		 */
		private static int projectReceiversStart(List<String> names) {
			int start = names.size();
			while (start > 0 && (PROJECT_MARKERS.contains(names.get(start - 1))
					|| SHORT_CIRCUIT.equals(names.get(start - 1)) || "project".equals(names.get(start - 1)))) {
				start--;
			}
			return start;
		}

		/**
		 * Where the run of ANCESTOR, MAYBE_THIS_PROJECT and SHORT_CIRCUIT markers
		 * ending these names begins, as in {@code parent?.parent}.
		 */
		private static int receiversStart(List<String> names) {
			int start = names.size();
			while (start > 0 && RECEIVER_MARKERS.contains(names.get(start - 1))) {
				start--;
			}
			return start;
		}

		/**
		 * The name the walker records for one segment of a dotted name. As a receiver:
		 * LOOKED_UP_PROJECT for {@code project}, {@code rootProject} or {@code parent}
		 * where this script assigns or binds that name, ANCESTOR for {@code parent},
		 * MAYBE_THIS_PROJECT for a {@code rootProject} that may be this project. Else
		 * OTHER_PROJECT where {@link #reachesAnotherProject} says so, LOOKED_UP_PROJECT
		 * for any other project lookup, the segment itself.
		 */
		private String recorded(String segment, boolean last, int end) {
			boolean receiver = !last || leadsOn(end);
			if (receiver && shadowedProjects.contains(segment)) {
				return LOOKED_UP_PROJECT;
			}
			if (receiver && "parent".equals(segment)) {
				return ANCESTOR;
			}
			if (receiver && "rootProject".equals(segment) && !reading.rootProjectIsThis) {
				return MAYBE_THIS_PROJECT;
			}
			if (last && reachesAnotherProject(segment, end)) {
				return OTHER_PROJECT;
			}
			return last && looksUpAProject(segment, end) ? LOOKED_UP_PROJECT : segment;
		}

		/**
		 * Whether the name ending here is the receiver of what follows it, because it
		 * ends with a dot, or the next code is {@code .}, {@code ?.} or {@code !!.}.
		 */
		private boolean leadsOn(int end) {
			int next = skipInactive(code, end);
			return code.charAt(end - 1) == '.' || code.startsWith(".", next) || code.startsWith("?.", next)
					|| code.startsWith("!!.", next);
		}

		/**
		 * Whether the name ending here reaches projects other than the one being read:
		 * {@code subprojects}, {@code childProjects}, or, in the root project, a lookup
		 * of a literal path other than {@code ':'}. What {@code configure} reaches, its
		 * arguments decide.
		 */
		private boolean reachesAnotherProject(String name, int end) {
			if ("subprojects".equals(name) || "childProjects".equals(name)) {
				return true;
			}
			if (!looksUpAProject(name, end) || !reading.rootProjectIsThis) {
				return false;
			}
			int open = skipInactive(code, end);
			int close = matchingClose(code, open);
			int[] first = splitTopLevel(code, open + 1, close < 0 ? code.length() : close).get(0);
			return unnamedLiteral(first[0], first[1]).filter(path -> !path.isEmpty() && !":".equals(path)).isPresent();
		}

		/**
		 * Whether the name ending here is a call to {@code project} or
		 * {@code findProject}, which may return any project, this one included.
		 */
		private boolean looksUpAProject(String name, int end) {
			int next = skipInactive(code, end);
			return ("project".equals(name) || "findProject".equals(name)) && next < code.length()
					&& code.charAt(next) == '(';
		}

		/**
		 * Handles a lookup such as {@code named("main")}: the literal first argument
		 * stands for the call among the names, or {@code ?} where it is not a literal,
		 * after {@link #DEFERRED} where its action may never run. The arguments are
		 * then walked as nested code.
		 */
		private int lookup(int end, List<String> prefix, String name, boolean deferred) {
			int open = skipTypeArguments(skipInactive(code, end));
			head.addAll(prefix);
			if (deferred) {
				head.add(DEFERRED);
			}
			if (open >= code.length() || code.charAt(open) != '(') {
				head.add(name);
				return end;
			}
			int close = matchingClose(code, open);
			int[] first = splitTopLevel(code, open + 1, close < 0 ? code.length() : close).get(0);
			head.add(unnamedLiteral(first[0], first[1]).orElse("?"));
			openArguments();
			return open + 1;
		}

		/**
		 * Whether a lookup's configuration action waits until Gradle creates the
		 * element: {@code register} always, {@code named} unless the names leading to
		 * it, enclosing blocks included, end in {@code sourceSets}.
		 *
		 * @param path the names leading to the lookup, outermost first
		 * @param name the lookup's name
		 * @return whether its action may never run
		 */
		private static boolean defersItsAction(List<String> path, String name) {
			return "register".equals(name)
					|| "named".equals(name) && (path.isEmpty() || !"sourceSets".equals(path.get(path.size() - 1)));
		}

		/**
		 * The offset after a type argument list such as {@code <JavaCompile>}, if one
		 * starts here.
		 */
		private int skipTypeArguments(int at) {
			if (at >= code.length() || code.charAt(at) != '<') {
				return at;
			}
			int close = code.indexOf('>', at);
			return close < 0 ? at : skipInactive(code, close + 1);
		}

		/**
		 * Handles {@code srcDir}, {@code srcDirs} or {@code setSrcDirs}: reads its
		 * operand where it is set and applies it to the source set its place names.
		 */
		private int declaration(int start, int end, List<String> prefix, String name) {
			Use use = useOf(start, end);
			if (use == Use.READ || use == Use.INERT) {
				return end;
			}
			List<String> path = pathWith(prefix);
			if (use == Use.BLOCK) {
				declare(start, path, false, Resolution.UNREAD);
				return end;
			}
			int operator = skipInactive(code, end);
			int operandStart = switch (use) {
			case REPLACE -> skipInactive(code, operator + 1);
			case APPEND -> skipInactive(code, operator + 2);
			default -> operator;
			};
			Operand operand = operandAt(operandStart);
			Resolution resolution = takesAClosureAfter(operand.end()) ? Resolution.UNREAD : operand.resolution();
			declare(start, path, use == Use.REPLACE || "setSrcDirs".equals(name), resolution);
			return operand.end();
		}

		/**
		 * Whether a brace follows this offset, past an argument list there, on its line
		 * or the next: Groovy passes that closure to the call as one more path, which
		 * Gradle works out only when it resolves the paths, and which this reader
		 * cannot read.
		 */
		private boolean takesAClosureAfter(int at) {
			int next = skipInactive(code, at);
			if (next < code.length() && code.charAt(next) == '(') {
				int close = matchingClose(code, next);
				next = close < 0 ? code.length() : skipInactive(code, close + 1);
			}
			return next < code.length() && code.charAt(next) == '{';
		}

		/**
		 * Handles {@code source} or {@code setSource} being set. On a task, which may
		 * be the Java compile task under any of its names or types, it compiles files
		 * outside every source set; on a source set it adds directories this reader
		 * cannot name. Either way main becomes incomplete.
		 */
		private int source(int start, int end, List<String> prefix, String name) {
			Use use = useOf(start, end);
			if (use == Use.READ || use == Use.INERT) {
				return end;
			}
			List<String> path = pathWith(prefix);
			if (path.contains("sourceSets")) {
				declare(start, path, false, Resolution.UNREAD);
			} else {
				reading.blocked.add(locationOf(start) + ": " + name + " set on a task, which may compile Java");
			}
			return end;
		}

		/**
		 * Handles {@code apply from} on a project: a script inside the project, applied
		 * to this project and not under a condition, is read as part of the build; any
		 * other is recorded as a gap. Other uses of {@code apply}, such as applying a
		 * plugin, are passed over.
		 *
		 * @param start  where the name begins
		 * @param end    where the name ends
		 * @param prefix the names before {@code apply} in its dotted name
		 * @return the offset to go on from
		 */
		private int apply(int start, int end, List<String> prefix) {
			int open = skipInactive(code, end);
			if (open >= code.length()) {
				return end;
			}
			List<String> path = pathWith(prefix);
			if (code.charAt(open) == '{') {
				reading.opaque(locationOf(start) + ": apply with a block, which is not read");
				return end;
			}
			boolean call = code.charAt(open) == '(';
			int argumentsEnd = call ? matchingClose(code, open) : bareOperandEnd(open);
			if (argumentsEnd < 0) {
				return end;
			}
			Map<String, int[]> arguments = namedArguments(call ? open + 1 : open, argumentsEnd);
			int[] from = arguments.get("from");
			if (from == null) {
				return end;
			}
			String where = locationOf(start);
			Resolution resolution = resolve(from[0], from[1]);
			if (arguments.containsKey("to")) {
				reading.opaque(where + ": apply from with a target of its own, which is not read");
			} else if (reachesOnlyAnotherProject(path)) {
				reading.blocked.add(where + ": apply from inside another project's configuration");
			} else if (!CERTAIN_RECEIVERS.contains(prefix) || !appliesToThisProject()
					|| path.stream().anyMatch(CONDITIONS::contains)) {
				reading.opaque(where + ": apply from a script that may or may not be applied to this project");
			} else if (resolution.named()) {
				reading.opaque(where + ": apply from a script that a property or a variable names");
			} else if (!resolution.complete() || resolution.values().size() != 1
					|| resolution.values().get(0).contains("://")) {
				reading.opaque(where + ": apply from a script that cannot be located inside the project");
			} else {
				reading.applyScript(reading.root.resolve(resolution.values().get(0)), where);
			}
			return end;
		}

		/**
		 * Whether code here runs against this project: at the top level, or directly in
		 * {@code allprojects}.
		 */
		private boolean appliesToThisProject() {
			return blocks.isEmpty() || blocks.size() == 1 && List.of("allprojects").equals(blocks.peek().path());
		}

		/**
		 * The named arguments in this span, written {@code name: value} or
		 * {@code name = value}, each with the span of its value.
		 */
		private Map<String, int[]> namedArguments(int from, int to) {
			Map<String, int[]> arguments = new LinkedHashMap<>();
			for (int[] span : splitTopLevel(code, from, to)) {
				int nameStart = skipInactive(code, span[0]);
				int nameEnd = nameStart;
				while (nameEnd < span[1] && isIdentifierPart(code.charAt(nameEnd)) && code.charAt(nameEnd) != '.') {
					nameEnd++;
				}
				int separator = skipInactive(code, nameEnd);
				if (nameEnd > nameStart && separator < span[1] && (code.charAt(separator) == ':'
						|| code.charAt(separator) == '=' && !code.startsWith("==", separator))) {
					arguments.put(code.substring(nameStart, nameEnd), new int[] { separator + 1, span[1] });
				}
			}
			return arguments;
		}

		/**
		 * How the name ending at {@code end} is used. A name declared as a local
		 * variable or function, or passed as a named argument, is only read.
		 */
		private Use useOf(int start, int end) {
			int operator = skipInactive(code, end);
			if (operator >= code.length() || declaresALocal(start)) {
				return Use.READ;
			}
			char next = code.charAt(operator);
			if (next == '=') {
				return code.startsWith("==", operator) || namedArgument(start, end) ? Use.READ : Use.REPLACE;
			}
			if (code.startsWith("+=", operator)) {
				return kotlin ? Use.INERT : Use.APPEND;
			}
			if (code.substring(end, operator).indexOf('\n') >= 0) {
				return Use.READ;
			}
			if (next == '(') {
				return Use.CALL;
			}
			if (next == '{') {
				return Use.BLOCK;
			}
			return next == '\'' || next == '"' || isIdentifierStart(next) ? Use.COMMAND : Use.READ;
		}

		/**
		 * Whether the name starting here is being declared, as in {@code def srcDirs},
		 * {@code File srcDir} or {@code List<File> srcDirs}: a declaring word, a
		 * capitalised type, a closing type argument or {@code []} precedes it on the
		 * same line. A closure's {@code ->} does not count.
		 */
		private boolean declaresALocal(int start) {
			int index = start - 1;
			while (index >= 0 && (code.charAt(index) == ' ' || code.charAt(index) == '\t'
					|| code.charAt(index) == COMMENT_MASK)) {
				index--;
			}
			if (index < 0) {
				return false;
			}
			char previous = code.charAt(index);
			if (previous == '>') {
				return index == 0 || code.charAt(index - 1) != '-';
			}
			if (previous == ']') {
				return index > 0 && code.charAt(index - 1) == '[';
			}
			int wordStart = index;
			while (wordStart > 0 && isIdentifierPart(code.charAt(wordStart - 1)) && code.charAt(wordStart - 1) != '.') {
				wordStart--;
			}
			String word = code.substring(wordStart, index + 1);
			return DECLARING_WORDS.contains(word) || Character.isUpperCase(word.charAt(0));
		}

		/**
		 * Whether the plain name starting here opens a Kotlin named argument, as in
		 * {@code f(srcDir = x)}. In Groovy the same text is an assignment.
		 */
		private boolean namedArgument(int start, int end) {
			if (!kotlin || code.substring(start, end).indexOf('.') >= 0) {
				return false;
			}
			int index = start - 1;
			while (index >= 0 && (Character.isWhitespace(code.charAt(index)) || code.charAt(index) == COMMENT_MASK)) {
				index--;
			}
			return index >= 0 && (code.charAt(index) == '(' || code.charAt(index) == ',');
		}

		/**
		 * Reads the operand starting here. A list or an argument list ends at its
		 * closing bracket, and anything after that other than the end of the statement
		 * means it was not read in full. A bare operand ends at the end of its line.
		 */
		private Operand operandAt(int start) {
			char opener = start < code.length() ? code.charAt(start) : '\0';
			if (opener == '[' || opener == '(') {
				int close = matchingClose(code, start);
				if (close < 0) {
					return new Operand(Resolution.UNREAD, code.length());
				}
				Resolution resolution = opener == '[' ? resolve(start, close + 1) : resolveAll(start + 1, close);
				return new Operand(endsStatement(code, close + 1) ? resolution : partOf(resolution), close + 1);
			}
			int end = bareOperandEnd(start);
			Resolution resolution = resolveAll(start, end);
			boolean continues = end < code.length() && code.charAt(end) == '\n' && continuesTheExpression(end);
			return new Operand(continues ? partOf(resolution) : resolution, end);
		}

		/** The same values, no longer known to be all of them. */
		private static Resolution partOf(Resolution resolution) {
			return new Resolution(resolution.values(), false, resolution.named());
		}

		/**
		 * Where an operand without brackets ends: at a line break or {@code ;} outside
		 * any bracket, unless the line ends with a comma, or at a bracket closing
		 * around it.
		 */
		private int bareOperandEnd(int start) {
			int nesting = 0;
			int index = start;
			while (index < code.length()) {
				char current = code.charAt(index);
				if (current == '(' || current == '[' || current == '{') {
					nesting++;
				} else if (current == ')' || current == ']' || current == '}') {
					if (nesting == 0) {
						return index;
					}
					nesting--;
				} else if (nesting == 0 && (current == ';' || current == '\n' && !endsWithAComma(start, index))) {
					return index;
				}
				index++;
			}
			return index;
		}

		/**
		 * Whether the last code before this line break, after {@code from}, is a comma.
		 */
		private boolean endsWithAComma(int from, int lineBreak) {
			int index = lineBreak - 1;
			while (index >= from
					&& (Character.isWhitespace(code.charAt(index)) || code.charAt(index) == COMMENT_MASK)) {
				index--;
			}
			return index >= from && code.charAt(index) == ',';
		}

		/**
		 * Whether the next line carries the expression on, starting with an operator or
		 * a word in {@link #CONTINUING_WORDS} such as {@code as}.
		 */
		private boolean continuesTheExpression(int lineBreak) {
			int next = skipInactive(code, lineBreak);
			if (next >= code.length()) {
				return false;
			}
			int wordEnd = next;
			while (wordEnd < code.length() && isIdentifierPart(code.charAt(wordEnd)) && code.charAt(wordEnd) != '.') {
				wordEnd++;
			}
			return ".?+-*/:".indexOf(code.charAt(next)) >= 0
					|| CONTINUING_WORDS.contains(code.substring(next, wordEnd));
		}

		/**
		 * Resolves every comma-separated entry of this span, keeping what can be read.
		 */
		private Resolution resolveAll(int from, int to) {
			List<String> values = new ArrayList<>();
			boolean complete = true;
			boolean named = false;
			for (int[] span : splitTopLevel(code, from, to)) {
				Resolution entry = resolve(span[0], span[1]);
				values.addAll(entry.values());
				complete &= entry.complete();
				named |= entry.named();
			}
			return new Resolution(values, complete, named);
		}

		/**
		 * Resolves one entry, giving up past {@link #MAX_NESTING} entries inside one
		 * another rather than running out of stack.
		 */
		private Resolution resolve(int from, int to) {
			if (nesting >= MAX_NESTING) {
				return Resolution.UNREAD;
			}
			nesting++;
			try {
				return resolveEntry(from, to);
			} finally {
				nesting--;
			}
		}

		/**
		 * Resolves one entry: a list, a string literal, a property, or a call to
		 * {@code file}, {@code files}, {@code listOf} or {@code setOf} over entries
		 * like these. Anything else cannot be worked out without running Gradle.
		 */
		private Resolution resolveEntry(int from, int to) {
			int start = skipInactive(code, from);
			int end = to;
			while (end > start
					&& (Character.isWhitespace(code.charAt(end - 1)) || code.charAt(end - 1) == COMMENT_MASK)) {
				end--;
			}
			if (start >= end) {
				return new Resolution(List.of(), true, false);
			}
			char first = code.charAt(start);
			if (first == '[') {
				return matchingClose(code, start) == end - 1 ? resolveAll(start + 1, end - 1) : Resolution.UNREAD;
			}
			if (first == '\'' || first == '"') {
				boolean named = interpolatesAName(start, end);
				return staticLiteral(start, end).map(value -> new Resolution(List.of(value), true, named))
						.orElse(Resolution.UNREAD);
			}
			if (!isIdentifierStart(first)) {
				return Resolution.UNREAD;
			}
			int nameEnd = start;
			while (nameEnd < end && isIdentifierPart(code.charAt(nameEnd))) {
				nameEnd++;
			}
			String name = code.substring(start, nameEnd);
			if (nameEnd == end) {
				return propertyValue(name).map(value -> new Resolution(List.of(value), true, true))
						.orElse(Resolution.UNREAD);
			}
			int open = skipInactive(code, nameEnd);
			return open < end && code.charAt(open) == '(' && matchingClose(code, open) == end - 1
					? call(name, open, end - 1)
					: Resolution.UNREAD;
		}

		/**
		 * Whether the double-quoted literal filling this span interpolates anything,
		 * the project directory included, since a closure parameter or a variable of
		 * that name may stand in for it.
		 */
		private boolean interpolatesAName(int from, int to) {
			int start = skipInactive(code, from);
			return start < to && code.charAt(start) == '"' && content.substring(start, to).indexOf('$') >= 0;
		}

		/**
		 * The value of the string literal filling this span, where it interpolates
		 * nothing, so that no property or variable chose it.
		 */
		private Optional<String> unnamedLiteral(int from, int to) {
			return interpolatesAName(from, to) ? Optional.empty() : staticLiteral(from, to);
		}

		/**
		 * Resolves a call to one of the file or collection functions this reader knows.
		 * The result counts as named, so it gives main roots but no test root.
		 */
		private Resolution call(String name, int open, int close) {
			if (FILE_CALLS.contains(name)) {
				List<int[]> arguments = splitTopLevel(code, open + 1, close);
				Resolution argument = arguments.size() == 1 ? resolve(arguments.get(0)[0], arguments.get(0)[1])
						: Resolution.UNREAD;
				return argument.complete() && argument.values().size() == 1 ? named(argument) : Resolution.UNREAD;
			}
			return COLLECTION_CALLS.contains(name) ? named(resolveAll(open + 1, close)) : Resolution.UNREAD;
		}

		/**
		 * The same values, now counted as named: a call's result depends on what the
		 * name it calls stands for, which a script may change, for example with a
		 * method, a parameter or a variable of that name.
		 */
		private static Resolution named(Resolution resolution) {
			return new Resolution(resolution.values(), resolution.complete(), true);
		}

		/**
		 * The value of the one string literal filling this span, or empty where the
		 * span holds anything else. A single-quoted body is taken as written unless it
		 * has an escape; a double-quoted one goes through {@link #interpolate}.
		 */
		private Optional<String> staticLiteral(int from, int to) {
			int start = skipInactive(code, from);
			int end = to;
			while (end > start
					&& (Character.isWhitespace(code.charAt(end - 1)) || code.charAt(end - 1) == COMMENT_MASK)) {
				end--;
			}
			if (start >= end || code.charAt(start) != '\'' && code.charAt(start) != '"') {
				return Optional.empty();
			}
			int close = code.indexOf(code.charAt(start), start + 1);
			if (close != end - 1 || code.substring(start, close).indexOf('\n') >= 0) {
				return Optional.empty();
			}
			String body = content.substring(start + 1, close);
			if (code.charAt(start) == '"') {
				return interpolate(body);
			}
			return body.indexOf('\\') < 0 ? Optional.of(body) : Optional.empty();
		}

		/**
		 * The value of a double-quoted body. Only {@code $projectDir},
		 * {@code ${projectDir}}, {@code ${project.projectDir}} and properties are
		 * filled in; any other interpolation or an escape means it cannot be worked
		 * out.
		 */
		private Optional<String> interpolate(String body) {
			StringBuilder value = new StringBuilder(body.length());
			int index = 0;
			while (index < body.length()) {
				char current = body.charAt(index);
				if (current == '\\') {
					return Optional.empty();
				}
				if (current != '$') {
					value.append(current);
					index++;
					continue;
				}
				boolean braced = index + 1 < body.length() && body.charAt(index + 1) == '{';
				int nameStart = index + (braced ? 2 : 1);
				int nameEnd = braced ? body.indexOf('}', nameStart) : simpleNameEnd(body, nameStart);
				if (nameEnd < 0 || !braced && nameEnd + 1 < body.length() && body.charAt(nameEnd) == '.'
						&& isIdentifierStart(body.charAt(nameEnd + 1))) {
					return Optional.empty();
				}
				Optional<String> filled = interpolated(body.substring(nameStart, nameEnd).trim());
				if (filled.isEmpty()) {
					return Optional.empty();
				}
				value.append(filled.get());
				index = braced ? nameEnd + 1 : nameEnd;
			}
			return Optional.of(value.toString());
		}

		/**
		 * Where a name without braces after {@code $} ends: at the first character that
		 * cannot be part of it.
		 */
		private static int simpleNameEnd(String body, int from) {
			int index = from;
			while (index < body.length() && isIdentifierPart(body.charAt(index)) && body.charAt(index) != '.') {
				index++;
			}
			return index;
		}

		/** The value an interpolated name stands for, where this reader knows it. */
		private Optional<String> interpolated(String name) {
			if ("project.projectDir".equals(name) || "projectDir".equals(name) && !localNames.contains(name)) {
				return Optional.of(reading.root.toString());
			}
			return propertyValue(name);
		}

		/**
		 * Records a declaration against what its place names: main or test gets the
		 * operand, and an unattributed one leaves main incomplete and, if it replaces
		 * and is in this project, drops the test roots. Anything else is passed over.
		 */
		private void declare(int at, List<String> path, boolean replacing, Resolution resolution) {
			Slot slot = slotOf(path);
			String where = locationOf(at);
			if (slot == Slot.MAIN_JAVA) {
				reading.declare("main", replacing, resolution, where, script);
			} else if (slot == Slot.TEST_JAVA) {
				reading.declare("test", replacing, resolution, where, script);
			} else if (slot == Slot.UNATTRIBUTED) {
				reading.blocked.add(where + ": a source directory set outside sourceSets.main.java and "
						+ "sourceSets.test.java, which may reach the main source set");
				if (replacing && !reachesOnlyAnotherProject(path)) {
					reading.opaque(where + ": a replacement that may reach the test source set");
				}
			}
		}

		/** The names leading to this point, outermost first, followed by these. */
		private List<String> pathWith(List<String> extra) {
			Frame outer = blocks.peek();
			List<String> path = new ArrayList<>(outer == null ? List.of() : outer.path());
			brackets.forEach(path::addAll);
			path.addAll(head);
			path.addAll(extra);
			return path;
		}

		/** The script, relative to the project, and the line of this offset. */
		private String locationOf(int at) {
			long line = content.substring(0, at).chars().filter(character -> character == '\n').count() + 1;
			return reading.root.relativize(script) + ":" + line;
		}
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
			if (current == '[' || current == '(' || current == '{') {
				depth++;
			} else if (current == ']' || current == ')' || current == '}') {
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
	 * Whether the declaration is over at this offset rather than continuing into
	 * more of the same expression.
	 * <p>
	 * Only a definite boundary counts, because guessing the other way is what makes
	 * a partly read declaration look complete. A statement separator, the close of
	 * an enclosing block or call, a following statement, which a name other than a
	 * word in {@link #CONTINUING_WORDS} such as {@code as} starts, and the end of
	 * the descriptor all end it; anything else continues it, which covers
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
		if (isIdentifierStart(current)) {
			int end = index;
			while (end < code.length() && isIdentifierPart(code.charAt(end)) && code.charAt(end) != '.') {
				end++;
			}
			return !CONTINUING_WORDS.contains(code.substring(index, end));
		}
		return current == ';' || current == '}' || current == ')' || current == ']';
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
			roots.add(canonicaliseSourceRoot(root, conventional));
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
	 * Whether a string literal's body is a fixed path rather than something this
	 * reader would have to evaluate. Interpolation and undecoded escapes both mean
	 * it is not.
	 */
	private static boolean isStaticLiteral(String literal) {
		return literal.indexOf('$') < 0 && literal.indexOf('\\') < 0;
	}

	/**
	 * Says once that the production roots are not known to be all of them, and why.
	 * They are still offered, but the supervised package is not derived from them.
	 *
	 * @param productionComplete whether they are the whole source set
	 * @param descriptor         the descriptor they were read from
	 * @param reasons            what made them partial, with script and line;
	 *                           possibly empty
	 */
	private static void warnIfProductionRootsArePartial(boolean productionComplete, Path descriptor,
			List<String> reasons) {
		if (productionComplete) {
			return;
		}
		LOG.warn(
				"The main source set in {} is not known to be the whole of the production sources, so the "
						+ "supervised package will not be derived from them.{}",
				descriptor, reasons.isEmpty() ? "" : " Reasons: " + String.join("; ", reasons));
	}

	/**
	 * The canonical form of a source root inside the project.
	 * <p>
	 * Containment is decided here and nowhere else, so every path that becomes a
	 * root has passed through it. A root outside the project is refused rather than
	 * reported as one more directory that is not there, because the two mean
	 * different things.
	 *
	 * @param root      the project root
	 * @param candidate the path the descriptor named
	 * @return the candidate in canonical form
	 */
	private static Path canonicaliseSourceRoot(Path root, Path candidate) {
		Path canonical = BuildToolConfiguration
				.canonicalise(candidate.isAbsolute() ? candidate : root.resolve(candidate));
		if (!canonical.startsWith(root)) {
			throw new SecurityException("Configured source root escapes project root: " + candidate);
		}
		return canonical;
	}

	/**
	 * A declared source root in canonical form, or empty where it is absent.
	 * <p>
	 * A build tool collects nothing from a directory that is not there, so naming
	 * one is legal and passing it over keeps discovery going. An existing
	 * non-directory, and a path the filesystem will not answer for, stay errors.
	 *
	 * @param root       the project root
	 * @param candidate  the declared path
	 * @param descriptor the descriptor naming it
	 * @return the root, or empty where absent
	 */
	private static Optional<Path> resolveDeclaredRoot(Path root, Path candidate, Path descriptor) {
		Path canonical = canonicaliseSourceRoot(root, candidate);
		if (Files.isDirectory(canonical)) {
			return Optional.of(canonical);
		}
		if (!Files.notExists(canonical)) {
			throw new IllegalStateException("Configured source root is not a directory: " + candidate);
		}
		LOG.warn("The source root {} declared in {} is not there, so it is passed over. Its source set is no longer "
				+ "known to be the whole of the project.", candidate, descriptor);
		return Optional.empty();
	}

	/**
	 * The group the build descriptor itself declares, or empty where none can be
	 * taken literally.
	 * <p>
	 * Maven: the project's own {@code groupId}, never an inherited one. Gradle: a
	 * plain {@code group = '...'} at the top level, else {@code group} in
	 * {@code gradle.properties}; any other top-level use of {@code group} yields
	 * none rather than a guess.
	 *
	 * @param projectRoot the project root
	 * @param buildMode   the build tool of the project
	 * @return the declared group, possibly empty
	 */
	@Nonnull
	public static Optional<String> declaredGroup(@Nonnull Path projectRoot, @Nonnull BuildMode buildMode) {
		if (buildMode == BuildMode.MAVEN) {
			return mavenGroupOf(projectRoot.resolve("pom.xml"));
		}
		Optional<GroupSetting> setting = gradleGroupOf(gradleDescriptorOf(projectRoot));
		return setting.isPresent() ? setting.get().literal()
				: gradlePropertiesGroupOf(projectRoot.resolve("gradle.properties"));
	}

	/**
	 * The build script Gradle runs for the root project: {@code build.gradle} where
	 * it exists, even beside a {@code build.gradle.kts}, as measured with Gradle
	 * 9.8.0, else {@code build.gradle.kts}.
	 */
	private static Path gradleDescriptorOf(Path root) {
		Path groovy = root.resolve("build.gradle");
		return Files.isRegularFile(groovy) ? groovy : root.resolve("build.gradle.kts");
	}

	/**
	 * What the Gradle descriptor says about the root project's group. Its literal
	 * is empty where the descriptor touches the group in any way this reader does
	 * not take literally; that still overrides {@code gradle.properties}.
	 *
	 * @param literal the group set, or empty when it cannot be read literally
	 */
	private record GroupSetting(@Nonnull Optional<String> literal) {
	}

	/**
	 * The {@code groupId} that is a direct child of the descriptor's document
	 * element, read namespace-aware so that a prefixed element counts too.
	 */
	private static Optional<String> mavenGroupOf(Path descriptor) {
		if (!Files.isRegularFile(descriptor)) {
			return Optional.empty();
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			factory.setNamespaceAware(true);
			NodeList children = factory.newDocumentBuilder().parse(descriptor.toFile()).getDocumentElement()
					.getChildNodes();
			for (int index = 0; index < children.getLength(); index++) {
				Node child = children.item(index);
				if (child.getNodeType() == Node.ELEMENT_NODE && "groupId".equals(child.getLocalName())) {
					return literalGroup(child.getTextContent().trim());
				}
			}
			return Optional.empty();
		} catch (Exception exception) {
			LOG.debug("Could not read a groupId from {}", descriptor, exception);
			return Optional.empty();
		}
	}

	/**
	 * The root project's group as a Gradle descriptor sets it, read narrowly.
	 * <p>
	 * Only a top-level {@code group = '...'} or {@code project.group = '...'}
	 * counts. Any other use of the name anywhere, a task's group or a write inside
	 * a condition included, makes the answer unknown: telling which of them reach
	 * the root project would need a Groovy or Kotlin interpreter.
	 */
	private static Optional<GroupSetting> gradleGroupOf(Path descriptor) {
		if (!Files.isRegularFile(descriptor)) {
			return Optional.empty();
		}
		String content;
		try {
			content = Files.readString(descriptor);
		} catch (IOException exception) {
			LOG.debug("Could not read a group from {}", descriptor, exception);
			return Optional.of(new GroupSetting(Optional.empty()));
		}
		String code = maskInactiveRegions(content, Objects.toString(descriptor.getFileName(), "").endsWith(".kts"));
		if (reachesTheGroupByOtherMeans(content, code) || interpolationMentionsTheGroup(content, code)) {
			return Optional.of(new GroupSetting(Optional.empty()));
		}
		Matcher mention = GROUP_MENTION.matcher(code);
		if (!mention.find()) {
			return Optional.empty();
		}
		int start = mention.start();
		int end = mention.end();
		Optional<String> literal = !mention.find() && atTopLevel(code, start)
				&& !RETURN.matcher(code).region(0, start).find() ? plainLiteralAssignment(content, code, start, end)
						: Optional.empty();
		return Optional.of(new GroupSetting(literal));
	}

	/**
	 * Whether the descriptor uses {@code project} other than as a receiver or a
	 * call, as a local variable of that name would be declared, so that
	 * {@code project.group} might not reach Gradle's project.
	 */
	private static boolean projectMayBeRebound(String code) {
		Matcher name = PROJECT_NAME.matcher(code);
		while (name.find()) {
			int next = skipBlanks(code, name.end());
			if (next >= code.length() || code.charAt(next) != '.' && code.charAt(next) != '(') {
				return true;
			}
		}
		return false;
	}

	/**
	 * The literal a statement {@code group = '...'} or
	 * {@code project.group = '...'} sets, or empty for any other use of the name.
	 *
	 * @param content the descriptor as written
	 * @param code    the masked descriptor
	 * @param start   where the name begins
	 * @param end     where the name ends
	 * @return the literal group, or empty
	 */
	private static Optional<String> plainLiteralAssignment(String content, String code, int start, int end) {
		int statement = start;
		int before = skipBlanksBackwards(code, start - 1);
		if (before >= 0 && code.charAt(before) == '.') {
			int qualifier = skipBlanksBackwards(code, before - 1);
			statement = qualifier - "project".length() + 1;
			boolean namedProject = statement >= 0 && "project".equals(code.substring(statement, qualifier + 1))
					&& (statement == 0
							|| !isIdentifierPart(code.charAt(statement - 1)) && code.charAt(statement - 1) != '.');
			if (!namedProject || projectMayBeRebound(code)) {
				return Optional.empty();
			}
		}
		int equals = skipBlanks(code, end);
		boolean assigns = equals < code.length() && code.charAt(equals) == '='
				&& (equals + 1 >= code.length() || code.charAt(equals + 1) != '=');
		if (!assigns || !startsAStatement(code, statement) || !followsACompleteStatement(code, statement)) {
			return Optional.empty();
		}
		return literalRightHandSide(content, code, equals + 1).flatMap(ProjectSourcesFinder::literalGroup);
	}

	/**
	 * Whether a statement begins at this offset, so that {@code group} there is set
	 * rather than declared ({@code def group}, {@code String group}) or read
	 * ({@code println group}).
	 */
	private static boolean startsAStatement(String code, int start) {
		int index = skipBlanksBackwards(code, start - 1);
		return index < 0 || "\n\r{};".indexOf(code.charAt(index)) >= 0;
	}

	/**
	 * Whether the code before this statement is a finished statement, so that it
	 * cannot be the unbraced body of {@code if}, {@code else}, {@code for},
	 * {@code while} or {@code do}, nor the continuation of an expression.
	 */
	private static boolean followsACompleteStatement(String code, int statement) {
		int index = statement - 1;
		while (index >= 0 && (Character.isWhitespace(code.charAt(index)) || code.charAt(index) == COMMENT_MASK)) {
			index--;
		}
		if (index < 0 || "};'\"]".indexOf(code.charAt(index)) >= 0) {
			return true;
		}
		if (!isIdentifierPart(code.charAt(index))) {
			return false;
		}
		int wordStart = index;
		while (wordStart > 0 && isIdentifierPart(code.charAt(wordStart - 1))) {
			wordStart--;
		}
		String word = code.substring(wordStart, index + 1);
		return !"else".equals(word) && !"do".equals(word);
	}

	/**
	 * The literal that is the whole right-hand side, or empty when the value is
	 * computed.
	 *
	 * @param content the descriptor as written
	 * @param code    the masked descriptor
	 * @param from    where the value begins
	 * @return the literal, or empty
	 */
	private static Optional<String> literalRightHandSide(String content, String code, int from) {
		int open = skipBlanks(code, from);
		if (open >= code.length() || code.charAt(open) != '\'' && code.charAt(open) != '"') {
			return Optional.empty();
		}
		int close = code.indexOf(code.charAt(open), open + 1);
		int lineEnd = code.indexOf('\n', open);
		if (close < 0 || lineEnd >= 0 && close > lineEnd) {
			return Optional.empty();
		}
		int after = skipBlanks(code, close + 1);
		boolean ends = after >= code.length() || ";}".indexOf(code.charAt(after)) >= 0
				|| "\n\r".indexOf(code.charAt(after)) >= 0 && !continuesOnTheNextLine(code, after);
		return ends ? Optional.of(content.substring(open + 1, close)) : Optional.empty();
	}

	/**
	 * Whether the next line of code continues the value ended by this line break,
	 * as {@code .plus(...)} or {@code ?: '...'} would. Only a line that opens with
	 * a name, other than a word in {@link #CONTINUING_WORDS}, starts a new
	 * statement.
	 */
	private static boolean continuesOnTheNextLine(String code, int lineBreak) {
		int next = skipInactive(code, lineBreak);
		if (next >= code.length()) {
			return false;
		}
		if (!isIdentifierStart(code.charAt(next))) {
			return true;
		}
		int wordEnd = next;
		while (wordEnd < code.length() && Character.isLetterOrDigit(code.charAt(wordEnd))) {
			wordEnd++;
		}
		return CONTINUING_WORDS.contains(code.substring(next, wordEnd));
	}

	/** The first offset from here that is neither a blank nor a masked comment. */
	private static int skipBlanks(String code, int from) {
		int index = from;
		while (index < code.length() && isBlankOrComment(code.charAt(index))) {
			index++;
		}
		return index;
	}

	/** The last offset up to here that is neither a blank nor a masked comment. */
	private static int skipBlanksBackwards(String code, int from) {
		int index = from;
		while (index >= 0 && isBlankOrComment(code.charAt(index))) {
			index--;
		}
		return index;
	}

	/** Whether the character is a space, a tab or part of a masked comment. */
	private static boolean isBlankOrComment(char character) {
		return character == ' ' || character == '\t' || character == COMMENT_MASK;
	}

	/**
	 * Whether the code reaches the group through its accessors or by its name as a
	 * string literal of its own. An accessor name counts only as code, the string
	 * only where its opening quote is code, so text in comments or other strings
	 * does not count.
	 */
	private static boolean reachesTheGroupByOtherMeans(String content, String code) {
		Matcher other = GROUP_BY_OTHER_MEANS.matcher(content);
		while (other.find()) {
			char masked = code.charAt(other.start());
			boolean literal = other.group(1) != null;
			if (literal ? masked == other.group(1).charAt(0) : masked != COMMENT_MASK && masked != STRING_MASK) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the group may be reached from code interpolated into a string, as in
	 * {@code "${...}"}. Where that code ends cannot be told without parsing it, so
	 * any spelling of {@code group} from the first such interpolation on counts.
	 */
	private static boolean interpolationMentionsTheGroup(String content, String code) {
		int open = content.indexOf("${");
		while (open >= 0 && code.charAt(open) != STRING_MASK) {
			open = content.indexOf("${", open + 2);
		}
		return open >= 0 && content.substring(open).toLowerCase(Locale.ROOT).contains("group");
	}

	/**
	 * Whether no block is open at this offset. Comments and strings are masked, so
	 * every brace counted here is code.
	 */
	private static boolean atTopLevel(String code, int offset) {
		int depth = 0;
		for (int index = 0; index < offset; index++) {
			if (code.charAt(index) == '{') {
				depth++;
			} else if (code.charAt(index) == '}' && depth > 0) {
				depth--;
			}
		}
		return depth == 0;
	}

	/**
	 * The {@code group} entry of {@code gradle.properties}, read as Gradle does.
	 */
	private static Optional<String> gradlePropertiesGroupOf(Path file) {
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		Properties properties = new Properties();
		try (var reader = Files.newBufferedReader(file)) {
			properties.load(reader);
		} catch (IOException | IllegalArgumentException exception) {
			LOG.debug("Could not read a group from {}", file, exception);
			return Optional.empty();
		}
		String group = properties.getProperty("group");
		return group == null ? Optional.empty() : literalGroup(group.trim());
	}

	/** The value as a group, or empty when it is blank or would be evaluated. */
	private static Optional<String> literalGroup(String value) {
		return value.isBlank() || !isStaticLiteral(value) ? Optional.empty() : Optional.of(value);
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

	/**
	 * The legacy answer for Gradle: the value of {@code assignmentSrcDir} in the
	 * configured build.gradle, read as the old adapter did.
	 *
	 * @return the path, or empty where none is assigned or the file is unreadable
	 */
	private static Optional<Path> findLegacyGradleSource() {
		try {
			Matcher matcher = LEGACY_PROPERTY_ASSIGNMENT.matcher(Files.readString(Path.of(buildGradlePath)));
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
