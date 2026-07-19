package de.tum.cit.ase.ares.api.architecture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.opencsv.exceptions.CsvException;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.LruCache;

/**
 * Enum representing the architecture modes for Java security test cases.
 * <p>
 * Description: Provides different modes for architecture test case generation
 * and execution in Java. The modes determine how files and settings are copied
 * and resolved based on the underlying architecture analysis tool.
 * </p>
 * <p>
 * Design Rationale: Using an enum to represent architecture modes centralises
 * configuration and enables future extensions (e.g. supporting WALA) while
 * ensuring that file handling and resource resolution are consistently applied.
 * </p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum ArchitectureMode {

	/**
	 * Archunit mode for analysing Java code with TNGs Archunit.
	 */
	ARCHUNIT,

	/**
	 * WALA mode for analysing Java code with IBMs WALA.
	 */
	WALA;

	private static JavaFileLoader fileLoader = new JavaCSVFileLoader();
	private static final int MAX_CONFIGURATION_COUNT = 100;
	private static final int MAX_JAVA_CLASSES_CACHE_ENTRIES = 256;
	private static final Map<String, FutureTask<JavaClasses>> JAVA_CLASSES_CACHE = LruCache
			.synchronizedCache(MAX_JAVA_CLASSES_CACHE_ENTRIES);

	/**
	 * Replaces the configuration loader used by every architecture mode.
	 *
	 * @param loader the non-null loader to use
	 */
	public static void setFileLoader(JavaFileLoader loader) {
		fileLoader = java.util.Objects.requireNonNull(loader, "loader must not be null");
	}

	// <editor-fold desc="Load configuration">
	/**
	 * Loads the filesystem copy configuration for this mode.
	 *
	 * @return the validated configuration rows
	 */
	public List<List<String>> getCopyFSConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadCopyData(this, true);
			validateConfigurationRows(entries, "copy.fs", false);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(
					Messages.localized("security.architecture.mode.configuration.load.failure", "copy.fs", name()), e);
		}
	}

	/**
	 * Loads the non-filesystem copy configuration for this mode.
	 *
	 * @return the validated configuration rows
	 */
	public List<List<String>> getCopyNonFSConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadCopyData(this, false);
			validateConfigurationRows(entries, "copy.nonfs", false);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(
					Messages.localized("security.architecture.mode.configuration.load.failure", "copy.nonfs", name()),
					e);
		}
	}

	/**
	 * Loads the edit configuration for this mode.
	 *
	 * @return the validated, non-empty configuration rows
	 */
	public List<List<String>> getEditConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadEditData(this);
			validateConfigurationRows(entries, "edit", true);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(
					Messages.localized("security.architecture.mode.configuration.load.failure", "edit", name()), e);
		}
	}

	private void validateConfigurationRows(@Nonnull List<List<String>> entries, @Nonnull String context,
			boolean requireNonEmpty) {
		if (entries == null || requireNonEmpty && entries.isEmpty()) {
			throw new SecurityException(
					Messages.localized("security.architecture.mode.configuration.empty", context, name()));
		}
		boolean validateCountColumn = context.startsWith("copy");
		for (int row = 0; row < entries.size(); row++) {
			List<String> entry = entries.get(row);
			if (entry == null || entry.size() < 3) {
				throw new SecurityException(Messages.localized("security.architecture.mode.configuration.malformed.row",
						context, row, name(), entry == null ? 0 : entry.size()));
			}
			for (int column = 0; column < 3; column++) {
				String cell = entry.get(column);
				if (cell == null || cell.isBlank()) {
					throw new SecurityException(Messages.localized(
							"security.architecture.mode.configuration.blank.cell", context, row, column, name()));
				}
			}
			if (validateCountColumn) {
				validateConfigurationCount(entry.get(1).trim(), context, row);
			}
		}
	}

	private void validateConfigurationCount(@Nonnull String countCell, @Nonnull String context, int row) {
		int count;
		try {
			count = Integer.parseInt(countCell);
		} catch (NumberFormatException notAnInteger) {
			throw new SecurityException(Messages.localized("security.architecture.mode.configuration.invalid.count",
					context, row, name(), countCell), notAnInteger);
		}
		if (count < 0 || count > MAX_CONFIGURATION_COUNT) {
			throw new SecurityException(Messages.localized("security.architecture.mode.configuration.invalid.count",
					context, row, name(), countCell));
		}
	}
	// </editor-fold>

	// <editor-fold desc="Multi-file methods">

	/**
	 * Resolves the filesystem templates copied for this mode.
	 *
	 * @return the template paths
	 */
	@Nonnull
	public List<Path> fsFilesToCopy() {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	/**
	 * Resolves the non-filesystem templates copied for this mode.
	 *
	 * @return the template paths
	 */
	@Nonnull
	public List<Path> nonFSFilesToCopy() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	/**
	 * Builds the placeholder values for non-filesystem templates.
	 *
	 * @return one placeholder array per configured template
	 */
	@Nonnull
	public List<String[]> placeholderValues() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { "de.tum.cit.ase", "de.tum.cit.ase", "Main" };
				default -> FileTools.generatePackageNameArray("de.tum.cit.ase", entry);
				}).toList();
	}

	/**
	 * Builds the placeholder values for filesystem templates.
	 *
	 * @param packageName            the supervised package name
	 * @param mainClassInPackageName the supervised main-class name
	 * @return one placeholder array per configured template
	 */
	@Nonnull
	public List<String[]> fsFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { packageName, packageName, mainClassInPackageName };
				default -> FileTools.generatePackageNameArray(packageName, entry);
				}).toList();
	}

	/**
	 * Builds the placeholder values for non-filesystem templates.
	 *
	 * @param packageName            the supervised package name
	 * @param mainClassInPackageName the supervised main-class name
	 * @return one placeholder array per configured template
	 */
	@Nonnull
	public List<String[]> nonFSFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { packageName, packageName, mainClassInPackageName };
				default -> FileTools.generatePackageNameArray(packageName, entry);
				}).toList();
	}

	/**
	 * Resolves the filesystem template destinations below a target directory.
	 *
	 * @param targetPath the target directory
	 * @return the destination paths
	 */
	@Nonnull
	public List<Path> fsTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}

	/**
	 * Resolves the non-filesystem template destinations below a target directory.
	 *
	 * @param targetPath the target directory
	 * @return the destination paths
	 */
	@Nonnull
	public List<Path> nonFSTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}
	// </editor-fold>

	// <editor-fold desc="Single-file methods">

	/**
	 * Retrieves the path to the file header template for the three-parted file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the file header template.
	 */
	@Nonnull
	public Path threePartedFileHeader() {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	/**
	 * Generates the body content for the three-parted file by concatenating the
	 * architecture test case definitions.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param testCases the list of test cases.
	 * @return a string representing the body content.
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public String threePartedFileBody(List<?> testCases) {
		return switch (this) {
		case ARCHUNIT -> String.join("\n",
				convertToJavaArchunitTestCases((List<JavaArchitectureTestCase>) testCases).stream()
						.map(javaArchunitTestCase -> javaArchunitTestCase.writeArchitectureTestCase("ARCHUNIT", ""))
						.toList()

			);
		case WALA -> String.join("\n", convertToJavaWalaTestCases((List<JavaArchitectureTestCase>) testCases).stream()
				.map(javaWalaTestCase -> javaWalaTestCase.writeArchitectureTestCase("WALA", "")).toList());
		};
	}

	/**
	 * Retrieves the path to the file footer template for the three-parted file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the file footer template.
	 */
	@Nonnull
	public Path threePartedFileFooter() {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(1).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	/**
	 * Generates the file value array based on the provided package name.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param packageName the base package name.
	 * @return an array of strings representing the file value.
	 */
	@Nonnull
	public String[] formatValues(@Nonnull String packageName) {
		return switch (this) {
		case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 3);
		case WALA -> FileTools.generatePackageNameArray(packageName, 3);
		};
	}

	/**
	 * Determines the target path for copying the main architecture test case file
	 * based on the project path and package name.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param targetPath the project path.
	 * @return the target path for the main architecture test case file.
	 */
	@Nonnull
	public Path targetToCopyTo(@Nonnull Path targetPath) {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList().get(0);
	}
	// </editor-fold>

	// <editor-fold desc="Reset methods">
	// (No reset methods defined for JavaArchitectureMode)
	// </editor-fold>

	// <editor-fold desc="Other methods">

	/**
	 * Imports and analyses Java classes from the specified class path using
	 * Archunit's ClassFileImporter. This method enables static code analysis by
	 * creating a collection of Java class metadata.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param classPath the file system path containing Java classes to import
	 * @return a JavaClasses object containing all imported classes for analysis
	 */
	@Nonnull
	public JavaClasses getJavaClasses(String classPath) {
		Path analysisPath = Path.of(classPath).toAbsolutePath().normalize();
		while (true) {
			String fingerprintBeforeImport = analysisInputFingerprint(analysisPath);
			String cacheKey = analysisPath + "\0" + fingerprintBeforeImport;
			FutureTask<JavaClasses> importTask;
			synchronized (JAVA_CLASSES_CACHE) {
				importTask = JAVA_CLASSES_CACHE.computeIfAbsent(cacheKey,
						unusedKey -> new FutureTask<>(() -> importJavaClasses(analysisPath)));
			}
			importTask.run();
			JavaClasses importedClasses = awaitJavaClasses(importTask, analysisPath);
			if (fingerprintBeforeImport.equals(analysisInputFingerprint(analysisPath))) {
				return importedClasses;
			}
			// Compilation changed the analysis input while it was being imported. Never
			// publish that mixed snapshot under either fingerprint; retry from a stable
			// bytecode state instead.
			synchronized (JAVA_CLASSES_CACHE) {
				JAVA_CLASSES_CACHE.remove(cacheKey, importTask);
			}
		}
	}

	private static JavaClasses importJavaClasses(Path analysisPath) {
		// Exclude Ares' own framework classes from analysis: the architecture rules
		// must only inspect the student/project code, never Ares' trusted advice (which
		// legitimately calls e.g. System.getProperty), otherwise the rules flag the
		// framework itself and every instrumented test fails with a self-interception.
		return new ClassFileImporter()
				.withImportOption(
						location -> !location.toString().replace("\\", "/").contains("/de/tum/cit/ase/ares/api/"))
				.importPath(analysisPath);
	}

	private static JavaClasses awaitJavaClasses(FutureTask<JavaClasses> importTask, Path analysisPath) {
		try {
			return importTask.get();
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new SecurityException(
					Messages.localized("security.architecture.mode.analysis.input.failure", analysisPath), interrupted);
		} catch (ExecutionException importFailure) {
			Throwable cause = importFailure.getCause();
			if (cause instanceof RuntimeException runtimeCause) {
				throw runtimeCause;
			}
			if (cause instanceof Error errorCause) {
				throw errorCause;
			}
			throw new SecurityException(
					Messages.localized("security.architecture.mode.analysis.input.failure", analysisPath), cause);
		}
	}

	private static String analysisInputFingerprint(Path analysisPath) {
		if (Files.notExists(analysisPath)) {
			return "missing";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			if (Files.isRegularFile(analysisPath)) {
				Path fileName = analysisPath.getFileName();
				updateAnalysisInputDigest(digest, fileName == null ? analysisPath : fileName, analysisPath);
			} else {
				try (Stream<Path> files = Files.walk(analysisPath)) {
					for (Path file : files.filter(Files::isRegularFile).filter(ArchitectureMode::isArchitectureInput)
							.sorted().toList()) {
						updateAnalysisInputDigest(digest, analysisPath.relativize(file), file);
					}
				}
			}
			return HexFormat.of().formatHex(digest.digest());
		} catch (IOException | NoSuchAlgorithmException fingerprintFailure) {
			throw new SecurityException(
					Messages.localized("security.architecture.mode.analysis.input.failure", analysisPath),
					fingerprintFailure);
		}
	}

	private static boolean isArchitectureInput(Path file) {
		Path fileNamePath = file.getFileName();
		if (fileNamePath == null) {
			return false;
		}
		String fileName = fileNamePath.toString();
		return fileName.endsWith(".class") || fileName.endsWith(".jar");
	}

	private static void updateAnalysisInputDigest(MessageDigest digest, Path relativePath, Path file)
			throws IOException {
		digest.update(relativePath.toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
		digest.update((byte) 0);
		digest.update(Files.readAllBytes(file));
		digest.update((byte) 0);
	}

	/**
	 * Builds a call graph based on the current architecture mode. Returns null for
	 * ARCHUNIT mode as it employs non-call-graph analysis, while in WALA mode it
	 * constructs a proper call graph representing method caller-callee
	 * relationships.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param classPath the file system path containing Java classes to analyse
	 * @return a CallGraph object for WALA mode, or null for ARCHUNIT mode
	 */
	@Nullable
	public CallGraph getCallGraph(String classPath) {
		return switch (this) {
		case ARCHUNIT -> null;
		case WALA -> new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);
		};
	}
	// </editor-fold>

	// <editor-fold desc="Static methods">
	private static JavaArchunitTestCase convertToJavaArchunitTestCase(JavaArchitectureTestCase testCase) {
		return JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(
						(JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
				.allowedPackages(testCase.getAllowedPackages()).javaClasses(testCase.getJavaClasses()).build();
	}

	private static JavaWalaTestCase convertToJavaWalaTestCases(JavaArchitectureTestCase testCase) {
		// Prefer the lazy supplier path when available so the WALA outcome cache can
		// short-circuit rule checks without ever building the call graph in JVM
		// forks whose results are entirely served from disk.
		java.util.function.Supplier<com.ibm.wala.ipa.callgraph.CallGraph> supplier = testCase.getCallGraphSupplier();
		if (supplier != null) {
			return new JavaWalaTestCase((JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported(),
					testCase.getAllowedPackages(), testCase.getJavaClasses(), supplier);
		}
		return JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported(
						(JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
				.allowedPackages(testCase.getAllowedPackages()).callGraph(testCase.getCallGraph())
				.javaClasses(testCase.getJavaClasses()).build();
	}

	private static List<JavaArchunitTestCase> convertToJavaArchunitTestCases(List<JavaArchitectureTestCase> testCases) {
		return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaArchunitTestCase).toList());
	}

	private static List<JavaWalaTestCase> convertToJavaWalaTestCases(List<JavaArchitectureTestCase> testCases) {
		return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaWalaTestCases).toList());
	}
	// </editor-fold>
}
