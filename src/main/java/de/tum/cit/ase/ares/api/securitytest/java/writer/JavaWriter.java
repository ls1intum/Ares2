package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Localisation;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.Phobos;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Writes security test cases in Java programming language.
 * <p>
 * Description: This class is responsible for creating and writing security test
 * files for Java projects, including both architecture test files and AOP test
 * files.
 * <p>
 * Design Rationale: The JavaWriter implements the Writer interface following
 * the Strategy design pattern to allow for different implementation strategies
 * for writing security test cases for different programming languages and
 * frameworks.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaWriter implements Writer {
	@Nonnull
	private final Path projectRoot;
	private final BuildToolConfiguration buildConfiguration;

	public JavaWriter() {
		this(Path.of(""));
	}

	public JavaWriter(@Nonnull Path projectRoot) {
		this.buildConfiguration = null;
		this.projectRoot = BuildToolConfiguration
				.canonicalise(Objects.requireNonNull(projectRoot, "projectRoot must not be null"));
		if (!java.nio.file.Files.isDirectory(this.projectRoot)) {
			throw new IllegalArgumentException("projectRoot must be a directory: " + projectRoot);
		}
	}

	public JavaWriter(@Nonnull BuildToolConfiguration buildConfiguration) {
		this.buildConfiguration = Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
		this.projectRoot = buildConfiguration.projectRoot();
	}

	// <editor-fold desc="Helper methods">

	/**
	 * Creates Java architecture test files.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param architectureMode          the Java architecture mode to use; must not
	 *                                  be null
	 * @param packageName               the name of the package containing the main
	 *                                  class; must not be null
	 * @param mainClassInPackageName    the name of the main class; must not be null
	 * @param javaArchitectureTestCases the list of architecture test cases; must
	 *                                  not be null
	 * @param testFolderPath            the directory of the project; must not be
	 *                                  null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	private List<Path> createJavaArchitectureFiles(@Nonnull ArchitectureMode architectureMode,
			@Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases, @Nonnull Path testFolderPath) {
		return Stream
				.concat(Stream.concat(
						FileTools
								.copyAndFormatFSFiles(architectureMode.fsFilesToCopy(),
										confineTargets(architectureMode.fsTargetsToCopyTo(
												FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
										architectureMode.fsFormatValues(packageName, mainClassInPackageName))
								.stream(),
						FileTools.copyAndFormatNonFSFiles(architectureMode.nonFSFilesToCopy(),
								confineTargets(architectureMode.nonFSTargetsToCopyTo(
										FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
								architectureMode.placeholderValues(),
								architectureMode.nonFSFormatValues(packageName, mainClassInPackageName)).stream()),
						Stream.of(FileTools.createThreePartedFormatStringFile(architectureMode.threePartedFileHeader(),
								architectureMode.threePartedFileBody(javaArchitectureTestCases),
								architectureMode.threePartedFileFooter(),
								confineToProject(architectureMode.targetToCopyTo(
										FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
								architectureMode.formatValues(packageName))))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	/**
	 * Creates Java AOP test files.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param aopMode                the Java AOP mode to use; must not be null
	 * @param essentialClasses       the list of essential classes; must not be null
	 * @param testClasses            the list of test classes; must not be null
	 * @param packageName            the name of the package containing the main
	 *                               class; must not be null
	 * @param mainClassInPackageName the name of the main class; must not be null
	 * @param javaAOPTestCases       the list of AOP test cases; must not be null
	 * @param testFolderPath         the directory of the project; must not be null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	private List<Path> createJavaAOPFiles(@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages,
			@Nonnull List<String> essentialClasses, @Nonnull List<String> testClasses, @Nonnull String packageName,
			@Nonnull String mainClassInPackageName, @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
			@Nonnull Path testFolderPath) {
		@Nonnull
		ArrayList<String> allowedClasses = Stream
				.concat(Stream.concat(essentialPackages.stream(), essentialClasses.stream()), testClasses.stream())
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		return Stream.concat(
				Stream.concat(
						FileTools
								.copyAndFormatFSFiles(aopMode.fsFilesToCopy(), confineTargets(aopMode.fsTargetsToCopyTo(
										FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
										aopMode.fsFormatValues(packageName, mainClassInPackageName))
								.stream(),
						FileTools.copyAndFormatNonFSFiles(aopMode.nonFSFilesToCopy(),
								confineTargets(aopMode.nonFSTargetsToCopyTo(
										FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
								aopMode.placeholderValues(),
								aopMode.nonFSFormatValues(packageName, mainClassInPackageName)).stream()),
				Stream.of(FileTools.createThreePartedFormatStringFile(aopMode.threePartedFileHeader(),
						aopMode.threePartedFileBody(aopMode.toString(), packageName, allowedClasses, javaAOPTestCases),
						aopMode.threePartedFileFooter(),
						confineToProject(aopMode
								.targetToCopyTo(FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
						aopMode.formatValues(packageName))))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	/**
	 * Resolves the resources directory sibling to a source root such as
	 * {@code src/test/java}, replacing its final segment with {@code resources} -
	 * the Maven/Gradle convention. A too-shallow path (e.g. a bare project root)
	 * gets resources placed directly beneath it instead.
	 *
	 * @param testFolderPath the source root, or project root when too shallow; must
	 *                       not be null.
	 * @return the resolved resources directory.
	 */
	@Nonnull
	private Path resolveResourcesFolderPath(@Nonnull Path testFolderPath) {
		int nameCount = testFolderPath.getNameCount();
		if (testFolderPath.toString().isEmpty() || nameCount < 3) {
			return testFolderPath.resolve("resources");
		}
		Path parentPath = testFolderPath.subpath(0, nameCount - 1);
		Path root = testFolderPath.getRoot();
		return (root == null) ? Paths.get(parentPath.toString(), "resources")
				: Paths.get(root.toString(), parentPath.toString(), "resources");
	}

	/**
	 * Copies the localisation files into the resources directory sibling to
	 * {@code testFolderPath}.
	 *
	 * @param testFolderPath the source root; must not be null.
	 * @return the copied files' paths.
	 */
	@Nonnull
	private List<Path> createLocalisationFiles(@Nonnull Path testFolderPath) {
		Path resourcesFolderPath = resolveResourcesFolderPath(testFolderPath);
		return FileTools.copyFiles(Localisation.filesToCopy(),
				confineTargets(Localisation.targetsToCopyTo(resourcesFolderPath)));
	}

	/**
	 * Writes the generated behavioural-defaults resource {@code ConfigurationUtils}
	 * reads at precompile runtime; writes nothing when no category is set.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param testBehaviorConfiguration the configuration to serialise; must not be
	 *                                  null.
	 * @param testFolderPath            the project directory; must not be null.
	 * @return the written file's path, or an empty list if nothing was configured.
	 */
	@Nonnull
	private List<Path> createTestBehaviorFiles(@Nonnull TestBehaviorConfiguration testBehaviorConfiguration,
			@Nonnull Path testFolderPath) {
		PrivilegedExceptionsConfiguration privilegedExceptions = testBehaviorConfiguration
				.regardingPrivilegedExceptions();
		if (privilegedExceptions == null) {
			return List.of();
		}
		Path resourcesFolderPath = resolveResourcesFolderPath(testFolderPath);
		Path target = confineToProject(resourcesFolderPath.resolve(TestBehaviorConfiguration.GENERATED_RESOURCE_PATH));
		String content = "regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported="
				+ privilegedExceptions.onlyPrivilegedExceptionsAreReported() + System.lineSeparator()
				+ "regardingPrivilegedExceptions.theFailureMessageIs=" + privilegedExceptions.theFailureMessageIs()
				+ System.lineSeparator();
		try {
			Files.createDirectories(
					Objects.requireNonNull(target.getParent(), "generated resource target has no parent: " + target));
			Files.writeString(target, content);
		} catch (IOException failure) {
			throw new SecurityException("Unable to write generated test-behaviour resource: " + target, failure);
		}
		return List.of(target);
	}

	@Nonnull
	private List<Path> createPhobosFiles(@Nonnull String packageName,
			@Nonnull List<JavaPhobosTestCase> javaPhobosTestCases, @Nonnull Path testFolderPath) {
		List<Path> copyTargets = Phobos.targetsToCopyTo(testFolderPath).stream().map(this::confineToProject).toList();
		Path editTarget = confineToProject(Phobos.targetToCopyTo(testFolderPath));
		return Stream
				.concat(FileTools.copyFiles(Phobos.filesToCopy(), copyTargets).stream(),
						Stream.of(FileTools.createThreePartedFormatStringFile(Phobos.threePartedFileHeader(),
								Phobos.threePartedFileBody(javaPhobosTestCases), Phobos.threePartedFileFooter(),
								editTarget, Phobos.fileValue(packageName))))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Nonnull
	private List<Path> confineTargets(@Nonnull List<Path> targets) {
		return targets.stream().map(this::confineToProject).toList();
	}

	@Nonnull
	Path confineToProject(@Nonnull Path targetPath) {
		Path requested = Objects.requireNonNull(targetPath, "targetPath must not be null");
		Path resolved = requested.isAbsolute() ? requested : projectRoot.resolve(requested);
		Path canonical = BuildToolConfiguration.canonicalise(resolved);
		if (!canonical.startsWith(projectRoot)) {
			throw new SecurityException("Writer target escapes authorised project root: " + targetPath);
		}
		return canonical;
	}

	// </editor-fold>

	// <editor-fold desc="Write security test cases methods">

	/**
	 * Writes security test cases to files.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param buildMode                 the Java build mode to use; must not be null
	 * @param architectureMode          the Java architecture mode to use; must not
	 *                                  be null
	 * @param aopMode                   the Java AOP mode to use; must not be null
	 * @param essentialPackages         the list of essential packages; must not be
	 *                                  null
	 * @param essentialClasses          the list of essential classes; must not be
	 *                                  null
	 * @param testClasses               the list of test classes; must not be null
	 * @param packageName               the name of the package containing the main
	 *                                  class; must not be null
	 * @param mainClassInPackageName    the name of the main class; must not be null
	 * @param javaArchitectureTestCases the list of architecture test cases; must
	 *                                  not be null
	 * @param javaAOPTestCases          the list of AOP test cases; must not be null
	 * @param javaPhobosTestCases       the list of Phobos test cases; must not be
	 *                                  null
	 * @param testBehaviorConfiguration the behavioural test-lifecycle configuration
	 *                                  to carry forward for a precompile
	 *                                  deployment; must not be null.
	 * @param testFolderPath            the directory of the project; must not be
	 *                                  null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	public List<Path> writeTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
			@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull List<JavaAOPTestCase> javaAOPTestCases, @Nonnull List<JavaPhobosTestCase> javaPhobosTestCases,
			@Nonnull TestBehaviorConfiguration testBehaviorConfiguration, @Nonnull Path testFolderPath) {
		Objects.requireNonNull(buildMode, "buildMode must not be null");
		if (buildConfiguration != null && buildMode != buildConfiguration.buildMode()) {
			throw new IllegalStateException("Writer build mode does not match the discovered build configuration");
		}
		boolean descriptorPresent = java.util.Arrays.stream(buildMode.fileName()).map(projectRoot::resolve)
				.anyMatch(java.nio.file.Files::isRegularFile);
		if (!descriptorPresent) {
			throw new IllegalStateException(
					"Selected " + buildMode + " layout has no build descriptor in " + projectRoot);
		}
		Path validatedTestFolderPath = confineToProject(
				Objects.requireNonNull(testFolderPath, "testFolderPath must not be null"));
		return Stream
				.of(createJavaArchitectureFiles(architectureMode, packageName, mainClassInPackageName,
						javaArchitectureTestCases, validatedTestFolderPath).stream(),
						createJavaAOPFiles(aopMode, essentialPackages, essentialClasses, testClasses, packageName,
								mainClassInPackageName, javaAOPTestCases, validatedTestFolderPath).stream(),
						createLocalisationFiles(validatedTestFolderPath).stream(),
						createPhobosFiles(packageName, javaPhobosTestCases, validatedTestFolderPath).stream(),
						createTestBehaviorFiles(testBehaviorConfiguration, validatedTestFolderPath).stream())
				.flatMap(s -> s).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}
	// </editor-fold>
}
