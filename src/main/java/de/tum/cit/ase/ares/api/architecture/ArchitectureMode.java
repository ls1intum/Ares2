package de.tum.cit.ase.ares.api.architecture;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.opencsv.exceptions.CsvException;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Enum representing the architecture modes for Java security test cases.
 *
 * <p>
 * Description: Provides different modes for architecture test case generation
 * and execution in Java. The modes determine how files and settings are copied
 * and resolved based on the underlying architecture analysis tool.
 * </p>
 *
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

	// <editor-fold desc="Load configuration">
	public List<List<String>> getCopyFSConfigurationEntries() {
		try {
			return (new JavaCSVFileLoader()).loadCopyData(this, true);
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
	}

	public List<List<String>> getCopyNonFSConfigurationEntries() {
		try {
			return (new JavaCSVFileLoader()).loadCopyData(this, false);
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
	}

	public List<List<String>> getEditConfigurationEntries() {
		try {
			return (new JavaCSVFileLoader()).loadEditData(this);
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Multi-file methods">

	@Nonnull
	public List<Path> fsFilesToCopy() {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/")).map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	@Nonnull
	public List<Path> nonFSFilesToCopy() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/")).map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	@Nonnull
	public List<String[]> placeholderValues() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt).map(entry -> switch (entry) {
			case 0 -> new String[]{ "de.tum.cit.ase", "de.tum.cit.ase", "Main" };
			default -> FileTools.generatePackageNameArray("de.tum.cit.ase", entry);
		}).toList();
	}

	@Nonnull
	public List<String[]> fsFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt).map(entry -> switch (entry) {
			case 0 -> new String[]{ packageName, packageName, mainClassInPackageName };
			default -> FileTools.generatePackageNameArray(packageName, entry);
		}).toList();
	}

	@Nonnull
	public List<String[]> nonFSFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt).map(entry -> switch (entry) {
			case 0 -> new String[]{ packageName, packageName, mainClassInPackageName };
			default -> FileTools.generatePackageNameArray(packageName, entry);
		}).toList();
	}

	@Nonnull
	public List<Path> fsTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/")).map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}

	@Nonnull
	public List<Path> nonFSTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/")).map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
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
		return getEditConfigurationEntries().stream().map(entry -> entry.get(0).split("/")).map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
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
			case ARCHUNIT -> String.join("\n", convertToJavaArchunitTestCases((List<JavaArchitectureTestCase>) testCases).stream()
					.map(javaArchunitTestCase -> javaArchunitTestCase.writeArchitectureTestCase("ARCHUNIT", "")).toList()

				);
			case WALA -> String.join("\n",
					convertToJavaWalaTestCases((List<JavaArchitectureTestCase>) testCases).stream().map(javaWalaTestCase -> javaWalaTestCase.writeArchitectureTestCase("WALA", "")).toList());
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
		return getEditConfigurationEntries().stream().map(entry -> entry.get(1).split("/")).map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
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
		return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/")).map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList().get(0);
	}
	// </editor-fold>

	// <editor-fold desc="Reset methods">
	// (No reset methods defined for JavaArchitectureMode)
	// </editor-fold>

	// <editor-fold desc="Other methods">

	/**
	 * Imports and analyzes Java classes from the specified class path using
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
		return new ClassFileImporter().importPath(classPath);
	}

	/**
	 * Builds a call graph based on the current architecture mode. Returns null for
	 * ARCHUNIT mode as it employs non-call-graph analysis, while in WALA mode it
	 * constructs a proper call graph representing method caller-callee
	 * relationships.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param classPath the file system path containing Java classes to analyze
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
		return JavaArchunitTestCase.archunitBuilder().javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
				.allowedPackages(testCase.getAllowedPackages()).javaClasses(testCase.getJavaClasses()).build();
	}

	private static JavaWalaTestCase convertToJavaWalaTestCases(JavaArchitectureTestCase testCase) {
		return JavaWalaTestCase.walaBuilder().javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
				.allowedPackages(testCase.getAllowedPackages()).callGraph(testCase.getCallGraph()).javaClasses(testCase.getJavaClasses()).build();
	}

	private static List<JavaArchunitTestCase> convertToJavaArchunitTestCases(List<JavaArchitectureTestCase> testCases) {
		return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaArchunitTestCase).toList());
	}

	private static List<JavaWalaTestCase> convertToJavaWalaTestCases(List<JavaArchitectureTestCase> testCases) {
		return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaWalaTestCases).toList());
	}
	// </editor-fold>
}