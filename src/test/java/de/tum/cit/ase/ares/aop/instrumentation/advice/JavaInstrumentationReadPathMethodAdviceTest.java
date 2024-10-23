package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class JavaInstrumentationReadPathMethodAdviceTest {

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
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq("read"),
                    eq("de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice"),
                    eq("methodName"),
                    eq("methodSignature"),
                    aryEq(attributes),
                    aryEq(new Object[]{"param1", "param2"})
            )).thenAnswer(invocation -> null);

            JavaInstrumentationReadPathMethodAdvice.onEnter(
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice",
                    "methodName",
                    "methodSignature",
                    mockInstance,
                    "param1", "param2"
            );

            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq("read"),
                    eq("de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice"),
                    eq("methodName"),
                    eq("methodSignature"),
                    aryEq(attributes),
                    aryEq(new Object[]{"param1", "param2"})
            ));
        }
    }
}