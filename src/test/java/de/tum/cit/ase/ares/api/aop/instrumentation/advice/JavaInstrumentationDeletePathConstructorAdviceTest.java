package de.tum.cit.ase.ares.api.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationDeletePathConstructorAdviceTest {

    private static final String OPERATION = "delete";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice";
    private static final String METHOD_NAME = "<init>";
    private static final String METHOD_SIGNATURE = "";
    private static final Object[] ATTRIBUTES = new Object[0];
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};

    @Test
    void testOnEnterVerifiesFileSystemInteractionForDelete() {
        // Arrange
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    OPERATION,
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    ATTRIBUTES,
                    PARAMETERS
            )).thenAnswer(invocation -> null);

            // Act
            JavaInstrumentationDeletePathConstructorAdvice.onEnter(
                    CLASS_NAME,
                    PARAMETERS
            );

            // Assert
            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
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