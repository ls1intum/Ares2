package de.tum.cit.ase.ares.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaSecurityTestCaseTest {

    private JavaSecurityTestCase javaSecurityTestCase;
    private ResourceAccesses resourceAccesses;

    @BeforeEach
    void setUp() {
        JavaSecurityTestCaseSupported supported = JavaSecurityTestCaseSupported.FILESYSTEM_INTERACTION;
        resourceAccesses = mock(ResourceAccesses.class);
        javaSecurityTestCase = new JavaSecurityTestCase(supported, resourceAccesses);
    }

    @Test
    void testWriteAOPSecurityTestCase() {
        String result = javaSecurityTestCase.writeAOPSecurityTestCase("INSTRUMENTATION");
        assertEquals("", result);
    }

    @Test
    void testWriteAOPSecurityTestCaseFile() {
        List<String> allowedListedClasses = List.of("TestClass");
        List<JavaSecurityTestCase> javaSecurityTestCases = List.of(javaSecurityTestCase);

        String result = JavaSecurityTestCase.writeAOPSecurityTestCaseFile(
                "INSTRUMENTATION",
                "de.tum.cit",
                allowedListedClasses,
                javaSecurityTestCases
        );

        assertTrue(result.contains("private static String aopMode"));
        assertTrue(result.contains("private static String restrictedPackage"));
        assertTrue(result.contains("private static String[] allowedListedClasses"));
    }

    @Test
    void testExecuteAOPSecurityTestCase() {
        try (MockedStatic<JavaSecurityTestCase> mockedStatic = mockStatic(JavaSecurityTestCase.class)) {
            javaSecurityTestCase.executeAOPSecurityTestCase("INSTRUMENTATION");
            mockedStatic.verify(() -> JavaSecurityTestCase.setJavaAdviceSettingValue(anyString(), any(), eq("INSTRUMENTATION")), atLeastOnce());
        }
    }

    @Test
    void testGetPermittedFilePaths() throws Exception {
        Method method = JavaSecurityTestCase.class.getDeclaredMethod("getPermittedFilePaths", String.class);
        method.setAccessible(true);
        List<String> filePaths = (List<String>) method.invoke(javaSecurityTestCase, "read");
        assertEquals(filePaths.size(), 0);
    }

    @Test
    void testGenerateAdviceSettingValue() throws Exception {
        Method method = JavaSecurityTestCase.class.getDeclaredMethod("generateAdviceSettingValue", String.class, String.class, Object.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, "String", "testAdvice", "testValue");
        assertEquals("private static String testAdvice = \"testValue\";\n", result);
        result = (String) method.invoke(null, "String[]", "testAdviceArray", List.of("value1", "value2"));
        assertEquals("private static String[] testAdviceArray = new String[] {\"value1\", \"value2\"};\n", result);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(null, "UnknownType", "testAdvice", "value");
        });
        assertEquals(SecurityException.class, thrown.getCause().getClass());
        assertEquals("Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Erstellung): Unbekannter Datentyp beim Erstellen des Wertes value für die Rateneinstellungen UnknownType testAdvice", thrown.getCause().getMessage());
    }
}