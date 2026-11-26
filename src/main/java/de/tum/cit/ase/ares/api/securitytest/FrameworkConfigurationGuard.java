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
 * supervises anything. The build boundary rejects these files first. This check
 * looks only in the default output folders and runs when JUnit opens the run,
 * too late for a JUnit listener or the JaCoCo agent, which read theirs before.
 */
public final class FrameworkConfigurationGuard {

	/**
	 * The folders the build tools compile a submission into, relative to the
	 * working directory.
	 */
	private static final List<Path> PRODUCTION_OUTPUTS = List.of(Path.of("target", "classes"),
			Path.of("build", "classes", "java", "main"), Path.of("build", "resources", "main"));

	/**
	 * The folders below an output whose every file names classes to create: Java
	 * services and Mockito plugins.
	 */
	private static final List<Path> SERVICE_FOLDERS = List.of(Path.of("META-INF", "services"),
			Path.of("mockito-extensions"));

	/**
	 * The settings files at the top of an output that a test library or agent
	 * reads: they can name classes to create or hide findings.
	 */
	private static final Set<String> SETTINGS_FILES = Set.of("archunit.properties", "archunit_ignore_patterns.txt",
			"junit-platform.properties", "logback.xml", "logback-test.xml", "logback.groovy", "logback.scmo",
			"logback-test.scmo", "jacoco-agent.properties");

	/**
	 * The name of the module descriptor the compiler writes for a module.
	 */
	private static final String MODULE_DESCRIPTOR = "module-info.class";

	/**
	 * The folder of a multi-release output that holds one folder per Java version.
	 */
	private static final Path VERSIONS_FOLDER = Path.of("META-INF", "versions");

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
	 * Returns the first settings file, service file, plugin file or module
	 * descriptor in an output, if any.
	 *
	 * @param output the output folder
	 * @return the file found
	 */
	@Nonnull
	static Optional<Path> firstSettingsFileIn(@Nonnull Path output) {
		Optional<Path> settings = SETTINGS_FILES.stream().sorted().map(output::resolve)
				.filter(file -> Files.exists(file, LinkOption.NOFOLLOW_LINKS)).findFirst();
		if (settings.isPresent()) {
			return settings;
		}
		Optional<Path> service = SERVICE_FOLDERS.stream().map(output::resolve)
				.map(FrameworkConfigurationGuard::firstServiceFileIn).flatMap(Optional::stream).findFirst();
		return service.isPresent() ? service : firstModuleDescriptorIn(output);
	}

	/**
	 * Returns a module descriptor at the top of an output or below its versioned
	 * folders, if any. Its {@code provides} lines make the JDK create a class the
	 * student wrote, for example when Logback asks for an XML parser.
	 *
	 * @param output the output folder
	 * @return the descriptor found
	 */
	@Nonnull
	private static Optional<Path> firstModuleDescriptorIn(@Nonnull Path output) {
		Path descriptor = output.resolve(MODULE_DESCRIPTOR);
		if (Files.exists(descriptor, LinkOption.NOFOLLOW_LINKS)) {
			return Optional.of(descriptor);
		}
		Path versions = output.resolve(VERSIONS_FOLDER);
		if (!Files.isDirectory(versions, LinkOption.NOFOLLOW_LINKS)) {
			return Optional.empty();
		}
		try (Stream<Path> entries = Files.list(versions)) {
			return entries.map(version -> version.resolve(MODULE_DESCRIPTOR))
					.filter(file -> Files.exists(file, LinkOption.NOFOLLOW_LINKS)).sorted().findFirst();
		} catch (IOException | UncheckedIOException unreadable) {
			return Optional.of(versions);
		}
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
