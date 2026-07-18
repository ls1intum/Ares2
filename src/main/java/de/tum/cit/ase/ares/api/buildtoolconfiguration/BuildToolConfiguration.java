package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable, shared description of a discovered Java build. All paths are
 * absolute, normalised and confined to the canonical project root.
 */
public record BuildToolConfiguration(BuildMode buildMode, Path projectRoot, List<Path> productionSourceRoots,
		List<Path> testSourceRoots, Path productionOutputRoot, Path testOutputRoot) {

	private static final Logger LOG = LoggerFactory.getLogger(BuildToolConfiguration.class);

	public BuildToolConfiguration {
		buildMode = Objects.requireNonNull(buildMode, "buildMode must not be null");
		projectRoot = canonicalise(Objects.requireNonNull(projectRoot, "projectRoot must not be null"));
		productionSourceRoots = validateRoots(projectRoot, productionSourceRoots, "productionSourceRoots");
		testSourceRoots = validateRoots(projectRoot, testSourceRoots, "testSourceRoots");
		productionOutputRoot = validateContained(projectRoot, productionOutputRoot, "productionOutputRoot");
		testOutputRoot = validateContained(projectRoot, testOutputRoot, "testOutputRoot");
	}

	/**
	 * Resolves instructor-controlled bytecode scope against this build's outputs.
	 */
	public String classpath(Path withinPath, String packageName) {
		Path base = productionOutputRoot;
		String remainder;
		if (withinPath == null || withinPath.toString().isEmpty()) {
			remainder = packageName == null ? "" : packageName.replace('.', '/');
		} else {
			String configured = withinPath.toString();
			if (configured.startsWith("classes/java/main/")) {
				remainder = configured.substring("classes/java/main/".length());
			} else if (configured.startsWith("classes/")) {
				remainder = configured.substring("classes/".length());
			} else if (configured.startsWith("test-classes/java/test/")) {
				base = testOutputRoot;
				remainder = configured.substring("test-classes/java/test/".length());
			} else if (configured.startsWith("test-classes/")) {
				base = testOutputRoot;
				remainder = configured.substring("test-classes/".length());
			} else {
				remainder = configured;
			}
		}
		String resolved = remainder.isEmpty() ? base.toString() : base.resolve(remainder).toString();
		LOG.info("Resolved Ares analysis/import path for {}: {}", buildMode, resolved);
		return resolved;
	}

	private static List<Path> validateRoots(Path projectRoot, List<Path> roots, String name) {
		Objects.requireNonNull(roots, name + " must not be null");
		return roots.stream().map(root -> validateContained(projectRoot, root, name)).distinct().sorted().toList();
	}

	private static Path validateContained(Path projectRoot, Path path, String name) {
		Path candidate = Objects.requireNonNull(path, name + " must not contain null");
		Path resolved = candidate.isAbsolute() ? candidate : projectRoot.resolve(candidate);
		Path canonical = canonicalise(resolved);
		if (!canonical.startsWith(projectRoot)) {
			throw new SecurityException(name + " escapes project root: " + path);
		}
		return canonical;
	}

	/**
	 * Canonicalises the nearest existing ancestor and safely appends missing names.
	 */
	public static Path canonicalise(Path path) {
		Path absolute = path.toAbsolutePath().normalize();
		Path existing = absolute;
		while (existing != null && !Files.exists(existing)) {
			existing = existing.getParent();
		}
		if (existing == null) {
			throw new SecurityException("Path has no existing ancestor: " + path);
		}
		try {
			Path realExisting = existing.toRealPath();
			return realExisting.resolve(existing.relativize(absolute)).normalize();
		} catch (IOException exception) {
			throw new SecurityException("Cannot canonicalise path: " + path, exception);
		}
	}
}
