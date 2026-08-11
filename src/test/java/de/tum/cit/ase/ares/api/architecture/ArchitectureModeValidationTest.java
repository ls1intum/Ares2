package de.tum.cit.ase.ares.api.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;

class ArchitectureModeValidationTest {

	@AfterEach
	void restoreLoader() {
		ArchitectureMode.setFileLoader(new JavaCSVFileLoader());
	}

	@Test
	void acceptsWellFormedCopyRows() throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true))
				.thenReturn(List.of(List.of("source", "3", "target")));
		ArchitectureMode.setFileLoader(loader);

		assertThat(ArchitectureMode.ARCHUNIT.getCopyFSConfigurationEntries())
				.containsExactly(List.of("source", "3", "target"));
	}

	@Test
	void rejectsShortNullAndBlankRows() throws Exception {
		assertInvalidCopyRows(List.of(List.of("source", "1")), "copy.fs");
		assertInvalidCopyRows(Collections.singletonList(null), "copy.fs");
		assertInvalidCopyRows(List.of(List.of("source", " ", "target")), "copy.fs");
	}

	@Test
	void rejectsNonNumericNegativeAndExcessiveCounts() throws Exception {
		assertInvalidCopyRows(List.of(List.of("source", "many", "target")), "many");
		assertInvalidCopyRows(List.of(List.of("source", "-1", "target")), "-1");
		assertInvalidCopyRows(List.of(List.of("source", "101", "target")), "101");
	}

	@Test
	void rejectsEmptyEditConfiguration() throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadEditData(ArchitectureMode.WALA)).thenReturn(List.of());
		ArchitectureMode.setFileLoader(loader);

		assertThatThrownBy(ArchitectureMode.WALA::getEditConfigurationEntries).isInstanceOf(SecurityException.class)
				.hasMessageContaining("edit").hasMessageContaining("WALA");
	}

	@Test
	void wrapsLoaderFailuresAsSecurityErrors() throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.WALA, false)).thenThrow(new IOException("unreadable"));
		ArchitectureMode.setFileLoader(loader);

		assertThatThrownBy(ArchitectureMode.WALA::getCopyNonFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessageContaining("copy.nonfs").hasMessageContaining("WALA")
				.hasCauseInstanceOf(IOException.class);
	}

	private static void assertInvalidCopyRows(List<List<String>> rows, String expectedMessage) throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(rows);
		ArchitectureMode.setFileLoader(loader);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getCopyFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessageContaining(expectedMessage);
	}
}
