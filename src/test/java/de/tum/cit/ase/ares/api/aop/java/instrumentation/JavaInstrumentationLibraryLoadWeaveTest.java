package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationLibraryLoadWeaveRecord;

/**
 * Activates the real agent of this test JVM and checks that installing it
 * marked and sealed the weave record the JDK's copy of the advice reads. It
 * leaves the agent activated, as the escape check does.
 */
class JavaInstrumentationLibraryLoadWeaveTest {

	/**
	 * After activation the record held by the JVM's own loader says the check was
	 * woven into {@code Runtime.loadLibrary0}, and a later attempt to wipe it is
	 * ignored. The application loader's copy, which no woven JDK method reads, was
	 * never opened.
	 *
	 * @throws ReflectiveOperationException if a record cannot be read
	 */
	@Test
	void installingMarksAndSealsTheJdkRecord() throws ReflectiveOperationException {
		JavaInstrumentationAgent.activate();

		Class<?> bootRecord = Class.forName(JavaInstrumentationLibraryLoadWeaveRecord.class.getName(), true, null);
		assertThat(bootRecord).isNotSameAs(JavaInstrumentationLibraryLoadWeaveRecord.class);
		assertThat(JavaInstrumentationAgent.wasTransformed(Runtime.class)).isTrue();
		assertThat(call(bootRecord, "isWoven")).isEqualTo(Boolean.TRUE);

		call(bootRecord, "open");
		assertThat(call(bootRecord, "isWoven")).isEqualTo(Boolean.TRUE);
		assertThat(call(JavaInstrumentationLibraryLoadWeaveRecord.class, "isWoven")).isEqualTo(Boolean.FALSE);
	}

	/**
	 * Calls a method without arguments on the installation record of one copy of
	 * the record class.
	 *
	 * @param recordClass the copy
	 * @param methodName  the method
	 * @return what it returned
	 * @throws ReflectiveOperationException if it cannot be called
	 */
	private static Object call(Class<?> recordClass, String methodName) throws ReflectiveOperationException {
		Field installation = recordClass.getDeclaredField("INSTALLATION");
		installation.setAccessible(true);
		Method method = recordClass.getDeclaredMethod(methodName);
		method.setAccessible(true);
		return method.invoke(installation.get(null));
	}
}
