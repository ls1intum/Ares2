package de.tum.cit.ase.ares.api.securitytest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Refuses a compiled submission that carries a settings file a test library
 * reads, such as a service file or {@code archunit.properties}. Such a file can
 * make JUnit, ArchUnit or Logback create a class the student wrote before Ares
 * supervises anything. The build boundary rejects these files first; this check
 * catches an exercise whose build does not.
 */
public final class FrameworkConfigurationGuard {

	/**
	 * The folders the build tools compile a submission into, relative to the
	 * working directory.
	 */
	private static final List<Path> PRODUCTION_OUTPUTS = List.of(Path.of("target", "classes"),
			Path.of("build", "classes", "java", "main"), Path.of("build", "resources", "main"));

	/**
	 * The folder below an output whose every file names classes to create.
	 */
	private static final Path SERVICE_FOLDER = Path.of("META-INF", "services");

	/**
	 * The settings files at the top of an output that name classes to create.
	 */
	private static final Set<String> SETTINGS_FILES = Set.of("archunit.properties", "junit-platform.properties",
			"logback.xml", "logback-test.xml", "logback.groovy");

	/**
	 * Prevents instantiation, since the class only offers a static check.
	 */
	private FrameworkConfigurationGuard() {
		throw new IllegalStateException(
				Messages.localized("security.general.utility.initialization", "FrameworkConfigurationGuard"));
	}

	/**
	 * Refuses the run if an output of the working directory carries such a file.
	 * The folder Ares itself was loaded from is skipped, since that is Ares's own
	 * build rather than a submission.
	 *
	 * @throws SecurityException naming the first such file
	 */
	public static void requireNone() {
		Path aresOrigin = aresOrigin();
		for (Path output : PRODUCTION_OUTPUTS) {
			Path absolute = output.toAbsolutePath().normalize();
			if (Files.isDirectory(absolute) && !absolute.equals(aresOrigin)) {
				firstSettingsFileIn(absolute).ifPresent(file -> {
					throw new SecurityException(Messages.localized("security.framework.configuration",
							absolute.relativize(file).toString(), absolute.toString()));
				});
			}
		}
	}

	/**
	 * Returns the first settings file or service file in an output, if any.
	 *
	 * @param output the output folder
	 * @return the file found
	 */
	@Nonnull
	static Optional<Path> firstSettingsFileIn(@Nonnull Path output) {
		Optional<Path> settings = SETTINGS_FILES.stream().sorted().map(output::resolve)
				.filter(file -> Files.exists(file, LinkOption.NOFOLLOW_LINKS)).findFirst();
		return settings.isPresent() ? settings : firstServiceFileIn(output.resolve(SERVICE_FOLDER));
	}

	/**
	 * Returns the first entry below the service folder, if it exists.
	 *
	 * @param serviceFolder the service folder of an output
	 * @return the entry found
	 */
	@Nonnull
	private static Optional<Path> firstServiceFileIn(@Nonnull Path serviceFolder) {
		if (!Files.exists(serviceFolder, LinkOption.NOFOLLOW_LINKS)) {
			return Optional.empty();
		}
		if (!Files.isDirectory(serviceFolder, LinkOption.NOFOLLOW_LINKS)) {
			return Optional.of(serviceFolder);
		}
		try (Stream<Path> entries = Files.walk(serviceFolder)) {
			return entries.filter(entry -> !entry.equals(serviceFolder)).sorted().findFirst();
		} catch (IOException | UncheckedIOException unreadable) {
			return Optional.of(serviceFolder);
		}
	}

	/**
	 * Returns the folder this class was loaded from, or {@code null} when it came
	 * from an archive or cannot be told.
	 *
	 * @return the folder this class was loaded from
	 */
	@Nullable
	private static Path aresOrigin() {
		CodeSource source = FrameworkConfigurationGuard.class.getProtectionDomain().getCodeSource();
		if (source == null || source.getLocation() == null || !"file".equals(source.getLocation().getProtocol())) {
			return null;
		}
		try {
			return Path.of(source.getLocation().toURI()).toAbsolutePath().normalize();
		} catch (URISyntaxException | IllegalArgumentException unreadable) {
			return null;
		}
	}
}
