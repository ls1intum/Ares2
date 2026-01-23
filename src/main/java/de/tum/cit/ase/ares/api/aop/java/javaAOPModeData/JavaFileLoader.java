package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;

public interface JavaFileLoader {
	List<List<String>> loadCopyData(ArchitectureMode mode, boolean formatStringFileRequested) throws IOException, CsvException;

	List<List<String>> loadCopyData(AOPMode mode, boolean formatStringFileRequested) throws IOException, CsvException;

	List<List<String>> loadLocalisationCopyData() throws IOException, CsvException;

	List<List<String>> loadPhobosCopyData() throws IOException, CsvException;

	List<List<String>> loadEditData(ArchitectureMode mode) throws IOException, CsvException;

	List<List<String>> loadEditData(AOPMode mode) throws IOException, CsvException;

	List<List<String>> loadPhobosEditData() throws IOException, CsvException;
}
