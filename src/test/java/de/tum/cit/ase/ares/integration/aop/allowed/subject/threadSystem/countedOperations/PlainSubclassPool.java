package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A pool the student wrote that changes nothing about how tasks are wrapped.
 */
public final class PlainSubclassPool extends ThreadPoolExecutor {

	/**
	 * Builds a pool of two threads.
	 */
	public PlainSubclassPool() {
		super(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
	}
}
