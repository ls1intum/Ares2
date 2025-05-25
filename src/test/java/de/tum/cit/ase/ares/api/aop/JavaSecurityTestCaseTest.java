package de.tum.cit.ase.ares.api.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaSecurityTestCaseTest {

    private JavaAOPTestCase javaSecurityTestCase;
    private ResourceAccesses resourceAccesses;

    @BeforeEach
    void setUp() {
        JavaAOPTestCaseSupported supported = JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION;
        resourceAccesses = mock(ResourceAccesses.class);
        javaSecurityTestCase = new JavaAOPTestCase(supported, () -> resourceAccesses.regardingFileSystemInteractions(), Set.of());
    }

    @Test
    void testWriteAOPSecurityTestCase() {
        String result = javaSecurityTestCase.writeAOPTestCase("ARCHUNIT", "INSTRUMENTATION");
        assertEquals("", result);
    }

    @Test
    void testWriteAOPSecurityTestCaseFile() {
        List<String> allowedListedClasses = List.of("TestClass");
        List<JavaAOPTestCase> javaSecurityTestCases = List.of(javaSecurityTestCase);

        String result = JavaAOPTestCase.writeAOPTestCaseFile(
                "INSTRUMENTATION",
                "de.tum.cit",
                allowedListedClasses,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        assertTrue(result.contains("private static String aopMode"));
        assertTrue(result.contains("private static String restrictedPackage"));
        assertTrue(result.contains("private static String[] allowedListedClasses"));
    }

    @Test
    void testExecuteAOPSecurityTestCase() {
        try (MockedStatic<JavaAOPTestCase> mockedStatic = mockStatic(JavaAOPTestCase.class)) {
            javaSecurityTestCase.executeAOPTestCase("WALA", "INSTRUMENTATION");
            mockedStatic.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(anyString(), any(), any(), eq("INSTRUMENTATION")), atLeastOnce());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetPermittedFilePaths() throws Exception {
        Method method = JavaAOPTestCase.class.getDeclaredMethod("getPermittedFilePaths", String.class);
        method.setAccessible(true);
        List<String> filePaths = (List<String>) method.invoke(javaSecurityTestCase, "read");
        assertEquals(filePaths.size(), 0);
    }

    @Test
    void testGenerateAdviceSettingValue() throws Exception {
        Method method = JavaAOPTestCase.class.getDeclaredMethod("generateAdviceSettingValue", String.class, String.class, Object.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, "String", "testAdvice", "testValue");
        assertEquals("private static String testAdvice = \"testValue\";" + System.lineSeparator(), result);
        result = (String) method.invoke(null, "String[]", "testAdviceArray", List.of("value1", "value2"));
        assertEquals("private static String[] testAdviceArray = new String[] {\"value1\", \"value2\"};" + System.lineSeparator(), result);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
            method.invoke(null, "UnknownType", "testAdvice", "value")
        );
        assertEquals(SecurityException.class, thrown.getCause().getClass());
    }
}