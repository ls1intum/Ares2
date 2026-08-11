package de.tum.cit.ase.ares.api.localization;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;
import de.tum.cit.ase.ares.api.util.FileTools;

public final class Localisation {
	private static JavaFileLoader fileLoader = new JavaCSVFileLoader();

	private Localisation() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "Localisation"));
	}

	public static List<Path> filesToCopy() {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	public static List<List<String>> getCopyConfigurationEntries() {
		try {
			return fileLoader.loadLocalisationCopyData();
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setFileLoader(JavaFileLoader loader) {
		fileLoader = java.util.Objects.requireNonNull(loader, "loader must not be null");
	}

	public static List<Path> targetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}
}
