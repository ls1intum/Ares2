package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class JavaInstrumentationReadPathConstructorAdviceTest {

    @Test
    void testOnEnter() {
        class JavaInstrumentationReadPathConstructorAdviceTest {
            private static final String OPERATION_TYPE = "read";
            private static final String TEST_CLASS_NAME = 
                    "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice";
            private static final String CONSTRUCTOR_NAME = "<init>";
            private static final String EMPTY_METHOD_DESC = "";
            private static final Object[] TEST_PARAMS = new Object[]{"param1", "param2"};
            private static final Object[] EMPTY_ARGS = new Object[0];

            @Test
            void testOnEnter() {
                try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
                    mockedToolbox.when(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                            eq(OPERATION_TYPE),
                            eq(TEST_CLASS_NAME),
                            eq(CONSTRUCTOR_NAME),
                            eq(EMPTY_METHOD_DESC),
                            aryEq(EMPTY_ARGS),
                            aryEq(TEST_PARAMS)
                    )).thenAnswer(invocation -> null);

                    JavaInstrumentationReadPathConstructorAdvice.onEnter(
                            TEST_CLASS_NAME,
                            TEST_PARAMS[0].toString(), TEST_PARAMS[1].toString()
                    );

                    mockedToolbox.verify(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                            eq(OPERATION_TYPE),
                            eq(TEST_CLASS_NAME),
                            eq(CONSTRUCTOR_NAME),
                            eq(EMPTY_METHOD_DESC),
                            aryEq(EMPTY_ARGS),
                            aryEq(TEST_PARAMS)
                    ));
                }
            }
        }
}
