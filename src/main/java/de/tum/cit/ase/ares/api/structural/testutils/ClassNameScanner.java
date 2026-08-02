package de.tum.cit.ase.ares.api.structural.testutils;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static de.tum.cit.ase.ares.api.structural.testutils.ScanResultType.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;

import de.tum.cit.ase.ares.api.AresConfiguration;
import de.tum.cit.ase.ares.api.util.LruCache;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;
import de.tum.cit.ase.ares.api.util.StringSimilarity;

/**
 * This class scans the submission project if the current expected class is
 * actually present in it or not. The result is returned as an instance of
 * ScanResult. The ScanResult consists of a ScanResultType and a
 * ScanResultMessage as a string. ScanResultType is an enum and is implemented
 * so that identifying just the type of the error and the binding of several
 * messages to a certain result is possible.
 * <p>
 * There are the following possible results:
 * <ul>
 * <li>The class has the correct name and is placed in the correct package.</li>
 * <li>The class has the correct name but is misplaced.</li>
 * <li>The class name has wrong casing, but is placed in the correct
 * package.</li>
 * <li>The class name has wrong casing and is misplaced.</li>
 * <li>The class name has typos, but is placed in the correct package.</li>
 * <li>The class name has typos and is misplaced.</li>
 * <li>The class name has too many typos, thus is declared as not found.</li>
 * <li>Undefined, which is used to initialize the scan result.</li>
 * </ul>
 * <p>
 * A note on the limit of allowed number of typos: the maximal number depends on
 * the length of the class name and is defined as ceiling(classNameLength / 4).
 *
 * @author Stephan Krusche (krusche@in.tum.de)
 * @version 5.1 (2022-03-30)
 */
@API(status = Status.STABLE)
public class ClassNameScanner {

	private static final Logger LOG = LoggerFactory.getLogger(ClassNameScanner.class);

	/*
	 * A dedicated JavaParser instance per thread, configured for this project's
	 * Java 17 language level (records, pattern-matching instanceof, text blocks,
	 * ...), rather than the bare StaticJavaParser entry point: StaticJavaParser's
	 * default configuration only supports much older syntax and its shared static
	 * configuration is mutated as a side effect elsewhere (e.g.
	 * UnwantedNodesAssert#withLanguageLevel), which would make parsing here depend
	 * on unrelated test execution order. Mirrors the same language-level setup
	 * already used by JavaProjectScanner. Thread-local rather than a single shared
	 * instance: StructuralTestProvider documents that provider subclasses for
	 * different exercises may execute concurrently, and JavaParser does not
	 * guarantee that a single instance can safely be reused across threads.
	 */
	private static final ThreadLocal<JavaParser> JAVA_PARSER = ThreadLocal.withInitial(() -> new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)));

	/*
	 * Every ClassNameScanner instantiation re-walks and, since I-099, re-parses the
	 * whole assignment source tree from scratch (a pre-existing "one full scan per
	 * expected class" design this fix doesn't change) — a single structural test
	 * run constructs dozens of scanners across
	 * testAttributes/testClasses/testConstructors/ testMethods. Caching each file's
	 * discovered type names, keyed by absolute path and invalidated on
	 * last-modified-time change, means only the *first* scan in a run actually
	 * parses; every later scan of the same (unchanged) file reuses the result.
	 * Follows the same LruCache + Collections.synchronizedMap pattern already used
	 * for Messages' resource-bundle cache.
	 */
	private static final Map<Path, CachedFileTypes> JAVA_FILE_TYPE_CACHE = LruCache.synchronizedCache(4096);

	private record CachedFileTypes(long lastModifiedMillis, List<String> qualifiedTypeNames) {
	}

	/*
	 * The class name and package name of the expected class that is currently being
	 * searched after.
	 */
	private final String expectedClassName;
	private final String expectedPackageName;

	/**
	 * Mapping between the class name and the observed package names (list) in the
	 * project
	 */
	private final Map<String, List<String>> observedClasses = new HashMap<>();
	private final ScanResult scanResult;

	public ClassNameScanner(String expectedClassName, String expectedPackageName) {
		this.expectedClassName = expectedClassName;
		this.expectedPackageName = expectedPackageName;
		findObservedClassesInProject();
		this.scanResult = computeScanResult();
	}

	public ScanResult getScanResult() {
		return scanResult;
	}

	/**
	 * This method computes the scan result of the submission for the expected class
	 * name. It first checks if the class is in the project at all. If that's the
	 * case, it then checks if that class is properly placed or not and generates
	 * feedback accordingly. Otherwise the method loops over the observed classes
	 * and checks if any of the observed classes is actually the expected one but
	 * with the wrong case or types in the name. It again checks in each case if the
	 * class is misplaced or not and delivers the feedback. Finally, in none of
	 * these holds, the class is simply declared as not found.
	 *
	 * @return An instance of ScanResult containing the result type and the feedback
	 *         message.
	 */
	private ScanResult computeScanResult() {
		if (observedClasses.containsKey(expectedClassName)) {
			// the class was found in the correct package
			List<String> observedPackageNames = observedClasses.get(expectedClassName);
			return createScanResult(getScanResultTypeClassFound(observedPackageNames), expectedClassName,
					observedPackageNames.toString());
		}
		/*
		 * if the class was NOT found in the correct package, we try to to find it in a
		 * different package or try to find similar classes (e.g. with typos)
		 */
		for (var observedClass : observedClasses.entrySet()) {
			var foundObservedClassName = observedClass.getKey();
			var observedPackageNames = observedClass.getValue();
			var foundObservedPackageNames = observedPackageNames.toString();

			boolean classPresentMultiple = observedPackageNames.size() > 1;
			boolean classCorrectlyPlaced = !classPresentMultiple
					&& (observedPackageNames.contains(expectedPackageName));
			/*
			 * 1) check whether the class might have the wrong case
			 */
			if (foundObservedClassName.equalsIgnoreCase(expectedClassName)) {
				return createScanResult(foundObservedClassName, foundObservedPackageNames, classPresentMultiple,
						classCorrectlyPlaced, WRONG_CASE_MULTIPLE, WRONG_CASE_CORRECT_PLACE, WRONG_CASE_MISPLACED);
			}
			/*
			 * 2) check whether there are similar classes (e.g. the student has a small typo
			 * in the class name)
			 */
			if (isMisspelledWithHighProbability(expectedClassName, foundObservedClassName)) {
				return createScanResult(foundObservedClassName, foundObservedPackageNames, classPresentMultiple,
						classCorrectlyPlaced, TYPOS_MULTIPLE, TYPOS_CORRECT_PLACE, TYPOS_MISPLACED);
			}
		}
		return createScanResult(ScanResultType.NOTFOUND, expectedClassName, null);
	}

	private ScanResult createScanResult(String foundObservedClassName, String foundObservedPackageName,
			boolean classPresentMultiple, boolean classCorrectlyPlaced, ScanResultType multipleTimes,
			ScanResultType correctPlace, ScanResultType misplaced) {
		ScanResultType scanResultType;
		if (classPresentMultiple) {
			scanResultType = multipleTimes;
		} else {
			scanResultType = classCorrectlyPlaced ? correctPlace : misplaced;
		}
		return createScanResult(scanResultType, foundObservedClassName, foundObservedPackageName);
	}

	private ScanResultType getScanResultTypeClassFound(List<String> observedPackageNames) {
		boolean classIsPresentMultipleTimes = observedPackageNames.size() > 1;
		if (classIsPresentMultipleTimes) {
			return CORRECT_NAME_MULTIPLE;
		}
		boolean classIsCorrectlyPlaced = observedPackageNames.contains(expectedPackageName);
		return classIsCorrectlyPlaced ? CORRECT_NAME_CORRECT_PLACE : CORRECT_NAME_MISPLACED;
	}

	private ScanResult createScanResult(ScanResultType scanResultType, String foundClassName, String foundPackageName) {
		String scanResultMessage = createScanResultMessage(scanResultType, foundClassName, foundPackageName);
		return new ScanResult(scanResultType, scanResultMessage);
	}

	private String createScanResultMessage(ScanResultType scanResultType, String foundClassName,
			String foundPackageName) {
		var expectedPackageDescription = describePackageNameLocalized(expectedPackageName);
		var foundPackageDescription = describePackageNameLocalized(foundPackageName);
		switch (scanResultType) {
		case CORRECT_NAME_CORRECT_PLACE:
			return localized("structural.scan.correctNameCorrectPlace", foundClassName); //$NON-NLS-1$
		case CORRECT_NAME_MISPLACED:
			return localized("structural.scan.correctNameMisplaced", foundClassName, foundPackageDescription); //$NON-NLS-1$
		case CORRECT_NAME_MULTIPLE:
			return localized("structural.scan.correctNameMultiple", foundClassName, foundPackageDescription); //$NON-NLS-1$
		case WRONG_CASE_CORRECT_PLACE:
			return localized("structural.scan.wrongCaseCorrectPlace", expectedClassName, foundClassName); //$NON-NLS-1$
		case WRONG_CASE_MISPLACED:
			return localized("structural.scan.wrongCaseMisplaced", expectedClassName, expectedPackageDescription, //$NON-NLS-1$
					foundClassName, foundPackageDescription);
		case WRONG_CASE_MULTIPLE:
			return localized("structural.scan.wrongCaseMultiple", expectedClassName, expectedPackageDescription, //$NON-NLS-1$
					foundClassName, foundPackageDescription);
		case TYPOS_CORRECT_PLACE:
			return localized("structural.scan.typosCorrectPlace", expectedClassName, foundClassName); //$NON-NLS-1$
		case TYPOS_MISPLACED:
			return localized("structural.scan.typosMisplaced", expectedClassName, expectedPackageDescription, //$NON-NLS-1$
					foundClassName, foundPackageDescription);
		case TYPOS_MULTIPLE:
			return localized("structural.scan.typosMultiple", expectedClassName, expectedPackageDescription, //$NON-NLS-1$
					foundClassName, observedClasses.get(foundClassName).toString());
		case NOTFOUND:
			return localized("structural.scan.notFound", expectedClassName, expectedPackageDescription); //$NON-NLS-1$
		default:
			return localized("structural.scan.default"); //$NON-NLS-1$
		}
	}

	/**
	 * This method retrieves the actual type names and their packages by walking the
	 * project file structure. The root node (which is the assignment folder) is
	 * defined in the project build file (pom.xml or build.gradle) of the project.
	 */
	private void findObservedClassesInProject() {
		var assignmentFolderName = ProjectSourcesFinder.findProjectSourcesPath();
		if (assignmentFolderName.isPresent()) {
			walkProjectFileStructure(assignmentFolderName.get(), assignmentFolderName.get().toFile(), observedClasses);
		} else {
			LOG.error("Could not retrieve source directory from project file. Contact your instructor."); //$NON-NLS-1$ ´
		}
	}

	/**
	 * This method recursively walks the actual folder file structure starting from
	 * the assignment folder and adds each type it finds e.g. filenames ending with
	 * <code>.java</code> and <code>.kt</code> to the passed JSON object.
	 * <p>
	 * For <code>.java</code> files, every top-level type declared in the file (Java
	 * permits more than one) and every member/nested type declared inside those
	 * types is registered, not just the single type whose name matches the
	 * filename. A nested type is registered under its dot-separated
	 * qualified-within-file name (e.g. <code>Outer.Inner</code>), which
	 * {@link de.tum.cit.ase.ares.api.structural.StructuralTestProvider.ExpectedClassStructure#getQualifiedClassName()}
	 * translates into the corresponding JVM binary name (<code>Outer$Inner</code>)
	 * when loading the class. Local and anonymous classes are not addressable this
	 * way and are skipped. <code>.kt</code> files have no parser available here, so
	 * they keep the previous filename-derived single-type behaviour, as does a
	 * <code>.java</code> file that fails to parse (e.g. a submission that does not
	 * currently compile) — falling back rather than aborting the whole scan.
	 *
	 * @param assignmentFolder The root folder where the method starts walking the
	 *                         project structure.
	 * @param node             The current node the method is visiting.
	 * @param foundClasses     The JSON object where the type names and packages get
	 *                         appended.
	 */
	private void walkProjectFileStructure(Path assignmentFolder, File node, Map<String, List<String>> foundClasses) {
		// Example:
		// * assignmentFolderName: assignment/src
		// * fileName: assignment/src/de/tum/in/ase/eist/BubbleSort.java
		// Required Package Name: de.tum.in.ase.eist
		var fileName = node.getName();
		if (fileName.endsWith(".java")) { //$NON-NLS-1$
			registerJavaFileTypes(assignmentFolder, node, foundClasses);
		} else if (fileName.endsWith(".kt")) { //$NON-NLS-1$
			registerFoundClass(foundClasses, fileNameDerivedTypeName(fileName), packageNameOf(assignmentFolder, node));
		}
		if (node.isDirectory()) {
			String[] subNodes = node.list();
			if (subNodes != null && subNodes.length > 0) {
				for (String currentSubNode : subNodes) {
					walkProjectFileStructure(assignmentFolder, new File(node, currentSubNode), foundClasses);
				}
			}
		}
	}

	/**
	 * Registers every top-level and nested/member type a <code>.java</code> file
	 * declares (see {@link #qualifiedTypeNamesOf(File)}).
	 *
	 * @param assignmentFolder The root folder the package name is computed relative
	 *                         to.
	 * @param node             The <code>.java</code> file being visited.
	 * @param foundClasses     The map the discovered type names and packages get
	 *                         appended to.
	 */
	private void registerJavaFileTypes(Path assignmentFolder, File node, Map<String, List<String>> foundClasses) {
		var packageName = packageNameOf(assignmentFolder, node);
		for (String qualifiedName : qualifiedTypeNamesOf(node)) {
			registerFoundClass(foundClasses, qualifiedName, packageName);
		}
	}

	/**
	 * Returns every top-level and nested/member type name a <code>.java</code> file
	 * declares, dot-separated (e.g. <code>Outer.Inner</code>) via
	 * {@link #qualifiedNameWithinFile(TypeDeclaration)}, falling back to the single
	 * filename-derived type name if the file cannot be parsed (e.g. it does not
	 * currently compile). Cached by absolute path and last-modified time (see
	 * {@link #JAVA_FILE_TYPE_CACHE}) since a single structural test run constructs
	 * many {@code ClassNameScanner}s, each re-walking the same assignment tree.
	 *
	 * @param node The <code>.java</code> file to determine the declared type names
	 *             of.
	 * @return The file's declared type names.
	 */
	private static List<String> qualifiedTypeNamesOf(File node) {
		var absolutePath = node.toPath().toAbsolutePath().normalize();
		var lastModifiedMillis = node.lastModified();
		var cached = JAVA_FILE_TYPE_CACHE.get(absolutePath);
		if (cached != null && cached.lastModifiedMillis() == lastModifiedMillis) {
			return cached.qualifiedTypeNames();
		}
		var qualifiedTypeNames = parseQualifiedTypeNames(node);
		JAVA_FILE_TYPE_CACHE.put(absolutePath, new CachedFileTypes(lastModifiedMillis, qualifiedTypeNames));
		return qualifiedTypeNames;
	}

	private static List<String> parseQualifiedTypeNames(File node) {
		try {
			var parseResult = JAVA_PARSER.get().parse(node.toPath());
			var compilationUnit = parseResult.getResult()
					.orElseThrow(() -> new ParseProblemException(parseResult.getProblems()));
			List<String> qualifiedTypeNames = new ArrayList<>();
			for (TypeDeclaration<?> type : compilationUnit.findAll(TypeDeclaration.class)) {
				qualifiedNameWithinFile(type).ifPresent(qualifiedTypeNames::add);
			}
			return qualifiedTypeNames;
		} catch (IOException | ParseProblemException | StackOverflowError e) {
			LOG.debug("Could not parse '{}' for nested-type discovery; falling back to its filename-derived type name", //$NON-NLS-1$
					node, e);
			return List.of(fileNameDerivedTypeName(node.getName()));
		}
	}

	/**
	 * Computes the dot-separated name of a type declaration relative to its
	 * enclosing file, e.g. <code>Outer.Inner</code> for a member type
	 * <code>Inner</code> declared inside top-level type <code>Outer</code>, or just
	 * <code>Outer</code> for a top-level type. Local and anonymous classes (whose
	 * enclosing chain does not consist solely of type declarations up to the
	 * compilation unit) have no such stable name and are reported as empty.
	 *
	 * @param type The type declaration to compute the qualified-within-file name
	 *             for.
	 * @return The dot-separated qualified-within-file name, or empty if
	 *         {@code type} is a local or anonymous class.
	 */
	static Optional<String> qualifiedNameWithinFile(TypeDeclaration<?> type) {
		Deque<String> parts = new ArrayDeque<>();
		Node current = type;
		while (current instanceof TypeDeclaration<?> currentType) {
			parts.addFirst(currentType.getNameAsString());
			current = currentType.getParentNode().orElse(null);
		}
		if (!(current instanceof CompilationUnit)) {
			// the enclosing chain didn't resolve straight up to the compilation unit: a
			// local class (nested inside a method body) or similar, which has no stable
			// qualified-within-file name
			return Optional.empty();
		}
		return Optional.of(String.join(".", parts)); //$NON-NLS-1$
	}

	/**
	 * Derives a type's name from its source filename (the pre-existing behaviour,
	 * kept as the <code>.kt</code> path and the <code>.java</code> parse-failure
	 * fallback): the filename without its extension.
	 *
	 * @param fileName The source filename, e.g. <code>BubbleSort.java</code>.
	 * @return The filename-derived type name, e.g. <code>BubbleSort</code>.
	 */
	private static String fileNameDerivedTypeName(String fileName) {
		var fileNameComponents = fileName.split("\\."); //$NON-NLS-1$
		return fileNameComponents[fileNameComponents.length - 2];
	}

	/**
	 * Computes the dot-separated package name of a file from its path relative to
	 * the assignment folder.
	 *
	 * @param assignmentFolder The root folder the package name is computed relative
	 *                         to.
	 * @param node             The file whose package name is being computed.
	 * @return The dot-separated package name.
	 */
	private static String packageNameOf(Path assignmentFolder, File node) {
		Path packagePath = assignmentFolder.relativize(node.toPath().getParent());
		return StreamSupport.stream(packagePath.spliterator(), false).map(Object::toString)
				.collect(Collectors.joining(".")); //$NON-NLS-1$
	}

	/**
	 * Registers one discovered type name/package pairing in the found-classes map,
	 * appending to any package names already recorded under the same type name.
	 *
	 * @param foundClasses The map the discovered type name and package get appended
	 *                     to.
	 * @param className    The (possibly dot-qualified, for nested types) type name.
	 * @param packageName  The package the type was found in.
	 */
	private static void registerFoundClass(Map<String, List<String>> foundClasses, String className,
			String packageName) {
		foundClasses.computeIfAbsent(className, key -> new ArrayList<>()).add(packageName);
	}

	/**
	 * Returns the global Maven POM-file path used by Ares.
	 * <p>
	 * Defaults to the relative path <code>pom.xml</code>.
	 *
	 * @return the configured pom.xml file path as string
	 * @deprecated Moved to a more general package. Please use
	 *             {@link AresConfiguration#getPomXmlPath()} instead.
	 */
	@Deprecated(since = "1.12.0")
	public static String getPomXmlPath() {
		return AresConfiguration.getPomXmlPath();
	}

	/**
	 * Sets the global Maven POM-file path to the given file path string.
	 * <p>
	 * Set by default to the relative path <code>pom.xml</code>.
	 *
	 * @param path the path as string, may be both relative or absolute
	 * @deprecated Moved to a more general package. Please use
	 *             {@link AresConfiguration#setPomXmlPath(String)} instead.
	 */
	@Deprecated(since = "1.12.0")
	public static void setPomXmlPath(String path) {
		AresConfiguration.setPomXmlPath(path);
	}

	/**
	 * Returns the global Gradle build file path used by Ares.
	 * <p>
	 * Defaults to the relative path <code>build.gradle</code>.
	 *
	 * @return the configured gradle.build file path as string
	 * @deprecated Moved to a more general package. Please use
	 *             {@link AresConfiguration#getBuildGradlePath()} instead.
	 */
	@Deprecated(since = "1.12.0")
	public static String getBuildGradlePath() {
		return AresConfiguration.getBuildGradlePath();
	}

	/**
	 * Sets the global Gradle build file path to the given file path string.
	 * <p>
	 * Set by default to the relative path <code>build.gradle</code>.
	 *
	 * @param path the path as string, may be both relative or absolute
	 * @deprecated Moved to a more general package. Please use
	 *             {@link AresConfiguration#setBuildGradlePath(String)} instead.
	 */
	@Deprecated(since = "1.12.0")
	public static void setBuildGradlePath(String path) {
		AresConfiguration.setBuildGradlePath(path);
	}

	static boolean isMisspelledWithHighProbability(String a, String b) {
		/*
		 * This based on observations and experiments with
		 * https://github.com/src-d/datasets/tree/master/Typos and collections of real
		 * (not misspelled) classes.
		 */
		/*
		 * This is a fast check which should work often and not be a problem for the
		 * user to spot (as this requires a significant length difference). Such "long"
		 * typos seem to occur almost never by accident.
		 */
		int lengthDifferenceAbs = Math.abs(a.length() - b.length());
		if (lengthDifferenceAbs > 2) {
			return false;
		}
		/*
		 * This is the case for most typos, simply one missing or added character or two
		 * next to each other are swapped. (We only use this rule for strings with
		 * length of at least two)
		 */
		double distance = StringSimilarity.damerauLevenshteinDistance(a, b);
		if (distance <= 1.0 && Math.max(a.length(), b.length()) > 2) {
			return true;
		}
		/*
		 * We accept everything with a distance below two as typo. At three and above,
		 * misspelled identifiers can be easily recognized by a human or might not be
		 * spelling errors.
		 */
		if (distance > 2) {
			return false;
		}
		/*
		 * Otherwise, if the JW-similarity is below 0.9, it is unlikely that the two
		 * names should be the same. Often, they are opposites or different concepts
		 * that share some letters. It is similar for NL-similarity (which has some
		 * benefits for long strings).
		 */
		return StringSimilarity.jaroWinklerSimilarity(a, b) > 0.9
				|| StringSimilarity.normalizedLevenshteinSimilarity(a, b) > 0.9;
	}

	static String describePackageNameLocalized(String packageName) {
		if (packageName == null || packageName.isBlank()) {
			return localized("structural.scan.defaultPackage"); //$NON-NLS-1$
		}
		return packageName;
	}
}
