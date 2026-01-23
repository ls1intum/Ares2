package de.tum.cit.ase.ares.api.localization;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.util.FileTools;

public class Localisation {

	private Localisation() {
		throw new IllegalStateException("Utility class should not be instantiated");
	}

	public static List<Path> filesToCopy() {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	public static List<List<String>> getCopyConfigurationEntries() {
		try {
			return (new JavaCSVFileLoader()).loadLocalisationCopyData();
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Path> targetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}
}
