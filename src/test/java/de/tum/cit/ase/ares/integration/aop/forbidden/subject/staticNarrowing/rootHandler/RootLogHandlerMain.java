package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.rootHandler;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * Supervised code that adds a handler to the root logger, which receives what
 * every other part of the program logs.
 */
public final class RootLogHandlerMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private RootLogHandlerMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Adds a console handler to the root logger.
	 */
	public static void addRootHandler() {
		Logger.getLogger("").addHandler(new ConsoleHandler());
	}
}
