package de.tum.cit.ase.ares.api.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class JavaInstrumentationReadPathConstructorAdviceTest {

    private static final String OPERATION = "read";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice";
    private static final String METHOD_NAME = "<init>";
    private static final String METHOD_SIGNATURE = "";
    private static final Object[] ATTRIBUTES = new Object[0];
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};

    @Test
    void testOnEnter() {
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq(OPERATION),
                    eq(CLASS_NAME),
                    eq(METHOD_NAME),
                    eq(METHOD_SIGNATURE),
                    aryEq(ATTRIBUTES),
                    aryEq(PARAMETERS)
            )).thenAnswer(invocation -> null);

            JavaInstrumentationReadPathConstructorAdvice.onEnter(
                    CLASS_NAME,
                    PARAMETERS
            );

            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq(OPERATION),
                    eq(CLASS_NAME),
                    eq(METHOD_NAME),
                    eq(METHOD_SIGNATURE),
                    aryEq(ATTRIBUTES),
                    aryEq(PARAMETERS)
            ));
        }
    }
}
