package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

class JavaInstrumentationExecutePathMethodAdviceTest {

    @Test
    void testOnEnter() throws IllegalAccessException {
        Object mockInstance = new Object() {
            public final String field1 = "value1";
            public final int field2 = 42;
        };

        Field[] fields = mockInstance.getClass().getDeclaredFields();
        Object[] attributes = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            attributes[i] = fields[i].get(mockInstance);
        }

        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "execute",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice",
                    "methodName",
                    "methodSignature",
                    attributes,
                    new Object[]{"param1", "param2"}
            )).thenAnswer(invocation -> null);

            JavaInstrumentationExecutePathMethodAdvice.onEnter(
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice",
                    "methodName",
                    "methodSignature",
                    mockInstance,
                    "param1", "param2"
            );

            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "execute",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice",
                    "methodName",
                    "methodSignature",
                    attributes,
                    new Object[]{"param1", "param2"}
            ));
        }
    }
}