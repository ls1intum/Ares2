package de.tum.cit.ase.ares.api.aop.java.aopModeData;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaCSVFileLoader implements JavaFileLoader {

    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public Path getCopyPaths(AOPMode mode) {
        return switch (mode) {
            case INSTRUMENTATION -> FileTools.resolveOnPackage("configuration/InstrumentationCopyFiles.csv");
            case ASPECTJ -> FileTools.resolveOnPackage("configuration/AspectJCopyFiles.csv");
        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public Path getEditPaths(AOPMode mode) {
        return FileTools.resolveOnPackage("EditFiles.csv");
    }

    /**
     * Loads the copy configuration from the CSV file for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the copy configuration.
     */
    @Override
    public List<List<String>> loadCopyData(AOPMode mode) throws IOException {
        return FileTools.readCSVFile(getCopyPaths(mode));
    }

    /**
     * Loads the edit configuration from the CSV file for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the edit configuration.
     */
    @Override
    public List<List<String>> loadEditData(AOPMode mode) throws IOException {
        return FileTools.readCSVFile(getEditPaths(mode));
    }
}
