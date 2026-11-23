package de.tum.cit.ase.ares.api.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.opencsv.exceptions.CsvException;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.util.FileTools;

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

	/**
	 * Checks that a valid non-filesystem row comes back unchanged and that a row
	 * with a blank count is refused.
	 */
	@Test
	void validatesTheNonFsCopyRows() throws Exception {
		List<List<String>> valid = List.of(List.of("source", "3", "target"));
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.WALA, false)).thenReturn(valid);
		ArchitectureMode.setFileLoader(loader);

		assertThat(ArchitectureMode.WALA.getCopyNonFSConfigurationEntries()).isEqualTo(valid);

		when(loader.loadCopyData(ArchitectureMode.WALA, false)).thenReturn(List.of(List.of("source", " ", "target")));
		assertThatThrownBy(ArchitectureMode.WALA::getCopyNonFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessage(Messages
						.localized("security.architecture.mode.configuration.blank.cell", "copy.nonfs", 0, 1, "WALA"));
	}

	/**
	 * Checks that a blank or missing first, middle or last cell is refused with the
	 * column it was found in.
	 */
	@Test
	void refusesABlankFirstOrLastCell() throws Exception {
		assertBlankCell(List.of(List.of("", "1", "t")), 0);
		assertBlankCell(List.of(List.of("s", "1", " ")), 2);
		assertBlankCell(List.of(Arrays.asList("s", null, "t")), 1);
		assertBlankCell(List.of(Arrays.asList(null, "1", "t")), 0);
		assertBlankCell(List.of(Arrays.asList("s", "1", null)), 2);
	}

	/**
	 * Checks that the counts 0 and 100, the two ends of the allowed range, are
	 * accepted.
	 */
	@Test
	void acceptsTheCountBounds() throws Exception {
		List<List<String>> rows = List.of(List.of("source", "0", "target"), List.of("source", "100", "target"));
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(rows);
		ArchitectureMode.setFileLoader(loader);

		assertThat(ArchitectureMode.ARCHUNIT.getCopyFSConfigurationEntries()).isEqualTo(rows);
	}

	/**
	 * Checks that a missing table is refused as empty and that a load failure of
	 * the filesystem copy table and of the edit table keeps its cause.
	 */
	@Test
	void refusesAMissingTableAndWrapsLoadFailures() throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		ArchitectureMode.setFileLoader(loader);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(null);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getCopyFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessage(
						Messages.localized("security.architecture.mode.configuration.empty", "copy.fs", "ARCHUNIT"));

		CsvException csvFailure = new CsvException("broken");
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenThrow(csvFailure);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getCopyFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessage(Messages
						.localized("security.architecture.mode.configuration.load.failure", "copy.fs", "ARCHUNIT"))
				.hasCause(csvFailure);

		IOException ioFailure = new IOException("unreadable");
		when(loader.loadEditData(ArchitectureMode.ARCHUNIT)).thenThrow(ioFailure);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getEditConfigurationEntries).isInstanceOf(SecurityException.class)
				.hasMessage(
						Messages.localized("security.architecture.mode.configuration.load.failure", "edit", "ARCHUNIT"))
				.hasCause(ioFailure);
	}

	/**
	 * Checks that the template paths and the placeholder values follow the rows of
	 * the table: count 0 repeats the package and adds the main class, any other
	 * count gives that many copies of the package.
	 */
	@Test
	void resolvesTheFilesAndValuesOfEachRow() throws Exception {
		List<List<String>> rows = List.of(List.of("templates/a.txt", "0", "x/b.txt"),
				List.of("templates/c.txt", "2", "y"));
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(rows);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, false)).thenReturn(rows);
		ArchitectureMode.setFileLoader(loader);
		List<Path> expectedFiles = List.of(FileTools.resolveFileOnSourceDirectory("templates", "a.txt"),
				FileTools.resolveFileOnSourceDirectory("templates", "c.txt"));

		assertThat(ArchitectureMode.ARCHUNIT.fsFilesToCopy()).isEqualTo(expectedFiles);
		assertThat(ArchitectureMode.ARCHUNIT.nonFSFilesToCopy()).isEqualTo(expectedFiles);
		for (List<String[]> values : List.of(ArchitectureMode.ARCHUNIT.fsFormatValues("p.q", "Main"),
				ArchitectureMode.ARCHUNIT.nonFSFormatValues("p.q", "Main"))) {
			assertThat(values).hasSize(2);
			assertThat(values.get(0)).containsExactly("p.q", "p.q", "Main");
			assertThat(values.get(1)).containsExactly(FileTools.generatePackageNameArray("p.q", 2));
		}
		List<String[]> placeholders = ArchitectureMode.ARCHUNIT.placeholderValues();
		assertThat(placeholders).hasSize(2);
		assertThat(placeholders.get(0)).containsExactly("de.tum.cit.ase", "de.tum.cit.ase", "Main");
		assertThat(placeholders.get(1)).containsExactly(FileTools.generatePackageNameArray("de.tum.cit.ase", 2));
	}

	/**
	 * Checks that ArchUnit gets three copies of the package and WALA four.
	 */
	@Test
	void formatsThePackageForEachMode() {
		assertThat(ArchitectureMode.ARCHUNIT.formatValues("p.q"))
				.containsExactly(FileTools.generatePackageNameArray("p.q", 3));
		assertThat(ArchitectureMode.WALA.formatValues("p.q"))
				.containsExactly(FileTools.generatePackageNameArray("p.q", 4));
	}

	/**
	 * Checks that a test case with a call graph and no supplier is written as the
	 * same WALA test case built directly.
	 */
	@Test
	void writesAWalaBodyFromAnEagerTestCase() {
		JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(Set.of()).javaClasses(new ClassFileImporter().importClasses())
				.callGraph(mock(CallGraph.class)).supervisedPackage("student.pkg").build();
		String expected = JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(Set.of()).javaClasses(new ClassFileImporter().importClasses())
				.callGraph(mock(CallGraph.class)).supervisedPackage("student.pkg").build()
				.writeArchitectureTestCase("WALA", "");

		assertThat(ArchitectureMode.WALA.threePartedFileBody(List.of(testCase))).isEqualTo(expected);
	}

	/**
	 * Makes the loader return the given copy rows and checks that they are refused
	 * as a blank cell in the given column of row 0.
	 */
	private static void assertBlankCell(List<List<String>> rows, int column) throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(rows);
		ArchitectureMode.setFileLoader(loader);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getCopyFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessage(Messages.localized(
						"security.architecture.mode.configuration.blank.cell", "copy.fs", 0, column, "ARCHUNIT"));
	}

	private static void assertInvalidCopyRows(List<List<String>> rows, String expectedMessage) throws Exception {
		JavaFileLoader loader = mock(JavaFileLoader.class);
		when(loader.loadCopyData(ArchitectureMode.ARCHUNIT, true)).thenReturn(rows);
		ArchitectureMode.setFileLoader(loader);
		assertThatThrownBy(ArchitectureMode.ARCHUNIT::getCopyFSConfigurationEntries)
				.isInstanceOf(SecurityException.class).hasMessageContaining(expectedMessage);
	}
}
