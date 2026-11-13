package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

/**
 * The error Ares throws when it blocks an action of student code. Besides the
 * full message it keeps the kind, action and target separately, so the report
 * can show less, or add the policy entry that would allow the action. Only
 * Ares's own checks in this package can create one; any other caller receives a
 * plain {@link SecurityException}. Each checking package holds its own copy,
 * because generated exercises copy each package separately.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public final class AresSecurityViolation extends SecurityException {

	/** Serialisation version. */
	private static final long serialVersionUID = 1L;

	/** What was reached for: file, network, command, thread or architecture. */
	private final String kind;

	/** What was attempted, such as read or create. */
	private final String action;

	/** What the action targeted, such as a path or host. */
	private final String target;

	/**
	 * Creates a violation; only {@link #create} calls this.
	 *
	 * @param kind    what was reached for.
	 * @param action  what was attempted.
	 * @param target  what the action targeted.
	 * @param message the full message.
	 */
	private AresSecurityViolation(String kind, String action, String target, String message) {
		super(message);
		this.kind = kind;
		this.action = action;
		this.target = target;
	}

	/**
	 * Creates a violation when called by a class of this package, which only Ares
	 * can define; any other caller receives a plain {@link SecurityException}
	 * carrying the same message, so a forged one gains nothing.
	 *
	 * @param kind    what was reached for.
	 * @param action  what was attempted.
	 * @param target  what the action targeted.
	 * @param message the full message.
	 * @return the exception to throw.
	 */
	public static SecurityException create(String kind, Object action, Object target, String message) {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller == AresSecurityViolation.class || !caller.getPackageName().equals(packageName())
				|| caller.getClassLoader() != AresSecurityViolation.class.getClassLoader()) {
			return new SecurityException(message);
		}
		return new AresSecurityViolation(kind, String.valueOf(action), String.valueOf(target), message);
	}

	/**
	 * Returns this class's package name.
	 *
	 * @return the package name.
	 */
	private static String packageName() {
		return AresSecurityViolation.class.getPackageName();
	}

	/**
	 * Returns what was reached for.
	 *
	 * @return file, network, command, thread or architecture.
	 */
	public String kind() {
		return kind;
	}

	/**
	 * Returns what was attempted.
	 *
	 * @return the action.
	 */
	public String action() {
		return action;
	}

	/**
	 * Returns what the action targeted.
	 *
	 * @return the target.
	 */
	public String target() {
		return target;
	}
}
