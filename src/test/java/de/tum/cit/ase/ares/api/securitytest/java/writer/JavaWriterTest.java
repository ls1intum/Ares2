package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

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
	private List<JavaPhobosTestCase> javaPhobosTestCases;

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
		javaArchitectureTestCases = List.of(mock(JavaArchitectureTestCase.class), mock(JavaArchitectureTestCase.class));
		javaAOPTestCases = List.of(mock(JavaAOPTestCase.class), mock(JavaAOPTestCase.class));
		javaPhobosTestCases = List.of(mock(JavaPhobosTestCase.class), mock(JavaPhobosTestCase.class));
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
				when(architectureMode.fsFilesToCopy()).thenReturn(List.of());
				when(architectureMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(architectureMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.placeholderValues()).thenReturn(List.of());
				// provide format values actually used by JavaWriter
				when(architectureMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(architectureMode.threePartedFileBody(any())).thenReturn("body");
				when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(architectureMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("arch.java"));
				when(architectureMode.formatValues(any())).thenReturn(new String[]{ "pkg", "pkg" });

				// Mock AOPMode
				when(aopMode.fsFilesToCopy()).thenReturn(List.of());
				when(aopMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(aopMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.placeholderValues()).thenReturn(List.of());
				when(aopMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
				when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(aopMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("aop.java"));
				when(aopMode.formatValues(any())).thenReturn(new String[]{ "pkg" });
				when(aopMode.toString()).thenReturn("INSTRUMENTATION");

				// Mock FileTools
				mockedFileTools.when(() -> FileTools.copyAndFormatFSFiles(any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.copyAndFormatNonFSFiles(any(), any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any())).thenReturn(tempDir.resolve("test.java"));

				// Act
				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses, testClasses, packageName, mainClassInPackageName,
						javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases, tempDir);

				// Assert
				assertNotNull(result);
				assertEquals(2, result.size()); // One from arch, one from AOP

				// Verify mocks were called correctly
				verify(architectureMode).fsFilesToCopy();
				verify(architectureMode).nonFSFilesToCopy();
				verify(architectureMode).fsTargetsToCopyTo(any());
				verify(architectureMode).nonFSTargetsToCopyTo(any());
				verify(architectureMode).placeholderValues();
				verify(architectureMode).fsFormatValues(packageName, mainClassInPackageName);
				verify(architectureMode).nonFSFormatValues(packageName, mainClassInPackageName);
				verify(architectureMode).threePartedFileHeader();
				verify(architectureMode).threePartedFileBody(javaArchitectureTestCases);
				verify(architectureMode).threePartedFileFooter();
				verify(architectureMode).targetToCopyTo(any());
				verify(architectureMode).formatValues(packageName);

				verify(aopMode).fsFilesToCopy();
				verify(aopMode).nonFSFilesToCopy();
				verify(aopMode).fsTargetsToCopyTo(any());
				verify(aopMode).nonFSTargetsToCopyTo(any());
				verify(aopMode).placeholderValues();
				verify(aopMode).fsFormatValues(packageName, mainClassInPackageName);
				verify(aopMode).nonFSFormatValues(packageName, mainClassInPackageName);
				verify(aopMode).threePartedFileHeader();
				verify(aopMode).threePartedFileFooter();
				verify(aopMode).targetToCopyTo(any());
				verify(aopMode).formatValues(packageName);
			}
		}

		// Removed null-path test; JavaWriter delegates to FileTools.resolveOnPath which
		// requires non-null Path

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

				when(architectureMode.fsFilesToCopy()).thenReturn(List.of());
				when(architectureMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(architectureMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.placeholderValues()).thenReturn(List.of());
				when(architectureMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(architectureMode.threePartedFileBody(emptyArchTestCases)).thenReturn("body");
				when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(architectureMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("arch.java"));
				when(architectureMode.formatValues(any())).thenReturn(new String[]{ "pkg", "pkg" });

				when(aopMode.fsFilesToCopy()).thenReturn(List.of());
				when(aopMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(aopMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.placeholderValues()).thenReturn(List.of());
				when(aopMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(aopMode.threePartedFileBody(any(), any(), any(), eq(emptyAOPTestCases))).thenReturn("body");
				when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(aopMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("aop.java"));
				when(aopMode.formatValues(any())).thenReturn(new String[]{ "pkg" });
				when(aopMode.toString()).thenReturn("INSTRUMENTATION");

				mockedFileTools.when(() -> FileTools.copyAndFormatFSFiles(any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.copyAndFormatNonFSFiles(any(), any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any())).thenReturn(tempDir.resolve("test.java"));

				// Act
				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, emptyPackages, emptyClasses, emptyTestClasses, packageName, mainClassInPackageName,
						emptyArchTestCases, emptyAOPTestCases, javaPhobosTestCases, tempDir);

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
				when(architectureMode.fsFilesToCopy()).thenReturn(List.of());
				when(architectureMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(architectureMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.placeholderValues()).thenReturn(List.of());
				when(architectureMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(architectureMode.threePartedFileBody(any())).thenReturn("body");
				when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(architectureMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("arch.java"));
				when(architectureMode.formatValues(any())).thenReturn(new String[]{ "pkg", "pkg" });

				when(aopMode.fsFilesToCopy()).thenReturn(List.of());
				when(aopMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(aopMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.placeholderValues()).thenReturn(List.of());
				when(aopMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
				when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(aopMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("aop.java"));
				when(aopMode.formatValues(any())).thenReturn(new String[]{ "pkg" });
				when(aopMode.toString()).thenReturn("INSTRUMENTATION");

				mockedFileTools.when(() -> FileTools.copyAndFormatFSFiles(any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.copyAndFormatNonFSFiles(any(), any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any())).thenReturn(tempDir.resolve("test.java"));

				// Act
				javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, tempDir);

				// Assert - verify that merged list contains both essential and test classes
				verify(aopMode).threePartedFileBody(eq("INSTRUMENTATION"), eq(packageName), argThat(list -> {
					List<String> allowedClasses = (List<String>) list;
					return allowedClasses.containsAll(essentialClasses) && allowedClasses.containsAll(testClasses) && allowedClasses.size() == essentialClasses.size() + testClasses.size();
				}), eq(javaAOPTestCases));
			}
		}

		@Test
		@DisplayName("Should handle different build modes")
		void shouldHandleDifferentBuildModes() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
				// Arrange
				when(architectureMode.fsFilesToCopy()).thenReturn(List.of());
				when(architectureMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(architectureMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(architectureMode.placeholderValues()).thenReturn(List.of());
				when(architectureMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(architectureMode.threePartedFileBody(any())).thenReturn("body");
				when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(architectureMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("arch.java"));
				when(architectureMode.formatValues(any())).thenReturn(new String[]{ "pkg", "pkg" });

				when(aopMode.fsFilesToCopy()).thenReturn(List.of());
				when(aopMode.nonFSFilesToCopy()).thenReturn(List.of());
				when(aopMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
				when(aopMode.placeholderValues()).thenReturn(List.of());
				when(aopMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.nonFSFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[]{ "pkg", "pkg", "Main" }));
				when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
				when(aopMode.threePartedFileBody(any(), any(), any(), any())).thenReturn("body");
				when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
				when(aopMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("aop.java"));
				when(aopMode.formatValues(any())).thenReturn(new String[]{ "pkg" });
				when(aopMode.toString()).thenReturn("INSTRUMENTATION");

				mockedFileTools.when(() -> FileTools.copyAndFormatFSFiles(any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.copyAndFormatNonFSFiles(any(), any(), any(), any())).thenReturn(List.of());
				mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any())).thenReturn(tempDir.resolve("test.java"));

				// Act & Assert - should work with different build modes
				assertDoesNotThrow(() -> {
					javaWriter.writeTestCases(BuildMode.GRADLE, architectureMode, aopMode, essentialPackages, essentialClasses, testClasses, packageName, mainClassInPackageName,
							javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases, tempDir);
				});
			}
		}
	}
}
