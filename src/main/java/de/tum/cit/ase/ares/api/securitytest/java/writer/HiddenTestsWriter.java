package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.javaparser.ast.body.TypeDeclaration;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.HiddenTestsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Writes the hidden-test hook of a precompile run and its sentinel test, only
 * while the policy sets {@code regardingHiddenTests}, and removes them again
 * once it does not. Every hidden-test entry must match a test source.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class HiddenTestsWriter {

	/** The shared generated-file handling of this exercise. */
	@Nonnull
	private final GeneratedHookFiles hookFiles;

	/**
	 * Creates the writer for one exercise.
	 *
	 * @param hookFiles the shared generated-file handling.
	 */
	HiddenTestsWriter(@Nonnull GeneratedHookFiles hookFiles) {
		this.hookFiles = Objects.requireNonNull(hookFiles, "hookFiles must not be null");
	}

	/**
	 * Writes or removes the hidden-test hook for the policy's setting.
	 *
	 * @param configuration  the policy's behaviour configuration.
	 * @param packageName    the exercise package the copied Ares classes live in.
	 * @param testFolderPath the test source root.
	 * @return what was written and which hook to register; nothing when the setting
	 *         is absent
	 * @throws SecurityException naming the first entry no test source matches
	 */
	@Nonnull
	GeneratedHookFiles.Contribution write(@Nonnull TestBehaviorConfiguration configuration, @Nonnull String packageName,
			@Nonnull Path testFolderPath) {
		HiddenTestsConfiguration hiddenTests = configuration.regardingHiddenTests();
		if (hiddenTests == null) {
			hookFiles.deleteGenerated(testFolderPath, HiddenTestsSources.JUPITER_HOOK);
			hookFiles.deleteGenerated(testFolderPath, HiddenTestsSources.JUPITER_SENTINEL);
			return GeneratedHookFiles.Contribution.NONE;
		}
		TestSourceDeclarations declarations = new TestSourceDeclarations(testFolderPath);
		for (String entry : hiddenTests.theFollowingTestsAreHidden()) {
			requireMatch(entry, declarations);
		}
		String messagesClass = packageName + ".ares.api.localization.Messages";
		List<Path> written = List.of(
				hookFiles.writeSource(testFolderPath, HiddenTestsSources.JUPITER_HOOK,
						HiddenTestsSources.jupiterHook(messagesClass)),
				hookFiles.writeSource(testFolderPath, HiddenTestsSources.JUPITER_SENTINEL,
						HiddenTestsSources.jupiterSentinel(messagesClass)));
		return new GeneratedHookFiles.Contribution(written, List.of(HiddenTestsSources.JUPITER_HOOK), List.of());
	}

	/**
	 * Fails unless an entry names a class declared in the test sources and, if it
	 * names a method, one that class declares or inherits. A method of an enclosing
	 * class does not count, since the generated hook would never match it.
	 *
	 * @param entry        the entry, {@code pkg.Class} or {@code pkg.Class#m}.
	 * @param declarations the classes of the test sources.
	 * @throws SecurityException naming the entry
	 */
	private static void requireMatch(@Nonnull String entry, @Nonnull TestSourceDeclarations declarations) {
		int hash = entry.indexOf('#');
		Optional<TypeDeclaration<?>> testClass = declarations.findClass(hash < 0 ? entry : entry.substring(0, hash));
		boolean matches = testClass.isPresent()
				&& (hash < 0 || declarations.hasMethod(testClass.get(), entry.substring(hash + 1)));
		if (!matches) {
			throw new SecurityException(Messages.localized("security.writer.hidden.tests.unmatched", entry));
		}
	}
}
