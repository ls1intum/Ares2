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
	private static final String JAVA_IDENTIFIER = "(?!(?:" + JAVA_RESERVED_WORD
			+ ")(?=\\.|$))\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
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
}
