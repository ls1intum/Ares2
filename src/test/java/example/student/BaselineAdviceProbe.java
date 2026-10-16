package example.student;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

/**
 * Enters the real file-system check of either backend from a supervised frame,
 * for any intercepted method, so both backends judge exactly the same call.
 */
public final class BaselineAdviceProbe {

	/**
	 * One intercepted call, described the way both backends see it.
	 *
	 * @param owner          the class declaring the method
	 * @param method         the method name, {@code <init>} for a constructor
	 * @param descriptor     the method's JVM descriptor, as instrumentation reads
	 *                       it
	 * @param parameterTypes the declared parameter types, as AspectJ reads them
	 * @param parameters     the arguments
	 * @param instance       the receiver, or {@code null}
	 */
	public record Call(Class<?> owner, String method, String descriptor, Class<?>[] parameterTypes, Object[] parameters,
			Object instance) {
	}

	/** Prevents creating probe instances. */
	private BaselineAdviceProbe() {
	}

	/**
	 * Runs one call through the check of the given backend.
	 *
	 * @param mode   the backend
	 * @param action the file-system action the call performs
	 * @param call   the call
	 * @throws SecurityException if the backend denies the call
	 */
	public static void check(AOPMode mode, String action, Call call) {
		if (mode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(action, call.owner().getName(),
					call.method(), call.descriptor(), fieldsOf(call.instance()), call.parameters(), call.instance());
		} else {
			JavaAspectJFileSystemAdviceDefinitions.aspectOf().checkFileSystemInteraction(action, joinPointOf(call));
		}
	}

	/**
	 * Builds the join point AspectJ's woven call site would pass.
	 *
	 * @param call the call
	 * @return a method-call or constructor-call join point
	 */
	private static JoinPoint joinPointOf(Call call) {
		Factory factory = new Factory("BaselineAdviceProbe.java", BaselineAdviceProbe.class);
		String[] names = new String[call.parameterTypes().length];
		boolean constructor = "<init>".equals(call.method());
		Signature signature = constructor
				? factory.makeConstructorSig(Modifier.PUBLIC, call.owner(), call.parameterTypes(), names,
						new Class<?>[0])
				: factory.makeMethodSig(Modifier.PUBLIC, call.method(), call.owner(), call.parameterTypes(), names,
						new Class<?>[0], void.class);
		String kind = constructor ? JoinPoint.CONSTRUCTOR_CALL : JoinPoint.METHOD_CALL;
		return Factory.makeJP(factory.makeSJP(kind, signature, 1), null, call.instance(), call.parameters());
	}

	/**
	 * Reads the receiver's fields the way the instrumentation advice does, skipping
	 * a field it cannot read.
	 *
	 * @param instance the receiver, or {@code null}
	 * @return the field values, empty without a receiver
	 */
	private static Object[] fieldsOf(Object instance) {
		if (instance == null) {
			return new Object[0];
		}
		Field[] fields = instance.getClass().getDeclaredFields();
		Object[] values = new Object[fields.length];
		for (int i = 0; i < fields.length; i++) {
			values[i] = valueOf(fields[i], instance);
		}
		return values;
	}

	/**
	 * Reads one field, or gives {@code null} when the JDK does not allow it.
	 *
	 * @param field    the field
	 * @param instance the object holding it
	 * @return its value, or {@code null}
	 */
	private static Object valueOf(Field field, Object instance) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (InaccessibleObjectException | IllegalAccessException | SecurityException exception) {
			return null;
		}
	}
}
