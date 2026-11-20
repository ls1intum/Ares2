package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.InputOutputManagerKind;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the output-mirroring hook of a precompile run and its sentinel test,
 * only while the policy sets {@code regardingTheOutputOfTheTestCases}, and
 * removes them again once it does not.
 *
 * @since 2.3.0
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
		requireSupported(configuration);
		if (!contributes(configuration)) {
			hookFiles.deleteGenerated(testFolderPath, OutputMirroringSources.JUPITER_HOOK);
			hookFiles.deleteGenerated(testFolderPath, OutputMirroringSources.JUPITER_SENTINEL);
			return GeneratedHookFiles.Contribution.NONE;
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = List.of(
				hookFiles.writeSource(testFolderPath, OutputMirroringSources.JUPITER_HOOK,
						OutputMirroringSources.jupiterHook(messagesClass,
								HiddenTestsWriter.contributes(configuration))),
				hookFiles.writeSource(testFolderPath, OutputMirroringSources.JUPITER_SENTINEL,
						OutputMirroringSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(OutputMirroringSources.JUPITER_HOOK));
	}

	/**
	 * Fails when the policy asks for a custom I/O manager, which a precompile
	 * exercise cannot load.
	 *
	 * @param configuration the policy's behaviour configuration.
	 * @throws SecurityException when the manager is {@code CUSTOM}
	 */
	static void requireSupported(@Nonnull TestBehaviorConfiguration configuration) {
		OutputConfiguration output = configuration.regardingTheOutputOfTheTestCases();
		if (output != null && output.managerKind() == InputOutputManagerKind.CUSTOM) {
			throw new SecurityException(Messages.localized("security.writer.io.manager.custom.precompile"));
		}
	}

	/**
	 * Whether the policy needs the hook: it sets the output category with a manager
	 * other than {@code NONE}.
	 *
	 * @param configuration the policy's behaviour configuration.
	 * @return true when the hook is written
	 */
	static boolean contributes(@Nonnull TestBehaviorConfiguration configuration) {
		OutputConfiguration output = configuration.regardingTheOutputOfTheTestCases();
		return output != null && output.managerKind() != InputOutputManagerKind.NONE;
	}
}
