package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class JavaInstrumentationExecutePathMethodAdviceTest {

	private static final String CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice";
	private static final String METHOD_NAME = "methodName";
	private static final String METHOD_SIGNATURE = "methodSignature";
	/** The descriptor handed to the advice, which it must pass on unchanged. */
	private static final String METHOD_DESCRIPTOR = "methodDescriptor";
	private static final Object[] ATTRIBUTES = new Object[] { "attrib1", "attrib2" };
	private static final Object[] PARAMETERS = new Object[] { "param1", "param2" };

	private static final class InstanceWithAttributes {
		public final String attrib1 = "attrib1";
		public final String attrib2 = "attrib2";
	}

	private static final Object INSTANCE = new InstanceWithAttributes();

	@Test
	void testOnEnter() throws IllegalAccessException {
		try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(
				JavaInstrumentationAdviceFileSystemToolbox.class)) {
			// Arrange
			mockedToolbox
					.when(() -> JavaInstrumentationAdviceFileSystemToolbox.checkExecuteInteraction(CLASS_NAME,
							METHOD_NAME, METHOD_SIGNATURE, METHOD_DESCRIPTOR, ATTRIBUTES, PARAMETERS, INSTANCE))
					.thenAnswer(invocation -> null);

			// Act
			JavaInstrumentationExecutePathMethodAdvice.onEnter(CLASS_NAME, METHOD_NAME, METHOD_SIGNATURE,
					METHOD_DESCRIPTOR, INSTANCE, PARAMETERS);

			// Assert
			mockedToolbox.verify(() -> JavaInstrumentationAdviceFileSystemToolbox.checkExecuteInteraction(CLASS_NAME,
					METHOD_NAME, METHOD_SIGNATURE, METHOD_DESCRIPTOR, ATTRIBUTES, PARAMETERS, INSTANCE));
		}
	}
}
