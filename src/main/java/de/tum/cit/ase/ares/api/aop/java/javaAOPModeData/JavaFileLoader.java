package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;

import java.io.IOException;
import java.util.List;

public interface JavaFileLoader {
    List<List<String>> loadCopyData(ArchitectureMode mode) throws IOException, CsvException;

    List<List<String>> loadCopyData(AOPMode mode) throws IOException, CsvException;

    List<List<String>> loadEditData(ArchitectureMode mode) throws IOException, CsvException;

    List<List<String>> loadEditData(AOPMode mode) throws IOException, CsvException;
}
