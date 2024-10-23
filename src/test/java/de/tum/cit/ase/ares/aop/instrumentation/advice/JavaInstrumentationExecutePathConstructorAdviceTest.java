package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationExecutePathConstructorAdviceTest {

    @Test
    void testOnEnter() {
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "execute",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice",
                    "<init>",
                    "",
                    new Object[0],
                    new Object[]{"param1", "param2"}
            )).thenAnswer(invocation -> null);

            JavaInstrumentationExecutePathConstructorAdvice.onEnter(
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice",
                    "param1", "param2"
            );

            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    "execute",
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice",
                    "<init>",
                    "",
                    new Object[0],
                    new Object[]{"param1", "param2"}
            ));
        }
    }
}