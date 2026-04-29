package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;
//</editor-fold>

/**
 * Architecture test case for the Java programming language using WALA and
 * concrete product of the abstract factory design pattern.
 *
 * @author Sarp Sahinalp
 * @version 2.0.0
 * @see <a href=
 *      "https://refactoring.guru/design-patterns/abstract-factory">Abstract
 *      Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaWalaTestCase extends JavaArchitectureTestCase {

	// <editor-fold desc="Disk-backed rule outcome cache">

	/**
	 * In-memory mirror of the disk cache: maps "classpath fingerprint + rule" to
	 * the cached outcome of running that WALA rule.
	 *
	 * <p>WALA call-graph construction dominates the cost of {@code testAllModes}
	 * (~5–7s × #packages × #JVM forks). Gradle launches a separate JVM for each
	 * sub-task ({@code testUnprotected}, {@code testPermitted},
	 * {@code testBlockedPartial}, {@code testBlockedAll}), so the in-memory cache
	 * in {@code JavaCreator.cacheResult} doesn't cross fork boundaries. This
	 * disk-backed cache survives JVM exits and lets later forks short-circuit
	 * rule checks for which the outcome was already computed.
	 */
	/**
	 * Serializable holder for a cached rule outcome. {@link Optional} is intentionally
	 * not {@link Serializable} in the JDK, which caused silent persistence failure when
	 * the map values were {@code Optional<SecurityException>}.
	 */
	private static final class CachedOutcome implements Serializable {
		private static final long serialVersionUID = 1L;
		@Nullable
		final SecurityException exception;

		CachedOutcome(@Nullable SecurityException exception) {
			this.exception = exception;
		}
	}

	@Nonnull
	private static final ConcurrentHashMap<String, CachedOutcome> RULE_OUTCOME_CACHE =
			new ConcurrentHashMap<>();

	/**
	 * Persistent cache file location. Lives in the system temp directory so it
	 * survives across Gradle test forks but is cleaned by the OS or by the next
	 * Ares-jar rebuild (the jar mtime is folded into the cache key).
	 */
	@Nonnull
	private static final Path CACHE_FILE = Paths.get(System.getProperty("java.io.tmpdir"),
			"ares-wala-outcomes.bin");

	static {
		loadCacheFromDisk();
		Runtime.getRuntime().addShutdownHook(new Thread(JavaWalaTestCase::saveCacheToDisk,
				"ares-wala-outcomes-saver"));
	}

	private static void loadCacheFromDisk() {
		if (!Files.isRegularFile(CACHE_FILE)) {
			return;
		}
		try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(CACHE_FILE))) {
			Object loaded = in.readObject();
			if (loaded instanceof Map<?, ?> raw) {
				raw.forEach((k, v) -> {
					if (k instanceof String key && v instanceof CachedOutcome outcome) {
						RULE_OUTCOME_CACHE.put(key, outcome);
					}
				});
			}
		} catch (IOException | ClassNotFoundException | ClassCastException ignored) {
			// On any deserialization failure, start with an empty in-memory cache and
			// rebuild outcomes from scratch. The corrupt file will be overwritten on
			// the next save.
		}
	}

	private static void saveCacheToDisk() {
		if (RULE_OUTCOME_CACHE.isEmpty()) {
			return;
		}
		try {
			Path parent = CACHE_FILE.getParent();
			if (parent != null && !Files.isDirectory(parent)) {
				Files.createDirectories(parent);
			}
			try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(CACHE_FILE,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
				out.writeObject(new HashMap<>(RULE_OUTCOME_CACHE));
			}
		} catch (IOException ignored) {
			// Failure to persist the cache is non-fatal — the next run rebuilds.
		}
	}

	/**
	 * Build a stable cache key combining the analysis classpath, the Ares-jar mtime,
	 * the rule name, AND the per-package input fingerprint (sorted class names of
	 * {@code javaClasses} plus the sorted set of {@code allowedPackages}). The
	 * per-package fingerprint is essential: different student packages produce
	 * different call graphs and therefore different rule outcomes for the same
	 * rule name; without it, the first package's outcome would be served to every
	 * other package.
	 */
	@Nonnull
	private String cacheKey() {
		long aresMtime = 0L;
		try {
			Path own = Paths.get(JavaWalaTestCase.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI());
			aresMtime = Files.getLastModifiedTime(own).toMillis();
		} catch (Exception ignored) {
			// Fall back to the version-string component below.
		}
		String classpath = Stream.of(System.getProperty("java.class.path", "").split(java.io.File.pathSeparator))
				.filter(p -> p.contains("classes") || p.contains("build"))
				.sorted()
				.collect(Collectors.joining(java.io.File.pathSeparator));
		String classNamesFingerprint = javaClasses.stream()
				.map(JavaClass::getFullName)
				.sorted()
				.collect(Collectors.joining(","));
		String allowedPackagesFingerprint = allowedPackages.stream()
				.map(PackagePermission::importTheFollowingPackage)
				.sorted()
				.collect(Collectors.joining(","));
		return classpath + "|" + aresMtime + "|"
				+ ((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name()
				+ "|cls=" + Integer.toHexString(classNamesFingerprint.hashCode())
				+ "|pkg=" + Integer.toHexString(allowedPackagesFingerprint.hashCode());
	}

	// </editor-fold>

	// <editor-fold desc="Constructors">

	/**
	 * Lazy supplier of the WALA call graph used for rule checks.
	 *
	 * <p>When constructed via the eager-CallGraph constructor (kept for backward
	 * compatibility), this is a thunk over the already-resolved instance. When
	 * constructed via the supplier-based constructor (used by {@code JavaCreator}),
	 * the call graph is built only on the first cache miss in
	 * {@link #runRuleAndCapture}, so JVM forks whose rule outcomes are entirely
	 * served from disk never pay the call-graph construction cost.
	 */
	@Nonnull
	private final Supplier<CallGraph> callGraphSupplier;

	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull CallGraph callGraph) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph);
		this.callGraphSupplier = () -> callGraph;
	}

	/**
	 * Lazy variant: defer call-graph construction until the rule outcome is
	 * actually needed. Used by {@code JavaCreator.javaCreate} so that disk-cached
	 * rule outcomes can short-circuit before WALA touches the bytecode.
	 */
	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull Supplier<CallGraph> callGraphSupplier) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, /* callGraph */ null);
		this.callGraphSupplier = callGraphSupplier;
	}

	// </editor-fold>

	// <editor-fold desc="Write security test case methods">

	/**
	 * Formats the Set<PackagePermission> structure as a Java-literal
	 * Set.of(PackagePermission(...), ...).
	 */
	private String allowedPackagesAsCode() {
		if (allowedPackages.isEmpty()) {
			return "Set.of()";
		}
		String inner = allowedPackages.stream().map(pp -> String.format("new %s(\"%s\")",
				PackagePermission.class.getSimpleName(), pp.importTheFollowingPackage()))
				.collect(Collectors.joining(", "));
		return "Set.of(" + inner + ")";
	}

	/**
	 * Formats the JavaClasses structure as a Java-literal
	 * ClassFileImporter.importPackages(...) String.
	 */
	private String javaClassesAsCode() {
		Set<String> packages = javaClasses.stream().map(JavaClass::getPackageName)
				.collect(Collectors.toCollection(HashSet::new));

		if (packages.isEmpty()) {
			return "new ClassFileImporter().importPackages()";
		}
		String packagesAsString = packages.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", "));
		return "new ClassFileImporter().importPackages(" + packagesAsString + ")";
	}

	/**
	 * Formats the CallGraph structure as a Java-literal expression that builds a
	 * WALA CallGraph.
	 */
	private String callGraphAsCode() {
		String classPathExpr = "System.getProperty(\"java.class.path\")";
		return "new de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder(" + classPathExpr + ")"
				+ ".buildCallGraph(" + classPathExpr + ")";
	}

	/**
	 * Returns the content of the architecture test case file in the Java
	 * programming language.
	 */
	@Override
	@Nonnull
	public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			String testWithPlaceholders = FileTools
					.readRuleFile(FileTools.readFile(FileTools.resolveFileOnSourceDirectory("templates", "architecture",
							"java", "wala", "rules",
							((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name() + ".txt")))
					.stream().reduce("", (acc, line) -> acc + line + "\n");
			return testWithPlaceholders.replace("${allowedPackages}", allowedPackagesAsCode())
					.replace("${javaClasses}", javaClassesAsCode()).replace("${callGraph}", callGraphAsCode());
		} catch (AssertionError | IOException e) {
			throw new SecurityException(Messages.localized("architecture.illegal.statement", e.getMessage()));
		}
	}
	// </editor-fold>

	// <editor-fold desc="Execute security test case methods">

	/**
	 * Executes the architecture test case.
	 *
	 * <p>Outcomes are cached on disk keyed by classpath + Ares-jar mtime + rule name, so
	 * subsequent JVM forks (e.g. {@code testPermitted} after {@code testUnprotected})
	 * skip the expensive call-graph traversal when the same rule was already evaluated
	 * for the same classpath.
	 */
	@Override
	public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		JavaArchitectureTestCaseSupported supported =
				(JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported;
		// Priority rules (NATIVE_CODE, AGENT_ATTACH, ENVIRONMENT_ACCESS, MODULE_SYSTEM,
		// JNDI_INJECTION) always run in every test mode and delegate to ArchUnit. Their
		// outcome depends on which resources are permitted in the current test scenario,
		// but the allowedPackages component of the cache key is identical between
		// testPermitted and testBlockedAll for these rules (permitted agent/env/JNDI
		// resources do not contribute package imports). Skipping the disk cache for
		// these cases prevents a testBlockedAll violation from being served to
		// testPermitted.
		boolean skipCache = (supported == JavaArchitectureTestCaseSupported.NATIVE_CODE
				|| supported == JavaArchitectureTestCaseSupported.AGENT_ATTACH
				|| supported == JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS
				|| supported == JavaArchitectureTestCaseSupported.MODULE_SYSTEM
				|| supported == JavaArchitectureTestCaseSupported.JNDI_INJECTION);
		if (skipCache) {
			runRuleAndCapture(architectureMode, aopMode).ifPresent(e -> {
				throw e;
			});
			return;
		}
		String key = cacheKey();
		CachedOutcome cached = RULE_OUTCOME_CACHE.get(key);
		if (cached != null) {
			if (cached.exception != null) {
				throw cached.exception;
			}
			return;
		}
		CachedOutcome outcome = new CachedOutcome(runRuleAndCapture(architectureMode, aopMode).orElse(null));
		RULE_OUTCOME_CACHE.put(key, outcome);
		if (outcome.exception != null) {
			throw outcome.exception;
		}
	}

	/**
	 * Run the WALA rule for this case and translate the AssertionError outcome
	 * (if any) into a {@link SecurityException} so it can be cached uniformly.
	 */
	@Nonnull
	private Optional<SecurityException> runRuleAndCapture(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			JavaArchitectureTestCaseSupported supported = (JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported;
			// Resolve the call graph lazily and only for rules that actually need it.
			// PACKAGE_IMPORT and the JDK-delegated categories operate on javaClasses or
			// delegate to ArchUnit and never touch the call graph.
			boolean needsCallGraph = !(supported == JavaArchitectureTestCaseSupported.PACKAGE_IMPORT
					|| supported == JavaArchitectureTestCaseSupported.NATIVE_CODE
					|| supported == JavaArchitectureTestCaseSupported.AGENT_ATTACH
					|| supported == JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS
					|| supported == JavaArchitectureTestCaseSupported.MODULE_SYSTEM
					|| supported == JavaArchitectureTestCaseSupported.JNDI_INJECTION);
			CallGraph cg = needsCallGraph ? callGraphSupplier.get() : null;
			switch (supported) {
			case FILESYSTEM_INTERACTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM.check(cg);
			case NETWORK_CONNECTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK.check(cg);
			case COMMAND_EXECUTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(cg);
			case THREAD_CREATION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(cg);
			case PACKAGE_IMPORT -> JavaWalaTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages)
					.check(javaClasses);
			case REFLECTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION.check(cg);
			case TERMINATE_JVM -> JavaWalaTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM.check(cg);
			case SERIALIZATION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_SERIALIZE.check(cg);
			case CLASS_LOADING -> JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS.check(cg);
			case NATIVE_CODE, AGENT_ATTACH, ENVIRONMENT_ACCESS, MODULE_SYSTEM, JNDI_INJECTION ->
				// WALA's call-graph approach needs JDK methods inside the analysis scope to
				// trace a path from student code to a forbidden API. For these categories the
				// forbidden APIs (Runtime.load, ManagementFactory, java.lang.Module,
				// javax.naming, …) live in JDK packages outside the application analysis
				// scope. ArchUnit's class-level scan does see them — delegate.
				de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase.archunitBuilder()
						.javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported)
						.javaClasses(this.javaClasses)
						.allowedPackages(this.allowedPackages)
						.build()
						.executeArchitectureTestCase(architectureMode, aopMode);
			default -> throw new SecurityException(
					Messages.localized("security.common.unsupported.operation", this.architectureTestCaseSupported));
			}
			return Optional.empty();
		} catch (AssertionError ae) {
			try {
				JavaArchitectureTestCase.parseErrorMessage(ae);
				return Optional.empty();
			} catch (SecurityException parsed) {
				return Optional.of(parsed);
			}
		} catch (SecurityException sec) {
			// The NATIVE_CODE/etc. branch delegates to ArchUnit which throws SecurityException
			// directly. Preserve and cache.
			return Optional.of(sec);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Builder">

	/**
	 * Creates a new builder instance for constructing JavaArchitectureTestCase
	 * objects.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @return A new Builder instance
	 */
	@Nonnull
	public static JavaWalaTestCase.Builder walaBuilder() {
		return new JavaWalaTestCase.Builder();
	}

	/**
	 * Builder for the Java architecture test case.
	 */
	public static class Builder {
		@Nullable
		private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
		@Nullable
		private JavaClasses javaClasses;
		@Nullable
		private CallGraph callGraph;
		@Nullable
		private Set<PackagePermission> allowedPackages;

		/**
		 * Sets the architecture test case type supported by this instance.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaArchitectureTestCaseSupported The type of architecture test case
		 *                                          to support
		 * @return This builder instance for method chaining
		 * @throws SecurityException if the parameter is null
		 */
		@Nonnull
		public JavaWalaTestCase.Builder javaArchitectureTestCaseSupported(
				@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
			this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
					"javaArchitecturalTestCaseSupported must not be null");
			return this;
		}

		/**
		 * Sets the Java classes to be analyzed by this architecture test case.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaClasses Collection of Java classes for analysis
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder javaClasses(@Nonnull JavaClasses javaClasses) {
			this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
			return this;
		}

		/**
		 * Sets the call graph to be used for analysis. Required for WALA mode but not
		 * for ARCHUNIT mode.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param callGraph Call graph representing method relationships
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder callGraph(@Nullable CallGraph callGraph) {
			this.callGraph = callGraph;
			return this;
		}

		/**
		 * Sets the allowed package permissions.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param allowedPackages Set of package permissions that should be allowed
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
			this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
			return this;
		}

		/**
		 * Builds and returns a new JavaArchitectureTestCase instance with the
		 * configured properties.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @return A new JavaArchitectureTestCase instance
		 * @throws SecurityException if required parameters are missing
		 */
		@Nonnull
		public JavaWalaTestCase build() {
			return new JavaWalaTestCase(
					Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
							"javaArchitecturalTestCaseSupported must not be null"),
					Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null"),
					Preconditions.checkNotNull(javaClasses, "javaClasses must not be null"),
					Preconditions.checkNotNull(callGraph, "callGraph must not be null"));
		}
	}
	// </editor-fold>
}
