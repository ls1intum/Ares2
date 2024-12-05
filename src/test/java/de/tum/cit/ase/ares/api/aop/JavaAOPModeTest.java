package de.tum.cit.ase.ares.api.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaAOPModeTest {

    private static final int SETUP_OPTIONS_COUNT = 2;
    /**
     * Expected number of files to copy for instrumentation mode.
     * This includes:
     * - [List the specific files or types of files expected]
     */
    private static final int INSTRUMENTATION_FILES_COUNT = 13;
    private static final int INSTRUMENTATION_VALUES_COUNT = 13;

    /**
     * Expected number of files to copy for AspectJ mode.
     * This includes:
     * - [List the specific files or types of files expected]
     */
    private static final int ASPECTJ_FILES_COUNT = 2;
    private static final int ASPECTJ_VALUES_COUNT = 2;

    private static String TEST_PACKAGE = "com.example";
    private static String TEST_MAIN_CLASS = "MainClass";
    private static String[] EXPECTED_ARRAY = {"mocked", "array"};

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
        assertEquals(SETUP_OPTIONS_COUNT, modes.length);
        assertTrue(Arrays.asList(modes).contains(JavaAOPMode.INSTRUMENTATION));
        assertTrue(Arrays.asList(modes).contains(JavaAOPMode.ASPECTJ));
    }

    @Test
    void testFilesToCopy_InstrumentationMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools
                    .when(() -> FileTools.resolveOnResources(any(String[].class)))
                    .thenReturn(mock(Path.class));
            instrumentationMode.filesToCopy();
            mockedFileTools
                    .verify(() -> FileTools.resolveOnResources(any(String[].class)),
                            times(INSTRUMENTATION_FILES_COUNT)
                    );
        }
    }

    @Test
    void testFilesToCopy_AspectJMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools
                    .when(() -> FileTools.resolveOnResources(any(String[].class)))
                    .thenReturn(mock(Path.class));
            aspectjMode.filesToCopy();
            mockedFileTools
                    .verify(() -> FileTools.resolveOnResources(any(String[].class)),
                            times(ASPECTJ_FILES_COUNT)
                    );
        }
    }

    @Test
    void testFileValues_InstrumentationMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools
                    .when(() -> FileTools.generatePackageNameArray(anyString(), anyInt()))
                    .thenReturn(EXPECTED_ARRAY);
            instrumentationMode.fileValues(TEST_PACKAGE, TEST_MAIN_CLASS);
            mockedFileTools
                    .verify(() -> FileTools.generatePackageNameArray(anyString(), anyInt()),
                            times(INSTRUMENTATION_VALUES_COUNT)
                    );
        }
    }

    @Test
    void testFileValues_AspectJMode() {
        try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class)) {
            mockedFileTools
                    .when(() -> FileTools.generatePackageNameArray(anyString(), anyInt()))
                    .thenReturn(EXPECTED_ARRAY);
            aspectjMode.fileValues(TEST_PACKAGE, TEST_MAIN_CLASS);
            mockedFileTools
                    .verify(() -> FileTools.generatePackageNameArray(anyString(), anyInt()),
                            times(ASPECTJ_VALUES_COUNT)
                    );
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