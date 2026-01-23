package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.util.FileTools;

public class JavaCSVFileLoader implements JavaFileLoader {

	// <editor-fold desc="getCopyPaths">

	/**
	 * Retrieves the path to the CSV file containing the copy configuration for the
	 * selected architecture mode.
	 *
	 * @param mode the selected architecture mode.
	 * @return the path to the CSV file containing the copy configuration.
	 */
	public File getCopyPaths(ArchitectureMode mode, boolean formatStringFileRequested) {
		if (formatStringFileRequested) {
			return switch (mode) {
			case ARCHUNIT -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles",
					"java", "ArchunitTemplateCopyFiles.csv"));
			case WALA -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles", "java",
					"WalaTemplateCopyFiles.csv"));
			};
		} else {
			return switch (mode) {
			case ARCHUNIT -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles",
					"java", "ArchunitJavaCopyFiles.csv"));
			case WALA -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles", "java",
					"WalaJavaCopyFiles.csv"));
			};
		}
	}

	/**
	 * Retrieves the path to the CSV file containing the copy configuration for the
	 * selected AOP mode.
	 *
	 * @param mode the selected AOP mode.
	 * @return the path to the CSV file containing the copy configuration.
	 */
	public File getCopyPaths(AOPMode mode, boolean formatStringFileRequested) {
		if (formatStringFileRequested) {
			return switch (mode) {
			case INSTRUMENTATION -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration",
					"copyFiles", "java", "InstrumentationTemplateCopyFiles.csv"));
			case ASPECTJ -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles",
					"java", "AspectJTemplateCopyFiles.csv"));
			};
		} else {
			return switch (mode) {
			case INSTRUMENTATION -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration",
					"copyFiles", "java", "InstrumentationJavaCopyFiles.csv"));
			case ASPECTJ -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles",
					"java", "AspectJAJCopyFiles.csv"));
			};
		}
	}

	/**
	 * Retrieves the path to the CSV file containing the copy configuration for
	 * Localisation.
	 *
	 * @return the path to the CSV file containing the copy configuration.
	 */
	public File getLocalisationCopyPaths() {
		return FileTools.readFile(
				FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles", "LocalisationCopyFiles.csv"));
	}

	/**
	 * Retrieves the path to the CSV file containing the copy configuration for
	 * Phobos.
	 *
	 * @return the path to the CSV file containing the copy configuration.
	 */
	private File getPhobosCopyPaths() {
		return FileTools
				.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "copyFiles", "PhobosCopyFiles.csv"));
	}
	// </editor-fold>

	// <editor-fold desc="getEditPaths">

	/**
	 * Retrieves the path to the CSV file containing the edit configuration for the
	 * selected architecture mode.
	 *
	 * @param mode the selected architecture mode.
	 * @return the path to the CSV file containing the edit configuration.
	 */
	public File getEditPaths(ArchitectureMode mode) {
		return switch (mode) {
		case ARCHUNIT -> FileTools.readFile(
				FileTools.resolveFileOnSourceDirectory("configuration", "editFiles", "java", "ArchunitEditFiles.csv"));

		case WALA -> FileTools.readFile(
				FileTools.resolveFileOnSourceDirectory("configuration", "editFiles", "java", "WalaEditFiles.csv"));
		};
	}

	/**
	 * Retrieves the path to the CSV file containing the edit configuration for the
	 * selected AOP mode.
	 *
	 * @param mode the selected AOP mode.
	 * @return the path to the CSV file containing the edit configuration.
	 */
	public File getEditPaths(AOPMode mode) {
		return switch (mode) {
		case INSTRUMENTATION -> FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "editFiles",
				"java", "InstrumentationEditFiles.csv"));

		case ASPECTJ -> FileTools.readFile(
				FileTools.resolveFileOnSourceDirectory("configuration", "editFiles", "java", "AspectJEditFiles.csv"));
		};
	}

	/**
	 * Retrieves the path to the CSV file containing the edit configuration for
	 * Phobos.
	 *
	 * @return the path to the CSV file containing the edit configuration.
	 */
	private File getPhobosEditPaths() {
		return FileTools
				.readFile(FileTools.resolveFileOnSourceDirectory("configuration", "editFiles", "PhobosEditFiles.csv"));
	}
	// </editor-fold>

	// <editor-fold desc="loadCopyData">

	/**
	 * Loads the copy configuration from the CSV file for the selected architecture
	 * mode.
	 *
	 * @param mode the selected architecture mode.
	 * @return the copy configuration.
	 */
	@Override
	public List<List<String>> loadCopyData(ArchitectureMode mode, boolean fs) throws IOException, CsvException {
		return FileTools.readCSVFile(getCopyPaths(mode, fs));
	}

	/**
	 * Loads the copy configuration from the CSV file for the selected AOP mode.
	 *
	 * @param mode the selected AOP mode.
	 * @return the copy configuration.
	 */
	@Override
	public List<List<String>> loadCopyData(AOPMode mode, boolean fs) throws IOException, CsvException {
		return FileTools.readCSVFile(getCopyPaths(mode, fs));
	}

	/**
	 * Loads the copy configuration from the CSV file for Localisation.
	 *
	 * @return the copy configuration.
	 * @throws IOException  if an I/O error occurs
	 * @throws CsvException if a CSV parsing error occurs
	 */
	public List<List<String>> loadLocalisationCopyData() throws IOException, CsvException {
		return FileTools.readCSVFile(getLocalisationCopyPaths());
	}

	/**
	 * Loads the copy configuration from the CSV file for Phobos.
	 *
	 * @return the copy configuration.
	 */
	public List<List<String>> loadPhobosCopyData() throws IOException, CsvException {
		return FileTools.readCSVFile(getPhobosCopyPaths());
	}
	// </editor-fold>

	// <editor-fold desc="loadEditData">

	/**
	 * Loads the edit configuration from the CSV file for the selected architecture
	 * mode.
	 *
	 * @param mode the selected architecture mode.
	 * @return the edit configuration.
	 */
	@Override
	public List<List<String>> loadEditData(ArchitectureMode mode) throws IOException, CsvException {
		return FileTools.readCSVFile(getEditPaths(mode));
	}

	/**
	 * Loads the edit configuration from the CSV file for the selected AOP mode.
	 *
	 * @param mode the selected AOP mode.
	 * @return the edit configuration.
	 */
	@Override
	public List<List<String>> loadEditData(AOPMode mode) throws IOException, CsvException {
		return FileTools.readCSVFile(getEditPaths(mode));
	}

	/**
	 * Loads the edit configuration from the CSV file for Phobos.
	 *
	 * @return the edit configuration.
	 */
	public List<List<String>> loadPhobosEditData() throws IOException, CsvException {
		return FileTools.readCSVFile(getPhobosEditPaths());
	}
	// </editor-fold>
}
