package de.tum.cit.ase.ares.api.architecture.java;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.TypeAnnotationDependencies;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * Architecture test case for the Java programming language.
 * <p>
 * Description: This class provides methods to write and execute architecture
 * test cases that verify code compliance with security policies. It supports
 * different static analysis approaches including Archunit and WALA, with
 * appropriate delegation to specialized implementations based on the selected
 * architecture mode.
 * </p>
 * <p>
 * Design Rationale: Implements a shared interface for architecture testing
 * strategies in different programming languages, facilitating consistent
 * security enforcement across varying analysis techniques while decoupling the
 * client code from specific implementations.
 * </p>
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class JavaArchitectureTestCase extends ArchitectureTestCase {

	// <editor-fold desc="Attributes">

	/** The message catalogue that names the architecture rules. */
	private static final String MESSAGES_BUNDLE = "de.tum.cit.ase.ares.api.localization.messages"; //$NON-NLS-1$

	/**
	 * The catalogues a rule name may come from. A rule keeps the name it got when
	 * its class was loaded, which can be a different language from the one active
	 * when its violation is reported, so every catalogue is searched.
	 */
	private static final List<Locale> RULE_NAME_LOCALES = List.of(Locale.ROOT, Locale.GERMAN);

	/**
	 * The architecture rules, by the part of their message key after
	 * {@code security.architecture.}; the same part names their action under
	 * {@code security.archunit.action.}.
	 */
	private static final List<String> RULE_KEYS = List.of("file.system.access", "network.access", "terminate.jvm", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"reflection.uses", "execute.command", "manipulate.threads", "package.import", "serialize", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"class.loading", "native.code.access", "agent.attach", "environment.access", "module.system.access", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"jndi.injection"); //$NON-NLS-1$

	/**
	 * Optional lazy supplier of the call graph. When non-null, the WALA conversion
	 * uses this supplier (deferred construction) instead of the eagerly resolved
	 * {@link #callGraph} inherited from the parent class. This lets the WALA
	 * outcome cache short-circuit rule checks before the (expensive) call-graph
	 * construction ever runs.
	 */
	@Nullable
	private final Supplier<CallGraph> callGraphSupplier;

	/** Returns the lazy call-graph supplier set on this test case, if any. */
	@Nullable
	public Supplier<CallGraph> getCallGraphSupplier() {
		return callGraphSupplier;
	}

	/**
	 * Classes exempt from the architecture rules (the essential + test
	 * infrastructure classes). A class on this list may use otherwise-forbidden
	 * APIs. Never null; empty means no exemptions.
	 */
	@Nonnull
	private final Set<ClassPermission> allowedClasses;

	/**
	 * The binary names of the test classes. The supervised code may not depend on
	 * them or on classes named like ones nested in them. Never null.
	 */
	@Nonnull
	private final Set<String> testClassNames;

	/**
	 * The supervised scope the generated test must analyse, and whether Ares
	 * derived it rather than reading it from a policy. A generated test needs both:
	 * the scope to ask for at runtime, and whether it must first be checked against
	 * the whole compiled output, which is only so when Ares worked the scope out
	 * for itself.
	 * <p>
	 * Taken through every constructor rather than assigned afterwards. A test case
	 * without a scope refuses to write its file, so it is not a valid object, yet
	 * two-step construction let it exist: three call sites had to remember the
	 * second step and two did not.
	 */
	@Nullable
	private final String supervisedPackage;

	/** Whether {@link #supervisedPackage} was derived rather than pinned. */
	private final boolean supervisedScopeWasDerived;

	/**
	 * The WALA test case the last run in WALA mode handed this case to, kept so a
	 * later phase of the same test can compare the inputs of its verdict;
	 * {@code null} until such a run.
	 */
	@Nullable
	private volatile JavaWalaTestCase lastWalaRun;

	/**
	 * @return the supervised scope, or null when none was supplied
	 */
	@Nullable
	public String getSupervisedPackage() {
		return supervisedPackage;
	}

	/**
	 * @return whether the supervised scope was derived rather than pinned
	 */
	public boolean isSupervisedScopeWasDerived() {
		return supervisedScopeWasDerived;
	}

	/** Returns the set of classes exempt from the architecture rules. */
	@Nonnull
	public Set<ClassPermission> getAllowedClasses() {
		return allowedClasses;
	}

	/**
	 * @return the binary names of the test classes the supervised code may not
	 *         depend on
	 */
	@Nonnull
	public Set<String> getTestClassNames() {
		return testClassNames;
	}

	/** Checks exact and nested class names against the policy exemptions. */
	public static boolean isAllowedClass(@Nullable String fullyQualifiedClassName,
			@Nonnull Set<ClassPermission> allowedClasses) {
		return ClassPermission.isAllowedClass(fullyQualifiedClassName, allowedClasses);
	}
	// </editor-fold>

	// <editor-fold desc="Constructor">

	/**
	 * Constructs a new Java architecture test case with the specified parameters.
	 *
	 * @param javaArchitectureTestCaseSupported The type of architecture test case
	 *                                          supported, determining which rules
	 *                                          to apply
	 * @param allowedPackages                   Set of package permissions that are
	 *                                          allowed in the analysed code (may be
	 *                                          null for no restrictions)
	 * @param javaClasses                       Collection of Java classes to be
	 *                                          analysed by the test case
	 * @param callGraph                         Call graph representing
	 *                                          caller-callee relationships in the
	 *                                          code (may be null for ARCHUNIT mode)
	 * @param supervisedPackage                 The supervised scope the generated
	 *                                          test asks for at runtime, or
	 *                                          {@code null} on a path that never
	 *                                          writes a file
	 * @param supervisedScopeWasDerived         Whether that scope was derived from
	 *                                          the project rather than pinned by a
	 *                                          policy
	 * @author Sarp Sahinalp
	 * @since 2.0.0
	 */
	public JavaArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nullable CallGraph callGraph, @Nullable String supervisedPackage, boolean supervisedScopeWasDerived) {
		this(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph, null, java.util.Set.of(),
				supervisedPackage, supervisedScopeWasDerived);
	}

	/**
	 * Constructs a new Java architecture test case that defers call-graph
	 * construction. Used by WALA mode so that disk-cached rule outcomes can
	 * short-circuit before the call graph is built.
	 *
	 * @param javaArchitectureTestCaseSupported The type of architecture test case
	 *                                          supported, determining which rules
	 *                                          to apply
	 * @param allowedPackages                   Set of package permissions that are
	 *                                          allowed in the analysed code
	 * @param javaClasses                       Collection of Java classes to be
	 *                                          analysed by the test case
	 * @param callGraph                         Eagerly resolved call graph, or
	 *                                          {@code null} when only the lazy
	 *                                          supplier is provided
	 * @param callGraphSupplier                 Lazy supplier of the call graph, or
	 *                                          {@code null} when an eager
	 *                                          {@code callGraph} is provided
	 * @param allowedClasses                    Classes exempt from the architecture
	 *                                          rules
	 * @param supervisedPackage                 The supervised scope the generated
	 *                                          test asks for at runtime, or
	 *                                          {@code null} on a path that never
	 *                                          writes a file
	 * @param supervisedScopeWasDerived         Whether that scope was derived from
	 *                                          the project rather than pinned by a
	 *                                          policy
	 * @since 2.0.0
	 */
	public JavaArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nullable CallGraph callGraph, @Nullable Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<ClassPermission> allowedClasses, @Nullable String supervisedPackage,
			boolean supervisedScopeWasDerived) {
		this(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph, callGraphSupplier,
				allowedClasses, Set.of(), supervisedPackage, supervisedScopeWasDerived);
	}

	/**
	 * Constructs a test case that also knows the test classes.
	 *
	 * @param javaArchitectureTestCaseSupported the category
	 * @param allowedPackages                   the permitted packages
	 * @param javaClasses                       the classes to check
	 * @param callGraph                         the call graph
	 * @param callGraphSupplier                 the lazy call graph
	 * @param allowedClasses                    the exempt classes
	 * @param testClassNames                    the test class names
	 * @param supervisedPackage                 the scope, or {@code null}
	 * @param supervisedScopeWasDerived         whether it was derived
	 */
	public JavaArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nullable CallGraph callGraph, @Nullable Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<ClassPermission> allowedClasses, @Nonnull Set<String> testClassNames,
			@Nullable String supervisedPackage, boolean supervisedScopeWasDerived) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph);
		this.callGraphSupplier = callGraphSupplier;
		this.allowedClasses = allowedClasses;
		this.testClassNames = Set.copyOf(Objects.requireNonNull(testClassNames, "testClassNames must not be null"));
		this.supervisedPackage = supervisedPackage;
		this.supervisedScopeWasDerived = supervisedScopeWasDerived;
	}

	/**
	 * The signature released in 2.1.2, kept so that a client compiled against it
	 * still links.
	 * <p>
	 * A test case built this way carries no supervised scope. It can be executed,
	 * but it refuses to write a generated file, because a file with no scope
	 * analyses nothing and every rule in it passes. Supply the scope through the
	 * builder or through the scope-aware constructor beside this one.
	 *
	 * @param javaArchitectureTestCaseSupported the supported architecture test case
	 * @param allowedPackages                   the permitted package imports
	 * @param javaClasses                       the classes to analyse
	 * @param callGraph                         the call graph, or {@code null}
	 * @deprecated supply the supervised scope, which this overload cannot express
	 */
	@Deprecated(forRemoval = true)
	public JavaArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nullable CallGraph callGraph) {
		this(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph, (String) null, false);
	}

	/**
	 * The signature released in 2.1.2, kept so that a client compiled against it
	 * still links.
	 * <p>
	 * A test case built this way carries no supervised scope. It can be executed,
	 * but it refuses to write a generated file, because a file with no scope
	 * analyses nothing and every rule in it passes. Supply the scope through the
	 * builder or through the scope-aware constructor beside this one.
	 *
	 * @param javaArchitectureTestCaseSupported the supported architecture test case
	 * @param allowedPackages                   the permitted package imports
	 * @param javaClasses                       the classes to analyse
	 * @param callGraph                         the call graph, or {@code null}
	 * @param callGraphSupplier                 the lazy call graph, or {@code null}
	 * @param allowedClasses                    the classes exempt from the rules
	 * @deprecated supply the supervised scope, which this overload cannot express
	 */
	@Deprecated(forRemoval = true)
	public JavaArchitectureTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nullable CallGraph callGraph, @Nullable Supplier<CallGraph> callGraphSupplier,
			@Nonnull Set<ClassPermission> allowedClasses) {
		this(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph, callGraphSupplier,
				allowedClasses, null, false);
	}
	// </editor-fold>

	// <editor-fold desc="Tool methods">

	/**
	 * Returns one string standing for a set of names, for use in a cache key. Each
	 * name is prefixed with its length, so no name, whatever it contains, can make
	 * two different sets produce the same string.
	 *
	 * @param names the names, in any order
	 * @return the signature of the names
	 */
	@Nonnull
	protected static String unambiguousSignature(@Nonnull Stream<String> names) {
		return names.sorted().map(name -> name.length() + ":" + name).collect(Collectors.joining(","));
	}
	// TODO Markus: Move to better fitting class

	/**
	 * Throws the dedicated error when the type-annotation check could not read a
	 * class file. The check marks that case with a text that is never translated,
	 * so it is found before the rule is told apart by its translated description.
	 *
	 * @param message the message of the rule violation
	 * @throws SecurityException naming the unreadable class, if the marker is there
	 */
	private static void throwIfTypeAnnotationsWereUnreadable(@Nonnull String message) {
		int marker = message.indexOf(TypeAnnotationDependencies.UNREADABLE_MARKER);
		if (marker < 0) {
			return;
		}
		int start = message.indexOf('<', marker);
		int end = start < 0 ? -1 : message.indexOf('>', start);
		String className = start < 0 || end < 0 ? "unknown class" : message.substring(start + 1, end);
		throw new SecurityException(Messages.localized("security.archunit.type.annotation.unreadable", className));
	}

	/**
	 * Turns the report of a broken architecture rule into the Ares refusal. A
	 * forbidden import names the importing class and its forbidden packages; any
	 * other rule names the caller and the target, with method parameters written as
	 * source types split by {@code ", "}, the same for ArchUnit and WALA.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param e The assertion error produced by an architectural rule violation
	 * @throws SecurityException with a formatted error message describing the
	 *                           security violation
	 */
	public static void parseErrorMessage(@Nonnull AssertionError e) {
		Objects.requireNonNull(e, "error must not be null");
		@Nullable
		String message = e.getMessage();
		Objects.requireNonNull(message, "message must not be null");
		throwIfTypeAnnotationsWereUnreadable(message);
		@Nullable
		String[] messageParts = e.getMessage().split("\n");
		Objects.requireNonNull(messageParts, "messageParts must not be null");
		if (messageParts.length < 2) {
			throw new SecurityException(Messages.localized("security.archunit.illegal.execution", e.getMessage()));
		}

		// Extract the rule name (e.g., "Accesses file system") from first line
		@Nonnull
		String ruleNamePattern = ".*?'(.*?)'.*";
		@Nonnull
		String ruleName = messageParts[0].replaceAll(ruleNamePattern, "$1").trim();

		if (isImportRule(ruleName)) {
			throw importViolation(messageParts, message);
		}

		// Parse the detail line to extract caller and target method info.
		@Nonnull
		String detailLine = messageParts[1].trim();

		// Extract the caller (Method/Constructor) and target (accesses/calls) with
		// fallbacks.
		String caller = null;
		String target = null;

		java.util.regex.Matcher callerMatcher = java.util.regex.Pattern
				.compile("(?:Method|Constructor) <(.+?)>(?:\\s|$)").matcher(detailLine);
		if (callerMatcher.find()) {
			caller = callerMatcher.group(1);
		}

		java.util.regex.Matcher targetMatcher = java.util.regex.Pattern
				.compile("(?:accesses|calls method|calls constructor|calls|gets field|sets field) <(.+?)>(?:\\s|$)")
				.matcher(detailLine);
		if (targetMatcher.find()) {
			target = targetMatcher.group(1);
		}

		if (caller == null || target == null) {
			java.util.List<String> angleMatches = new java.util.ArrayList<>();
			java.util.regex.Matcher angleMatcher = java.util.regex.Pattern.compile("<(.+?)>(?=\\s|$)")
					.matcher(detailLine);
			while (angleMatcher.find()) {
				angleMatches.add(angleMatcher.group(1));
			}
			if (caller == null && !angleMatches.isEmpty()) {
				caller = angleMatches.get(0);
			}
			if (target == null) {
				if (angleMatches.size() >= 2) {
					target = angleMatches.get(angleMatches.size() - 1);
				} else if (angleMatches.size() == 1) {
					target = angleMatches.get(0);
				}
			}
		}

		@Nonnull
		String callerResolved = caller != null ? sourceForm(caller) : "unknown";
		@Nonnull
		String targetResolved = target != null ? sourceForm(target) : "unknown";

		// Extract parent caller from the caller (class.method -> extract class).
		//
		// The separator is searched before the parameter list, not across the whole
		// string. A caller carrying a fully qualified parameter type ends in a dot of
		// its own, so searching the whole string cuts inside the parameter list:
		// "a.b.C.m(java.lang.String)" yielded "a.b.C.m(java.lang". This surfaced
		// through the WALA path, whose callers carry such parameter types.
		@Nonnull
		String parentCaller = extractDeclaringType(callerResolved);

		// Determine the action based on the rule name
		@Nonnull
		String action = mapRuleNameToAction(ruleName);

		throw new SecurityException(Messages.localized("security.archunit.violation.error", callerResolved, action,
				targetResolved, parentCaller));
	}

	/**
	 * Writes the array parameters of a method name as source types, so ArchUnit's
	 * {@code [Ljava.lang.String;} reads {@code java.lang.String[]} as WALA's does.
	 * A name without an array is returned unchanged, since a lone parameter such as
	 * {@code Data} would otherwise be misread as a JVM descriptor.
	 *
	 * @param reference the rendered method, constructor or field name
	 * @return the name in source notation
	 */
	@Nonnull
	private static String sourceForm(@Nonnull String reference) {
		return reference.indexOf('[') < 0 ? reference
				: Objects.requireNonNull(ForbiddenMethodMatcher.canonicalise(reference));
	}

	/**
	 * Builds the refusal for a forbidden import from the rule's report. It names
	 * the class of the first reported line and every forbidden package that class
	 * imports. A line naming no class gives the text without a class. A report
	 * naming no package is passed on whole.
	 *
	 * @param messageParts the report, split into lines
	 * @param message      the whole report
	 * @return the refusal to throw
	 */
	@Nonnull
	private static SecurityException importViolation(@Nonnull String[] messageParts, @Nonnull String message) {
		String importingClass = null;
		Set<String> forbiddenPackages = new java.util.TreeSet<>();
		for (int i = 1; i < messageParts.length; i++) {
			List<String> names = angleBracketedNames(messageParts[i]);
			if (names.size() < 2) {
				continue;
			}
			String lineClass = importingClassOf(messageParts[i], names.get(0));
			if (importingClass == null) {
				importingClass = lineClass;
			}
			String packageName = packageOf(names.get(names.size() - 1));
			if (importingClass.equals(lineClass) && !packageName.isEmpty()) {
				forbiddenPackages.add(packageName);
			}
		}
		if (forbiddenPackages.isEmpty()) {
			return new SecurityException(Messages.localized("security.archunit.illegal.execution", message));
		}
		return new SecurityException(importMessage(Objects.requireNonNullElse(importingClass, ""), forbiddenPackages));
	}

	/**
	 * Words the import refusal, agreeing in number with the packages and leaving
	 * out the class when none is known.
	 *
	 * @param importingClass    the class that imported them, or an empty string
	 * @param forbiddenPackages the forbidden packages, at least one
	 * @return the localised message
	 */
	@Nonnull
	private static String importMessage(@Nonnull String importingClass, @Nonnull Set<String> forbiddenPackages) {
		String packages = String.join(", ", forbiddenPackages);
		boolean plural = forbiddenPackages.size() > 1;
		if (importingClass.isEmpty()) {
			return plural
					? Messages.localized("security.archunit.package.import.violation.without.class.plural", packages)
					: Messages.localized("security.archunit.package.import.violation.without.class", packages);
		}
		return plural
				? Messages.localized("security.archunit.package.import.violation.plural", importingClass, packages)
				: Messages.localized("security.archunit.package.import.violation", importingClass, packages);
	}

	/**
	 * Reads the class a report line starts from. A line opening with {@code Class}
	 * names it directly; a method, constructor or field line names a member, whose
	 * declaring type is taken.
	 *
	 * @param line   the report line
	 * @param origin the first name in angle brackets on that line
	 * @return the class, or an empty string when the origin names none
	 */
	@Nonnull
	private static String importingClassOf(@Nonnull String line, @Nonnull String origin) {
		if (line.strip().startsWith("Class <")) { //$NON-NLS-1$
			return origin;
		}
		String declaringType = extractDeclaringType(origin);
		return declaringType.equals(origin) ? "" : declaringType;
	}

	/**
	 * Lists the names a report line puts in angle brackets, in order. Brackets are
	 * counted, so a constructor such as {@code a.B.<init>()} stays one name.
	 *
	 * @param line the report line
	 * @return the names, possibly none
	 */
	@Nonnull
	private static List<String> angleBracketedNames(@Nonnull String line) {
		List<String> names = new java.util.ArrayList<>();
		int depth = 0;
		int start = -1;
		for (int i = 0; i < line.length(); i++) {
			char character = line.charAt(i);
			if (character == '<' && depth++ == 0) {
				start = i + 1;
			} else if (character == '>' && depth > 0 && --depth == 0) {
				names.add(line.substring(start, i));
			}
		}
		return names;
	}

	/**
	 * Takes the package from a class, method or field name: every segment before
	 * the first one starting with an upper-case letter, as the report gives no
	 * other sign of where the package ends. An array type such as
	 * {@code [Ljava.io.File;} is read as its element type.
	 *
	 * @param name the rendered name
	 * @return the package, empty when the name starts with a class
	 */
	@Nonnull
	private static String packageOf(@Nonnull String name) {
		String elementName = name.startsWith("[") ? name.replaceFirst("^\\[+L?", "") : name; //$NON-NLS-1$ //$NON-NLS-2$
		List<String> segments = new java.util.ArrayList<>();
		for (String segment : elementName.split("\\.")) { //$NON-NLS-1$
			if (!segment.isEmpty() && Character.isUpperCase(segment.charAt(0))) {
				break;
			}
			segments.add(segment);
		}
		return String.join(".", segments); //$NON-NLS-1$
	}

	/**
	 * Extracts the declaring type from a rendered method or constructor reference.
	 * <p>
	 * The reference may carry a parameter list, and a fully qualified parameter
	 * type contains dots of its own. The method separator is therefore searched
	 * only in the part preceding the parameter list, so that
	 * {@code a.b.C.m(java.lang.String)} yields {@code a.b.C} rather than
	 * {@code a.b.C.m(java.lang}.
	 *
	 * @since 2.1.3
	 * @author Markus Paulsen
	 * @param reference The rendered method or constructor reference
	 * @return The declaring type, or the reference itself when it carries no
	 *         separator
	 */
	@Nonnull
	private static String extractDeclaringType(@Nonnull String reference) {
		int parameterListStart = reference.indexOf('(');
		int searchEnd = parameterListStart >= 0 ? parameterListStart - 1 : reference.length() - 1;
		if (searchEnd < 0) {
			return reference;
		}
		int methodSeparator = reference.lastIndexOf('.', searchEnd);
		return methodSeparator >= 0 ? reference.substring(0, methodSeparator) : reference;
	}

	/**
	 * Maps a rule name, in any supported language, to the action it forbids, worded
	 * in the active language. An unknown name is returned unchanged.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param ruleName The name of the violated rule
	 * @return A human-readable action description
	 */
	@Nonnull
	private static String mapRuleNameToAction(@Nonnull String ruleName) {
		@Nullable
		String ruleKey = findRuleKey(ruleName);
		return ruleKey == null ? ruleName : Messages.localized("security.archunit.action." + ruleKey); //$NON-NLS-1$
	}

	/**
	 * Finds which architecture rule a name belongs to, comparing it with that
	 * rule's name in every supported language.
	 *
	 * @param ruleName The name of the violated rule
	 * @return The rule's key part after {@code security.architecture.}, or
	 *         {@code null} when the name belongs to no known rule
	 */
	@Nullable
	private static String findRuleKey(@Nonnull String ruleName) {
		String trimmed = ruleName.trim();
		return RULE_KEYS.stream().filter(ruleKey -> ruleNamesOf(ruleKey).stream().anyMatch(trimmed::equalsIgnoreCase))
				.findFirst().orElse(null);
	}

	/**
	 * Tells whether a rule name is the forbidden-import rule, in any supported
	 * language. It matches when the name contains that rule's name, as the
	 * English-only check before it did.
	 *
	 * @param ruleName The name of the violated rule
	 * @return {@code true} when the name is the forbidden-import rule
	 */
	private static boolean isImportRule(@Nonnull String ruleName) {
		String lowerCaseName = ruleName.toLowerCase(Locale.ROOT);
		return ruleNamesOf("package.import").stream() //$NON-NLS-1$
				.anyMatch(name -> lowerCaseName.contains(name.toLowerCase(Locale.ROOT)));
	}

	/**
	 * Reads one rule's name from every supported catalogue.
	 *
	 * @param ruleKey The rule's key part after {@code security.architecture.}
	 * @return The rule's name in each supported language
	 */
	@Nonnull
	private static List<String> ruleNamesOf(@Nonnull String ruleKey) {
		return RULE_NAME_LOCALES.stream().map(locale -> ResourceBundle.getBundle(MESSAGES_BUNDLE, locale)
				.getString("security.architecture." + ruleKey)).toList(); //$NON-NLS-1$
	}
	// </editor-fold>

	// <editor-fold desc="Write architecture test case methods">

	/** The namespace Ares's own classes live in before Precompile copies them. */
	private static final String ARES_NAMESPACE = "de.tum.cit.ase";

	/** The part of that namespace that Precompile copies below the scope. */
	private static final String COPIED_API = ARES_NAMESPACE + ".ares.api";

	/**
	 * Formats the permitted packages as a Java literal
	 * {@code Set.of(new PackagePermission(...), ...)} for a generated test.
	 * <p>
	 * Precompile copies Ares's api into {@code <scope>.ares.api}, and woven code
	 * then refers to the copy, so each permission inside
	 * {@code de.tum.cit.ase.ares.api} is written a second time for the copy. The
	 * supervised code's own packages are not written: the generated rule reads them
	 * when it runs.
	 *
	 * @return the literal
	 */
	@Nonnull
	protected String allowedPackagesAsCopiedCode() {
		return "Set.of("
				+ allowedPackages.stream().flatMap(this::withCopiedCounterpart).distinct()
						.map(pp -> String.format("new %s(\"%s\")", PackagePermission.class.getSimpleName(),
								pp.importTheFollowingPackage()))
						.collect(java.util.stream.Collectors.joining(", "))
				+ ")";
	}

	/**
	 * Returns a permission and, if it lies inside Ares's copied api and a scope is
	 * known, the same permission for the copy below the scope.
	 *
	 * @param permission the permission
	 * @return the permission, followed by its counterpart for the copy if any
	 */
	@Nonnull
	private java.util.stream.Stream<PackagePermission> withCopiedCounterpart(@Nonnull PackagePermission permission) {
		String packageName = permission.importTheFollowingPackage();
		if (supervisedPackage == null || supervisedPackage.isBlank()
				|| !(packageName.equals(COPIED_API) || packageName.startsWith(COPIED_API + "."))) {
			return java.util.stream.Stream.of(permission);
		}
		return java.util.stream.Stream.of(permission,
				new PackagePermission(supervisedPackage + packageName.substring(ARES_NAMESPACE.length())));
	}

	/**
	 * Formats the test class names as a Java literal {@code Set.of("a.B", ...)} for
	 * a generated test.
	 *
	 * @return the literal
	 */
	@Nonnull
	protected String testClassNamesAsCode() {
		return "Set.of(" + testClassNames.stream().sorted().map(name -> "\"" + name + "\"")
				.collect(java.util.stream.Collectors.joining(", ")) + ")";
	}

	/**
	 * Generates the architecture test case code as a string based on the specified
	 * architecture mode. Delegates to the appropriate implementation based on the
	 * architecture mode parameter.
	 * <p>
	 * It delegates through {@code archunitBuilder()} and {@code walaBuilder()}, not
	 * {@code builder()}: Java inherits statics by name, so {@code builder()} on
	 * either subclass resolved to the one declared here and built another
	 * base-class instance, which delegated again until the stack ran out. Nothing
	 * caught it, because the production path converts through
	 * {@code ArchitectureMode} and never reaches these branches. The delegate is
	 * also given the scope, since it is what writes the file and one built without
	 * a scope refuses to.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param architectureMode The mode of architecture analysis (e.g., "WALA" or
	 *                         "ARCHUNIT")
	 * @param aopMode          The aspect-oriented programming mode to use
	 * @return A string containing the generated architecture test case code
	 * @throws SecurityException if the specified architecture mode is not supported
	 */
	@Nonnull
	@Override
	public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		return switch (architectureMode) {
		case "ARCHUNIT" -> JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) architectureTestCaseSupported)
				.allowedPackages(allowedPackages).javaClasses(javaClasses).allowedClasses(allowedClasses)
				.testClassNames(testClassNames).supervisedPackage(supervisedPackage)
				.supervisedScopeWasDerived(supervisedScopeWasDerived).build()
				.writeArchitectureTestCase(architectureMode, aopMode);
		case "WALA" -> JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) architectureTestCaseSupported)
				.allowedPackages(allowedPackages).javaClasses(javaClasses).callGraph(callGraph)
				.allowedClasses(allowedClasses).testClassNames(testClassNames).supervisedPackage(supervisedPackage)
				.supervisedScopeWasDerived(supervisedScopeWasDerived).build()
				.writeArchitectureTestCase(architectureMode, aopMode);
		default -> throw new SecurityException(
				Messages.localized("security.architecture.testcase.mode.not.supported", architectureMode));
		};
	}
	// </editor-fold>

	// <editor-fold desc="Execute architecture test case methods">

	/**
	 * Checks the code against the policy with the implementation of the given
	 * architecture mode.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param architectureMode The mode of architecture analysis (e.g., "WALA" or
	 *                         "ARCHUNIT")
	 * @param aopMode          The aspect-oriented programming mode to use
	 * @throws SecurityException if validation fails or if required resources are
	 *                           missing for the selected mode
	 */
	@Override
	public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		ArchitectureTestCaseSupported protectedArchitectureTestCaseSupported = Objects
				.requireNonNull(architectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null");
		if (!(architectureTestCaseSupported instanceof JavaArchitectureTestCaseSupported)) {
			throw new SecurityException(
					Messages.localized("security.architecture.testcase.type.invalid", architectureTestCaseSupported));
		}
		JavaArchitectureTestCaseSupported protectedJavaArchitectureTestCaseSupported = (JavaArchitectureTestCaseSupported) protectedArchitectureTestCaseSupported;
		Set<PackagePermission> protectedAllowedPackages = Objects.requireNonNull(allowedPackages,
				"allowedPackages must not be null");
		JavaClasses protectedJavaClasses = Objects.requireNonNull(javaClasses, "javaClasses must not be null");

		switch (architectureMode) {
		case "ARCHUNIT" -> JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(protectedJavaArchitectureTestCaseSupported)
				.allowedPackages(protectedAllowedPackages).allowedClasses(allowedClasses).testClassNames(testClassNames)
				.javaClasses(protectedJavaClasses).build().executeArchitectureTestCase(architectureMode, aopMode);
		case "WALA" -> {
			// Use the lazy supplier path when present so the WALA outcome cache can
			// short-circuit before any call-graph construction. The eager path remains
			// for callers that already resolved the call graph (legacy code).
			JavaWalaTestCase tc;
			if (callGraphSupplier != null) {
				tc = new JavaWalaTestCase(protectedJavaArchitectureTestCaseSupported, protectedAllowedPackages,
						protectedJavaClasses, callGraphSupplier, allowedClasses, testClassNames, supervisedPackage,
						supervisedScopeWasDerived);
			} else {
				tc = JavaWalaTestCase.walaBuilder()
						.javaArchitectureTestCaseSupported(protectedJavaArchitectureTestCaseSupported)
						.allowedPackages(protectedAllowedPackages).allowedClasses(allowedClasses)
						.testClassNames(testClassNames).javaClasses(protectedJavaClasses).callGraph(callGraph).build();
			}
			lastWalaRun = tc;
			tc.executeArchitectureTestCase(architectureMode, aopMode);
		}
		default -> throw new SecurityException(
				Messages.localized("security.architecture.testcase.mode.not.supported", architectureMode));
		}
	}

	/**
	 * Tells whether the WALA verdict this case last relied on still has the inputs
	 * it was looked up with, apart from the policy and the analysed classes: the
	 * runtime, the content of the dependencies the call graph follows and the
	 * method lists. A case never run in WALA mode has no such verdict and answers
	 * {@code false}.
	 *
	 * @param dependencyFingerprint the analysed classes' dependency fingerprint,
	 *                              read afresh
	 * @return whether the verdict still has the same inputs
	 */
	public boolean verdictInputsUnchanged(@Nonnull String dependencyFingerprint) {
		JavaWalaTestCase lastRun = lastWalaRun;
		return lastRun != null && lastRun.verdictInputsUnchanged(dependencyFingerprint);
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
	public static Builder builder() {
		return new Builder();
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
		/**
		 * Optional lazy supplier for the call graph. When set, the conversion to a
		 * {@link de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase}
		 * propagates this supplier instead of resolving the call graph eagerly. This
		 * lets the WALA outcome cache short-circuit rule checks before the (expensive)
		 * call-graph construction ever runs.
		 */
		@Nullable
		private Supplier<CallGraph> callGraphSupplier;
		@Nullable
		private Set<PackagePermission> allowedPackages;
		@Nonnull
		private Set<ClassPermission> allowedClasses = java.util.Set.of();
		/** The binary names of the test classes; empty unless set. */
		@Nonnull
		private Set<String> testClassNames = java.util.Set.of();
		@Nullable
		private String supervisedPackage;
		private boolean supervisedScopeWasDerived;

		/**
		 * Records the test classes the supervised code may not depend on.
		 *
		 * @param testClassNames the binary names of the test classes
		 * @return this builder
		 */
		@Nonnull
		public Builder testClassNames(@Nonnull Set<String> testClassNames) {
			this.testClassNames = Objects.requireNonNull(testClassNames, "testClassNames must not be null");
			return this;
		}

		/**
		 * Records the scope a generated test asks for at runtime.
		 *
		 * @param supervisedPackage the supervised scope
		 * @return this builder
		 */
		@Nonnull
		public Builder supervisedPackage(@Nullable String supervisedPackage) {
			this.supervisedPackage = supervisedPackage;
			return this;
		}

		/**
		 * Records whether that scope was derived rather than pinned, which decides
		 * whether the generated test must check it against the whole compiled output.
		 *
		 * @param supervisedScopeWasDerived whether the scope was derived
		 * @return this builder
		 */
		@Nonnull
		public Builder supervisedScopeWasDerived(boolean supervisedScopeWasDerived) {
			this.supervisedScopeWasDerived = supervisedScopeWasDerived;
			return this;
		}

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
		public Builder javaArchitectureTestCaseSupported(
				@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
			this.javaArchitectureTestCaseSupported = Objects.requireNonNull(javaArchitectureTestCaseSupported,
					"javaArchitecturalTestCaseSupported must not be null");
			return this;
		}

		/**
		 * Sets the Java classes to be analysed by this architecture test case.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaClasses Collection of Java classes for analysis
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public Builder javaClasses(@Nonnull JavaClasses javaClasses) {
			this.javaClasses = Objects.requireNonNull(javaClasses, "javaClasses must not be null");
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
		public Builder callGraph(@Nullable CallGraph callGraph) {
			this.callGraph = callGraph;
			return this;
		}

		/**
		 * Sets a lazy supplier for the call graph. Required only for WALA mode and only
		 * when callers want construction deferred until the rule actually needs the
		 * graph (e.g. cache miss). For ARCHUNIT mode this is irrelevant.
		 */
		@Nonnull
		public Builder callGraphSupplier(@Nullable Supplier<CallGraph> callGraphSupplier) {
			this.callGraphSupplier = callGraphSupplier;
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
		public Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
			this.allowedPackages = Objects.requireNonNull(allowedPackages, "allowedPackages must not be null");
			return this;
		}

		/**
		 * Sets the allowed (exempt) class permissions.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param allowedClasses Set of class permissions exempt from the architecture
		 *                       rules
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public Builder allowedClasses(@Nonnull Set<ClassPermission> allowedClasses) {
			this.allowedClasses = Objects.requireNonNull(allowedClasses, "allowedClasses must not be null");
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
		public JavaArchitectureTestCase build() {
			return new JavaArchitectureTestCase(
					Objects.requireNonNull(javaArchitectureTestCaseSupported,
							"javaArchitecturalTestCaseSupported must not be null"),
					Objects.requireNonNull(allowedPackages, "allowedPackages must not be null"),
					Objects.requireNonNull(javaClasses, "javaClasses must not be null"), callGraph, callGraphSupplier,
					allowedClasses, testClassNames, supervisedPackage, supervisedScopeWasDerived);
		}
	}
	// </editor-fold>
}
