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
    private static final Object[] ATTRIBUTES = new Object[]{"attrib1", "attrib2"};
    private static final Object[] PARAMETERS = new Object[]{"param1", "param2"};
    private static final Object INSTANCE = new Object() {
        public final String attrib1 = "attrib1";
        public final String attrib2 = "attrib2";
    };

    @Test
    void testOnEnter() throws Exception {

        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            // Arrange
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    OPERATION,
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    ATTRIBUTES,
                    PARAMETERS
            )).thenAnswer(invocation -> null);

            // Act
            JavaInstrumentationOverwritePathMethodAdvice.onEnter(
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    INSTANCE,
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