package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPAdviceSettingTriple;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class JavaAOPTestCaseTest {

    @BeforeEach
    void resetSettings() throws Exception {
        Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
        reset.setAccessible(true);
        reset.invoke(null);
    }

    @Test
    void testGenerateAdviceSettingValueNull() {
        String result = invokeGenerate("String", "foo", null);
        assertEquals("private static String foo = null;\n", result);
    }

    @Test
    void testGenerateAdviceSettingValueKnown() {
        String result = invokeGenerate("String", "bar", "baz");
        assertTrue(result.contains("bar"), "should contain field name");
        assertTrue(result.contains("baz"), "should contain value");
    }

    @Test
    void testGenerateAdviceSettingValueTriple() {
        JavaAOPAdviceSettingTriple triple = new JavaAOPAdviceSettingTriple(
                "String", "foo", null);
        String direct = invokeGenerate("String", "foo", null);
        String viaTriple = invokeGenerate(triple);

        assertEquals(direct, viaTriple);
    }

    @Test
    void testGenerateAdviceSettingValueUnknownTypeThrows() {
        assertThrows(RuntimeException.class,
                () -> invokeGenerate("Unknown", "x", "value")
        );
    }

    @Test
    void testSetJavaAdviceSettingValueHappyPathOtherMode() throws Exception {
        String field = "restrictedPackage";

        JavaAOPTestCase.setJavaAdviceSettingValue(
                field,
                "xyz",
                "ARCH",
                "ASPECTJ"
        );

        Field f = JavaAOPTestCaseSettings.class.getDeclaredField(field);
        f.setAccessible(true);

        assertEquals("xyz", f.get(null));
    }

    @Test
    void testSetJavaAdviceSettingValueNoSuchFieldThrows() {
        assertThrows(SecurityException.class,
                () -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        "nonexistent",
                        "val",
                        "A",
                        "INSTRUMENTATION"
                )
        );
    }

    @Test
    void testSetJavaAdviceSettingValueNullFieldThrows() {
        assertThrows(SecurityException.class,
                () -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        null,
                        "val",
                        "A",
                        "INSTRUMENTATION"
                )
        );
    }

    @Test
    void testSetJavaAdviceSettingValueIllegalArgumentThrows() {
        assertThrows(SecurityException.class,
                () -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        "portsAllowedToBeConnectedTo",
                        new String[]{"a"},
                        "A",
                        "INSTRUMENTATION"
                )
        );
    }

    @Test
    void testBuilderNullSupportedThrows() {
        JavaAOPTestCase.Builder builder = JavaAOPTestCase.builder();
        assertThrows(SecurityException.class,
                builder::build
        );
    }

    @Test
    void testBuilderNullResourceThrows() {
        JavaAOPTestCase.Builder builder = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);

        assertThrows(SecurityException.class,
                builder::build
        );
    }

    @Test
    void testBuilderNullAllowedThrows() {
        Supplier<List<?>> supplier = Collections::emptyList;

        JavaAOPTestCase.Builder builder = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION)
                .resourceAccessSupplier(supplier);

        assertThrows(SecurityException.class,
                builder::build
        );
    }

    @Test
    void testBuilderHappyPath() {
        Supplier<List<?>> supplier = List::of;
        Set<ClassPermission> allowed = Set.of();

        JavaAOPTestCase tc = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.NETWORK_CONNECTION)
                .resourceAccessSupplier(supplier)
                .allowedClasses(allowed)
                .build();

        assertEquals(
                JavaAOPTestCaseSupported.NETWORK_CONNECTION,
                tc.getAopTestCaseSupported()
        );

        assertSame(
                supplier,
                tc.getResourceAccessSupplier()
        );

        assertSame(
                allowed,
                tc.getAllowedClasses()
        );
    }

    @Test
    void testWriteAOPTestCaseReturnsEmpty() {
        Supplier<List<?>> supplier = Collections::emptyList;
        Set<ClassPermission> allowed = Set.of();

        JavaAOPTestCase tc = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.COMMAND_EXECUTION)
                .resourceAccessSupplier(supplier)
                .allowedClasses(allowed)
                .build();

        assertEquals(
                "",
                tc.writeAOPTestCase("arch", "mode")
        );
    }

    @Test
    void testExecuteAOPTestCaseNetwork() throws Exception {
        resetSettings();

        Supplier<List<?>> supplier = Collections::emptyList;
        Set<ClassPermission> allowed = Set.of();

        JavaAOPTestCase tc = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.NETWORK_CONNECTION)
                .resourceAccessSupplier(supplier)
                .allowedClasses(allowed)
                .build();

        tc.executeAOPTestCase("A", "B");

        Field fHosts = JavaAOPTestCaseSettings.class
                .getDeclaredField("hostsAllowedToBeConnectedTo");
        fHosts.setAccessible(true);

        String[] hostsVal = (String[]) fHosts.get(null);

        assertArrayEquals(new String[0], hostsVal);
    }

    @Test
    void testExecuteAOPTestCaseCommand() throws Exception {
        resetSettings();

        Supplier<List<?>> supplier = Collections::emptyList;
        Set<ClassPermission> allowed = Set.of();

        JavaAOPTestCase tc = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.COMMAND_EXECUTION)
                .resourceAccessSupplier(supplier)
                .allowedClasses(allowed)
                .build();

        tc.executeAOPTestCase("X", "Y");

        Field fCmd = JavaAOPTestCaseSettings.class
                .getDeclaredField("commandsAllowedToBeExecuted");
        fCmd.setAccessible(true);

        String[] cmdVal = (String[]) fCmd.get(null);

        assertArrayEquals(new String[0], cmdVal);
    }

    @Test
    void testExecuteAOPTestCaseThread() throws Exception {
        resetSettings();

        Supplier<List<?>> supplier = Collections::emptyList;
        Set<ClassPermission> allowed = Set.of();

        JavaAOPTestCase tc = JavaAOPTestCase.builder()
                .javaAOPTestCaseSupported(JavaAOPTestCaseSupported.THREAD_CREATION)
                .resourceAccessSupplier(supplier)
                .allowedClasses(allowed)
                .build();

        tc.executeAOPTestCase("X", "Y");

        Field fNum = JavaAOPTestCaseSettings.class
                .getDeclaredField("threadNumberAllowedToBeCreated");
        fNum.setAccessible(true);

        int[] numVal = (int[]) fNum.get(null);

        assertArrayEquals(new int[0], numVal);
    }

    // Helper to invoke private static generateAdviceSettingValue(String, String, Object)
    private String invokeGenerate(
            String dataType,
            String adviceSetting,
            Object value
    ) {
        try {
            Method m = JavaAOPTestCase.class.getDeclaredMethod(
                    "generateAdviceSettingValue",
                    String.class,
                    String.class,
                    Object.class
            );
            m.setAccessible(true);
            return (String) m.invoke(null, dataType, adviceSetting, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to invoke private static generateAdviceSettingValue(JavaAOPAdviceSettingTriple)
    private String invokeGenerate(
            JavaAOPAdviceSettingTriple triple
    ) {
        try {
            Method m = JavaAOPTestCase.class.getDeclaredMethod(
                    "generateAdviceSettingValue",
                    JavaAOPAdviceSettingTriple.class
            );
            m.setAccessible(true);
            return (String) m.invoke(null, triple);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
