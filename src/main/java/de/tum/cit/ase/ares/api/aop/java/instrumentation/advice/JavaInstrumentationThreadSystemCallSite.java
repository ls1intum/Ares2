package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Call-site replacements for final monitor methods inherited from
 * {@link Object}. Byte Buddy cannot add advice to these methods by transforming
 * {@link Thread}, so the instrumentation agent rewrites application call sites
 * to these semantically equivalent wrappers.
 */
public final class JavaInstrumentationThreadSystemCallSite {
	private static final ReferenceQueue<Thread> ALLOWED_THREAD_CLASSES_QUEUE = new ReferenceQueue<>();
	private static final Map<WeakReference<Thread>, String> ALLOWED_THREAD_CLASSES = new HashMap<>();

	private JavaInstrumentationThreadSystemCallSite() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationThreadSystemCallSite"));
	}

	public static void notify(Object receiver) {
		checkMonitorInteraction(receiver, "notify", "()V", new Object[0]);
		receiver.notify();
	}

	public static void start(Thread receiver) {
		JavaInstrumentationAdviceThreadSystemToolbox.recordThreadClassBeforeStart(receiver);
		receiver.start();
	}

	public static void notifyAll(Object receiver) {
		checkMonitorInteraction(receiver, "notifyAll", "()V", new Object[0]);
		receiver.notifyAll();
	}

	public static void wait(Object receiver) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "()V", new Object[0]);
		receiver.wait();
	}

	public static void wait(Object receiver, long timeout) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "(J)V", new Object[] { timeout });
		receiver.wait(timeout);
	}

	public static void wait(Object receiver, long timeout, int nanos) throws InterruptedException {
		checkMonitorInteraction(receiver, "wait", "(JI)V", new Object[] { timeout, nanos });
		receiver.wait(timeout, nanos);
	}

	private static void checkMonitorInteraction(Object receiver, String methodName, String methodSignature,
			Object[] parameters) {
		if (receiver instanceof Thread) {
			JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("manipulate",
					Object.class.getName(), methodName, methodSignature, new Object[0], parameters, receiver);
		}
	}

	static String getRecordedThreadClass(Thread thread) {
		synchronized (ALLOWED_THREAD_CLASSES) {
			removeClearedThreadEntries();
			for (Map.Entry<WeakReference<Thread>, String> entry : ALLOWED_THREAD_CLASSES.entrySet()) {
				if (entry.getKey().get() == thread) {
					return entry.getValue();
				}
			}
			return null;
		}
	}

	static void recordAllowedThread(Thread thread, String threadClassName) {
		if (threadClassName != null) {
			synchronized (ALLOWED_THREAD_CLASSES) {
				removeClearedThreadEntries();
				Iterator<WeakReference<Thread>> iterator = ALLOWED_THREAD_CLASSES.keySet().iterator();
				while (iterator.hasNext()) {
					if (iterator.next().get() == thread) {
						iterator.remove();
					}
				}
				ALLOWED_THREAD_CLASSES.put(new WeakReference<>(thread, ALLOWED_THREAD_CLASSES_QUEUE), threadClassName);
			}
		}
	}

	private static void removeClearedThreadEntries() {
		Reference<? extends Thread> clearedReference;
		while ((clearedReference = ALLOWED_THREAD_CLASSES_QUEUE.poll()) != null) {
			ALLOWED_THREAD_CLASSES.remove(clearedReference);
		}
	}
}
