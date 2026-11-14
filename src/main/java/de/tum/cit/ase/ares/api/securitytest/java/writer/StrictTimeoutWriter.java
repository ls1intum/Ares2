package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the strict-timeout hooks of a precompile run: a JUnit interceptor and
 * a sentinel test proving it ran. Writes them only while the policy sets
 * {@code regardingTheStrictTimeoutForTheTestCases}, and removes them again once
 * it does not.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
final class StrictTimeoutWriter {

	/** The shared generated-file handling of this exercise. */
	@Nonnull
	private final GeneratedHookFiles hookFiles;

	/**
	 * Creates the writer for one exercise.
	 *
	 * @param hookFiles the shared generated-file handling.
	 */
	StrictTimeoutWriter(@Nonnull GeneratedHookFiles hookFiles) {
		this.hookFiles = Objects.requireNonNull(hookFiles, "hookFiles must not be null");
	}

	/**
	 * Writes or removes the strict-timeout hooks for the policy's setting.
	 *
	 * @param configuration  the policy's behaviour configuration.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root.
	 * @return what was written and which hooks to register; nothing when the
	 *         setting is absent
	 */
	@Nonnull
	GeneratedHookFiles.Contribution write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath) {
		if (!contributes(configuration)) {
			removeAll(testFolderPath);
			return GeneratedHookFiles.Contribution.NONE;
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = new ArrayList<>();
		written.add(hookFiles.writeSource(testFolderPath, StrictTimeoutSources.JUPITER_HOOK,
				StrictTimeoutSources.jupiterHook(messagesClass, HiddenTestsWriter.contributes(configuration))));
		written.add(hookFiles.writeSource(testFolderPath, StrictTimeoutSources.JUPITER_SENTINEL,
				StrictTimeoutSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(StrictTimeoutSources.JUPITER_HOOK));
	}

	/**
	 * Removes every strict-timeout hook and sentinel, source and compiled.
	 *
	 * @param testFolderPath the test source root.
	 */
	private void removeAll(@Nonnull Path testFolderPath) {
		hookFiles.deleteGenerated(testFolderPath, StrictTimeoutSources.JUPITER_HOOK);
		hookFiles.deleteGenerated(testFolderPath, StrictTimeoutSources.JUPITER_SENTINEL);
	}

	/**
	 * Whether the policy needs the hook: it sets a strict timeout.
	 *
	 * @param configuration the policy's behaviour configuration.
	 * @return true when the category is present
	 */
	static boolean contributes(@Nonnull TestBehaviorConfiguration configuration) {
		return configuration.regardingTheStrictTimeoutForTheTestCases() != null;
	}
}
