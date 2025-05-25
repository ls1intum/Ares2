package de.tum.cit.ase.ares.api.aop.java.aopModeData;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.AOPMode;

import java.io.IOException;
import java.util.List;

public interface JavaFileLoader {
    List<List<String>> loadCopyData(AOPMode mode) throws IOException, CsvException;
    List<List<String>> loadEditData(AOPMode mode) throws IOException, CsvException;
}
