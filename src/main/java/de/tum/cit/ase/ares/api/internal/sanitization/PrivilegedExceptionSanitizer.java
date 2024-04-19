package de.tum.cit.ase.ares.api.internal.sanitization;

import de.tum.cit.ase.ares.api.internal.PrivilegedException;

enum PrivilegedExceptionSanitizer implements SpecificThrowableSanitizer {
	INSTANCE;

	@Override
	public boolean canSanitize(Throwable t) {
		return t instanceof PrivilegedException;
	}

	@Override
	public Throwable sanitize(Throwable t, MessageTransformer messageTransformer) {
		// returning only the content of the privileged exception is the purpose here
		return ThrowableSanitizer.sanitize(((PrivilegedException) t).getPriviledgedThrowable(), messageTransformer);
	}
}
