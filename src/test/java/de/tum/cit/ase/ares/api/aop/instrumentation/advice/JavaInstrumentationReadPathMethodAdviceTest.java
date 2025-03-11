package de.tum.cit.ase.ares.api.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class JavaInstrumentationReadPathMethodAdviceTest {

    private static final String OPERATION = "read";
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_SIGNATURE = "methodSignature";
    private static final Object[] ATTRIBUTES = new Object[]{"attrib1", "attrib2"};
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};
    private static final Object INSTANCE = new Object() {
        public final String attrib1 = "attrib1";
        public final String attrib2 = "attrib2";
    };

    @Test
    void testOnEnter() throws Exception {

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
            JavaInstrumentationReadPathMethodAdvice.onEnter(
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