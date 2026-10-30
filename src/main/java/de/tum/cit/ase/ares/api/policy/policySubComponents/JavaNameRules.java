package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Java implementation of {@link LanguageNameRules}.
 * <p>
 * Description: Holds the patterns that say how packages, types, classes and
 * thread constructs are named in Java, and checks values against them.
 * <p>
 * Design Rationale: Keeping these Java rules here, and not in the shared
 * {@code PolicyValueValidator}, lets another language supply its own
 * {@link LanguageNameRules}. One shared instance suffices because the patterns
 * never change.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
public final class JavaNameRules implements LanguageNameRules {

	/** The shared, stateless instance. */
	@Nonnull
	public static final JavaNameRules INSTANCE = new JavaNameRules();

	/** The Java keywords and literals that cannot be used as a name part. */
	private static final String JAVA_RESERVED_WORD = "abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|false|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|true|try|void|volatile|while|_";
	/**
	 * One character after the first in an identifier, minus the ignorable ones.
	 * <p>
	 * Java's own identifier rule accepts ignorable control and invisible format
	 * characters, such as {@code U+0000} or {@code U+200B}. A name is written into
	 * generated sources, configuration and messages, where such a character breaks
	 * a one-value-per-line file or makes two entries look identical, so it is
	 * refused. The first character needs no such rule, as Java never accepts an
	 * ignorable one there.
	 */
	private static final String JAVA_IDENTIFIER_PART = "[\\p{javaJavaIdentifierPart}&&[^\\p{javaIdentifierIgnorable}]]";

	/** One Java identifier: no reserved word, a start character, then parts. */
	private static final String JAVA_IDENTIFIER = "(?!(?:" + JAVA_RESERVED_WORD + ")(?=\\.|$))"
			+ "\\p{javaJavaIdentifierStart}" + JAVA_IDENTIFIER_PART + "*";
	/**
	 * An identifier that may also name a type, so not a word such as {@code var}.
	 */
	private static final String JAVA_TYPE_IDENTIFIER = "(?!(?:var|yield|record|sealed|permits)$)" + JAVA_IDENTIFIER;
	/** Identifiers joined by dots, such as a package name. */
	private static final String JAVA_QUALIFIED_IDENTIFIER = JAVA_IDENTIFIER + "(?:\\." + JAVA_IDENTIFIER + ")*";
	/** Package identifiers and dots, ending in a type name. */
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

	/** Prevents instances besides {@link #INSTANCE}. */
	private JavaNameRules() {
	}

	/** Tests the value against the Java package pattern. */
	@Override
	public boolean matchesPackage(@Nullable String value) {
		return PolicyValueValidator.matches(value, PACKAGE_PATTERN);
	}

	/** Tests the value against the Java type-name pattern. */
	@Override
	public boolean matchesTypeName(@Nullable String value) {
		return PolicyValueValidator.matches(value, TYPE_NAME_PATTERN);
	}

	/** Tests the value against the Java qualified class name pattern. */
	@Override
	public boolean matchesClassPath(@Nullable String value) {
		return PolicyValueValidator.matches(value, CLASS_PATH_PATTERN);
	}

	/** Tests the value against the Java class name or thread token pattern. */
	@Override
	public boolean matchesThreadConstruct(@Nullable String value) {
		return PolicyValueValidator.matches(value, THREAD_CONSTRUCT_PATTERN);
	}

	/** Accepts the wildcard {@code *} or a Java package name. */
	@Override
	public boolean matchesPackageImport(@Nullable String value) {
		return "*".equals(value) || matchesPackage(value);
	}

	/**
	 * Throws a localised {@link IllegalArgumentException} unless the value is a
	 * Java package.
	 */
	@Override
	public void requirePackage(String field, @Nullable String value) {
		if (!matchesPackage(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.package", field, String.valueOf(value)));
		}
	}

	/**
	 * Throws a localised {@link IllegalArgumentException} unless the value is
	 * {@code *} or a Java package.
	 */
	@Override
	public void requirePackageImport(String field, @Nullable String value) {
		if (!matchesPackageImport(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.package.import", field, String.valueOf(value)));
		}
	}

	/**
	 * Throws a localised {@link IllegalArgumentException} unless the value is a
	 * Java type name.
	 */
	@Override
	public void requireTypeName(String field, @Nullable String value) {
		if (!matchesTypeName(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.value.java.class.name", field, String.valueOf(value)));
		}
	}

	/**
	 * Throws a localised {@link IllegalArgumentException} unless the value is a
	 * qualified Java class name.
	 */
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
