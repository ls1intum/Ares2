package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

/**
 * Resolvers for the non-policy test annotations retained by Ares, and their
 * policy fallbacks.
 */
@API(status = Status.INTERNAL)
public final class ConfigurationUtils {

	private ConfigurationUtils() {
	}

	/**
	 * Resolves whether standard output should be mirrored for a test.
	 *
	 * @param context the current test context
	 * @return whether mirroring is enabled
	 */
	public static boolean shouldMirrorOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::value)
				.map(MirrorOutputPolicy::isEnabled).orElse(false);
	}

	/**
	 * Resolves the maximum number of standard-output characters for a test.
	 *
	 * @param context the current test context
	 * @return the configured limit
	 */
	public static long getMaxStandardOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::maxCharCount)
				.orElse(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	/**
	 * Resolves the message a failed test shows instead of its real error, if any.
	 * Checks, in order: the nearest {@code @PrivilegedExceptionsOnly}; the policy
	 * file named by an active {@code @Policy} (postcompile); the settings class a
	 * precompile run generated. An active policy's answer is final, even
	 * "disabled", so a leftover generated class can never override it.
	 *
	 * @param context the current test context
	 * @return the configured message, if privileged-exceptions-only reporting is
	 *         effectively enabled
	 */
	public static Optional<String> getNonprivilegedFailureMessage(TestContext context) {
		Optional<String> fromAnnotation = TestContextUtils.findAnnotationIn(context, PrivilegedExceptionsOnly.class)
				.map(PrivilegedExceptionsOnly::value);
		if (fromAnnotation.isPresent()) {
			return fromAnnotation;
		}
		Optional<Path> dynamicPolicyPath = activeDynamicPolicyPath(context);
		if (dynamicPolicyPath.isPresent()) {
			SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(dynamicPolicyPath.get())
					.readSecurityPolicyFrom(dynamicPolicyPath.get());
			return privilegedExceptionsMessageFrom(securityPolicy);
		}
		return resolveFromGeneratedSettingsClass(context);
	}

	/**
	 * Resolves the file path of the policy YAML dynamically active for this test,
	 * exactly as {@code JupiterSecurityExtension}/{@code JqwikSecurityExtension}
	 * already do at real test-run time - skipping {@code SecurityPolicyDirector},
	 * since nothing here needs test-case creation.
	 *
	 * @param context the current test context
	 * @return the active policy's path, or empty when no policy dynamically applies
	 */
	private static Optional<Path> activeDynamicPolicyPath(TestContext context) {
		Optional<Policy> policyAnnotation = TestContextUtils.findAnnotationIn(context, Policy.class);
		if (policyAnnotation.isEmpty() || !policyAnnotation.get().activated()
				|| policyAnnotation.get().value().isBlank()) {
			return Optional.empty();
		}
		return Optional.of(JupiterSecurityExtension.testAndGetPolicyValue(policyAnnotation.get()));
	}

	/**
	 * Extracts the effective privileged-exceptions message from a resolved policy.
	 *
	 * @param securityPolicy the policy to read; must not be null.
	 * @return the configured message, if the policy enables the feature.
	 */
	@Nonnull
	private static Optional<String> privilegedExceptionsMessageFrom(SecurityPolicy securityPolicy) {
		SupervisedCode supervisedCode = securityPolicy.regardingTheSupervisedCode();
		PrivilegedExceptionsConfiguration configuration = supervisedCode.theFollowingTestBehaviorIsConfiguredOrEmpty()
				.regardingPrivilegedExceptions();
		if (configuration == null || !configuration.onlyPrivilegedExceptionsAreReported()) {
			return Optional.empty();
		}
		return Optional.of(configuration.theFailureMessageIs());
	}

	/**
	 * Resolves the policy default from the settings class a precompile run
	 * generates. No such class means nothing is configured. A class that does not
	 * sit beside the test classes, or lacks an expected field, fails closed with a
	 * {@link SecurityException}, because "not configured" is the direction that
	 * shows a student the real failure.
	 *
	 * @param context the current test context
	 * @return the configured message, if the generated settings class enables the
	 *         feature
	 */
	private static Optional<String> resolveFromGeneratedSettingsClass(TestContext context) {
		Optional<Class<?>> settingsClass = findGeneratedSettingsClass();
		if (settingsClass.isEmpty()) {
			return Optional.empty();
		}
		requireGeneratedBesideTheTestClass(settingsClass.get(), context);
		return privilegedExceptionsMessageFrom(settingsClass.get());
	}

	/**
	 * Looks up the generated settings class by its fixed name without initialising
	 * it, so that no static initialiser runs before the class is known to be the
	 * generated one.
	 *
	 * @return the class, or empty when no precompile run generated one
	 */
	private static Optional<Class<?>> findGeneratedSettingsClass() {
		try {
			return Optional.of(Class.forName(TestBehaviorConfiguration.GENERATED_CLASS_NAME, false,
					generatedSettingsClassLoader()));
		} catch (ClassNotFoundException notConfigured) {
			return Optional.empty();
		}
	}

	/**
	 * The loader that can see the exercise's own generated classes: the thread
	 * context loader, since Ares itself may be loaded by a different loader than
	 * the exercise's compiled tests.
	 *
	 * @return the current thread's context class loader
	 */
	private static ClassLoader generatedSettingsClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Rejects a settings class that was not compiled into the same output as the
	 * running test class. The generator writes it among the test sources, so a
	 * class of that name anywhere else, such as in student code, is not the
	 * generated one.
	 *
	 * @param settingsClass the class found under the generated name; must not be
	 *                      null.
	 * @param context       the current test context
	 * @throws SecurityException if the two locations differ or cannot be told
	 */
	private static void requireGeneratedBesideTheTestClass(Class<?> settingsClass, TestContext context) {
		Optional<String> settingsLocation = codeSourceLocationOf(settingsClass);
		Optional<String> testLocation = context.testClass().flatMap(ConfigurationUtils::codeSourceLocationOf);
		if (settingsLocation.isEmpty() || !settingsLocation.equals(testLocation)) {
			throw new SecurityException(Messages.localized("security.policy.behavior.settings.class.untrusted",
					TestBehaviorConfiguration.GENERATED_CLASS_NAME, settingsLocation.orElse(null)));
		}
	}

	/**
	 * The location a class was loaded from, such as a class output directory.
	 *
	 * @param type the class to locate; must not be null.
	 * @return the location as a URL string, or empty when the class has none
	 */
	private static Optional<String> codeSourceLocationOf(Class<?> type) {
		return Optional.ofNullable(type.getProtectionDomain().getCodeSource()).map(CodeSource::getLocation)
				.map(URL::toExternalForm);
	}

	/**
	 * Reads the effective message from a trusted generated settings class. A
	 * missing field means the class came from a different Ares version and fails
	 * closed, since treating it as absent would show the real failure.
	 *
	 * @param settingsClass the generated settings class; must not be null.
	 * @return the configured message, if the class enables the feature
	 * @throws SecurityException if an expected field is missing
	 */
	private static Optional<String> privilegedExceptionsMessageFrom(Class<?> settingsClass) {
		boolean enabled;
		String message;
		try {
			Field enabledField = settingsClass
					.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME);
			Field messageField = settingsClass
					.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME);
			enabled = (boolean) enabledField.get(null);
			message = (String) messageField.get(null);
		} catch (NoSuchFieldException versionSkew) {
			throw new SecurityException(Messages.localized("security.policy.behavior.settings.class.malformed",
					TestBehaviorConfiguration.GENERATED_CLASS_NAME), versionSkew);
		} catch (IllegalAccessException impossible) {
			throw new IllegalStateException("Unable to read generated test-behaviour settings class: "
					+ TestBehaviorConfiguration.GENERATED_CLASS_NAME, impossible);
		}
		if (!enabled) {
			return Optional.empty();
		}
		return Optional
				.of(message == null || message.isBlank() ? PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE
						: message);
	}
}
