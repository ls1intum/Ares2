package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaCSVFileLoader implements JavaFileLoader {

    /**
     * Retrieves the path to the CSV file containing the copy configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the copy configuration.
     */
    public Path getCopyPaths(JavaAOPMode mode) {
        return switch (mode) {
            case INSTRUMENTATION -> FileTools.resolveOnResources("configuration/InstrumentationCopyFiles.csv");
            case ASPECTJ -> FileTools.resolveOnResources("configuration/AspectJCopyFiles.csv");
        };
    }

    /**
     * Retrieves the path to the CSV file containing the edit configuration for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the path to the CSV file containing the edit configuration.
     */
    public Path getEditPaths(JavaAOPMode mode) {
        return FileTools.resolveOnResources("EditFiles.csv");
    }

    /**
     * Loads the copy configuration from the CSV file for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the copy configuration.
     */
    @Override
    public List<List<String>> loadCopyData(JavaAOPMode mode) throws IOException {
        return FileTools.readCSVFile(getCopyPaths(mode));
    }

    /**
     * Loads the edit configuration from the CSV file for the selected AOP mode.
     *
     * @param mode the selected AOP mode.
     * @return the edit configuration.
     */
    @Override
    public List<List<String>> loadEditData(JavaAOPMode mode) throws IOException {
        return FileTools.readCSVFile(getEditPaths(mode));
    }
}
