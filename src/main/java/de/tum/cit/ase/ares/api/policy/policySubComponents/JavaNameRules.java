package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Java implementation of {@link LanguageNameRules}.
 * <p>
 * Description: Holds the regular expressions that describe how packages, types,
 * classes and thread constructs are named in Java, and validates values against
 * them. This is the one place, together with
 * {@link ProgrammingLanguageConfiguration}, where the policy subsystem knows
 * anything Java-specific about names.
 * <p>
 * Design Rationale: These patterns used to live in {@code PolicyValueValidator}
 * and made the neutral core Java-bound. Moving them into a clearly Java-named
 * component keeps the core language-agnostic: a further language supplies its
 * own {@link LanguageNameRules} rather than editing shared constants. The type
 * is a stateless singleton because the patterns are immutable and shared.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
public final class JavaNameRules implements LanguageNameRules {

	/** The shared, stateless instance. */
	@Nonnull
	public static final JavaNameRules INSTANCE = new JavaNameRules();

	private static final String JAVA_RESERVED_WORD = "abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|false|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|true|try|void|volatile|while|_";
	/**
	 * One character inside an identifier, excluding the identifier-ignorable ones.
	 * <p>
	 * {@code \p{javaJavaIdentifierPart}} alone is too permissive here.
	 * {@link Character#isJavaIdentifierPart} accepts every character for which
	 * {@link Character#isIdentifierIgnorable} holds, which is the C0 and C1
	 * controls including {@code U+0000}, {@code U+007F} and the format characters
	 * {@code U+00AD}, {@code U+200B}, {@code U+200D} and {@code U+FEFF}. A package
	 * name is not merely compared: it is written into generated sources, AspectJ
	 * and ArchUnit configuration and failure messages, where a NUL or a line
	 * separator corrupts an artefact that assumes one value per line, and an
	 * invisible zero-width character produces two allowlist entries that a reader
	 * cannot tell apart. The same reasoning already excludes these characters from
	 * paths and commands in {@code PolicyValueValidator}; leaving names alone would
	 * have applied the rule to two thirds of the policy.
	 * <p>
	 * Only the part is constrained. {@link Character#isJavaIdentifierStart} accepts
	 * no ignorable character, so the first one needs no exclusion.
	 */
	private static final String JAVA_IDENTIFIER_PART = "[\\p{javaJavaIdentifierPart}&&[^\\p{javaIdentifierIgnorable}]]";

	private static final String JAVA_IDENTIFIER = "(?!(?:" + JAVA_RESERVED_WORD + ")(?=\\.|$))"
			+ "\\p{javaJavaIdentifierStart}" + JAVA_IDENTIFIER_PART + "*";
	private static final String JAVA_TYPE_IDENTIFIER = "(?!(?:var|yield|record|sealed|permits)$)" + JAVA_IDENTIFIER;
	private static final String JAVA_QUALIFIED_IDENTIFIER = JAVA_IDENTIFIER + "(?:\\." + JAVA_IDENTIFIER + ")*";
	private static final String JAVA_CLASS_PATH = "(?:" + JAVA_IDENTIFIER + "\\.)*" + JAVA_TYPE_IDENTIFIER;

	/** Matches a dot-separated Java package name. */
	private static final Pattern PACKAGE_PATTERN = Pattern.compile("^" + JAVA_QUALIFIED_IDENTIFIER + "$");

	/** Matches a single Java type name. */
	private static final Pattern TYPE_NAME_PATTERN = Pattern.compile("^" + JAVA_TYPE_IDENTIFIER + "$");

	/** Matches a fully qualified Java class name. */
	private static final Pattern CLASS_PATH_PATTERN = Pattern.compile("^" + JAVA_CLASS_PATH + "$");

	/** Matches a Java class path or one of the supported special thread tokens. */
	private static final Pattern THREAD_CONSTRUCT_PATTERN = Pattern.compile("^(?:" + JAVA_CLASS_PATH
			+ "|\\*|Lambda-Expression|<implicit-thread-op:(?:parallelStream|parallel|Thread\\.sleep|SubmissionPublisher\\.(?:submit|offer))>)$");

	private JavaNameRules() {
	}

	@Override
	public boolean matchesPackage(@Nullable String value) {
		return PolicyValueValidator.matches(value, PACKAGE_PATTERN);
	}

	@Override
	public boolean matchesTypeName(@Nullable String value) {
		return PolicyValueValidator.matches(value, TYPE_NAME_PATTERN);
	}

	@Override
	public boolean matchesClassPath(@Nullable String value) {
		return PolicyValueValidator.matches(value, CLASS_PATH_PATTERN);
	}

	@Override
	public boolean matchesThreadConstruct(@Nullable String value) {
		return PolicyValueValidator.matches(value, THREAD_CONSTRUCT_PATTERN);
	}

	@Override
	public boolean matchesPackageImport(@Nullable String value) {
		return "*".equals(value) || matchesPackage(value);
	}

	@Override
	public void requirePackage(String field, @Nullable String value) {
		if (!matchesPackage(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.package", field, String.valueOf(value)));
		}
	}

	@Override
	public void requirePackageImport(String field, @Nullable String value) {
		if (!matchesPackageImport(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.package.import", field, String.valueOf(value)));
		}
	}

	@Override
	public void requireTypeName(String field, @Nullable String value) {
		if (!matchesTypeName(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.class.name", field, String.valueOf(value)));
		}
	}

	@Override
	public void requireClassPath(String field, @Nullable String value) {
		if (!matchesClassPath(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.class.path", field, String.valueOf(value)));
		}
	}

	// <editor-fold desc="Compatibility accessors">

	/*
	 * The four accessors below exist for the deprecated
	 * policySubComponents.PolicyValueValidator facade, which republished these
	 * expressions as public constants up to 2.1.2 and must keep doing so for one
	 * deprecation cycle. They are package-private rather than public on purpose:
	 * the facade is the only permitted caller, and routing it through an accessor
	 * keeps each expression declared exactly once, so what the facade advertises
	 * can never drift from what this class enforces. They are deleted together with
	 * the facade.
	 */

	/**
	 * Returns the pattern matching a dot-separated Java package name.
	 *
	 * @since 2.1.3
	 * @author Markus Paulsen
	 * @return the package pattern backing the deprecated facade constant.
	 */
	@Nonnull
	static Pattern packagePattern() {
		return PACKAGE_PATTERN;
	}

	/**
	 * Returns the pattern matching a single Java type name.
	 *
	 * @since 2.1.3
	 * @author Markus Paulsen
	 * @return the type-name pattern backing the deprecated facade constant.
	 */
	@Nonnull
	static Pattern typeNamePattern() {
		return TYPE_NAME_PATTERN;
	}

	/**
	 * Returns the pattern matching a fully qualified Java class name.
	 *
	 * @since 2.1.3
	 * @author Markus Paulsen
	 * @return the class-path pattern backing the deprecated facade constant.
	 */
	@Nonnull
	static Pattern classPathPattern() {
		return CLASS_PATH_PATTERN;
	}

	/**
	 * Returns the pattern matching a Java class path or a special thread token.
	 *
	 * @since 2.1.3
	 * @author Markus Paulsen
	 * @return the thread-construct pattern backing the deprecated facade constant.
	 */
	@Nonnull
	static Pattern threadConstructPattern() {
		return THREAD_CONSTRUCT_PATTERN;
	}
	// </editor-fold>
}
