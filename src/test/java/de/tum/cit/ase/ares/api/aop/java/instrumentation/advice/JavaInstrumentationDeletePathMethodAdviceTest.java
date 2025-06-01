package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationDeletePathMethodAdviceTest {

    private static final String OPERATION = "delete";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_SIGNATURE = "methodSignature";
    private static final Object[] ATTRIBUTES = new Object[]{"attrib1", "attrib2"};
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};
    private static final Object INSTANCE = new Object() {
        public final String attrib1 = "attrib1";
        public final String attrib2 = "attrib2";
    };

    @Test
    void shouldCheckFileSystemInteraction_whenDeletingPath() {
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
            JavaInstrumentationDeletePathMethodAdvice.onEnter(
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    INSTANCE,
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