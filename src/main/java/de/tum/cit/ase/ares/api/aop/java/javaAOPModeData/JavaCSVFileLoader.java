package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaCSVFileLoader implements JavaFileLoader {


    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected architecture mode.
     *
     * @param mode the selected architecture mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public File getCopyPaths(ArchitectureMode mode) throws IOException {
        return switch (mode) {
            case ARCHUNIT ->
                    FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/ArchunitCopyFiles.csv");
            case WALA ->
                    FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/WalaCopyFiles.csv");
        };
    }

    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public File getCopyPaths(AOPMode mode) throws IOException {
        return switch (mode) {
            case INSTRUMENTATION ->
                    FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/InstrumentationCopyFiles.csv");
            case ASPECTJ ->
                    FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/AspectJCopyFiles.csv");
        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected architecture mode.
     *
     * @param mode the selected architecture mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public File getEditPaths(ArchitectureMode mode) throws IOException {
        return switch (mode) {
            case ARCHUNIT ->
                FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/editFiles/java/ArchunitEditFiles.csv");

            case WALA ->
                FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/editFiles/java/WalaEditFiles.csv");

        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public File getEditPaths(AOPMode mode) throws IOException {
        return switch (mode) {
            case INSTRUMENTATION ->
                FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/editFiles/java/InstrumentationEditFiles.csv");

            case ASPECTJ ->
                FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/editFiles/java/AspectJEditFiles.csv");
        };
    }

    /**
     * Loads the copy configuration from the CSV file for the selected architecture mode.
     *
     * @param mode the selected architecture mode.
     * @return the copy configuration.
     */
    @Override
    public List<List<String>> loadCopyData(ArchitectureMode mode) throws IOException, CsvException {
        return FileTools.readCSVFile(getCopyPaths(mode));
    }


    private Path getPhobosCopyPaths() {
        return FileTools.resolveOnPackage("configuration/PhobosCopyFiles.csv");
    }
    private Path getPhobosEditPaths() {
        return FileTools.resolveOnPackage("configuration/PhobosEditFiles.csv");
    }

    /**
     * Loads the copy configuration from the CSV file for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the copy configuration.
     */
    @Override
    public List<List<String>> loadCopyData(AOPMode mode) throws IOException, CsvException {
        return FileTools.readCSVFile(getCopyPaths(mode));
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

    @Override
    public List<List<String>> loadCopyData() throws IOException, CsvException {
        return FileTools.readCSVFile(getPhobosCopyPaths().toFile());
    }

    @Override
    public List<List<String>> loadEditData() throws IOException, CsvException {
        return FileTools.readCSVFile(getPhobosEditPaths().toFile());
    }

}
