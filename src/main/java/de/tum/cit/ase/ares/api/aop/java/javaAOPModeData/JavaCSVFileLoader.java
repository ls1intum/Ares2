package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.cit.ase.ares.api.aop.java.aopModeData.JavaFileLoader;

public class JavaCSVFileLoader implements JavaFileLoader{

    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public File getCopyPaths(AOPMode mode) throws IOException {
        return switch (mode) {
            case INSTRUMENTATION -> FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/InstrumentationCopyFiles.csv");
            case ASPECTJ -> FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/copyFiles/java/AspectJCopyFiles.csv");
        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public File getEditPaths(AOPMode mode) throws IOException {
        return FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/configuration/editFiles/java/EditFiles.csv");
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
