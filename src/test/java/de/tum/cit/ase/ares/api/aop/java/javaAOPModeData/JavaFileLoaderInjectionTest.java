package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.localization.Localisation;
import de.tum.cit.ase.ares.api.phobos.Phobos;

class JavaFileLoaderInjectionTest {
	@Test
	void everyProductionConsumerUsesTheInjectedInterface() throws Exception {
		JavaFileLoader memory = new JavaFileLoader() {
			@Override
			public List<List<String>> loadCopyData(ArchitectureMode mode, boolean formatStringFileRequested) {
				return List.of(List.of("architecture-source", "1", "architecture-target"));
			}

			@Override
			public List<List<String>> loadCopyData(AOPMode mode, boolean formatStringFileRequested) {
				return List.of(List.of("aop-source", "1", "aop-target"));
			}

			@Override
			public List<List<String>> loadLocalisationCopyData() {
				return List.of(List.of("locale-source", "0", "locale-target"));
			}

			@Override
			public List<List<String>> loadPhobosCopyData() {
				return List.of(List.of("phobos-source", "0", "phobos-target"));
			}

			@Override
			public List<List<String>> loadEditData(ArchitectureMode mode) {
				return List.of(List.of("architecture-edit", "1", "architecture-output"));
			}

			@Override
			public List<List<String>> loadEditData(AOPMode mode) {
				return List.of(List.of("aop-edit", "1", "aop-output"));
			}

			@Override
			public List<List<String>> loadPhobosEditData() {
				return List.of(List.of("phobos-edit", "0", "phobos-output"));
			}
		};
		AOPMode.setFileLoader(memory);
		ArchitectureMode.setFileLoader(memory);
		Localisation.setFileLoader(memory);
		Phobos.setFileLoader(memory);
		try {
			assertEquals("aop-source", AOPMode.ASPECTJ.getCopyFSConfigurationEntries().get(0).get(0));
			assertEquals("architecture-source",
					ArchitectureMode.ARCHUNIT.getCopyFSConfigurationEntries().get(0).get(0));
			assertEquals("locale-source", Localisation.getCopyConfigurationEntries().get(0).get(0));
			assertEquals("phobos-source", Phobos.getCopyConfigurationEntries().get(0).get(0));
			assertEquals("phobos-edit", Phobos.getEditConfigurationEntries().get(0).get(0));
		} finally {
			JavaCSVFileLoader csv = new JavaCSVFileLoader();
			AOPMode.setFileLoader(csv);
			ArchitectureMode.setFileLoader(csv);
			Localisation.setFileLoader(csv);
			Phobos.setFileLoader(csv);
		}
	}
}
