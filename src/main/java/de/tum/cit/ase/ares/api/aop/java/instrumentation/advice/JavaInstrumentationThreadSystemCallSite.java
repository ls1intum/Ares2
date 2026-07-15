package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Call-site replacements for final monitor methods inherited from
 * {@link Object}. Byte Buddy cannot add advice to these methods by transforming
 * {@link Thread}, so the instrumentation agent rewrites application call sites
 * to these semantically equivalent wrappers.
 */
public final class JavaInstrumentationThreadSystemCallSite {
	private static final Map<Thread, String> ALLOWED_THREAD_CLASSES = Collections.synchronizedMap(new WeakHashMap<>());

	private JavaInstrumentationThreadSystemCallSite() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationThreadSystemCallSite"));
	}

	public static void notify(Object receiver) {
		checkMonitorInteraction(receiver, "notify", "()V");
		receiver.notify();
	}

	public static void start(Thread receiver) {
		JavaInstrumentationAdviceThreadSystemToolbox.recordThreadClassBeforeStart(receiver);
		receiver.start();
	}

	public static void notifyAll(Object receiver) {
		checkMonitorInteraction(receiver, "notifyAll", "()V");
		receiver.notifyAll();
	}

	public static void wait(Object receiver) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "()V");
		receiver.wait();
	}

	public static void wait(Object receiver, long timeout) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "(J)V");
		receiver.wait(timeout);
	}

	public static void wait(Object receiver, long timeout, int nanos) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "(JI)V");
		receiver.wait(timeout, nanos);
	}

	private static void checkMonitorInteraction(Object receiver, String methodName, String methodSignature) {
		if (receiver instanceof Thread) {
			JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("manipulate",
					Object.class.getName(), methodName, methodSignature, new Object[0], new Object[0], receiver);
		}
	}

	public static String getRecordedThreadClass(Thread thread) {
		return ALLOWED_THREAD_CLASSES.get(thread);
	}

	public static void recordAllowedThread(Thread thread, String threadClassName) {
		if (threadClassName != null) {
			ALLOWED_THREAD_CLASSES.put(thread, threadClassName);
		}
	}
}
