package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Utility to build a call graph for Java binaries using WALA framework.
 * <p>
 * Every builder owns one isolated WALA analysis session. WALA class hierarchies
 * are mutable while a call graph is built, so scopes, hierarchies, analysis
 * caches, entry points, and graphs must never be shared between policy
 * executions in the same JVM. A second request for the same analysed classpath
 * may reuse the graph inside this builder only.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class CustomCallgraphBuilder {

	/**
	 * Canonical code-source locations of the concrete framework artefacts loaded by
	 * Ares. Trust is an origin decision: filenames and directory fragments are
	 * entirely student-controlled and must never remove an entry from WALA's scope.
	 */
	private static final Set<Path> TRUSTED_FRAMEWORK_CODE_SOURCES = trustedFrameworkCodeSources(
			CustomCallgraphBuilder.class, CallGraph.class, JavaClasses.class, org.apiguardian.api.API.class,
			org.aspectj.lang.JoinPoint.class, org.assertj.core.api.Assertions.class, org.hamcrest.Matcher.class,
			org.junit.Test.class, org.junit.jupiter.api.Test.class, org.junit.jupiter.params.ParameterizedTest.class,
			org.junit.jupiter.engine.JupiterTestEngine.class, org.junit.platform.commons.JUnitException.class,
			org.junit.platform.engine.TestDescriptor.class, org.junit.platform.launcher.Launcher.class,
			org.junit.platform.testkit.engine.EngineTestKit.class, org.mockito.Mockito.class,
			org.opentest4j.AssertionFailedError.class, net.bytebuddy.ByteBuddy.class,
			net.bytebuddy.agent.ByteBuddyAgent.class, net.jqwik.api.Property.class,
			net.jqwik.engine.JqwikTestEngine.class);

	private final ClassFileImporter classFileImporter;
	private final AnalysisScope scope;
	private final ClassHierarchy classHierarchy;
	private String analysedClassPath;
	private CallGraph callGraph;

	public CustomCallgraphBuilder(String classPath) {
		this.classFileImporter = new ClassFileImporter();
		String filteredClassPath = filterClassPath(classPath);
		String expandedClassPath = expandClassPathWithReachableDependencies(filteredClassPath);
		try {
			this.scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(expandedClassPath,
					FileTools.readFile(FileTools.resolveFileOnSourceDirectory("templates", "architecture", "java",
							"exclusions.txt")));
			this.classHierarchy = ClassHierarchyFactory.make(scope);
		} catch (ClassHierarchyException | IOException e) {
			throw new SecurityException(Messages.localized("security.architecture.class.hierarchy.error"), e);
		}
	}

	/**
	 * Adds the bytecode locations of application classes referenced by the narrowly
	 * supervised package. Entry points remain restricted to that original package,
	 * but WALA can follow calls into helpers and third-party classes outside it.
	 * <p>
	 * Adding only the packages that contain reachable dependencies avoids widening
	 * the hierarchy to the complete build output, which previously made virtual
	 * dispatch over every unrelated {@link Runnable} implementation prohibitively
	 * expensive.
	 */
	private static String expandClassPathWithReachableDependencies(String classPath) {
		LinkedHashSet<String> entries = Arrays.stream(classPath.split(File.pathSeparator))
				.filter(entry -> !entry.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Path> projectClassRoots = discoverProjectClassRoots(entries);
		Deque<JavaClass> pending = new ArrayDeque<>();
		ClassFileImporter importer = new ClassFileImporter();

		for (String entry : entries) {
			File entryFile = new File(entry);
			if (!entryFile.isDirectory()) {
				continue;
			}
			try {
				pending.addAll(importer.importPath(entryFile.toPath()));
			} catch (RuntimeException exception) {
				throw new SecurityException("Could not import application classpath entry " + entry, exception); //$NON-NLS-1$
			}
		}

		addReachableDependencies(entries, pending, importer, projectClassRoots);
		return String.join(File.pathSeparator, entries);
	}

	/**
	 * Hashes every non-platform, non-framework bytecode location reachable from the
	 * imported application classes. This is the same dependency expansion used for
	 * the WALA scope and therefore invalidates cached verdicts when a reachable JAR
	 * or class directory is replaced at the same path.
	 */
	static String dependencyFingerprint(JavaClasses applicationClasses) {
		LinkedHashSet<String> entries = new LinkedHashSet<>();
		Deque<JavaClass> pending = new ArrayDeque<>(applicationClasses);
		addReachableDependencies(entries, pending, new ClassFileImporter(), Set.of());
		return fingerprintAnalysisEntries(entries);
	}

	private static void addReachableDependencies(LinkedHashSet<String> entries, Deque<JavaClass> pending,
			ClassFileImporter importer, Set<Path> projectClassRoots) {
		Set<String> visitedClasses = new HashSet<>();
		Map<String, JavaClasses> importedJars = new HashMap<>();
		while (!pending.isEmpty()) {
			JavaClass source = pending.removeFirst();
			if (!visitedClasses.add(source.getName())) {
				continue;
			}
			for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
				String targetName = normaliseDependencyClassName(dependency.getTargetClass().getName());
				if (targetName == null) {
					continue;
				}
				if (visitedClasses.contains(targetName)) {
					continue;
				}
				URL location = locateClass(targetName, projectClassRoots);
				if (location == null) {
					throw new SecurityException("Could not locate reachable application dependency " + targetName); //$NON-NLS-1$
				}
				if ("jrt".equals(location.getProtocol())) { //$NON-NLS-1$
					// Platform ownership is established by the module-image origin, not by a
					// package prefix. Third-party file:/jar: classes may legally use javax.* or
					// com.sun.* and must remain visible to the analysis.
					visitedClasses.add(targetName);
					continue;
				}
				if (!("file".equals(location.getProtocol()) || "jar".equals(location.getProtocol()))) {
					throw new SecurityException("Unsupported location for reachable application dependency " //$NON-NLS-1$
							+ targetName + ": " + location); //$NON-NLS-1$
				}
				Optional<String> classpathEntry = classpathEntryFor(location, targetName);
				if (classpathEntry.isEmpty()) {
					throw new SecurityException("Could not derive a classpath entry for reachable dependency " //$NON-NLS-1$
							+ targetName);
				}
				if (isTrustedFrameworkLocation(location, classpathEntry.get())) {
					visitedClasses.add(targetName);
					continue;
				}
				entries.add(classpathEntry.get());
				try {
					JavaClass resolved;
					if ("jar".equals(location.getProtocol())) {
						JavaClasses jarClasses = importedJars.computeIfAbsent(classpathEntry.get(),
								ignored -> importJar(importer, classpathEntry.get()));
						resolved = jarClasses.get(targetName);
					} else {
						resolved = importer.importUrl(location).get(targetName);
					}
					pending.addLast(resolved);
				} catch (RuntimeException exception) {
					throw new SecurityException("Could not resolve reachable application dependency " + targetName, //$NON-NLS-1$
							exception);
				}
			}
		}
	}

	private static URL locateClass(String targetName, Set<Path> projectClassRoots) {
		URL runtimeLocation = CustomCallgraphBuilder.class.getResource(convertTypeNameToClassName(targetName));
		if (runtimeLocation != null) {
			return runtimeLocation;
		}
		Path relativeClass = Path.of(targetName.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$
		for (Path classRoot : projectClassRoots) {
			Path candidate = classRoot.resolve(relativeClass);
			if (Files.isRegularFile(candidate)) {
				try {
					return candidate.toUri().toURL();
				} catch (IOException malformedUrl) {
					throw new SecurityException("Could not address reachable project class " + targetName, //$NON-NLS-1$
							malformedUrl);
				}
			}
		}
		return null;
	}

	/**
	 * Discovers conventional Maven and Gradle class-output roots in the current
	 * reactor/project. Roots are resolution candidates only: they enter WALA's
	 * scope when a class is actually reachable from the supervised package.
	 */
	private static Set<Path> discoverProjectClassRoots(Set<String> classpathEntries) {
		LinkedHashSet<Path> roots = new LinkedHashSet<>();
		for (String entry : classpathEntries) {
			Path supplied = Path.of(entry).toAbsolutePath().normalize();
			Path outputRoot = outputRootOf(supplied);
			if (outputRoot == null) {
				continue;
			}
			roots.add(outputRoot);
			Path projectRoot = containingProjectRoot(outputRoot);
			try (Stream<Path> candidates = Files.walk(projectRoot, 8)) {
				candidates.filter(Files::isDirectory).filter(CustomCallgraphBuilder::isConventionalClassRoot)
						.map(Path::toAbsolutePath).map(Path::normalize).forEach(roots::add);
			} catch (IOException unreadableProject) {
				throw new SecurityException("Could not discover project class-output roots below " + projectRoot, //$NON-NLS-1$
						unreadableProject);
			}
		}
		return Set.copyOf(roots);
	}

	private static Path containingProjectRoot(Path outputRoot) {
		Path candidate = outputRoot;
		while (candidate.getParent() != null && !isProjectDirectory(candidate)) {
			candidate = candidate.getParent();
		}
		while (candidate.getParent() != null && isProjectDirectory(candidate.getParent())) {
			candidate = candidate.getParent();
		}
		return candidate;
	}

	private static boolean isProjectDirectory(Path path) {
		return Files.isRegularFile(path.resolve("pom.xml")) || Files.isRegularFile(path.resolve("settings.gradle"))
				|| Files.isRegularFile(path.resolve("settings.gradle.kts"));
	}

	private static Path outputRootOf(Path path) {
		String normalized = path.toString().replace(File.separatorChar, '/');
		for (String marker : List.of("/target/test-classes", "/target/classes", "/build/classes/java/main",
				"/build/classes/java/test")) {
			int index = normalized.indexOf(marker);
			if (index >= 0) {
				return Path.of(normalized.substring(0, index + marker.length()));
			}
		}
		return null;
	}

	private static boolean isConventionalClassRoot(Path path) {
		String normalized = path.toString().replace(File.separatorChar, '/');
		return normalized.endsWith("/target/classes") || normalized.endsWith("/target/test-classes")
				|| normalized.endsWith("/build/classes/java/main") || normalized.endsWith("/build/classes/java/test");
	}

	private static String normaliseDependencyClassName(String className) {
		int componentStart = 0;
		while (componentStart < className.length() && className.charAt(componentStart) == '[') {
			componentStart++;
		}
		if (componentStart == 0) {
			return className;
		}
		if (componentStart >= className.length() || className.charAt(componentStart) != 'L'
				|| !className.endsWith(";")) { //$NON-NLS-1$
			return null;
		}
		return className.substring(componentStart + 1, className.length() - 1).replace('/', '.');
	}

	private static JavaClasses importJar(ClassFileImporter importer, String jarPath) {
		try {
			return importer.importUrl(Path.of(jarPath).toUri().toURL());
		} catch (IOException exception) {
			throw new SecurityException("Could not create a URL for application dependency JAR " + jarPath, //$NON-NLS-1$
					exception);
		}
	}

	private static boolean isTrustedFrameworkLocation(String classpathEntry) {
		try {
			return TRUSTED_FRAMEWORK_CODE_SOURCES.contains(Path.of(classpathEntry).toRealPath());
		} catch (IOException | RuntimeException unavailableLocation) {
			return false;
		}
	}

	private static boolean isTrustedFrameworkLocation(URL classResource, String classpathEntry) {
		if (isTrustedFrameworkLocation(classpathEntry)) {
			return true;
		}
		if (!"file".equals(classResource.getProtocol())) { //$NON-NLS-1$
			return false;
		}
		try {
			Path classFile = Path.of(classResource.toURI()).toRealPath();
			return TRUSTED_FRAMEWORK_CODE_SOURCES.stream().filter(Files::isDirectory).anyMatch(classFile::startsWith);
		} catch (IOException | java.net.URISyntaxException | RuntimeException unavailableLocation) {
			return false;
		}
	}

	private static Set<Path> trustedFrameworkCodeSources(Class<?>... frameworkClasses) {
		LinkedHashSet<Path> locations = new LinkedHashSet<>();
		for (Class<?> frameworkClass : frameworkClasses) {
			try {
				var codeSource = frameworkClass.getProtectionDomain().getCodeSource();
				if (codeSource != null && codeSource.getLocation() != null) {
					locations.add(Path.of(codeSource.getLocation().toURI()).toRealPath());
				}
			} catch (IOException | java.net.URISyntaxException | RuntimeException unavailableLocation) {
				// If an origin cannot be proven, do not trust it and leave it in the scope.
			}
		}
		return Set.copyOf(locations);
	}

	static String fingerprintAnalysisEntries(Set<String> classpathEntries) {
		try {
			String contentIdentity = classpathEntries.stream().map(CustomCallgraphBuilder::canonicalAnalysisEntry)
					.sorted().map(CustomCallgraphBuilder::analysisEntryFingerprint).collect(Collectors.joining(";")); //$NON-NLS-1$
			return sha256Hex(contentIdentity.getBytes(StandardCharsets.UTF_8));
		} catch (RuntimeException unreadableEntry) {
			if (unreadableEntry.getCause() instanceof IOException ioException) {
				throw new SecurityException("Could not fingerprint a WALA analysis dependency", ioException); //$NON-NLS-1$
			}
			throw unreadableEntry;
		}
	}

	private static Path canonicalAnalysisEntry(String classpathEntry) {
		try {
			return Path.of(classpathEntry).toRealPath();
		} catch (IOException unreadableEntry) {
			throw new java.io.UncheckedIOException(unreadableEntry);
		}
	}

	private static String analysisEntryFingerprint(Path entry) {
		try {
			if (Files.isRegularFile(entry)) {
				return "file#" + entry + "#" + sha256Hex(Files.readAllBytes(entry)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!Files.isDirectory(entry)) {
				throw new IOException("Analysis entry is neither a file nor a directory: " + entry); //$NON-NLS-1$
			}
			try (Stream<Path> files = Files.walk(entry)) {
				String directoryIdentity = files.filter(Files::isRegularFile).sorted()
						.map(path -> entry.relativize(path) + "#" + hashAnalysisFile(path)) //$NON-NLS-1$
						.collect(Collectors.joining(";")); //$NON-NLS-1$
				return "directory#" + entry + "#" + sha256Hex(directoryIdentity.getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (IOException unreadableEntry) {
			throw new java.io.UncheckedIOException(unreadableEntry);
		}
	}

	private static String hashAnalysisFile(Path path) {
		try {
			return sha256Hex(Files.readAllBytes(path));
		} catch (IOException unreadableFile) {
			throw new java.io.UncheckedIOException(unreadableFile);
		}
	}

	private static String sha256Hex(byte[] value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value); //$NON-NLS-1$
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte element : digest) {
				hex.append(Character.forDigit((element >> 4) & 0xF, 16)).append(Character.forDigit(element & 0xF, 16));
			}
			return hex.toString();
		} catch (java.security.NoSuchAlgorithmException unavailableAlgorithm) {
			throw new SecurityException("SHA-256 is unavailable", unavailableAlgorithm); //$NON-NLS-1$
		}
	}

	private static Optional<String> classpathEntryFor(URL location, String targetClassName) {
		try {
			if ("file".equals(location.getProtocol())) {
				Path classFile = Path.of(location.toURI()).toRealPath();
				Path relativeClassFile = Path.of(targetClassName.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$
				Path classpathRoot = classFile;
				for (int segment = 0; segment < relativeClassFile.getNameCount(); segment++) {
					classpathRoot = classpathRoot.getParent();
					if (classpathRoot == null) {
						return Optional.empty();
					}
				}
				return Optional.of(classpathRoot.toString());
			}
			if ("jar".equals(location.getProtocol())) {
				JarURLConnection connection = (JarURLConnection) location.openConnection();
				URI jarUri = connection.getJarFileURL().toURI();
				return Optional.of(new File(jarUri).getPath());
			}
		} catch (IOException | java.net.URISyntaxException | ClassCastException ignored) {
			// Unsupported locations remain unresolved and are handled by WALA.
		}
		return Optional.empty();
	}

	/**
	 * Drops classpath entries that belong to the test framework, JaCoCo, the Ares
	 * agent, and the WALA/AspectJ runtime. Student code and any non-test library
	 * dependencies remain in the scope. Library code is reachable on demand via the
	 * analysis: only entries actually referenced from student-code entry points end
	 * up in the call graph.
	 */
	private static String filterClassPath(String classPath) {
		String[] entries = classPath.split(File.pathSeparator);
		LinkedHashSet<String> kept = Arrays.stream(entries).filter(entry -> !entry.isBlank())
				.filter(entry -> !isTrustedFrameworkLocation(entry))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// Ares' BuildMode.getClasspath narrows the analysis classpath to a single
		// package subdirectory (e.g. build/classes/java/main/anonymous/foo/bar) so
		// WALA's analysis stays tractable. The cost of that narrowing is severe:
		// any student class whose superclass, helper class or returned-collection
		// type lives outside that package directory (e.g. extending a base class in
		// anonymous.toolclasses) cannot be resolved by the WALA class hierarchy and
		// gets silently dropped, leaving its methods out of the entry-point set and
		// hiding every forbidden API call inside them.
		//
		// Earlier revisions widened the scope to the entire build-root (e.g.
		// build/classes/java/main) which fixed the resolution problem but pulled in
		// every other category's classes — Thread.start() then dispatched via CHA
		// across all student Runnable subclasses, exploding the call graph
		// (ThreadSystemCreate Permitted: 343 s for the first invocation, JVM crash).
		// Add only the verified helper subdirectory (anonymous/toolclasses) instead
		// of the whole build-root.
		LinkedHashSet<String> widened = new LinkedHashSet<>(kept);
		for (String entry : kept) {
			String normalized = entry.replace(File.separatorChar, '/');
			String mainMarker = "build/classes/java/main";
			int idx = normalized.indexOf(mainMarker);
			if (idx >= 0) {
				String mainRoot = entry.substring(0, idx + mainMarker.length());
				addHelperRoots(widened, mainRoot);
				continue;
			}
			String mavenMarker = "target/classes";
			idx = normalized.indexOf(mavenMarker);
			if (idx >= 0) {
				String mavenMain = entry.substring(0, idx + mavenMarker.length());
				addHelperRoots(widened, mavenMain);
			}
		}
		return String.join(File.pathSeparator, widened);
	}

	/**
	 * Helper subpackages that hold base classes / utilities shared across student
	 * categories. Listed explicitly (instead of widening the whole build root) so
	 * WALA's class hierarchy can resolve supertypes without dragging in every other
	 * category's Runnable / Thread / Executor implementations, which would blow up
	 * CHA-based dispatch resolution for Thread.start() and friends.
	 */
	private static final List<String> HELPER_SUBPACKAGES = List.of("anonymous/toolclasses");

	private static void addHelperRoots(LinkedHashSet<String> target, String classRoot) {
		for (String subPackage : HELPER_SUBPACKAGES) {
			File helperDir = new File(classRoot, subPackage);
			if (helperDir.isDirectory()) {
				target.add(helperDir.getPath());
			}
		}
	}

	private static String convertTypeNameToClassName(String typeName) {
		if (typeName == null || typeName.isEmpty()) {
			throw new SecurityException(Messages.localized("security.architecture.class.type.resolution.error"));
		}
		return "/" + typeName.replace(".", "/") + ".class";
	}

	private static String convertTypeNameToWalaName(String typeName) {
		if (typeName == null || typeName.isEmpty()) {
			throw new SecurityException(Messages.localized("security.architecture.class.type.resolution.error"));
		}
		return "L" + typeName.replace('.', '/');
	}

	private Optional<JavaClass> tryResolve(String typeName) {
		List<String> ignoredTypeNames = List.of(
				"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions");
		if (ignoredTypeNames.contains(typeName)) {
			return Optional.empty();
		}
		return Optional.ofNullable(CustomCallgraphBuilder.class.getResource(convertTypeNameToClassName(typeName)))
				.map(location -> classFileImporter.withImportOption(loc -> !loc.contains("jrt")).importUrl(location))
				.map(imported -> {
					try {
						return imported.get(typeName);
					} catch (IllegalArgumentException e) {
						return null;
					}
				});
	}

	@SuppressWarnings("unused")
	public Set<JavaClass> getImmediateSubclasses(String typeName) {
		TypeReference reference = TypeReference.find(ClassLoaderReference.Application,
				convertTypeNameToWalaName(typeName));
		if (reference == null) {
			return Collections.emptySet();
		}
		IClass clazz = classHierarchy.lookupClass(reference);
		if (clazz == null) {
			return Collections.emptySet();
		}
		return classHierarchy.getImmediateSubclasses(clazz).stream().map(IClass::getName).map(Object::toString)
				.map(this::tryResolve).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
	}

	/**
	 * Builds the call graph for the given classpath. Repeated calls on this builder
	 * for the same path return its session-local graph; a builder cannot be reused
	 * for a different analysis target.
	 */
	public synchronized CallGraph buildCallGraph(String classPathToAnalyze) {
		String filteredClassPathToAnalyse = filterClassPath(classPathToAnalyze);
		validateClassPath(filteredClassPathToAnalyse);
		if (callGraph != null) {
			if (!analysedClassPath.equals(filteredClassPathToAnalyse)) {
				throw new SecurityException(Messages.localized("security.architecture.build.call.graph.error"));
			}
			return callGraph;
		}
		try {
			// Build entry points from this session's widened hierarchy so it can fully
			// resolve student class supertypes
			// (anonymous.toolclasses.ProtectedResourceAccess, …); without that step,
			// the outer student classes are silently dropped and only standalone
			// helpers (inner enums) become entry points. After collection, restrict
			// the entry points to the test's own package directory so each
			// architecture rule only flags violations in the class under test rather
			// than spilling into unrelated category classes that share the wider scope.
			String narrowPackagePrefix = derivePackagePrefix(classPathToAnalyze);
			List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker
					.getEntryPointsFromStudentSubmission(filteredClassPathToAnalyse, classHierarchy).stream()
					.filter(ep -> narrowPackagePrefix == null
							|| ep.getMethod().getDeclaringClass().getName().toString().startsWith(narrowPackagePrefix))
					.collect(Collectors.toCollection(ArrayList::new));
			AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
			CallGraphBuilder<?> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(),
					classHierarchy);
			// Util.makeZeroOneCFABuilder installs the default + bypass selectors on
			// options. Wrap
			// them with a primordial-blackbox selector so WALA stops descending into JDK
			// methods:
			// the call edge from student code to a JDK method is still recorded (so policy
			// checks
			// for "calls java.io.FileInputStream.<init>" continue to work), but WALA does
			// not load
			// or analyse the JDK method's bytecode. The class hierarchy still contains all
			// JDK
			// types, so Ares' lookup-based policy decisions are unaffected.
			// SSAPropagationCallGraphBuilder
			// reads options.getMethodTargetSelector() dynamically (see
			// SSAPropagationCallGraphBuilder.java
			// around line 1577 in WALA 1.6.12), so wrapping after makeZeroOneCFABuilder
			// takes effect.
			options.setSelector(new JdkOpaqueMethodTargetSelector(options.getMethodTargetSelector(), classHierarchy));
			callGraph = builder.makeCallGraph(options, null);
			analysedClassPath = filteredClassPathToAnalyse;
			return callGraph;
		} catch (Exception e) {
			// Chain the cause so a genuine analysis failure is diagnosable and never
			// silently mistaken for a clean result.
			throw new SecurityException(Messages.localized("security.architecture.build.call.graph.error"), e);
		}
	}

	private static void validateClassPath(String classPath) {
		boolean hasExistingEntry = Arrays.stream(classPath.split(File.pathSeparator)).filter(entry -> !entry.isBlank())
				.map(File::new).anyMatch(File::exists);
		if (!hasExistingEntry) {
			throw new SecurityException(Messages.localized("security.architecture.build.call.graph.error"));
		}
	}

	/**
	 * Convert a classpath entry like
	 * {@code build/classes/java/main/anonymous/foo/bar} into the WALA TypeName
	 * prefix {@code Lanonymous/foo/bar/} so we can keep entry points limited to the
	 * test's package without re-deriving it from the policy. Returns {@code null}
	 * if the path doesn't look like a build-output package directory (in which case
	 * no narrowing is applied).
	 */
	private static String derivePackagePrefix(String classPath) {
		if (classPath == null) {
			return null;
		}
		String first = classPath.split(File.pathSeparator)[0].replace(File.separatorChar, '/');
		String[] markers = { "/build/classes/java/main/", "build/classes/java/main/", "/build/classes/java/test/",
				"build/classes/java/test/", "/target/classes/", "target/classes/", "/target/test-classes/",
				"target/test-classes/" };
		for (String marker : markers) {
			int idx = first.indexOf(marker);
			if (idx >= 0) {
				String pkg = first.substring(idx + marker.length());
				if (pkg.isBlank()) {
					return null;
				}
				if (!pkg.endsWith("/")) {
					pkg = pkg + "/";
				}
				return "L" + pkg;
			}
		}
		return null;
	}

	/**
	 * Treats every method declared in a primordial class (the JDK) as opaque:
	 * returns a {@link SummarizedMethod} with an empty {@link MethodSummary} for
	 * primordial targets so WALA records the call edge and creates a CGNode
	 * (preserving the JDK signature for downstream forbidden-call predicates such
	 * as {@code WalaRule.isForbidden}), but does not load or analyse the JDK
	 * method's bytecode. Non-primordial calls are forwarded to the wrapped selector
	 * chain set up by {@link Util#addDefaultSelectors} and
	 * {@link Util#addDefaultBypassLogic} (LambdaMethodTargetSelector →
	 * BypassMethodTargetSelector → ClassHierarchyMethodTargetSelector).
	 * <p>
	 * An earlier revision returned {@code null} for primordial targets, which
	 * removed the CGNode entirely.
	 * {@link de.tum.cit.ase.ares.api.architecture.java.wala.WalaRule#check} relies
	 * on those CGNodes to recognise forbidden JDK calls, so {@code null} silently
	 * disabled blocked-all detection. Returning a SummarizedMethod keeps the node
	 * and edge but skips body analysis. Summaries are cached by
	 * {@link MethodReference} to avoid per-call allocation during propagation.
	 */
	private static final class JdkOpaqueMethodTargetSelector implements MethodTargetSelector {

		private final MethodTargetSelector delegate;
		private final ClassHierarchy classHierarchy;
		private final ConcurrentHashMap<MethodReference, SummarizedMethod> opaqueCache = new ConcurrentHashMap<>();

		JdkOpaqueMethodTargetSelector(MethodTargetSelector delegate, ClassHierarchy classHierarchy) {
			this.delegate = delegate;
			this.classHierarchy = classHierarchy;
		}

		@Override
		public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
			IMethod target = delegate.getCalleeTarget(caller, site, receiver);
			if (target == null) {
				// The delegate could not resolve the call. This happens for interface default
				// methods such as java.util.Collection.parallelStream() invoked on a receiver
				// that does not override them. Fall back to the statically declared target so
				// the JDK sink is recorded as an opaque node when WALA can represent it.
				target = classHierarchy.resolveMethod(site.getDeclaredTarget());
				if (target == null) {
					return null;
				}
			}
			if (!target.getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
				return target;
			}
			MethodReference ref = target.getReference();
			SummarizedMethod cached = opaqueCache.get(ref);
			if (cached != null) {
				return cached;
			}
			IClass declaringClass = target.getDeclaringClass();
			boolean isStatic = target.isStatic();
			SummarizedMethod fresh = opaqueCache.computeIfAbsent(ref, r -> {
				MethodSummary summary = new MethodSummary(r);
				summary.setStatic(isStatic);
				return new SummarizedMethod(r, summary, declaringClass);
			});
			return fresh;
		}
	}
}
