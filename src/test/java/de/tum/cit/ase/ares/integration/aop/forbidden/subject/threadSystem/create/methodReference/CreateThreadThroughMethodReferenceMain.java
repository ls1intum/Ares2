package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.methodReference;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

/** Student code that starts a thread through a method reference. */
public final class CreateThreadThroughMethodReferenceMain {

	/** Prevents creating instances of this utility class. */
	private CreateThreadThroughMethodReferenceMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Starts a thread through a method reference to {@code Thread.start}, whose
	 * call the JDK places in a class it generates itself.
	 */
	public static void startThreadThroughMethodReference() {
		Runnable starter = new Thread(new IllegalThread())::start;
		starter.run();
	}
}
