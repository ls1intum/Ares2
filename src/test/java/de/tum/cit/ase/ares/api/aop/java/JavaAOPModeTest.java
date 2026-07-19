package de.tum.cit.ase.ares.api.aop.java;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;
import de.tum.cit.ase.ares.api.util.FileTools;

public class JavaAOPModeTest {

	public static final List<List<String>> COPY_ENTRIES = List.of(List.of("pkg1/pkg2", "1", "destPkg"),
			List.of("foo/bar", "0", "anotherDest"));

	public static final List<List<String>> EDIT_ENTRIES = List.of(List.of("hdr/pkg", "ftr/pkg", "unused"));

	public static final Path DUMMY_PATH = Mockito.mock(Path.class);

	public static final String[] GENERATED_ARRAY = new String[] { "a", "b", "c" };

	public static final String BODY_RESULT = "SECURITY_BODY";

	/**
	 * The real {@link JavaFileLoader} in place before each test, restored
	 * afterwards so mocking it in one test cannot leak into another test sharing
	 * the same Surefire fork (see {@code reuseForks} in {@code pom.xml}).
	 */
	private JavaFileLoader originalFileLoader;

	@BeforeEach
	public void captureOriginalFileLoader() throws Exception {
		Field fileLoaderField = AOPMode.class.getDeclaredField("fileLoader");
		fileLoaderField.setAccessible(true);
		originalFileLoader = (JavaFileLoader) fileLoaderField.get(null);
		fileLoaderField.setAccessible(false);
	}

	@AfterEach
	public void restoreOriginalFileLoader() {
		AOPMode.setFileLoader(originalFileLoader);
	}

	@Test
	public void testGetConfigurationEntries() throws Exception {
		JavaFileLoader loaderMock = Mockito.mock(JavaFileLoader.class);
		Mockito.when(loaderMock.loadCopyData(ArgumentMatchers.any(AOPMode.class), ArgumentMatchers.eq(true)))
				.thenReturn(COPY_ENTRIES);
		Mockito.when(loaderMock.loadCopyData(ArgumentMatchers.any(AOPMode.class), ArgumentMatchers.eq(false)))
				.thenReturn(COPY_ENTRIES);
		Mockito.when(loaderMock.loadEditData(ArgumentMatchers.any(AOPMode.class))).thenReturn(EDIT_ENTRIES);
		AOPMode.setFileLoader(loaderMock);

		List<List<String>> fsCopyEntries = AOPMode.INSTRUMENTATION.getCopyFSConfigurationEntries();
		Assertions.assertSame(COPY_ENTRIES, fsCopyEntries);
		List<List<String>> nonFsCopyEntries = AOPMode.INSTRUMENTATION.getCopyNonFSConfigurationEntries();
		Assertions.assertSame(COPY_ENTRIES, nonFsCopyEntries);
		List<List<String>> editEntries = AOPMode.ASPECTJ.getEditConfigurationEntries();
		Assertions.assertSame(EDIT_ENTRIES, editEntries);
	}

	@Test
	public void testFilesAndTargetsToCopy() throws Exception {
		JavaFileLoader loaderMock = Mockito.mock(JavaFileLoader.class);
		Mockito.when(loaderMock.loadCopyData(ArgumentMatchers.any(AOPMode.class), ArgumentMatchers.anyBoolean()))
				.thenReturn(COPY_ENTRIES);
		AOPMode.setFileLoader(loaderMock);

		try (MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class)) {
			toolsMock.when(() -> FileTools.resolveFileOnSourceDirectory(ArgumentMatchers.any(String[].class)))
					.thenReturn(DUMMY_PATH);
			toolsMock.when(() -> FileTools.resolveFileOnTargetDirectory(ArgumentMatchers.any(Path.class),
					ArgumentMatchers.any(String[].class))).thenReturn(DUMMY_PATH);

			List<Path> files = AOPMode.ASPECTJ.fsFilesToCopy();
			Assertions.assertEquals(COPY_ENTRIES.size(), files.size());
			files.forEach(file -> Assertions.assertSame(DUMMY_PATH, file));

			List<Path> targets = AOPMode.INSTRUMENTATION.fsTargetsToCopyTo(Path.of("."));
			Assertions.assertEquals(COPY_ENTRIES.size(), targets.size());
			targets.forEach(target -> Assertions.assertSame(DUMMY_PATH, target));

			toolsMock.verify(() -> FileTools.resolveFileOnSourceDirectory(ArgumentMatchers.any(String[].class)),
					Mockito.times(COPY_ENTRIES.size()));
			toolsMock.verify(() -> FileTools.resolveFileOnTargetDirectory(ArgumentMatchers.any(Path.class),
					ArgumentMatchers.any(String[].class)), Mockito.times(COPY_ENTRIES.size()));
		}
	}

	@Test
	public void testHeaderFooterAndSingleTarget() throws Exception {
		JavaFileLoader loaderMock = Mockito.mock(JavaFileLoader.class);
		Mockito.when(loaderMock.loadEditData(ArgumentMatchers.any(AOPMode.class))).thenReturn(EDIT_ENTRIES);
		AOPMode.setFileLoader(loaderMock);

		try (MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class)) {
			toolsMock.when(() -> FileTools.resolveFileOnSourceDirectory("hdr", "pkg")).thenReturn(DUMMY_PATH);
			toolsMock.when(() -> FileTools.resolveFileOnSourceDirectory("ftr", "pkg")).thenReturn(DUMMY_PATH);
			toolsMock.when(() -> FileTools.resolveFileOnTargetDirectory(ArgumentMatchers.any(Path.class), "unused"))
					.thenReturn(DUMMY_PATH);

			Path header = AOPMode.INSTRUMENTATION.threePartedFileHeader();
			Assertions.assertSame(DUMMY_PATH, header);
			Path footer = AOPMode.ASPECTJ.threePartedFileFooter();
			Assertions.assertSame(DUMMY_PATH, footer);
			Path singleTarget = AOPMode.INSTRUMENTATION.targetToCopyTo(Path.of("."));
			Assertions.assertSame(DUMMY_PATH, singleTarget);

			toolsMock.verify(() -> FileTools.resolveFileOnSourceDirectory("hdr", "pkg"));
			toolsMock.verify(() -> FileTools.resolveFileOnSourceDirectory("ftr", "pkg"));
		}
	}

	@Test
	public void testFormatValuesMethods() {
		try (MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class)) {
			toolsMock.when(
					() -> FileTools.generatePackageNameArray(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
					.thenReturn(GENERATED_ARRAY);

			String[] aspectJValue = AOPMode.ASPECTJ.formatValues("com.test");
			Assertions.assertArrayEquals(GENERATED_ARRAY, aspectJValue);
			String[] instrumentationValue = AOPMode.INSTRUMENTATION.formatValues("com.test");
			Assertions.assertArrayEquals(GENERATED_ARRAY, instrumentationValue);

			toolsMock.verify(() -> FileTools.generatePackageNameArray("com.test", 1), Mockito.times(2));
		}
	}

	@Test
	public void testThreePartedFileBodyAndExtractPermissions() {
		FilePermission filePermission = Mockito.mock(FilePermission.class);
		JavaAOPTestCase fileSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
		Mockito.when(fileSystemTestCase.getAopTestCaseSupported())
				.thenReturn(JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);
		Mockito.when(fileSystemTestCase.getResourceAccessSupplier())
				.thenAnswer(invocation -> (Supplier<List<FilePermission>>) () -> List.of(filePermission));

		NetworkPermission networkPermission = Mockito.mock(NetworkPermission.class);
		JavaAOPTestCase networkSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
		Mockito.when(networkSystemTestCase.getAopTestCaseSupported())
				.thenReturn(JavaAOPTestCaseSupported.NETWORK_CONNECTION);
		Mockito.when(networkSystemTestCase.getResourceAccessSupplier())
				.thenAnswer(invocation -> (Supplier<List<NetworkPermission>>) () -> List.of(networkPermission));

		CommandPermission commandPermission = Mockito.mock(CommandPermission.class);
		JavaAOPTestCase commandSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
		Mockito.when(commandSystemTestCase.getAopTestCaseSupported())
				.thenReturn(JavaAOPTestCaseSupported.COMMAND_EXECUTION);
		Mockito.when(commandSystemTestCase.getResourceAccessSupplier())
				.thenAnswer(invocation -> (Supplier<List<CommandPermission>>) () -> List.of(commandPermission));

		ThreadPermission threadPermission = Mockito.mock(ThreadPermission.class);
		JavaAOPTestCase threadSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
		Mockito.when(threadSystemTestCase.getAopTestCaseSupported())
				.thenReturn(JavaAOPTestCaseSupported.THREAD_CREATION);
		Mockito.when(threadSystemTestCase.getResourceAccessSupplier())
				.thenAnswer(invocation -> (Supplier<List<ThreadPermission>>) () -> List.of(threadPermission));

		List<JavaAOPTestCase> allCases = List.of(fileSystemTestCase, networkSystemTestCase, commandSystemTestCase,
				threadSystemTestCase);

		MockedStatic<JavaAOPTestCase> tcMock = Mockito.mockStatic(JavaAOPTestCase.class);
		tcMock.when(() -> JavaAOPTestCase.writeAOPTestCaseFile(ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList(),
				ArgumentMatchers.anyList(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList()))
				.thenReturn(BODY_RESULT);

		try (tcMock) {
			String bodyResult = AOPMode.ASPECTJ.threePartedFileBody("ASPECTJ", "restricted.pkg", List.of("A", "B"),
					allCases);
			Assertions.assertEquals(BODY_RESULT, bodyResult);

			tcMock.verify(() -> JavaAOPTestCase.writeAOPTestCaseFile("ASPECTJ", "restricted.pkg", List.of("A", "B"),
					List.of(filePermission), List.of(networkPermission), List.of(commandPermission),
					List.of(threadPermission)));
		}
	}

	@Test
	public void testResetDoesNotThrow() {
		Executable resetCall = AOPMode.INSTRUMENTATION::reset;
		Assertions.assertDoesNotThrow(resetCall);
	}
}
