package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

/**
 * Records whether the agent really put its check into the JDK method
 * {@code Runtime.loadLibrary0}. Only then may the public {@code loadLibrary}
 * checks leave the decision to it. The record is open only while the agent
 * installs its transformers and is sealed for good afterwards. It assumes no
 * exercise code runs during that installation. The class is public only so the
 * agent's list of injected classes can name it.
 */
public final class JavaInstrumentationLibraryLoadWeaveRecord {

	/**
	 * The record the checks read. The JVM's own loader holds one copy and the
	 * application loader another; only the agent changes the first, by reflection,
	 * which is why the methods below need no wider visibility.
	 */
	static final JavaInstrumentationLibraryLoadWeaveRecord INSTALLATION = new JavaInstrumentationLibraryLoadWeaveRecord();

	/** The class whose method the check must be woven into. */
	static final String DECLARING_TYPE = "java.lang.Runtime";

	/** The name of that method. */
	static final String METHOD_NAME = "loadLibrary0";

	/**
	 * The JVM descriptor of that method: {@code (Class, String)} returning void.
	 */
	static final String METHOD_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;)V";

	/** Whether the agent is installing its transformers right now. */
	private boolean open;

	/** Whether the check was woven into exactly that method during this window. */
	private boolean marked;

	/** Whether the record is final; once true, nothing changes it again. */
	private boolean sealed;

	/**
	 * Creates an empty record that is neither open nor sealed.
	 */
	JavaInstrumentationLibraryLoadWeaveRecord() {
		this.open = false;
		this.marked = false;
		this.sealed = false;
	}

	/**
	 * Starts the installation window and forgets any earlier mark, so a mark made
	 * before the installation counts for nothing. Does nothing once sealed.
	 */
	synchronized void open() {
		if (sealed) {
			return;
		}
		marked = false;
		open = true;
	}

	/**
	 * Notes that the check was woven into a method. It counts only while the window
	 * is open, and only for {@code java.lang.Runtime.loadLibrary0(Class, String)}
	 * of the JVM's own loader.
	 *
	 * @param declaringType the class the check was woven into
	 * @param methodName    the method the check was woven into
	 * @param descriptor    the JVM descriptor of that method
	 * @param loaderIsBoot  whether the JVM's own loader defined that class
	 */
	synchronized void mark(String declaringType, String methodName, String descriptor, boolean loaderIsBoot) {
		if (open && !sealed && loaderIsBoot && DECLARING_TYPE.equals(declaringType) && METHOD_NAME.equals(methodName)
				&& METHOD_DESCRIPTOR.equals(descriptor)) {
			marked = true;
		}
	}

	/**
	 * Closes the window for good. After this, {@link #open()} and
	 * {@link #mark(String, String, String, boolean)} have no effect.
	 */
	synchronized void seal() {
		open = false;
		sealed = true;
	}

	/**
	 * Tells whether the record is sealed and holds a mark.
	 *
	 * @return {@code true} only if the check was woven during the sealed window
	 */
	synchronized boolean isWoven() {
		return sealed && marked;
	}
}
