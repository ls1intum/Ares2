package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
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

	/** Substrings that disqualify a classpath entry from the analysis scope. */
	private static final List<String> CLASSPATH_EXCLUDE_SUBSTRINGS = List.of(
			// JUnit and supporting test-platform libraries
			"/junit-", "/junit5-", "/junit-jupiter", "/junit-platform", "/junit-vintage", "/opentest4j",
			"/apiguardian-api", "/junit-pioneer",
			// Mocking and test-double libraries
			"/mockito-", "/byte-buddy-agent", "/objenesis",
			// Assertion and property-based testing libraries
			"/assertj-", "/hamcrest", "/jqwik-",
			// Code coverage instrumentation
			"/jacoco", "/org.jacoco",
			// Gradle test-runner infrastructure
			"/gradle-worker", "/gradle-test-kit", "/test-kit",
			// Static analysis, linting and bug-finding tools
			"/spotbugs", "/spotbugsAnnotations", "/checkstyle", "/pmd-", "/spoon-",
			// Auxiliary parsing and graph libraries pulled in by analysis tooling
			"/javaparser-", "/jgrapht-", "/info.debatty",
			// Ares itself must never appear in its own analysis scope
			"/ares-", "/ares.jar",
			// WALA runtime, used to perform the analysis, must not be analysed
			"/com.ibm.wala", "/wala-",
			// AspectJ runtime and tooling, used by Ares' AOP layer
			"/aspectjweaver", "/aspectjrt", "/aspectjtools");

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
		Deque<JavaClass> pending = new ArrayDeque<>();
		Set<String> visitedClasses = new HashSet<>();
		Map<String, JavaClasses> importedJars = new HashMap<>();
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
				URL location = CustomCallgraphBuilder.class.getResource(convertTypeNameToClassName(targetName));
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
				Optional<String> classpathEntry = classpathEntryFor(location);
				if (classpathEntry.isEmpty()) {
					throw new SecurityException("Could not derive a classpath entry for reachable dependency " //$NON-NLS-1$
							+ targetName);
				}
				if (isTrustedFrameworkLocation(targetName, location, classpathEntry.get())) {
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
		return String.join(File.pathSeparator, entries);
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

	private static boolean isTrustedFrameworkLocation(String className, URL location, String classpathEntry) {
		String normalizedEntry = classpathEntry.replace(File.separatorChar, '/');
		if (CLASSPATH_EXCLUDE_SUBSTRINGS.stream().anyMatch(normalizedEntry::contains)) {
			return true;
		}
		if (!className.startsWith("de.tum.cit.ase.ares.api.")) {
			return false;
		}
		try {
			String aresCodeSource = CustomCallgraphBuilder.class.getProtectionDomain().getCodeSource().getLocation()
					.toExternalForm();
			return location.toExternalForm().startsWith(aresCodeSource);
		} catch (RuntimeException unavailableCodeSource) {
			return false;
		}
	}

	private static Optional<String> classpathEntryFor(URL location) {
		try {
			if ("file".equals(location.getProtocol())) {
				File classFile = new File(location.toURI());
				File packageDirectory = classFile.getParentFile();
				return packageDirectory == null ? Optional.empty() : Optional.of(packageDirectory.getPath());
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
		LinkedHashSet<String> kept = Arrays.stream(entries).filter(entry -> !entry.isBlank()).filter(entry -> {
			String normalized = entry.replace(File.separatorChar, '/');
			return CLASSPATH_EXCLUDE_SUBSTRINGS.stream().noneMatch(normalized::contains);
		}).collect(Collectors.toCollection(LinkedHashSet::new));

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
