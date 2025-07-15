package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationExecutePathConstructorAdviceTest {

    private static final String OPERATION = "execute";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice";
    private static final String METHOD_NAME = "<init>";
    private static final String METHOD_SIGNATURE = "";
    private static final Object[] ATTRIBUTES = new Object[0];
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};

    @Test
    void testOnEnter() {
        try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceFileSystemToolbox.class)) {
            // Arrange
            mockedToolbox.when(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                    OPERATION,
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    ATTRIBUTES,
                    PARAMETERS
            )).thenAnswer(invocation -> null);

            // Act
            JavaInstrumentationExecutePathConstructorAdvice.onEnter(
                    CLASS_NAME,
                    PARAMETERS
            );

            // Assert
            mockedToolbox.verify(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                    OPERATION,
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    ATTRIBUTES,
                    PARAMETERS
            ));
        }
    }
}