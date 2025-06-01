package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class JavaAOPModeTest {

    public static final List<List<String>> COPY_ENTRIES = List.of(
            List.of("pkg1/pkg2", "1", "destPkg"),
            List.of("foo/bar", "0", "anotherDest")
    );

    public static final List<List<String>> EDIT_ENTRIES = List.of(
            List.of("hdr/pkg", "ftr/pkg", "unused")
    );

    public static final Path DUMMY_PATH = Mockito.mock(Path.class);

    public static final String[] GENERATED_ARRAY = new String[]{"a", "b", "c"};

    public static final String BODY_RESULT = "SECURITY_BODY";

    /*@Test
    public void testGetConfigurations() {
        MockedConstruction<JavaCSVFileLoader> loaderMock = Mockito.mockConstruction(
                JavaCSVFileLoader.class,
                (mock, ctx) -> {
                    Mockito.when(mock.loadCopyData(ArgumentMatchers.any())).thenReturn(COPY_ENTRIES);
                    Mockito.when(mock.loadEditData(ArgumentMatchers.any())).thenReturn(EDIT_ENTRIES);
                }
        );
        try (loaderMock) {
            List<List<String>> copyEntries = AOPMode.INSTRUMENTATION.getCopyConfigurationEntries();
            Assertions.assertSame(COPY_ENTRIES, copyEntries);
            List<List<String>> editEntries = AOPMode.ASPECTJ.getEditConfigurationEntries();
            Assertions.assertSame(EDIT_ENTRIES, editEntries);
        }
    }

    @Test
    public void testFilesAndTargetsToCopy() {
        MockedConstruction<JavaCSVFileLoader> loaderMock = Mockito.mockConstruction(
                JavaCSVFileLoader.class,
                (mock, ctx) -> {
                    Mockito.when(mock.loadCopyData(ArgumentMatchers.any())).thenReturn(COPY_ENTRIES);
                    Mockito.when(mock.loadEditData(ArgumentMatchers.any())).thenReturn(EDIT_ENTRIES);
                }
        );
        MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class);
        toolsMock.when(() -> FileTools.resolveOnPackage(ArgumentMatchers.any(String[].class))).thenReturn(DUMMY_PATH);

        try (loaderMock; toolsMock) {
            // first: filesToCopy
            List<Path> files = AOPMode.ASPECTJ.filesToCopy();
            Assertions.assertEquals(COPY_ENTRIES.size(), files.size());
            files.forEach(file -> Assertions.assertSame(DUMMY_PATH, file));
            toolsMock.verify(() -> FileTools.resolveOnPackage(ArgumentMatchers.any(String[].class)), Mockito.times(COPY_ENTRIES.size()));

            // then: targetsToCopyTo
            List<Path> targets = AOPMode.INSTRUMENTATION.targetsToCopyTo(Path.of("."), "pkg");
            Assertions.assertEquals(COPY_ENTRIES.size(), targets.size());
            targets.forEach(target -> Assertions.assertSame(DUMMY_PATH, target));
            toolsMock.verify(() -> FileTools.resolveOnPackage(ArgumentMatchers.any(String[].class)), Mockito.times(COPY_ENTRIES.size()));
        }
    }

    @Test
    public void testHeaderFooterAndSingleTarget() {
        MockedConstruction<JavaCSVFileLoader> loaderMock = Mockito.mockConstruction(
                JavaCSVFileLoader.class,
                (mock, ctx) -> {
                    Mockito.when(mock.loadCopyData(ArgumentMatchers.any())).thenReturn(COPY_ENTRIES);
                    Mockito.when(mock.loadEditData(ArgumentMatchers.any())).thenReturn(EDIT_ENTRIES);
                }
        );
        MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class);
        toolsMock.when(() -> FileTools.resolveOnPackage(new String[]{"hdr", "pkg"})).thenReturn(DUMMY_PATH);
        toolsMock.when(() -> FileTools.resolveOnPackage(new String[]{"ftr", "pkg"})).thenReturn(DUMMY_PATH);

        try (loaderMock; toolsMock) {
            Path header = AOPMode.INSTRUMENTATION.threePartedFileHeader();
            Assertions.assertSame(DUMMY_PATH, header);

            Path footer = AOPMode.ASPECTJ.threePartedFileFooter();
            Assertions.assertSame(DUMMY_PATH, footer);

            Path singleTarget = AOPMode.INSTRUMENTATION.targetToCopyTo(Path.of("."), "pkg");
            Assertions.assertSame(DUMMY_PATH, singleTarget);

            toolsMock.verify(() -> FileTools.resolveOnPackage(new String[]{"hdr", "pkg"}));
            toolsMock.verify(() -> FileTools.resolveOnPackage(new String[]{"ftr", "pkg"}));
        }
    }

    @Test
    public void testFormatValuesMethods() {
        MockedConstruction<JavaCSVFileLoader> loaderMock = Mockito.mockConstruction(
                JavaCSVFileLoader.class,
                (mock, ctx) -> {
                    Mockito.when(mock.loadCopyData(ArgumentMatchers.any())).thenReturn(COPY_ENTRIES);
                    Mockito.when(mock.loadEditData(ArgumentMatchers.any())).thenReturn(EDIT_ENTRIES);
                }
        );
        MockedStatic<FileTools> toolsMock = Mockito.mockStatic(FileTools.class);
        toolsMock.when(() -> FileTools.generatePackageNameArray(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(GENERATED_ARRAY);

        try (loaderMock; toolsMock) {
            String[] singleValue = AOPMode.ASPECTJ.formatValues("com.test");
            Assertions.assertArrayEquals(GENERATED_ARRAY, singleValue);

            List<String[]> multiValues = AOPMode.INSTRUMENTATION.formatValues("com.test", "Main");
            multiValues.forEach(arr -> Assertions.assertArrayEquals(GENERATED_ARRAY, arr));
            toolsMock.verify(() -> FileTools.generatePackageNameArray("com.test", 1), Mockito.times(2));
        }
    }*/

    @Test
    public void testThreePartedFileBodyAndExtractPermissions() {
        FilePermission filePermission = Mockito.mock(FilePermission.class);
        JavaAOPTestCase fileSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
        Mockito.when(fileSystemTestCase.getAopTestCaseSupported()).thenReturn(JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);
        Mockito.when(fileSystemTestCase.getResourceAccessSupplier()).thenAnswer(invocation -> (Supplier<List<FilePermission>>) () -> List.of(filePermission));

        NetworkPermission networkPermission = Mockito.mock(NetworkPermission.class);
        JavaAOPTestCase networkSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
        Mockito.when(networkSystemTestCase.getAopTestCaseSupported()).thenReturn(JavaAOPTestCaseSupported.NETWORK_CONNECTION);
        Mockito.when(networkSystemTestCase.getResourceAccessSupplier()).thenAnswer(invocation -> (Supplier<List<NetworkPermission>>) () -> List.of(networkPermission));

        CommandPermission commandPermission = Mockito.mock(CommandPermission.class);
        JavaAOPTestCase commandSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
        Mockito.when(commandSystemTestCase.getAopTestCaseSupported()).thenReturn(JavaAOPTestCaseSupported.COMMAND_EXECUTION);
        Mockito.when(commandSystemTestCase.getResourceAccessSupplier()).thenAnswer(invocation -> (Supplier<List<CommandPermission>>) () -> List.of(commandPermission));

        ThreadPermission threadPermission = Mockito.mock(ThreadPermission.class);
        JavaAOPTestCase threadSystemTestCase = Mockito.mock(JavaAOPTestCase.class);
        Mockito.when(threadSystemTestCase.getAopTestCaseSupported()).thenReturn(JavaAOPTestCaseSupported.THREAD_CREATION);
        Mockito.when(threadSystemTestCase.getResourceAccessSupplier()).thenAnswer(invocation -> (Supplier<List<ThreadPermission>>) () -> List.of(threadPermission));

        List<JavaAOPTestCase> allCases = List.of(fileSystemTestCase, networkSystemTestCase, commandSystemTestCase, threadSystemTestCase);

        MockedStatic<JavaAOPTestCase> tcMock = Mockito.mockStatic(JavaAOPTestCase.class);
        tcMock.when(() -> JavaAOPTestCase.writeAOPTestCaseFile(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList()
        )).thenReturn(BODY_RESULT);

        try (tcMock) {
            String bodyResult = AOPMode.ASPECTJ.threePartedFileBody(
                    "ASPECTJ",
                    "restricted.pkg",
                    List.of("A", "B"),
                    allCases
            );
            Assertions.assertEquals(BODY_RESULT, bodyResult);

            tcMock.verify(() -> JavaAOPTestCase.writeAOPTestCaseFile(
                    "ASPECTJ",
                    "restricted.pkg",
                    List.of("A", "B"),
                    List.of(filePermission),
                    List.of(networkPermission),
                    List.of(commandPermission),
                    List.of(threadPermission)
            ));
        }
    }

    @Test
    public void testResetDoesNotThrow() {
        Executable resetCall = AOPMode.INSTRUMENTATION::reset;
        Assertions.assertDoesNotThrow(resetCall);
    }
}
