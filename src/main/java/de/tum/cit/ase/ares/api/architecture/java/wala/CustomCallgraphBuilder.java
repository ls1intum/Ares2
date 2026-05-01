package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Utility to build a call graph for Java binaries using WALA framework.
 * <p>
 * Caches the analysis scope, class hierarchy, and the call graph itself per
 * JVM, keyed by the classpath strings, so that subsequent tests in the same
 * Gradle task reuse a single graph instead of rebuilding it for every test.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class CustomCallgraphBuilder {

	/** Substrings that disqualify a classpath entry from the analysis scope. */
	private static final List<String> CLASSPATH_EXCLUDE_SUBSTRINGS = List.of(
			// Test build outputs (compiled test sources and resources, across JVM languages)
			"/build/classes/java/test/", "/build/resources/test/",
			"/build/classes/groovy/test/", "/build/classes/kotlin/test/",
			// JUnit and supporting test-platform libraries
			"/junit-", "/junit5-", "/junit-jupiter", "/junit-platform", "/junit-vintage",
			"/opentest4j", "/apiguardian-api", "/junit-pioneer",
			// Mocking and test-double libraries
			"/mockito-", "/byte-buddy-agent", "/objenesis",
			// Assertion and property-based testing libraries
			"/assertj-", "/hamcrest", "/jqwik-",
			// Code coverage instrumentation
			"/jacoco", "/org.jacoco",
			// Gradle test-runner infrastructure
			"/gradle-worker", "/gradle-test-kit", "/test-kit",
			// Static analysis, linting and bug-finding tools
			"/spotbugs", "/spotbugsAnnotations",
			"/checkstyle", "/pmd-", "/spoon-",
			// Auxiliary parsing and graph libraries pulled in by analysis tooling
			"/javaparser-", "/jgrapht-", "/info.debatty",
			// Ares itself must never appear in its own analysis scope
			"/ares-", "/ares.jar",
			// WALA runtime, used to perform the analysis, must not be analysed
			"/com.ibm.wala", "/wala-",
			// AspectJ runtime and tooling, used by Ares' AOP layer
			"/aspectjweaver", "/aspectjrt", "/aspectjtools");

	private static final ConcurrentHashMap<String, CachedAnalysis> ANALYSIS_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, CallGraph> CALL_GRAPH_CACHE = new ConcurrentHashMap<>();

	private final ClassFileImporter classFileImporter;
	private final AnalysisScope scope;
	private final ClassHierarchy classHierarchy;
	private final String constructionClassPath;

	public CustomCallgraphBuilder(String classPath) {
		this.classFileImporter = new ClassFileImporter();
		this.constructionClassPath = classPath;
		CachedAnalysis cached = ANALYSIS_CACHE.computeIfAbsent(filterClassPath(classPath), CachedAnalysis::build);
		this.scope = cached.scope;
		this.classHierarchy = cached.classHierarchy;
	}

	/**
	 * Drops classpath entries that belong to the test framework, JaCoCo, the Ares
	 * agent, and the WALA/AspectJ runtime. Student code and any non-test library
	 * dependencies remain in the scope. Library code is reachable on demand via
	 * the analysis: only entries actually referenced from student-code entry
	 * points end up in the call graph.
	 */
	private static String filterClassPath(String classPath) {
		String[] entries = classPath.split(File.pathSeparator);
		LinkedHashSet<String> kept = Arrays.stream(entries)
				.filter(entry -> !entry.isBlank())
				.filter(entry -> {
					String normalized = entry.replace(File.separatorChar, '/');
					return CLASSPATH_EXCLUDE_SUBSTRINGS.stream().noneMatch(normalized::contains);
				})
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
		String result = String.join(File.pathSeparator, widened);
		try {
			java.nio.file.Files.writeString(java.nio.file.Paths.get("/tmp/wala-filter-trace.txt"),
					"input=" + classPath + "\nwidened=" + result + "\ncwd=" + System.getProperty("user.dir") + "\n",
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ignored) {}
		return result;
	}

	/**
	 * Helper subpackages that hold base classes / utilities shared across student
	 * categories. Listed explicitly (instead of widening the whole build root) so
	 * WALA's class hierarchy can resolve supertypes without dragging in every
	 * other category's Runnable / Thread / Executor implementations, which would
	 * blow up CHA-based dispatch resolution for Thread.start() and friends.
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
		return classHierarchy.getImmediateSubclasses(clazz).stream().map(IClass::getName)
				.map(Object::toString)
				.map(this::tryResolve)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	/**
	 * Builds the call graph for the given classpath, or returns a previously
	 * built graph cached on the JVM. The cache lives for the lifetime of the
	 * test JVM: the next Gradle run rebuilds it.
	 */
	public CallGraph buildCallGraph(String classPathToAnalyze) {
		long _profileStart = System.nanoTime();
		String cacheKey = filterClassPath(constructionClassPath) + "::" + filterClassPath(classPathToAnalyze);
		CallGraph cached = CALL_GRAPH_CACHE.get(cacheKey);
		if (cached != null) {
			long _hitMs = (System.nanoTime() - _profileStart) / 1_000_000L;
			try {
				java.nio.file.Files.writeString(java.nio.file.Paths.get("/tmp/wala-build-times.log"),
						System.currentTimeMillis() + "|HIT|" + _hitMs + "|" + cacheKey.hashCode() + "\n",
						java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
			} catch (Exception ignored) {}
			return cached;
		}
		try {
			// Build entry points from the WIDENED classpath so getEntryPointsFromStudentSubmission's
			// internal hierarchy can fully resolve student class supertypes
			// (anonymous.toolclasses.ProtectedResourceAccess, …); without that step,
			// the outer student classes are silently dropped and only standalone
			// helpers (inner enums) become entry points. After collection, restrict
			// the entry points to the test's own package directory so each
			// architecture rule only flags violations in the class under test rather
			// than spilling into unrelated category classes that share the wider scope.
			String narrowPackagePrefix = derivePackagePrefix(classPathToAnalyze);
			List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker
					.getEntryPointsFromStudentSubmission(filterClassPath(classPathToAnalyze), classHierarchy)
					.stream()
					.filter(ep -> narrowPackagePrefix == null
							|| ep.getMethod().getDeclaringClass().getName().toString()
									.startsWith(narrowPackagePrefix))
					.collect(Collectors.toCollection(ArrayList::new));
			AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
			CallGraphBuilder<?> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(),
					classHierarchy);
			// Util.makeZeroOneCFABuilder installs the default + bypass selectors on options. Wrap
			// them with a primordial-blackbox selector so WALA stops descending into JDK methods:
			// the call edge from student code to a JDK method is still recorded (so policy checks
			// for "calls java.io.FileInputStream.<init>" continue to work), but WALA does not load
			// or analyse the JDK method's bytecode. The class hierarchy still contains all JDK
			// types, so Ares' lookup-based policy decisions are unaffected. SSAPropagationCallGraphBuilder
			// reads options.getMethodTargetSelector() dynamically (see SSAPropagationCallGraphBuilder.java
			// around line 1577 in WALA 1.6.12), so wrapping after makeZeroOneCFABuilder takes effect.
			options.setSelector(new JdkOpaqueMethodTargetSelector(options.getMethodTargetSelector()));
			long _mkCgStart = System.nanoTime();
			CallGraph callGraph = builder.makeCallGraph(options, null);
			long _mkCgMs = (System.nanoTime() - _mkCgStart) / 1_000_000L;
			long _totalMs = (System.nanoTime() - _profileStart) / 1_000_000L;
			try {
				java.nio.file.Files.writeString(java.nio.file.Paths.get("/tmp/wala-build-times.log"),
						System.currentTimeMillis() + "|MISS|" + _totalMs + "|mkcg=" + _mkCgMs + "|" + cacheKey.hashCode() + "|" + classPathToAnalyze + "\n",
						java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
			} catch (Exception ignored) {}
			CallGraph existing = CALL_GRAPH_CACHE.putIfAbsent(cacheKey, callGraph);
			if (existing == null) {
				try {
					java.util.Set<String> anonClasses = new java.util.TreeSet<>();
					int totalCgNodes = 0;
					for (com.ibm.wala.ipa.callgraph.CGNode n : (Iterable<com.ibm.wala.ipa.callgraph.CGNode>) () -> callGraph.iterator()) {
						totalCgNodes++;
						String dn = n.getMethod().getDeclaringClass().getName().toString();
						if (dn.startsWith("Lanonymous/")) anonClasses.add(dn);
					}
					int chCount = 0;
					java.util.Set<String> chAnon = new java.util.TreeSet<>();
					for (com.ibm.wala.classLoader.IClass c : (Iterable<com.ibm.wala.classLoader.IClass>) () -> classHierarchy.iterator()) {
						chCount++;
						String n = c.getName().toString();
						if (n.startsWith("Lanonymous/")) chAnon.add(n);
					}
					StringBuilder sb = new StringBuilder();
					sb.append("CG totalNodes=").append(totalCgNodes).append("\n");
					sb.append("CG anon student classes (").append(anonClasses.size()).append("):\n");
					for (String c : anonClasses) sb.append("  ").append(c).append("\n");
					sb.append("Hierarchy total=").append(chCount).append(" anon=").append(chAnon.size()).append("\n");
					for (String c : chAnon) sb.append("  ").append(c).append("\n");
					java.nio.file.Files.writeString(java.nio.file.Paths.get("/tmp/wala-cg-stats.txt"), sb.toString());
				} catch (Exception ignored) {}
			}
			return existing != null ? existing : callGraph;
		} catch (Exception e) {
			throw new SecurityException(Messages.localized("security.architecture.build.call.graph.error"));
		}
	}


	/**
	 * Convert a classpath entry like {@code build/classes/java/main/anonymous/foo/bar}
	 * into the WALA TypeName prefix {@code Lanonymous/foo/bar/} so we can keep
	 * entry points limited to the test's package without re-deriving it from the
	 * policy. Returns {@code null} if the path doesn't look like a build-output
	 * package directory (in which case no narrowing is applied).
	 */
	private static String derivePackagePrefix(String classPath) {
		if (classPath == null) return null;
		String first = classPath.split(File.pathSeparator)[0].replace(File.separatorChar, '/');
		String[] markers = {"/build/classes/java/main/", "build/classes/java/main/",
				"/target/classes/", "target/classes/"};
		for (String marker : markers) {
			int idx = first.indexOf(marker);
			if (idx >= 0) {
				String pkg = first.substring(idx + marker.length());
				if (pkg.isBlank()) return null;
				if (!pkg.endsWith("/")) pkg = pkg + "/";
				return "L" + pkg;
			}
		}
		return null;
	}

	/**
	 * Treats every method declared in a primordial class (the JDK) as opaque: returns a
	 * {@link SummarizedMethod} with an empty {@link MethodSummary} for primordial targets so
	 * WALA records the call edge and creates a CGNode (preserving the JDK signature for
	 * downstream forbidden-call predicates such as {@code WalaRule.isForbidden}), but does
	 * not load or analyse the JDK method's bytecode. Non-primordial calls are forwarded to
	 * the wrapped selector chain set up by {@link Util#addDefaultSelectors} and
	 * {@link Util#addDefaultBypassLogic} (LambdaMethodTargetSelector → BypassMethodTargetSelector
	 * → ClassHierarchyMethodTargetSelector).
	 *
	 * <p>An earlier revision returned {@code null} for primordial targets, which removed the
	 * CGNode entirely. {@link de.tum.cit.ase.ares.api.architecture.java.wala.WalaRule#check}
	 * relies on those CGNodes to recognise forbidden JDK calls, so {@code null} silently
	 * disabled blocked-all detection. Returning a SummarizedMethod keeps the node and edge
	 * but skips body analysis. Summaries are cached by {@link MethodReference} to avoid
	 * per-call allocation during propagation.
	 */
	private static final class JdkOpaqueMethodTargetSelector implements MethodTargetSelector {

		private final MethodTargetSelector delegate;
		private final ConcurrentHashMap<MethodReference, SummarizedMethod> opaqueCache = new ConcurrentHashMap<>();

		JdkOpaqueMethodTargetSelector(MethodTargetSelector delegate) {
			this.delegate = delegate;
		}

		@Override
		public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
			IMethod target = delegate.getCalleeTarget(caller, site, receiver);
			if (target == null) {
				return null;
			}
			if (!target.getDeclaringClass().getClassLoader().getReference()
					.equals(ClassLoaderReference.Primordial)) {
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

	/** Cached scope and hierarchy keyed by filtered classpath. */
	private static final class CachedAnalysis {
		final AnalysisScope scope;
		final ClassHierarchy classHierarchy;

		private CachedAnalysis(AnalysisScope scope, ClassHierarchy classHierarchy) {
			this.scope = scope;
			this.classHierarchy = classHierarchy;
		}

		static CachedAnalysis build(String filteredClassPath) {
			try {
				AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
						filteredClassPath, FileTools.readFile(FileTools
								.resolveFileOnSourceDirectory("templates", "architecture", "java", "exclusions.txt")));
				ClassHierarchy hierarchy = ClassHierarchyFactory.make(scope);
				return new CachedAnalysis(scope, hierarchy);
			} catch (ClassHierarchyException | IOException e) {
				throw new SecurityException(Messages.localized("security.architecture.class.hierarchy.error"));
			}
		}
	}
}
