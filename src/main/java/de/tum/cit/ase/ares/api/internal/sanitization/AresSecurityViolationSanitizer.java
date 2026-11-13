package de.tum.cit.ase.ares.api.internal.sanitization;

/**
 * Reports a blocked action that Ares's own checks raised as the plain
 * {@link SecurityException} it extends, with the same message, cause and stack.
 * Ares's exception for a blocked action cannot be copied like other exceptions,
 * so without this the report would wrap it as an unexpected error.
 */
enum AresSecurityViolationSanitizer implements SpecificThrowableSanitizer {
	/** The only instance. */
	INSTANCE;

	/** The class name every copy of Ares's blocked-action exception has. */
	private static final String SIMPLE_NAME = "AresSecurityViolation"; //$NON-NLS-1$

	/** The package every copy lives below, which student code cannot use. */
	private static final String ARES_PACKAGE = "de.tum.cit.ase.ares.api."; //$NON-NLS-1$

	/**
	 * Says whether the failure is one of Ares's own blocked-action exceptions.
	 *
	 * @param t the failure.
	 * @return whether this sanitiser handles it.
	 */
	@Override
	public boolean canSanitize(Throwable t) {
		Class<? extends Throwable> type = t.getClass();
		return t instanceof SecurityException && type.getName().startsWith(ARES_PACKAGE)
				&& SIMPLE_NAME.equals(type.getSimpleName());
	}

	/**
	 * Returns a plain {@link SecurityException} with the failure's message, cause
	 * and stack, each sanitised.
	 *
	 * @param t                  the failure.
	 * @param messageTransformer what to do with the message.
	 * @return the failure to report.
	 */
	@Override
	public Throwable sanitize(Throwable t, MessageTransformer messageTransformer) {
		var info = ThrowableInfo.getEssentialInfosSafeFrom(t);
		info.sanitize();
		SecurityException plain = new SecurityException(messageTransformer.apply(info));
		SanitizationUtils.copyThrowableInfoSafe(info, plain);
		return plain;
	}
}
