package de.tum.cit.ase.ares.api.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class JavaInstrumentationOverwritePathMethodAdviceTest {

    private static final String OPERATION = "overwrite";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathMethodAdvice";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_SIGNATURE = "methodSignature";
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};

    @Test
    void testOnEnter() throws Exception {
        class MockClass {
            private final String field1 = "value1";
            private final int field2 = 42;
        }

        MockClass mockInstance = new MockClass();
        Field[] fields = mockInstance.getClass().getDeclaredFields();
        Object[] attributes = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            attributes[i] = fields[i].get(mockInstance);
        }

        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            // Arrange
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq(OPERATION),
                    eq(CLASS_NAME),
                    eq(METHOD_NAME),
                    eq("methodSignature"),
                    aryEq(attributes),
                    aryEq(PARAMETERS)
            )).thenAnswer(invocation -> null);

            // Act
            JavaInstrumentationOverwritePathMethodAdvice.onEnter(
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    mockInstance,
                    PARAMETERS
            );

            // Assert
            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq(OPERATION),
                    eq(CLASS_NAME),
                    eq(METHOD_NAME),
                    eq(METHOD_SIGNATURE),
                    aryEq(attributes),
                    aryEq(PARAMETERS)
            ));
        }
    }
}