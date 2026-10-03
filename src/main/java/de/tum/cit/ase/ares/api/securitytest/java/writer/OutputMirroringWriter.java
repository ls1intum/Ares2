package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the output-mirroring hook of a precompile run and its sentinel test,
 * only while the policy sets {@code regardingOutputMirroring}, and removes them
 * again once it does not.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class OutputMirroringWriter {

	/** The shared generated-file handling of this exercise. */
	@Nonnull
	private final GeneratedHookFiles hookFiles;

	/**
	 * Creates the writer for one exercise.
	 *
	 * @param hookFiles the shared generated-file handling.
	 */
	OutputMirroringWriter(@Nonnull GeneratedHookFiles hookFiles) {
		this.hookFiles = Objects.requireNonNull(hookFiles, "hookFiles must not be null");
	}

	/**
	 * Writes or removes the output-mirroring hook for the policy's setting.
	 *
	 * @param configuration  the policy's behaviour configuration.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root.
	 * @return what was written and which hook to register; nothing when the setting
	 *         is absent
	 */
	@Nonnull
	GeneratedHookFiles.Contribution write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath) {
		if (configuration.regardingOutputMirroring() == null) {
			hookFiles.deleteGenerated(testFolderPath, OutputMirroringSources.JUPITER_HOOK);
			hookFiles.deleteGenerated(testFolderPath, OutputMirroringSources.JUPITER_SENTINEL);
			return GeneratedHookFiles.Contribution.NONE;
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = List.of(
				hookFiles.writeSource(testFolderPath, OutputMirroringSources.JUPITER_HOOK,
						OutputMirroringSources.jupiterHook(messagesClass)),
				hookFiles.writeSource(testFolderPath, OutputMirroringSources.JUPITER_SENTINEL,
						OutputMirroringSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(OutputMirroringSources.JUPITER_HOOK), List.of());
	}
}
