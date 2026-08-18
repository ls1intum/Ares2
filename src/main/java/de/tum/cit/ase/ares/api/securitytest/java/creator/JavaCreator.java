package de.tum.cit.ase.ares.api.securitytest.java.creator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.securitytest.ReservedPackageGuard;

/**
 * Creates security test cases for Java projects based on security policies.
 * <p>
 * Description: This class is responsible for generating architecture and
 * aspect-oriented programming (AOP) test cases based on the provided security
 * policies. It extracts necessary information from the project, prepares
 * allowed packages and classes, and creates both fixed and variable test cases.
 * <p>
 * Design Rationale: The JavaCreator follows the Creator design pattern to
 * encapsulate the complex process of generating security test cases. It
 * separates the creation logic from the execution and writing logic, adhering
 * to the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaCreator implements Creator {
	private static final Logger LOG = LoggerFactory.getLogger(JavaCreator.class);

	/**
	 * The supervised scope of the run in progress, and whether Ares derived it.
	 * <p>
	 * Held here rather than threaded through the three test-case builders, which
	 * would gain two parameters apiece for a value none of them reasons about. A
	 * creator is built per director and {@code createTestCases} is called once on
	 * it, so this is state for one run rather than shared state.
	 */
	private String supervisedPackage;

	/** Whether {@link #supervisedPackage} was derived rather than pinned. */
	private boolean supervisedScopeWasDerived;
	private final BuildToolConfiguration buildConfiguration;

	public JavaCreator() {
		this.buildConfiguration = null;
	}

	public JavaCreator(@Nonnull BuildToolConfiguration buildConfiguration) {
		this.buildConfiguration = java.util.Objects.requireNonNull(buildConfiguration,
				"buildConfiguration must not be null");
	}

	// Within-run memoisation of the resolved classpath, imported JavaClasses and
	// call
	// graph, shared across the test cases built by a single createTestCases call.
	// This
	// is intentionally an INSTANCE field, not static: a JavaCreator lives for
	// exactly
	// one factory build, so the cache cannot leak across runs or serve one run's
	// analysis (compiled classes / call graph) to a later run in a long-lived JVM.
	// The
	// only cross-process sharing that must survive forks is handled separately by
	// the
	// disk-backed WALA outcome cache, which is keyed by a per-package fingerprint.
	private final Map<String, Object> cache = new ConcurrentHashMap<>();

	// <editor-fold desc="Helper methods">

	// TODO Markus: Move to a better place

	/**
	 * Caches the result of a supplier to avoid recomputation based on a classPath
	 * key.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param classPath the classPath key to use for caching; must not be null
	 * @param supplier  the supplier to cache the result for; must not be null
	 * @param <T>       the type of the result
	 * @return a supplier that returns the cached result; never null
	 */
	@SuppressWarnings("unchecked")
	public <T> Supplier<T> cacheResult(@Nonnull String classPath, @Nonnull Supplier<T> supplier) {
		return () -> (T) cache.computeIfAbsent(classPath, k -> supplier.get());
	}

	/**
	 * Prepares the set of allowed packages based on essential packages, security
	 * policy, and test classes.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param resourceAccesses   the resource accesses permitted by the security
	 *                           policy; must not be null
	 * @param essentialPackages  the list of essential packages; must not be null
	 * @param packageName        the name of the package containing the main class;
	 *                           must not be null
	 * @param supervisedPackages the packages actually declared by the validated
	 *                           production output; must not be null
	 * @param testClasses        the list of test classes whose packages should be
	 *                           allowed; must not be null
	 * @return a set of allowed package permissions; never null
	 */
	@Nonnull
	private Set<PackagePermission> prepareAllowedPackages(@Nonnull List<String> essentialPackages,
			@Nonnull ResourceAccesses resourceAccesses, @Nonnull String packageName,
			@Nonnull Set<String> supervisedPackages, @Nonnull List<String> testClasses) {
		return Stream.of(
				// Essential packages are allowed to do anything
				essentialPackages.stream().filter(p -> p != null && !p.isBlank()).map(PackagePermission::new),
				// The permitted packages are allowed
				resourceAccesses.regardingPackageImports().stream(),
				/*
				 * The supervised code is allowed to use its own code, else a student could not
				 * call one of their own classes from another. SECURITY: name the packages the
				 * validated production output actually declares rather than the supervised
				 * prefix. A permission is matched as a prefix, so seeding a scope of de.tum.cit
				 * would have permitted every import from de.tum.cit.ase.ares.api along with it,
				 * which is the top-level-root hazard this comment used to warn about while the
				 * scanner was free to hand one over.
				 */
				supervisedPackages.isEmpty() ? (packageName != null && !packageName.isBlank()
						// Nothing is compiled, so nothing can be imported from the supervised
						// code either and this permission grants no reachable class. It is left
						// unguarded because packageName is the instructor's own declaration
						// whenever a policy pinned it, and a pinned scope such as
						// de.tum.cit.ase.ares legitimately sits above a reserved namespace. The
						// case where it is instead a derived scope over an output that turns
						// out to hold nothing importable is refused at execution, where the
						// compiled inventory is the whole truth.
						? Stream.of(new PackagePermission(packageName))
						: Stream.<PackagePermission>empty())
						: supervisedPackages.stream().map(JavaCreator::derivedAllowedPackage),
				/*
				 * The packages of the test classes are allowed (test infrastructure classes
				 * like ProtectedResourceAccess need to be accessible from the supervised code)
				 */
				testClasses.stream().filter(java.util.Objects::nonNull).map(className -> {
					int lastDot = className.lastIndexOf('.');
					return lastDot > 0 ? className.substring(0, lastDot) : className;
				}).filter(p -> !p.isBlank()).distinct().map(JavaCreator::testClassAllowedPackage)

		).flatMap(Function.identity()).collect(Collectors.toSet());
	}

	/**
	 * Wraps a package permission that was read out of the project rather than
	 * declared by a policy, refusing one that would carry a trusted namespace with
	 * it.
	 * <p>
	 * A permission is matched on segment boundaries but still as a prefix, so
	 * permitting a package permits everything below it. A package name is exactly
	 * what whoever adds files to the project controls, so a derived permission that
	 * happens to sit above {@code de.tum.cit.ase.ares.api} would hand the
	 * supervised code the framework's own namespace. The reserved-package guard
	 * does not catch it, because that one refuses a package <em>inside</em> a
	 * trusted prefix rather than one <em>containing</em> it.
	 * <p>
	 * Only derived permissions are held to this. What a policy names in
	 * {@code theFollowingResourceAccessesArePermitted}, and the essential packages
	 * Ares needs itself, are declarations rather than readings and stay
	 * authoritative.
	 *
	 * @param packageName the derived package name
	 * @return the permission for it
	 * @throws SecurityException when a reserved namespace lies below it
	 */
	@Nonnull
	private static PackagePermission derivedAllowedPackage(@Nonnull String packageName) {
		String reserved = ReservedPackageGuard.ancestorOfReservedPrefix(packageName);
		if (reserved != null) {
			throw new SecurityException(Messages.localized("security.policy.ancestor.package", packageName, reserved));
		}
		return new PackagePermission(packageName);
	}

	/**
	 * The permission taken from the package of a declared or scanned test class.
	 * <p>
	 * Held to the same question as {@link #derivedAllowedPackage(String)} but not
	 * to the same answer. A test class sits in the test tree, which in an Artemis
	 * exercise the instructor controls and the submitter does not, so a permission
	 * read from it is not the submitter-steerable value the supervised scope is.
	 * Refusing here would also refuse a convention this repository's own fixtures
	 * rely on: nine of them name {@code de.tum.cit.ase.ares.testutilities}, a
	 * package rather than a class, whose enclosing package is exactly such an
	 * ancestor.
	 * <p>
	 * Those two facts make refusal the wrong instrument, not the finding wrong. The
	 * grant really is wider than anything a test class needs, so it is reported
	 * rather than made fatal, and a policy that trips it should name its test
	 * classes instead of their package.
	 *
	 * @param packageName the package of a test class
	 * @return the permission for it
	 */
	@Nonnull
	private static PackagePermission testClassAllowedPackage(@Nonnull String packageName) {
		String reserved = ReservedPackageGuard.ancestorOfReservedPrefix(packageName);
		if (reserved != null) {
			LOG.warn("A test class was declared in the package {}, which permits the supervised code to import "
					+ "everything below it, including the trusted namespace {}. Name the test classes "
					+ "themselves rather than their package, or declare what may be imported in "
					+ "theFollowingResourceAccessesArePermitted.", packageName, reserved);
		}
		return new PackagePermission(packageName);
	}

	/**
	 * Prepares the set of allowed classes based on the essential classes and
	 * security policy.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialClasses the list of essential classes; must not be null
	 * @param testClasses      the list of test classes; must not be null
	 * @return a set of allowed class names; never null
	 */
	@Nonnull
	private Set<ClassPermission> prepareAllowedClasses(@Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses) {
		return Stream.of(
				// Essential classes are allowed to do anything
				essentialClasses.stream(),
				// Test classes are allowed to do anything
				testClasses.stream()).flatMap(Function.identity())
				// Filter null/blank before constructing ClassPermission: its constructor throws
				// on null/blank, so an unfiltered bad entry (e.g. from a scanned project or a
				// malformed policy) would otherwise abort all test-case creation. Mirrors
				// prepareAllowedPackages.
				.filter(java.util.Objects::nonNull).filter(className -> !className.isBlank()).map(ClassPermission::new)
				.collect(Collectors.toSet());
	}

	/**
	 * Creates a JavaArchitectureTestCase with the given parameters.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param supported         the supported architecture test case type; must not
	 *                          be null
	 * @param classes           the Java classes to analyse; must not be null
	 * @param callGraphSupplier the call graph supplier to use; must not be null
	 * @param allowedPackages   the set of allowed package permissions; must not be
	 *                          null
	 * @param allowedClasses    the set of allowed class names; must not be null
	 * @return a new JavaArchitectureTestCase; never null
	 */
	@Nonnull
	private JavaArchitectureTestCase createArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported supported,
			@Nonnull JavaClasses classes, @Nonnull Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull Set<ClassPermission> allowedClasses) {
		return JavaArchitectureTestCase.builder()
				// The scope the generated file asks for at runtime, and whether it has to be
				// checked against the whole compiled output first
				.supervisedPackage(supervisedPackage).supervisedScopeWasDerived(supervisedScopeWasDerived)
				// The architecture test case checks for the following aspect
				.javaArchitectureTestCaseSupported(supported)
				// The architecture test cases are built over the following classes
				.javaClasses(classes)
				// The architecture test cases are built over the following call graph
				.callGraphSupplier(callGraphSupplier)
				// The following packages are allowed
				.allowedPackages(allowedPackages)
				// The following classes are exempt (essential + test infrastructure)
				.allowedClasses(allowedClasses)
				// Build the architecture test case
				.build();
	}

	/**
	 * Maps an AOP test-case category to its architecture counterpart with an
	 * exhaustive switch, so a future enum divergence is caught at compile time
	 * rather than as a runtime {@link IllegalArgumentException}.
	 *
	 * @param supported the supported AOP test-case category; must not be null
	 * @return the corresponding architecture test-case category; never null
	 */
	@Nonnull
	private static JavaArchitectureTestCaseSupported architectureCounterpartOf(
			@Nonnull JavaAOPTestCaseSupported supported) {
		return switch (supported) {
		case FILESYSTEM_INTERACTION -> JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION;
		case NETWORK_CONNECTION -> JavaArchitectureTestCaseSupported.NETWORK_CONNECTION;
		case COMMAND_EXECUTION -> JavaArchitectureTestCaseSupported.COMMAND_EXECUTION;
		case THREAD_CREATION -> JavaArchitectureTestCaseSupported.THREAD_CREATION;
		};
	}

	/**
	 * Creates a JavaAOPTestCase with the given parameters.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param resourceAccesses          the resource accesses permitted by the
	 *                                  security policy; must not be null
	 * @param javaArchitectureTestCases the list of architecture test cases to
	 *                                  populate; must not be null
	 * @param supported                 the supported AOP test case type; must not
	 *                                  be null
	 * @param classes                   the Java classes to analyse; must not be
	 *                                  null
	 * @param callGraphSupplier         the call graph supplier to use; must not be
	 *                                  null
	 * @param allowedPackages           the set of allowed package permissions; must
	 *                                  not be null
	 * @param allowedClasses            the set of allowed class names; must not be
	 *                                  null
	 * @return a new JavaAOPTestCase; never null
	 */
	@Nonnull
	private JavaAOPTestCase createAOPTestCase(@Nonnull ResourceAccesses resourceAccesses,
			@Nonnull List<ArchitectureTestCase> javaArchitectureTestCases, @Nonnull JavaAOPTestCaseSupported supported,
			@Nonnull JavaClasses classes, @Nonnull Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull Set<ClassPermission> allowedClasses) {
		// Switch on the enum constant rather than indexing by ordinal(): the latter
		// silently pairs the wrong supplier with the aspect if the enum is ever
		// reordered.
		@Nonnull
		Supplier<List<?>> resourceAccessSupplier = switch (supported) {
		case FILESYSTEM_INTERACTION -> resourceAccesses::regardingFileSystemInteractions;
		case NETWORK_CONNECTION -> resourceAccesses::regardingNetworkConnections;
		case COMMAND_EXECUTION -> resourceAccesses::regardingCommandExecutions;
		case THREAD_CREATION -> resourceAccesses::regardingThreadCreations;
		};
		if (resourceAccessSupplier.get().isEmpty()) {
			javaArchitectureTestCases.add(JavaArchitectureTestCase.builder()
					// The scope the generated file asks for at runtime, and whether it has to be
					// checked against the whole compiled output first
					.supervisedPackage(supervisedPackage).supervisedScopeWasDerived(supervisedScopeWasDerived)
					// The architecture test case checks for the following aspect. Map the AOP
					// category to its architecture counterpart with an exhaustive switch rather
					// than valueOf(name()): a future divergence of the two enums then becomes a
					// compile error instead of a runtime IllegalArgumentException.
					.javaArchitectureTestCaseSupported(architectureCounterpartOf(supported))
					// The architecture test cases are built over the following classes
					.javaClasses(classes)
					// The architecture test cases are built over the following call graph
					.callGraphSupplier(callGraphSupplier)
					// The following packages are allowed
					.allowedPackages(allowedPackages)
					// The following classes are exempt (essential + test infrastructure)
					.allowedClasses(allowedClasses)
					// Build the architecture test case
					.build());
		}
		return JavaAOPTestCase.builder()
				// The AOP test case checks for the following aspect
				.javaAOPTestCaseSupported(supported)
				// The AOP test cases are built over the following resources
				.resourceAccessSupplier(resourceAccessSupplier)
				// The following classes are allowed
				.allowedClasses(allowedClasses)
				// Build the AOP test case
				.build();
	}

	private PhobosTestCase createPhobosTestCase(ResourceAccesses resourceAccesses,
			JavaPhobosTestCaseSupported supported) {
		// Switch on the enum constant rather than indexing by ordinal() so a reordering
		// of the enum cannot silently pair the wrong supplier with the aspect.
		@Nonnull
		Supplier<List<?>> resourceAccessSupplier = switch (supported) {
		case FILESYSTEM_INTERACTION -> resourceAccesses::regardingFileSystemInteractions;
		case NETWORK_CONNECTION -> resourceAccesses::regardingNetworkConnections;
		case TIMEOUT -> resourceAccesses::regardingTimeouts;
		};

		return JavaPhobosTestCase.builder().javaPhobosTestCaseSupported(supported)
				.resourceAccessSupplier(resourceAccessSupplier).build();
	}

	/**
	 * Adds priority architecture test cases that must be checked before variable
	 * (dynamic) domains. These represent domain-specific checks (e.g., native code,
	 * agent attach) whose APIs overlap with broader domains like FILESYSTEM or
	 * REFLECTION. By running them first, the more specific domain fires before the
	 * broader one.
	 *
	 * @since 2.0.1
	 * @param javaArchitectureTestCases the list to populate with architecture test
	 *                                  cases; must not be null
	 * @param classes                   the Java classes to analyse; must not be
	 *                                  null
	 * @param callGraph                 the call graph to analyse; must not be null
	 * @param allowedPackages           the set of allowed package permissions; must
	 *                                  not be null
	 * @param allowedClasses            the set of allowed class names; must not be
	 *                                  null
	 */
	private void addPriorityTestCases(@Nonnull List<ArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull JavaClasses classes, @Nonnull Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull Set<ClassPermission> allowedClasses) {
		javaArchitectureTestCases.addAll(JavaArchitectureTestCaseSupported.NATIVE_CODE.getPriority().stream()
				.map(priorityCase -> createArchitectureTestCase((JavaArchitectureTestCaseSupported) priorityCase,
						classes, callGraphSupplier, allowedPackages, allowedClasses))
				.toList());
	}

	/**
	 * Adds fixed architecture test cases that are always required.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param javaArchitectureTestCases the list to populate with architecture test
	 *                                  cases; must not be null
	 * @param classes                   the Java classes to analyse; must not be
	 *                                  null
	 * @param callGraph                 the call graph to analyse; must not be null
	 * @param allowedPackages           the set of allowed package permissions; must
	 *                                  not be null
	 * @param allowedClasses            the set of allowed class names; must not be
	 *                                  null
	 */
	private void addFixedTestCases(@Nonnull List<ArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull JavaClasses classes, @Nonnull Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull Set<ClassPermission> allowedClasses) {
		javaArchitectureTestCases.addAll(JavaArchitectureTestCaseSupported
				// The choice of using TERMINATE_JVM was taken randomly for getting an instance
				// of JavaArchitectureTestCaseSupported (otherwise we cannot operate over an
				// interface)
				.TERMINATE_JVM.getStatic().stream()
						.map(fixedCase -> createArchitectureTestCase((JavaArchitectureTestCaseSupported) fixedCase,
								classes, callGraphSupplier, allowedPackages, allowedClasses))
						.toList());
	}

	/**
	 * Adds variable architecture test cases that are generated based on the
	 * security policy.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param resourceAccesses          the resource accesses permitted by the
	 *                                  security policy; must not be null
	 * @param javaArchitectureTestCases the list to populate with architecture test
	 *                                  cases; must not be null
	 * @param javaAOPTestCases          the list to populate with AOP test cases;
	 *                                  must not be null
	 * @param classes                   the Java classes to analyse; must not be
	 *                                  null
	 * @param callGraph                 the call graph to analyse; must not be null
	 * @param allowedPackages           the set of allowed package permissions; must
	 *                                  not be null
	 * @param allowedClasses            the set of allowed class names; must not be
	 *                                  null
	 */
	private void addVariableTestCases(@Nonnull List<ArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull List<AOPTestCase> javaAOPTestCases, @Nonnull List<PhobosTestCase> javaPhobosTestCases,
			@Nonnull JavaClasses classes, @Nonnull Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull Set<ClassPermission> allowedClasses,
			@Nonnull ResourceAccesses resourceAccesses) {
		javaAOPTestCases.addAll(JavaAOPTestCaseSupported
				// The choice of using FILESYSTEM_INTERACTION was taken randomly for getting an
				// instance of JavaAOPTestCaseSupported (otherwise we cannot operate over an
				// interface)
				.FILESYSTEM_INTERACTION
						.getDynamic().stream()
						.map(variableCase -> createAOPTestCase(resourceAccesses, javaArchitectureTestCases,
								(JavaAOPTestCaseSupported) variableCase, classes, callGraphSupplier, allowedPackages,
								allowedClasses))
						.toList());

		javaPhobosTestCases.addAll(JavaPhobosTestCaseSupported
				// The choice of using FILESYSTEM_INTERACTION was taken randomly for getting an
				// instance of JavaPhobosTestCaseSupported (otherwise we cannot operate over an
				// interface)
				.FILESYSTEM_INTERACTION.getPhobosTestCases().stream()
						.map(variableCase -> createPhobosTestCase(resourceAccesses,
								(JavaPhobosTestCaseSupported) variableCase))
						.toList());
	}

	// </editor-fold>

	// <editor-fold desc="Create security test cases methods">

	/**
	 * Creates the security test cases based on the security policy.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param buildMode              the Java build mode to use; must not be null
	 * @param architectureMode       the Java architecture mode to use; must not be
	 *                               null
	 * @param aopMode                the Java AOP mode to use; must not be null
	 * @param essentialPackages      the list of essential packages; must not be
	 *                               null
	 * @param essentialClasses       the list of essential classes; must not be null
	 * @param testClasses            the list of test classes; must not be null
	 * @param packageName            the name of the package containing the main
	 *                               class; must not be null
	 * @param mainClassInPackageName the name of the main class; must not be null
	 * @param architectureTestCases  the list to populate with architecture test
	 *                               cases; must not be null
	 * @param aopTestCases           the list to populate with AOP test cases; must
	 *                               not be null
	 * @param resourceAccesses       the resource accesses permitted by the security
	 *                               policy; must not be null
	 * @param projectPath            the path to the project; must not be null
	 */
	public void createTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
			@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<ArchitectureTestCase> architectureTestCases, @Nonnull List<AOPTestCase> aopTestCases,
			@Nonnull List<PhobosTestCase> phobosTestCases, @Nonnull ResourceAccesses resourceAccesses,
			@Nonnull Path projectPath, boolean supervisedScopeWasDerived) {
		// <editor-fold desc="Extraction">
		@Nonnull
		String classPath = cacheResult(projectPath + "_" + packageName + "_classPath",
				() -> buildConfiguration == null ? buildMode.getClasspath(projectPath, packageName)
						: buildConfiguration.classpath(projectPath, packageName)).get();
		// Reserved-package guard (#2, point iii): import the supervised classpath
		// WITHOUT the framework exclusion that getJavaClasses applies, and reject any
		// compiled class whose declared package is a trusted infrastructure prefix.
		// Reading the true package from bytecode catches precompiled or generated
		// classes that never appear in the scanned source and would otherwise be
		// trusted by name.
		JavaClasses supervisedClasses = new ClassFileImporter().importPath(Path.of(classPath));
		ReservedPackageGuard.validateClassNames(supervisedClasses.stream().map(JavaClass::getName).toList());
		// The packages the supervised output actually declares, taken from the very
		// import that just refused a reserved one, so nothing is read twice. Empty
		// while nothing is compiled, which is ordinary during generation.
		@Nonnull
		Set<String> supervisedPackages = supervisedClasses.stream().map(JavaClass::getPackageName)
				.filter(name -> !name.isBlank()).collect(Collectors.toSet());
		@Nonnull
		JavaClasses javaClasses = cacheResult(projectPath + "_" + packageName + "_javaClasses",
				() -> architectureMode.getJavaClasses(classPath)).get();
		// Keep the call graph as a Supplier (no eager .get()) so downstream test cases
		// can defer construction. The disk-backed WALA outcome cache in
		// JavaWalaTestCase
		// short-circuits rule checks before this supplier is ever invoked when the same
		// (classpath, rule, per-package fingerprint) was already evaluated by an
		// earlier
		// JVM fork.
		@Nonnull
		Supplier<CallGraph> callGraphSupplier = cacheResult(projectPath + "_" + packageName + "_callGraph",
				() -> architectureMode.getCallGraph(classPath));
		// </editor-fold>

		// <editor-fold desc="Preparation">
		// Recorded before the test cases are built, because each of them carries the
		// scope into the file it generates: a generated test asks for it by name at
		// runtime and cannot work it out for itself.
		this.supervisedPackage = packageName;
		this.supervisedScopeWasDerived = supervisedScopeWasDerived;
		@Nonnull
		Set<PackagePermission> allowedPackages = prepareAllowedPackages(essentialPackages, resourceAccesses,
				packageName, supervisedPackages, testClasses);
		@Nonnull
		Set<ClassPermission> allowedClasses = prepareAllowedClasses(essentialClasses, testClasses);
		// </editor-fold>

		// <editor-fold desc="Create priority rules code (checked before variable
		// domains)">
		addPriorityTestCases(architectureTestCases, javaClasses, callGraphSupplier, allowedPackages, allowedClasses);
		// </editor-fold>

		// <editor-fold desc="Create variable rules code">
		addVariableTestCases(architectureTestCases, aopTestCases, phobosTestCases, javaClasses, callGraphSupplier,
				allowedPackages, allowedClasses, resourceAccesses);
		// </editor-fold>

		// <editor-fold desc="Create fixed rules code">
		addFixedTestCases(architectureTestCases, javaClasses, callGraphSupplier, allowedPackages, allowedClasses);
		// </editor-fold>
	}
	// </editor-fold>
}
