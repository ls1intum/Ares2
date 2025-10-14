package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavaCSVFileLoader implements JavaFileLoader {


    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected architecture mode.
     *
     * @param mode the selected architecture mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public File getCopyPaths(ArchitectureMode mode, boolean formatStringFileRequested) {
        if(formatStringFileRequested) {
            return switch (mode) {
                case ARCHUNIT ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","ArchunitTemplateCopyFiles.csv"));
                case WALA ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","WalaTemplateCopyFiles.csv"));
            };
        } else {
            return switch (mode) {
                case ARCHUNIT ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","ArchunitJavaCopyFiles.csv"));
                case WALA ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","WalaJavaCopyFiles.csv"));
            };
        }
    }

    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public File getCopyPaths(AOPMode mode, boolean formatStringFileRequested) {
        if(formatStringFileRequested) {
            return switch (mode) {
                case INSTRUMENTATION ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","InstrumentationTemplateCopyFiles.csv"));
                case ASPECTJ ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","AspectJTemplateCopyFiles.csv"));
            };
        } else {
            return switch (mode) {
                case INSTRUMENTATION ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","InstrumentationJavaCopyFiles.csv"));
                case ASPECTJ ->
                        FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","copyFiles","java","AspectJAJCopyFiles.csv"));
            };
        }
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected architecture mode.
     *
     * @param mode the selected architecture mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public File getEditPaths(ArchitectureMode mode) {
        return switch (mode) {
            case ARCHUNIT ->
                FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","editFiles","java","ArchunitEditFiles.csv"));

            case WALA ->
                FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","editFiles","java","WalaEditFiles.csv"));

        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public File getEditPaths(AOPMode mode) {
        return switch (mode) {
            case INSTRUMENTATION ->
                FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","editFiles","java","InstrumentationEditFiles.csv"));

            case ASPECTJ ->
                FileTools.readFile(FileTools.resolveFileOnSourceDirectory("configuration","editFiles","java","AspectJEditFiles.csv"));
        };
    }

    /**
     * Loads the copy configuration from the CSV file for the selected architecture mode.
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
     * Loads the edit configuration from the CSV file for the selected architecture mode.
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
}
