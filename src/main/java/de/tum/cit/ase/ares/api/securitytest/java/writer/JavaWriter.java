package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.localization.Localisation;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.Phobos;
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
	 * @param testFolderPath            the directory of the project; may be null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	private List<Path> createJavaArchitectureFiles(@Nonnull ArchitectureMode architectureMode,
			@Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases, @Nullable Path testFolderPath) {
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
								confineToWorkingDirectory(architectureMode.targetToCopyTo(
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
	 * @param testFolderPath         the directory of the project; may be null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	private List<Path> createJavaAOPFiles(@Nonnull AOPMode aopMode, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaAOPTestCase> javaAOPTestCases, @Nullable Path testFolderPath) {
		@Nonnull
		ArrayList<String> allowedClasses = Stream.concat(essentialClasses.stream(), testClasses.stream())
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
						confineToWorkingDirectory(aopMode
								.targetToCopyTo(FileTools.resolveOnPath(testFolderPath, packageName.split("\\.")))),
						aopMode.formatValues(packageName))))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Nonnull
	private List<Path> createLocalisationFiles(@Nullable Path testFolderPath) {
		if (testFolderPath == null || testFolderPath.toString().isBlank()) {
			return List.of();
		}

		int nameCount = testFolderPath.getNameCount();
		Path resourcesFolderPath;
		if (testFolderPath.toString().isEmpty() || nameCount < 3) {
			// Too shallow to strip the trailing two segments (e.g. the project root in
			// precompile mode): place the resources folder directly beneath it.
			resourcesFolderPath = testFolderPath.resolve("resources");
		} else {
			Path parentPath = testFolderPath.subpath(0, nameCount - 2);
			Path root = testFolderPath.getRoot();
			resourcesFolderPath = (root == null) ? Paths.get(parentPath.toString(), "resources")
					: Paths.get(root.toString(), parentPath.toString(), "resources");
		}

		return FileTools.copyFiles(Localisation.filesToCopy(),
				confineTargets(Localisation.targetsToCopyTo(resourcesFolderPath)));
	}

	@Nonnull
	private List<Path> createPhobosFiles(@Nonnull String packageName,
			@Nonnull List<JavaPhobosTestCase> javaPhobosTestCases, @Nullable Path testFolderPath) {
		if (testFolderPath == null || testFolderPath.toString().isBlank()) {
			return List.of();
		}
		List<Path> copyTargets = Phobos.targetsToCopyTo(testFolderPath).stream()
				.map(JavaWriter::confineToWorkingDirectory).toList();
		Path editTarget = confineToWorkingDirectory(Phobos.targetToCopyTo(testFolderPath));
		return Stream
				.concat(FileTools.copyFiles(Phobos.filesToCopy(), copyTargets).stream(),
						Stream.of(FileTools.createThreePartedFormatStringFile(Phobos.threePartedFileHeader(),
								Phobos.threePartedFileBody(javaPhobosTestCases), Phobos.threePartedFileFooter(),
								editTarget, Phobos.fileValue(packageName))))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	/**
	 * Confines a relative target path to the working directory.
	 * <p>
	 * The Phobos targets climb three levels above a {@code src/test/java}-style
	 * test folder. For shallower folders (e.g. the project root in precompile mode)
	 * that climb would escape the project, so escaping {@code ..} segments of
	 * relative targets are dropped after normalisation.
	 * </p>
	 *
	 * @param targetPath the resolved target path; must not be null
	 * @return the normalised target path without escaping {@code ..} segments
	 */
	/**
	 * Confines every target in a list to the working directory (see
	 * {@link #confineToWorkingDirectory}), so a crafted package string containing
	 * {@code ..} segments cannot redirect generated files outside the project. A
	 * normal in-project target is returned unchanged.
	 *
	 * @param targets the resolved target paths; must not be null
	 * @return the confined target paths
	 */
	@Nonnull
	private static List<Path> confineTargets(@Nonnull List<Path> targets) {
		return targets.stream().map(JavaWriter::confineToWorkingDirectory).toList();
	}

	@Nonnull
	private static Path confineToWorkingDirectory(@Nonnull Path targetPath) {
		Path normalised = targetPath.normalize();
		if (normalised.isAbsolute()) {
			Path workingDirectory = Path.of("").toAbsolutePath().normalize();
			if (normalised.startsWith(workingDirectory)) {
				return normalised;
			}
			// An absolute target that escapes the working directory is re-anchored
			// inside it: relativise against the working directory and drop the escaping
			// ".." segments, mirroring the relative-path handling below.
			return stripLeadingParentSegments(workingDirectory.relativize(normalised));
		}
		return stripLeadingParentSegments(normalised);
	}

	/**
	 * Drops any leading {@code ".."} segments from a relative path so it cannot
	 * escape the working directory.
	 *
	 * @param path the relative path to confine; must not be null
	 * @return the path with leading {@code ".."} segments removed
	 */
	@Nonnull
	private static Path stripLeadingParentSegments(@Nonnull Path path) {
		int firstRealName = 0;
		while (firstRealName < path.getNameCount() && path.getName(firstRealName).toString().equals("..")) {
			firstRealName++;
		}
		if (firstRealName == 0) {
			return path;
		}
		return firstRealName == path.getNameCount() ? Path.of("") : path.subpath(firstRealName, path.getNameCount());
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
	 * @param testFolderPath            the directory of the project; may be null
	 * @return a list of paths to the created files
	 */
	@Nonnull
	public List<Path> writeTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
			@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull List<JavaAOPTestCase> javaAOPTestCases, @Nonnull List<JavaPhobosTestCase> javaPhobosTestCases,
			@Nullable Path testFolderPath) {
		return Stream
				.of(createJavaArchitectureFiles(architectureMode, packageName, mainClassInPackageName,
						javaArchitectureTestCases, testFolderPath).stream(),
						createJavaAOPFiles(aopMode, essentialClasses, testClasses, packageName, mainClassInPackageName,
								javaAOPTestCases, testFolderPath).stream(),
						createLocalisationFiles(testFolderPath).stream(),
						createPhobosFiles(packageName, javaPhobosTestCases, testFolderPath).stream())
				.flatMap(s -> s).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}
	// </editor-fold>
}
