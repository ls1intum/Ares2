package example.student;

import java.lang.reflect.Modifier;
import java.net.Socket;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJCommandSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJNetworkSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJThreadSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceCommandSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;

/**
 * Enters the real command, network and thread checks of either backend from a
 * supervised frame, so both backends judge exactly the same call and their
 * denial sentences can be compared. Each method names the call it stands for.
 */
public final class DenialSentenceProbe {

	/** Prevents creating probe instances. */
	private DenialSentenceProbe() {
	}

	/**
	 * Judges {@code Runtime.exec(String[])} with the given command line.
	 *
	 * @param mode        the backend
	 * @param commandLine the command and its arguments
	 * @throws SecurityException if the backend denies the call
	 */
	public static void runtimeExec(AOPMode mode, String... commandLine) {
		Object[] parameters = { commandLine };
		if (mode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAdviceCommandSystemToolbox.checkCommandSystemInteraction("execute", "java.lang.Runtime",
					"exec", "([Ljava.lang.String;)", null, parameters, null);
		} else {
			JavaAspectJCommandSystemAdviceDefinitions.checkCommandSystemInteraction("execute",
					methodCall(Runtime.class, "exec", new Class<?>[] { String[].class }, parameters));
		}
	}

	/**
	 * Judges {@code new Socket(String, int)} with the given host and port.
	 *
	 * @param mode the backend
	 * @param host the host
	 * @param port the port
	 * @throws SecurityException if the backend denies the call
	 */
	public static void socketConnect(AOPMode mode, String host, int port) {
		Object[] parameters = { host, port };
		if (mode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("connect", "java.net.Socket",
					"<init>", "(java.lang.String,int)", null, parameters, null);
		} else {
			JavaAspectJNetworkSystemAdviceDefinitions.checkNetworkSystemInteraction("connect",
					constructorCall(Socket.class, new Class<?>[] { String.class, int.class }, parameters));
		}
	}

	/**
	 * Judges {@code new Thread(Runnable)} with a lambda written in this method, so
	 * the denial sentence can name it as a lambda in this method.
	 *
	 * @param mode the backend
	 * @throws SecurityException if the backend denies the call
	 */
	public static void threadWithLambda(AOPMode mode) {
		Runnable task = () -> {
		};
		Object[] parameters = { task };
		if (mode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("create", "java.lang.Thread",
					"<init>", "(java.lang.Runnable)", new Object[0], parameters, null);
		} else {
			JavaAspectJThreadSystemAdviceDefinitions.aspectOf().checkThreadSystemInteraction("create",
					constructorCall(Thread.class, new Class<?>[] { Runnable.class }, parameters), true);
		}
	}

	/**
	 * Builds the join point of a woven static method call.
	 *
	 * @param owner          the declaring class
	 * @param method         the method name
	 * @param parameterTypes the declared parameter types
	 * @param parameters     the arguments
	 * @return the join point
	 */
	private static JoinPoint methodCall(Class<?> owner, String method, Class<?>[] parameterTypes, Object[] parameters) {
		Factory factory = new Factory("DenialSentenceProbe.java", DenialSentenceProbe.class);
		Signature signature = factory.makeMethodSig(Modifier.PUBLIC, method, owner, parameterTypes,
				new String[parameterTypes.length], new Class<?>[0], Object.class);
		return Factory.makeJP(factory.makeSJP(JoinPoint.METHOD_CALL, signature, 1), null, null, parameters);
	}

	/**
	 * Builds the join point of a woven constructor call.
	 *
	 * @param owner          the class being constructed
	 * @param parameterTypes the declared parameter types
	 * @param parameters     the arguments
	 * @return the join point
	 */
	private static JoinPoint constructorCall(Class<?> owner, Class<?>[] parameterTypes, Object[] parameters) {
		Factory factory = new Factory("DenialSentenceProbe.java", DenialSentenceProbe.class);
		Signature signature = factory.makeConstructorSig(Modifier.PUBLIC, owner, parameterTypes,
				new String[parameterTypes.length], new Class<?>[0]);
		return Factory.makeJP(factory.makeSJP(JoinPoint.CONSTRUCTOR_CALL, signature, 1), null, null, parameters);
	}
}
