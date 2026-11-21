package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Checks that the advice for questions about a file hands each call to the file
 * system toolbox as an inspection, with the receiver but without its fields.
 */
class JavaInstrumentationInspectPathMethodAdviceTest {

	/**
	 * The class the call is declared in.
	 */
	private static final String CLASS_NAME = "java.io.File";

	/**
	 * The method called.
	 */
	private static final String METHOD_NAME = "exists";

	/**
	 * The method's parameter list.
	 */
	private static final String METHOD_SIGNATURE = "()";

	/**
	 * The call's arguments.
	 */
	private static final Object[] PARAMETERS = new Object[] { "argument" };

	/**
	 * The receiver of the call.
	 */
	private static final Object INSTANCE = new Object();

	/**
	 * The advice passes the action "inspect", the receiver and the arguments, and
	 * no receiver fields.
	 */
	@Test
	void testOnEnterChecksAnInspection() {
		try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(
				JavaInstrumentationAdviceFileSystemToolbox.class)) {
			JavaInstrumentationInspectPathMethodAdvice.onEnter(CLASS_NAME, METHOD_NAME, METHOD_SIGNATURE, INSTANCE,
					PARAMETERS);
			mockedToolbox
					.verify(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(eq("inspect"),
							eq(CLASS_NAME), eq(METHOD_NAME), eq(METHOD_SIGNATURE),
							argThat(attributes -> attributes.length == 0), same(PARAMETERS), same(INSTANCE)));
		}
	}
}
