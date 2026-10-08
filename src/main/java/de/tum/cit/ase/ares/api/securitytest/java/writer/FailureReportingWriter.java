package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the failure-reporting hooks of a precompile run: a JUnit extension
 * that shows the policy's message instead of a failed test's real error, and a
 * sentinel test proving it ran. Writes them only while the policy switches
 * {@code regardingPrivilegedExceptions} on, and removes them again otherwise.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class FailureReportingWriter {

	/** The shared generated-file handling of this exercise. */
	@Nonnull
	private final GeneratedHookFiles hookFiles;

	/**
	 * Creates the writer for one exercise.
	 *
	 * @param hookFiles the shared generated-file handling.
	 */
	FailureReportingWriter(@Nonnull GeneratedHookFiles hookFiles) {
		this.hookFiles = Objects.requireNonNull(hookFiles, "hookFiles must not be null");
	}

	/**
	 * Writes or removes the failure-reporting hooks for the policy's setting.
	 *
	 * @param configuration  the policy's behaviour configuration.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root.
	 * @return what was written and which hook to register; nothing when the setting
	 *         is absent or switched off
	 */
	@Nonnull
	GeneratedHookFiles.Contribution write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath) {
		if (!isEnabled(configuration)) {
			hookFiles.deleteGenerated(testFolderPath, FailureReportingSources.JUPITER_HOOK);
			hookFiles.deleteGenerated(testFolderPath, FailureReportingSources.JUPITER_SENTINEL);
			return GeneratedHookFiles.Contribution.NONE;
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = List.of(
				hookFiles.writeSource(testFolderPath, FailureReportingSources.JUPITER_HOOK,
						FailureReportingSources.jupiterHook(messagesClass)),
				hookFiles.writeSource(testFolderPath, FailureReportingSources.JUPITER_SENTINEL,
						FailureReportingSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(FailureReportingSources.JUPITER_HOOK));
	}

	/**
	 * Whether the policy switches privileged-exceptions-only reporting on.
	 *
	 * @param configuration the policy's behaviour configuration.
	 * @return true only for a present category set to true
	 */
	private static boolean isEnabled(@Nonnull TestBehaviorConfiguration configuration) {
		PrivilegedExceptionsConfiguration category = configuration.regardingPrivilegedExceptions();
		return category != null && category.onlyPrivilegedExceptionsAreReported();
	}
}
