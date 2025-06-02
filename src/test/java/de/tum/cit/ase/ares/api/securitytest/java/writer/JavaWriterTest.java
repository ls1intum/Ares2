package de.tum.cit.ase.ares.api.securitytest.java.writer;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JavaWriter Tests")
public class JavaWriterTest {

    private JavaWriter javaWriter;
    private BuildMode buildMode;
    private ArchitectureMode architectureMode;
    private AOPMode aopMode;
    private List<String> essentialPackages;
    private List<String> essentialClasses;
    private List<String> testClasses;
    private String packageName;
    private String mainClassInPackageName;
    private List<JavaArchitectureTestCase> javaArchitectureTestCases;
    private List<JavaAOPTestCase> javaAOPTestCases;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        javaWriter = new JavaWriter();
        buildMode = BuildMode.MAVEN;
        architectureMode = mock(ArchitectureMode.class);
        aopMode = mock(AOPMode.class);
        essentialPackages = List.of("java.lang", "java.util");
        essentialClasses = List.of("java.lang.String", "java.util.List");
        testClasses = List.of("TestClass1", "TestClass2");
        packageName = "com.example";
        mainClassInPackageName = "MainClass";
        javaArchitectureTestCases = List.of(
                mock(JavaArchitectureTestCase.class),
                mock(JavaArchitectureTestCase.class)
        );
        javaAOPTestCases = List.of(
                mock(JavaAOPTestCase.class),
                mock(JavaAOPTestCase.class)
        );
    }

    @Nested
    @DisplayName("writeTestCases() Tests")
    class WriteTestCasesTests {

        @Test
        @DisplayName("Should implement Writer interface")
        void shouldImplementWriterInterface() {
            // Assert
            assertTrue(javaWriter instanceof Writer);
        }

        @Test
        @DisplayName("Should write test cases and return file paths")
        void shouldWriteTestCasesAndReturnFilePaths() {
            try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
                // Arrange
                // Mock ArchitectureMode
                when(architectureMode.filesToCopy()).thenReturn(List.of());
                when(architectureMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(architectureMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(architectureMode.threePartedFileBody(any())).thenReturn("body");
                when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(architectureMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("arch.java"));
                when(architectureMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});

                // Mock AOPMode
                when(aopMode.filesToCopy()).thenReturn(List.of());
                when(aopMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(aopMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
                when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(aopMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("aop.java"));
                when(aopMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});
                when(aopMode.toString()).thenReturn("INSTRUMENTATION");

                // Mock FileTools
                mockedFileTools.when(() -> FileTools.copyFormatStringFiles(any(), any(), any()))
                        .thenReturn(List.of());
                mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
                        .thenReturn(tempDir.resolve("test.java"));

                // Act
                List<Path> result = javaWriter.writeTestCases(
                        buildMode, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases, tempDir
                );

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size()); // One from arch, one from AOP

                // Verify mocks were called correctly
                verify(architectureMode).filesToCopy();
                verify(architectureMode).targetsToCopyTo(tempDir, packageName);
                verify(architectureMode).formatValues(packageName, mainClassInPackageName);
                verify(architectureMode).threePartedFileHeader();
                verify(architectureMode).threePartedFileBody(javaArchitectureTestCases);
                verify(architectureMode).threePartedFileFooter();
                verify(architectureMode).targetToCopyTo(tempDir, packageName);
                verify(architectureMode).formatValues(packageName);

                verify(aopMode).filesToCopy();
                verify(aopMode).targetsToCopyTo(tempDir, packageName);
                verify(aopMode).formatValues(packageName, mainClassInPackageName);
                verify(aopMode).threePartedFileHeader();
                verify(aopMode).threePartedFileFooter();
                verify(aopMode).targetToCopyTo(tempDir, packageName);
                verify(aopMode).formatValues(packageName);
            }
        }

        @Test
        @DisplayName("Should handle null testFolderPath")
        void shouldHandleNullTestFolderPath() {
            try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
                // Arrange
                when(architectureMode.filesToCopy()).thenReturn(List.of());
                when(architectureMode.targetsToCopyTo(isNull(), any())).thenReturn(List.of());
                when(architectureMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(architectureMode.threePartedFileHeader()).thenReturn(Path.of("header.java"));
                when(architectureMode.threePartedFileBody(any())).thenReturn("body");
                when(architectureMode.threePartedFileFooter()).thenReturn(Path.of("footer.java"));
                when(architectureMode.targetToCopyTo(isNull(), any())).thenReturn(Path.of("arch.java"));
                when(architectureMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});

                when(aopMode.filesToCopy()).thenReturn(List.of());
                when(aopMode.targetsToCopyTo(isNull(), any())).thenReturn(List.of());
                when(aopMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(aopMode.threePartedFileHeader()).thenReturn(Path.of("header.java"));
                when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
                when(aopMode.threePartedFileFooter()).thenReturn(Path.of("footer.java"));
                when(aopMode.targetToCopyTo(isNull(), any())).thenReturn(Path.of("aop.java"));
                when(aopMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});
                when(aopMode.toString()).thenReturn("INSTRUMENTATION");

                mockedFileTools.when(() -> FileTools.copyFormatStringFiles(any(), any(), any()))
                        .thenReturn(List.of());
                mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
                        .thenReturn(Path.of("test.java"));

                // Act & Assert
                assertDoesNotThrow(() -> {
                    List<Path> result = javaWriter.writeTestCases(
                            buildMode, architectureMode, aopMode, essentialPackages,
                            essentialClasses, testClasses, packageName, mainClassInPackageName,
                            javaArchitectureTestCases, javaAOPTestCases, null
                    );
                    assertNotNull(result);
                });
            }
        }

        @Test
        @DisplayName("Should handle empty lists")
        void shouldHandleEmptyLists() {
            try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
                // Arrange
                List<String> emptyPackages = List.of();
                List<String> emptyClasses = List.of();
                List<String> emptyTestClasses = List.of();
                List<JavaArchitectureTestCase> emptyArchTestCases = List.of();
                List<JavaAOPTestCase> emptyAOPTestCases = List.of();

                when(architectureMode.filesToCopy()).thenReturn(List.of());
                when(architectureMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(architectureMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(architectureMode.threePartedFileBody(emptyArchTestCases)).thenReturn("body");
                when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(architectureMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("arch.java"));
                when(architectureMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});

                when(aopMode.filesToCopy()).thenReturn(List.of());
                when(aopMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(aopMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(aopMode.threePartedFileBody(any(), any(), any(), eq(emptyAOPTestCases))).thenReturn("body");
                when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(aopMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("aop.java"));
                when(aopMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});
                when(aopMode.toString()).thenReturn("INSTRUMENTATION");

                mockedFileTools.when(() -> FileTools.copyFormatStringFiles(any(), any(), any()))
                        .thenReturn(List.of());
                mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
                        .thenReturn(tempDir.resolve("test.java"));

                // Act
                List<Path> result = javaWriter.writeTestCases(
                        buildMode, architectureMode, aopMode, emptyPackages,
                        emptyClasses, emptyTestClasses, packageName, mainClassInPackageName,
                        emptyArchTestCases, emptyAOPTestCases, tempDir
                );

                // Assert
                assertNotNull(result);
                verify(architectureMode).threePartedFileBody(emptyArchTestCases);
                verify(aopMode).threePartedFileBody(eq("INSTRUMENTATION"), eq(packageName), any(), eq(emptyAOPTestCases));
            }
        }

        @Test
        @DisplayName("Should merge essential classes and test classes correctly")
        void shouldMergeEssentialClassesAndTestClassesCorrectly() {
            try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
                // Arrange
                when(architectureMode.filesToCopy()).thenReturn(List.of());
                when(architectureMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(architectureMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(architectureMode.threePartedFileBody(any())).thenReturn("body");
                when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(architectureMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("arch.java"));
                when(architectureMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});

                when(aopMode.filesToCopy()).thenReturn(List.of());
                when(aopMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(aopMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
                when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(aopMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("aop.java"));
                when(aopMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});
                when(aopMode.toString()).thenReturn("INSTRUMENTATION");

                mockedFileTools.when(() -> FileTools.copyFormatStringFiles(any(), any(), any()))
                        .thenReturn(List.of());
                mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
                        .thenReturn(tempDir.resolve("test.java"));

                // Act
                javaWriter.writeTestCases(
                        buildMode, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases, tempDir
                );

                // Assert - verify that merged list contains both essential and test classes
                verify(aopMode).threePartedFileBody(eq("INSTRUMENTATION"), eq(packageName), argThat(list -> {
                    List<String> allowedClasses = (List<String>) list;
                    return allowedClasses.containsAll(essentialClasses) && 
                           allowedClasses.containsAll(testClasses) &&
                           allowedClasses.size() == essentialClasses.size() + testClasses.size();
                }), eq(javaAOPTestCases));
            }
        }

        @Test
        @DisplayName("Should handle different build modes")
        void shouldHandleDifferentBuildModes() {
            try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
                // Arrange
                when(architectureMode.filesToCopy()).thenReturn(List.of());
                when(architectureMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(architectureMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(architectureMode.threePartedFileBody(any())).thenReturn("body");
                when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(architectureMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("arch.java"));
                when(architectureMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});

                when(aopMode.filesToCopy()).thenReturn(List.of());
                when(aopMode.targetsToCopyTo(any(), any())).thenReturn(List.of());
                when(aopMode.formatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{"pkg", "pkg", "Main"}));
                when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
                when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
                when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
                when(aopMode.targetToCopyTo(any(), any())).thenReturn(tempDir.resolve("aop.java"));
                when(aopMode.formatValues(any())).thenReturn(new String[]{"pkg", "pkg", "Main"});
                when(aopMode.toString()).thenReturn("INSTRUMENTATION");

                mockedFileTools.when(() -> FileTools.copyFormatStringFiles(any(), any(), any()))
                        .thenReturn(List.of());
                mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
                        .thenReturn(tempDir.resolve("test.java"));

                // Act & Assert - should work with different build modes
                assertDoesNotThrow(() -> {
                    javaWriter.writeTestCases(
                            BuildMode.GRADLE, architectureMode, aopMode, essentialPackages,
                            essentialClasses, testClasses, packageName, mainClassInPackageName,
                            javaArchitectureTestCases, javaAOPTestCases, tempDir
                    );
                });
            }
        }
    }
}
