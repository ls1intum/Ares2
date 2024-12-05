package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class JavaInstrumentationDeletePathMethodAdviceTest {

    @Test
    void testOnEnter() {
class JavaInstrumentationDeletePathMethodAdviceTest {
    private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_SIGNATURE = "methodSignature";
    private static final String OPERATION = "delete";
    private static final String TEST_PATH = "/test/file/path";
    private static final Object[] TEST_CONTEXT = new Object[]{"context"};

    @Test
    void shouldCheckFileSystemInteraction_whenDeletingPath() {
        // given
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            // Expected result from the toolbox
            SecurityViolation expectedViolation = null;
            mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    OPERATION,
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    TEST_CONTEXT,
                    new Object[]{TEST_PATH}
            )).thenReturn(expectedViolation);

            // when
            JavaInstrumentationDeletePathMethodAdvice.onEnter(
                    CLASS_NAME,
                    METHOD_NAME,
                    METHOD_SIGNATURE,
                    TEST_CONTEXT,
                    TEST_PATH
            );

            // then
            mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                    eq(OPERATION),
                    eq(CLASS_NAME),
                    eq(METHOD_NAME),
                    eq(METHOD_SIGNATURE),
                    eq(TEST_CONTEXT),
                    eq(new Object[]{TEST_PATH})
            ));
        }
    }
}