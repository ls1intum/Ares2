package de.tum.cit.ase.ares.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaAOPModeTest {

    private JavaAOPMode instrumentationMode;
    private JavaAOPMode aspectjMode;

    @BeforeEach
    void setUp() {
        instrumentationMode = JavaAOPMode.INSTRUMENTATION;
        aspectjMode = JavaAOPMode.ASPECTJ;
    }

    @Test
    void testEnumValues() {
        JavaAOPMode[] modes = JavaAOPMode.values();
        assertEquals(2, modes.length);
        assertTrue(Arrays.asList(modes).contains(JavaAOPMode.INSTRUMENTATION));
        assertTrue(Arrays.asList(modes).contains(JavaAOPMode.ASPECTJ));
    }

    @Test
    void testFilesToCopy_InstrumentationMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools.when(() -> FileTools.resolveOnResources(any(String[].class)))
                    .thenReturn(mock(Path.class));
            instrumentationMode.filesToCopy();
            mockedFileTools.verify(() -> FileTools.resolveOnResources(any(String[].class)), times(13));
        }
    }

    @Test
    void testFilesToCopy_AspectJMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools.when(() -> FileTools.resolveOnResources(any(String[].class)))
                    .thenReturn(mock(Path.class));
            aspectjMode.filesToCopy();
            mockedFileTools.verify(() -> FileTools.resolveOnResources(any(String[].class)), times(2));
        }
    }

    @Test
    void testFileValues_InstrumentationMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools.when(() -> FileTools.generatePackageNameArray(anyString(), anyInt()))
                    .thenReturn(new String[]{"mocked", "array"});
            instrumentationMode.fileValues("com.example", "MainClass");
            mockedFileTools.verify(() -> FileTools.generatePackageNameArray(anyString(), anyInt()), times(12));
        }
    }

    @Test
    void testReset() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> settingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, classLoader);
            Method resetMethod = settingsClass.getDeclaredMethod("reset");
            resetMethod.setAccessible(true);
            resetMethod.invoke(null);
        } catch (Exception e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }
}