package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationOverwritePathConstructorAdviceTest {

    @Test
    void testOnEnter() {
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "overwrite",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice",
                    "<init>",
                    "",
                    new Object[0],
                    new Object[]{"param1", "param2"}
            )).thenAnswer(invocation -> null);

            JavaInstrumentationOverwritePathConstructorAdvice.onEnter(
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice",
                    "param1", "param2"
            );

            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "overwrite",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice",
                    "<init>",
                    "",
                    new Object[0],
                    new Object[]{"param1", "param2"}
            ));
        }
    }
}